# AEM Orchestrator Scale Down Events
Scale down events come from either decreasing the desired capacity of an auto scaling group (ASG) or a terminated instance (for what ever reason). Generally *Scale Down Actions* perform clean up operations on resources associated with the terminated instance. Currently there are three types of scale down actions:

* Scale Down Author Dispatcher
* Scale Down Publish
* Scale Down Publish Dispatcher

The ASG will trigger the Orchestrator to perform one of these actions by placing a message with subject **"Auto Scaling: termination"** on the SQS queue. The Orchestrator will determine which tier the event belongs to by inspecting the **"AutoScalingGroupName"** on the message.

You can also view details of *Scale Up Actions* [here](./scale-up-events.md).


## Messages
The message format is the Amazon SNS [HTTP/HTTPS Notification JSON Format](http://docs.aws.amazon.com/sns/latest/dg/json-formats.html). A JSON definition of the format can be found [here](https://sns.us-west-2.amazonaws.com/doc/2010-03-31/Notification.json). Below is an example of a *Scale Down Event* message (note the subject):

```
{
  "Type" : "Notification",
  "MessageId" : "d97394bd-2d60-57e8-8fd9-c5ae23ee0140",
  "TopicArn" : "arn:aws:sns:ap-southeast-2:123456789012:example-aem-asg-event-topic",
  "Subject" : "Auto Scaling: termination",
  "Message" : "...<see message content below>...",
  "Timestamp" : "2017-02-06T00:00:00.000Z",
  "SignatureVersion" : "1",
  "Signature" : "lJbIBK0144t6M8cD/Ihb9Ja76YfYVBXC1rsBMnke5FY...",
  "SigningCertURL" : "https://sns.ap-southeast-2.amazonaws.com/...",
  "UnsubscribeURL" : "https://sns.ap-southeast-2.amazonaws.com/..."
}
```

### Message Content
The message content comes from the "Message" part of the SNS formatted SQS message (see above). Details on the message content format can be found [here](http://docs.aws.amazon.com/autoscaling/latest/userguide/ASGettingNotifications.html#auto-scaling-sns-notifications). The content is a single line unescaped JSON string. Here is an escaped version (for readability purposes):

```
{
  "Progress": 50,
  "AccountId": "918473123457",
  "Description": "Terminating EC2 instance: i-09d59xxxxxxxxxxxxx",
  "RequestId": "9db38661-0435-4a5f-afd8-d4aef30bd859",
  "EndTime": "2017-03-23T05:41:17.218Z",
  "AutoScalingGroupARN": "arn:aws:autoscaling:ap-southeast-2:918473058104:autoScalingGroup...",
  "ActivityId": "9db38661-0435-4a5f-afd8-d4aef30bd859",
  "StartTime": "2017-03-23T05:41:04.640Z",
  "Service": "AWS Auto Scaling",
  "Time": "2017-03-23T05:41:17.218Z",
  "EC2InstanceId": "i-09d59xxxxxxxxxxxxx",
  "StatusCode": "InProgress",
  "StatusMessage": "",
  "Details": {
    "Subnet ID": "subnet-011f9197",
    "Availability Zone": "ap-southeast-2a"
  },
  "AutoScalingGroupName": "example-aem-publish-dispatcher-stack-PublishDispatcherAutoScalingGroup",
  "Cause": "Instance was taken out of service in response to a EC2 health check",
  "Event": "autoscaling:EC2_INSTANCE_TERMINATE"
}
```

## Event Actions

### Scale Down Author Dispatcher
When a *Scale Down Author Dispatcher Event* occurs, the following actions are taken:

1. Removes the AEM Flush Agent between the Author Primary and the Author Dispatcher

Below is an example of a log entry for this event:

```
INFO c.s.a.actions.ScaleDownAuthorDispatcherAction - ScaleDownAuthorDispatcherAction executing
INFO c.s.a.actions.ScaleDownAuthorDispatcherAction - Flush Agent removed successfully
```

### Scale Down Publish
When a *Scale Down Publish Event* occurs, the following actions are taken:

1. Terminates the paired Publish Dispatcher
2. Removes the AEM Replication Agent between the Author Primary and the Publish instance
3. Removes the AEM Reverse Replication Agent (if enabled)
4. Removes the associated [AEM Content Health Check](https://github.com/shinesolutions/aem-aws-stack-provisioner/blob/master/templates/aem-tools/content-healthcheck.py.epp) [alarm](./alarms.md)

Below is an example of a log entry for this event:

```
INFO  c.s.a.actions.ScaleDownPublishAction - ScaleDownPublishAction executing
WARN  c.s.a.actions.ScaleDownPublishAction - Unable to terminate paired publish dispatcher with ID: null. It may already be terminated
INFO  c.s.a.aem.ReplicationAgentManager - Deleting replication agent for publish id: i-070e1xxxxxxxxxxxx
INFO  c.s.a.aem.ReplicationAgentManager - Deleting reverse replication agent for publish id: i-070e1xxxxxxxxxxxx
```

### Scale Down Publish Dispatcher
When a *Scale Down Publish Dispatcher Event* occurs, the following actions are taken:

1. Terminate paired Publish instance
2. Change Publish auto scaling group desired capacity to match Publish Dispatcher tier

Below is an example of a log entry for this event:

```
INFO  c.s.a.a.ScaleDownPublishDispatcherAction - ScaleDownPublishDispatcherAction executing
INFO  c.s.a.a.ScaleDownPublishDispatcherAction - Terminating paired publish instance with ID: i-070e1xxxxxxxxxxxx
INFO  c.s.a.a.ScaleDownPublishDispatcherAction - Desired capacity already matching for publish and dispatcher. No changes will be made
```

