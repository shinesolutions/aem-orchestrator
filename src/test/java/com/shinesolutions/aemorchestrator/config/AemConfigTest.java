package com.shinesolutions.aemorchestrator.config;

import com.shinesolutions.aemorchestrator.model.AemCredentials;
import com.shinesolutions.aemorchestrator.model.EnvironmentValues;
import com.shinesolutions.aemorchestrator.service.AwsHelperService;
import org.junit.Before;
import org.junit.Test;

import java.io.IOException;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static org.springframework.test.util.ReflectionTestUtils.setField;

public class AemConfigTest {

    private AemConfig aemConfig;

    @Before
    public void setup() {
        aemConfig = new AemConfig();
    }

    @Test
    public void testAemCredentials_ReadFromPropertiesFile() throws Exception {
        String orchestratorUsername = "orchestratorUsername";
        String orchestratorPassword = "orchestratorPassword";
        String replicatorUsername = "replicatorUsername";
        String replicatorPassword = "replicatorPassword";

        setField(aemConfig, "readCredentialsFromS3", false);
        setField(aemConfig, "orchestratorUsername", orchestratorUsername);
        setField(aemConfig, "orchestratorPassword", orchestratorPassword);
        setField(aemConfig, "replicatorUsername", replicatorUsername);
        setField(aemConfig, "replicatorPassword", replicatorPassword);

        AemCredentials aemCredentials = aemConfig.aemCredentials(null);

        assertThat(aemCredentials.getOrchestratorCredentials().getUserName(), equalTo(orchestratorUsername));
        assertThat(aemCredentials.getOrchestratorCredentials().getPassword(), equalTo(orchestratorPassword));
        assertThat(aemCredentials.getReplicatorCredentials().getUserName(), equalTo(replicatorUsername));
        assertThat(aemCredentials.getReplicatorCredentials().getPassword(), equalTo(replicatorPassword));
    }

    @Test(expected = IOException.class)
    public void testAemCredentials_ReadingThrowsException() throws Exception {
        String s3CredentialFileUri = "s3CredentialFileUri";
        setField(aemConfig, "s3CredentialFileUri", s3CredentialFileUri);
        setField(aemConfig, "readCredentialsFromS3", true);

        AwsHelperService awsHelperService = mock(AwsHelperService.class);
        when(awsHelperService.readFileFromS3(s3CredentialFileUri)).thenThrow(new IOException());
        aemConfig.aemCredentials(awsHelperService);
    }

