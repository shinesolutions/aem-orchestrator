package com.shinesolutions.aemorchestrator.service;

import static com.shinesolutions.aemorchestrator.service.InstanceTags.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.annotation.Resource;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import com.shinesolutions.aemorchestrator.model.AutoScaleGroupNames;

/*
 * Service used for finding URLs, IDs etc of AEM/AWS instances
 */
@Component
public class AemHelperService {
    
    @Value("${aem.protocol.publisherDispatcher}")
    private String aemPublisherDispatcherProtocol;

    @Value("${aem.protocol.publisher}")
    private String aemPublisherProtocol;

    @Value("${aem.protocol.authorDispatcher}")
    private String aemAuthorDispatcherProtocol;
    
    @Value("${aem.port.publisherDispatcher}")
    private Integer aemPublisherDispatcherPort;

    @Value("${aem.port.publisher}")
    private Integer aemPublisherPort;

    @Value("${aem.port.authorDispatcher}")
    private Integer aemAuthorDispatcherPort;
    
    @Resource
    private AutoScaleGroupNames asgNames;
    
    @Resource
    private AwsHelperService awsHelperService;
    
    private static final String URL_FORMAT = "%s://%s:%s";
    
    public String getAemUrlForPublisherDispatcher(String instanceId) {
        //Publisher dispatcher must be accessed via private IP
        return String.format(URL_FORMAT, aemPublisherDispatcherProtocol, 
            awsHelperService.getPrivateIp(instanceId), aemPublisherDispatcherPort);
    }
    
    public String getAemUrlForPublisher(String instanceId) {
        //Publisher must be accessed via private IP
        return String.format(URL_FORMAT, aemPublisherProtocol, 
            awsHelperService.getPrivateIp(instanceId), aemPublisherPort);
    }
    
    public String getAemUrlForAuthorElb() {
        //Author can be accessed from the load balancer
        return String.format(URL_FORMAT, aemAuthorDispatcherProtocol, 
            awsHelperService.getElbDnsName(asgNames.getAuthorDispatcher()), aemAuthorDispatcherPort);
    }
    
    public String getAemUrlForAuthorDispatcher(String instanceId) {
        //Author dispatcher can be accessed via private IP
        return String.format(URL_FORMAT, aemAuthorDispatcherProtocol, 
            awsHelperService.getPrivateIp(instanceId), aemAuthorDispatcherPort);
    }

    public String getPublisherIdForPairedDispatcher(String dispatcherInstanceId) {
        List<String> publisherIds = awsHelperService.getInstanceIdsForAutoScalingGroup(asgNames.getPublisher());
        
        return publisherIds.stream().filter(p -> dispatcherInstanceId.equals(
            awsHelperService.getTags(p).get(PAIR_INSTANCE_ID.getTagName()))).findFirst().get();

    }
    
    public String getDispatcherIdForPairedPublisher(String publisherInstanceId) {
        List<String> dispatcherIds = awsHelperService.getInstanceIdsForAutoScalingGroup(asgNames.getPublisherDispatcher());
        
        return dispatcherIds.stream().filter(d -> publisherInstanceId.equals(
            awsHelperService.getTags(d).get(PAIR_INSTANCE_ID.getTagName()))).findFirst().get();

    }
    
    public int getAutoScalingGroupDesiredCapacityForPublisher() {
        return awsHelperService.getAutoScalingGroupDesiredCapacity(asgNames.getPublisher());
    }
    
    public int getAutoScalingGroupDesiredCapacityForPublisherDispatcher() {
        return awsHelperService.getAutoScalingGroupDesiredCapacity(asgNames.getPublisherDispatcher());
    }
    
    public void setAutoScalingGroupDesiredCapacityForPublisher(int desiredCapacity) {
        awsHelperService.setAutoScalingGroupDesiredCapacity(asgNames.getPublisher(), desiredCapacity);
    }
    
    public String getPublisherIdToSnapshotFrom(String excludeInstanceId) {
        List<String> publisherIds = awsHelperService.getInstanceIdsForAutoScalingGroup(asgNames.getPublisher());
        return publisherIds.stream().filter(s -> !s.equals(excludeInstanceId)).findFirst().get();
    }
    
    public void tagInstanceWithSnapshotId(String instanceId, String snapshotId) {
        Map<String, String> tags = new HashMap<String, String>();
        tags.put(SNAPSHOT_ID.getTagName(), snapshotId);
        awsHelperService.addTags(instanceId, tags);
    }
    
    public void pairPublisherWithDispatcher(String publisherId, String dispatcherId) {
        Map<String, String> publisherTags = new HashMap<String, String>();
        publisherTags.put(AEM_DISPATCHER_HOST.getTagName(), awsHelperService.getPrivateIp(dispatcherId));
        publisherTags.put(PAIR_INSTANCE_ID.getTagName(), dispatcherId);
        awsHelperService.addTags(publisherId, publisherTags);
        
        Map<String, String> dispatcherTags = new HashMap<String, String>();
        dispatcherTags.put(AEM_PUBLISHER_HOST.getTagName(), awsHelperService.getPrivateIp(publisherId));
        dispatcherTags.put(PAIR_INSTANCE_ID.getTagName(), publisherId);
        awsHelperService.addTags(dispatcherId, dispatcherTags);
    }
    
    public String findUnpairedPublisherDispatcher() {
        List<String> dispatcherIds = awsHelperService.getInstanceIdsForAutoScalingGroup(asgNames.getPublisherDispatcher());
        return dispatcherIds.stream().filter(d -> !awsHelperService.getTags(d).containsKey(PAIR_INSTANCE_ID.getTagName()))
            .findFirst().get();
    }
    
}
