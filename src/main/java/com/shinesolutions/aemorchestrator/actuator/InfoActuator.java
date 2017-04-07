package com.shinesolutions.aemorchestrator.actuator;

import java.util.HashMap;
import java.util.Map;

import javax.annotation.Resource;

import org.springframework.boot.actuate.info.Info;
import org.springframework.boot.actuate.info.InfoContributor;
import org.springframework.stereotype.Component;

import com.shinesolutions.aemorchestrator.model.EnvironmentValues;

@Component
public class InfoActuator implements InfoContributor {
    
    @Resource
    private EnvironmentValues envValues;

    @Override
    public void contribute(Info.Builder builder) {
        Map<String, String> asgNames = new HashMap<String, String>();
        asgNames.put("author-dispatcher", envValues.getAutoScaleGroupNameForAuthorDispatcher());
        asgNames.put("publish", envValues.getAutoScaleGroupNameForPublish());
        asgNames.put("publish-dispatcher", envValues.getAutoScaleGroupNameForPublishDispatcher());
        
        builder.withDetail("auto-scaling-group-names", asgNames);
        
        Map<String, String> authorInfo = new HashMap<String, String>();
        authorInfo.put("elastic-load-balancer-name", envValues.getElasticLoadBalancerNameForAuthor());
        authorInfo.put("elastic-load-balancer-dns", envValues.getElasticLoadBalancerAuthorDns());
        
        builder.withDetail("author", authorInfo);
        
        builder.withDetail("alarm-notification-topic-arn", envValues.getTopicArn());
    }

}
