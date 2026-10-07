package com.shinesolutions.aemorchestrator.service;

import com.shinesolutions.aemorchestrator.model.EC2Instance;
import com.shinesolutions.aemorchestrator.model.InstanceTags;
import jakarta.annotation.Resource;
import java.io.IOException;
import java.net.URI;
import java.time.Instant;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import org.springframework.retry.annotation.Backoff;
import org.springframework.retry.annotation.Retryable;
import org.springframework.stereotype.Component;
import software.amazon.awssdk.awscore.exception.AwsServiceException;
import software.amazon.awssdk.core.ResponseInputStream;
import software.amazon.awssdk.services.autoscaling.AutoScalingClient;
import software.amazon.awssdk.services.autoscaling.model.AutoScalingGroup;
import software.amazon.awssdk.services.autoscaling.model.DescribeAutoScalingGroupsRequest;
import software.amazon.awssdk.services.autoscaling.model.DescribeAutoScalingGroupsResponse;
import software.amazon.awssdk.services.autoscaling.model.Instance;
import software.amazon.awssdk.services.autoscaling.model.SetDesiredCapacityRequest;
import software.amazon.awssdk.services.cloudformation.CloudFormationClient;
import software.amazon.awssdk.services.cloudformation.model.DescribeStackResourcesRequest;
import software.amazon.awssdk.services.cloudformation.model.DescribeStackResourcesResponse;
import software.amazon.awssdk.services.cloudwatch.CloudWatchClient;
import software.amazon.awssdk.services.cloudwatch.model.ComparisonOperator;
import software.amazon.awssdk.services.cloudwatch.model.DeleteAlarmsRequest;
import software.amazon.awssdk.services.cloudwatch.model.Dimension;
import software.amazon.awssdk.services.cloudwatch.model.PutMetricAlarmRequest;
import software.amazon.awssdk.services.cloudwatch.model.Statistic;
import software.amazon.awssdk.services.ec2.Ec2Client;
import software.amazon.awssdk.services.ec2.model.CreateSnapshotRequest;
import software.amazon.awssdk.services.ec2.model.CreateSnapshotResponse;
import software.amazon.awssdk.services.ec2.model.CreateTagsRequest;
import software.amazon.awssdk.services.ec2.model.DescribeInstanceAttributeRequest;
import software.amazon.awssdk.services.ec2.model.DescribeInstanceAttributeResponse;
import software.amazon.awssdk.services.ec2.model.DescribeInstancesRequest;
import software.amazon.awssdk.services.ec2.model.DescribeInstancesResponse;
import software.amazon.awssdk.services.ec2.model.DescribeTagsRequest;
import software.amazon.awssdk.services.ec2.model.DescribeTagsResponse;
import software.amazon.awssdk.services.ec2.model.EbsInstanceBlockDevice;
import software.amazon.awssdk.services.ec2.model.Filter;
import software.amazon.awssdk.services.ec2.model.InstanceAttributeName;
import software.amazon.awssdk.services.ec2.model.InstanceBlockDeviceMapping;
import software.amazon.awssdk.services.ec2.model.InstanceStateName;
import software.amazon.awssdk.services.ec2.model.Tag;
import software.amazon.awssdk.services.ec2.model.TagDescription;
import software.amazon.awssdk.services.ec2.model.TerminateInstancesRequest;
import software.amazon.awssdk.services.elasticloadbalancingv2.ElasticLoadBalancingV2Client;
import software.amazon.awssdk.services.elasticloadbalancingv2.model.DescribeLoadBalancersRequest;
import software.amazon.awssdk.services.elasticloadbalancingv2.model.DescribeLoadBalancersResponse;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.S3Uri;
import software.amazon.awssdk.services.s3.model.GetObjectRequest;
import software.amazon.awssdk.services.s3.model.GetObjectResponse;
import software.amazon.awssdk.utils.IoUtils;

/** Helper class for performing a range of AWS functions */
@Component
public class AwsHelperService {

  @Resource public Ec2Client amazonEC2Client;

  @Resource public ElasticLoadBalancingV2Client amazonElbClient;

  @Resource public AutoScalingClient amazonAutoScalingClient;

