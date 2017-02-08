package com.shinesolutions.aemorchestrator.service;

import static com.shinesolutions.aemorchestrator.service.InstanceTags.AEM_AUTHOR_HOST;
import static com.shinesolutions.aemorchestrator.service.InstanceTags.AEM_PUBLISH_DISPATCHER_HOST;
import static com.shinesolutions.aemorchestrator.service.InstanceTags.AEM_PUBLISH_HOST;
import static com.shinesolutions.aemorchestrator.service.InstanceTags.PAIR_INSTANCE_ID;
import static com.shinesolutions.aemorchestrator.service.InstanceTags.SNAPSHOT_ID;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.NoSuchElementException;

import javax.annotation.Resource;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.retry.annotation.Backoff;
import org.springframework.retry.annotation.Retryable;
import org.springframework.stereotype.Component;

import com.shinesolutions.aemorchestrator.model.EnvironmentValues;

/*
 * Service used for finding URLs, IDs etc of AEM/AWS instances
 */
@Component
public class AemInstanceHelperService {
    
    @Value("${aem.protocol.publishDispatcher}")
    private String aemPublishDispatcherProtocol;

    @Value("${aem.protocol.publish}")
    private String aemPublishProtocol;

    @Value("${aem.protocol.authorDispatcher}")
    private String aemAuthorDispatcherProtocol;
    
    @Value("${aem.protocol.author}")
    private String aemAuthorProtocol;
    
    @Value("${aem.port.publishDispatcher}")
    private Integer aemPublishDispatcherPort;

    @Value("${aem.port.publish}")
    private Integer aemPublishPort;

    @Value("${aem.port.authorDispatcher}")
    private Integer aemAuthorDispatcherPort;
    
    @Value("${aem.port.author}")
    private Integer aemAuthorPort;
    
    @Resource
    private EnvironmentValues envValues;
    
    @Resource
    private AwsHelperService awsHelperService;
    
    private static final String URL_FORMAT = "%s://%s:%s";
    
    /**
     * Gets the Publish Dispatcher base AEM URL for a given EC2 instance ID 
     * @param instanceId EC2 instance ID
     * @return Base AEM URL (includes protocol, IP and port). E.g. http://[ip]:[port]
     */
    public String getAemUrlForPublishDispatcher(String instanceId) {
        //Publish dispatcher must be accessed via private IP
        return String.format(URL_FORMAT, aemPublishDispatcherProtocol, 
            awsHelperService.getPrivateIp(instanceId), aemPublishDispatcherPort);
    }
    
    /**
     * Gets the Publish instance base AEM URL for a given EC2 instance ID 
     * @param instanceId C2 instance ID
     * @return Base AEM URL (includes protocol, IP and port). E.g. http://[ip]:[port]
     */
    public String getAemUrlForPublish(String instanceId) {
        //Publish must be accessed via private IP
        return String.format(URL_FORMAT, aemPublishProtocol, 
            awsHelperService.getPrivateIp(instanceId), aemPublishPort);
    }
    
    /**
     * Gets the Author base AEM URL for a given EC2 instance ID 
     * @param instanceId C2 instance ID
     * @return Base AEM URL (includes protocol, IP and port). E.g. http://[ip]:[port]
     */
    public String getAemUrlForAuthorDispatcher(String instanceId) {
        //Author dispatcher can be accessed via private IP
        return String.format(URL_FORMAT, aemAuthorDispatcherProtocol, 
            awsHelperService.getPrivateIp(instanceId), aemAuthorDispatcherPort);
    }
    
    /**
     * Gets the Author base AEM URL for the Author AWS Elastic Load Balancer
     * @return Base AEM URL (includes protocol, DNS name and port). E.g. http://[dns-name]:[port]
     */
    public String getAemUrlForAuthorElb() {
        //Author can be accessed from the load balancer
        return String.format(URL_FORMAT, aemAuthorProtocol, awsHelperService.getElbDnsName(
            envValues.getElasticLoadBalancerNameForAuthor()), aemAuthorPort);
    }
    
    /**
     * Gets the Publish EC2 instance ID that is paired to the given Publish Dispatcher. 
     * Uses a tag on the instance to find the pair
     * @param dispatcherInstanceId EC2 instance ID of Publish Dispatcher
     * @return Publish EC2 instance ID. If no pair found, then returns null
     */
    public String getPublishIdForPairedDispatcher(String dispatcherInstanceId) {
        List<String> publishIds = awsHelperService.getInstanceIdsForAutoScalingGroup(
            envValues.getAutoScaleGroupNameForPublish());
        
        return publishIds.stream().filter(p -> dispatcherInstanceId.equals(
            awsHelperService.getTags(p).get(PAIR_INSTANCE_ID.getTagName()))).findFirst().orElse(null);
    }
    
