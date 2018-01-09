package com.shinesolutions.aemorchestrator.service;

import com.amazonaws.AmazonServiceException;
import com.amazonaws.services.autoscaling.AmazonAutoScalingClient;
import com.amazonaws.services.autoscaling.model.DescribeAutoScalingGroupsRequest;
import com.amazonaws.services.autoscaling.model.DescribeAutoScalingGroupsResult;
import com.amazonaws.services.autoscaling.model.SetDesiredCapacityRequest;
import com.amazonaws.services.cloudformation.AmazonCloudFormationClient;
import com.amazonaws.services.cloudformation.model.DescribeStackResourcesRequest;
import com.amazonaws.services.cloudformation.model.DescribeStackResourcesResult;
import com.amazonaws.services.cloudformation.model.StackResource;
import com.amazonaws.services.ec2.AmazonEC2;
import com.amazonaws.services.ec2.model.*;
import com.amazonaws.services.elasticloadbalancing.AmazonElasticLoadBalancing;
import com.amazonaws.services.elasticloadbalancing.model.DescribeLoadBalancersRequest;
import com.amazonaws.services.elasticloadbalancing.model.DescribeLoadBalancersResult;
import com.amazonaws.services.elasticloadbalancing.model.LoadBalancerDescription;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import java.util.*;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyInt;
import static org.mockito.Mockito.*;


@RunWith(MockitoJUnitRunner.class)
public class AwsHelperServiceTest {

    private static final String TEST_INSTANCE_ID = "testInstanceId";

    @Mock
    private AmazonAutoScalingClient amazonAutoScalingClient;

    @Mock
    private AmazonCloudFormationClient amazonCloudFormationClient;

    @Mock
    private AmazonEC2 amazonEC2Client;

    @Mock
    private AmazonElasticLoadBalancing amazonElbClient;

    @InjectMocks
    private AwsHelperService awsHelperService;

    @Test
    public void testAddTags() {
        Map<String, String> tags = new HashMap<>();
        tags.put("key1", "value1");
        awsHelperService.addTags(TEST_INSTANCE_ID, tags);

        final ArgumentCaptor<CreateTagsRequest> argumentCaptor = ArgumentCaptor.forClass(CreateTagsRequest.class);
        verify(amazonEC2Client).createTags(argumentCaptor.capture());

        assertThat(argumentCaptor.getValue().getResources().get(0), equalTo(TEST_INSTANCE_ID));
        assertThat(argumentCaptor.getValue().getTags().get(0).getKey(), equalTo("key1"));
        assertThat(argumentCaptor.getValue().getTags().get(0).getValue(), equalTo("value1"));
    }

    @Test
    public void testCreateSnapshot() {
        final String snapshotId = "testSnapshotId";
        final CreateSnapshotResult result = mock(CreateSnapshotResult.class, RETURNS_DEEP_STUBS);
        when(result.getSnapshot().getSnapshotId()).thenReturn(snapshotId);

        when(amazonEC2Client.createSnapshot(any(CreateSnapshotRequest.class))).thenReturn(result);

        assertThat(awsHelperService.createSnapshot("testVolumeId", "Test Description"), equalTo(snapshotId));
        verify(amazonEC2Client).createSnapshot(any(CreateSnapshotRequest.class));
    }

    @Test
    public void testGetAutoScalingGroupDesiredCapacity() {
        final int desiredCapacity = 5;

        final DescribeAutoScalingGroupsResult result = mock(DescribeAutoScalingGroupsResult.class, RETURNS_DEEP_STUBS);
        when(result.getAutoScalingGroups().get(0).getDesiredCapacity()).thenReturn(desiredCapacity);

        when(amazonAutoScalingClient.describeAutoScalingGroups(any(DescribeAutoScalingGroupsRequest.class))).thenReturn(result);

        assertThat(awsHelperService.getAutoScalingGroupDesiredCapacity("testGroupName"), equalTo(desiredCapacity));
    }

    @Test
    public void testGetAvailabilityZone() {
        final String availabilityZone = "testZone";
        final DescribeInstancesResult result = mock(DescribeInstancesResult.class, RETURNS_DEEP_STUBS);
        when(result.getReservations().get(0).getInstances().get(0).getPlacement().getAvailabilityZone()).thenReturn(availabilityZone);

        when(amazonEC2Client.describeInstances(any(DescribeInstancesRequest.class))).thenReturn(result);

        assertThat(awsHelperService.getAvailabilityZone(TEST_INSTANCE_ID), equalTo(availabilityZone));
    }

    @Test
    public void testGetElbDnsName() {
        final LoadBalancerDescription description = new LoadBalancerDescription();
        description.setDNSName("testDnsName");

        final List<LoadBalancerDescription> descriptions = new ArrayList<>();
        descriptions.add(description);

        final DescribeLoadBalancersResult result = mock(DescribeLoadBalancersResult.class);
        when(result.getLoadBalancerDescriptions()).thenReturn(descriptions);

        when(amazonElbClient.describeLoadBalancers(any(DescribeLoadBalancersRequest.class))).thenReturn(result);

        assertThat(awsHelperService.getElbDnsName("testElbName"), equalTo(description.getDNSName()));
    }

