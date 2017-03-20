package com.shinesolutions.aemorchestrator.actions;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import com.shinesolutions.aemorchestrator.service.AwsHelperService;

@RunWith(MockitoJUnitRunner.class)
public class AlarmContentHealthCheckActionTest {
    
    @Mock
    private AwsHelperService awsHelperService;
    
    @InjectMocks
    private AlarmContentHealthCheckAction alarmContentHealthCheckAction;

    private String instanceId;
    
    @Before
    public void setUp() throws Exception {
        instanceId = "i-704262407";
    }

    @Test
    public void testSuccess() {
        boolean result = alarmContentHealthCheckAction.execute(instanceId);
        
        //Insure it terminates the publish instance
        verify(awsHelperService, times(1)).terminateInstance(instanceId);
        
        assertThat(result, equalTo(true));
    }
    
    @Test
    public void testWithException() {
        doThrow(new RuntimeException()).when(awsHelperService).terminateInstance(instanceId);
        
        boolean result = alarmContentHealthCheckAction.execute(instanceId);
        
        assertThat(result, equalTo(true));
    }

}
