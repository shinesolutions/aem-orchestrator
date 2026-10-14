package com.shinesolutions.aemorchestrator.model;

/**
 * EC2 instance tags the orchestrator uses to describe and pair AEM instances, each holding the tag
 * key.
 */
public enum InstanceTags {
  PAIR_INSTANCE_ID("PairInstanceId"),
  PREVIEW_PAIR_INSTANCE_ID("PreviewPairInstanceId"),
  INSTANCE_ID("InstanceId"),
  AEM_PUBLISH_HOST("PublishHost"),
  AEM_PUBLISH_DISPATCHER_HOST("PublishDispatcherHost"),
  AEM_PREVIEW_PUBLISH_HOST("PreviewPublishHost"),
  AEM_PREVIEW_PUBLISH_DISPATCHER_HOST("PreviewPublishDispatcherHost"),
  AEM_AUTHOR_HOST("AuthorHost"),
  COMPONENT_INIT_STATUS("ComponentInitStatus"),
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
