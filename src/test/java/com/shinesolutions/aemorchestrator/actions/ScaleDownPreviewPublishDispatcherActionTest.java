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
import org.mockito.junit.MockitoJUnitRunner;

import com.shinesolutions.aemorchestrator.service.AemInstanceHelperService;
import com.shinesolutions.aemorchestrator.service.AwsHelperService;

@RunWith(MockitoJUnitRunner.class)
public class ScaleDownPreviewPublishDispatcherActionTest {

    @Mock
    private AemInstanceHelperService aemHelperService;

    @Mock
    private AwsHelperService awsHelperService;

    @InjectMocks
    private ScaleDownPreviewPublishDispatcherAction action;

    private String instanceId;
    private String pairedPreviewPublishId;
    private int currentDispatcherDesiredCapacity;
    private int currentPreviewPublishDesiredCapacity;


    @Before
    public void setUp() throws Exception {
        instanceId = "instanceId";
        pairedPreviewPublishId = "pairedPreviewPublishId";
    }

    @Test
    public void testTerminatePairedPreviewPublishAndMatchDesiredCapacity() {
        currentDispatcherDesiredCapacity = 2;
        currentPreviewPublishDesiredCapacity = 3;

        when(aemHelperService.getPreviewPublishIdForPairedDispatcher(instanceId)).thenReturn(pairedPreviewPublishId);

        when(aemHelperService.getAutoScalingGroupDesiredCapacityForPreviewPublishDispatcher())
            .thenReturn(currentDispatcherDesiredCapacity);

        when(aemHelperService.getAutoScalingGroupDesiredCapacityForPreviewPublish())
            .thenReturn(currentPreviewPublishDesiredCapacity);

        boolean success = action.execute(instanceId);

        verify(awsHelperService, times(1)).terminateInstance(pairedPreviewPublishId);

        verify(aemHelperService, times(1))
            .setAutoScalingGroupDesiredCapacityForPreviewPublish(currentDispatcherDesiredCapacity);

        assertThat(success, equalTo(true));
    }

    @Test
    public void testTerminatePairedPreviewPublishAndDesiredCapacityAlreadyMatching() {
        currentDispatcherDesiredCapacity = 3;
        currentPreviewPublishDesiredCapacity = 3;

        when(aemHelperService.getPreviewPublishIdForPairedDispatcher(instanceId)).thenReturn(pairedPreviewPublishId);

        when(aemHelperService.getAutoScalingGroupDesiredCapacityForPreviewPublishDispatcher())
            .thenReturn(currentDispatcherDesiredCapacity);

        when(aemHelperService.getAutoScalingGroupDesiredCapacityForPreviewPublish())
            .thenReturn(currentPreviewPublishDesiredCapacity);

        boolean success = action.execute(instanceId);

        verify(awsHelperService, times(1)).terminateInstance(pairedPreviewPublishId);

        verify(aemHelperService, times(0))
            .setAutoScalingGroupDesiredCapacityForPreviewPublish(currentDispatcherDesiredCapacity);

        assertThat(success, equalTo(true));
    }


    @Test
    public void testCantFindPairedPreviewPublisher() {
        currentDispatcherDesiredCapacity = 1;
        currentPreviewPublishDesiredCapacity = 3;

        when(aemHelperService.getPreviewPublishIdForPairedDispatcher(instanceId)).thenReturn(null);

        when(aemHelperService.getAutoScalingGroupDesiredCapacityForPreviewPublishDispatcher())
            .thenReturn(currentDispatcherDesiredCapacity);

        when(aemHelperService.getAutoScalingGroupDesiredCapacityForPreviewPublish())
            .thenReturn(currentPreviewPublishDesiredCapacity);

        boolean success = action.execute(instanceId);

        verify(awsHelperService, times(0)).terminateInstance(pairedPreviewPublishId);

        verify(aemHelperService, times(1))
            .setAutoScalingGroupDesiredCapacityForPreviewPublish(currentDispatcherDesiredCapacity);

        assertThat(success, equalTo(true));
    }

}
