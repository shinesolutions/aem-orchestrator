package com.shinesolutions.aemorchestrator.service;

import com.shinesolutions.aemorchestrator.exception.InstanceNotInHealthyStateException;
import com.shinesolutions.aemorchestrator.exception.NoPairFoundException;
import com.shinesolutions.aemorchestrator.model.EC2Instance;
import com.shinesolutions.aemorchestrator.model.EnvironmentValues;
import com.shinesolutions.aemorchestrator.model.InstanceTags;
import com.shinesolutions.aemorchestrator.util.HttpUtil;
import org.joda.time.DateTime;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

import java.util.*;

import static com.shinesolutions.aemorchestrator.model.InstanceTags.*;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.startsWith;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.is;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;
import static org.springframework.test.util.ReflectionTestUtils.setField;

@RunWith(MockitoJUnitRunner.class)
public class AemInstanceHelperServiceTest {
    
    private String aemPublishDispatcherProtocol;
    private String aemPublishProtocol;
    private String aemAuthorDispatcherProtocol;
    private String aemAuthorProtocol;
    private String awsPublishDispatcherStackName;
    private Integer aemPublishDispatcherPort;
    private Integer aemPublishPort;
    private Integer aemAuthorDispatcherPort;
    private Integer aemAuthorPort;
    
    @Mock
    private AwsHelperService awsHelperService;
    
    @Mock
    private HttpUtil httpUtil;
    
    @InjectMocks
    private AemInstanceHelperService aemHelperService;
    
    @Captor
    private ArgumentCaptor<Map<String, String>> mapCaptor;
    
    
    private EnvironmentValues envValues;
    
    private String instanceId;
    private String privateIp;
    
    @Before
    public void setUp() {
        instanceId = "test-123456789";
        privateIp = "11.22.33.44";
        
        envValues = new EnvironmentValues();
        envValues.setAutoScaleGroupNameForPublishDispatcher("publishDispatcherTestName");
        envValues.setAutoScaleGroupNameForPublish("publishTestName");
        envValues.setAutoScaleGroupNameForAuthorDispatcher("authorTestName");
        envValues.setElasticLoadBalancerNameForAuthor("elasticLoadBalancerNameForAuthor");
        envValues.setElasticLoadBalancerAuthorDns("elasticLoadBalancerAuthorDns");
        envValues.setTopicArn("topicArn");
        
        aemPublishDispatcherProtocol = "pdpd";
        aemPublishProtocol = "pppp";
        aemAuthorDispatcherProtocol = "adad";
        aemAuthorProtocol = "aaaa";
        awsPublishDispatcherStackName = "awsPublishDispatcherStackName";
        aemPublishDispatcherPort = 1111;
        aemPublishPort = 2222;
        aemAuthorDispatcherPort = 3333;
        aemAuthorPort = 4444;
        
        setField(aemHelperService, "envValues", envValues);
        
        setField(aemHelperService, "aemPublishDispatcherProtocol", aemPublishDispatcherProtocol);
        setField(aemHelperService, "aemPublishProtocol", aemPublishProtocol);
        setField(aemHelperService, "aemAuthorDispatcherProtocol", aemAuthorDispatcherProtocol);
        setField(aemHelperService, "aemAuthorProtocol", aemAuthorProtocol);
        setField(aemHelperService, "awsPublishDispatcherStackName", awsPublishDispatcherStackName);
        
        setField(aemHelperService, "aemPublishDispatcherPort", aemPublishDispatcherPort);
        setField(aemHelperService, "aemPublishPort", aemPublishPort);
        setField(aemHelperService, "aemAuthorDispatcherPort", aemAuthorDispatcherPort);
        setField(aemHelperService, "aemAuthorPort", aemAuthorPort);
    }

    @Test
    public void testGetAemUrlForPublishDispatcher() {
        when(awsHelperService.getPrivateIp(instanceId)).thenReturn(privateIp);
        
        String aemUrl = aemHelperService.getAemUrlForPublishDispatcher(instanceId);
        
        assertThat(aemUrl, equalTo(aemPublishDispatcherProtocol + "://" + privateIp + ":" + aemPublishDispatcherPort));
    }
    
    @Test
    public void testGetAemUrlForPublish() {
        when(awsHelperService.getPrivateIp(instanceId)).thenReturn(privateIp);
        
        String aemUrl = aemHelperService.getAemUrlForPublish(instanceId);
        
        assertThat(aemUrl, equalTo(aemPublishProtocol + "://" + privateIp + ":" + aemPublishPort));
    }
    
    @Test
    public void testGetAemUrlForAuthorElb() {
        String aemUrl = aemHelperService.getAemUrlForAuthorElb();
        
        assertThat(aemUrl, equalTo(aemAuthorProtocol + "://" + 
            envValues.getElasticLoadBalancerAuthorDns() + ":" + aemAuthorPort));
    }
    
