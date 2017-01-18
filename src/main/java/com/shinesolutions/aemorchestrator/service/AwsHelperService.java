package com.shinesolutions.aemorchestrator.service;

import java.util.concurrent.TimeUnit;

import javax.annotation.Resource;

import org.springframework.stereotype.Component;

import com.amazonaws.services.autoscaling.AmazonAutoScaling;
import com.amazonaws.services.cloudformation.AmazonCloudFormation;
import com.amazonaws.services.ec2.AmazonEC2;
import com.amazonaws.services.ec2.model.DescribeInstancesRequest;
import com.amazonaws.services.ec2.model.DescribeInstancesResult;
import com.amazonaws.services.elasticloadbalancing.AmazonElasticLoadBalancing;
import com.amazonaws.services.elasticloadbalancing.model.DescribeLoadBalancersRequest;
import com.amazonaws.services.elasticloadbalancing.model.DescribeLoadBalancersResult;

@Component
public class AwsHelperService {
    
    @Resource
    public AmazonEC2 amazonEC2Client;
    
    @Resource
    public AmazonElasticLoadBalancing amazonElbClient;
    
    @Resource 
    private AmazonAutoScaling amazonAutoScalingClient;
    
    @Resource 
    private AmazonCloudFormation amazonCloudFormationClient;
    
    private static final int NUM_RETRIES = 20;
    private static final int SECONDS_TO_WAIT_BETWEEN_RETRIES = 5;
    
    public String getElbDnsName(String elbName) {
        DescribeLoadBalancersResult result = amazonElbClient.describeLoadBalancers(new DescribeLoadBalancersRequest()
            .withLoadBalancerNames(elbName));
        return result.getLoadBalancerDescriptions().get(0).getDNSName();
    }
    
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

}
