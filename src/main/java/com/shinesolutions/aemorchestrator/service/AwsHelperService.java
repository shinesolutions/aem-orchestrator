package com.shinesolutions.aemorchestrator.service;

import java.io.IOException;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import javax.annotation.Resource;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.retry.annotation.Backoff;
import org.springframework.retry.annotation.Retryable;
import org.springframework.stereotype.Component;

import com.amazonaws.AmazonServiceException;
import com.amazonaws.services.autoscaling.AmazonAutoScaling;
import com.amazonaws.services.autoscaling.model.AutoScalingGroup;
import com.amazonaws.services.autoscaling.model.DescribeAutoScalingGroupsRequest;
import com.amazonaws.services.autoscaling.model.DescribeAutoScalingGroupsResult;
import com.amazonaws.services.autoscaling.model.Instance;
import com.amazonaws.services.autoscaling.model.SetDesiredCapacityRequest;
import com.amazonaws.services.cloudformation.AmazonCloudFormation;
import com.amazonaws.services.cloudformation.model.DescribeStackResourcesRequest;
import com.amazonaws.services.cloudformation.model.DescribeStackResourcesResult;
import com.amazonaws.services.ec2.AmazonEC2;
import com.amazonaws.services.ec2.model.CreateSnapshotRequest;
import com.amazonaws.services.ec2.model.CreateSnapshotResult;
import com.amazonaws.services.ec2.model.CreateTagsRequest;
import com.amazonaws.services.ec2.model.DescribeInstanceAttributeRequest;
import com.amazonaws.services.ec2.model.DescribeInstanceAttributeResult;
import com.amazonaws.services.ec2.model.DescribeInstancesRequest;
import com.amazonaws.services.ec2.model.DescribeInstancesResult;
import com.amazonaws.services.ec2.model.DescribeTagsRequest;
import com.amazonaws.services.ec2.model.DescribeTagsResult;
import com.amazonaws.services.ec2.model.EbsInstanceBlockDevice;
import com.amazonaws.services.ec2.model.Filter;
import com.amazonaws.services.ec2.model.InstanceBlockDeviceMapping;
import com.amazonaws.services.ec2.model.InstanceState;
import com.amazonaws.services.ec2.model.InstanceStateName;
import com.amazonaws.services.ec2.model.Tag;
import com.amazonaws.services.ec2.model.TagDescription;
import com.amazonaws.services.ec2.model.TerminateInstancesRequest;
import com.amazonaws.services.elasticloadbalancing.AmazonElasticLoadBalancing;
import com.amazonaws.services.elasticloadbalancing.model.DescribeLoadBalancersRequest;
import com.amazonaws.services.elasticloadbalancing.model.DescribeLoadBalancersResult;
import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.AmazonS3URI;
import com.amazonaws.services.s3.model.GetObjectRequest;
import com.amazonaws.services.s3.model.S3Object;
import com.amazonaws.util.IOUtils;


/**
 * Helper class for performing a range of AWS functions
 */
@Component
public class AwsHelperService {
    
    private final Logger logger = LoggerFactory.getLogger(this.getClass());
    
    @Resource
    public AmazonEC2 amazonEC2Client;
    
    @Resource
    public AmazonElasticLoadBalancing amazonElbClient;
    
    @Resource
    public AmazonAutoScaling amazonAutoScalingClient;
    
    @Resource
    public AmazonCloudFormation amazonCloudFormationClient;
    
    @Resource
    public AmazonS3 amazonS3Client;
    
    /**
     * Return the DNS name for a given AWS ELB group name
     * @param elbName the ELB group name
     * @return String DNS name
     */
    public String getElbDnsName(String elbName) {
        DescribeLoadBalancersResult result = amazonElbClient.describeLoadBalancers(new DescribeLoadBalancersRequest()
            .withLoadBalancerNames(elbName));
        return result.getLoadBalancerDescriptions().get(0).getDNSName();
    }
    
    /**
     * Gets the private IP of a given AWS EC2 instance
     * Will automatically retry 10 times every 10 seconds if no instance is found
     * @param instanceId the EC2 instance ID
     * @return String private IP
     */
    @Retryable(maxAttempts=10, value=AmazonServiceException.class, backoff=@Backoff(delay=10000))
    public String getPrivateIp(String instanceId) {
        DescribeInstancesResult result = amazonEC2Client.describeInstances(
            new DescribeInstancesRequest().withInstanceIds(instanceId));
        
        try {
            return result.getReservations().get(0).getInstances().get(0).getPrivateIpAddress();
        } catch (Exception e) {
            throw new AmazonServiceException("Failed to get IP for instance ID: " + 
                instanceId + ". Instance may not be active", e);
        }
    }
    
    /**
     * Checks if a instance is in a 'running' state. Will return false if the instance 
     * is in any other of the possible states: pending, shutting-down, terminated, stopping or stopped.
     * @param instanceId EC2 instance id
     * @return true if the instance is in a 'running' state. False for any other state
     */
    public boolean isInstanceRunning(String instanceId) {
        DescribeInstancesResult result = amazonEC2Client.describeInstances(
            new DescribeInstancesRequest().withInstanceIds(instanceId));
        InstanceState state = result.getReservations().get(0).getInstances().get(0).getState();
        logger.debug("AWS instance " +  instanceId + " currently in state: " + state.getName());
        return InstanceStateName.fromValue(state.getName()) == InstanceStateName.Running;
    }
    
