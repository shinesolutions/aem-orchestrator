package com.shinesolutions.aemorchestrator.handler;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;

import org.junit.Before;
import org.junit.Test;

import com.shinesolutions.aemorchestrator.model.EventMessage;

public class TestNotificationEventHandlerTest {
    
    private TestNotificationEventHandler handler;
    private EventMessage message;

    @Before
    public void setUp() throws Exception {
        handler = new TestNotificationEventHandler();
        message = new EventMessage();
        message.setAutoScalingGroupName("testGroup");
        message.setEC2InstanceId("test-instance");
    }

    @Test
    public void testHandleEventSuccess() {
        boolean result = handler.handleEvent(message);
        assertThat(result, equalTo(true));
    }

}
