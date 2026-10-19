package com.shinesolutions.aemorchestrator;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.io.ByteArrayInputStream;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;
import org.mockito.stubbing.Answer;
import software.amazon.awssdk.awscore.exception.AwsErrorDetails;
import software.amazon.awssdk.core.ResponseInputStream;
import software.amazon.awssdk.core.SdkResponse;
import software.amazon.awssdk.http.AbortableInputStream;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.autoscaling.AutoScalingClient;
import software.amazon.awssdk.services.autoscaling.model.AutoScalingGroup;
import software.amazon.awssdk.services.autoscaling.model.DescribeAutoScalingGroupsRequest;
import software.amazon.awssdk.services.autoscaling.model.DescribeAutoScalingGroupsResponse;
import software.amazon.awssdk.services.autoscaling.model.SetDesiredCapacityRequest;
import software.amazon.awssdk.services.autoscaling.model.SetDesiredCapacityResponse;
import software.amazon.awssdk.services.cloudformation.CloudFormationClient;
import software.amazon.awssdk.services.cloudformation.model.DescribeStackResourcesRequest;
import software.amazon.awssdk.services.cloudformation.model.DescribeStackResourcesResponse;
import software.amazon.awssdk.services.cloudformation.model.StackResource;
import software.amazon.awssdk.services.cloudwatch.CloudWatchClient;
import software.amazon.awssdk.services.cloudwatch.model.DeleteAlarmsRequest;
import software.amazon.awssdk.services.cloudwatch.model.DeleteAlarmsResponse;
import software.amazon.awssdk.services.cloudwatch.model.PutMetricAlarmRequest;
import software.amazon.awssdk.services.cloudwatch.model.PutMetricAlarmResponse;
import software.amazon.awssdk.services.ec2.Ec2Client;
import software.amazon.awssdk.services.ec2.model.CreateSnapshotRequest;
import software.amazon.awssdk.services.ec2.model.CreateSnapshotResponse;
import software.amazon.awssdk.services.ec2.model.CreateTagsRequest;
import software.amazon.awssdk.services.ec2.model.CreateTagsResponse;
import software.amazon.awssdk.services.ec2.model.DescribeInstanceAttributeRequest;
import software.amazon.awssdk.services.ec2.model.DescribeInstanceAttributeResponse;
import software.amazon.awssdk.services.ec2.model.DescribeInstancesRequest;
import software.amazon.awssdk.services.ec2.model.DescribeInstancesResponse;
import software.amazon.awssdk.services.ec2.model.DescribeTagsRequest;
import software.amazon.awssdk.services.ec2.model.DescribeTagsResponse;
import software.amazon.awssdk.services.ec2.model.EbsInstanceBlockDevice;
import software.amazon.awssdk.services.ec2.model.Ec2Exception;
import software.amazon.awssdk.services.ec2.model.Filter;
import software.amazon.awssdk.services.ec2.model.InstanceBlockDeviceMapping;
import software.amazon.awssdk.services.ec2.model.InstanceState;
import software.amazon.awssdk.services.ec2.model.InstanceStateName;
import software.amazon.awssdk.services.ec2.model.Placement;
import software.amazon.awssdk.services.ec2.model.Reservation;
import software.amazon.awssdk.services.ec2.model.TagDescription;
import software.amazon.awssdk.services.ec2.model.TerminateInstancesRequest;
import software.amazon.awssdk.services.ec2.model.TerminateInstancesResponse;
import software.amazon.awssdk.services.elasticloadbalancingv2.ElasticLoadBalancingV2Client;
import software.amazon.awssdk.services.elasticloadbalancingv2.model.DescribeLoadBalancersRequest;
import software.amazon.awssdk.services.elasticloadbalancingv2.model.DescribeLoadBalancersResponse;
import software.amazon.awssdk.services.elasticloadbalancingv2.model.LoadBalancer;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.S3Utilities;
import software.amazon.awssdk.services.s3.model.GetObjectRequest;
import software.amazon.awssdk.services.s3.model.GetObjectResponse;
import software.amazon.awssdk.services.s3.model.NoSuchKeyException;
import software.amazon.awssdk.services.sqs.SqsClient;
import software.amazon.awssdk.services.sqs.model.DeleteMessageRequest;
import software.amazon.awssdk.services.sqs.model.DeleteMessageResponse;
import software.amazon.awssdk.services.sqs.model.GetQueueUrlRequest;
import software.amazon.awssdk.services.sqs.model.GetQueueUrlResponse;
import software.amazon.awssdk.services.sqs.model.Message;
import software.amazon.awssdk.services.sqs.model.MessageSystemAttributeName;
import software.amazon.awssdk.services.sqs.model.ReceiveMessageRequest;
import software.amazon.awssdk.services.sqs.model.ReceiveMessageResponse;
import software.amazon.awssdk.utils.builder.SdkBuilder;

