package com.shinesolutions.aemorchestrator.handler;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.mockito.Matchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.Map;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import com.shinesolutions.aemorchestrator.actions.ScaleAction;
import com.shinesolutions.aemorchestrator.model.EventMessage;
import com.shinesolutions.aemorchestrator.service.AwsHelperService;

@RunWith(MockitoJUnitRunner.class)
public class AutoScalingEventHandlerTest {
    
    @Mock
    private Map<String, ScaleAction> scaleDownAutoScaleGroupMappings;
    
    @Mock
    private AwsHelperService awsHelperService;
    
    @InjectMocks
    private AutoScalingEventHandler handler;
    
    private ScaleAction action;
    private EventMessage message;
    

    @Before
    public void setUp() throws Exception {
        action = mock(ScaleAction.class);
        
        message = new EventMessage();
        message.setAutoScalingGroupName("testGroup");
        message.setEC2InstanceId("test-instance");
    }

    @Test
    public void testNoActionFound() throws Exception {
        when(scaleDownAutoScaleGroupMappings.get(anyString())).thenReturn(null);
        
        boolean result = handler.handleEvent(message);
        
        assertThat(result, equalTo(false));
    }
    
    @Test
    public void testInstanceNotRunning() throws Exception {
        when(scaleDownAutoScaleGroupMappings.get(anyString())).thenReturn(action);
        when(awsHelperService.isInstanceRunning(message.getEC2InstanceId())).thenReturn(false);
        
        boolean result = handler.handleEvent(message);
        
        assertThat(result, equalTo(true));
        verify(action, never()).execute(message.getEC2InstanceId());
    }
    
    @Test
    public void testInstanceHandlesExceptionsGracefully() throws Exception {
        when(scaleDownAutoScaleGroupMappings.get(anyString())).thenThrow(new RuntimeException());
        
        boolean result = handler.handleEvent(message);
        
        assertThat(result, equalTo(false));
    }
    
    @Test
    public void testSuccessAndActionDeleteMessage() throws Exception {
        when(scaleDownAutoScaleGroupMappings.get(anyString())).thenReturn(action);
        when(awsHelperService.isInstanceRunning(message.getEC2InstanceId())).thenReturn(true);
        when(action.execute(message.getEC2InstanceId())).thenReturn(true);
        
        boolean result = handler.handleEvent(message);
        
        assertThat(result, equalTo(true));
        verify(action, times(1)).execute(message.getEC2InstanceId());
    }
    
    @Test
    public void testSuccessAndActionKeepMessage() throws Exception {
        when(scaleDownAutoScaleGroupMappings.get(anyString())).thenReturn(action);
        when(awsHelperService.isInstanceRunning(message.getEC2InstanceId())).thenReturn(true);
        when(action.execute(message.getEC2InstanceId())).thenReturn(false);
        
        boolean result = handler.handleEvent(message);
        
        assertThat(result, equalTo(false));
        verify(action, times(1)).execute(message.getEC2InstanceId());
    }

}
