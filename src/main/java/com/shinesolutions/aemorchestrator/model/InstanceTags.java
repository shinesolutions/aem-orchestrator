package com.shinesolutions.aemorchestrator.model;

public enum InstanceTags {

    PAIR_INSTANCE_ID("PairInstanceId"),
    INSTANCE_ID("InstanceId"),
    AEM_PUBLISH_HOST("PublishHost"), 
    AEM_PUBLISH_DISPATCHER_HOST("PublishDispatcherHost"), 
    AEM_AUTHOR_HOST("AuthorHost"),
    SNAPSHOT_ID("SnapshotId"),
    SNAPSHOT_TYPE("SnapshotType"),
    NAME("Name");

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
