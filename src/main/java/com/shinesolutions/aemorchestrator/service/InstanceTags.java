package com.shinesolutions.aemorchestrator.service;

public enum InstanceTags {

    PAIR_INSTANCE_ID("pair_instance_id"), 
    AEM_PUBLISHER_HOST("aem_publisher_host"), 
    AEM_DISPATCHER_HOST("aem_dispatcher_host"), 
    SNAPSHOT_ID("snapshot_id");

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