    @Test
    public void testGetAemUrlForAuthorDispatcher() {
        when(awsHelperService.getPrivateIp(instanceId)).thenReturn(privateIp);
        
        String aemUrl = aemHelperService.getAemUrlForAuthorDispatcher(instanceId);
        
        assertThat(aemUrl, equalTo(aemAuthorDispatcherProtocol + "://" + privateIp + ":" + aemAuthorDispatcherPort));
    }

    @Test
    public void testGetAemComponentInitStateOK() {

      Map<String, String> tagsComponentInitStatusSuccess = new HashMap<>();
      tagsComponentInitStatusSuccess.put(COMPONENT_INIT_STATUS.getTagName(), "Success");

      when(awsHelperService.getTags(instanceId)).thenReturn(tagsComponentInitStatusSuccess);

      boolean result = aemHelperService.getAemComponentInitState(instanceId);
      assertThat(result, equalTo(true));
    }

    @Test
    public void testGetAemComponentInitStateNotOK() {
      Map<String, String> tagsComponentInitStatusFailed = new HashMap<>();
      tagsComponentInitStatusFailed.put(COMPONENT_INIT_STATUS.getTagName(), "Failed");

      when(awsHelperService.getTags(instanceId)).thenReturn(tagsComponentInitStatusFailed);

      boolean result = aemHelperService.getAemComponentInitState(instanceId);
      assertThat(result, equalTo(false));
    }

    @Test
    public void testGetPublishIdToSnapshotFrom() throws Exception {
        String excludeInstanceId = "exclude-352768";
        List<String> instanceIds = new ArrayList<>();
        instanceIds.add(excludeInstanceId);
        instanceIds.add(instanceId);
        instanceIds.add("extra-89351");

        Date dt = new Date();

        DateTime originalDateTime = new DateTime(dt);
        Date originalDate = originalDateTime.toDate();
        DateTime originalPlusOneDateTime = originalDateTime.plusDays(1);
        Date originalPlusOneDate = originalPlusOneDateTime.toDate();

        when(awsHelperService.getInstanceIdsForAutoScalingGroup(
            envValues.getAutoScaleGroupNameForPublish())).thenReturn(instanceIds);

        Map<String, String> instanceTags1 = new HashMap<>();
        instanceTags1.put(InstanceTags.SNAPSHOT_ID.getTagName(), "");

        when(awsHelperService.getTags(anyString())).thenReturn(instanceTags1);

        when(awsHelperService.getLaunchTime(instanceId)).thenReturn(originalDate);
        when(awsHelperService.getLaunchTime("extra-89351")).thenReturn(originalPlusOneDate);

        String resultInstanceId = aemHelperService.getPublishIdToSnapshotFrom(excludeInstanceId);
        
        assertThat(resultInstanceId, equalTo(instanceId));
    }

    @Test
    public void testGetPublishIdToSnapshotFromWithSameLaunchTime() throws Exception {
        final String alphabeticallyFirst = "AAA";
        final String alphabeticallySecond = "BBB";
        List<String> instanceIds = new ArrayList<>();
        instanceIds.add(alphabeticallySecond);
        instanceIds.add(alphabeticallyFirst);

        when(awsHelperService.getInstanceIdsForAutoScalingGroup(
                envValues.getAutoScaleGroupNameForPublish())).thenReturn(instanceIds);

        Map<String, String> instanceTags1 = new HashMap<>();
        instanceTags1.put(InstanceTags.SNAPSHOT_ID.getTagName(), "");

        when(awsHelperService.getTags(anyString())).thenReturn(instanceTags1);

        Date originalDate = new Date();
        when(awsHelperService.getLaunchTime(alphabeticallyFirst)).thenReturn(originalDate);
        when(awsHelperService.getLaunchTime(alphabeticallySecond)).thenReturn(originalDate);

        String resultInstanceId = aemHelperService.getPublishIdToSnapshotFrom("exclude-352768");

        assertThat(resultInstanceId, equalTo(alphabeticallyFirst));
    }
    
    @Test
    public void testGetPublishIdToSnapshotFromWithOnlyExcludedInstance() {
        String excludeInstanceId = "exclude-352768";
        List<String> instanceIds = new ArrayList<>();
        instanceIds.add(excludeInstanceId);

        when(awsHelperService.getInstanceIdsForAutoScalingGroup(
                envValues.getAutoScaleGroupNameForPublish())).thenReturn(instanceIds);

        String resultInstanceId = aemHelperService.getPublishIdToSnapshotFrom(excludeInstanceId);

        assertThat(resultInstanceId, equalTo(null));
    }

