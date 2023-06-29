package com.shinesolutions.aemorchestrator.service;

import com.shinesolutions.aemorchestrator.exception.InstanceNotInHealthyStateException;
import com.shinesolutions.aemorchestrator.exception.NoPairFoundException;
import com.shinesolutions.aemorchestrator.model.EC2Instance;
import com.shinesolutions.aemorchestrator.model.EnvironmentValues;
import com.shinesolutions.aemorchestrator.model.InstanceTags;
import com.shinesolutions.aemorchestrator.util.HttpUtil;
import org.apache.http.client.ClientProtocolException;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.retry.annotation.Backoff;
import org.springframework.retry.annotation.Retryable;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import java.io.IOException;
import java.security.GeneralSecurityException;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import static com.shinesolutions.aemorchestrator.model.InstanceTags.*;

/*
 * Service used for finding URLs, IDs etc of AEM/AWS instances
 */
@Component
public class AemInstanceHelperService {

    @Value("${aem.protocol.publishDispatcher}")
    private String aemPublishDispatcherProtocol;

    @Value("${aem.protocol.previewPublishDispatcher}")
    private String aemPreviewPublishDispatcherProtocol;

    @Value("${aem.protocol.publish}")
    private String aemPublishProtocol;

    @Value("${aem.protocol.previewPublish}")
    private String aemPreviewPublishProtocol;

    @Value("${aem.protocol.authorDispatcher}")
    private String aemAuthorDispatcherProtocol;

    @Value("${aem.protocol.author}")
    private String aemAuthorProtocol;

    @Value("${aem.port.publishDispatcher}")
    private Integer aemPublishDispatcherPort;

    @Value("${aem.port.publishDispatcher}")
    private Integer aemPreviewPublishDispatcherPort;

    @Value("${aem.port.publish}")
    private Integer aemPublishPort;

    @Value("${aem.port.previewPublish}")
    private Integer aemPreviewPublishPort;

    @Value("${aem.port.authorDispatcher}")
    private Integer aemAuthorDispatcherPort;

    @Value("${aem.port.author}")
    private Integer aemAuthorPort;

    @Value("${aws.snapshot.tags}")
    private List<String> tagsToApplyToSnapshot;

    @Value("${aws.cloudformation.stackName.publishDispatcher}")
    private String awsPublishDispatcherStackName;

