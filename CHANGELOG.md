### 0.9.3
* By default, now logs to root application directory (orchestrator.log)

### 0.9.2
* Added configurable snapshot tags #2
* Added support for reverse replication queues
* Device name (used for snapshots) is now configurable #10
* Reading a test notification message off the SQS queue no longer causes the logs to show an error #7
* Fixed a bug where the scale down actions where not triggering correctly, causing replication agents to never be removed
* Fixed NoSuchElementException error when scaling publish up bug #4
* Fixed unable to delete flush and replication agents #11

### 0.9.1
* Initial version
