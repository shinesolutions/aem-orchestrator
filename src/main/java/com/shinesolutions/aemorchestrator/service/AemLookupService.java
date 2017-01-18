package com.shinesolutions.aemorchestrator.service;

import javax.annotation.Resource;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

/*
 * Service used for finding URLs, IDs etc of AEM/AWS instances
 */
@Component
public class AemLookupService {

    @Value("${aws.autoscale.group.name.authorDispatcher}")
    private String awsAuthorDispatcherGroupName;
    
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
        //Author dispatcher can be accessed from the load balancer
        return String.format(URL_FORMAT, aemAuthorDispatcherProtocol, 
            awsHelperService.getElbDnsName(awsAuthorDispatcherGroupName), aemAuthorDispatcherPort);
    }
    
    public String getAemUrlForAuthorDispatcher(String instanceId) {
        //Author dispatcher can be accessed via private IP
        return String.format(URL_FORMAT, aemAuthorDispatcherProtocol, 
            awsHelperService.getPrivateIp(instanceId), aemAuthorDispatcherPort);
    }

    
}
