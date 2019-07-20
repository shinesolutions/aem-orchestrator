package com.shinesolutions.aemorchestrator.service;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.util.ReflectionTestUtils.setField;

import java.io.IOException;

import org.apache.http.client.ClientProtocolException;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

import com.shinesolutions.aemorchestrator.service.AemInstanceHelperService;
import com.shinesolutions.aemorchestrator.service.ResourceReadyChecker;

@RunWith(MockitoJUnitRunner.class)
public class ResourceReadyCheckerTest {
    
    @Mock
    private AemInstanceHelperService aemInstanceHelperService;
    
    @InjectMocks
    private ResourceReadyChecker startupManager;

    @Before
    public void setUp() throws Exception {
        setFields(1, 1, 1, 1);
    }

    @Test
    public void testSuccess() throws Exception {
        when(aemInstanceHelperService.isAuthorElbHealthy()).thenReturn(true);

        boolean result = startupManager.isResourcesReady();
        
        assertThat(result, equalTo(true));
    }
    
    @Test
    public void testElbNotHealthyStateOneRetry() throws Exception {
        when(aemInstanceHelperService.isAuthorElbHealthy()).thenReturn(false);
        
        boolean result = startupManager.isResourcesReady();
        
        assertThat(result, equalTo(false));
    }
    
    @Test
    public void testManyFailsThenSucceed() throws Exception {
        when(aemInstanceHelperService.isAuthorElbHealthy()).thenReturn(false,false,false,true);
        
        setFields(5, 1, 1, 1);
        
        boolean result = startupManager.isResourcesReady();
        
        assertThat(result, equalTo(true));
    }
    
    @Test
    public void testElbNotHealthyStateManyRetries() throws Exception {
        int numberOfRetries = 5;
        when(aemInstanceHelperService.isAuthorElbHealthy()).thenReturn(false);
        
        setFields(numberOfRetries, 1, 1, 1);
        
        boolean result = startupManager.isResourcesReady();
        
        verify(aemInstanceHelperService, times(numberOfRetries)).isAuthorElbHealthy();
        
        assertThat(result, equalTo(false));
    }
    
    @Test
    public void testHealthCheckThrowsException() throws Exception {
        
        when(aemInstanceHelperService.isAuthorElbHealthy()).thenThrow(new IOException());

        boolean result = startupManager.isResourcesReady();
        
        assertThat(result, equalTo(false));
    }
    
    @Test
    public void testManyReturnOptionsBeforeSuccess() throws Exception {
        
        when(aemInstanceHelperService.isAuthorElbHealthy()).thenThrow(new IOException())
            .thenThrow(new ClientProtocolException()).thenReturn(false).thenReturn(true);
        
        setFields(5, 1, 1, 1);

        boolean result = startupManager.isResourcesReady();
        
        assertThat(result, equalTo(true));
    }
    
    private void setFields(int waitForAuthorElbMaxAttempts, long waitForAuthorBackOffPeriod, 
                           long waitForAuthorMaxBackOffPeriod, long waitForAuthorBackOffPeriodMultiplier) {
        setField(startupManager, "waitForAuthorElbMaxAttempts", waitForAuthorElbMaxAttempts);
        setField(startupManager, "waitForAuthorBackOffPeriod", waitForAuthorBackOffPeriod);
        setField(startupManager, "waitForAuthorMaxBackOffPeriod", waitForAuthorMaxBackOffPeriod);
        setField(startupManager, "waitForAuthorBackOffPeriodMultiplier", waitForAuthorBackOffPeriodMultiplier);
    }

}
