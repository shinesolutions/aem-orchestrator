# AEM Orchestrator Configuration Details

The AEM Orchestrator requires that there be an application.properties placed in the same root directory as the application JAR file. 

## List of Properties
Here is a complete list of all configurable properties:

### AWS Related Properties
| Property Name                                                 | Default Value                     | Example Value                        | Description                                                                                                                      |
|---------------------------------------------------------------|-----------------------------------|--------------------------------------|----------------------------------------------------------------------------------------------------------------------------------|
| aws.region                                                    |                                   | ap-southeast-2                       | Only needed if not running the Orchestrator on an AWS EC2 instance. By default, it will get the region from the instance itself. |
| aws.sqs.queueName                                             |                                   | example-aem-asg-event-queue          | The SQS queue name where the messages will be read from                                                                          |
| aws.sns.topicName                                             |                                   | example-aem-asg-event-topic          | The SNS topic name will be resolved to an ARN and used in the creation of the content health check alarm.                        |
| aws.cloudformation.stackName.publishDispatcher                |                                   | example-aem-publish-dispatcher-stack | The name of the stack Publish Dispatcher tier. Look for the 'aws:cloudformation:stack-name' tag on instances within the tier.    |
| aws.cloudformation.stackName.publish                          |                                   | example-aem-publish-stack            | The name of the stack Publish tier. Look for the 'aws:cloudformation:stack-name' tag on instances within the tier.               |
| aws.cloudformation.stackName.authorDispatcher                 |                                   | example-aem-author-dispatcher-stack  | The name of the stack Author Dispatcher tier. Look for the 'aws:cloudformation:stack-name' tag on instances within the tier.     |
| aws.cloudformation.stackName.author                           |                                   | example-aem-author-stack             | The name of the stack Author tier. Look for the 'aws:cloudformation:stack-name' tag on instances within the tier.                |
| aws.cloudformation.autoScaleGroup.logicalId.publishDispatcher | PublishDispatcherAutoScalingGroup |                                      | The logical ID of the Publish Dispatcher ASG. Look for the 'aws:cloudformation:logical-id' tag on instances within the tier.     |
| aws.cloudformation.autoScaleGroup.logicalId.publish           | PublishAutoScalingGroup           |                                      | The logical ID of the Publish ASG. Look for the 'aws:cloudformation:logical-id' tag on instances within the tier.                |
| aws.cloudformation.autoScaleGroup.logicalId.authorDispatcher  | AuthorDispatcherAutoScalingGroup  |                                      | The logical ID of the Author Dispatcher ASG. Look for the 'aws:cloudformation:logical-id' tag on instances within the tier.      |
| aws.cloudformation.loadBalancer.logicalId.author              | AuthorLoadBalancer                |                                      | The logical id of the Author Load Balancer.                                                                                      |
| aws.client.connection.timeout                                 | 30000                             |                                      | The timeout wait period when making AWS API calls                                                                                |
| aws.client.max.errorRetry                                     | 10                                |                                      | The max error retry attempts when making AWS API calls                                                                           |
| aws.client.useProxy                                           | false                             | true                                 | If set to true, it will send all AWS API calls through the defined proxy.                                                        |
| aws.client.proxy.host                                         |                                   | yourdomain.proxy.com                 | The proxy host name to use when making AWS API calls. Requires that 'aws.client.useProxy=true'                                   |
| aws.client.proxy.port                                         |                                   | 8080                                 | The proxy port number to use when making AWS API calls. Requires that 'aws.client.useProxy=true'                                 |
| aws.client.protocol                                           | https                             |                                      | The proxy protocol to use when making AWS API calls. Requires that 'aws.client.useProxy=true'                                    |
| aws.snapshot.tags                                             | Component,StackPrefix             |                                      | Instance tags that are transferred from instance to the snapshot when a snapshot is taken.                                       |
| aws.device.name                                               | /dev/sdb                          |                                      | Used when getting the Volume ID prior to performing a snapshot.                                                                  |

You can also view the base [application.properties](src/main/resources/application.properties) file.

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