  @Resource public CloudFormationClient amazonCloudFormationClient;

  @Resource public S3Client amazonS3Client;

  @Resource public CloudWatchClient amazonCloudWatchClient;

  /**
   * Return the DNS name for a given AWS ELB group name
   *
   * @param elbName the ELB group name
   * @return String DNS name
   */
  public String getElbDnsName(String elbName) {
    DescribeLoadBalancersResponse result =
        amazonElbClient.describeLoadBalancers(
            DescribeLoadBalancersRequest.builder().names(elbName).build());
    return result.loadBalancers().get(0).dnsName();
  }

  /**
   * Return the name for a given AWS ELB group name
   *
   * @param elbArn the ELB Arn
   * @return String ELB name
   */
  public String getElbName(String elbArn) {
    DescribeLoadBalancersResponse result =
        amazonElbClient.describeLoadBalancers(
            DescribeLoadBalancersRequest.builder().loadBalancerArns(elbArn).build());
    return result.loadBalancers().get(0).loadBalancerName();
  }

  /**
   * Gets the private IP of a given AWS EC2 instance Will automatically retry 10 times every 10
   * seconds if no instance is found
   *
   * @param instanceId the EC2 instance ID
   * @return String private IP
   */
  @Retryable(maxAttempts = 10, value = AwsServiceException.class, backoff = @Backoff(delay = 5000))
  public String getPrivateIp(String instanceId) {
    DescribeInstancesResponse result =
        amazonEC2Client.describeInstances(
            DescribeInstancesRequest.builder().instanceIds(instanceId).build());

    try {
      return result.reservations().get(0).instances().get(0).privateIpAddress();
    } catch (Exception e) {
      throw AwsServiceException.builder()
          .message(
              "Failed to get IP for instance ID: " + instanceId + ". Instance may not be active")
          .cause(e)
          .build();
    }
  }

  /**
   * Gets the launch time of a given AWS EC2 instance Will automatically retry 10 times every 10
   * seconds if no instance is found
   *
   * @param instanceId the EC2 instance ID
   * @return Date launchTime
   */
  @Retryable(maxAttempts = 10, value = AwsServiceException.class, backoff = @Backoff(delay = 5000))
  public Date getLaunchTime(String instanceId) {
    DescribeInstancesResponse result =
        amazonEC2Client.describeInstances(
            DescribeInstancesRequest.builder().instanceIds(instanceId).build());

    try {
      Instant launchTime = result.reservations().get(0).instances().get(0).launchTime();
      return Date.from(launchTime);
    } catch (Exception e) {
      throw AwsServiceException.builder()
          .message(
              "Failed to get Launch Date for instance ID: "
                  + instanceId
                  + ". Instance may not be active")
          .cause(e)
          .build();
    }
  }

  /**
   * Gets the availability zone of a given instance
   *
   * @param instanceId the AWS instance ID
   * @return The Availability Zone of the instance.
   */
  public String getAvailabilityZone(String instanceId) {
    DescribeInstancesResponse result =
        amazonEC2Client.describeInstances(
            DescribeInstancesRequest.builder().instanceIds(instanceId).build());

    return result.reservations().get(0).instances().get(0).placement().availabilityZone();
  }

  /**
   * Checks if a instance is in a 'running' state. Will return false if the instance is in any other
   * of the possible states: pending, shutting-down, terminated, stopping, stopped or non-existent.
   *
   * @param instanceId EC2 instance id
   * @return true if the instance is in a 'running' state. False for any other state
   */
  public boolean isInstanceRunning(String instanceId) {
    boolean isInstanceRunning = false;
    DescribeInstancesResponse result =
        amazonEC2Client.describeInstances(
            DescribeInstancesRequest.builder().instanceIds(instanceId).build());

    try {
      InstanceStateName stateName = result.reservations().get(0).instances().get(0).state().name();
      isInstanceRunning = (stateName == InstanceStateName.RUNNING);
    } catch (IndexOutOfBoundsException e) {
    } // Instance is long gone

    return isInstanceRunning;
  }

  /**
   * Terminates an EC2 instance for a given instance ID
   *
   * @param instanceId the EC2 instance ID
   */
  public void terminateInstance(String instanceId) {
    amazonEC2Client.terminateInstances(
        TerminateInstancesRequest.builder().instanceIds(instanceId).build());
  }

