package com.shinesolutions.aemorchestrator.service;

import static com.shinesolutions.aemorchestrator.model.InstanceTags.AEM_AUTHOR_HOST;
import static com.shinesolutions.aemorchestrator.model.InstanceTags.AEM_PUBLISH_DISPATCHER_HOST;
import static com.shinesolutions.aemorchestrator.model.InstanceTags.AEM_PUBLISH_HOST;
import static com.shinesolutions.aemorchestrator.model.InstanceTags.PAIR_INSTANCE_ID;
import static com.shinesolutions.aemorchestrator.model.InstanceTags.SNAPSHOT_ID;
import static com.shinesolutions.aemorchestrator.model.InstanceTags.SNAPSHOT_TYPE;
import static com.shinesolutions.aemorchestrator.model.InstanceTags.NAME;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.startsWith;
import static org.mockito.Matchers.anyString;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.util.ReflectionTestUtils.setField;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import com.shinesolutions.aemorchestrator.exception.InstanceNotInHealthyState;
import com.shinesolutions.aemorchestrator.model.EnvironmentValues;
import com.shinesolutions.aemorchestrator.model.InstanceTags;
import com.shinesolutions.aemorchestrator.util.HttpUtil;

@RunWith(MockitoJUnitRunner.class)
public class AemInstanceHelperServiceTest {
    
    private String aemPublishDispatcherProtocol;
    private String aemPublishProtocol;
    private String aemAuthorDispatcherProtocol;
    private String aemAuthorProtocol;
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
    public void setUp() throws Exception {
        instanceId = "test-123456789";
        privateIp = "11.22.33.44";
        
        envValues = new EnvironmentValues();
        envValues.setAutoScaleGroupNameForPublishDispatcher("publishDispatcherTestName");
        envValues.setAutoScaleGroupNameForPublish("publishTestName");
        envValues.setAutoScaleGroupNameForAuthorDispatcher("authorTestName");
        envValues.setElasticLoadBalancerNameForAuthor("elasticLoadBalancerNameForAuthor");
        envValues.setElasticLoadBalancerAuthorDns("elasticLoadBalancerAuthorDns");
        
        aemPublishDispatcherProtocol = "pdpd";
        aemPublishProtocol = "pppp";
        aemAuthorDispatcherProtocol = "adad";
        aemAuthorProtocol = "aaaa";
        aemPublishDispatcherPort = 1111;
        aemPublishPort = 2222;
        aemAuthorDispatcherPort = 3333;
        aemAuthorPort = 4444;
        
        setField(aemHelperService, "envValues", envValues);
        
        setField(aemHelperService, "aemPublishDispatcherProtocol", aemPublishDispatcherProtocol);
        setField(aemHelperService, "aemPublishProtocol", aemPublishProtocol);
        setField(aemHelperService, "aemAuthorDispatcherProtocol", aemAuthorDispatcherProtocol);
        setField(aemHelperService, "aemAuthorProtocol", aemAuthorProtocol);
        
        setField(aemHelperService, "aemPublishDispatcherPort", aemPublishDispatcherPort);
        setField(aemHelperService, "aemPublishPort", aemPublishPort);
        setField(aemHelperService, "aemAuthorDispatcherPort", aemAuthorDispatcherPort);
        setField(aemHelperService, "aemAuthorPort", aemAuthorPort);
    }

    @Test
    public void testGetAemUrlForPublishDispatcher() throws Exception {
        when(awsHelperService.getPrivateIp(instanceId)).thenReturn(privateIp);
        
        String aemUrl = aemHelperService.getAemUrlForPublishDispatcher(instanceId);
        
        assertThat(aemUrl, equalTo(aemPublishDispatcherProtocol + "://" + privateIp + ":" + aemPublishDispatcherPort));
    }
    
    @Test
    public void testGetAemUrlForPublish() throws Exception {
        when(awsHelperService.getPrivateIp(instanceId)).thenReturn(privateIp);
        
        String aemUrl = aemHelperService.getAemUrlForPublish(instanceId);
        
        assertThat(aemUrl, equalTo(aemPublishProtocol + "://" + privateIp + ":" + aemPublishPort));
    }
    
