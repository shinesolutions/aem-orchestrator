package com.shinesolutions.aemorchestrator.actions;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import com.shinesolutions.aemorchestrator.aem.AgentRunMode;
import com.shinesolutions.aemorchestrator.aem.FlushAgentManager;
import com.shinesolutions.aemorchestrator.service.AemInstanceHelperService;
import com.shinesolutions.swaggeraem4j.ApiException;

@RunWith(MockitoJUnitRunner.class)
public class ScaleDownAuthorDispatcherActionTest {

    @Mock
    private FlushAgentManager flushAgentManager;
    
    @Mock
    private AemInstanceHelperService aemHelperService;
    
    @InjectMocks
    private ScaleDownAuthorDispatcherAction action;
    
    private String aemBasePath;
    private String instanceId;
    
    @Before
    public void setUp() throws Exception {
        instanceId = "instanceId";
        aemBasePath = "aemBasePath";
        
        when(aemHelperService.getAemUrlForAuthorElb()).thenReturn(aemBasePath);
    }
    
    @Test
    public void testDeleteFlushAgentSuccess() throws Exception {
        boolean success = action.execute(instanceId);
        
        verify(flushAgentManager, times(1)).deleteFlushAgent(instanceId, aemBasePath, AgentRunMode.AUTHOR);
        
        assertThat(success, equalTo(true));
    }
    
    @Test
    public void testHandlesException()  throws Exception {
        doThrow(new ApiException()).when(flushAgentManager).deleteFlushAgent(instanceId, aemBasePath, AgentRunMode.AUTHOR);
        
        boolean success = action.execute(instanceId);
        
        assertThat(success, equalTo(false));
    }
    
    
}
