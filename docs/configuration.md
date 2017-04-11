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

If one of these required properties is not defined in the application.properties file, then you will see an error in the *orchestrator.log* file when the Orchestrator is started. Here is an example:

```
ERROR o.s.boot.SpringApplication - Application startup failed
...
Caused by: org.springframework.beans.InvalidPropertyException: Invalid property 'aws.sqs.queueName' of bean class [com.shinesolutions.aemorchestrator.model.EnvironmentValues]: Queue name cannot be empty or null
```

## List of Properties
Here is a complete list of all configurable properties:

| Property Name                                                 | Default Value                         | Example Value                        |
|---------------------------------------------------------------|---------------------------------------|--------------------------------------|
| aws.region                                                    |                                       | ap-southeast-2                       |
| aws.sqs.queueName                                             |                                       | example-aem-asg-event-queue          |
| aws.cloudformation.stackName.messaging                        |                                       | example-aem-messaging-stack          |
| aws.cloudformation.stackName.publishDispatcher                |                                       | example-aem-publish-dispatcher-stack |
| aws.cloudformation.stackName.publish                          |                                       | example-aem-publish-stack            |
| aws.cloudformation.stackName.authorDispatcher                 |                                       | example-aem-author-dispatcher-stack  |
| aws.cloudformation.stackName.author                           |                                       | example-aem-author-stack             |
| aws.cloudformation.autoScaleGroup.logicalId.publishDispatcher | PublishDispatcherAutoScalingGroup     |                                      |
| aws.cloudformation.autoScaleGroup.logicalId.publish           | PublishAutoScalingGroup               |                                      |
| aws.cloudformation.autoScaleGroup.logicalId.authorDispatcher  | AuthorDispatcherAutoScalingGroup      |                                      |
| aws.cloudformation.loadBalancer.logicalId.author              | AuthorLoadBalancer                    |                                      |
| aws.cloudformation.sns.logicalId.eventTopic                   | AEMASGEventTopic                      |                                      |
| aws.client.connection.timeout                                 | 30000                                 |                                      |
| aws.client.max.errorRetry                                     | 10                                    |                                      |
| aws.client.useProxy                                           | false                                 | true                                 |
| aws.client.proxy.host                                         |                                       | yourdomain.proxy.com                 |
| aws.client.proxy.port                                         |                                       | 8080                                 |
| aws.client.protocol                                           | https                                 |                                      |
| aws.snapshot.tags                                             | Component,StackPrefix                 |                                      |
| aws.device.name                                               | /dev/sdb                              |                                      |
| aem.credentials.s3.use                                        | false                                 | true                                 |
| aem.credentials.s3.file.uri                                   |                                       | s3://bucket/file.ext                 |
| aem.credentials.replicator.username                           | admin                                 |                                      |
| aem.credentials.replicator.password                           | admin                                 |                                      |
| aem.credentials.orchestrator.username                         | admin                                 |                                      |
| aem.credentials.orchestrator.password                         | admin                                 |                                      |
| aem.protocol.publishDispatcher                                | http                                  |                                      |
| aem.protocol.publish                                          | http                                  |                                      |
| aem.protocol.authorDispatcher                                 | http                                  |                                      |
| aem.protocol.author                                           | http                                  |                                      |
| aem.port.publishDispatcher                                    | 80                                    |                                      |
| aem.port.publish                                              | 4503                                  |                                      |
| aem.port.authorDispatcher                                     | 80                                    |                                      |
| aem.port.author                                               | 80                                    |                                      |
| aem.client.api.debug                                          | false                                 |                                      |
| aem.client.api.connection.timeout                             | 30000                                 |                                      |
| aem.reverseReplication.enable                                 | false                                 |                                      |
| aem.reverseReplication.transportUri.postfix                   | /bin/receive?sling:authRequestLogin=1 |                                      |
| aem.relaxed.ssl.enable                                        | true                                  | false                                |
| endpoints.health.enabled                                      | true                                  | false                                |
| endpoints.info.enabled                                        | true                                  | false                                |
| startup.waitForAuthorElb.maxAttempts                          | 100                                   |                                      |
| startup.waitForAuthorElb.backOffPeriod                        | 5000                                  |                                      |
| http.client.relaxed.ssl.enable                                | true                                  | false                                |
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
By default the AWS client uses **https**, but this can be set to **http** if needed

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
The protocol to use when performing AEM actions at each tier (Author, Author Dispatcher, Publish, Publish Dispatcher). Will be **http** or **https**.

#### aem.port.\*
The port to use when connecting to AEM at each tier (Author, Author Dispatcher, Publish, Publish Dispatcher).

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

#### aem.relaxed.ssl.enable
Enable if you want self-certified SSL certificates to be accepted by AEM when setting up replication agents. 


### Orchestrator Properties

#### endpoints.health.enabled
The Orchestrator has a very basic JSON formatted health check page, which can be accessed via:

```
http://<orchestrator domain>:8080/health
```
By default the health check page is enabled, but this can be turned off by setting this property to **false**

#### endpoints.info.enabled
The Orchestrator has a very basic JSON formatted info page, which can be accessed via:

```
http://<orchestrator domain>:8080/info
```
It generally contains information about environment properties loaded at startup. Can be useful for debugging.

#### startup.waitForAuthorElb.maxAttempts
Before the Orchestrator starts receiving messages on the SQS queue, it first waits for the Author Elastic Load Balancer (ELB) to be in a healthy state. This helps to avoid continuous errors in the logs at start up. This property defines the max attempts to check the Author ELB is healthy before terminating the Orchestrator.

#### startup.waitForAuthorElb.backOffPeriod
Same as above, but defines how long to wait in seconds between checking the Author ELB is in a healthy state.

#### http.client.relaxed.ssl.enable
When Orchestrator performs health checks to AEM instances (via https) it will ignore host name verification if this is enabled.


