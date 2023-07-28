package com.shinesolutions.aemorchestrator.config;

import com.shinesolutions.aemorchestrator.model.AemCredentials;
import com.shinesolutions.aemorchestrator.model.EnvironmentValues;
import com.shinesolutions.aemorchestrator.model.UserPasswordCredentials;
import com.shinesolutions.aemorchestrator.service.AwsHelperService;
import com.shinesolutions.aemorchestrator.util.CredentialsExtractor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;

@Configuration
@Profile("default")
public class AemConfig {

    private final Logger logger = LoggerFactory.getLogger(this.getClass());

    @Value("${aem.credentials.replicator.username}")
    private String replicatorUsername;

    @Value("${aem.credentials.replicator.password}")
    private String replicatorPassword;

    @Value("${aem.credentials.orchestrator.username}")
    private String orchestratorUsername;

    @Value("${aem.credentials.orchestrator.password}")
    private String orchestratorPassword;

    @Value("${aem.credentials.s3.use}")
    private Boolean readCredentialsFromS3;

    @Value("${aem.credentials.s3.file.uri:@null}")
    private String s3CredentialFileUri;

    @Value("${aws.cloudformation.stackName.publishDispatcher}")
    private String awsPublishDispatcherStackName;

    @Value("${aws.cloudformation.stackName.publish}")
    private String awsPublishStackName;

    @Value("${aws.cloudformation.stackName.previewPublishDispatcher}")
    private String awsPreviewPublishDispatcherStackName;

    @Value("${aws.cloudformation.stackName.previewPublish}")
    private String awsPreviewPublishStackName;

    @Value("${aws.cloudformation.stackName.authorDispatcher}")
    private String awsAuthorDispatcherStackName;

    @Value("${aws.cloudformation.stackName.messaging}")
    private String awsMessagingStackName;

    @Value("${aws.cloudformation.stackName.author}")
    private String awsAuthorStackName;

    @Value("${aws.cloudformation.autoScaleGroup.logicalId.publishDispatcher}")
    private String awsPublishDispatcherAutoScaleGroupLogicalId;

    @Value("${aws.cloudformation.autoScaleGroup.logicalId.publish}")
    private String awsPublishAutoScaleGroupLogicalId;

    @Value("${aws.cloudformation.autoScaleGroup.logicalId.previewPublishDispatcher}")
    private String awsPreviewPublishDispatcherAutoScaleGroupLogicalId;

    @Value("${aws.cloudformation.autoScaleGroup.logicalId.previewPublish}")
    private String awsPreviewPublishAutoScaleGroupLogicalId;

    @Value("${aws.cloudformation.autoScaleGroup.logicalId.authorDispatcher}")
    private String awsAuthorDispatcherAutoScaleGroupLogicalId;

    @Value("${aws.cloudformation.loadBalancer.logicalId.author}")
    private String awsAuthorLoadBalancerLogicalId;

    @Value("${aws.cloudformation.sns.logicalId.eventTopic}")
    private String awsSnsTopicLogicalId;

    @Bean
    public AemCredentials aemCredentials(final AwsHelperService awsHelper) throws Exception {
        AemCredentials aemCredentials;

        //If storing credentials in S3
        if(readCredentialsFromS3) {
            //Retrieve and read JSON formatted file from given S3 URI
            logger.debug("Reading AEM credentials from S3 bucket");

            try {
                String s3CredentialsFileContent = awsHelper.readFileFromS3(s3CredentialFileUri);

                aemCredentials = CredentialsExtractor.extractAemCredentials(s3CredentialsFileContent);

            } catch (Exception e) {
                logger.error("Failed to read AEM credentials file from S3 location: " + s3CredentialFileUri, e);
                throw e;
            }

        } else {
            logger.debug("Reading AEM credentials from application properties file");
            aemCredentials = new AemCredentials();
            aemCredentials.setOrchestratorCredentials(
                new UserPasswordCredentials().withUserName(orchestratorUsername).withPassword(orchestratorPassword));
            aemCredentials.setReplicatorCredentials(
                new UserPasswordCredentials().withUserName(replicatorUsername).withPassword(replicatorPassword));
        }

        return aemCredentials;
    }

    @Bean
    public EnvironmentValues envValues(final AwsHelperService awsHelper) {
        EnvironmentValues envValues = new EnvironmentValues();

        envValues.setAutoScaleGroupNameForPublishDispatcher(
            awsHelper.getStackPhysicalResourceId(awsPublishDispatcherStackName, awsPublishDispatcherAutoScaleGroupLogicalId));
        logger.debug("Resolved auto scaling group name for publish dispatcher to: " +
            envValues.getAutoScaleGroupNameForPublishDispatcher());

        if (awsPreviewPublishDispatcherStackName != null && !awsPreviewPublishDispatcherStackName.equals("")) {
            envValues.setAutoScaleGroupNameForPreviewPublishDispatcher(
                awsHelper.getStackPhysicalResourceId(awsPreviewPublishDispatcherStackName, awsPreviewPublishDispatcherAutoScaleGroupLogicalId)
            );
            
                logger.debug("Resolved auto scaling group name for previewPublish dispatcher to: " +
                envValues.getAutoScaleGroupNameForPreviewPublishDispatcher());

            envValues.setAutoScaleGroupNameForPreviewPublish(
                awsHelper.getStackPhysicalResourceId(awsPreviewPublishStackName, awsPreviewPublishAutoScaleGroupLogicalId));

            logger.debug("Resolved auto scaling group name for previewPublish to: " +
                envValues.getAutoScaleGroupNameForPreviewPublish());
        }
        
        envValues.setAutoScaleGroupNameForPublish(
            awsHelper.getStackPhysicalResourceId(awsPublishStackName, awsPublishAutoScaleGroupLogicalId));

        logger.debug("Resolved auto scaling group name for publish to: " +
            envValues.getAutoScaleGroupNameForPublish());

        envValues.setAutoScaleGroupNameForAuthorDispatcher(
            awsHelper.getStackPhysicalResourceId(awsAuthorDispatcherStackName, awsAuthorDispatcherAutoScaleGroupLogicalId));

        logger.debug("Resolved auto scaling group name for author dispatcher to: " +
            envValues.getAutoScaleGroupNameForAuthorDispatcher());

        envValues.setElasticLoadBalancerNameForAuthor(awsHelper.getElbName(
            awsHelper.getStackPhysicalResourceId(awsAuthorStackName, awsAuthorLoadBalancerLogicalId)));

        logger.debug("Resolved elastic load balancer name for author to: " +
            envValues.getElasticLoadBalancerNameForAuthor());

        envValues.setElasticLoadBalancerAuthorDns(awsHelper.getElbDnsName(
            envValues.getElasticLoadBalancerNameForAuthor()));

        logger.debug("Resolved elastic load balancer DNS for author to: " +
            envValues.getElasticLoadBalancerAuthorDns());

        envValues.setTopicArn(awsHelper.getStackPhysicalResourceId(awsMessagingStackName, awsSnsTopicLogicalId));

        logger.debug("Resolved SNS topic ARN to: " + envValues.getTopicArn());

        return envValues;
    }

}
