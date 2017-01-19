package com.shinesolutions.aemorchestrator.service;

import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

import javax.annotation.Resource;

import org.springframework.stereotype.Component;

import com.amazonaws.services.autoscaling.AmazonAutoScaling;
import com.amazonaws.services.autoscaling.model.AutoScalingGroup;
import com.amazonaws.services.autoscaling.model.DescribeAutoScalingGroupsRequest;
import com.amazonaws.services.autoscaling.model.DescribeAutoScalingGroupsResult;
import com.amazonaws.services.autoscaling.model.Instance;
import com.amazonaws.services.autoscaling.model.SetDesiredCapacityRequest;
import com.amazonaws.services.ec2.AmazonEC2;
import com.amazonaws.services.ec2.model.DescribeInstancesRequest;
import com.amazonaws.services.ec2.model.DescribeInstancesResult;
import com.amazonaws.services.ec2.model.DescribeTagsRequest;
import com.amazonaws.services.ec2.model.DescribeTagsResult;
import com.amazonaws.services.ec2.model.Filter;
import com.amazonaws.services.ec2.model.TagDescription;
import com.amazonaws.services.ec2.model.TerminateInstancesRequest;
import com.amazonaws.services.elasticloadbalancing.AmazonElasticLoadBalancing;
import com.amazonaws.services.elasticloadbalancing.model.DescribeLoadBalancersRequest;
import com.amazonaws.services.elasticloadbalancing.model.DescribeLoadBalancersResult;

/**
 * Helper class for performing a range of AWS functions
 */
@Component
public class AwsHelperService {
    
    @Resource
    public AmazonEC2 amazonEC2Client;
    
    @Resource
    public AmazonElasticLoadBalancing amazonElbClient;
    
    @Resource
    public AmazonAutoScaling amazonAutoScalingClient;
    
    
    private static final int NUM_RETRIES = 20;
    private static final int SECONDS_TO_WAIT_BETWEEN_RETRIES = 5;
    
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
     * @param instanceId the EC2 instance ID
     * @return String private ip
     */
    public String getPrivateIp(String instanceId) {
        DescribeInstancesResult result = amazonEC2Client.describeInstances(
            new DescribeInstancesRequest().withInstanceIds(instanceId));
        
        String privateIp = null;
        if(result.getReservations().size() > 0) {
            
            //If instance is still spinning up, then may need to wait
            for(int i = 0; i < NUM_RETRIES && privateIp != null; i++) {
                privateIp = result.getReservations().get(0).getInstances().get(0).getPrivateIpAddress();
                if(privateIp == null) {
                    try {
                        Thread.sleep(TimeUnit.SECONDS.toMillis(SECONDS_TO_WAIT_BETWEEN_RETRIES));
                    } catch (InterruptedException e) {}
                }
            }
        }
        
        return privateIp;
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
     * @return Map<String, String> map of AWS tags
     */
    public Map<String, String> getTags(String instanceId) {
        Filter filter = new Filter("resource-id", Arrays.asList(instanceId));
        DescribeTagsResult result = amazonEC2Client.describeTags(new DescribeTagsRequest().withFilters(filter));
        return result.getTags().stream().collect(Collectors.toMap(TagDescription::getKey, TagDescription::getValue));
    }
    
    
    /**
     * Gets a list of EC2 instance IDs for a given auto scaling group name
     * @param groupName auto scaling group name
     * @return List<String> list of instance ids
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
    
    
    private AutoScalingGroup getAutoScalingGroup(String groupName) {
        DescribeAutoScalingGroupsResult result = amazonAutoScalingClient.describeAutoScalingGroups(
            new DescribeAutoScalingGroupsRequest().withAutoScalingGroupNames(groupName));
        return result.getAutoScalingGroups().get(0);
    }

}
