package com.shinesolutions.aemorchestrator;

import static org.assertj.core.api.Assertions.assertThat;
import static org.awaitility.Awaitility.await;

import com.shinesolutions.aemorchestrator.FakeAws.Ec2Instance;
import com.shinesolutions.aemorchestrator.FakeAws.Snapshot;
import com.shinesolutions.aemorchestrator.service.OrchestratorMessageListener;
import com.shinesolutions.aemorchestrator.service.ResourceReadyChecker;
import java.io.IOException;
import java.io.InputStream;
import java.io.UncheckedIOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.time.Instant;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.config.BeanPostProcessor;
import org.springframework.boot.SpringBootVersion;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.context.annotation.Bean;
import org.springframework.core.SpringVersion;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import software.amazon.awssdk.services.cloudwatch.model.PutMetricAlarmRequest;
import software.amazon.awssdk.services.ec2.model.InstanceStateName;
import tools.jackson.databind.ObjectMapper;

/**
 * End-to-end test of the orchestrator: the full Spring context starts with only the AWS SDK clients
 * swapped for in-memory fakes ({@link FakeAws}) and AEM Author served by a local HTTP stub ({@link
 * FakeAem}). Each scenario puts a real-shaped SNS notification on the SQS queue and asserts the
 * resulting AWS state, AEM agent API calls, and whether the message was acknowledged (deleted).
 */
@SpringBootTest(
    classes = AemOrchestrator.class,
    webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT,
    properties = {
      "aws.region=ap-southeast-2",
      "aws.sqs.queueName=" + AemOrchestratorIT.QUEUE_NAME,
      "aws.cloudformation.stackName.publishDispatcher="
          + AemOrchestratorIT.PUBLISH_DISPATCHER_STACK,
      "aws.cloudformation.stackName.publish=" + AemOrchestratorIT.PUBLISH_STACK,
      "aws.cloudformation.stackName.previewPublishDispatcher="
          + AemOrchestratorIT.PREVIEW_PUBLISH_DISPATCHER_STACK,
      "aws.cloudformation.stackName.previewPublish=" + AemOrchestratorIT.PREVIEW_PUBLISH_STACK,
      "aws.cloudformation.stackName.authorDispatcher=" + AemOrchestratorIT.AUTHOR_DISPATCHER_STACK,
      "aws.cloudformation.stackName.author=" + AemOrchestratorIT.AUTHOR_STACK,
      "aws.cloudformation.stackName.messaging=" + AemOrchestratorIT.MESSAGING_STACK,
      "aem.credentials.s3.use=true",
      "aem.credentials.s3.file.uri=s3://" + AemOrchestratorIT.CREDENTIALS_S3_PATH,
      "aem.protocol.author=http",
      "startup.waitForAuthorElb.maxAttempts=3",
      "startup.waitForAuthorElb.backOffPeriod=100",
      "startup.waitForAuthorElb.maxBackOffPeriod=200",
      "startup.waitForAuthorElb.backOffPeriodMultiplier=1"
    })
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
class AemOrchestratorIT {

  static final String ACCOUNT_ID = "123456789012";
  static final String QUEUE_NAME = "aem-orchestrator-queue";
  static final String PUBLISH_DISPATCHER_STACK = "aem-publish-dispatcher-stack";
  static final String PUBLISH_STACK = "aem-publish-stack";
  static final String PREVIEW_PUBLISH_DISPATCHER_STACK = "aem-preview-publish-dispatcher-stack";
  static final String PREVIEW_PUBLISH_STACK = "aem-preview-publish-stack";
  static final String AUTHOR_DISPATCHER_STACK = "aem-author-dispatcher-stack";
  static final String AUTHOR_STACK = "aem-author-stack";
  static final String MESSAGING_STACK = "aem-messaging-stack";
  static final String CREDENTIALS_S3_PATH = "aem-stack-config/aem-credentials.json";

  static final String PUBLISH_DISPATCHER_ASG = "publish-dispatcher-asg";
  static final String PUBLISH_ASG = "publish-asg";
  static final String PREVIEW_PUBLISH_DISPATCHER_ASG = "preview-publish-dispatcher-asg";
  static final String PREVIEW_PUBLISH_ASG = "preview-publish-asg";
  static final String AUTHOR_DISPATCHER_ASG = "author-dispatcher-asg";
  static final String AUTHOR_ELB_NAME = "aem-author-elb";
  static final String AUTHOR_ELB_ARN =
      "arn:aws:elasticloadbalancing:ap-southeast-2:"
          + ACCOUNT_ID
          + ":loadbalancer/app/"
          + AUTHOR_ELB_NAME
          + "/50dc6c495c0c9188";
  static final String AUTHOR_ELB_DNS = "localhost";
  static final String TOPIC_ARN = "arn:aws:sns:ap-southeast-2:" + ACCOUNT_ID + ":aem-asg-events";
  static final String DEVICE_NAME = "/dev/sdb";