    @Test
    public void testGetPublishIdToSnapshotFromWithNoTagName() {
        String excludeInstanceId = "exclude-352768";
        List<String> instanceIds = new ArrayList<>();
        instanceIds.add(instanceId);

        when(awsHelperService.getInstanceIdsForAutoScalingGroup(
                envValues.getAutoScaleGroupNameForPublish())).thenReturn(instanceIds);

        when(awsHelperService.getTags(anyString())).thenReturn(new HashMap<>());

        String resultInstanceId = aemHelperService.getPublishIdToSnapshotFrom(excludeInstanceId);

        assertThat(resultInstanceId, equalTo(null));
    }
    
    @Test
    public void testGetPublishIdToSnapshotFromWithNoInstances() {
        when(awsHelperService.getInstanceIdsForAutoScalingGroup(
            envValues.getAutoScaleGroupNameForPublish())).thenReturn(new ArrayList<>());
        
        String resultInstanceId = aemHelperService.getPublishIdToSnapshotFrom("s-2397106");
        
        assertThat(resultInstanceId, equalTo(null));
    }
    
    @Test
    public void testIsFirstPublishInstanceNoSnapshotTags() {
        Map<String, String> instanceTags = new HashMap<>();
        
        List<String> instanceIds = new ArrayList<>();
        instanceIds.add("i-1");
        instanceIds.add("i-2");
        
        when(awsHelperService.getInstanceIdsForAutoScalingGroup(
            envValues.getAutoScaleGroupNameForPublish())).thenReturn(instanceIds);
        
        when(awsHelperService.getTags(anyString())).thenReturn(instanceTags);
        
        boolean result = aemHelperService.isFirstPublishInstance();
        
        assertThat(result, equalTo(true));
    }
    
    @Test
    @SuppressWarnings("unchecked")
    public void testIsFirstPublishInstanceOneSnapshotTag() {
        Map<String, String> instanceTags1 = new HashMap<>();
        Map<String, String> instanceTags2 = new HashMap<>();
        instanceTags2.put(InstanceTags.SNAPSHOT_ID.getTagName(), "");
        
        List<String> instanceIds = new ArrayList<>();
        instanceIds.add("i-1");
        instanceIds.add("i-2");
        
        when(awsHelperService.getInstanceIdsForAutoScalingGroup(
            envValues.getAutoScaleGroupNameForPublish())).thenReturn(instanceIds);
        
        when(awsHelperService.getTags(anyString())).thenReturn(instanceTags1, instanceTags2);
        
        boolean result = aemHelperService.isFirstPublishInstance();
        
        assertThat(result, equalTo(false));
    }
    
    @Test
    public void testIsFirstPublishInstanceWithNoInstances() {
        List<String> instanceIds = new ArrayList<>();
        
        when(awsHelperService.getInstanceIdsForAutoScalingGroup(
            envValues.getAutoScaleGroupNameForPublish())).thenReturn(instanceIds);
        
        boolean result = aemHelperService.isFirstPublishInstance();
        
        assertThat(result, equalTo(true));
    }
    
    @Test
    public void testFindUnpairedPublishDispatcher() throws Exception {
        String publishAZ = "A";
        EC2Instance instance1 = new EC2Instance().withInstanceId("1st-324981").withAvailabilityZone(publishAZ);
        EC2Instance instance2 = new EC2Instance().withInstanceId("2nd-111982").withAvailabilityZone(publishAZ);
        EC2Instance instance3 = new EC2Instance().withInstanceId("3rd-222983").withAvailabilityZone(publishAZ);
        
        Map<String, String> tagsWithPairName = new HashMap<>();
        tagsWithPairName.put(PAIR_INSTANCE_ID.getTagName(), "testPair");
        
        Map<String, String> tagsWithoutPairName = new HashMap<>();
        
        List<EC2Instance> instanceIds = new ArrayList<>();
        instanceIds.add(instance1);
        instanceIds.add(instance2);
        instanceIds.add(instance3);
        
        when(awsHelperService.getInstancesForAutoScalingGroup(
            envValues.getAutoScaleGroupNameForPublishDispatcher())).thenReturn(instanceIds);

        when(awsHelperService.getTags(instance1.getInstanceId())).thenReturn(tagsWithPairName);
        when(awsHelperService.getTags(instance2.getInstanceId())).thenReturn(tagsWithoutPairName); //Instance 2 is the winner
        when(awsHelperService.getTags(instance3.getInstanceId())).thenReturn(tagsWithPairName);
        
        String resultInstanceId = aemHelperService.findUnpairedPublishDispatcher(instanceId);
        
        assertThat(resultInstanceId, equalTo(instance2.getInstanceId()));
    }
    
