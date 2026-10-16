# Changelog
All notable changes to this project will be documented in this file.

The format is based on [Keep a Changelog](https://keepachangelog.com/en/1.0.0/),
and this project adheres to [Semantic Versioning](https://semver.org/spec/v2.0.0.html).

## [Unreleased]
### Added
- Added an end-to-end integration test that runs the orchestrator against mocked AWS and AEM
- Added Makefile targets for ci, deps, build, test, integration-test, changelog-check, release-check, release-test, release-status, release-drop and release-major/minor/patch
- Added `make release-check` to sign and bundle a release for Maven Central without uploading it
- Added Maven Central credentials to `.mvn/settings.xml`, read from environment variables
- Added Release Major/Minor/Patch GitHub workflows, and a Publish to Maven Central workflow that runs `make publish` on a pushed release tag with the Maven Central and GPG secrets

### Changed
- Upgraded Java from 8 to 21 and requires Maven 3.10 or later
- Upgraded Spring Boot from 2.1 to 4.1
- Upgraded AWS SDK for Java from v1 to v2 and Amazon SQS Java Messaging Library to 2.1.4
- Upgraded Jackson to 3, keeping the previous JSON parsing behaviour
- Replaced Apache HttpClient 4 with HttpClient 5
- Migrated unit tests to JUnit 5 and Mockito 5
- The JAR is no longer a fully executable script, start it with `java -jar`
- Replaced the obsolete `endpoints.*` properties with `management.endpoints.web.exposure.include=health,info`
- `aws.client.protocol` no longer has an effect, AWS SDK v2 always calls AWS over HTTPS
- CI now runs `make ci` in aem-platform-buildenv 6.2.1, including a changelog check, integration tests and SpotBugs
- Pull requests must now add an entry under `## [Unreleased]` in CHANGELOG.md
- Artifact signing now only runs in the `release` Maven profile
- Maven Central deployments now wait for a manual Publish or Drop in the Central Portal, use `-DautoPublish=true` to publish automatically
- Formatted the code with Google Java Style and fixed all Checkstyle warnings
- Releases are now made with RTK, by `make release-major|minor|patch` or the Release Major/Minor/Patch GitHub workflows, replacing maven-release-plugin
- Development versions are now pre-release versions such as `4.0.1-pre.0` instead of `-SNAPSHOT`
- Dated the old releases in CHANGELOG.md from their git tags, as RTK requires, and linked issue references and versions
- Simplify CI workflow to not use action files anymore

### Fixed
- Fixed pausing a replication agent reusing a request shared between calls
- Fixed log messages that showed a null or wrong instance ID during scale-down and content health alarms

## [4.0.0] - 2023-07-28
### Added
- Add Preview-Publish & Preview-Publish-Dispatcher orchestration logic #RS-161

## [3.0.1] - 2020-12-02
### Fixed
- Fixed failing connection when Author ELB is assigned with a self-signed certificate [#56]

## [3.0.0] - 2020-11-23
### Added
- Add configuration to control the verification of the SSL Certificate when executing API calls to AEM [#51]

### Changed
- Replaced ELB Client with ELBv2 client to support the new AEM OpenCloud architecture which uses an Application Load Balancer [#47]
- Changed `com.shinesolutions.aemorchestrator.config.ProxyConfig` to return empty string instead of `null` [#49]
- Change SSL Verification of the AEM SSL certificate to false [#51]
- Change default AEM connection configuration from HTTP to HTTPS [#54]

## [2.0.1] - 2019-05-20
### Added
- Added new COMPONENT_INIT_STATUS to instance tags

### Changed
- Change default orchestrator and replicator credentials to overwrite-me/overwrite-me
- Changed process of determining the healthy publish instance to check component init state instead of just AEM health

## [2.0.0] - 2018-06-25
### Added
- Add JCR content CQ distribute property to post agent request model

### Changed
- Upgrade swaggeraem4j to 0.10.0 with AEM 6.4 support

## [1.0.3] - 2018-05-26
### Changed
- Change alarm.content.health.check.terminate.instance.enable default to false
- By default, Orchestrator does not terminate instance when content health check fails

## [1.0.2] - 2018-05-07
### Changed
- Fix aws.cloudformation.stackName.publishDispatcher to be really the stack name, it used to have stack ARN

## [1.0.1] - 2018-01-18
### Added
- Add debug logs showing raw message on each event handler [#34]

### Changed
- Fix replication agent pausing not adding activated content to the queue [#31]
- Fix unrecognised InvokingAlarms field error when ASG scale up policy is triggered [#30]
- Modify resource readiness checker to use exponential backoff retry policy [#29]

## [1.0.0] - 2017-06-02
### Changed
- increase the default the wait for healthy author elb time to 12.5 minutes.
- ignore auto scaling launch event fired from moving ec2 instance out of standby
- use the oldest publish instance with snapshot id tag to get a snapshot from for new publish instance

## [0.9.3] - 2017-04-18
### Added
- Added a standardized name tag to the publish snapshot [#16]
- Added relaxed SSL support to AEM replication agent creation [#24]

### Changed
- By default, now logs to root application directory (orchestrator.log)
- Performs a health check on the Publish instance before doing a Snapshot [#15]
- Fixed bug causing continual snapshotting when unable to find a publish pair candidate [#12]
- Creates a content health check alarm on the publish instance [#19]
- Fix cross-snapshotting publish instances [#13]
- Changed loading of AWS credentials to use 'Instance Profile' only [#17]
- Changed the HTTP Method from POST to GET for reverse replication [#22]
- Changed '/libs/cq' to just 'cq' for sling:resourceType when creating a replication agent [#23]
- Now reads the SNS topic ARN from Cloud Formation based on the logical id [#21]
- Scale down handlers now cater for inexisting replication/flush agent [#18]
- Creation of reverse replication agents now includes 'userId' attribute
- Prioritises Publish and Publish-Dispatcher instances from same AZ [#3]

## [0.9.2] - 2017-03-07
### Added
- Added configurable snapshot tags [#2]
- Added support for reverse replication queues

### Changed
- Device name (used for snapshots) is now configurable [#10]
- Reading a test notification message off the SQS queue no longer causes the logs to show an error [#7]
- Fixed a bug where the scale down actions were not triggering correctly, causing replication agents to never be removed
- Fixed NoSuchElementException error when scaling publish up bug [#4]
- Fixed unable to delete flush and replication agents [#11]

## [0.9.1] - 2017-02-27
### Added
- Initial version

[#2]: https://github.com/shinesolutions/aem-orchestrator/issues/2
[#3]: https://github.com/shinesolutions/aem-orchestrator/issues/3
[#4]: https://github.com/shinesolutions/aem-orchestrator/issues/4
[#7]: https://github.com/shinesolutions/aem-orchestrator/issues/7
[#10]: https://github.com/shinesolutions/aem-orchestrator/issues/10
[#11]: https://github.com/shinesolutions/aem-orchestrator/issues/11
[#12]: https://github.com/shinesolutions/aem-orchestrator/issues/12
[#13]: https://github.com/shinesolutions/aem-orchestrator/issues/13
[#15]: https://github.com/shinesolutions/aem-orchestrator/issues/15
[#16]: https://github.com/shinesolutions/aem-orchestrator/issues/16
[#17]: https://github.com/shinesolutions/aem-orchestrator/issues/17
[#18]: https://github.com/shinesolutions/aem-orchestrator/issues/18
[#19]: https://github.com/shinesolutions/aem-orchestrator/issues/19
[#21]: https://github.com/shinesolutions/aem-orchestrator/issues/21
[#22]: https://github.com/shinesolutions/aem-orchestrator/issues/22
[#23]: https://github.com/shinesolutions/aem-orchestrator/issues/23
[#24]: https://github.com/shinesolutions/aem-orchestrator/issues/24
[#29]: https://github.com/shinesolutions/aem-orchestrator/issues/29
[#30]: https://github.com/shinesolutions/aem-orchestrator/issues/30
[#31]: https://github.com/shinesolutions/aem-orchestrator/issues/31
[#34]: https://github.com/shinesolutions/aem-orchestrator/issues/34
[#47]: https://github.com/shinesolutions/aem-orchestrator/issues/47
[#49]: https://github.com/shinesolutions/aem-orchestrator/issues/49
[#51]: https://github.com/shinesolutions/aem-orchestrator/issues/51
[#54]: https://github.com/shinesolutions/aem-orchestrator/issues/54
[#56]: https://github.com/shinesolutions/aem-orchestrator/issues/56

[Unreleased]: https://github.com/shinesolutions/aem-orchestrator/compare/aem-orchestrator-4.0.0...HEAD
[4.0.0]: https://github.com/shinesolutions/aem-orchestrator/compare/aem-orchestrator-3.0.1...aem-orchestrator-4.0.0
[3.0.1]: https://github.com/shinesolutions/aem-orchestrator/compare/aem-orchestrator-3.0.0...aem-orchestrator-3.0.1
[3.0.0]: https://github.com/shinesolutions/aem-orchestrator/compare/aem-orchestrator-2.0.1...aem-orchestrator-3.0.0
[2.0.1]: https://github.com/shinesolutions/aem-orchestrator/compare/aem-orchestrator-2.0.0...aem-orchestrator-2.0.1
[2.0.0]: https://github.com/shinesolutions/aem-orchestrator/compare/aem-orchestrator-1.0.3...aem-orchestrator-2.0.0
[1.0.3]: https://github.com/shinesolutions/aem-orchestrator/compare/aem-orchestrator-1.0.2...aem-orchestrator-1.0.3
[1.0.2]: https://github.com/shinesolutions/aem-orchestrator/compare/aem-orchestrator-1.0.1...aem-orchestrator-1.0.2
[1.0.1]: https://github.com/shinesolutions/aem-orchestrator/compare/aem-orchestrator-1.0.0...aem-orchestrator-1.0.1
[1.0.0]: https://github.com/shinesolutions/aem-orchestrator/compare/aem-orchestrator-0.9.3...aem-orchestrator-1.0.0
[0.9.3]: https://github.com/shinesolutions/aem-orchestrator/compare/aem-orchestrator-0.9.2...aem-orchestrator-0.9.3
[0.9.2]: https://github.com/shinesolutions/aem-orchestrator/compare/aem-orchestrator-0.9.1...aem-orchestrator-0.9.2
[0.9.1]: https://github.com/shinesolutions/aem-orchestrator/releases/tag/aem-orchestrator-0.9.1