  /**
   * Gets a map of tags for an AWS EC2 instance
   *
   * @param instanceId the EC2 instance ID
   * @return Map of AWS tags
   */
  public Map<String, String> getTags(String instanceId) {
    Filter filter = Filter.builder().name("resource-id").values(instanceId).build();
    DescribeTagsResponse result =
        amazonEC2Client.describeTags(DescribeTagsRequest.builder().filters(filter).build());
    return result.tags().stream()
        .collect(Collectors.toMap(TagDescription::key, TagDescription::value));
  }

  /**
   * Adds provided map of tags to the given instance
   *
   * @param instanceId the EC2 instance ID
   * @param tags the Map of tags to add
   */
  public void addTags(String instanceId, Map<String, String> tags) {
    List<Tag> ec2Tags =
        tags.entrySet().stream()
            .map(e -> Tag.builder().key(e.getKey()).value(e.getValue()).build())
            .collect(Collectors.toList());
    amazonEC2Client.createTags(
        CreateTagsRequest.builder().resources(instanceId).tags(ec2Tags).build());
  }

  /**
   * Gets a list of EC2 instance IDs for a given auto scaling group name
   *
   * @param groupName auto scaling group name
   * @return List of strings containing instance IDs
   */
  public List<String> getInstanceIdsForAutoScalingGroup(String groupName) {
    List<Instance> instanceList = getAutoScalingGroup(groupName).instances();
    return instanceList.stream().map(Instance::instanceId).collect(Collectors.toList());
  }

  /**
   * Gets a list of EC2 Instance objects for a given auto scaling group name
   *
   * @param groupName auto scaling group name
   * @return List of Instances containing instance IDs and availability zones
   */
  public List<EC2Instance> getInstancesForAutoScalingGroup(String groupName) {
    List<Instance> instanceList = getAutoScalingGroup(groupName).instances();

    return instanceList.stream()
        .map(
            i ->
                new EC2Instance()
                    .withInstanceId(i.instanceId())
                    .withAvailabilityZone(i.availabilityZone()))
        .collect(Collectors.toList());
  }

  /**
   * Gets the auto scaling group's desired capacity for a given group name
   *
   * @param groupName auto scaling group name
   * @return int the desired capacity of the group
   */
  public int getAutoScalingGroupDesiredCapacity(String groupName) {
    return getAutoScalingGroup(groupName).desiredCapacity();
  }

  /**
   * Sets the auto scaling desired capacity for a given group name
   *
   * @param groupName auto scaling group name
   * @param desiredCapacity the desired capacity of the group to set
   */
  public void setAutoScalingGroupDesiredCapacity(String groupName, int desiredCapacity) {
    SetDesiredCapacityRequest request =
        SetDesiredCapacityRequest.builder()
            .autoScalingGroupName(groupName)
            .desiredCapacity(desiredCapacity)
            .build();
    amazonAutoScalingClient.setDesiredCapacity(request);
  }

  /**
   * Gets the volume id of a given instance and device name
   *
   * @param instanceId the EC2 instance ID
   * @param deviceName the block device mapping name
   * @return Volume Id of the EBS block device
   */
  public String getVolumeId(String instanceId, String deviceName) {
    DescribeInstanceAttributeResponse result =
        amazonEC2Client.describeInstanceAttribute(
            DescribeInstanceAttributeRequest.builder()
                .instanceId(instanceId)
                .attribute(InstanceAttributeName.BLOCK_DEVICE_MAPPING)
                .build());

    List<InstanceBlockDeviceMapping> instanceBlockDeviceMappings = result.blockDeviceMappings();

    EbsInstanceBlockDevice ebsInstanceBlockDevice =
        instanceBlockDeviceMappings.stream()
            .filter(m -> m.deviceName().equals(deviceName))
            .findFirst()
            .orElseThrow()
            .ebs();

    return ebsInstanceBlockDevice.volumeId();
  }