    @Test
    public void testGetAemUrlForAuthorElb() throws Exception {
        String aemUrl = aemHelperService.getAemUrlForAuthorElb();
        
        assertThat(aemUrl, equalTo(aemAuthorProtocol + "://" + 
            envValues.getElasticLoadBalancerAuthorDns() + ":" + aemAuthorPort));
    }
    
    @Test
    public void testGetAemUrlForAuthorDispatcher() throws Exception {
        when(awsHelperService.getPrivateIp(instanceId)).thenReturn(privateIp);
        
        String aemUrl = aemHelperService.getAemUrlForAuthorDispatcher(instanceId);
        
        assertThat(aemUrl, equalTo(aemAuthorDispatcherProtocol + "://" + privateIp + ":" + aemAuthorDispatcherPort));
    }
    
    @Test
    public void testGetPublishIdToSnapshotFrom() throws Exception {
        String excludeInstanceId = "exclude-352768";
        List<String> instanceIds = new ArrayList<String>();
        instanceIds.add(excludeInstanceId);
        instanceIds.add(instanceId);
        instanceIds.add("extra-89351");
        
        when(awsHelperService.getInstanceIdsForAutoScalingGroup(
            envValues.getAutoScaleGroupNameForPublish())).thenReturn(instanceIds);
        
        when(httpUtil.isHttpGetResponseOk(anyString())).thenReturn(true);
        
        String resultInstanceId = aemHelperService.getPublishIdToSnapshotFrom(excludeInstanceId);
        
        assertThat(resultInstanceId, equalTo(instanceId));
    }
    
    @Test
    public void testGetPublishIdToSnapshotFromWithNoInstances() throws Exception {
        when(awsHelperService.getInstanceIdsForAutoScalingGroup(
            envValues.getAutoScaleGroupNameForPublish())).thenReturn(new ArrayList<String>());
        
        String resultInstanceId = aemHelperService.getPublishIdToSnapshotFrom("s-2397106");
        
        assertThat(resultInstanceId, equalTo(null));
    }
    
