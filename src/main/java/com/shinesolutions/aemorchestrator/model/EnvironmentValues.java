package com.shinesolutions.aemorchestrator.model;

public class EnvironmentValues {
    private String autoScaleGroupNameForAuthorDispatcher;
    private String autoScaleGroupNameForPublish;
    private String autoScaleGroupNameForPublishDispatcher;
    private String elasticLoadBalancerNameForAuthor;
    private String elasticLoadBalancerAuthorDns;
    private String topicArn;

    public String getAutoScaleGroupNameForAuthorDispatcher() {
        return autoScaleGroupNameForAuthorDispatcher;
    }

    public void setAutoScaleGroupNameForAuthorDispatcher(String autoScaleGroupNameForAuthorDispatcher) {
        this.autoScaleGroupNameForAuthorDispatcher = autoScaleGroupNameForAuthorDispatcher;
    }

    public String getAutoScaleGroupNameForPublish() {
        return autoScaleGroupNameForPublish;
    }

    public void setAutoScaleGroupNameForPublish(String autoScaleGroupNameForPublish) {
        this.autoScaleGroupNameForPublish = autoScaleGroupNameForPublish;
    }

    public String getAutoScaleGroupNameForPublishDispatcher() {
        return autoScaleGroupNameForPublishDispatcher;
    }

    public void setAutoScaleGroupNameForPublishDispatcher(String autoScaleGroupNameForPublishDispatcher) {
        this.autoScaleGroupNameForPublishDispatcher = autoScaleGroupNameForPublishDispatcher;
    }

    public String getElasticLoadBalancerNameForAuthor() {
        return elasticLoadBalancerNameForAuthor;
    }

    public void setElasticLoadBalancerNameForAuthor(String elasticLoadBalancerNameForAuthor) {
        this.elasticLoadBalancerNameForAuthor = elasticLoadBalancerNameForAuthor;
    }

    public String getElasticLoadBalancerAuthorDns() {
        return elasticLoadBalancerAuthorDns;
    }

    public void setElasticLoadBalancerAuthorDns(String elasticLoadBalancerAuthorDns) {
        this.elasticLoadBalancerAuthorDns = elasticLoadBalancerAuthorDns;
    }

    public String getTopicArn() {
        return topicArn;
    }

    public void setTopicArn(String topicArn) {
        this.topicArn = topicArn;
    }
}
