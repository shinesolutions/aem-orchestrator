package com.shinesolutions.aemorchestrator;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.mockito.Mockito.doThrow;
import static org.springframework.test.util.ReflectionTestUtils.setField;

import java.io.IOException;

import org.apache.http.client.ClientProtocolException;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import com.shinesolutions.aemorchestrator.service.AemInstanceHelperService;
import com.shinesolutions.aemorchestrator.service.MessageReceiver;

@RunWith(MockitoJUnitRunner.class)
public class StartupManagerTest {
    
    @Mock
    private AemInstanceHelperService aemInstanceHelperService;
    
    @Mock
    private MessageReceiver messageReceiver;
    
    @InjectMocks
    private StartupManager startupManager;

    @Before
    public void setUp() throws Exception {
        setFields(1, 1);
    }

    @Test
    public void testSuccess() throws Exception {
        when(aemInstanceHelperService.isAuthorElbHealthy()).thenReturn(true);

        boolean result = startupManager.isStartupOk();
        
        verify(messageReceiver, times(1)).start();
        
        assertThat(result, equalTo(true));
    }
    
    @Test
    public void testElbNotHealthyStateOneRetry() throws Exception {
        when(aemInstanceHelperService.isAuthorElbHealthy()).thenReturn(false);
        
        boolean result = startupManager.isStartupOk();
        
        verify(messageReceiver, times(0)).start();
        
        assertThat(result, equalTo(false));
    }
    
    @Test
    public void testManyFailsThenSucceed() throws Exception {
        when(aemInstanceHelperService.isAuthorElbHealthy()).thenReturn(false,false,false,true);
        
        setFields(5, 1);
        
        boolean result = startupManager.isStartupOk();
        
        verify(messageReceiver, times(1)).start();
        
        assertThat(result, equalTo(true));
    }
    
    @Test
    public void testElbNotHealthyStateManyRetries() throws Exception {
        int numberOfRetries = 5;
        when(aemInstanceHelperService.isAuthorElbHealthy()).thenReturn(false);
        
        setFields(numberOfRetries, 1);
        
        boolean result = startupManager.isStartupOk();
        
        verify(messageReceiver, times(0)).start();
        verify(aemInstanceHelperService, times(numberOfRetries)).isAuthorElbHealthy();
        
        assertThat(result, equalTo(false));
    }
    
    @Test
    public void testHealthCheckThrowsException() throws Exception {
        
        when(aemInstanceHelperService.isAuthorElbHealthy()).thenThrow(new IOException());

        boolean result = startupManager.isStartupOk();
        
        verify(messageReceiver, times(0)).start();
        
        assertThat(result, equalTo(false));
    }
    
    @Test
    public void testManyReturnOptionsBeforeSuccess() throws Exception {
        
        when(aemInstanceHelperService.isAuthorElbHealthy()).thenThrow(new IOException())
            .thenThrow(new ClientProtocolException()).thenReturn(false).thenReturn(true);
        
        setFields(5, 1);

        boolean result = startupManager.isStartupOk();
        
        verify(messageReceiver, times(1)).start();
        
        assertThat(result, equalTo(true));
    }
    
    @Test
    public void testNotOkWhenMessageReceiverThrowsException() throws Exception {
        
        when(aemInstanceHelperService.isAuthorElbHealthy()).thenReturn(true);
        doThrow(new Exception()).when(messageReceiver).start();

        boolean result = startupManager.isStartupOk();
        
        verify(messageReceiver, times(1)).start();
        
        assertThat(result, equalTo(false));
    }
    
    private void setFields(int waitForAuthorElbMaxAttempts, long waitForAuthorBackOffPeriod) {
        setField(startupManager, "waitForAuthorElbMaxAttempts", waitForAuthorElbMaxAttempts);
        setField(startupManager, "waitForAuthorBackOffPeriod", waitForAuthorBackOffPeriod);
    }

}