  private static final ObjectMapper JSON = new ObjectMapper();

  static final FakeAws AWS = new FakeAws(ACCOUNT_ID, QUEUE_NAME);
  static final FakeAem AEM = startFakeAem();
  static final Map<String, String> CREDENTIALS = loadCredentials();

  static {
    AWS.addStackResource(
        PUBLISH_DISPATCHER_STACK, "PublishDispatcherAutoScalingGroup", PUBLISH_DISPATCHER_ASG);
    AWS.addStackResource(PUBLISH_STACK, "PublishAutoScalingGroup", PUBLISH_ASG);
    AWS.addStackResource(
        PREVIEW_PUBLISH_DISPATCHER_STACK,
        "PreviewPublishDispatcherAutoScalingGroup",
        PREVIEW_PUBLISH_DISPATCHER_ASG);
    AWS.addStackResource(
        PREVIEW_PUBLISH_STACK, "PreviewPublishAutoScalingGroup", PREVIEW_PUBLISH_ASG);
    AWS.addStackResource(
        AUTHOR_DISPATCHER_STACK, "AuthorDispatcherAutoScalingGroup", AUTHOR_DISPATCHER_ASG);
    AWS.addStackResource(AUTHOR_STACK, "AuthorLoadBalancer", AUTHOR_ELB_ARN);
    AWS.addStackResource(MESSAGING_STACK, "AEMASGEventTopic", TOPIC_ARN);
    AWS.addLoadBalancer(AUTHOR_ELB_ARN, AUTHOR_ELB_NAME, AUTHOR_ELB_DNS);
    // Read by AemConfig while the context starts (aem.credentials.s3.use=true)
    AWS.s3Objects.put(CREDENTIALS_S3_PATH, readResource("/sample-aem-credentials.json"));
  }

  /** Replaces the SDK client beans built by AwsConfig with the in-memory fakes. */
  @TestConfiguration
  static class FakeAwsClients {
    @Bean
    static BeanPostProcessor fakeAwsClientsPostProcessor() {
      return new BeanPostProcessor() {
        @Override
        public Object postProcessAfterInitialization(Object bean, String beanName) {
          return AWS.replaceClient(bean);
        }
      };
    }
  }

  @DynamicPropertySource
  static void aemAuthorPort(DynamicPropertyRegistry registry) {
    registry.add("aem.port.author", AEM::port);
  }

  private static final Duration TIMEOUT = Duration.ofSeconds(30);

  @Autowired ResourceReadyChecker resourceReadyChecker;
  @Autowired OrchestratorMessageListener messageListener;
  @LocalServerPort int serverPort;

  private List<FakeAem.Request> startupAemRequests;
  private boolean resourcesReady;

  @BeforeAll
  void startOrchestrator() throws Exception {
    // Same sequence as AemOrchestrator.main(): wait for the Author ELB, then consume the queue
    resourcesReady = resourceReadyChecker.isResourcesReady();
    startupAemRequests = AEM.requests();
    if (resourcesReady) {
      messageListener.start();
    }
  }

  @BeforeEach
  void resetFakes() {
    AWS.reset();
    AEM.reset();
  }

  // ======================================================================== startup

  @Test
  void runsOnSpringBoot4AndSpringFramework7() {
    assertThat(SpringBootVersion.getVersion()).startsWith("4.");
    assertThat(SpringVersion.getVersion()).startsWith("7.");
  }

  @Test
  void startupWaitsForHealthyAuthorElbThenListensOnQueue() {
    assertThat(resourcesReady).isTrue();
    assertThat(startupAemRequests)
        .anySatisfy(
            r -> {
              assertThat(r.method()).isEqualTo("GET");
              assertThat(r.path()).isEqualTo(FakeAem.HEALTH_CHECK_PATH);
              assertThat(r.param("tags")).isEqualTo("shallow");
            });
    assertThat(AWS.queueUrlRequests).contains(QUEUE_NAME);
  }

  @Test
  void actuatorHealthAndInfoEndpointsAreServed() throws Exception {
    HttpClient http = HttpClient.newHttpClient();

    HttpResponse<String> health = get(http, "/actuator/health");
    assertThat(health.statusCode()).isEqualTo(200);
    assertThat(health.body()).contains("\"status\":\"UP\"");

    HttpResponse<String> info = get(http, "/actuator/info");
    assertThat(info.statusCode()).isEqualTo(200);
    assertThat(info.body()).contains(PUBLISH_ASG).contains(AUTHOR_ELB_DNS);
  }

