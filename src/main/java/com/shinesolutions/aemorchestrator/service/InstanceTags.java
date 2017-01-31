package com.shinesolutions.aemorchestrator.service;

public enum InstanceTags {

    PAIR_INSTANCE_ID("PairInstanceId"), 
    AEM_PUBLISHER_HOST("PublishHost"), 
    AEM_DISPATCHER_HOST("PublishDispatcherHost"), 
    SNAPSHOT_ID("SnapshotId");

    private final String tagName;

    private InstanceTags(String s) {
        tagName = s;
    }

    @Override
    public String toString() {
        return this.tagName;
    }
    
    public String getTagName() {
        return this.tagName;
    }
}