    /**
     * Gets the Publish Dispatcher EC2 instance ID that is paired to the given Publish instance. 
     * Uses a tag on the instance to find the pair
     * @param publishInstanceId the Publish EC2 instance ID
     * @return Publish Dispatcher EC2 instance ID. If no pair found, then returns null
     */
    public String getDispatcherIdForPairedPublish(String publishInstanceId) {
        List<String> dispatcherIds = awsHelperService.getInstanceIdsForAutoScalingGroup(
            envValues.getAutoScaleGroupNameForPublishDispatcher());
        
        return dispatcherIds.stream().filter(d -> publishInstanceId.equals(
            awsHelperService.getTags(d).get(PAIR_INSTANCE_ID.getTagName()))).findFirst().orElse(null);
    }
    
    /**
     * Gets the desired capacity of the Publish auto scaling group
     * @return Desired capacity for Publish auto scaling group
     */
    public int getAutoScalingGroupDesiredCapacityForPublish() {
        return awsHelperService.getAutoScalingGroupDesiredCapacity(envValues.getAutoScaleGroupNameForPublish());
    }
    
    /**
     * Gets the desired capacity of the Publish Dispatcher auto scaling group
     * @return Desired capacity for Publish Dispatcher auto scaling group
     */
    public int getAutoScalingGroupDesiredCapacityForPublishDispatcher() {
        return awsHelperService.getAutoScalingGroupDesiredCapacity(envValues.getAutoScaleGroupNameForPublishDispatcher());
    }
    
    /**
     * Sets the desired capacity of the Publish auto scaling group.
     * NOTE: Changing the desired capacity will cause the auto scaling group to either add or remove instances
     * @param desiredCapacity the new desired capacity
     */
    public void setAutoScalingGroupDesiredCapacityForPublish(int desiredCapacity) {
        awsHelperService.setAutoScalingGroupDesiredCapacity(envValues.getAutoScaleGroupNameForPublish(), desiredCapacity);
    }
    
    /**
     * Finds an active Publish instance (excluding the given instance ID) suitable for taking a snapshot from
     * @param excludeInstanceId the publish instance ID to exclude 
     * from the search (generally the new Publish instance that needs the snapshot)
     * @return Active publish instance ID to get snapshot from
     */
    public String getPublishIdToSnapshotFrom(String excludeInstanceId) {
        List<String> publishIds = awsHelperService.getInstanceIdsForAutoScalingGroup(
            envValues.getAutoScaleGroupNameForPublish());
        return publishIds.stream().filter(s -> !s.equals(excludeInstanceId)).findFirst().get();
    }
    
    /**
     * Tags the given instance with a given snapshot ID 
     * @param instanceId the EC2 instance to tag
     * @param snapshotId the snapshot ID to place in the tag
     */
    public void tagInstanceWithSnapshotId(String instanceId, String snapshotId) {
        Map<String, String> tags = new HashMap<String, String>();
        tags.put(SNAPSHOT_ID.getTagName(), snapshotId);
        awsHelperService.addTags(instanceId, tags);
    }
    
    /**
     * Tags the given Author Dispatcher instance with Author DNS host name
     * @param authorDispatcherInstanceId the EC2 Author Dispatcher instance ID
     */
    public void tagAuthorDispatcherWithAuthorHost(String authorDispatcherInstanceId) {
        Map<String, String> authorTags = new HashMap<String, String>();
        authorTags.put(AEM_AUTHOR_HOST.getTagName(), awsHelperService.getElbDnsName(
            envValues.getElasticLoadBalancerNameForAuthor()));
        awsHelperService.addTags(authorDispatcherInstanceId, authorTags);
    }
    
    /**
     * Adds pair ID EC2 tags to both the Publish Dispatcher and Publish instance that point to each other.
     * @param publishId the EC2 Publish instance ID
     * @param dispatcherId the Publish Dispatcher EC2 instance ID
     */
    public void pairPublishWithDispatcher(String publishId, String dispatcherId) {
        Map<String, String> publishTags = new HashMap<String, String>();
        publishTags.put(AEM_PUBLISH_DISPATCHER_HOST.getTagName(), awsHelperService.getPrivateIp(dispatcherId));
        publishTags.put(PAIR_INSTANCE_ID.getTagName(), dispatcherId);
        awsHelperService.addTags(publishId, publishTags);
        
        Map<String, String> dispatcherTags = new HashMap<String, String>();
        dispatcherTags.put(AEM_PUBLISH_HOST.getTagName(), awsHelperService.getPrivateIp(publishId));
        dispatcherTags.put(PAIR_INSTANCE_ID.getTagName(), publishId);
        awsHelperService.addTags(dispatcherId, dispatcherTags);
    }
    
    /**
     * Looks at all Publish Dispatcher instances on the auto scaling group and retrieves the
     * first one missing a pair ID tag (unpaired).
     * @return Publish Dispatcher instance ID tag
     * @throws NoSuchElementException if can't find unpaired Publish Dispatcher
     */
    @Retryable(maxAttempts=6, value=NoSuchElementException.class, backoff=@Backoff(delay=10000))
    public String findUnpairedPublishDispatcher() throws NoSuchElementException {
        List<String> dispatcherIds = awsHelperService.getInstanceIdsForAutoScalingGroup(
            envValues.getAutoScaleGroupNameForPublishDispatcher());
        return dispatcherIds.stream().filter(d -> !awsHelperService.getTags(d).containsKey(
            PAIR_INSTANCE_ID.getTagName())).findFirst().get();
    }
    
}
