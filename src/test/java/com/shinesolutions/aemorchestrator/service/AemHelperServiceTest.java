package com.shinesolutions.aemorchestrator.service;

import static com.shinesolutions.aemorchestrator.service.InstanceTags.PAIR_INSTANCE_ID;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.mockito.Mockito.when;
import static org.springframework.test.util.ReflectionTestUtils.setField;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import com.shinesolutions.aemorchestrator.model.EnvironmentValues;

@RunWith(MockitoJUnitRunner.class)
public class AemHelperServiceTest {
    
    private String aemPublishDispatcherProtocol;
    private String aemPublishProtocol;
    private String aemAuthorDispatcherProtocol;
    private Integer aemPublishDispatcherPort;
    private Integer aemPublishPort;
    private Integer aemAuthorDispatcherPort;
    
    @Mock
    private AwsHelperService awsHelperService;
    
    @InjectMocks
    private AemHelperService aemHelperService;
    
    private EnvironmentValues envValues;
    
    private String instanceId;
    private String privateIp;
    
    @Before
    public void setUp() throws Exception {
        instanceId = "test-123456789";
        privateIp = "11.22.33.44";
        
        envValues = new EnvironmentValues();
        envValues.setAutoScaleGroupNameForPublishDispatcher("publisherDispatcherTestName");
        envValues.setAutoScaleGroupNameForPublish("publisherTestName");
        envValues.setAutoScaleGroupNameForAuthorDispatcher("authorTestName");
        
        aemPublishDispatcherProtocol = "pdpd";
        aemPublishProtocol = "pppp";
        aemAuthorDispatcherProtocol = "aaaa";
        aemPublishDispatcherPort = 1111;
        aemPublishPort = 2222;
        aemAuthorDispatcherPort = 3333;
        
        setField(aemHelperService, "envValues", envValues);
        
        setField(aemHelperService, "aemPublishDispatcherProtocol", aemPublishDispatcherProtocol);
        setField(aemHelperService, "aemPublishProtocol", aemPublishProtocol);
        setField(aemHelperService, "aemAuthorDispatcherProtocol", aemAuthorDispatcherProtocol);
        
        setField(aemHelperService, "aemPublishDispatcherPort", aemPublishDispatcherPort);
        setField(aemHelperService, "aemPublishPort", aemPublishPort);
        setField(aemHelperService, "aemAuthorDispatcherPort", aemAuthorDispatcherPort);
    }

    @Test
    public void testGetAemUrlForPublisherDispatcher() throws Exception {
        when(awsHelperService.getPrivateIp(instanceId)).thenReturn(privateIp);
        
        String aemUrl = aemHelperService.getAemUrlForPublisherDispatcher(instanceId);
        
        assertThat(aemUrl, equalTo(aemPublishDispatcherProtocol + "://" + privateIp + ":" + aemPublishDispatcherPort));
    }
    
    @Test
    public void testGetAemUrlForPublisher() throws Exception {
        when(awsHelperService.getPrivateIp(instanceId)).thenReturn(privateIp);
        
        String aemUrl = aemHelperService.getAemUrlForPublisher(instanceId);
        
        assertThat(aemUrl, equalTo(aemPublishProtocol + "://" + privateIp + ":" + aemPublishPort));
    }
    
    @Test
    public void testGetAemUrlForAuthorElb() throws Exception {
        when(awsHelperService.getElbDnsName(envValues.getElasticLoadBalancerNameForAuthor())).thenReturn(privateIp);
        
        String aemUrl = aemHelperService.getAemUrlForAuthorElb();
        
        assertThat(aemUrl, equalTo(aemAuthorDispatcherProtocol + "://" + privateIp + ":" + aemAuthorDispatcherPort));
    }
    
    @Test
    public void testGetAemUrlForAuthorDispatcher() throws Exception {
        when(awsHelperService.getPrivateIp(instanceId)).thenReturn(privateIp);
        
        String aemUrl = aemHelperService.getAemUrlForAuthorDispatcher(instanceId);
        
        assertThat(aemUrl, equalTo(aemAuthorDispatcherProtocol + "://" + privateIp + ":" + aemAuthorDispatcherPort));
    }
    
    @Test
    public void testGetPublisherIdToSnapshotFrom() throws Exception {
        String excludeInstanceId = "exclude-352768";
        List<String> instanceIds = new ArrayList<String>();
        instanceIds.add(excludeInstanceId);
        instanceIds.add(instanceId);
        instanceIds.add("extra-89351");
        
        when(awsHelperService.getInstanceIdsForAutoScalingGroup(envValues.getAutoScaleGroupNameForPublish())).thenReturn(instanceIds);
        String resultInstanceId = aemHelperService.getPublisherIdToSnapshotFrom(excludeInstanceId);
        
        assertThat(resultInstanceId, equalTo(instanceId));
    }
    
    @Test
    public void testFindUnpairedPublisherDispatcher() throws Exception {
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
        
        String resultInstanceId = aemHelperService.findUnpairedPublisherDispatcher();
        
        assertThat(resultInstanceId, equalTo(instance2));
    }
    
    @Test
    public void testGetPublisherIdForPairedDispatcher() throws Exception {
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
        
        String resultInstanceId = aemHelperService.getPublisherIdForPairedDispatcher(dispatcherId);
        
        assertThat(resultInstanceId, equalTo(instance3));
    }
    

}
