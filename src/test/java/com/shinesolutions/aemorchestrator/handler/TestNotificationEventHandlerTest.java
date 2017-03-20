package com.shinesolutions.aemorchestrator.handler;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;

import org.junit.Before;
import org.junit.Test;

public class TestNotificationEventHandlerTest {
    
    private TestNotificationEventHandler handler;

    @Before
    public void setUp() throws Exception {
        handler = new TestNotificationEventHandler();
    }

    @Test
    public void testHandleEventSuccess() {
        boolean result = handler.handleEvent("Some text");
        assertThat(result, equalTo(true));
    }

}