  // ======================================================================== scale up

  @Nested
  class ScaleUp {

    @Test
    void publishDispatcherLaunchRaisesPublishCapacityToMatch() {
      launchDispatcher(PUBLISH_DISPATCHER_ASG, "i-pd-1", 2);
      launchDispatcher(PUBLISH_DISPATCHER_ASG, "i-pd-2", 2);
      AWS.autoScalingGroup(PUBLISH_ASG).desiredCapacity = 1;

      String receipt = send(launchNotification(PUBLISH_DISPATCHER_ASG, "i-pd-2"));

      awaitDeleted(receipt);
      assertThat(AWS.autoScalingGroup(PUBLISH_ASG).desiredCapacity).isEqualTo(2);
      assertThat(AEM.requests()).isEmpty();
    }

    @Test
    void previewPublishDispatcherLaunchRaisesPreviewPublishCapacityToMatch() {
      launchDispatcher(PREVIEW_PUBLISH_DISPATCHER_ASG, "i-ppd-1", 3);
      AWS.autoScalingGroup(PREVIEW_PUBLISH_ASG).desiredCapacity = 2;

      String receipt = send(launchNotification(PREVIEW_PUBLISH_DISPATCHER_ASG, "i-ppd-1"));

      awaitDeleted(receipt);
      assertThat(AWS.autoScalingGroup(PREVIEW_PUBLISH_ASG).desiredCapacity).isEqualTo(3);
    }

    @Test
    void authorDispatcherLaunchCreatesFlushAgentAndTagsAuthorHost() {
      Ec2Instance dispatcher = launchDispatcher(AUTHOR_DISPATCHER_ASG, "i-ad-1", 1);

      String receipt = send(launchNotification(AUTHOR_DISPATCHER_ASG, dispatcher.id));

      awaitDeleted(receipt);
      FakeAem.Request create = single(AEM.agentRequests("flushAgent-i-ad-1"));
      assertThat(create.path()).isEqualTo("/etc/replication/agents.author/flushAgent-i-ad-1");
      assertReplicatorAuth(create);
      assertThat(create.param("jcr:content/transportUri"))
          .isEqualTo("http://" + dispatcher.privateIp + ":443/dispatcher/invalidate.cache");
      assertThat(create.param("jcr:content/serializationType")).isEqualTo("flush");
      assertThat(dispatcher.tags).containsEntry("AuthorHost", AUTHOR_ELB_DNS);
    }

    @Test
    void firstPublishIsPairedGetsReplicationAgentEmptySnapshotTagAndAlarm() {
      Ec2Instance dispatcher = launchDispatcher(PUBLISH_DISPATCHER_ASG, "i-pd-1", 1);
      Ec2Instance publish = launch(PUBLISH_ASG, "i-p-1", "10.0.2.11", "ap-southeast-2a");

      String receipt = send(launchNotification(PUBLISH_ASG, publish.id));

      awaitDeleted(receipt);
      assertPaired(publish, dispatcher, "PairInstanceId", "PublishHost", "PublishDispatcherHost");

      FakeAem.Request create = single(AEM.agentRequests("replicationAgent-i-p-1"));
      assertReplicatorAuth(create);
      assertThat(create.param("jcr:content/transportUri"))
          .isEqualTo("https://10.0.2.11:5433/bin/receive?sling:authRequestLogin=1");
      assertThat(create.param("jcr:content/enabled")).isEqualTo("true");

      // First publish instance has nothing to snapshot from
      assertThat(publish.tags).containsEntry("SnapshotId", "");
      assertThat(AWS.snapshots).isEmpty();

      assertContentHealthAlarm(publish.id, PUBLISH_DISPATCHER_STACK);
    }

