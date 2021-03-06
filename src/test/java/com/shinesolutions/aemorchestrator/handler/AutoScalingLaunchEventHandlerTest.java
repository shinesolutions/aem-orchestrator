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
import org.mockito.junit.MockitoJUnitRunner;

import com.shinesolutions.aemorchestrator.actions.Action;
import com.shinesolutions.aemorchestrator.model.EventMessage;
import com.shinesolutions.aemorchestrator.service.AwsHelperService;
import com.shinesolutions.aemorchestrator.util.EventMessageExtractor;

@RunWith(MockitoJUnitRunner.class)
public class AutoScalingLaunchEventHandlerTest {
    
    @Mock
    private Map<String, Action> scaleUpAutoScaleGroupMappings;
    
    @Mock
    private AwsHelperService awsHelperService;
    
    @Mock
    private EventMessageExtractor eventMessageExtractor;
    
    @InjectMocks
    private AutoScalingLaunchEventHandler handler;
    
    private Action action;
    private String messageContent;
    private EventMessage message;
    

    @Before
    public void setUp() throws Exception {
        action = mock(Action.class);
        messageContent = "testMessage";
        
        message = new EventMessage();
        message.setAutoScalingGroupName("testGroup");
        message.setEC2InstanceId("test-instance");
        
        when(eventMessageExtractor.extractMessage(messageContent)).thenReturn(message);
    }

    @Test
    public void testNoActionToPerform() {
        message.setDescription("Moving EC2 instance out of Standby");
    
        boolean result = handler.handleEvent(messageContent);
        
        assertThat(result, equalTo(true));
    } 
    
    @Test
    public void testNoActionFound() {
        when(scaleUpAutoScaleGroupMappings.get(anyString())).thenReturn(null);
        
        boolean result = handler.handleEvent(messageContent);
        
        assertThat(result, equalTo(false));
    }
    
    @Test
    public void testNoActionFoundWithEventDescription() {
        message.setDescription("Test description");
        when(scaleUpAutoScaleGroupMappings.get(anyString())).thenReturn(null);
        
        boolean result = handler.handleEvent(messageContent);
        
        assertThat(result, equalTo(false));
    }
    
    @Test
    public void testInstanceNotRunning() {
        when(scaleUpAutoScaleGroupMappings.get(anyString())).thenReturn(action);
        when(awsHelperService.isInstanceRunning(message.getEC2InstanceId())).thenReturn(false);
        
        boolean result = handler.handleEvent(messageContent);
        
        assertThat(result, equalTo(true));
        verify(action, never()).execute(message.getEC2InstanceId());
    }
    
    @Test
    public void testInstanceHandlesExceptionsGracefully() {
        when(scaleUpAutoScaleGroupMappings.get(anyString())).thenThrow(new RuntimeException());
        
        boolean result = handler.handleEvent(messageContent);
        
        assertThat(result, equalTo(false));
    }
    
    @Test
    public void testSuccessAndActionDeleteMessage() {
        when(scaleUpAutoScaleGroupMappings.get(anyString())).thenReturn(action);
        when(awsHelperService.isInstanceRunning(message.getEC2InstanceId())).thenReturn(true);
        when(action.execute(message.getEC2InstanceId())).thenReturn(true);
        
        boolean result = handler.handleEvent(messageContent);
        
        assertThat(result, equalTo(true));
        verify(action, times(1)).execute(message.getEC2InstanceId());
    }
    
    @Test
    public void testSuccessAndActionKeepMessage() {
        when(scaleUpAutoScaleGroupMappings.get(anyString())).thenReturn(action);
        when(awsHelperService.isInstanceRunning(message.getEC2InstanceId())).thenReturn(true);
        when(action.execute(message.getEC2InstanceId())).thenReturn(false);
        
        boolean result = handler.handleEvent(messageContent);
        
        assertThat(result, equalTo(false));
        verify(action, times(1)).execute(message.getEC2InstanceId());
    }

}
