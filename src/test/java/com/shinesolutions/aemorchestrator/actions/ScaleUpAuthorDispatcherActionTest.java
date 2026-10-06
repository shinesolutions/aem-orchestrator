package com.shinesolutions.aemorchestrator.actions;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.shinesolutions.aemorchestrator.aem.AgentRunMode;
import com.shinesolutions.aemorchestrator.aem.FlushAgentManager;
import com.shinesolutions.aemorchestrator.service.AemInstanceHelperService;
import com.shinesolutions.swaggeraem4j.ApiException;
import org.junit.jupiter.api.extension.ExtendWith;

@ExtendWith(MockitoExtension.class)
public class ScaleUpAuthorDispatcherActionTest {
    
    @Mock
    private FlushAgentManager flushAgentManager;

    @Mock
    private AemInstanceHelperService aemHelperService;
    
    @InjectMocks
    private ScaleUpAuthorDispatcherAction action;
    
    private String instanceId;
    private String authDispatcherAemBaseUrl;
    private String authElbAemBaseUrl;

    @BeforeEach
    public void setUp() throws Exception {
        instanceId = "instanceId";
        authDispatcherAemBaseUrl = "authDispatcherAemBaseUrl";
        authElbAemBaseUrl = "authElbAemBaseUrl";
        
        when(aemHelperService.getAemUrlForAuthorDispatcher(instanceId)).thenReturn(authDispatcherAemBaseUrl);
        when(aemHelperService.getAemUrlForAuthorElb()).thenReturn(authElbAemBaseUrl);
    }

    @Test
    public void testCreateFlushAgentAndAddTags() throws Exception {
        boolean success = action.execute(instanceId);
        
        verify(flushAgentManager, times(1)).createFlushAgent(instanceId, authElbAemBaseUrl, 
            authDispatcherAemBaseUrl, AgentRunMode.AUTHOR);
        
        verify(aemHelperService, times(1)).tagAuthorDispatcherWithAuthorHost(instanceId);
        
        assertThat(success, equalTo(true));
    }
    
    @Test
    public void testHandlesExceptionOnFlushAgentCreation() throws Exception {
        doThrow(new ApiException()).when(flushAgentManager).createFlushAgent(instanceId, authElbAemBaseUrl, 
            authDispatcherAemBaseUrl, AgentRunMode.AUTHOR);
        
        boolean success = action.execute(instanceId);
        
        verify(aemHelperService, times(0)).tagAuthorDispatcherWithAuthorHost(instanceId);
        
        assertThat(success, equalTo(false));
    }
    
    @Test
    public void testHandlesExceptionOnTagCreation() throws Exception {
        doThrow(new RuntimeException()).when(aemHelperService).tagAuthorDispatcherWithAuthorHost(instanceId);
        
        boolean success = action.execute(instanceId);
        
        verify(flushAgentManager, times(1)).createFlushAgent(instanceId, authElbAemBaseUrl, 
            authDispatcherAemBaseUrl, AgentRunMode.AUTHOR);
        
        assertThat(success, equalTo(false));
    }

}