    @Test
    void additionalPublishIsSeededFromSnapshotOfOldestActivePublish() {
      Ec2Instance oldDispatcher = launchDispatcher(PUBLISH_DISPATCHER_ASG, "i-pd-1", 2);
      Ec2Instance newDispatcher = launchDispatcher(PUBLISH_DISPATCHER_ASG, "i-pd-2", 2);
      Ec2Instance active = activePublish(PUBLISH_ASG, "i-p-1", "10.0.2.11", "vol-p-1");
      active.tags.put("PairInstanceId", oldDispatcher.id);
      oldDispatcher.tags.put("PairInstanceId", active.id);
      Ec2Instance publish = launch(PUBLISH_ASG, "i-p-2", "10.0.2.12", "ap-southeast-2b");

      String receipt = send(launchNotification(PUBLISH_ASG, publish.id));

      awaitDeleted(receipt);
      assertPaired(
          publish, newDispatcher, "PairInstanceId", "PublishHost", "PublishDispatcherHost");

      // Create agent for the new instance; pause then resume the active instance's agent
      assertReplicatorAuth(single(AEM.agentRequests("replicationAgent-i-p-2")));
      List<FakeAem.Request> activeAgent = AEM.agentRequests("replicationAgent-i-p-1");
      assertThat(activeAgent).hasSize(2);
      assertThat(activeAgent.get(0).param("jcr:content/transportUser"))
          .isEqualTo("orchestrator-pause");
      assertOrchestratorAuth(activeAgent.get(0));
      assertThat(activeAgent.get(1).param("jcr:content/transportUser")).isEqualTo("replicator");
      assertThat(activeAgent.get(1).param("jcr:content/transportPassword"))
          .isEqualTo(CREDENTIALS.get("replicator"));

      Snapshot snapshot = single(List.copyOf(AWS.snapshots.values()));
      assertThat(snapshot.volumeId()).isEqualTo("vol-p-1");
      assertThat(snapshot.description()).contains("publish instance i-p-1");
      assertThat(snapshot.tags())
          .containsEntry("SnapshotType", "orchestration")
          .containsEntry("Name", "AEM publish Snapshot i-p-1")
          .containsEntry("Component", "publish")
          .containsEntry("StackPrefix", "aem-it")
          .doesNotContainKey("ComponentInitStatus");
      assertThat(publish.tags).containsEntry("SnapshotId", snapshot.id());

      assertContentHealthAlarm(publish.id, PUBLISH_DISPATCHER_STACK);
    }

    @Test
    void additionalPreviewPublishIsSeededFromSnapshotOfActivePreviewPublish() {
      Ec2Instance oldDispatcher = launchDispatcher(PREVIEW_PUBLISH_DISPATCHER_ASG, "i-ppd-1", 2);
      Ec2Instance newDispatcher = launchDispatcher(PREVIEW_PUBLISH_DISPATCHER_ASG, "i-ppd-2", 2);
      Ec2Instance active = activePublish(PREVIEW_PUBLISH_ASG, "i-pp-1", "10.0.3.11", "vol-pp-1");
      active.tags.put("PreviewPairInstanceId", oldDispatcher.id);
      oldDispatcher.tags.put("PreviewPairInstanceId", active.id);
      Ec2Instance previewPublish =
          launch(PREVIEW_PUBLISH_ASG, "i-pp-2", "10.0.3.12", "ap-southeast-2b");

      String receipt = send(launchNotification(PREVIEW_PUBLISH_ASG, previewPublish.id));

      awaitDeleted(receipt);
      assertPaired(
          previewPublish,
          newDispatcher,
          "PreviewPairInstanceId",
          "PreviewPublishHost",
          "PreviewPublishDispatcherHost");

      FakeAem.Request create = single(AEM.agentRequests("previewReplicationAgent-i-pp-2"));
      assertReplicatorAuth(create);
      assertThat(create.param("jcr:content/transportUri"))
          .isEqualTo("https://10.0.3.12:5433/bin/receive?sling:authRequestLogin=1");
      assertThat(AEM.agentRequests("previewReplicationAgent-i-pp-1")).hasSize(2);

      Snapshot snapshot = single(List.copyOf(AWS.snapshots.values()));
      assertThat(snapshot.volumeId()).isEqualTo("vol-pp-1");
      assertThat(snapshot.tags()).containsEntry("Name", "AEM previewPublish Snapshot i-pp-1");
      assertThat(previewPublish.tags).containsEntry("SnapshotId", snapshot.id());

      assertContentHealthAlarm(previewPublish.id, PREVIEW_PUBLISH_DISPATCHER_STACK);
    }

    @Test
    void launchOfInstanceThatIsNoLongerRunningIsAcknowledgedWithoutAction() {
      launchDispatcher(PUBLISH_DISPATCHER_ASG, "i-pd-1", 1);
      Ec2Instance publish = launch(PUBLISH_ASG, "i-p-1", "10.0.2.11", "ap-southeast-2a");
      publish.state = InstanceStateName.SHUTTING_DOWN;

      String receipt = send(launchNotification(PUBLISH_ASG, publish.id));

      awaitDeleted(receipt);
      assertThat(publish.tags).isEmpty();
      assertThat(AEM.requests()).isEmpty();
      assertThat(AWS.alarms).isEmpty();
    }