    @Test
    public void testIsFirstPublishInstanceNoSnapshotTags() throws Exception {
        Map<String, String> instanceTags = new HashMap<String, String>();
        
        List<String> instanceIds = new ArrayList<String>();
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
    public void testIsFirstPublishInstanceOneSnapshotTag() throws Exception {
        Map<String, String> instanceTags1 = new HashMap<String, String>();
        Map<String, String> instanceTags2 = new HashMap<String, String>();
        instanceTags2.put(InstanceTags.SNAPSHOT_ID.getTagName(), "");
        
        List<String> instanceIds = new ArrayList<String>();
        instanceIds.add("i-1");
        instanceIds.add("i-2");
        
        when(awsHelperService.getInstanceIdsForAutoScalingGroup(
            envValues.getAutoScaleGroupNameForPublish())).thenReturn(instanceIds);
        
        when(awsHelperService.getTags(anyString())).thenReturn(instanceTags1, instanceTags2);
        
        boolean result = aemHelperService.isFirstPublishInstance();
        
        assertThat(result, equalTo(false));
    }
    
    @Test
    public void testIsFirstPublishInstanceWithNoInstances() throws Exception {
        List<String> instanceIds = new ArrayList<String>();
        
        when(awsHelperService.getInstanceIdsForAutoScalingGroup(
            envValues.getAutoScaleGroupNameForPublish())).thenReturn(instanceIds);
        
        boolean result = aemHelperService.isFirstPublishInstance();
        
        assertThat(result, equalTo(true));
    }
    
    @Test
    public void testFindUnpairedPublishDispatcher() throws Exception {
        String instance1 = "1st-324983";
        String instance2 = "2nd-348894";
        String instance3 = "3rd-134333";
        
        Map<String, String> tagsWithPairName = new HashMap<String, String>();
        tagsWithPairName.put(PAIR_INSTANCE_ID.getTagName(), "testPair");
        
        Map<String, String> tagsWithoutPairName = new HashMap<String, String>();
        
        List<String> instanceIds = new ArrayList<String>();
        instanceIds.add(instance1);
        instanceIds.add(instance2);
        instanceIds.add(instance3);
        
        when(awsHelperService.getInstanceIdsForAutoScalingGroup(envValues.getAutoScaleGroupNameForPublishDispatcher())).thenReturn(instanceIds);
        when(awsHelperService.getTags(instance1)).thenReturn(tagsWithPairName);
        when(awsHelperService.getTags(instance2)).thenReturn(tagsWithoutPairName); //Instance 2 is the winner
        when(awsHelperService.getTags(instance3)).thenReturn(tagsWithPairName);
        
        String resultInstanceId = aemHelperService.findUnpairedPublishDispatcher(instanceId);
        
        assertThat(resultInstanceId, equalTo(instance2));
    }
    
    @Test
    public void testFindUnpairedPublishDispatcherAlreadyPaired() throws Exception {
        String instance1 = "1st-324983";
        String instance2 = "2nd-348894";
        
        Map<String, String> tagsWithPairName = new HashMap<String, String>();
        tagsWithPairName.put(PAIR_INSTANCE_ID.getTagName(), "testPair");
        
        Map<String, String> tagsWithAlreadyPairedId = new HashMap<String, String>();
        tagsWithAlreadyPairedId.put(PAIR_INSTANCE_ID.getTagName(), instanceId);
        
        List<String> instanceIds = new ArrayList<String>();
        instanceIds.add(instance1);
        instanceIds.add(instance2);
        
        when(awsHelperService.getInstanceIdsForAutoScalingGroup(envValues.getAutoScaleGroupNameForPublishDispatcher())).thenReturn(instanceIds);
        when(awsHelperService.getTags(instance1)).thenReturn(tagsWithPairName);
        when(awsHelperService.getTags(instance2)).thenReturn(tagsWithAlreadyPairedId); //Instance 2 is the winner
        
        String resultInstanceId = aemHelperService.findUnpairedPublishDispatcher(instanceId);
        
        assertThat(resultInstanceId, equalTo(instance2));
    }
    
    @Test
    public void testGetPublishIdForPairedDispatcherWithFoundPair() throws Exception {
        String instance1 = "1st-876543";
        String instance2 = "2nd-546424";
        String instance3 = "3rd-134777";
        String instance4 = "4th-736544";
        
        String dispatcherId = "dis-4385974";
        
        Map<String, String> tagsWithPair = new HashMap<String, String>();
        tagsWithPair.put(PAIR_INSTANCE_ID.getTagName(), dispatcherId);
        
        Map<String, String> tagsWithoutPair = new HashMap<String, String>();
        tagsWithoutPair.put(PAIR_INSTANCE_ID.getTagName(), "abc-35734685");
        
        Map<String, String> tagsMissingPair = new HashMap<String, String>();
        
        // Mock adding a bunch of instances to the auto sacling group
        List<String> instanceIds = new ArrayList<String>();
        instanceIds.add(instance1);
        instanceIds.add(instance2);
        instanceIds.add(instance3);
        instanceIds.add(instance4);
        
        when(awsHelperService.getInstanceIdsForAutoScalingGroup(envValues.getAutoScaleGroupNameForPublish())).thenReturn(instanceIds);
        when(awsHelperService.getTags(instance1)).thenReturn(tagsWithoutPair);
        when(awsHelperService.getTags(instance2)).thenReturn(tagsMissingPair); 
        when(awsHelperService.getTags(instance3)).thenReturn(tagsWithPair); //Instance 3 is the winner
        when(awsHelperService.getTags(instance4)).thenReturn(tagsWithoutPair);
        
        String resultInstanceId = aemHelperService.getPublishIdForPairedDispatcher(dispatcherId);
        
        assertThat(resultInstanceId, equalTo(instance3));
    }
    
    @Test
    public void testGetPublishIdForPairedDispatcherWithNoPair() throws Exception {
        String instance1 = "1st-876543";
        Map<String, String> tagsMissingPair = new HashMap<String, String>();
        
        List<String> instanceIds = new ArrayList<String>();
        instanceIds.add("1st-876543");

        when(awsHelperService.getInstanceIdsForAutoScalingGroup(envValues.getAutoScaleGroupNameForPublish())).thenReturn(instanceIds);
        when(awsHelperService.getTags(instance1)).thenReturn(tagsMissingPair);

        String resultInstanceId = aemHelperService.getPublishIdForPairedDispatcher("irrelevant-id");
        
        // If can't find pair, then should return null
        assertThat(resultInstanceId, equalTo(null));
    }
    
    @Test
    public void testGetDispatcherIdForPairedPublishWithFoundPair() throws Exception {
        String instance1 = "1st-876543";
        String instance2 = "2nd-546424";
        String instance3 = "3rd-134777";
        String instance4 = "4th-736544";
        
        String publishId = "dis-4385974";
        
        Map<String, String> tagsWithPair = new HashMap<String, String>();
        tagsWithPair.put(PAIR_INSTANCE_ID.getTagName(), publishId);
        
        Map<String, String> tagsWithoutPair = new HashMap<String, String>();
        tagsWithoutPair.put(PAIR_INSTANCE_ID.getTagName(), "abc-35734685");
        
        Map<String, String> tagsMissingPair = new HashMap<String, String>();
        
        // Mock adding a bunch of instances to the auto sacling group
        List<String> instanceIds = new ArrayList<String>();
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
    public void testGetDispatcherIdForPairedPublishWithNoPair() throws Exception {
        String instance1 = "1st-876543";
        Map<String, String> tagsMissingPair = new HashMap<String, String>();
        
        List<String> instanceIds = new ArrayList<String>();
        instanceIds.add("1st-876543");

        when(awsHelperService.getInstanceIdsForAutoScalingGroup(envValues.getAutoScaleGroupNameForPublish())).thenReturn(instanceIds);
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
    public void testIsPubishHealthyOk() throws Exception {
        ArgumentCaptor<String> urlCaptor = ArgumentCaptor.forClass(String.class);
        String publishIp = "testPublishIP";
        
        when(httpUtil.isHttpGetResponseOk(urlCaptor.capture())).thenReturn(true);
        
        when(awsHelperService.getPrivateIp(instanceId)).thenReturn(publishIp);
        
        boolean result = aemHelperService.isPubishHealthy(instanceId);
        
        String url = urlCaptor.getValue();
        
        assertThat(url, startsWith(aemPublishProtocol + "://" + publishIp + ":" + aemPublishPort));
        assertThat(result, equalTo(true));
    }
    
    @Test
    public void testIsPubishHealthyNotOk() throws Exception {
        when(httpUtil.isHttpGetResponseOk(anyString())).thenReturn(false);
        
        boolean result = aemHelperService.isPubishHealthy(instanceId);
        
        assertThat(result, equalTo(false));
    }
    
    @Test
    public void testWaitForPublishToBeHealthyOk() throws Exception {
        when(httpUtil.isHttpGetResponseOk(anyString())).thenReturn(true);
        
        //Will throw an exception if not ok
        aemHelperService.waitForPublishToBeHealthy(instanceId);
    }
    
    @Test(expected=InstanceNotInHealthyState.class)
    public void testWaitForPublishToBeHealthyNotOk() throws Exception {
        when(httpUtil.isHttpGetResponseOk(anyString())).thenReturn(false);
        
        aemHelperService.waitForPublishToBeHealthy(instanceId);
    }
    
    @Test(expected=InstanceNotInHealthyState.class)
    public void testWaitForPublishToBeHealthyWithIOException() throws Exception {
        when(httpUtil.isHttpGetResponseOk(anyString())).thenThrow(new IOException());
        
        aemHelperService.waitForPublishToBeHealthy(instanceId);
    }
    
    @Test
    public void testCreatePublishSnapshotWithSelectedTags() throws Exception {
        String tag1 = "testTag1";
        String tag2 = "testTag2";
        
        String snapshotId = "x3289751048";
        
        List <String> tagsToApplyToSnapshot = Arrays.asList(tag1, tag2);
        
        setField(aemHelperService, "tagsToApplyToSnapshot", tagsToApplyToSnapshot);
        
        Map<String, String> activePublishTags = new HashMap<String, String>();
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
    public void testCreatePublishSnapshotWithNoSelectedTags() throws Exception {
        String snapshotId = "x3289751048";
        
        List <String> tagsToApplyToSnapshot = new ArrayList<String>();
        
        setField(aemHelperService, "tagsToApplyToSnapshot", tagsToApplyToSnapshot);
        
        Map<String, String> activePublishTags = new HashMap<String, String>();
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

}
