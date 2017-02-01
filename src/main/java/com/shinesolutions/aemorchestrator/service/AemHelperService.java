package com.shinesolutions.aemorchestrator.service;

import static com.shinesolutions.aemorchestrator.service.InstanceTags.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.annotation.Resource;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import com.shinesolutions.aemorchestrator.model.EnvironmentValues;

/*
 * Service used for finding URLs, IDs etc of AEM/AWS instances
 */
@Component
public class AemHelperService {
    
    @Value("${aem.protocol.publishDispatcher}")
    private String aemPublishDispatcherProtocol;

    @Value("${aem.protocol.publish}")
    private String aemPublishProtocol;

    @Value("${aem.protocol.authorDispatcher}")
    private String aemAuthorDispatcherProtocol;
    
    @Value("${aem.port.publishDispatcher}")
    private Integer aemPublishDispatcherPort;

    @Value("${aem.port.publish}")
    private Integer aemPublishPort;

    @Value("${aem.port.authorDispatcher}")
    private Integer aemAuthorDispatcherPort;
    
    @Resource
    private EnvironmentValues envValues;
    
    @Resource
    private AwsHelperService awsHelperService;
    
    private static final String URL_FORMAT = "%s://%s:%s";
    
    public String getAemUrlForPublishDispatcher(String instanceId) {
        //Publish dispatcher must be accessed via private IP
        return String.format(URL_FORMAT, aemPublishDispatcherProtocol, 
            awsHelperService.getPrivateIp(instanceId), aemPublishDispatcherPort);
    }
    
    public String getAemUrlForPublish(String instanceId) {
        //Publish must be accessed via private IP
        return String.format(URL_FORMAT, aemPublishProtocol, 
            awsHelperService.getPrivateIp(instanceId), aemPublishPort);
    }
    
    public String getAemUrlForAuthorElb() {
        //Author can be accessed from the load balancer
        return String.format(URL_FORMAT, aemAuthorDispatcherProtocol, awsHelperService.getElbDnsName(
            envValues.getElasticLoadBalancerNameForAuthor()), aemAuthorDispatcherPort);
    }
    
    public String getAemUrlForAuthorDispatcher(String instanceId) {
        //Author dispatcher can be accessed via private IP
        return String.format(URL_FORMAT, aemAuthorDispatcherProtocol, 
            awsHelperService.getPrivateIp(instanceId), aemAuthorDispatcherPort);
    }

    public String getPublishIdForPairedDispatcher(String dispatcherInstanceId) {
        List<String> publishIds = awsHelperService.getInstanceIdsForAutoScalingGroup(
            envValues.getAutoScaleGroupNameForPublish());
        
        return publishIds.stream().filter(p -> dispatcherInstanceId.equals(
            awsHelperService.getTags(p).get(PAIR_INSTANCE_ID.getTagName()))).findFirst().get();
    }
    
    public String getDispatcherIdForPairedPublish(String publishInstanceId) {
        List<String> dispatcherIds = awsHelperService.getInstanceIdsForAutoScalingGroup(
            envValues.getAutoScaleGroupNameForPublishDispatcher());
        
        return dispatcherIds.stream().filter(d -> publishInstanceId.equals(
            awsHelperService.getTags(d).get(PAIR_INSTANCE_ID.getTagName()))).findFirst().get();
    }
    
    public int getAutoScalingGroupDesiredCapacityForPublish() {
        return awsHelperService.getAutoScalingGroupDesiredCapacity(envValues.getAutoScaleGroupNameForPublish());
    }
    
    public int getAutoScalingGroupDesiredCapacityForPublishDispatcher() {
        return awsHelperService.getAutoScalingGroupDesiredCapacity(envValues.getAutoScaleGroupNameForPublishDispatcher());
    }
    
    public void setAutoScalingGroupDesiredCapacityForPublish(int desiredCapacity) {
        awsHelperService.setAutoScalingGroupDesiredCapacity(envValues.getAutoScaleGroupNameForPublish(), desiredCapacity);
    }
    
    public String getPublishIdToSnapshotFrom(String excludeInstanceId) {
        List<String> publishIds = awsHelperService.getInstanceIdsForAutoScalingGroup(
            envValues.getAutoScaleGroupNameForPublish());
        return publishIds.stream().filter(s -> !s.equals(excludeInstanceId)).findFirst().get();
    }
    
    public void tagAuthorDispatcherWithAuthorELB(String authorDispatcherId) {
        Map<String, String> authorTags = new HashMap<String, String>();
        authorTags.put(AEM_AUTHOR_HOST.getTagName(), envValues.getElasticLoadBalancerNameForAuthor());
        awsHelperService.addTags(authorDispatcherId, authorTags);
    }
    
    public void tagInstanceWithSnapshotId(String instanceId, String snapshotId) {
        Map<String, String> tags = new HashMap<String, String>();
        tags.put(SNAPSHOT_ID.getTagName(), snapshotId);
        awsHelperService.addTags(instanceId, tags);
    }
    
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
    
    public String findUnpairedPublishDispatcher() {
        List<String> dispatcherIds = awsHelperService.getInstanceIdsForAutoScalingGroup(
            envValues.getAutoScaleGroupNameForPublishDispatcher());
        return dispatcherIds.stream().filter(d -> !awsHelperService.getTags(d).containsKey(
            PAIR_INSTANCE_ID.getTagName())).findFirst().get();
    }
    
}
