# AEM Orchestrator Scale Up Events
Scale up events come from either increasing the desired capacity of an auto scaling group (ASG), recovering from a terminated instance or stack start up. Generally an instance will have already been initialised by the ASG and is ready for the Orchestrator to perform a *Scale Up Action* to prepare it for use. Currently there are three types of scale up actions:

* Scale Up Author Dispatcher
* Scale Up Publish
* Scale Up Publish Dispatcher

The ASG will trigger the Orchestrator to perform one of these actions by placing a message with subject **"Auto Scaling: launch"** on the SQS queue. The Orchestrator will determine which tier the event belongs to by inspecting the **"AutoScalingGroupName"** on the message.

You can also view details of *Scale Down Actions* [here](./scale-down-events.md).


## Messages
The message format is the Amazon SNS [HTTP/HTTPS Notification JSON Format](http://docs.aws.amazon.com/sns/latest/dg/json-formats.html). A JSON definition of the format can be found [here](https://sns.us-west-2.amazonaws.com/doc/2010-03-31/Notification.json). Below is an example of a *Scale Up Event* message (note the subject):

```
{
  "Type" : "Notification",
  "MessageId" : "d97394bd-2d60-57e8-8fd9-c5ae23ee0140",
  "TopicArn" : "arn:aws:sns:ap-southeast-2:123456789012:example-aem-asg-event-topic",
  "Subject" : "Auto Scaling: launch",
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
  "AccountId": "918473123456",
  "Description": "Launching a new EC2 instance: i-07d203xxxxxxxxxxx",
  "RequestId": "891107c2-1d51-401b-8857-7fcd9369537c",
  "EndTime": "2017-02-06T00:00:00.000Z",
  "AutoScalingGroupARN": "arn:aws:autoscaling:ap-southeast-2:918473058104:autoScalingGroup...",
  "ActivityId": "891107c2-1d51-401b-8857-7fcd9969537c",
  "StartTime": "2017-02-06T00:00:00.000Z",
  "Service": "AWS Auto Scaling",
  "Time": "2017-02-06T00:00:00.000Z",
  "EC2InstanceId": "i-07d203xxxxxxxxxxx",
  "StatusCode": "InProgress",
  "StatusMessage": "",
  "Details": {
    "Subnet ID": "subnet-011f9197",
    "Availability Zone": "ap-southeast-2b"
  },
  "AutoScalingGroupName": "example-aem-publish-dispatcher-stack-PublishDispatcherAutoScalingGroup",
  "Cause": "An instance was started in response to a difference between desired and actual capacity...",
  "Event": "autoscaling:EC2_INSTANCE_LAUNCH"
}
```

## Event Actions

### Scale Up Author Dispatcher
When a *Scale Up Author Dispatcher Event* occurs, the following actions are taken:

1. An AEM Flush Agent is created between the Author Dispatcher and the Author Primary
2. An *AuthorHost* tag (pointing to the Author Primary) is added Author Dispatcher instance

Below is an example of a log entry for this event:

```
INFO  c.s.a.a.ScaleUpAuthorDispatcherAction - ScaleUpAuthorDispatcherAction executing
INFO  c.s.a.aem.FlushAgentManager - Creating flush agent for dispatcher id: i-02aaxxxxxxx, and run mode: author
```

### Scale Up Publish
When a *Scale Up Publish Event* occurs, the following actions are taken:

1. The Publish instance is 'paired' with an unpaired Publish Dispatcher. This entitles adding the following instance tags:
 * PairInstanceId (Both)
 * PublishDispatcherHost (Publish only)
 * PublishHost (Publish Dispatcher only)
2. Creates an AEM Replication Agent between the Author Primary and the Publish Instance
3. If Reverse Replication is enabled, then an AEM Reverse Replication Agent is created between the Author Primary and the Publish Instance
4. Takes a snapshot of an existing healthy Publish instance to load onto the new instance (not performed if is first Publish Instance). The replication queue of the healthy Publish instance is paused during the snapshot, and resumed upon completion. If successful, the following tag is added to the instance:
 * SnapshotId (Will be blank if first Publish Instance)
5. Creates a [AEM Content Health Check](https://github.com/shinesolutions/aem-aws-stack-provisioner/blob/master/templates/aem-tools/content-healthcheck.py.epp) [alarm](./alarms.md)

Below is an example of a log entry for this event:

```
INFO  c.s.a.actions.ScaleUpPublishAction - ScaleUpPublishAction executing
INFO  c.s.a.aem.ReplicationAgentManager - Creating replication agent for publish id: i-09a9axxxxxxxx
INFO  c.s.a.aem.ReplicationAgentManager - Creating reverse replication agent for publish id: i-09a9axxxxxxxx
INFO  c.s.a.actions.ScaleUpPublishAction - Created snapshot with ID:s-3261xxxxxxxx
INFO  c.s.a.actions.ScaleUpPublishAction - Creating content health check alarm
```


### Scale Up Publish Dispatcher
When a *Scale Up Publish Dispatcher Event* occurs, the following actions are taken:

1. Changes the Publish auto scaling group desired capacity to match Publish Dispatcher's desired capacity

Below is an example of a log entry for this event:

```
INFO  c.s.a.a.ScaleUpPublishDispatcherAction - ScaleUpPublishDispatcherAction executing
INFO  c.s.a.a.ScaleUpPublishDispatcherAction - Desired capacity already matching for publish auto scaling group and it's dispatcher. No changes will be made
```


