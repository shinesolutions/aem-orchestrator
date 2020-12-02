# Changelog
All notable changes to this project will be documented in this file.

The format is based on [Keep a Changelog](https://keepachangelog.com/en/1.0.0/),
and this project adheres to [Semantic Versioning](https://semver.org/spec/v2.0.0.html).

## [Unreleased]
### Fixed
- Fixed failing connection when Author ELB is assigned with a self-signed certificate #56

## [3.0.0] - 2020-11-23

### Added
- Add configuration to control the verification of the SSL Certificate when executing API calls to AEM #51

### Changed
- Replaced ELB Client with ELBv2 client to support the new AEM OpenCloud architecture which uses an Application Load Balancer #47
- Changed `com.shinesolutions.aemorchestrator.config.ProxyConfig` to return empty string instead of `null` #49
- Change SSL Verification of the AEM SSL certificate to false #51
- Change default AEM connection configuration from HTTP to HTTPS #54

## [2.0.1] - unknown

### Added
- Added new COMPONENT_INIT_STATUS to instance tags

### Changed
- Change default orchestrator and replicator credentials to overwrite-me/overwrite-me
- Changed process of determining the healthy publish instance to check component init state instead of just AEM health

## [2.0.0] - unknown

### Added
- Add JCR content CQ distribute property to post agent request model

### Changed
- Upgrade swaggeraem4j to 0.10.0 with AEM 6.4 support

## [1.0.3] - unknown

### Changed
- Change alarm.content.health.check.terminate.instance.enable default to false
- By default, Orchestrator does not terminate instance when content health check fails

## [1.0.2] - unknown

### Changed
- Fix aws.cloudformation.stackName.publishDispatcher to be really the stack name, it used to have stack ARN

## [1.0.1] - 2018-01-18

### Added
- Add debug logs showing raw message on each event handler #34

### Changed
- Fix replication agent pausing not adding activated content to the queue #31
- Fix unrecognised InvokingAlarms field error when ASG scale up policy is triggered #30
- Modify resource readiness checker to use exponential backoff retry policy #29

## [1.0.0] - 2017-06-02

### Changed
- increase the default the wait for healthy author elb time to 12.5 minutes.
- ignore auto scaling launch event fired from moving ec2 instance out of standby
- use the oldest publish instance with snapshot id tag to get a snapshot from for new publish instance

## [0.9.3] - unknown

### Added
- Added a standardized name tag to the publish snapshot #16
- Added relaxed SSL support to AEM replication agent creation #24

### Changed
- By default, now logs to root application directory (orchestrator.log)
- Performs a health check on the Publish instance before doing a Snapshot #15
- Fixed bug causing continual snapshotting when unable to find a publish pair candidate #12
- Creates a content health check alarm on the publish instance #19
- Fix cross-snapshotting publish instances #13
- Changed loading of AWS credentials to use 'Instance Profile' only #17
- Changed the HTTP Method from POST to GET for reverse replication #22
- Changed '/libs/cq' to just 'cq' for sling:resourceType when creating a replication agent #23
- Now reads the SNS topic ARN from Cloud Formation based on the logical id #21
- Scale down handlers now cater for inexisting replication/flush agent #18
- Creation of reverse replication agents now includes 'userId' attribute
- Prioritises Publish and Publish-Dispatcher instances from same AZ #3

## [0.9.2] - unknown

### Added
- Added configurable snapshot tags #2
- Added support for reverse replication queues

### Changed
- Device name (used for snapshots) is now configurable #10
- Reading a test notification message off the SQS queue no longer causes the logs to show an error #7
- Fixed a bug where the scale down actions were not triggering correctly, causing replication agents to never be removed
- Fixed NoSuchElementException error when scaling publish up bug #4
- Fixed unable to delete flush and replication agents #11

## [0.9.1] - unknown

### Added
- Initial version