    @Test
    public void testFindUnpairedPublishDispatcherDiffAvailablityZone() throws Exception {
        String publishAZ = "A";
        EC2Instance instance1 = new EC2Instance().withInstanceId("1st-324981").withAvailabilityZone(publishAZ);
        EC2Instance instance2 = new EC2Instance().withInstanceId("2nd-111982").withAvailabilityZone("B"); //Diff AZ
        EC2Instance instance3 = new EC2Instance().withInstanceId("3rd-222983").withAvailabilityZone(publishAZ);
        
        Map<String, String> tagsWithPairName = new HashMap<>();
        tagsWithPairName.put(PAIR_INSTANCE_ID.getTagName(), "testPair");
        
        Map<String, String> tagsWithoutPairName = new HashMap<>();
        
        List<EC2Instance> instanceIds = new ArrayList<>();
        instanceIds.add(instance1);
        instanceIds.add(instance2);
        instanceIds.add(instance3);
        
        when(awsHelperService.getInstancesForAutoScalingGroup(
            envValues.getAutoScaleGroupNameForPublishDispatcher())).thenReturn(instanceIds);
        when(awsHelperService.getAvailabilityZone(instanceId)).thenReturn(publishAZ);
        when(awsHelperService.getTags(instance1.getInstanceId())).thenReturn(tagsWithPairName);
        when(awsHelperService.getTags(instance2.getInstanceId())).thenReturn(tagsWithoutPairName); 
        when(awsHelperService.getTags(instance3.getInstanceId())).thenReturn(tagsWithoutPairName);
        
        String resultInstanceId = aemHelperService.findUnpairedPublishDispatcher(instanceId);
        
        //It should pick the one with same AZ
        assertThat(resultInstanceId, equalTo(instance3.getInstanceId()));
    }
    
    @Test
    public void testFindUnpairedPublishDispatcherSameAvailablityZone() throws Exception {
        String publishAZ = "A";
        EC2Instance instance1 = new EC2Instance().withInstanceId("1st-324981").withAvailabilityZone(publishAZ);
        EC2Instance instance2 = new EC2Instance().withInstanceId("2nd-111982").withAvailabilityZone(publishAZ);
        EC2Instance instance3 = new EC2Instance().withInstanceId("3rd-222983").withAvailabilityZone(publishAZ);
        
        Map<String, String> tagsWithPairName = new HashMap<>();
        tagsWithPairName.put(PAIR_INSTANCE_ID.getTagName(), "testPair");
        
        Map<String, String> tagsWithoutPairName = new HashMap<>();
        
        List<EC2Instance> instanceIds = new ArrayList<>();
        instanceIds.add(instance1);
        instanceIds.add(instance2);
        instanceIds.add(instance3);
        
        when(awsHelperService.getInstancesForAutoScalingGroup(
            envValues.getAutoScaleGroupNameForPublishDispatcher())).thenReturn(instanceIds);
        when(awsHelperService.getAvailabilityZone(instanceId)).thenReturn(publishAZ);
        when(awsHelperService.getTags(instance1.getInstanceId())).thenReturn(tagsWithPairName);
        when(awsHelperService.getTags(instance2.getInstanceId())).thenReturn(tagsWithoutPairName); 
        when(awsHelperService.getTags(instance3.getInstanceId())).thenReturn(tagsWithoutPairName);
        
        String resultInstanceId = aemHelperService.findUnpairedPublishDispatcher(instanceId);
        
        //If AZ the same, then it should pick the first one
        assertThat(resultInstanceId, equalTo(instance2.getInstanceId()));
    }
    
    @Test
    public void testFindUnpairedPublishDispatcherNoSameAvailablityZone() throws Exception {
        String publishAZ = "A";
        EC2Instance instance1 = new EC2Instance().withInstanceId("1st-324981").withAvailabilityZone(publishAZ);
        EC2Instance instance2 = new EC2Instance().withInstanceId("2nd-111982").withAvailabilityZone("B");
        EC2Instance instance3 = new EC2Instance().withInstanceId("3rd-222983").withAvailabilityZone("B");
        
        Map<String, String> tagsWithPairName = new HashMap<>();
        tagsWithPairName.put(PAIR_INSTANCE_ID.getTagName(), "testPair");
        
        Map<String, String> tagsWithoutPairName = new HashMap<>();
        
        List<EC2Instance> instanceIds = new ArrayList<>();
        instanceIds.add(instance1);
        instanceIds.add(instance2);
        instanceIds.add(instance3);
        
        when(awsHelperService.getInstancesForAutoScalingGroup(
            envValues.getAutoScaleGroupNameForPublishDispatcher())).thenReturn(instanceIds);
        when(awsHelperService.getAvailabilityZone(instanceId)).thenReturn(publishAZ);
        when(awsHelperService.getTags(instance1.getInstanceId())).thenReturn(tagsWithPairName);
        when(awsHelperService.getTags(instance2.getInstanceId())).thenReturn(tagsWithoutPairName); 
        when(awsHelperService.getTags(instance3.getInstanceId())).thenReturn(tagsWithoutPairName);
        
        String resultInstanceId = aemHelperService.findUnpairedPublishDispatcher(instanceId);
        
        //If all AZ are different, then it should pick the first one
        assertThat(resultInstanceId, equalTo(instance2.getInstanceId()));
    }
    