    @Test
    public void testGetInstanceIdsForAutoScalingGroup() {
        final com.amazonaws.services.autoscaling.model.Instance instance = new com.amazonaws.services.autoscaling.model.Instance();
        instance.setInstanceId(TEST_INSTANCE_ID);

        final List<com.amazonaws.services.autoscaling.model.Instance> instanceList = new ArrayList<>();
        instanceList.add(instance);

        final DescribeAutoScalingGroupsResult result = mock(DescribeAutoScalingGroupsResult.class, RETURNS_DEEP_STUBS);
        when(result.getAutoScalingGroups().get(0).getInstances()).thenReturn(instanceList);

        when(amazonAutoScalingClient.describeAutoScalingGroups(any(DescribeAutoScalingGroupsRequest.class))).thenReturn(result);

        assertThat(awsHelperService.getInstanceIdsForAutoScalingGroup("testGroupName").get(0), equalTo(TEST_INSTANCE_ID));
    }

    @Test
    public void testGetInstancesForAutoScalingGroup() {
        final com.amazonaws.services.autoscaling.model.Instance instance = new com.amazonaws.services.autoscaling.model.Instance();
        instance.setInstanceId(TEST_INSTANCE_ID);
        instance.setAvailabilityZone("testZone");

        final List<com.amazonaws.services.autoscaling.model.Instance> instanceList = new ArrayList<>();
        instanceList.add(instance);

        final DescribeAutoScalingGroupsResult result = mock(DescribeAutoScalingGroupsResult.class, RETURNS_DEEP_STUBS);
        when(result.getAutoScalingGroups().get(0).getInstances()).thenReturn(instanceList);

        when(amazonAutoScalingClient.describeAutoScalingGroups(any(DescribeAutoScalingGroupsRequest.class))).thenReturn(result);

        assertThat(awsHelperService.getInstancesForAutoScalingGroup("testGroupName").get(0),
                allOf(
                        hasProperty("instanceId", equalTo(TEST_INSTANCE_ID)),
                        hasProperty("availabilityZone", equalTo("testZone"))
                ));
    }

    @Test
    public void testGetLaunchTime() {
        final Date launchDate = new Date();
        final DescribeInstancesResult result = mock(DescribeInstancesResult.class, RETURNS_DEEP_STUBS);
        when(result.getReservations().get(0).getInstances().get(0).getLaunchTime()).thenReturn(launchDate);

        when(amazonEC2Client.describeInstances(any(DescribeInstancesRequest.class))).thenReturn(result);

        assertThat(awsHelperService.getLaunchTime(TEST_INSTANCE_ID), equalTo(launchDate));
    }

    @Test(expected = AmazonServiceException.class)
    public void testGetLaunchTime_NoInstance() {
        awsHelperService.getLaunchTime(TEST_INSTANCE_ID);
    }

    @Test
    public void testGetPrivateIp() {
        final String privateIp = "0.0.0.0";
        final DescribeInstancesResult result = mock(DescribeInstancesResult.class, RETURNS_DEEP_STUBS);
        when(result.getReservations().get(0).getInstances().get(0).getPrivateIpAddress()).thenReturn(privateIp);

        when(amazonEC2Client.describeInstances(any(DescribeInstancesRequest.class))).thenReturn(result);

        assertThat(awsHelperService.getPrivateIp(TEST_INSTANCE_ID), equalTo(privateIp));
    }

    @Test(expected = AmazonServiceException.class)
    public void testGetPrivateIp_NoInstance() {
        awsHelperService.getPrivateIp(TEST_INSTANCE_ID);
    }

    @Test
    public void testGetStackPhysicalResourceId() {
        StackResource stackResource1 = new StackResource();
        stackResource1.setLogicalResourceId("logical1");
        stackResource1.setPhysicalResourceId("physical1");

        StackResource stackResource2 = new StackResource();
        stackResource2.setLogicalResourceId("logical2");
        stackResource2.setPhysicalResourceId("physical2");

        List<StackResource> stackResources = new ArrayList<>();
        stackResources.add(stackResource1);
        stackResources.add(stackResource2);

        DescribeStackResourcesResult result = mock(DescribeStackResourcesResult.class);
        when(result.getStackResources()).thenReturn(stackResources);

        when(amazonCloudFormationClient.describeStackResources(any(DescribeStackResourcesRequest.class))).thenReturn(result);

        assertThat(awsHelperService.getStackPhysicalResourceId("testStackName", "logical2"), equalTo("physical2"));
    }

    @Test(expected = NoSuchElementException.class)
    public void testGetStackPhysicalResourceId_NoStackResource() {
        DescribeStackResourcesResult result = mock(DescribeStackResourcesResult.class);
        when(result.getStackResources()).thenReturn(new ArrayList<>());

        when(amazonCloudFormationClient.describeStackResources(any(DescribeStackResourcesRequest.class))).thenReturn(result);

        awsHelperService.getStackPhysicalResourceId("testStackName", "logical1");
    }

