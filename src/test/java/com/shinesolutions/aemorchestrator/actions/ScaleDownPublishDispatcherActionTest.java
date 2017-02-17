package com.shinesolutions.aemorchestrator.actions;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import com.shinesolutions.aemorchestrator.service.AemInstanceHelperService;
import com.shinesolutions.aemorchestrator.service.AwsHelperService;

@RunWith(MockitoJUnitRunner.class)
public class ScaleDownPublishDispatcherActionTest {
    
    @Mock
    private AemInstanceHelperService aemHelperService;

    @Mock
    private AwsHelperService awsHelperService;
    
    @InjectMocks
    private ScaleDownPublishDispatcherAction action;
    
    private String instanceId;
    private String pairedPublishId;
    private int currentDispatcherDesiredCapacity;
    private int currentPublishDesiredCapacity;
    

    @Before
    public void setUp() throws Exception {
        instanceId = "instanceId";
        pairedPublishId = "pairedPublishId";
    }

    @Test
    public void testTerminatePairedPublishAndMatchDesiredCapacity() {
        currentDispatcherDesiredCapacity = 2;
        currentPublishDesiredCapacity = 3;
        
        when(aemHelperService.getPublishIdForPairedDispatcher(instanceId)).thenReturn(pairedPublishId);
        
        when(aemHelperService.getAutoScalingGroupDesiredCapacityForPublishDispatcher())
            .thenReturn(currentDispatcherDesiredCapacity);
        
        when(aemHelperService.getAutoScalingGroupDesiredCapacityForPublish())
            .thenReturn(currentPublishDesiredCapacity);
        
        boolean success = action.execute(instanceId);
        
        verify(awsHelperService, times(1)).terminateInstance(pairedPublishId);
        
        verify(aemHelperService, times(1))
            .setAutoScalingGroupDesiredCapacityForPublish(currentDispatcherDesiredCapacity);
        
        assertThat(success, equalTo(true));
    }
    
    @Test
    public void testTerminatePairedPublishAndDesiredCapacityAlreadyMatching() {
        currentDispatcherDesiredCapacity = 3;
        currentPublishDesiredCapacity = 3;
        
        when(aemHelperService.getPublishIdForPairedDispatcher(instanceId)).thenReturn(pairedPublishId);
        
        when(aemHelperService.getAutoScalingGroupDesiredCapacityForPublishDispatcher())
            .thenReturn(currentDispatcherDesiredCapacity);
        
        when(aemHelperService.getAutoScalingGroupDesiredCapacityForPublish())
            .thenReturn(currentPublishDesiredCapacity);
        
        boolean success = action.execute(instanceId);
        
        verify(awsHelperService, times(1)).terminateInstance(pairedPublishId);
        
        verify(aemHelperService, times(0))
            .setAutoScalingGroupDesiredCapacityForPublish(currentDispatcherDesiredCapacity);
        
        assertThat(success, equalTo(true));
    }
    
    
    @Test
    public void testCantFindPairedPublisher() {
        currentDispatcherDesiredCapacity = 1;
        currentPublishDesiredCapacity = 3;
        
        when(aemHelperService.getPublishIdForPairedDispatcher(instanceId)).thenReturn(null);
        
        when(aemHelperService.getAutoScalingGroupDesiredCapacityForPublishDispatcher())
            .thenReturn(currentDispatcherDesiredCapacity);
        
        when(aemHelperService.getAutoScalingGroupDesiredCapacityForPublish())
            .thenReturn(currentPublishDesiredCapacity);
        
        boolean success = action.execute(instanceId);
        
        verify(awsHelperService, times(0)).terminateInstance(pairedPublishId);
        
        verify(aemHelperService, times(1))
            .setAutoScalingGroupDesiredCapacityForPublish(currentDispatcherDesiredCapacity);
        
        assertThat(success, equalTo(true));
    }

}
