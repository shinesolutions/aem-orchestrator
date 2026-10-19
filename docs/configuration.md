# AEM Orchestrator Configuration Details

The AEM Orchestrator requires that there be an application.properties placed in the same root directory as the application JAR file. 

## Minimum Required Properties
There are several properties that do not have default values (generally because they are defined at stack creation), which need to be present in the application.properties. Here is a list of these properties with example values:

```properties
aws.cloudformation.stackName.author = example-aem-author-stack
aws.cloudformation.stackName.authorDispatcher = example-aem-author-dispatcher-stack
aws.cloudformation.stackName.publish = example-aem-publish-stack
aws.cloudformation.stackName.publishDispatcher = example-aem-publish-dispatcher-stack
aws.cloudformation.stackName.messaging = example-aem-messaging-stack
aws.sqs.queueName = example-aem-asg-event-queue
```
The [aem-aws-stack-builder](https://github.com/shinesolutions/aem-aws-stack-builder) will generate these names for you, they just need to be added to this Orchestrator application.properties file.

The preview publish stack names (`aws.cloudformation.stackName.previewPublish*`) are optional; leave them empty if the stack has no preview publish tier.

If one of the required properties is not defined in the application.properties file, the Orchestrator fails at start up and the cause is shown in the *orchestrator.log* file.

## List of Properties
Here is a complete list of all configurable properties:

| Property Name                                                        | Default Value                            | Example Value                                |
|----------------------------------------------------------------------|------------------------------------------|----------------------------------------------|
| aws.region                                                           |                                          | ap-southeast-2                               |
| aws.sqs.queueName                                                    |                                          | example-aem-asg-event-queue                  |
| aws.cloudformation.stackName.messaging                               |                                          | example-aem-messaging-stack                  |
| aws.cloudformation.stackName.publishDispatcher                       |                                          | example-aem-publish-dispatcher-stack         |
| aws.cloudformation.stackName.publish                                 |                                          | example-aem-publish-stack                    |
| aws.cloudformation.stackName.previewPublishDispatcher                |                                          | example-aem-preview-publish-dispatcher-stack |
| aws.cloudformation.stackName.previewPublish                          |                                          | example-aem-preview-publish-stack            |
| aws.cloudformation.stackName.authorDispatcher                        |                                          | example-aem-author-dispatcher-stack          |
| aws.cloudformation.stackName.author                                  |                                          | example-aem-author-stack                     |
| aws.cloudformation.autoScaleGroup.logicalId.publishDispatcher        | PublishDispatcherAutoScalingGroup        |                                              |
| aws.cloudformation.autoScaleGroup.logicalId.publish                  | PublishAutoScalingGroup                  |                                              |
| aws.cloudformation.autoScaleGroup.logicalId.previewPublishDispatcher | PreviewPublishDispatcherAutoScalingGroup |                                              |
| aws.cloudformation.autoScaleGroup.logicalId.previewPublish           | PreviewPublishAutoScalingGroup           |                                              |
| aws.cloudformation.autoScaleGroup.logicalId.authorDispatcher         | AuthorDispatcherAutoScalingGroup         |                                              |
| aws.cloudformation.loadBalancer.logicalId.author                     | AuthorLoadBalancer                       |                                              |
| aws.cloudformation.sns.logicalId.eventTopic                          | AEMASGEventTopic                         |                                              |
| aws.client.connection.timeout                                        | 30000                                    |                                              |
| aws.client.max.errorRetry                                            | 10                                       |                                              |
| aws.client.useProxy                                                  | false                                    | true                                         |
| aws.client.proxy.host                                                |                                          | yourdomain.proxy.com                         |
| aws.client.proxy.port                                                |                                          | 8080                                         |
| aws.client.protocol                                                  | https                                    | (no effect)                                  |
| aws.snapshot.tags                                                    | Component,StackPrefix                    |                                              |
| aws.device.name                                                      | /dev/sdb                                 |                                              |
| aem.credentials.s3.use                                               | false                                    | true                                         |
| aem.credentials.s3.file.uri                                          |                                          | s3://bucket/file.ext                         |
| aem.credentials.replicator.username                                  | overwrite-me                             |                                              |
| aem.credentials.replicator.password                                  | overwrite-me                             |                                              |
| aem.credentials.orchestrator.username                                | overwrite-me                             |                                              |
| aem.credentials.orchestrator.password                                | overwrite-me                             |                                              |
| aem.protocol.publishDispatcher                                       | https                                    |                                              |
| aem.protocol.publish                                                 | https                                    |                                              |
| aem.protocol.previewPublishDispatcher                                | https                                    |                                              |
| aem.protocol.previewPublish                                          | https                                    |                                              |
| aem.protocol.authorDispatcher                                        | http                                     |                                              |
| aem.protocol.author                                                  | https                                    |                                              |
| aem.port.publishDispatcher                                           | 443                                      |                                              |
| aem.port.publish                                                     | 5433                                     |                                              |
| aem.port.previewPublishDispatcher                                    | 443                                      |                                              |
| aem.port.previewPublish                                              | 5433                                     |                                              |
| aem.port.authorDispatcher                                            | 443                                      |                                              |
| aem.port.author                                                      | 443                                      |                                              |
| aem.client.api.verifyssl                                             | false                                    | true                                         |
| aem.client.api.debug                                                 | false                                    |                                              |
| aem.client.api.connection.timeout                                    | 30000                                    |                                              |
| aem.reverseReplication.enable                                        | false                                    |                                              |
| aem.reverseReplication.transportUri.postfix                          | /bin/receive?sling:authRequestLogin=1    |                                              |
| aem.flush.logLevel                                                   | info                                     |                                              |
| aem.replication.logLevel                                             | info                                     |                                              |
| aem.reverseReplication.logLevel                                      | error                                    |                                              |
| aem.relaxed.ssl.enable                                               | true                                     | false                                        |
| management.endpoints.web.exposure.include                            | health,info                              | health                                       |
| startup.waitForAuthorElb.maxAttempts                                 | 150                                      |                                              |
| startup.waitForAuthorElb.backOffPeriod                               | 5000                                     |                                              |
| startup.waitForAuthorElb.maxBackOffPeriod                            | 160000                                   |                                              |
| startup.waitForAuthorElb.backOffPeriodMultiplier                     | 2                                        |                                              |
| http.client.relaxed.ssl.enable                                       | true                                     | false                                        |
| alarm.content.health.check.terminate.instance.enable                 | false                                    | true                                         |

You can also view the base [application.properties](src/main/resources/application.properties) file.

## Additional Details

### AWS Properties

#### aws.region
By default the region is derived from the AWS host, so this property is only needed if planning to run the Orchestrator from a **non** AWS hosted instance. Helpful when integration testing from a local instance pointing to an AWS stack.

#### aws.sqs.queueName
This is the main SQS queue that the Orchestrator will use to be notified about events on the stack. It's a required property and the Orchestrator cannot work without this being set.

#### aws.cloudformation.stackName.\*
The stack names are generated by the [aem-aws-stack-builder](https://github.com/shinesolutions/aem-aws-stack-builder). Each tier (Author, Author Dispatcher, Publish etc) has a different stack name. Generally the naming pattern is:

```
<stack prefix>-aem-<tier name>-stack
```
For example a publish tier stack with a prefix of 'example' will have a name:

```
example-aem-publish-stack
```
If you are unsure, look for the 'aws:cloudformation:stack-name' tag on instances within the tier. The tag will contain the stack name.
These are required properties, as the stack names are needed to perform Cloud Formation resource lookups at startup.

#### aws.cloudformation.\*.logicalId.\*
The logical ids only need to be set if they have been changed from their default values given by the [aem-aws-stack-builder](https://github.com/shinesolutions/aem-aws-stack-builder). The logical ids are used in conjunction with the stack names (see property above) to get the AWS physical resource ids of the *auto scale groups*, *load balancer* and *SNS topic* at Orchestrator start up. You can view the logical id of a resource within the stack by looking at the 'aws:cloudformation:logical-id' tag.

#### aws.client.connection.timeout	
The timeout in milliseconds when making client calls to the AWS API.

#### aws.client.max.errorRetry
The max number of retry attempts when making client calls to the AWS API.

#### aws.client.useProxy
If the Orchestrator sits behind a proxy, then set this to **true**

#### aws.client.proxy.*
If the proxy is enabled (aws.client.useProxy=true), then these properties will define the host and port for the proxy

#### aws.client.protocol
No longer has an effect. AWS SDK for Java v2 always calls AWS over **https**, and the proxy is always reached over **http**

#### aws.snapshot.tags	
This is a comma delimetered list of AWS tag names that are transferred from instance to the snapshot when the snapshot is taken. For example if the instance has 10 tags, and you want the *Name* and *StackPrefix* tags to be applied to the snapshot, then add them to this property e.g.:

```
aws.snapshot.tags=Name,StackPrefix
```

#### aws.device.name
The AWS device name used when getting the Volume ID for creating the snapshot


### AEM Properties

#### aem.credentials.s3.use
Setting this to *true* will tell the Orchestrator to read the AEM credentials from an S3 bucket (see *aem.credentials.s3.file.uri* below for specifing the location), instead of the application.properties file.

#### aem.credentials.s3.file.uri
The AWS S3 bucket location of an unencrypted JSON file containing AEM credentials. Here is an example of the file format:

```
{
  "orchestrator": "<orchestrator-password>",
  "replicator": "<replicator-password>"
}
```
The Orchestrator has two roles. Below is a table outlining what actions each role performs:

| Role         | Actions                        |
|--------------|--------------------------------|
| replicator   | Create Flush/Replication Agent |
| orchestrator | Pause Replication Agent        |
| orchestrator | Restart Replication Agent      |
| orchestrator | Delete Flush/Replication Agent |


#### aem.credentials.\*
The AEM credentials (username/password) for the _**replicator**_ and _**orchestrator**_ roles. See table above for details

#### aem.protocol.\*
The protocol to use when performing AEM actions at each tier (Author, Author Dispatcher, Publish, Publish Dispatcher, Preview Publish, Preview Publish Dispatcher). Will be **http** or **https**.

#### aem.port.\*
The port to use when connecting to AEM at each tier (Author, Author Dispatcher, Publish, Publish Dispatcher, Preview Publish, Preview Publish Dispatcher).

#### aem.client.api.verifyssl
If set to true, the Orchestrator verifies the SSL certificates of AEM for all AEM http calls via the [swagger-aem-4j](https://github.com/shinesolutions/swagger-aem/tree/master/java) API.

#### aem.client.api.debug
If set to true, the Orchestrator will output debug information for all AEM http calls via the [swagger-aem-4j](https://github.com/shinesolutions/swagger-aem/tree/master/java) API.

#### aem.client.api.connection.timeout
Connection timeout duration for all AEM http calls via the [swagger-aem-4j](https://github.com/shinesolutions/swagger-aem/tree/master/java) API.

#### aem.reverseReplication.enable
If set to true, the Orchestrator will create (and delete) a Reverse Replication Agent between the Publish and Author Primary in addition to the regular Replication Agent.

#### aem.reverseReplication.transportUri.postfix
Only required if reverse replication is enabled and a different transport URI is required for reverse replication. Note the postfix only applies to the end of the Publish AEM url. For example:

```
<publish url><postfix>
```
The default postfix is:

```
/bin/receive?sling:authRequestLogin=1
```
Here is an example of a Publish URL:

```
http://localhost:4503
```

#### aem.flush.logLevel, aem.replication.logLevel, aem.reverseReplication.logLevel
The log level set on the Flush, Replication and Reverse Replication Agents the Orchestrator creates.

#### aem.relaxed.ssl.enable
Enable if you want self-certified SSL certificates to be accepted by AEM when setting up replication agents. 


### Orchestrator Properties

#### management.endpoints.web.exposure.include
The Orchestrator has a very basic JSON formatted health check page and info page, which can be accessed via:

```
http://<orchestrator domain>:8080/actuator/health
http://<orchestrator domain>:8080/actuator/info
```
The info page generally contains information about environment properties loaded at startup. Can be useful for debugging.

By default both pages are exposed. To expose only the health check page, set this property to **health**.

#### startup.waitForAuthorElb.maxAttempts
Before the Orchestrator starts receiving messages on the SQS queue, it first waits for the Author Elastic Load Balancer (ELB) to be in a healthy state. This helps to avoid continuous errors in the logs at start up. This property defines the max attempts to check the Author ELB is healthy before terminating the Orchestrator.

#### startup.waitForAuthorElb.backOffPeriod
Same as above, but defines how long to wait in milliseconds before checking the Author ELB again after the first check.

#### startup.waitForAuthorElb.backOffPeriodMultiplier
The wait between checks is multiplied by this value after each check, so the checks back off exponentially.

#### startup.waitForAuthorElb.maxBackOffPeriod
The longest wait in milliseconds between checks, however many checks have been made.

#### http.client.relaxed.ssl.enable
When Orchestrator performs health checks to AEM instances (via https) it will ignore host name verification if this is enabled.

#### alarm.content.health.check.terminate.instance.enable
If set to true, the Orchestrator terminates the Publish instance when the [content health check alarm](alarms.md) triggers. Otherwise the unhealthy instance is only logged as an error.