    /**
     * Terminates an EC2 instance for a given instance ID
     * @param instanceId the EC2 instance ID
     */
    public void terminateInstance(String instanceId) {
        amazonEC2Client.terminateInstances(new TerminateInstancesRequest().withInstanceIds(instanceId));
    }
    
    /**
     * Gets a map of tags for an AWS EC2 instance
     * @param instanceId the EC2 instance ID
     * @return Map of AWS tags
     */
    public Map<String, String> getTags(String instanceId) {
        Filter filter = new Filter("resource-id", Arrays.asList(instanceId));
        DescribeTagsResult result = amazonEC2Client.describeTags(new DescribeTagsRequest().withFilters(filter));
        return result.getTags().stream().collect(Collectors.toMap(TagDescription::getKey, TagDescription::getValue));
    }
    
    /**
     * Adds provided map of tags to the given instance
     * @param instanceId the EC2 instance ID
     * @param tags the Map of tags to add
     */
    public void addTags(String instanceId, Map<String, String> tags) {
        List <Tag> ec2Tags = tags.entrySet().stream().map(e -> 
            new Tag(e.getKey(), e.getValue())).collect(Collectors.toList());
        amazonEC2Client.createTags(new CreateTagsRequest().withResources(instanceId).withTags(ec2Tags));
    }
    
    
    /**
     * Gets a list of EC2 instance IDs for a given auto scaling group name
     * @param groupName auto scaling group name
     * @return List of string containing instance IDs
     */
    public List<String> getInstanceIdsForAutoScalingGroup(String groupName) {
        List<Instance> instanceList = getAutoScalingGroup(groupName).getInstances();
        return instanceList.stream().map(i -> i.getInstanceId()).collect(Collectors.toList());
    }
    
    /**
     * Gets the auto scaling group's desired capacity for a given group name
     * @param groupName auto scaling group name
     * @return int the desired capacity of the group
     */
    public int getAutoScalingGroupDesiredCapacity(String groupName) {
        return getAutoScalingGroup(groupName).getDesiredCapacity();
    }
    
    /**
     * Sets the auto scaling desired capacity for a given group name
     * @param groupName auto scaling group name
     * @param desiredCapacity the desired capacity of the group to set
     */
    public void setAutoScalingGroupDesiredCapacity(String groupName, int desiredCapacity) {
        SetDesiredCapacityRequest request = new SetDesiredCapacityRequest().
            withAutoScalingGroupName(groupName).withDesiredCapacity(desiredCapacity);
        amazonAutoScalingClient.setDesiredCapacity(request);
    }
    
    /**
     * Gets the volume id of a given instance and device name
     * @param instanceId the EC2 instance ID
     * @param deviceName the block device mapping name
     * @return Volume Id of the EBS block device
     */
    public String getVolumeId(String instanceId, String deviceName) {
        DescribeInstanceAttributeResult result = amazonEC2Client.describeInstanceAttribute(
            new DescribeInstanceAttributeRequest().withInstanceId(instanceId).withAttribute("blockDeviceMapping"));
        
        List<InstanceBlockDeviceMapping> instanceBlockDeviceMappings = 
            result.getInstanceAttribute().getBlockDeviceMappings();
        
        EbsInstanceBlockDevice ebsInstanceBlockDevice = instanceBlockDeviceMappings.stream().filter(
            m -> m.getDeviceName().equals(deviceName)).findFirst().get().getEbs();
        
        return ebsInstanceBlockDevice.getVolumeId();
    }
    
    /**
     * Creates a snapshot for a given volume
     * @param volumeId identifies the volume to snapshot
     * @param description of the new snap shot
     * @return Snapshot ID of the newly created snapshot
     */
    public String createSnapshot(String volumeId, String description) {
        CreateSnapshotResult result = amazonEC2Client.createSnapshot(
            new CreateSnapshotRequest().withVolumeId(volumeId).withDescription(description));
        return result.getSnapshot().getSnapshotId();
    }
    
    /**
     * Gets a physical resource ID on a given stack for a logical resource ID 
     * @param stackName the name of the cloud formation stack
     * @param logicalResourceId the logical name of the stack resource
     * @return Physical resource ID
     */
    public String getStackPhysicalResourceId(String stackName, String logicalResourceId) {
        DescribeStackResourcesResult result = amazonCloudFormationClient.describeStackResources(
            new DescribeStackResourcesRequest().withStackName(stackName));
        
        return result.getStackResources().stream().filter(s -> s.getLogicalResourceId().equals(logicalResourceId))
            .findFirst().get().getPhysicalResourceId();
    }
    
    /**
     * Reads a file from S3 into a String object
     * @param s3Uri (eg. s3://bucket/file.ext)
     * @return String containing the content of the file in S3
     * @throws IOException if error reading file
     */
    public String readFileFromS3(String s3Uri) throws IOException {
        AmazonS3URI s3FileUri = new AmazonS3URI(s3Uri);
        S3Object s3object = amazonS3Client.getObject(new GetObjectRequest(s3FileUri.getBucket(), s3FileUri.getKey()));
        
        return IOUtils.toString(s3object.getObjectContent());
    }
    
    
    private AutoScalingGroup getAutoScalingGroup(String groupName) {
        DescribeAutoScalingGroupsResult result = amazonAutoScalingClient.describeAutoScalingGroups(
            new DescribeAutoScalingGroupsRequest().withAutoScalingGroupNames(groupName));
        return result.getAutoScalingGroups().get(0);
    }

}