/**
 * In-memory AWS account backing Mockito mocks of the SDK v2 clients. Answers are computed from the
 * current state, and write calls (tags, termination, capacity, snapshots, alarms, SQS deletes)
 * change it, so tests can set up a scenario and assert on the resulting state.
 */
final class FakeAws {

  static final Region REGION = Region.AP_SOUTHEAST_2;

  /** An EC2 instance; tags and state are mutated by the orchestrator through the fake clients. */
  static final class Ec2Instance {
    final String id;
    final String privateIp;
    final String availabilityZone;
    final Instant launchTime;
    final Map<String, String> tags = new ConcurrentHashMap<>();
    final Map<String, String> volumesByDevice = new ConcurrentHashMap<>();
    volatile InstanceStateName state = InstanceStateName.RUNNING;

    Ec2Instance(String id, String privateIp, String availabilityZone, Instant launchTime) {
      this.id = id;
      this.privateIp = privateIp;
      this.availabilityZone = availabilityZone;
      this.launchTime = launchTime;
    }
  }

  /** An auto scaling group with its member instance IDs. */
  static final class AutoScalingGroupState {
    final String name;
    final List<String> instanceIds = new CopyOnWriteArrayList<>();
    volatile int desiredCapacity;

    AutoScalingGroupState(String name) {
      this.name = name;
    }
  }

  /** A snapshot created by the orchestrator. */
  record Snapshot(String id, String volumeId, String description, Map<String, String> tags) {}

  final Ec2Client ec2 = mock(Ec2Client.class, emptyResponses());
  final ElasticLoadBalancingV2Client elb =
      mock(ElasticLoadBalancingV2Client.class, emptyResponses());
  final AutoScalingClient autoScaling = mock(AutoScalingClient.class, emptyResponses());
  final CloudFormationClient cloudFormation = mock(CloudFormationClient.class, emptyResponses());
  final S3Client s3 = mock(S3Client.class, emptyResponses());
  final CloudWatchClient cloudWatch = mock(CloudWatchClient.class, emptyResponses());
  final SqsClient sqs = mock(SqsClient.class, emptyResponses());

  final Map<String, Ec2Instance> instances = new ConcurrentHashMap<>();
  final Map<String, AutoScalingGroupState> autoScalingGroups = new ConcurrentHashMap<>();
  final Map<String, Snapshot> snapshots = new ConcurrentHashMap<>();
  final Map<String, PutMetricAlarmRequest> alarms = new ConcurrentHashMap<>();
  final List<String> deletedAlarmNames = new CopyOnWriteArrayList<>();
  final Map<String, Map<String, String>> stackResources = new ConcurrentHashMap<>();
  final List<LoadBalancer> loadBalancers = new CopyOnWriteArrayList<>();
  final Map<String, String> s3Objects = new ConcurrentHashMap<>();
  final List<String> queueUrlRequests = new CopyOnWriteArrayList<>();
  final Set<String> receivedMessageIds = ConcurrentHashMap.newKeySet();
  final Set<String> deletedReceiptHandles = ConcurrentHashMap.newKeySet();

  private final BlockingQueue<Message> queue = new LinkedBlockingQueue<>();
  private final AtomicInteger snapshotSequence = new AtomicInteger();
  private final String queueUrl;

  FakeAws(String accountId, String queueName) {
    queueUrl = "https://sqs." + REGION.id() + ".amazonaws.com/" + accountId + "/" + queueName;
    stubEc2();
    stubAutoScaling();
    stubCloudFormation();
    stubElb();
    stubS3();
    stubCloudWatch();
    stubSqs();
  }

