package com.shinesolutions.aemorchestrator.service;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import software.amazon.awssdk.core.ResponseInputStream;
import software.amazon.awssdk.http.AbortableInputStream;
import software.amazon.awssdk.awscore.exception.AwsServiceException;
import software.amazon.awssdk.services.autoscaling.AutoScalingClient;
import software.amazon.awssdk.services.autoscaling.model.AutoScalingGroup;
import software.amazon.awssdk.services.autoscaling.model.DescribeAutoScalingGroupsRequest;
import software.amazon.awssdk.services.autoscaling.model.DescribeAutoScalingGroupsResponse;
import software.amazon.awssdk.services.autoscaling.model.SetDesiredCapacityRequest;
import software.amazon.awssdk.services.cloudformation.CloudFormationClient;
import software.amazon.awssdk.services.cloudformation.model.DescribeStackResourcesRequest;
import software.amazon.awssdk.services.cloudformation.model.DescribeStackResourcesResponse;
import software.amazon.awssdk.services.cloudformation.model.StackResource;
import software.amazon.awssdk.services.cloudwatch.CloudWatchClient;
import software.amazon.awssdk.services.cloudwatch.model.DeleteAlarmsRequest;
import software.amazon.awssdk.services.cloudwatch.model.PutMetricAlarmRequest;
import software.amazon.awssdk.services.ec2.Ec2Client;
import software.amazon.awssdk.services.ec2.model.*;
import software.amazon.awssdk.services.elasticloadbalancingv2.ElasticLoadBalancingV2Client;
import software.amazon.awssdk.services.elasticloadbalancingv2.model.DescribeLoadBalancersRequest;
import software.amazon.awssdk.services.elasticloadbalancingv2.model.DescribeLoadBalancersResponse;
import software.amazon.awssdk.services.elasticloadbalancingv2.model.LoadBalancer;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.GetObjectRequest;
import software.amazon.awssdk.services.s3.model.GetObjectResponse;
import software.amazon.awssdk.services.s3.S3Utilities;
import software.amazon.awssdk.regions.Region;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.util.*;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class AwsHelperServiceTest {

    private static final String TEST_INSTANCE_ID = "testInstanceId";

    @Mock
    private AutoScalingClient autoScalingClient;

    @Mock
    private CloudFormationClient cloudFormationClient;

    @Mock
    private CloudWatchClient cloudWatchClient;

    @Mock
    private Ec2Client ec2Client;

    @Mock
    private ElasticLoadBalancingV2Client elasticLoadBalancingClient;

    @Mock
    private S3Client s3Client;

    @InjectMocks
    private AwsHelperService awsHelperService;

    @Test
    public void testAddTags() {
        final Map<String, String> tags = new HashMap<>();
        tags.put("key1", "value1");
        awsHelperService.addTags(TEST_INSTANCE_ID, tags);

        final ArgumentCaptor<CreateTagsRequest> argumentCaptor = ArgumentCaptor.forClass(CreateTagsRequest.class);
        verify(ec2Client).createTags(argumentCaptor.capture());
        assertThat(argumentCaptor.getValue().resources().get(0), equalTo(TEST_INSTANCE_ID));
        assertThat(argumentCaptor.getValue().tags().get(0).key(), equalTo("key1"));
        assertThat(argumentCaptor.getValue().tags().get(0).value(), equalTo("value1"));
    }

    @Test
    public void testCreateContentHealthCheckAlarm() {
        final String alarmName = "testAlarmName";
        final String alarmDescription = "testAlarmDescription";
        final String publishInstanceId = "testPublishInstanceId";
        final String namespace = "testNamespace";
        final String topicArn = "testTopicArn";

        awsHelperService.createContentHealthCheckAlarm(alarmName, alarmDescription, publishInstanceId, namespace, topicArn);

        final ArgumentCaptor<PutMetricAlarmRequest> argumentCaptor = ArgumentCaptor.forClass(PutMetricAlarmRequest.class);
        verify(cloudWatchClient).putMetricAlarm(argumentCaptor.capture());
        assertThat(argumentCaptor.getValue().alarmName(), equalTo(alarmName));
        assertThat(argumentCaptor.getValue().alarmDescription(), equalTo(alarmDescription));
        assertThat(argumentCaptor.getValue().dimensions().get(0).value(), equalTo(publishInstanceId));
        assertThat(argumentCaptor.getValue().namespace(), equalTo(namespace));
        assertThat(argumentCaptor.getValue().alarmActions().get(0), equalTo(topicArn));
    }

    @Test
    public void testCreateSnapshot() {
        final String snapshotId = "testSnapshotId";
        final CreateSnapshotResponse response = CreateSnapshotResponse.builder()
                .snapshotId(snapshotId)
                .build();

        when(ec2Client.createSnapshot(any(CreateSnapshotRequest.class))).thenReturn(response);

        final String volumeId = "testVolumeId";
        final String description = "Test Description";
        assertThat(awsHelperService.createSnapshot(volumeId, description), equalTo(snapshotId));

        final ArgumentCaptor<CreateSnapshotRequest> argumentCaptor = ArgumentCaptor.forClass(CreateSnapshotRequest.class);
        verify(ec2Client).createSnapshot(argumentCaptor.capture());
        assertThat(argumentCaptor.getValue().volumeId(), equalTo(volumeId));
        assertThat(argumentCaptor.getValue().description(), equalTo(description));
    }

    @Test
    public void testDeleteAlarm() {
        final String alarmName = "testAlarmName";
        awsHelperService.deleteAlarm(alarmName);

        final ArgumentCaptor<DeleteAlarmsRequest> argumentCaptor = ArgumentCaptor.forClass(DeleteAlarmsRequest.class);
        verify(cloudWatchClient).deleteAlarms(argumentCaptor.capture());
        assertThat(argumentCaptor.getValue().alarmNames().get(0), equalTo(alarmName));
    }

    @Test
    public void testGetAutoScalingGroupDesiredCapacity() {
        final int desiredCapacity = 5;

        final AutoScalingGroup group = AutoScalingGroup.builder()
                .desiredCapacity(desiredCapacity)
                .build();
        final DescribeAutoScalingGroupsResponse response = DescribeAutoScalingGroupsResponse.builder()
                .autoScalingGroups(group)
                .build();

        when(autoScalingClient.describeAutoScalingGroups(any(DescribeAutoScalingGroupsRequest.class))).thenReturn(response);

        final String groupName = "testGroupName";
        assertThat(awsHelperService.getAutoScalingGroupDesiredCapacity(groupName), equalTo(desiredCapacity));

        final ArgumentCaptor<DescribeAutoScalingGroupsRequest> argumentCaptor = ArgumentCaptor.forClass(DescribeAutoScalingGroupsRequest.class);
        verify(autoScalingClient).describeAutoScalingGroups(argumentCaptor.capture());
        assertThat(argumentCaptor.getValue().autoScalingGroupNames().get(0), equalTo(groupName));
    }

    @Test
    public void testGetAvailabilityZone() {
        final String availabilityZone = "testZone";
        final Placement placement = Placement.builder().availabilityZone(availabilityZone).build();
        final Instance instance = Instance.builder().placement(placement).build();
        final Reservation reservation = Reservation.builder().instances(instance).build();
        final DescribeInstancesResponse response = DescribeInstancesResponse.builder()
                .reservations(reservation)
                .build();

        when(ec2Client.describeInstances(any(DescribeInstancesRequest.class))).thenReturn(response);

        assertThat(awsHelperService.getAvailabilityZone(TEST_INSTANCE_ID), equalTo(availabilityZone));

        final ArgumentCaptor<DescribeInstancesRequest> argumentCaptor = ArgumentCaptor.forClass(DescribeInstancesRequest.class);
        verify(ec2Client).describeInstances(argumentCaptor.capture());
        assertThat(argumentCaptor.getValue().instanceIds().get(0), equalTo(TEST_INSTANCE_ID));
    }

    @Test
    public void testGetElbName() {
        final LoadBalancer loadBalancer = LoadBalancer.builder()
                .loadBalancerName("testElbName")
                .build();

        final DescribeLoadBalancersResponse response = DescribeLoadBalancersResponse.builder()
                .loadBalancers(loadBalancer)
                .build();

        when(elasticLoadBalancingClient.describeLoadBalancers(any(DescribeLoadBalancersRequest.class))).thenReturn(response);

        final String elbArn = "arn:aws:elasticloadbalancing:ap-southeast-2:918473058104:loadbalancer/app/bloch-Autho-1GWRY37R2O1GQ/809dd96ac8ee4083";
        assertThat(awsHelperService.getElbName(elbArn), equalTo(loadBalancer.loadBalancerName()));

        final ArgumentCaptor<DescribeLoadBalancersRequest> argumentCaptor = ArgumentCaptor.forClass(DescribeLoadBalancersRequest.class);
        verify(elasticLoadBalancingClient).describeLoadBalancers(argumentCaptor.capture());
        assertThat(argumentCaptor.getValue().loadBalancerArns().get(0), equalTo(elbArn));
    }

    @Test
    public void testGetElbDnsName() {
        final LoadBalancer loadBalancer = LoadBalancer.builder()
                .dnsName("testDnsName")
                .build();

        final DescribeLoadBalancersResponse response = DescribeLoadBalancersResponse.builder()
                .loadBalancers(loadBalancer)
                .build();

        when(elasticLoadBalancingClient.describeLoadBalancers(any(DescribeLoadBalancersRequest.class))).thenReturn(response);

        final String elbName = "testElbName";
        assertThat(awsHelperService.getElbDnsName(elbName), equalTo(loadBalancer.dnsName()));

        final ArgumentCaptor<DescribeLoadBalancersRequest> argumentCaptor = ArgumentCaptor.forClass(DescribeLoadBalancersRequest.class);
        verify(elasticLoadBalancingClient).describeLoadBalancers(argumentCaptor.capture());
        assertThat(argumentCaptor.getValue().names().get(0), equalTo(elbName));
    }

    @Test
    public void testGetInstanceIdsForAutoScalingGroup() {
        final software.amazon.awssdk.services.autoscaling.model.Instance instance =
                software.amazon.awssdk.services.autoscaling.model.Instance.builder()
                        .instanceId(TEST_INSTANCE_ID)
                        .build();

        final AutoScalingGroup group = AutoScalingGroup.builder()
                .instances(instance)
                .build();

        final DescribeAutoScalingGroupsResponse response = DescribeAutoScalingGroupsResponse.builder()
                .autoScalingGroups(group)
                .build();

        when(autoScalingClient.describeAutoScalingGroups(any(DescribeAutoScalingGroupsRequest.class))).thenReturn(response);

        final String groupName = "testGroupName";
        assertThat(awsHelperService.getInstanceIdsForAutoScalingGroup(groupName).get(0), equalTo(TEST_INSTANCE_ID));

        final ArgumentCaptor<DescribeAutoScalingGroupsRequest> argumentCaptor = ArgumentCaptor.forClass(DescribeAutoScalingGroupsRequest.class);
        verify(autoScalingClient).describeAutoScalingGroups(argumentCaptor.capture());
        assertThat(argumentCaptor.getValue().autoScalingGroupNames().get(0), equalTo(groupName));
    }

@Test
public void testGetInstancesForAutoScalingGroup() {
    final software.amazon.awssdk.services.autoscaling.model.Instance instance =
            software.amazon.awssdk.services.autoscaling.model.Instance.builder()
                    .instanceId(TEST_INSTANCE_ID)
                    .availabilityZone("testZone")
                    .build();

    final AutoScalingGroup group = AutoScalingGroup.builder()
            .instances(instance)
            .build();

    final DescribeAutoScalingGroupsResponse response = DescribeAutoScalingGroupsResponse.builder()
            .autoScalingGroups(group)
            .build();

    when(autoScalingClient.describeAutoScalingGroups(any(DescribeAutoScalingGroupsRequest.class)))
            .thenReturn(response);

    final String groupName = "testGroupName";

    // Option A: Use original Hamcrest matchers (recommended if EC2Instance uses standard Java Beans getters)
    assertThat(awsHelperService.getInstancesForAutoScalingGroup(groupName).get(0),
            allOf(
                    hasProperty("instanceId", equalTo(TEST_INSTANCE_ID)),
                    hasProperty("availabilityZone", equalTo("testZone"))
            ));

    final ArgumentCaptor<DescribeAutoScalingGroupsRequest> argumentCaptor = 
            ArgumentCaptor.forClass(DescribeAutoScalingGroupsRequest.class);
    verify(autoScalingClient).describeAutoScalingGroups(argumentCaptor.capture());
    assertThat(argumentCaptor.getValue().autoScalingGroupNames().get(0), equalTo(groupName));
}

    @Test
public void testGetLaunchTime() {
    final Instant launchTime = Instant.now();
    final Instance instance = Instance.builder().launchTime(launchTime).build();
    final Reservation reservation = Reservation.builder().instances(instance).build();
    final DescribeInstancesResponse response = DescribeInstancesResponse.builder()
            .reservations(reservation)
            .build();

    when(ec2Client.describeInstances(any(DescribeInstancesRequest.class))).thenReturn(response);

    // Convert launchTime (Instant) to Date to match awsHelperService.getLaunchTime()'s return type
    assertThat(awsHelperService.getLaunchTime(TEST_INSTANCE_ID), equalTo(Date.from(launchTime)));

    final ArgumentCaptor<DescribeInstancesRequest> argumentCaptor = ArgumentCaptor.forClass(DescribeInstancesRequest.class);
    verify(ec2Client).describeInstances(argumentCaptor.capture());
    assertThat(argumentCaptor.getValue().instanceIds().get(0), equalTo(TEST_INSTANCE_ID));
}

    @Test
    public void testGetLaunchTime_NoInstance() {
        when(ec2Client.describeInstances(any(DescribeInstancesRequest.class)))
                .thenThrow(AwsServiceException.builder().message("Instance not found").build());

        assertThrows(AwsServiceException.class, () -> {
            awsHelperService.getLaunchTime(TEST_INSTANCE_ID);
        });
    }

    @Test
    public void testGetPrivateIp() {
        final String privateIp = "0.0.0.0";
        final Instance instance = Instance.builder().privateIpAddress(privateIp).build();
        final Reservation reservation = Reservation.builder().instances(instance).build();
        final DescribeInstancesResponse response = DescribeInstancesResponse.builder()
                .reservations(reservation)
                .build();

        when(ec2Client.describeInstances(any(DescribeInstancesRequest.class))).thenReturn(response);

        assertThat(awsHelperService.getPrivateIp(TEST_INSTANCE_ID), equalTo(privateIp));

        final ArgumentCaptor<DescribeInstancesRequest> argumentCaptor = ArgumentCaptor.forClass(DescribeInstancesRequest.class);
        verify(ec2Client).describeInstances(argumentCaptor.capture());
        assertThat(argumentCaptor.getValue().instanceIds().get(0), equalTo(TEST_INSTANCE_ID));
    }

    @Test
    public void testGetPrivateIp_NoInstance() {
        when(ec2Client.describeInstances(any(DescribeInstancesRequest.class)))
                .thenThrow(AwsServiceException.builder().message("Instance not found").build());

        assertThrows(AwsServiceException.class, () -> {
            awsHelperService.getPrivateIp(TEST_INSTANCE_ID);
        });
    }

    @Test
    public void testGetStackPhysicalResourceId() {
        final StackResource stackResource1 = StackResource.builder()
                .logicalResourceId("logical1")
                .physicalResourceId("physical1")
                .build();

        final StackResource stackResource2 = StackResource.builder()
                .logicalResourceId("logical2")
                .physicalResourceId("physical2")
                .build();

        final DescribeStackResourcesResponse response = DescribeStackResourcesResponse.builder()
                .stackResources(stackResource1, stackResource2)
                .build();

        when(cloudFormationClient.describeStackResources(any(DescribeStackResourcesRequest.class))).thenReturn(response);

        assertThat(awsHelperService.getStackPhysicalResourceId("testStackName", "logical2"), equalTo("physical2"));

        final ArgumentCaptor<DescribeStackResourcesRequest> argumentCaptor = ArgumentCaptor.forClass(DescribeStackResourcesRequest.class);
        verify(cloudFormationClient).describeStackResources(argumentCaptor.capture());
        assertThat(argumentCaptor.getValue().stackName(), equalTo("testStackName"));
    }

    @Test
    public void testGetStackPhysicalResourceId_NoStackResource() {
        final DescribeStackResourcesResponse response = DescribeStackResourcesResponse.builder()
                .stackResources(Collections.emptyList())
                .build();

        when(cloudFormationClient.describeStackResources(any(DescribeStackResourcesRequest.class))).thenReturn(response);

        assertThrows(NoSuchElementException.class, () -> {
            awsHelperService.getStackPhysicalResourceId("testStackName", "logical1");
        });
    }

    @Test
    public void testGetTags() {
        TagDescription tag1 = TagDescription.builder().key("key1").value("value1").build();
        TagDescription tag2 = TagDescription.builder().key("key2").value("value2").build();

        DescribeTagsResponse describeTagResult = DescribeTagsResponse.builder()
                .tags(tag1, tag2)
                .build();

        when(ec2Client.describeTags(any(DescribeTagsRequest.class))).thenReturn(describeTagResult);

        Map<String, String> tagMap = awsHelperService.getTags(TEST_INSTANCE_ID);

        assertThat(tagMap.get(tag1.key()), equalTo(tag1.value()));
        assertThat(tagMap.get(tag2.key()), equalTo(tag2.value()));
    }

    @Test
    public void testGetVolumeId() {
        final String volumeId = "testVolumeId";
        final String deviceName = "testDeviceName";

        final EbsInstanceBlockDevice ebs = EbsInstanceBlockDevice.builder().volumeId(volumeId).build();
        final InstanceBlockDeviceMapping mapping = InstanceBlockDeviceMapping.builder()
                .deviceName(deviceName)
                .ebs(ebs)
                .build();

        final DescribeInstanceAttributeResponse response = DescribeInstanceAttributeResponse.builder()
                .blockDeviceMappings(mapping)
                .build();

        when(ec2Client.describeInstanceAttribute(any(DescribeInstanceAttributeRequest.class))).thenReturn(response);

        assertThat(awsHelperService.getVolumeId(TEST_INSTANCE_ID, deviceName), equalTo(volumeId));

        final ArgumentCaptor<DescribeInstanceAttributeRequest> argumentCaptor = ArgumentCaptor.forClass(DescribeInstanceAttributeRequest.class);
        verify(ec2Client).describeInstanceAttribute(argumentCaptor.capture());
        assertThat(argumentCaptor.getValue().instanceId(), equalTo(TEST_INSTANCE_ID));
    }

    @Test
    public void testGetVolumeId_NoEbsInstance() {
        final DescribeInstanceAttributeResponse response = DescribeInstanceAttributeResponse.builder()
                .blockDeviceMappings(Collections.emptyList())
                .build();

        when(ec2Client.describeInstanceAttribute(any(DescribeInstanceAttributeRequest.class))).thenReturn(response);

        assertThrows(NoSuchElementException.class, () -> {
            awsHelperService.getVolumeId(TEST_INSTANCE_ID, "testDeviceName");
        });
    }

    @Test
    public void testIsInstanceRunning() {
        final DescribeInstancesResponse runningResponse = DescribeInstancesResponse.builder()
                .reservations(Reservation.builder()
                        .instances(Instance.builder()
                                .state(InstanceState.builder().name(InstanceStateName.RUNNING).build())
                                .build())
                        .build())
                .build();

        final DescribeInstancesResponse stoppedResponse = DescribeInstancesResponse.builder()
                .reservations(Reservation.builder()
                        .instances(Instance.builder()
                                .state(InstanceState.builder().name(InstanceStateName.STOPPED).build())
                                .build())
                        .build())
                .build();

        when(ec2Client.describeInstances(any(DescribeInstancesRequest.class)))
                .thenReturn(runningResponse)
                .thenReturn(stoppedResponse);

        assertThat(awsHelperService.isInstanceRunning(TEST_INSTANCE_ID), is(true));

        final ArgumentCaptor<DescribeInstancesRequest> argumentCaptor = ArgumentCaptor.forClass(DescribeInstancesRequest.class);
        verify(ec2Client).describeInstances(argumentCaptor.capture());
        assertThat(argumentCaptor.getValue().instanceIds().get(0), equalTo(TEST_INSTANCE_ID));

        assertThat(awsHelperService.isInstanceRunning(TEST_INSTANCE_ID), is(false));
        verify(ec2Client, times(2)).describeInstances(argumentCaptor.capture());
        assertThat(argumentCaptor.getValue().instanceIds().get(0), equalTo(TEST_INSTANCE_ID));
    }

    @Test
    public void testIsInstanceRunning_Default() {
        final DescribeInstancesResponse response = DescribeInstancesResponse.builder()
                .reservations(Collections.emptyList())
                .build();

        when(ec2Client.describeInstances(any(DescribeInstancesRequest.class))).thenReturn(response);

        assertThat(awsHelperService.isInstanceRunning(TEST_INSTANCE_ID), is(false));
    }

    @Test
    public void testReadFileFromS3() throws IOException {
        final String testInput = "Test Input";
        final ResponseInputStream<GetObjectResponse> responseInputStream = new ResponseInputStream<>(
                GetObjectResponse.builder().build(),
                AbortableInputStream.create(new ByteArrayInputStream(testInput.getBytes(StandardCharsets.UTF_8)))
        );

        when(s3Client.utilities()).thenReturn(S3Utilities.builder().region(Region.US_EAST_1).build());
        when(s3Client.getObject(any(GetObjectRequest.class))).thenReturn(responseInputStream);

        // Append an object key to the S3 URL path
        final String s3Url = "http://bucket.s3.amazonaws.com/test-key.txt";
        assertThat(awsHelperService.readFileFromS3(s3Url), equalTo(testInput));

        final ArgumentCaptor<GetObjectRequest> argumentCaptor = ArgumentCaptor.forClass(GetObjectRequest.class);
        verify(s3Client).getObject(argumentCaptor.capture());
        assertThat(argumentCaptor.getValue().bucket(), equalTo("bucket"));
        assertThat(argumentCaptor.getValue().key(), equalTo("test-key.txt"));
    }

    @Test
    public void testSetAutoScalingGroupDesiredCapacity() {
        final String groupName = "testGroupName";
        final int desiredCapacity = 5;

        awsHelperService.setAutoScalingGroupDesiredCapacity(groupName, desiredCapacity);

        final ArgumentCaptor<SetDesiredCapacityRequest> argumentCaptor = ArgumentCaptor.forClass(SetDesiredCapacityRequest.class);
        verify(autoScalingClient).setDesiredCapacity(argumentCaptor.capture());
        assertThat(argumentCaptor.getValue().autoScalingGroupName(), equalTo(groupName));
        assertThat(argumentCaptor.getValue().desiredCapacity(), equalTo(desiredCapacity));
    }

    @Test
    public void testTerminateInstance() {
        awsHelperService.terminateInstance(TEST_INSTANCE_ID);

        final ArgumentCaptor<TerminateInstancesRequest> argumentCaptor = ArgumentCaptor.forClass(TerminateInstancesRequest.class);
        verify(ec2Client).terminateInstances(argumentCaptor.capture());
        assertThat(argumentCaptor.getValue().instanceIds().get(0), equalTo(TEST_INSTANCE_ID));
    }
}