    @Test
    public void testGetTags() {
        TagDescription tag1 = new TagDescription().withKey("key1").withValue("value1");
        TagDescription tag2 = new TagDescription().withKey("key2").withValue("value2");

        List<TagDescription> tagList = new ArrayList<>();
        tagList.add(tag1);
        tagList.add(tag2);
        DescribeTagsResult describeTagResult = new DescribeTagsResult();
        describeTagResult.setTags(tagList);

        when(amazonEC2Client.describeTags(any(DescribeTagsRequest.class))).thenReturn(describeTagResult);

        Map<String, String> tagMap = awsHelperService.getTags(TEST_INSTANCE_ID);

        assertThat(tagMap.get(tag1.getKey()), equalTo(tag1.getValue()));
        assertThat(tagMap.get(tag2.getKey()), equalTo(tag2.getValue()));
    }

    @Test
    public void testGetVolumeId() {
        final String volumeId = "testVolumeId";
        EbsInstanceBlockDevice ebs = new EbsInstanceBlockDevice();
        ebs.setVolumeId(volumeId);

        final String deviceName = "testDeviceName";
        InstanceBlockDeviceMapping instanceBlockDeviceMapping = new InstanceBlockDeviceMapping();
        instanceBlockDeviceMapping.setDeviceName(deviceName);
        instanceBlockDeviceMapping.setEbs(ebs);


        List<InstanceBlockDeviceMapping> instanceBlockDeviceMappings = new ArrayList<>();
        instanceBlockDeviceMappings.add(instanceBlockDeviceMapping);

        DescribeInstanceAttributeResult result = mock(DescribeInstanceAttributeResult.class, RETURNS_DEEP_STUBS);
        when(result.getInstanceAttribute().getBlockDeviceMappings()).thenReturn(instanceBlockDeviceMappings);

        when(amazonEC2Client.describeInstanceAttribute(any(DescribeInstanceAttributeRequest.class))).thenReturn(result);

        assertThat(awsHelperService.getVolumeId(TEST_INSTANCE_ID, deviceName), equalTo(volumeId));
    }

    @Test(expected = NoSuchElementException.class)
    public void testGetVolumeId_NoEbsInstance() {
        DescribeInstanceAttributeResult result = mock(DescribeInstanceAttributeResult.class, RETURNS_DEEP_STUBS);
        when(result.getInstanceAttribute().getBlockDeviceMappings()).thenReturn(new ArrayList<>());

        when(amazonEC2Client.describeInstanceAttribute(any(DescribeInstanceAttributeRequest.class))).thenReturn(result);

        awsHelperService.getVolumeId(TEST_INSTANCE_ID, "testDeviceName");
    }

    @Test
    public void testIsInstanceRunning() {
        final InstanceState instanceState = new InstanceState();
        final DescribeInstancesResult result = mock(DescribeInstancesResult.class, RETURNS_DEEP_STUBS);
        when(result.getReservations().get(0).getInstances().get(0).getState()).thenReturn(instanceState);

        when(amazonEC2Client.describeInstances(any(DescribeInstancesRequest.class))).thenReturn(result);

        instanceState.setName(InstanceStateName.Running);
        assertThat(awsHelperService.isInstanceRunning(TEST_INSTANCE_ID), is(true));

        instanceState.setName(InstanceStateName.Stopped);
        assertThat(awsHelperService.isInstanceRunning(TEST_INSTANCE_ID), is(false));
    }

    @Test
    public void testIsInstanceRunning_Default() {
        final DescribeInstancesResult result = mock(DescribeInstancesResult.class, RETURNS_DEEP_STUBS);
        when(result.getReservations().get(anyInt())).thenThrow(new IndexOutOfBoundsException());

        when(amazonEC2Client.describeInstances(any(DescribeInstancesRequest.class))).thenReturn(result);

        assertThat(awsHelperService.isInstanceRunning(TEST_INSTANCE_ID), is(false));
    }

    @Test
    public void testSetAutoScalingGroupDesiredCapacity() {
        final String groupName = "testGroupName";
        final int desiredCapacity = 5;

        awsHelperService.setAutoScalingGroupDesiredCapacity(groupName, desiredCapacity);

        final ArgumentCaptor<SetDesiredCapacityRequest> argumentCaptor = ArgumentCaptor.forClass(SetDesiredCapacityRequest.class);
        verify(amazonAutoScalingClient).setDesiredCapacity(argumentCaptor.capture());

        assertThat(argumentCaptor.getValue().getAutoScalingGroupName(), equalTo(groupName));
        assertThat(argumentCaptor.getValue().getDesiredCapacity(), equalTo(desiredCapacity));
    }

    @Test
    public void testTerminateInstance() {
        awsHelperService.terminateInstance(TEST_INSTANCE_ID);

        verify(amazonEC2Client).terminateInstances(any(TerminateInstancesRequest.class));
    }
}
