### 0.9.3
* By default, now logs to root application directory (orchestrator.log)
* Performs a health check on the Publish instance before doing a Snapshot #15
* Fixed bug causing continual snapshotting when unable to find a publish pair candidate #12
* Creates a content health check alarm on the publish instance #19
* Fix cross-snapshotting publish instances #13
* Changed loading of AWS credentials to use 'Instance Profile' only #17
* Added a standardized name tag to the publish snapshot #16
* Changed the HTTP Method from POST to GET for reverse replication #22
* Changed '/libs/cq' to just 'cq' for sling:resourceType when creating a replication agent #23
* Now reads the SNS topic ARN from Cloud Formation based on the logical id #21
* Scale down handlers now cater for inexisting replication/flush agent #18
* Creation of reverse replication agents now includes 'userId' attribute
* Prioritises Publish and Publish-Dispatcher instances from same AZ #3
* Added relaxed SSL support to AEM replication agent creation #19

### 0.9.2
* Added configurable snapshot tags #2
* Added support for reverse replication queues
* Device name (used for snapshots) is now configurable #10
* Reading a test notification message off the SQS queue no longer causes the logs to show an error #7
* Fixed a bug where the scale down actions were not triggering correctly, causing replication agents to never be removed
* Fixed NoSuchElementException error when scaling publish up bug #4
* Fixed unable to delete flush and replication agents #11

### 0.9.1
* Initial version
