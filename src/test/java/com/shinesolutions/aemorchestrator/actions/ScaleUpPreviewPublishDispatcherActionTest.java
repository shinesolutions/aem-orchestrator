package com.shinesolutions.aemorchestrator.actions;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.mockito.Matchers.anyInt;
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

@RunWith(MockitoJUnitRunner.class)
public class ScaleUpPreviewPublishDispatcherActionTest {

    @Mock
    private AemInstanceHelperService aemHelperService;

    @InjectMocks
    private ScaleUpPreviewPublishDispatcherAction action;

    private String instanceId;

    @Before
    public void setUp() throws Exception {
        instanceId = "i-4398603686";

    }

    @Test
    public void testSameDesiredCapacity() {
        when(aemHelperService.getAutoScalingGroupDesiredCapacityForPreviewPublishDispatcher()).thenReturn(2);
        when(aemHelperService.getAutoScalingGroupDesiredCapacityForPreviewPublish()).thenReturn(2);

        boolean success = action.execute(instanceId);

        verify(aemHelperService, times(0)).setAutoScalingGroupDesiredCapacityForPreviewPublish(anyInt());

        assertThat(success, equalTo(true));
    }

    @Test
    public void testHigherDesiredCapacity() {
        int dispatcherDesiredCapcity = 4;
        when(aemHelperService.getAutoScalingGroupDesiredCapacityForPreviewPublishDispatcher()).thenReturn(
            dispatcherDesiredCapcity);
        when(aemHelperService.getAutoScalingGroupDesiredCapacityForPreviewPublish()).thenReturn(
            dispatcherDesiredCapcity + 1);

        boolean success = action.execute(instanceId);

        verify(aemHelperService, times(1)).setAutoScalingGroupDesiredCapacityForPreviewPublish(dispatcherDesiredCapcity);

        assertThat(success, equalTo(true));
    }

    @Test
    public void testLowerDesiredCapacity() {
        int dispatcherDesiredCapcity = 3;
        when(aemHelperService.getAutoScalingGroupDesiredCapacityForPreviewPublishDispatcher()).thenReturn(
            dispatcherDesiredCapcity);
        when(aemHelperService.getAutoScalingGroupDesiredCapacityForPreviewPublish()).thenReturn(
            dispatcherDesiredCapcity - 1);

        boolean success = action.execute(instanceId);

        verify(aemHelperService, times(1)).setAutoScalingGroupDesiredCapacityForPreviewPublish(dispatcherDesiredCapcity);

        assertThat(success, equalTo(true));
    }

}
