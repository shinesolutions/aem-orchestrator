# AEM Orchestrator Configuration Details

The AEM Orchestrator requires that there be an application.properties placed in the same root directory as the application JAR file. 

## Minimum Required Properties
There are several properties that do not have default values (generally because they are defined at stack creation), which need to be present in the application.properties. Here is a list of these properties with example values:

```properties
aws.cloudformation.stackName.author = example-aem-author-stack
aws.cloudformation.stackName.authorDispatcher = example-aem-author-dispatcher-stack
aws.cloudformation.stackName.publish = example-aem-publish-stack
aws.cloudformation.stackName.publishDispatcher = example-aem-publish-dispatcher-stack
aws.sns.topicName = example-aem-asg-event-topic
aws.sqs.queueName = example-aem-asg-event-queue
```
The [aem-aws-stack-builder](https://github.com/shinesolutions/aem-aws-stack-builder) will generate these names for you, they just need to be added to this Orchestrator application.properties file.

If one of these required properties is not defined in the application.properties file, then you will see an error in the *orchestrator.log* file when the Orchestrator is started. Here is an example:

```
ERROR o.s.boot.SpringApplication - Application startup failed
...
Caused by: org.springframework.beans.InvalidPropertyException: Invalid property 'aws.sns.topicName' of bean class [com.shinesolutions.aemorchestrator.model.EnvironmentValues]: Topic name cannot be empty or null
```

## List of Properties
Here is a complete list of all configurable properties:

| Property Name                                                 | Default Value                         | Example Value                        |
|---------------------------------------------------------------|---------------------------------------|--------------------------------------|
| aws.region                                                    |                                       | ap-southeast-2                       |
| aws.sqs.queueName                                             |                                       | example-aem-asg-event-queue          |
| aws.sns.topicName                                             |                                       | example-aem-asg-event-topic          |
| aws.cloudformation.stackName.publishDispatcher                |                                       | example-aem-publish-dispatcher-stack |
| aws.cloudformation.stackName.publish                          |                                       | example-aem-publish-stack            |
| aws.cloudformation.stackName.authorDispatcher                 |                                       | example-aem-author-dispatcher-stack  |
| aws.cloudformation.stackName.author                           |                                       | example-aem-author-stack             |
| aws.cloudformation.autoScaleGroup.logicalId.publishDispatcher | PublishDispatcherAutoScalingGroup     |                                      |
| aws.cloudformation.autoScaleGroup.logicalId.publish           | PublishAutoScalingGroup               |                                      |
| aws.cloudformation.autoScaleGroup.logicalId.authorDispatcher  | AuthorDispatcherAutoScalingGroup      |                                      |
| aws.cloudformation.loadBalancer.logicalId.author              | AuthorLoadBalancer                    |                                      |
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
| endpoints.health.enabled                                      | true                                  | true                                 |
| endpoints.info.enabled                                        | true                                  | true                                 |
| startup.waitForAuthorElb.maxAttempts                          | 100                                   |                                      |
| startup.waitForAuthorElb.backOffPeriod                        | 5000                                  |                                      |
You can also view the base [application.properties](src/main/resources/application.properties) file.

