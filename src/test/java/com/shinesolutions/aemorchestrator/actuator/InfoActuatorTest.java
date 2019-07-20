package com.shinesolutions.aemorchestrator.actuator;

import com.shinesolutions.aemorchestrator.model.EnvironmentValues;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import org.springframework.boot.actuate.info.Info;

import java.util.Map;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class InfoActuatorTest {

    @Mock
    EnvironmentValues envValues;

    @InjectMocks
    private InfoActuator infoActuator;

    @Test
    public void testContribute() {
        // Setup for "auto-scaling-group-names".
        String autoScaleGroupNameForAuthorDispatcher = "autoScaleGroupNameForAuthorDispatcher";
        String autoScaleGroupNameForPublish = "autoScaleGroupNameForPublish";
        String autoScaleGroupNameForPublishDispatcher = "autoScaleGroupNameForPublishDispatcher";
        when(envValues.getAutoScaleGroupNameForAuthorDispatcher()).thenReturn(autoScaleGroupNameForAuthorDispatcher);
        when(envValues.getAutoScaleGroupNameForPublish()).thenReturn(autoScaleGroupNameForPublish);
        when(envValues.getAutoScaleGroupNameForPublishDispatcher()).thenReturn(autoScaleGroupNameForPublishDispatcher);

        // Setup for "author".
        String elasticLoadBalancerNameForAuthor = "elasticLoadBalancerNameForAuthor";
        String elasticLoadBalancerAuthorDns = "elasticLoadBalancerAuthorDns";
        when(envValues.getElasticLoadBalancerNameForAuthor()).thenReturn(elasticLoadBalancerNameForAuthor);
        when(envValues.getElasticLoadBalancerAuthorDns()).thenReturn(elasticLoadBalancerAuthorDns);

        // Setup for "alarm-notification-topic-arn"
        String topicArn = "topicArn";
        when(envValues.getTopicArn()).thenReturn(topicArn);

        Info.Builder builder = new Info.Builder();
        infoActuator.contribute(builder);

        // Validate "auto-scaling-group-names".
        Map<String, Object> info = builder.build().getDetails();
        Map<String, String> asgNames = (Map<String, String>) info.get("auto-scaling-group-names");
        assertThat(asgNames.get("author-dispatcher"), equalTo(autoScaleGroupNameForAuthorDispatcher));
        assertThat(asgNames.get("publish"), equalTo(autoScaleGroupNameForPublish));
        assertThat(asgNames.get("publish-dispatcher"), equalTo(autoScaleGroupNameForPublishDispatcher));

        // Validate "author"
        Map<String, String> authorInfo = (Map<String, String>) info.get("author");
        assertThat(authorInfo.get("elastic-load-balancer-name"), equalTo(elasticLoadBalancerNameForAuthor));
        assertThat(authorInfo.get("elastic-load-balancer-dns"), equalTo(elasticLoadBalancerAuthorDns));

        // Validate "alarm-notification-topic-arn"
        assertThat(info.get("alarm-notification-topic-arn"), equalTo(topicArn));
    }
}