    @Test
    public void testEnvValue() {
        AwsHelperService awsHelperService = mock(AwsHelperService.class);

        // Set autoScaleGroupNameForPublishDispatcher
        String awsPublishDispatcherStackName = "awsPublishDispatcherStackName";
        String awsPublishDispatcherAutoScaleGroupLogicalId = "awsPublishDispatcherAutoScaleGroupLogicalId";
        String autoScaleGroupNameForPublishDispatcher = "autoScaleGroupNameForPublishDispatcher";
        setField(aemConfig, "awsPublishDispatcherStackName", awsPublishDispatcherStackName);
        setField(aemConfig, "awsPublishDispatcherAutoScaleGroupLogicalId", awsPublishDispatcherAutoScaleGroupLogicalId);
        when(awsHelperService.getStackPhysicalResourceId(
                awsPublishDispatcherStackName,
                awsPublishDispatcherAutoScaleGroupLogicalId))
                .thenReturn(autoScaleGroupNameForPublishDispatcher);

        // Set autoScaleGroupNameForPublish
        String awsPublishStackName = "awsPublishStackName";
        String awsPublishAutoScaleGroupLogicalId = "awsPublishAutoScaleGroupLogicalId";
        String autoScaleGroupNameForPublish = "autoScaleGroupNameForPublish";
        setField(aemConfig, "awsPublishStackName", awsPublishStackName);
        setField(aemConfig, "awsPublishAutoScaleGroupLogicalId", awsPublishAutoScaleGroupLogicalId);
        when(awsHelperService.getStackPhysicalResourceId(
                awsPublishStackName,
                awsPublishAutoScaleGroupLogicalId))
                .thenReturn(autoScaleGroupNameForPublish);

        // Set autoScaleGroupNameForPreviewPublishDispatcher
        String awsPreviewPublishDispatcherStackName = "awsPreviewPublishDispatcherStackName";
        String awsPreviewPublishDispatcherAutoScaleGroupLogicalId = "awsPreviewPublishDispatcherAutoScaleGroupLogicalId";
        String autoScaleGroupNameForPreviewPublishDispatcher = "autoScaleGroupNameForPreviewPublishDispatcher";
        setField(aemConfig, "awsPreviewPublishDispatcherStackName", awsPreviewPublishDispatcherStackName);
        setField(aemConfig, "awsPreviewPublishDispatcherAutoScaleGroupLogicalId", awsPreviewPublishDispatcherAutoScaleGroupLogicalId);
        when(awsHelperService.getStackPhysicalResourceId(
                awsPreviewPublishDispatcherStackName,
                awsPreviewPublishDispatcherAutoScaleGroupLogicalId))
                .thenReturn(autoScaleGroupNameForPreviewPublishDispatcher);

        // Set autoScaleGroupNameForPreviewPublish
        String awsPreviewPublishStackName = "awsPreviewPublishStackName";
        String awsPreviewPublishAutoScaleGroupLogicalId = "awsPreviewPublishAutoScaleGroupLogicalId";
        String autoScaleGroupNameForPreviewPublish = "autoScaleGroupNameForPreviewPublish";
        setField(aemConfig, "awsPreviewPublishStackName", awsPreviewPublishStackName);
        setField(aemConfig, "awsPreviewPublishAutoScaleGroupLogicalId", awsPreviewPublishAutoScaleGroupLogicalId);
        when(awsHelperService.getStackPhysicalResourceId(
                awsPreviewPublishStackName,
                awsPreviewPublishAutoScaleGroupLogicalId))
                .thenReturn(autoScaleGroupNameForPreviewPublish);

        // Set autoScaleGroupNameForAuthorDispatcher
        String awsAuthorDispatcherStackName = "awsAuthorDispatcherStackName";
        String awsAuthorDispatcherAutoScaleGroupLogicalId = "awsAuthorDispatcherAutoScaleGroupLogicalId";
        String autoScaleGroupNameForAuthorDispatcher = "autoScaleGroupNameForAuthorDispatcher";
        setField(aemConfig, "awsAuthorDispatcherStackName", awsAuthorDispatcherStackName);
        setField(aemConfig, "awsAuthorDispatcherAutoScaleGroupLogicalId", awsAuthorDispatcherAutoScaleGroupLogicalId);
        when(awsHelperService.getStackPhysicalResourceId(
                awsAuthorDispatcherStackName,
                awsAuthorDispatcherAutoScaleGroupLogicalId))
                .thenReturn(autoScaleGroupNameForAuthorDispatcher);
        
        // Set elasticLoadBalancerNameForAuthor
        String awsAuthorStackName = "awsAuthorStackName";
        String awsAuthorLoadBalancerLogicalId = "awsAuthorLoadBalancerLogicalId";
        String elasticLoadBalancerNameForAuthor = "elasticLoadBalancerNameForAuthor";
        String elasticLoadBalancerArnForAuthor = "arn:aws:elasticloadbalancing:ap-southeast-2:918473058104:loadbalancer/app/bloch-Autho-1GWRY37R2O1GQ/809dd96ac8ee4083";
        setField(aemConfig, "awsAuthorStackName", awsAuthorStackName);
        setField(aemConfig, "awsAuthorLoadBalancerLogicalId", awsAuthorLoadBalancerLogicalId);
        when(awsHelperService.getElbName(
          awsHelperService.getStackPhysicalResourceId(
                  awsAuthorStackName,
                  awsAuthorLoadBalancerLogicalId
                  ))).thenReturn(elasticLoadBalancerNameForAuthor);

        // Set elasticLoadBalancerAuthorDns
        String elasticLoadBalancerAuthorDns = "elasticLoadBalancerAuthorDns";
        when(awsHelperService.getElbDnsName(elasticLoadBalancerNameForAuthor)).thenReturn(elasticLoadBalancerAuthorDns);

        // Set topicArn
        String awsMessagingStackName = "awsMessagingStackName";
        String awsSnsTopicLogicalId = "awsSnsTopicLogicalId";
        String topicArn = "topicArn";
        setField(aemConfig, "awsMessagingStackName", awsMessagingStackName);
        setField(aemConfig, "awsSnsTopicLogicalId", awsSnsTopicLogicalId);
        when(awsHelperService.getStackPhysicalResourceId(awsMessagingStackName, awsSnsTopicLogicalId)).thenReturn(topicArn);

        EnvironmentValues envValues = aemConfig.envValues(awsHelperService);

        assertThat(envValues.getAutoScaleGroupNameForPublishDispatcher(), equalTo(autoScaleGroupNameForPublishDispatcher));
        assertThat(envValues.getAutoScaleGroupNameForPublish(), equalTo(autoScaleGroupNameForPublish));
        assertThat(envValues.getAutoScaleGroupNameForPreviewPublishDispatcher(), equalTo(autoScaleGroupNameForPreviewPublishDispatcher));
        assertThat(envValues.getAutoScaleGroupNameForPreviewPublish(), equalTo(autoScaleGroupNameForPreviewPublish));
        assertThat(envValues.getAutoScaleGroupNameForAuthorDispatcher(), equalTo(autoScaleGroupNameForAuthorDispatcher));
        assertThat(envValues.getElasticLoadBalancerNameForAuthor(), equalTo(elasticLoadBalancerNameForAuthor));
        assertThat(envValues.getElasticLoadBalancerAuthorDns(), equalTo(elasticLoadBalancerAuthorDns));
        assertThat(envValues.getTopicArn(), equalTo(topicArn));
    }
}