    @Test
    void instanceMovingOutOfStandbyIsAcknowledgedWithoutAction() {
      launchDispatcher(PUBLISH_DISPATCHER_ASG, "i-pd-1", 1);
      Ec2Instance publish = launch(PUBLISH_ASG, "i-p-1", "10.0.2.11", "ap-southeast-2a");
      Map<String, Object> event = launchEvent(PUBLISH_ASG, publish.id);
      event.put("Description", "Moving EC2 instance out of Standby: " + publish.id);

      String receipt = send(notification("Auto Scaling: launch for group \"publish-asg\"", event));

      awaitDeleted(receipt);
      assertThat(publish.tags).isEmpty();
      assertThat(AEM.requests()).isEmpty();
    }

    @Test
    void launchForUnknownAutoScalingGroupStaysOnQueue() {
      launch("some-other-asg", "i-x-1", "10.0.9.1", "ap-southeast-2a");

      String receipt = send(launchNotification("some-other-asg", "i-x-1"));

      awaitNotDeleted(receipt);
    }
  }

  // ======================================================================== scale down

  @Nested
  class ScaleDown {

    @Test
    void publishDispatcherTerminationTerminatesPairedPublishAndLowersCapacity() {
      Ec2Instance remainingDispatcher = launchDispatcher(PUBLISH_DISPATCHER_ASG, "i-pd-1", 1);
      Ec2Instance pairedPublish = launch(PUBLISH_ASG, "i-p-2", "10.0.2.12", "ap-southeast-2b");
      Ec2Instance otherPublish = launch(PUBLISH_ASG, "i-p-1", "10.0.2.11", "ap-southeast-2a");
      pairedPublish.tags.put("PairInstanceId", "i-pd-2");
      otherPublish.tags.put("PairInstanceId", remainingDispatcher.id);
      AWS.autoScalingGroup(PUBLISH_ASG).desiredCapacity = 2;

      String receipt = send(terminateNotification(PUBLISH_DISPATCHER_ASG, "i-pd-2"));

      awaitDeleted(receipt);
      assertThat(pairedPublish.state).isEqualTo(InstanceStateName.TERMINATED);
      assertThat(otherPublish.state).isEqualTo(InstanceStateName.RUNNING);
      assertThat(AWS.autoScalingGroup(PUBLISH_ASG).desiredCapacity).isEqualTo(1);
    }

    @Test
    void previewPublishDispatcherTerminationTerminatesPairedPreviewPublishAndLowersCapacity() {
      launchDispatcher(PREVIEW_PUBLISH_DISPATCHER_ASG, "i-ppd-1", 1);
      Ec2Instance pairedPreview =
          launch(PREVIEW_PUBLISH_ASG, "i-pp-2", "10.0.3.12", "ap-southeast-2b");
      pairedPreview.tags.put("PreviewPairInstanceId", "i-ppd-2");
      AWS.autoScalingGroup(PREVIEW_PUBLISH_ASG).desiredCapacity = 2;

      String receipt = send(terminateNotification(PREVIEW_PUBLISH_DISPATCHER_ASG, "i-ppd-2"));

      awaitDeleted(receipt);
      assertThat(pairedPreview.state).isEqualTo(InstanceStateName.TERMINATED);
      assertThat(AWS.autoScalingGroup(PREVIEW_PUBLISH_ASG).desiredCapacity).isEqualTo(1);
    }

    @Test
    void authorDispatcherTerminationDeletesFlushAgent() {
      String receipt = send(terminateNotification(AUTHOR_DISPATCHER_ASG, "i-ad-1"));

      awaitDeleted(receipt);
      FakeAem.Request delete = single(AEM.agentRequests("flushAgent-i-ad-1"));
      assertThat(delete.param(":operation")).isEqualTo("delete");
      assertOrchestratorAuth(delete);
    }

    @Test
    void publishTerminationTerminatesPairedDispatcherDeletesAgentAndAlarm() {
      Ec2Instance pairedDispatcher = launchDispatcher(PUBLISH_DISPATCHER_ASG, "i-pd-1", 1);
      pairedDispatcher.tags.put("PairInstanceId", "i-p-1");

      String receipt = send(terminateNotification(PUBLISH_ASG, "i-p-1"));

      awaitDeleted(receipt);
      assertThat(pairedDispatcher.state).isEqualTo(InstanceStateName.TERMINATED);
      FakeAem.Request delete = single(AEM.agentRequests("replicationAgent-i-p-1"));
      assertThat(delete.param(":operation")).isEqualTo("delete");
      assertOrchestratorAuth(delete);
      assertThat(AWS.deletedAlarmNames).containsExactly("contentHealthCheck-i-p-1");
    }