  /**
   * Swaps a real SDK client bean for the matching fake, leaving every other bean untouched. Used by
   * a BeanPostProcessor, so the application's own client configuration still runs.
   */
  Object replaceClient(Object bean) {
    if (bean instanceof SqsClient) {
      return sqs;
    } else if (bean instanceof Ec2Client) {
      return ec2;
    } else if (bean instanceof ElasticLoadBalancingV2Client) {
      return elb;
    } else if (bean instanceof AutoScalingClient) {
      return autoScaling;
    } else if (bean instanceof CloudFormationClient) {
      return cloudFormation;
    } else if (bean instanceof S3Client) {
      return s3;
    } else if (bean instanceof CloudWatchClient) {
      return cloudWatch;
    }
    return bean;
  }

  /** Clears per-scenario state; stacks, load balancers and S3 objects are kept. */
  void reset() {
    instances.clear();
    autoScalingGroups.values().forEach(g -> g.instanceIds.clear());
    autoScalingGroups.values().forEach(g -> g.desiredCapacity = 0);
    snapshots.clear();
    alarms.clear();
    deletedAlarmNames.clear();
  }

  // ---------------------------------------------------------------- world setup helpers

  void addStackResource(String stackName, String logicalId, String physicalId) {
    stackResources
        .computeIfAbsent(stackName, s -> new ConcurrentHashMap<>())
        .put(logicalId, physicalId);
  }

  void addLoadBalancer(String arn, String name, String dnsName) {
    loadBalancers.add(
        LoadBalancer.builder()
            .loadBalancerArn(arn)
            .loadBalancerName(name)
            .dnsName(dnsName)
            .build());
  }

  AutoScalingGroupState autoScalingGroup(String name) {
    return autoScalingGroups.computeIfAbsent(name, AutoScalingGroupState::new);
  }

  /** Adds a running instance to an auto scaling group (desired capacity is set separately). */
  Ec2Instance launch(
      String groupName, String instanceId, String privateIp, String az, Instant launchTime) {
    Ec2Instance instance = new Ec2Instance(instanceId, privateIp, az, launchTime);
    instances.put(instanceId, instance);
    autoScalingGroup(groupName).instanceIds.add(instanceId);
    return instance;
  }

  Ec2Instance instance(String instanceId) {
    return Objects.requireNonNull(instances.get(instanceId), instanceId);
  }

  // ---------------------------------------------------------------- SQS helpers

  /** Puts a message on the queue and returns its receipt handle. */
  String sendToQueue(String body) {
    String messageId = UUID.randomUUID().toString();
    String receiptHandle = "receipt-" + messageId;
    queue.add(
        Message.builder()
            .messageId(messageId)
            .receiptHandle(receiptHandle)
            .body(body)
            .attributes(
                Map.of(
                    MessageSystemAttributeName.APPROXIMATE_RECEIVE_COUNT,
                    "1",
                    MessageSystemAttributeName.SENT_TIMESTAMP,
                    String.valueOf(System.currentTimeMillis())))
            .build());
    return receiptHandle;
  }

  boolean isReceived(String receiptHandle) {
    return receivedMessageIds.contains(receiptHandle.substring("receipt-".length()));
  }

  boolean isDeleted(String receiptHandle) {
    return deletedReceiptHandles.contains(receiptHandle);
  }

  // ---------------------------------------------------------------- stubs