    @Value("${aws.cloudformation.stackName.previewPublishDispatcher}")
    private String awsPreviewPublishDispatcherStackName;

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
     * Gets the PreviewPublish Dispatcher base AEM URL for a given EC2 instance ID
     * @param instanceId EC2 instance ID
     * @return Base AEM URL (includes protocol, IP and port). E.g. http://[ip]:[port]
     */
    public String getAemUrlForPreviewPublishDispatcher(String instanceId) {
        //PreviewPublish dispatcher must be accessed via private IP
        return String.format(URL_FORMAT, aemPreviewPublishDispatcherProtocol,
            awsHelperService.getPrivateIp(instanceId), aemPreviewPublishDispatcherPort);
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
     * Gets the PreviewPublish instance base AEM URL for a given EC2 instance ID
     * @param instanceId C2 instance ID
     * @return Base AEM URL (includes protocol, IP and port). E.g. http://[ip]:[port]
     */
    public String getAemUrlForPreviewPublish(String instanceId) {
        //PreviewPublish must be accessed via private IP
        return String.format(URL_FORMAT, aemPreviewPublishProtocol,
            awsHelperService.getPrivateIp(instanceId), aemPreviewPublishPort);
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
     * @throws GeneralSecurityException for any SSL related issue
     */
    public boolean isAuthorElbHealthy() throws ClientProtocolException, IOException, GeneralSecurityException {
        String url = getAemUrlForAuthorElb() + AEM_HEALTH_CHECK_URL_POSTFIX;

        return httpUtil.isHttpGetResponseOk(url);
    }

    /**
     * Gets the Publish instance base AEM URL for a given EC2 instance ID.
     * Component init status indicates its cloud init status.
     * @param instanceId C2 instance ID
     * @return true if provisioning was successful
     */
    public boolean getAemComponentInitState(String instanceId) {
      String componentInitStatus = awsHelperService.getTags(instanceId).get(
          InstanceTags.COMPONENT_INIT_STATUS.getTagName());

      boolean aemComponentInitState;
      switch(componentInitStatus) {
        case "Failed":
          aemComponentInitState = false;
          break;
        case "InProgress":
          aemComponentInitState = false;
          break;
        case "Success":
          aemComponentInitState = true;
          break;
        // anticipate the tag not existing or not having expected value as a false state
        default:
          aemComponentInitState = false;
          break;
      }
      return aemComponentInitState;
    }

    /**
     * Helper method for determining if the given Publish instance is in a healthy state
     * @param instanceId the publish AWS instance id
     * @return true if the Publish instance is in a healthy state
     */
    public boolean isPublishHealthy(String instanceId) {
        return getAemComponentInitState(instanceId);
    }

    /**
     * Helper method for determining if the given PreviewPublish instance is in a healthy state
     * @param instanceId the previewPublish AWS instance id
     * @return true if the PreviewPublish instance is in a healthy state
     */
    public boolean isPreviewPublishHealthy(String instanceId) {
        return getAemComponentInitState(instanceId);
    }

    /**
     * Blocking method that continually checks to see if a given Publish instance is in a healthy state.
     * Will stop blocking once the Publish instance is deemed to be in a healthy state, or will
     * throw an exception if not
     * @param instanceId of the Publish instance
     * @throws InstanceNotInHealthyStateException thrown if reaches waiting period time out
     */
    @Retryable(maxAttempts=10, value=InstanceNotInHealthyStateException.class, backoff=@Backoff(delay=5000))
    public void waitForPublishToBeHealthy(String instanceId) throws InstanceNotInHealthyStateException {
        try {
            if(!isPublishHealthy(instanceId)) {
                throw new InstanceNotInHealthyStateException(instanceId);
            }
        } catch (Exception e) {
            throw new InstanceNotInHealthyStateException(instanceId, e);
        }
    }

    /**
     * Blocking method that continually checks to see if a given PreviewPublish instance is in a healthy state.
     * Will stop blocking once the PreviewPublish instance is deemed to be in a healthy state, or will
     * throw an exception if not
     * @param instanceId of the PreviewPublish instance
     * @throws InstanceNotInHealthyStateException thrown if reaches waiting period time out
     */
    @Retryable(maxAttempts=10, value=InstanceNotInHealthyStateException.class, backoff=@Backoff(delay=5000))
    public void waitForPreviewPublishToBeHealthy(String instanceId) throws InstanceNotInHealthyStateException {
        try {
            if(!isPreviewPublishHealthy(instanceId)) {
                throw new InstanceNotInHealthyStateException(instanceId);
            }
        } catch (Exception e) {
            throw new InstanceNotInHealthyStateException(instanceId, e);
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
     * Gets the PreviewPublish EC2 instance ID that is paired to the given PreviewPublish Dispatcher.
     * Uses a tag on the instance to find the pair
     * @param dispatcherInstanceId EC2 instance ID of PreviewPublish Dispatcher
     * @return PreviewPublish EC2 instance ID. If no pair found, then returns null
     */
    public String getPreviewPublishIdForPairedDispatcher(String dispatcherInstanceId) {
        List<String> previewPublishIds = awsHelperService.getInstanceIdsForAutoScalingGroup(
            envValues.getAutoScaleGroupNameForPreviewPublish());

        return previewPublishIds.stream().filter(p -> dispatcherInstanceId.equals(
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
     * Gets the PreviewPublish Dispatcher EC2 instance ID that is paired to the given PreviewPublish instance.
     * Uses a tag on the instance to find the pair
     * @param previewPublishInstanceId the PreviewPublish EC2 instance ID
     * @return PreviewPublish Dispatcher EC2 instance ID. If no pair found, then returns null
     */
    public String getDispatcherIdForPairedPreviewPublish(String previewPublishInstanceId) {
        List<String> dispatcherIds = awsHelperService.getInstanceIdsForAutoScalingGroup(
            envValues.getAutoScaleGroupNameForPreviewPublishDispatcher());

        return dispatcherIds.stream().filter(d -> previewPublishInstanceId.equals(
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
     * Gets the desired capacity of the PreviewPublish auto scaling group
     * @return Desired capacity for PreviewPublish auto scaling group
     */
    public int getAutoScalingGroupDesiredCapacityForPreviewPublish() {
        return awsHelperService.getAutoScalingGroupDesiredCapacity(envValues.getAutoScaleGroupNameForPreviewPublish());
    }

    /**
     * Gets the desired capacity of the Publish Dispatcher auto scaling group
     * @return Desired capacity for Publish Dispatcher auto scaling group
     */
    public int getAutoScalingGroupDesiredCapacityForPublishDispatcher() {
        return awsHelperService.getAutoScalingGroupDesiredCapacity(envValues.getAutoScaleGroupNameForPublishDispatcher());
    }

    /**
     * Gets the desired capacity of the PreviewPublish Dispatcher auto scaling group
     * @return Desired capacity for PreviewPublish Dispatcher auto scaling group
     */
    public int getAutoScalingGroupDesiredCapacityForPreviewPublishDispatcher() {
        return awsHelperService.getAutoScalingGroupDesiredCapacity(envValues.getAutoScaleGroupNameForPreviewPublishDispatcher());
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
     * Sets the desired capacity of the PreviewPublish auto scaling group.
     * NOTE: Changing the desired capacity will cause the auto scaling group to either add or remove instances
     * @param desiredCapacity the new desired capacity
     */
    public void setAutoScalingGroupDesiredCapacityForPreviewPublish(int desiredCapacity) {
        awsHelperService.setAutoScalingGroupDesiredCapacity(envValues.getAutoScaleGroupNameForPreviewPublish(), desiredCapacity);
    }

    /**
     * Finds an active Publish instance (excluding the given instance ID) suitable for taking a snapshot from
     * @param excludeInstanceId the publish instance ID to exclude
     * from the search (generally the new Publish instance that needs the snapshot)
     * @return Active publish instance ID to get snapshot from, null if can't be found
     */
    public String getPublishIdToSnapshotFrom(String excludeInstanceId) {

        List<String> publishIds = awsHelperService.getInstanceIdsForAutoScalingGroup(envValues.getAutoScaleGroupNameForPublish());

        return publishIds.stream().filter(s -> !s.equals(excludeInstanceId))
                .filter(i -> awsHelperService.getTags(i).get(InstanceTags.SNAPSHOT_ID.getTagName()) != null)
                .sorted((o1, o2) -> {

                    Date launchTime1 = awsHelperService.getLaunchTime(o1);
                    Date launchTime2 = awsHelperService.getLaunchTime(o2);

                    final int launchTimeCompareTo = launchTime1.compareTo(launchTime2);

                    if (launchTimeCompareTo == 0) {
                        return o1.compareTo(o2);
                    }

                    return launchTimeCompareTo;
                })
                .findFirst().orElse(null);
    }

    /**
     * Finds an active PreviewPublish instance (excluding the given instance ID) suitable for taking a snapshot from
     * @param excludeInstanceId the previewPublish instance ID to exclude
     * from the search (generally the new PreviewPublish instance that needs the snapshot)
     * @return Active previewPublish instance ID to get snapshot from, null if can't be found
     */
    public String getPreviewPublishIdToSnapshotFrom(String excludeInstanceId) {

        List<String> previewPublishIds = awsHelperService.getInstanceIdsForAutoScalingGroup(envValues.getAutoScaleGroupNameForPreviewPublish());

        return previewPublishIds.stream().filter(s -> !s.equals(excludeInstanceId))
                .filter(i -> awsHelperService.getTags(i).get(InstanceTags.SNAPSHOT_ID.getTagName()) != null)
                .sorted((o1, o2) -> {

                    Date launchTime1 = awsHelperService.getLaunchTime(o1);
                    Date launchTime2 = awsHelperService.getLaunchTime(o2);

                    final int launchTimeCompareTo = launchTime1.compareTo(launchTime2);

                    if (launchTimeCompareTo == 0) {
                        return o1.compareTo(o2);
                    }

                    return launchTimeCompareTo;
                })
                .findFirst().orElse(null);
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
     * If no PreviewPublish instances have been set up via snapshot, then the first one will not require a snapshot
     * (nothing to snapshot from). The method helps determine if it is the first previewPublish instance to be set up
     * after startup.
     * @return true if no instance son the PreviewPublish group have the SnapshotId tag, false otherwise
     */
    public boolean isFirstPreviewPublishInstance() {
        List<String> previewPublishIds = awsHelperService.getInstanceIdsForAutoScalingGroup(
            envValues.getAutoScaleGroupNameForPreviewPublish());

        //Check if any of the instances on the group have the SnapshotId tag, if not then it's the first
        return previewPublishIds.stream().filter(i -> awsHelperService.getTags(i).get(
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
        tagsForSnapshot.put(NAME.getTagName(), "AEM publish Snapshot " + instanceId);

        String snapshotId = awsHelperService.createSnapshot(volumeId,
            "Orchestration AEM snapshot of publish instance " + instanceId + " and volume " + volumeId);

        awsHelperService.addTags(snapshotId, tagsForSnapshot);

        return snapshotId;
    }

    /**
     * Creates and tags a snapshot resource with select tags taken from the previewPublish instance.
     * The tags to use are defined via properties (aws.snapshot.tags)
     * @param instanceId the previewPublish EC2 instance ID from which the snapshot was taken
     * @param volumeId of where the snapshot will be stored
     * @return snapshot ID
     */
    public String createPreviewPublishSnapshot(String instanceId, String volumeId) {
        Map<String, String> activePreviewPublishTags = awsHelperService.getTags(instanceId);

        Map<String, String> tagsForSnapshot = activePreviewPublishTags.entrySet().stream()
            .filter(map -> tagsToApplyToSnapshot.contains(map.getKey()))
            .collect(Collectors.toMap(p -> p.getKey(), p -> p.getValue()));

        tagsForSnapshot.put(SNAPSHOT_TYPE.getTagName(), "orchestration");
        tagsForSnapshot.put(NAME.getTagName(), "AEM previewPublish Snapshot " + instanceId);

        String snapshotId = awsHelperService.createSnapshot(volumeId,
            "Orchestration AEM snapshot of previewPublish instance " + instanceId + " and volume " + volumeId);

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
     * Adds pair ID EC2 tags to both the PreviewPublish Dispatcher and PreviewPublish instance that point to each other.
     * @param previewPublishId the EC2 PreviewPublish instance ID
     * @param dispatcherId the PreviewPublish Dispatcher EC2 instance ID
     */
    public void pairPreviewPublishWithDispatcher(String previewPublishId, String dispatcherId) {
        Map<String, String> previewPublishTags = new HashMap<String, String>();
        previewPublishTags.put(AEM_PUBLISH_DISPATCHER_HOST.getTagName(), awsHelperService.getPrivateIp(dispatcherId));
        previewPublishTags.put(PAIR_INSTANCE_ID.getTagName(), dispatcherId);
        awsHelperService.addTags(previewPublishId, previewPublishTags);

        Map<String, String> dispatcherTags = new HashMap<String, String>();
        dispatcherTags.put(AEM_PUBLISH_HOST.getTagName(), awsHelperService.getPrivateIp(previewPublishId));
        dispatcherTags.put(PAIR_INSTANCE_ID.getTagName(), previewPublishId);
        awsHelperService.addTags(dispatcherId, dispatcherTags);
    }

    /**
     * Looks at all Publish Dispatcher instances on the auto scaling group and retrieves the
     * first one missing a pair ID tag (unpaired).
     * @param instanceId the Publish instance ID
     * @return Publish Dispatcher instance ID tag
     * @throws NoPairFoundException if can't find unpaired Publish Dispatcher
     */
    @Retryable(maxAttempts=10, value=NoPairFoundException.class, backoff=@Backoff(delay=5000))
    public String findUnpairedPublishDispatcher(String instanceId) throws NoPairFoundException {
        String unpairedDispatcher = null;

        List<EC2Instance> dispatcherIds = awsHelperService.getInstancesForAutoScalingGroup(
            envValues.getAutoScaleGroupNameForPublishDispatcher());
        //Filter the list to get all possible eligible candidates
        dispatcherIds = dispatcherIds.stream().filter(d ->
            isViablePair(instanceId, d.getInstanceId())).collect(Collectors.toList());

        if(dispatcherIds.size() > 1) {
            String publishAZ = awsHelperService.getAvailabilityZone(instanceId);

            //If there are many candidates, then pick the one with the same AZ or else use first
            unpairedDispatcher = (dispatcherIds.stream().filter(i -> i.getAvailabilityZone().equalsIgnoreCase(publishAZ))
                .findFirst().orElse(dispatcherIds.get(0))).getInstanceId();
        } else if (dispatcherIds.size() == 1) {
            unpairedDispatcher = dispatcherIds.get(0).getInstanceId();
        } else {
            throw new NoPairFoundException(instanceId);
        }

        return unpairedDispatcher;
    }

    /**
     * Looks at all PreviewPublish Dispatcher instances on the auto scaling group and retrieves the
     * first one missing a pair ID tag (unpaired).
     * @param instanceId the PreviewPublish instance ID
     * @return PreviewPublish Dispatcher instance ID tag
     * @throws NoPairFoundException if can't find unpaired PreviewPublish Dispatcher
     */
    @Retryable(maxAttempts=10, value=NoPairFoundException.class, backoff=@Backoff(delay=5000))
    public String findUnpairedPreviewPublishDispatcher(String instanceId) throws NoPairFoundException {
        String unpairedDispatcher = null;

        List<EC2Instance> dispatcherIds = awsHelperService.getInstancesForAutoScalingGroup(
            envValues.getAutoScaleGroupNameForPreviewPublishDispatcher());
        //Filter the list to get all possible eligible candidates
        dispatcherIds = dispatcherIds.stream().filter(d ->
            isViablePair(instanceId, d.getInstanceId())).collect(Collectors.toList());

        if(dispatcherIds.size() > 1) {
            String previewPublishAZ = awsHelperService.getAvailabilityZone(instanceId);

            //If there are many candidates, then pick the one with the same AZ or else use first
            unpairedDispatcher = (dispatcherIds.stream().filter(i -> i.getAvailabilityZone().equalsIgnoreCase(previewPublishAZ))
                .findFirst().orElse(dispatcherIds.get(0))).getInstanceId();
        } else if (dispatcherIds.size() == 1) {
            unpairedDispatcher = dispatcherIds.get(0).getInstanceId();
        } else {
            throw new NoPairFoundException(instanceId);
        }

        return unpairedDispatcher;
    }

    private boolean isViablePair(String instanceId, String dispatcherInstanceId) {
        Map<String, String> tags = awsHelperService.getTags(dispatcherInstanceId);
        return !tags.containsKey(PAIR_INSTANCE_ID.getTagName()) || //Either it's missing a pairing tag
            tags.get(PAIR_INSTANCE_ID.getTagName()).equals(instanceId); //Or it's already paired to the instance
    }

    /**
     * Creates a CloudWatch content health alarm for a given publish instance
     * @param instanceId of the publish instance
     */
    public void createContentHealthAlarmForPublisher(String instanceId) {
        awsHelperService.createContentHealthCheckAlarm(
            getContentHealthCheckAlarmName(instanceId),
            "Content Health Alarm for Publish Instance " + instanceId,
            instanceId,
            awsPublishDispatcherStackName,
            envValues.getTopicArn());
    }

    /**
     * Creates a CloudWatch content health alarm for a given previewPublish instance
     * @param instanceId of the previewPublish instance
     */
    public void createContentHealthAlarmForPreviewPublisher(String instanceId) {
        awsHelperService.createContentHealthCheckAlarm(
            getContentHealthCheckAlarmName(instanceId),
            "Content Health Alarm for PreviewPublish Instance " + instanceId,
            instanceId,
            awsPreviewPublishDispatcherStackName,
            envValues.getTopicArn());
    }

    /**
     * Deletes a CloudWatch content health alarm for a given publish instance ID
     * @param instanceId of the publish instance
     */
    public void deleteContentHealthAlarmForPublisher(String instanceId) {
        awsHelperService.deleteAlarm(getContentHealthCheckAlarmName(instanceId));
    }

    /**
     * Deletes a CloudWatch content health alarm for a given previewPublish instance ID
     * @param instanceId of the previewPublish instance
     */
    public void deleteContentHealthAlarmForPreviewPublisher(String instanceId) {
        awsHelperService.deleteAlarm(getContentHealthCheckAlarmName(instanceId));
    }


    private String getContentHealthCheckAlarmName(String instanceId) {
        return "contentHealthCheck-" + instanceId;
    }

}