    @Test
    void previewPublishTerminationTerminatesPairedDispatcherDeletesAgentAndAlarm() {
      Ec2Instance pairedDispatcher = launchDispatcher(PREVIEW_PUBLISH_DISPATCHER_ASG, "i-ppd-1", 1);
      pairedDispatcher.tags.put("PreviewPairInstanceId", "i-pp-1");

      String receipt = send(terminateNotification(PREVIEW_PUBLISH_ASG, "i-pp-1"));

      awaitDeleted(receipt);
      assertThat(pairedDispatcher.state).isEqualTo(InstanceStateName.TERMINATED);
      FakeAem.Request delete = single(AEM.agentRequests("previewReplicationAgent-i-pp-1"));
      assertThat(delete.param(":operation")).isEqualTo("delete");
      assertThat(AWS.deletedAlarmNames).containsExactly("contentHealthCheck-i-pp-1");
    }
  }

  // ======================================================================== other messages

  @Nested
  class OtherMessages {

    @Test
    void contentHealthAlarmIsAcknowledgedWithoutTerminatingByDefault() {
      Ec2Instance publish = launch(PUBLISH_ASG, "i-p-1", "10.0.2.11", "ap-southeast-2a");

      String receipt = send(alarmNotification(publish.id));

      awaitDeleted(receipt);
      // alarm.content.health.check.terminate.instance.enable=false only logs the unhealthy publish
      assertThat(publish.state).isEqualTo(InstanceStateName.RUNNING);
    }

    @Test
    void autoScalingTestNotificationIsAcknowledged() {
      Map<String, Object> event = new LinkedHashMap<>();
      event.put("AccountId", ACCOUNT_ID);
      event.put("RequestId", UUID.randomUUID().toString());
      event.put("AutoScalingGroupARN", asgArn(PUBLISH_ASG));
      event.put("AutoScalingGroupName", PUBLISH_ASG);
      event.put("Service", "AWS Auto Scaling");
      event.put("Event", "autoscaling:TEST_NOTIFICATION");
      event.put("Time", Instant.now().toString());

      String receipt =
          send(notification("Auto Scaling: test notification for group \"publish-asg\"", event));

      awaitDeleted(receipt);
      assertThat(AEM.requests()).isEmpty();
    }

    @Test
    void messageWithUnknownSubjectIsAcknowledged() {
      String receipt = send(notification("Something else entirely", Map.of("foo", "bar")));

      awaitDeleted(receipt);
    }

    @Test
    void messageThatIsNotJsonIsAcknowledged() {
      String receipt = send("this is not an SNS notification");

      awaitDeleted(receipt);
    }
  }

  // ======================================================================== world helpers

  private static Ec2Instance launch(String group, String id, String ip, String az) {
    Ec2Instance instance = AWS.launch(group, id, ip, az, Instant.now());
    AWS.autoScalingGroup(group).desiredCapacity = AWS.autoScalingGroup(group).instanceIds.size();
    return instance;
  }

  private static Ec2Instance launchDispatcher(String group, String id, int desiredCapacity) {
    String ip = "10.0.1." + (AWS.instances.size() + 1);
    Ec2Instance instance = AWS.launch(group, id, ip, "ap-southeast-2a", Instant.now());
    AWS.autoScalingGroup(group).desiredCapacity = desiredCapacity;
    return instance;
  }

  /** A healthy publish/preview publish that has been running for a day, with its data volume. */
  private static Ec2Instance activePublish(String group, String id, String ip, String volumeId) {
    Ec2Instance instance =
        AWS.launch(group, id, ip, "ap-southeast-2a", Instant.now().minus(Duration.ofDays(1)));
    instance.tags.put("ComponentInitStatus", "Success");
    instance.tags.put("SnapshotId", "");
    instance.tags.put("Component", "publish");
    instance.tags.put("StackPrefix", "aem-it");
    instance.volumesByDevice.put("/dev/sda1", "vol-root-" + id);
    instance.volumesByDevice.put(DEVICE_NAME, volumeId);
    AWS.autoScalingGroup(group).desiredCapacity = AWS.autoScalingGroup(group).instanceIds.size();
    return instance;
  }

  // ======================================================================== message builders

  private static String launchNotification(String group, String instanceId) {
    return notification(
        "Auto Scaling: launch for group \"" + group + "\"", launchEvent(group, instanceId));
  }

