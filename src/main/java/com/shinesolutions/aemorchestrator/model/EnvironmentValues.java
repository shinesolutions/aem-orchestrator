package com.shinesolutions.aemorchestrator.model;

public class EnvironmentValues {
    private String autoScaleGroupNameForAuthorDispatcher;
    private String autoScaleGroupNameForPublish;
    private String autoScaleGroupNameForPublishDispatcher;
    private String elasticLoadBalancerNameForAuthor;

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
}