  /**
   * Creates a snapshot for a given volume
   *
   * @param volumeId identifies the volume to snapshot
   * @param description of the new snap shot
   * @return Snapshot ID of the newly created snapshot
   */
  public String createSnapshot(String volumeId, String description) {
    CreateSnapshotResponse result =
        amazonEC2Client.createSnapshot(
            CreateSnapshotRequest.builder().volumeId(volumeId).description(description).build());
    return result.snapshotId();
  }

  /**
   * Gets a physical resource ID on a given stack for a logical resource ID
   *
   * @param stackName the name or the unique stack ID of the cloud formation stack
   * @param logicalResourceId the logical name of the stack resource
   * @return Physical resource ID
   */
  public String getStackPhysicalResourceId(String stackName, String logicalResourceId) {
    // describeStackResources takes either name or stack ID
    // See:
    // https://docs.aws.amazon.com/AWSCloudFormation/latest/APIReference/API_DescribeStackResources.html
    DescribeStackResourcesResponse result =
        amazonCloudFormationClient.describeStackResources(
            DescribeStackResourcesRequest.builder().stackName(stackName).build());

    return result.stackResources().stream()
        .filter(s -> s.logicalResourceId().equals(logicalResourceId))
        .findFirst()
        .orElseThrow()
        .physicalResourceId();
  }

  /**
   * Reads a file from S3 into a String object
   *
   * @param s3Uri (eg. s3://bucket/file.ext)
   * @return String containing the content of the file in S3
   * @throws IOException if error reading file
   */
  public String readFileFromS3(String s3Uri) throws IOException {
    S3Uri s3FileUri = amazonS3Client.utilities().parseUri(URI.create(s3Uri));
    String bucket =
        s3FileUri
            .bucket()
            .orElseThrow(() -> new IllegalArgumentException("Invalid S3 bucket in URI: " + s3Uri));
    String key =
        s3FileUri
            .key()
            .orElseThrow(() -> new IllegalArgumentException("Invalid S3 key in URI: " + s3Uri));

    GetObjectRequest getObjectRequest = GetObjectRequest.builder().bucket(bucket).key(key).build();

    try (ResponseInputStream<GetObjectResponse> s3object =
        amazonS3Client.getObject(getObjectRequest)) {
      return IoUtils.toUtf8String(s3object);
    }
  }

  /**
   * Creates a CloudWatch content health check alarm The metric for the alarm is always
   * 'contentHealthCheck'
   *
   * @param alarmName recommend including the instance id
   * @param alarmDescription a brief description of the alarm
   * @param publishInstanceId ID of the publish instance
   * @param namespace this will be the stack name (i.e. xxxx-aem-publish-dispatcher-stack)
   * @param topicArn the ARN of the SNS topic that is used when the alarm triggers
   */
  public void createContentHealthCheckAlarm(
      String alarmName,
      String alarmDescription,
      String publishInstanceId,
      String namespace,
      String topicArn) {
    amazonCloudWatchClient.putMetricAlarm(
        PutMetricAlarmRequest.builder()
            .alarmName(alarmName)
            .alarmDescription(alarmDescription)
            .dimensions(
                Dimension.builder()
                    .name(InstanceTags.PAIR_INSTANCE_ID.getTagName())
                    .value(publishInstanceId)
                    .build())
            .metricName("contentHealthCheck")
            .namespace(namespace)
            .period(60)
            .threshold(1D)
            .evaluationPeriods(5)
            .statistic(Statistic.MAXIMUM)
            .comparisonOperator(ComparisonOperator.LESS_THAN_THRESHOLD)
            .alarmActions(topicArn)
            .actionsEnabled(true)
            .build());
  }

  /**
   * Delete a CloudWatch alarm for a given alarm name
   *
   * @param alarmName the name of the alarm
   */
  public void deleteAlarm(String alarmName) {
    amazonCloudWatchClient.deleteAlarms(
        DeleteAlarmsRequest.builder().alarmNames(alarmName).build());
  }

  private AutoScalingGroup getAutoScalingGroup(String groupName) {
    DescribeAutoScalingGroupsResponse result =
        amazonAutoScalingClient.describeAutoScalingGroups(
            DescribeAutoScalingGroupsRequest.builder().autoScalingGroupNames(groupName).build());
    return result.autoScalingGroups().get(0);
  }
}