  private void stubEc2() {
    when(ec2.describeInstances(any(DescribeInstancesRequest.class)))
        .thenAnswer(
            inv -> {
              DescribeInstancesRequest request = inv.getArgument(0);
              List<software.amazon.awssdk.services.ec2.model.Instance> found = new ArrayList<>();
              for (String id : request.instanceIds()) {
                Ec2Instance i = instances.get(id);
                if (i == null) {
                  // Same error EC2 returns for an unknown instance ID
                  throw Ec2Exception.builder()
                      .statusCode(400)
                      .awsErrorDetails(
                          AwsErrorDetails.builder()
                              .errorCode("InvalidInstanceID.NotFound")
                              .errorMessage("The instance ID '" + id + "' does not exist")
                              .build())
                      .build();
                }
                found.add(
                    software.amazon.awssdk.services.ec2.model.Instance.builder()
                        .instanceId(i.id)
                        .privateIpAddress(i.privateIp)
                        .placement(Placement.builder().availabilityZone(i.availabilityZone).build())
                        .state(InstanceState.builder().name(i.state).build())
                        .launchTime(i.launchTime)
                        .build());
              }
              return DescribeInstancesResponse.builder()
                  .reservations(Reservation.builder().instances(found).build())
                  .build();
            });

    when(ec2.describeTags(any(DescribeTagsRequest.class)))
        .thenAnswer(
            inv -> {
              DescribeTagsRequest request = inv.getArgument(0);
              List<TagDescription> tags = new ArrayList<>();
              for (Filter filter : request.filters()) {
                if (!filter.name().equals("resource-id")) {
                  throw new UnsupportedOperationException("Unsupported filter " + filter.name());
                }
                for (String resourceId : filter.values()) {
                  tagsOf(resourceId)
                      .forEach(
                          (k, v) ->
                              tags.add(
                                  TagDescription.builder()
                                      .resourceId(resourceId)
                                      .key(k)
                                      .value(v)
                                      .build()));
                }
              }
              return DescribeTagsResponse.builder().tags(tags).build();
            });

    when(ec2.createTags(any(CreateTagsRequest.class)))
        .thenAnswer(
            inv -> {
              CreateTagsRequest request = inv.getArgument(0);
              for (String resourceId : request.resources()) {
                request.tags().forEach(t -> tagsOf(resourceId).put(t.key(), t.value()));
              }
              return CreateTagsResponse.builder().build();
            });

    when(ec2.terminateInstances(any(TerminateInstancesRequest.class)))
        .thenAnswer(
            inv -> {
              TerminateInstancesRequest request = inv.getArgument(0);
              request
                  .instanceIds()
                  .forEach(id -> instance(id).state = InstanceStateName.TERMINATED);
              return TerminateInstancesResponse.builder().build();
            });

    when(ec2.describeInstanceAttribute(any(DescribeInstanceAttributeRequest.class)))
        .thenAnswer(
            inv -> {
              DescribeInstanceAttributeRequest request = inv.getArgument(0);
              List<InstanceBlockDeviceMapping> mappings =
                  instance(request.instanceId()).volumesByDevice.entrySet().stream()
                      .map(
                          e ->
                              InstanceBlockDeviceMapping.builder()
                                  .deviceName(e.getKey())
                                  .ebs(
                                      EbsInstanceBlockDevice.builder()
                                          .volumeId(e.getValue())
                                          .build())
                                  .build())
                      .toList();
              return DescribeInstanceAttributeResponse.builder()
                  .instanceId(request.instanceId())
                  .blockDeviceMappings(mappings)
                  .build();
            });

    when(ec2.createSnapshot(any(CreateSnapshotRequest.class)))
        .thenAnswer(
            inv -> {
              CreateSnapshotRequest request = inv.getArgument(0);
              String id = String.format("snap-%04d", snapshotSequence.incrementAndGet());
              snapshots.put(
                  id,
                  new Snapshot(
                      id, request.volumeId(), request.description(), new ConcurrentHashMap<>()));
              return CreateSnapshotResponse.builder()
                  .snapshotId(id)
                  .volumeId(request.volumeId())
                  .build();
            });
  }

  private Map<String, String> tagsOf(String resourceId) {
    if (snapshots.containsKey(resourceId)) {
      return snapshots.get(resourceId).tags();
    }
    return instance(resourceId).tags;
  }

  private void stubAutoScaling() {
    when(autoScaling.describeAutoScalingGroups(any(DescribeAutoScalingGroupsRequest.class)))
        .thenAnswer(
            inv -> {
              DescribeAutoScalingGroupsRequest request = inv.getArgument(0);
              List<AutoScalingGroup> groups =
                  request.autoScalingGroupNames().stream()
                      .filter(autoScalingGroups::containsKey)
                      .map(autoScalingGroups::get)
                      .map(
                          g ->
                              AutoScalingGroup.builder()
                                  .autoScalingGroupName(g.name)
                                  .desiredCapacity(g.desiredCapacity)
                                  .instances(
                                      g.instanceIds.stream()
                                          .map(this::instance)
                                          .map(
                                              i ->
                                                  software.amazon.awssdk.services.autoscaling.model
                                                      .Instance.builder()
                                                      .instanceId(i.id)
                                                      .availabilityZone(i.availabilityZone)
                                                      .build())
                                          .toList())
                                  .build())
                      .toList();
              return DescribeAutoScalingGroupsResponse.builder().autoScalingGroups(groups).build();
            });

    when(autoScaling.setDesiredCapacity(any(SetDesiredCapacityRequest.class)))
        .thenAnswer(
            inv -> {
              SetDesiredCapacityRequest request = inv.getArgument(0);
              autoScalingGroup(request.autoScalingGroupName()).desiredCapacity =
                  request.desiredCapacity();
              return SetDesiredCapacityResponse.builder().build();
            });
  }