    @Test(expected=NoPairFoundException.class)
    public void testFindUnpairedPublishFail() throws Exception {
        List<EC2Instance> instanceIds = new ArrayList<>();
        
        when(awsHelperService.getInstancesForAutoScalingGroup(
            envValues.getAutoScaleGroupNameForPublishDispatcher())).thenReturn(instanceIds);

        aemHelperService.findUnpairedPublishDispatcher(instanceId);
    }
    
    @Test
    public void testFindUnpairedPublishDispatcherAlreadyPaired() throws Exception {
        String publishAZ = "A";
        EC2Instance instance1 = new EC2Instance().withInstanceId("1st-324981").withAvailabilityZone(publishAZ);
        EC2Instance instance2 = new EC2Instance().withInstanceId("2nd-111982").withAvailabilityZone(publishAZ);
        
        Map<String, String> tagsWithPairName = new HashMap<>();
        tagsWithPairName.put(PAIR_INSTANCE_ID.getTagName(), "testPair");
        
        Map<String, String> tagsWithAlreadyPairedId = new HashMap<>();
        tagsWithAlreadyPairedId.put(PAIR_INSTANCE_ID.getTagName(), instanceId);
        
        List<EC2Instance> instanceIds = new ArrayList<>();
        instanceIds.add(instance1);
        instanceIds.add(instance2);
        
        when(awsHelperService.getInstancesForAutoScalingGroup(
            envValues.getAutoScaleGroupNameForPublishDispatcher())).thenReturn(instanceIds);
        
        when(awsHelperService.getTags(instance1.getInstanceId())).thenReturn(tagsWithPairName);
        when(awsHelperService.getTags(instance2.getInstanceId())).thenReturn(tagsWithAlreadyPairedId); //Instance 2 is the winner
        
        String resultInstanceId = aemHelperService.findUnpairedPublishDispatcher(instanceId);
        
        assertThat(resultInstanceId, equalTo(instance2.getInstanceId()));
    }
    
    
    @Test
    public void testGetPublishIdForPairedDispatcherWithFoundPair() {
        String instance1 = "1st-876543";
        String instance2 = "2nd-546424";
        String instance3 = "3rd-134777";
        String instance4 = "4th-736544";
        
        String dispatcherId = "dis-4385974";
        
        Map<String, String> tagsWithPair = new HashMap<>();
        tagsWithPair.put(PAIR_INSTANCE_ID.getTagName(), dispatcherId);
        
        Map<String, String> tagsWithoutPair = new HashMap<>();
        tagsWithoutPair.put(PAIR_INSTANCE_ID.getTagName(), "abc-35734685");
        
        Map<String, String> tagsMissingPair = new HashMap<>();
        
        // Mock adding a bunch of instances to the auto sacling group
        List<String> instanceIds = new ArrayList<>();
        instanceIds.add(instance1);
        instanceIds.add(instance2);
        instanceIds.add(instance3);
        instanceIds.add(instance4);
        
        when(awsHelperService.getInstanceIdsForAutoScalingGroup(envValues.getAutoScaleGroupNameForPublish())).thenReturn(instanceIds);
        when(awsHelperService.getTags(instance1)).thenReturn(tagsWithoutPair);
        when(awsHelperService.getTags(instance2)).thenReturn(tagsMissingPair); 
        when(awsHelperService.getTags(instance3)).thenReturn(tagsWithPair); //Instance 3 is the winner

        String resultInstanceId = aemHelperService.getPublishIdForPairedDispatcher(dispatcherId);

        verify(awsHelperService, never()).getTags(instance4);
        assertThat(resultInstanceId, equalTo(instance3));
    }
    
    @Test
    public void testGetPublishIdForPairedDispatcherWithNoPair() {
        String instance1 = "1st-876543";
        Map<String, String> tagsMissingPair = new HashMap<>();
        
        List<String> instanceIds = new ArrayList<>();
        instanceIds.add("1st-876543");

        when(awsHelperService.getInstanceIdsForAutoScalingGroup(envValues.getAutoScaleGroupNameForPublish())).thenReturn(instanceIds);
        when(awsHelperService.getTags(instance1)).thenReturn(tagsMissingPair);

        String resultInstanceId = aemHelperService.getPublishIdForPairedDispatcher("irrelevant-id");
        
        // If can't find pair, then should return null
        assertThat(resultInstanceId, equalTo(null));
    }
    
