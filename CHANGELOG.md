### 0.9.2
* Added configurable snapshot tags (adds AWS tags to the active publish snapshot)
* Added support for reverse replication queues (see `aem.reverseReplication.*` in application.properties file)
* Device name (used for snapshots) is now configurable (see `aws.device.name` in application.properties file)
* Reading a test notification message off the SQS queue no longer causes the logs to show an error
* Fixed a bug where the scale down actions where not triggering correctly, causing replication agents to never be removed

### 0.9.1
* Initial version
