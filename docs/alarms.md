# AEM Orchestrator Alarms
Alarm events generally come from AWS Cloud Watch alarms that monitor a particular metric and trigger when that metric moves outside of the acceptable threshold. Currently there is only one alarm:

* Content Health Check Alarm

When an alarm triggers, it will place a message with subject beggining with **"ALARM"** on the SQS queue. The message will contain details of the instance for which the alarm was monitoring, allowing the Orchestrator to take action on that instance.


## Messages
The message format is the Amazon SNS [HTTP/HTTPS Notification JSON Format](http://docs.aws.amazon.com/sns/latest/dg/json-formats.html). A JSON definition of the format can be found [here](https://sns.us-west-2.amazonaws.com/doc/2010-03-31/Notification.json). Below is an example of an *Alarm* message (note the subject):

```
{
  "Type" : "Notification",
  "MessageId" : "2c1aeb6a-e2e9-5132-a196-ba504b7244b0",
  "TopicArn" : "arn:aws:sns:ap-southeast-2:123456789012:example-aem-asg-event-topic",
  "Subject" : "ALARM: \"contentHealthCheck\" in Asia Pacific - Sydney",
  "Message" : "...<see message content below>...",
  "Timestamp" : "2017-01-11T00:00:00.000Z",
  "SignatureVersion" : "1",
  "Signature" : "DAFaXmSktOaVBwhWXmgQWQF0vHPrKy5L/NA+2pbJfdUX...",
  "SigningCertURL" : "https://sns.ap-southeast-2.amazonaws.com/...",
  "UnsubscribeURL" : "https://sns.ap-southeast-2.amazonaws.com/..."
}
```

### Message Content
The message content comes from the "Message" part of the SNS formatted SQS message (see above). Details on the message content format can be found [here](http://docs.aws.amazon.com/autoscaling/latest/userguide/ASGettingNotifications.html#auto-scaling-sns-notifications). The content is a single line unescaped JSON string. Here is an escaped version (for readability purposes):

```
{
  "AlarmName": "contentHealthCheck",
  "AlarmDescription": "Content Health Check for Publish",
  "AWSAccountId": "918474058204",
  "NewStateValue": "ALARM",
  "NewStateReason": "Threshold Crossed: 1 datapoint (0.0) was less than the threshold (1.0).",
  "StateChangeTime": "2017-01-11T00:00:00.000Z",
  "Region": "Asia Pacific - Sydney",
  "OldStateValue": "INSUFFICIENT_DATA",
  "Trigger": {
    "MetricName": "contentHealthCheck",
    "Namespace": "aem-publish-dispatcher-stack",
    "StatisticType": "Statistic",
    "Statistic": "MAXIMUM",
    "Unit": "unit",
    "Dimensions": [
      {
        "name": "PairInstanceId",
        "value": "i-0bddcd1e4afd0d046"
      }
    ],
    "Period": 60,
    "EvaluationPeriods": 1,
    "ComparisonOperator": "LessThanThreshold",
    "Threshold": 1,
    "TreatMissingData": "",
    "EvaluateLowSampleCountPercentile": ""
  }
}
```
The useable information from this message is within the **"Dimensions"** array. The value of the **"PairInstanceId"** is used to get the instance ID of the unhealthy Publish instance.

## Alarm Actions

### Content Health Check Alarm
The content health check alarm monitors the health of the content between the Publish Dispatcher and the Publish Instance. The details of the content to monitor are defined by the user via a [descriptor file](https://github.com/shinesolutions/aem-aws-stack-provisioner/blob/master/examples/content-healthcheck-descriptor.json). When a *Content Health Check AlarmEvent* occurs, the following actions are taken:

1. The associated Publish instance is terminated

Below is an example of a log entry for this event:

```
INFO c.s.a.actions.AlarmContentHealthCheckAction - Executing AlarmContentHealthCheckAction
INFO c.s.a.actions.AlarmContentHealthCheckAction - Terminated publish instance i-435dxxxxxxxxxxxxxxx
```