    @Test
    public void testGetDispatcherIdForPairedPublishWithFoundPair() {
        String instance1 = "1st-876543";
        String instance2 = "2nd-546424";
        String instance3 = "3rd-134777";
        String instance4 = "4th-736544";
        
        String publishId = "dis-4385974";
        
        Map<String, String> tagsWithPair = new HashMap<>();
        tagsWithPair.put(PAIR_INSTANCE_ID.getTagName(), publishId);
        
        Map<String, String> tagsWithoutPair = new HashMap<>();
        tagsWithoutPair.put(PAIR_INSTANCE_ID.getTagName(), "abc-35734685");
        
        Map<String, String> tagsMissingPair = new HashMap<>();
        
        // Mock adding a bunch of instances to the auto sacling group
        List<String> instanceIds = new ArrayList<>();
        instanceIds.add(instance1);
        instanceIds.add(instance2);
        instanceIds.add(instance3);
        instanceIds.add(instance4);
        
        when(awsHelperService.getInstanceIdsForAutoScalingGroup(envValues.getAutoScaleGroupNameForPublishDispatcher())).thenReturn(instanceIds);
        when(awsHelperService.getTags(instance1)).thenReturn(tagsWithoutPair);
        when(awsHelperService.getTags(instance2)).thenReturn(tagsMissingPair); 
        when(awsHelperService.getTags(instance3)).thenReturn(tagsWithoutPair); 
        when(awsHelperService.getTags(instance4)).thenReturn(tagsWithPair); //Instance 4 is the winner
        
        String resultInstanceId = aemHelperService.getDispatcherIdForPairedPublish(publishId);
        
        assertThat(resultInstanceId, equalTo(instance4));
    }
    
    @Test
    public void testGetDispatcherIdForPairedPublishWithNoPair() {
        String instance1 = "1st-876543";
        Map<String, String> tagsMissingPair = new HashMap<>();
        
        List<String> instanceIds = new ArrayList<>();
        instanceIds.add("1st-876543");

        when(awsHelperService.getInstanceIdsForAutoScalingGroup(anyString())).thenReturn(instanceIds);
        when(awsHelperService.getTags(instance1)).thenReturn(tagsMissingPair);

        String resultInstanceId = aemHelperService.getDispatcherIdForPairedPublish("irrelevant-id");
        
        // If can't find pair, then should return null
        assertThat(resultInstanceId, equalTo(null));
    }
    
    @Test
    public void testGetAutoScalingGroupDesiredCapacityForPublish() {
        int capacityToReturn = 1337;
        when(awsHelperService.getAutoScalingGroupDesiredCapacity(
            envValues.getAutoScaleGroupNameForPublish())).thenReturn(capacityToReturn);
        
        int desiredCapacity = aemHelperService.getAutoScalingGroupDesiredCapacityForPublish();
        assertThat(desiredCapacity, equalTo(capacityToReturn));
        verify(awsHelperService, times(1)).getAutoScalingGroupDesiredCapacity(
            envValues.getAutoScaleGroupNameForPublish());
    }
    
    @Test
    public void testGetAutoScalingGroupDesiredCapacityForPublishDispatcher() {
         int capacityToReturn = 1338;
         when(awsHelperService.getAutoScalingGroupDesiredCapacity(
             envValues.getAutoScaleGroupNameForPublishDispatcher())).thenReturn(capacityToReturn);
         
         int desiredCapacity = aemHelperService.getAutoScalingGroupDesiredCapacityForPublishDispatcher();
         assertThat(desiredCapacity, equalTo(capacityToReturn));
         
         verify(awsHelperService, times(1)).getAutoScalingGroupDesiredCapacity(
             envValues.getAutoScaleGroupNameForPublishDispatcher());
    }
    
    @Test
    public void testSetAutoScalingGroupDesiredCapacityForPublish() {
        int desiredCapacity = 1339;
        
        aemHelperService.setAutoScalingGroupDesiredCapacityForPublish(desiredCapacity);
        
        verify(awsHelperService, times(1)).setAutoScalingGroupDesiredCapacity(
            envValues.getAutoScaleGroupNameForPublish(), desiredCapacity);
    }
    