  private static String terminateNotification(String group, String instanceId) {
    Map<String, Object> event = autoScalingEvent(group, instanceId);
    event.put("Description", "Terminating EC2 instance: " + instanceId);
    event.put("Cause", "At 2024-01-01T00:00:00Z an instance was taken out of service.");
    event.put("Event", "autoscaling:EC2_INSTANCE_TERMINATE");
    return notification("Auto Scaling: termination for group \"" + group + "\"", event);
  }

  private static Map<String, Object> launchEvent(String group, String instanceId) {
    Map<String, Object> event = autoScalingEvent(group, instanceId);
    event.put("Description", "Launching a new EC2 instance: " + instanceId);
    event.put("Cause", "At 2024-01-01T00:00:00Z an instance was started in response to a change.");
    event.put("Event", "autoscaling:EC2_INSTANCE_LAUNCH");
    return event;
  }

  /** Body of an EC2 Auto Scaling SNS notification, as documented by AWS. */
  private static Map<String, Object> autoScalingEvent(String group, String instanceId) {
    String now = Instant.now().toString();
    Map<String, Object> event = new LinkedHashMap<>();
    event.put("Origin", "EC2");
    event.put("Destination", "AutoScalingGroup");
    event.put("Progress", 50);
    event.put("AccountId", ACCOUNT_ID);
    event.put("RequestId", UUID.randomUUID().toString());
    event.put("EndTime", now);
    event.put("AutoScalingGroupARN", asgArn(group));
    event.put("ActivityId", UUID.randomUUID().toString());
    event.put("StartTime", now);
    event.put("Service", "AWS Auto Scaling");
    event.put("Time", now);
    event.put("EC2InstanceId", instanceId);
    event.put("StatusCode", "InProgress");
    event.put("StatusMessage", "");
    event.put(
        "Details", Map.of("Subnet ID", "subnet-0a1b2c3d", "Availability Zone", "ap-southeast-2a"));
    event.put("AutoScalingGroupName", group);
    return event;
  }

  /** Body of a CloudWatch alarm SNS notification for the publish content health check. */
  private static String alarmNotification(String publishInstanceId) {
    Map<String, Object> trigger = new LinkedHashMap<>();
    trigger.put("MetricName", "contentHealthCheck");
    trigger.put("Namespace", PUBLISH_DISPATCHER_STACK);
    trigger.put("StatisticType", "Statistic");
    trigger.put("Statistic", "MAXIMUM");
    trigger.put("Unit", null);
    trigger.put(
        "Dimensions", List.of(Map.of("value", publishInstanceId, "name", "PairInstanceId")));
    trigger.put("Period", 60);
    trigger.put("EvaluationPeriods", 5);
    trigger.put("DatapointsToAlarm", 5);
    trigger.put("ComparisonOperator", "LessThanThreshold");
    trigger.put("Threshold", 1.0);
    trigger.put("TreatMissingData", "missing");
    trigger.put("EvaluateLowSampleCountPercentile", "");

    String alarmName = "contentHealthCheck-" + publishInstanceId;
    Map<String, Object> alarm = new LinkedHashMap<>();
    alarm.put("AlarmName", alarmName);
    alarm.put("AlarmDescription", "Content Health Alarm for Publish Instance " + publishInstanceId);
    alarm.put("AWSAccountId", ACCOUNT_ID);
    alarm.put("AlarmConfigurationUpdatedTimestamp", Instant.now().toString());
    alarm.put("NewStateValue", "ALARM");
    alarm.put(
        "NewStateReason",
        "Threshold Crossed: 5 datapoints [0.0, 0.0, 0.0, 0.0, 0.0] were less than the threshold"
            + " (1.0).");
    alarm.put("StateChangeTime", Instant.now().toString());
    alarm.put("Region", "Asia Pacific (Sydney)");
    alarm.put(
        "AlarmArn", "arn:aws:cloudwatch:ap-southeast-2:" + ACCOUNT_ID + ":alarm:" + alarmName);
    alarm.put("OldStateValue", "OK");
    alarm.put("OKActions", List.of());
    alarm.put("AlarmActions", List.of(TOPIC_ARN));
    alarm.put("InsufficientDataActions", List.of());
    alarm.put("Trigger", trigger);

    return notification("ALARM: \"" + alarmName + "\" in Asia Pacific (Sydney)", alarm);
  }

