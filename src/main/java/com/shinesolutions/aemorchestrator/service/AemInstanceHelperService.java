package com.shinesolutions.aemorchestrator.service;

import static com.shinesolutions.aemorchestrator.model.InstanceTags.AEM_AUTHOR_HOST;
import static com.shinesolutions.aemorchestrator.model.InstanceTags.AEM_PUBLISH_DISPATCHER_HOST;
import static com.shinesolutions.aemorchestrator.model.InstanceTags.AEM_PUBLISH_HOST;
import static com.shinesolutions.aemorchestrator.model.InstanceTags.PAIR_INSTANCE_ID;
import static com.shinesolutions.aemorchestrator.model.InstanceTags.SNAPSHOT_ID;
import static com.shinesolutions.aemorchestrator.model.InstanceTags.SNAPSHOT_TYPE;

import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.stream.Collectors;

import javax.annotation.Resource;

import org.apache.http.client.ClientProtocolException;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.retry.annotation.Backoff;
import org.springframework.retry.annotation.Retryable;
import org.springframework.stereotype.Component;

import com.shinesolutions.aemorchestrator.exception.InstanceNotInHealthyState;
import com.shinesolutions.aemorchestrator.model.EnvironmentValues;
import com.shinesolutions.aemorchestrator.model.InstanceTags;
import com.shinesolutions.aemorchestrator.util.HttpUtil;

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

    @Value("${aws.snapshot.tags}")
    private List<String> tagsToApplyToSnapshot;

    @Resource
    private EnvironmentValues envValues;

    @Resource
    private AwsHelperService awsHelperService;

    @Resource
    private HttpUtil httpUtil;

    private static final String URL_FORMAT = "%s://%s:%s";
    private static final String AEM_HEALTH_CHECK_URL_POSTFIX = "/system/health?tags=shallow";

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
        return String.format(URL_FORMAT, aemAuthorProtocol, envValues.getElasticLoadBalancerAuthorDns(), aemAuthorPort);
    }

    /**
     * Helper method for determining if the Author ELB is in a healthy state
     * @return true if the Author ELB is in a healthy state, false if not
     * @throws IOException (normally if can't connect)
     * @throws ClientProtocolException if there's an error in the HTTP protocol
     */
    public boolean isAuthorElbHealthy() throws ClientProtocolException, IOException {
        String url = getAemUrlForAuthorElb() + AEM_HEALTH_CHECK_URL_POSTFIX;

        return httpUtil.isHttpGetResponseOk(url);
    }
    
    /**
     * Helper method for determining if the given Publish instance is in a healthy state
     * @param instanceId the publish AWS instance id
     * @return true if the Publish instance is in a healthy state
     * @throws ClientProtocolException if there's an error in the HTTP protocol
     * @throws IOException (normally if can't connect)
     */
    public boolean isPubishHealthy(String instanceId) throws ClientProtocolException, IOException {
        String url = getAemUrlForPublish(instanceId) + AEM_HEALTH_CHECK_URL_POSTFIX;

        return httpUtil.isHttpGetResponseOk(url);
    }
    
    /**
     * Blocking method that continually checks to see if a given Publish instance is in a healthy state.
     * Will stop blocking once the Publish instance is deemed to be in a healthy state, or will
     * throw an exception if not
     * @param instanceId of the Publish instance
     * @throws InstanceNotInHealthyState thrown if reaches waiting period time out
     */
    @Retryable(maxAttempts=10, value=InstanceNotInHealthyState.class, backoff=@Backoff(delay=5000))
    public void waitForPublishToBeHealthy(String instanceId) throws InstanceNotInHealthyState {
        try {
            if(!isPubishHealthy(instanceId)) {
                throw new InstanceNotInHealthyState(instanceId);
            }
        } catch (IOException e) {
            throw new InstanceNotInHealthyState(instanceId, e);
        }
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
     * @return Active publish instance ID to get snapshot from, null if can't be found
     */
    public String getPublishIdToSnapshotFrom(String excludeInstanceId) {
        List<String> publishIds = awsHelperService.getInstanceIdsForAutoScalingGroup(
            envValues.getAutoScaleGroupNameForPublish());
        return publishIds.stream().filter(s -> !s.equals(excludeInstanceId)).findFirst().orElse(null);
    }
    
    /**
     * If no Publish instances have been set up via snapshot, then the first one will not require a snapshot 
     * (nothing to snapshot from). The method helps determine if it is the first publish instance to be set up 
     * after startup.
     * @return true if no instance son the Publish group have the SnapshotId tag, false otherwise
     */
    public boolean isFirstPublishInstance() {
        List<String> publishIds = awsHelperService.getInstanceIdsForAutoScalingGroup(
            envValues.getAutoScaleGroupNameForPublish());
        
        //Check if any of the instances on the group have the SnapshotId tag, if not then it's the first
        return publishIds.stream().filter(i -> awsHelperService.getTags(i).get(
            InstanceTags.SNAPSHOT_ID.getTagName()) != null).findFirst().orElse(null) == null;
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
        authorTags.put(AEM_AUTHOR_HOST.getTagName(), envValues.getElasticLoadBalancerAuthorDns());
        awsHelperService.addTags(authorDispatcherInstanceId, authorTags);
    }

    /**
     * Creates and tags a snapshot resource with select tags taken from the publish instance.
     * The tags to use are defined via properties (aws.snapshot.tags)
     * @param instanceId the publish EC2 instance ID from which the snapshot was taken
     * @param volumeId of where the snapshot will be stored
     * @return snapshot ID
     */
    public String createPublishSnapshot(String instanceId, String volumeId) {
        Map<String, String> activePublishTags = awsHelperService.getTags(instanceId);

        Map<String, String> tagsForSnapshot = activePublishTags.entrySet().stream()
            .filter(map -> tagsToApplyToSnapshot.contains(map.getKey()))
            .collect(Collectors.toMap(p -> p.getKey(), p -> p.getValue()));
        
        tagsForSnapshot.put(SNAPSHOT_TYPE.getTagName(), "orchestration");
        
        String snapshotId = awsHelperService.createSnapshot(volumeId,
            "Orchestration AEM snapshot of publish instance " + instanceId + " and volume " + volumeId);
        
        awsHelperService.addTags(snapshotId, tagsForSnapshot);

        return snapshotId;
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