  private void stubCloudFormation() {
    when(cloudFormation.describeStackResources(any(DescribeStackResourcesRequest.class)))
        .thenAnswer(
            inv -> {
              DescribeStackResourcesRequest request = inv.getArgument(0);
              List<StackResource> resources =
                  stackResources.getOrDefault(request.stackName(), Map.of()).entrySet().stream()
                      .map(
                          e ->
                              StackResource.builder()
                                  .stackName(request.stackName())
                                  .logicalResourceId(e.getKey())
                                  .physicalResourceId(e.getValue())
                                  .build())
                      .toList();
              return DescribeStackResourcesResponse.builder().stackResources(resources).build();
            });
  }

  private void stubElb() {
    when(elb.describeLoadBalancers(any(DescribeLoadBalancersRequest.class)))
        .thenAnswer(
            inv -> {
              DescribeLoadBalancersRequest request = inv.getArgument(0);
              List<LoadBalancer> found =
                  loadBalancers.stream()
                      .filter(
                          lb ->
                              request.names().contains(lb.loadBalancerName())
                                  || request.loadBalancerArns().contains(lb.loadBalancerArn()))
                      .toList();
              return DescribeLoadBalancersResponse.builder().loadBalancers(found).build();
            });
  }

  private void stubS3() {
    when(s3.utilities()).thenReturn(S3Utilities.builder().region(REGION).build());
    when(s3.getObject(any(GetObjectRequest.class)))
        .thenAnswer(
            inv -> {
              GetObjectRequest request = inv.getArgument(0);
              String content = s3Objects.get(request.bucket() + "/" + request.key());
              if (content == null) {
                throw NoSuchKeyException.builder().statusCode(404).message("NoSuchKey").build();
              }
              return new ResponseInputStream<>(
                  GetObjectResponse.builder().build(),
                  AbortableInputStream.create(new ByteArrayInputStream(content.getBytes())));
            });
  }

  private void stubCloudWatch() {
    when(cloudWatch.putMetricAlarm(any(PutMetricAlarmRequest.class)))
        .thenAnswer(
            inv -> {
              PutMetricAlarmRequest request = inv.getArgument(0);
              alarms.put(request.alarmName(), request);
              return PutMetricAlarmResponse.builder().build();
            });
    when(cloudWatch.deleteAlarms(any(DeleteAlarmsRequest.class)))
        .thenAnswer(
            inv -> {
              DeleteAlarmsRequest request = inv.getArgument(0);
              request.alarmNames().forEach(alarms::remove);
              deletedAlarmNames.addAll(request.alarmNames());
              return DeleteAlarmsResponse.builder().build();
            });
  }

  private void stubSqs() {
    when(sqs.getQueueUrl(any(GetQueueUrlRequest.class)))
        .thenAnswer(
            inv -> {
              GetQueueUrlRequest request = inv.getArgument(0);
              queueUrlRequests.add(request.queueName());
              return GetQueueUrlResponse.builder().queueUrl(queueUrl).build();
            });

    // Short poll instead of SQS's 20s long poll so idle consumers stay responsive
    when(sqs.receiveMessage(any(ReceiveMessageRequest.class)))
        .thenAnswer(
            inv -> {
              Message message = queue.poll(200, TimeUnit.MILLISECONDS);
              if (message == null) {
                return ReceiveMessageResponse.builder().build();
              }
              receivedMessageIds.add(message.messageId());
              return ReceiveMessageResponse.builder().messages(message).build();
            });

    when(sqs.deleteMessage(any(DeleteMessageRequest.class)))
        .thenAnswer(
            inv -> {
              DeleteMessageRequest request = inv.getArgument(0);
              if (!queueUrl.equals(request.queueUrl())) {
                throw new IllegalArgumentException("Unexpected queue URL " + request.queueUrl());
              }
              deletedReceiptHandles.add(request.receiptHandle());
              return DeleteMessageResponse.builder().build();
            });
  }

  /** Any SDK call without an explicit stub returns an empty response instead of null. */
  private static Answer<Object> emptyResponses() {
    return invocation -> {
      Class<?> type = invocation.getMethod().getReturnType();
      if (SdkResponse.class.isAssignableFrom(type)) {
        return ((SdkBuilder<?, ?>) type.getMethod("builder").invoke(null)).build();
      }
      return org.mockito.Mockito.RETURNS_DEFAULTS.answer(invocation);
    };
  }
}