    @Test
    public void testPairPublishWithDispatcher() {
        String publishId = "pub-1";
        String dispatcherId = "dis-1";
        
        String publishHost = "pub-host";
        String dispatcherHost = "dis-host";
        
        when(awsHelperService.getPrivateIp(dispatcherId)).thenReturn(dispatcherHost);
        when(awsHelperService.getPrivateIp(publishId)).thenReturn(publishHost);
        
        aemHelperService.pairPublishWithDispatcher(publishId, dispatcherId);
        
        verify(awsHelperService, times(1)).addTags(eq(publishId), mapCaptor.capture());
        verify(awsHelperService, times(1)).addTags(eq(dispatcherId), mapCaptor.capture());
        
        Map<String, String> publishTags = mapCaptor.getAllValues().get(0);
        
        //Confirm that the correct tags and their values are set for publish instance
        assertThat(publishTags.get(AEM_PUBLISH_DISPATCHER_HOST.getTagName()), equalTo(dispatcherHost));
        assertThat(publishTags.get(PAIR_INSTANCE_ID.getTagName()), equalTo(dispatcherId));
        
        Map<String, String> dispatcherTags = mapCaptor.getAllValues().get(1);
        
        //Confirm that the correct tags and their values are set for publish dispatcher
        assertThat(dispatcherTags.get(AEM_PUBLISH_HOST.getTagName()), equalTo(publishHost));
        assertThat(dispatcherTags.get(PAIR_INSTANCE_ID.getTagName()), equalTo(publishId));
    }
    
    @Test   
    public void testTagInstanceWithSnapshotId() {
        String instanceId = "instance-1";
        String snapshotId = "snapshot-1";
        
        aemHelperService.tagInstanceWithSnapshotId(instanceId, snapshotId);
        
        verify(awsHelperService, times(1)).addTags(eq(instanceId), mapCaptor.capture());
        
        Map<String, String> tags = mapCaptor.getValue();
        
        assertThat(tags.get(SNAPSHOT_ID.getTagName()), equalTo(snapshotId));
    }
    
    @Test
    public void testTagAuthorDispatcherWithAuthorHost() {
        String authorDispatcherInstanceId = "auth-dis-1";
        
        aemHelperService.tagAuthorDispatcherWithAuthorHost(authorDispatcherInstanceId);
        
        verify(awsHelperService, times(1)).addTags(eq(authorDispatcherInstanceId), mapCaptor.capture());
        
        Map<String, String> tags = mapCaptor.getValue();
        
        assertThat(tags.get(AEM_AUTHOR_HOST.getTagName()), equalTo(envValues.getElasticLoadBalancerAuthorDns()));
    }
    
    @Test
    public void testIsAuthorElbHealthyOk() throws Exception {
        ArgumentCaptor<String> urlCaptor = ArgumentCaptor.forClass(String.class);
        when(httpUtil.isHttpGetResponseOk(urlCaptor.capture())).thenReturn(true);
        
        boolean result = aemHelperService.isAuthorElbHealthy();
        
        String url = urlCaptor.getValue();
        
        assertThat(url, startsWith(aemAuthorProtocol + "://" + 
            envValues.getElasticLoadBalancerAuthorDns() + ":" + aemAuthorPort));
        assertThat(result, equalTo(true));
    }
    
    @Test
    public void testIsAuthorElbHealthyNotOk() throws Exception {
        when(httpUtil.isHttpGetResponseOk(anyString())).thenReturn(false);
        
        boolean result = aemHelperService.isAuthorElbHealthy();
        
        assertThat(result, equalTo(false));
    }
    
    @Test
    public void testIsPubishHealthyOk() {
        Map<String, String> tagsComponentInitStatusSuccess = new HashMap<>();
        tagsComponentInitStatusSuccess.put(COMPONENT_INIT_STATUS.getTagName(), "Success");

        when(awsHelperService.getTags(instanceId)).thenReturn(tagsComponentInitStatusSuccess);

        boolean result = aemHelperService.isPubishHealthy(instanceId);

        assertThat(result, equalTo(true));
    }
    
    @Test
    public void testIsPubishHealthyNotOk() {
        Map<String, String> tagsComponentInitStatusRunning = new HashMap<>();
        tagsComponentInitStatusRunning.put(COMPONENT_INIT_STATUS.getTagName(), "InProgress");

        when(awsHelperService.getTags(instanceId)).thenReturn(tagsComponentInitStatusRunning);

        boolean result = aemHelperService.isPubishHealthy(instanceId);
        
        assertThat(result, equalTo(false));
    }
    
    @Test
    public void testWaitForPublishToBeHealthyOk() throws Exception {
        Map<String, String> tagsComponentInitStatusSuccess = new HashMap<>();
        tagsComponentInitStatusSuccess.put(COMPONENT_INIT_STATUS.getTagName(), "Success");

        when(awsHelperService.getTags(instanceId)).thenReturn(tagsComponentInitStatusSuccess);

        aemHelperService.waitForPublishToBeHealthy(instanceId);
    }
    
    @Test(expected=InstanceNotInHealthyStateException.class)
    public void testWaitForPublishToBeHealthyNotOk() throws Exception {
        Map<String, String> tagsComponentInitStatusFailed = new HashMap<>();
        tagsComponentInitStatusFailed.put(COMPONENT_INIT_STATUS.getTagName(), "Failed");

        when(awsHelperService.getTags(instanceId)).thenReturn(tagsComponentInitStatusFailed);

        aemHelperService.waitForPublishToBeHealthy(instanceId);
    }
    