  /** SNS notification envelope as delivered to an SQS subscription (raw delivery disabled). */
  private static String notification(String subject, Map<String, ?> message) {
    Map<String, Object> envelope = new LinkedHashMap<>();
    envelope.put("Type", "Notification");
    envelope.put("MessageId", UUID.randomUUID().toString());
    envelope.put("TopicArn", TOPIC_ARN);
    envelope.put("Subject", subject);
    envelope.put("Message", JSON.writeValueAsString(message));
    envelope.put("Timestamp", Instant.now().toString());
    envelope.put("SignatureVersion", "1");
    envelope.put("Signature", "EXAMPLEpH+..");
    envelope.put(
        "SigningCertURL",
        "https://sns.ap-southeast-2.amazonaws.com/SimpleNotificationService-0000000000.pem");
    envelope.put(
        "UnsubscribeURL",
        "https://sns.ap-southeast-2.amazonaws.com/?Action=Unsubscribe&SubscriptionArn="
            + TOPIC_ARN
            + ":0000");
    return JSON.writeValueAsString(envelope);
  }

  private static String asgArn(String group) {
    return "arn:aws:autoscaling:ap-southeast-2:"
        + ACCOUNT_ID
        + ":autoScalingGroup:00000000-0000-0000-0000-000000000000:autoScalingGroupName/"
        + group;
  }

  // ======================================================================== assertions

  private static String send(String body) {
    return AWS.sendToQueue(body);
  }

  private static void awaitDeleted(String receiptHandle) {
    await("message acknowledged (deleted from queue)")
        .atMost(TIMEOUT)
        .until(() -> AWS.isDeleted(receiptHandle));
  }

  /** The handler reported failure, so the message must be left on the queue for redelivery. */
  private static void awaitNotDeleted(String receiptHandle) {
    await("message received").atMost(TIMEOUT).until(() -> AWS.isReceived(receiptHandle));
    await("message left on queue")
        .during(Duration.ofSeconds(3))
        .atMost(Duration.ofSeconds(5))
        .until(() -> !AWS.isDeleted(receiptHandle));
  }

  private static void assertPaired(
      Ec2Instance publish,
      Ec2Instance dispatcher,
      String pairTag,
      String publishHostTag,
      String dispatcherHostTag) {
    assertThat(publish.tags)
        .containsEntry(pairTag, dispatcher.id)
        .containsEntry(dispatcherHostTag, dispatcher.privateIp);
    assertThat(dispatcher.tags)
        .containsEntry(pairTag, publish.id)
        .containsEntry(publishHostTag, publish.privateIp);
  }

  private static void assertContentHealthAlarm(String instanceId, String namespace) {
    PutMetricAlarmRequest alarm = AWS.alarms.get("contentHealthCheck-" + instanceId);
    assertThat(alarm).as("content health alarm for " + instanceId).isNotNull();
    assertThat(alarm.namespace()).isEqualTo(namespace);
    assertThat(alarm.metricName()).isEqualTo("contentHealthCheck");
    assertThat(alarm.alarmActions()).containsExactly(TOPIC_ARN);
    assertThat(alarm.dimensions())
        .singleElement()
        .satisfies(
            d -> {
              assertThat(d.name()).isEqualTo("PairInstanceId");
              assertThat(d.value()).isEqualTo(instanceId);
            });
  }

  private static void assertReplicatorAuth(FakeAem.Request request) {
    assertThat(request.user()).isEqualTo("replicator");
    assertThat(request.password()).isEqualTo(CREDENTIALS.get("replicator"));
  }

  private static void assertOrchestratorAuth(FakeAem.Request request) {
    assertThat(request.user()).isEqualTo("orchestrator");
    assertThat(request.password()).isEqualTo(CREDENTIALS.get("orchestrator"));
  }

  private static <T> T single(List<T> items) {
    assertThat(items).as("AEM requests: " + AEM.describe()).hasSize(1);
    return items.get(0);
  }

  private HttpResponse<String> get(HttpClient http, String path) throws Exception {
    return http.send(
        HttpRequest.newBuilder(URI.create("http://localhost:" + serverPort + path)).build(),
        HttpResponse.BodyHandlers.ofString());
  }

  // ======================================================================== fixtures

  private static FakeAem startFakeAem() {
    try {
      return new FakeAem();
    } catch (IOException e) {
      throw new UncheckedIOException(e);
    }
  }

  private static Map<String, String> loadCredentials() {
    @SuppressWarnings("unchecked")
    Map<String, String> credentials =
        JSON.readValue(readResource("/sample-aem-credentials.json"), Map.class);
    return credentials;
  }

  private static String readResource(String path) {
    try (InputStream in = AemOrchestratorIT.class.getResourceAsStream(path)) {
      return new String(in.readAllBytes(), StandardCharsets.UTF_8);
    } catch (IOException e) {
      throw new UncheckedIOException(e);
    }
  }
}