    @Test(expected=InstanceNotInHealthyStateException.class)
    public void testWaitForPublishToBeHealthyWithIOException() throws Exception {
        Map<String, String> tagsComponentInitStatusFailed = new HashMap<>();
        tagsComponentInitStatusFailed.put(COMPONENT_INIT_STATUS.getTagName(), "Failed");

        when(awsHelperService.getTags(instanceId)).thenReturn(tagsComponentInitStatusFailed);

        aemHelperService.waitForPublishToBeHealthy(instanceId);
    }
    
    @Test
    public void testCreatePublishSnapshotWithSelectedTags() {
        String tag1 = "testTag1";
        String tag2 = "testTag2";
        
        String snapshotId = "x3289751048";
        
        List <String> tagsToApplyToSnapshot = Arrays.asList(tag1, tag2);
        
        setField(aemHelperService, "tagsToApplyToSnapshot", tagsToApplyToSnapshot);
        
        Map<String, String> activePublishTags = new HashMap<>();
        activePublishTags.put("someRandomTag1", "someRandomTag1");
        activePublishTags.put(tag1, tag1);
        activePublishTags.put("someRandomTag2", "someRandomTag2");
        activePublishTags.put(tag2, tag2);
        activePublishTags.put("someRandomTag3", "someRandomTag3");
        
        when(awsHelperService.getTags(instanceId)).thenReturn(activePublishTags);
        
        when(awsHelperService.createSnapshot(anyString(), anyString())).thenReturn(snapshotId);
        
        String resultId = aemHelperService.createPublishSnapshot(instanceId, "volumeId");
        
        verify(awsHelperService, times(1)).addTags(anyString(), mapCaptor.capture());
        
        Map<String, String> capturedTags = mapCaptor.getValue();
        
        //Ensure that it only uses the specified tags on the snapshot
        assertThat(capturedTags.size(), equalTo(tagsToApplyToSnapshot.size() + 2));
        assertThat(capturedTags.get(tag1), equalTo(tag1));
        assertThat(capturedTags.get(tag2), equalTo(tag2));
        assertThat(capturedTags.get(SNAPSHOT_TYPE.getTagName()), equalTo("orchestration"));
        assertThat(capturedTags.containsKey(NAME.getTagName()), equalTo(true));
        
        assertThat(resultId, equalTo(snapshotId));
    }
    
    
    @Test
    public void testCreatePublishSnapshotWithNoSelectedTags() {
        String snapshotId = "x3289751048";
        
        List <String> tagsToApplyToSnapshot = new ArrayList<>();
        
        setField(aemHelperService, "tagsToApplyToSnapshot", tagsToApplyToSnapshot);
        
        Map<String, String> activePublishTags = new HashMap<>();
        activePublishTags.put("someRandomTag1", "someRandomTag1");
        activePublishTags.put("someRandomTag2", "someRandomTag2");
        activePublishTags.put("someRandomTag3", "someRandomTag3");
        
        when(awsHelperService.getTags(instanceId)).thenReturn(activePublishTags);
        
        when(awsHelperService.createSnapshot(anyString(), anyString())).thenReturn(snapshotId);
        
        String resultId = aemHelperService.createPublishSnapshot(instanceId, "volumeId");
        
        verify(awsHelperService, times(1)).addTags(anyString(), mapCaptor.capture());
        
        Map<String, String> capturedTags = mapCaptor.getValue();
        
        //Ensure that it only uses the specified tags on the snapshot
        assertThat(capturedTags.size(), equalTo(2));
        assertThat(capturedTags.get(SNAPSHOT_TYPE.getTagName()), equalTo("orchestration"));
        assertThat(capturedTags.containsKey(NAME.getTagName()), equalTo(true));
        
        assertThat(resultId, equalTo(snapshotId));
    }

    @Test
    public void testCreateContentHealthAlarmForPublisher() {
        aemHelperService.createContentHealthAlarmForPublisher(instanceId);

        final ArgumentCaptor<String> captor = ArgumentCaptor.forClass(String.class);
        verify(awsHelperService).createContentHealthCheckAlarm(captor.capture(), captor.capture(), eq(instanceId), eq(awsPublishDispatcherStackName), eq(envValues.getTopicArn()));
        assertThat(captor.getAllValues().stream().allMatch(param -> param.endsWith(instanceId)), is(true));
    }
    
    @Test
    public void testDeleteContentHealthAlarmForPublisher() {
        aemHelperService.deleteContentHealthAlarmForPublisher(instanceId);

        final ArgumentCaptor<String> captor = ArgumentCaptor.forClass(String.class);
        verify(awsHelperService).deleteAlarm(captor.capture());
        assertThat(captor.getValue().endsWith(instanceId), is(true));
    } 
}
