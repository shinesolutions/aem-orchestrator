package com.shinesolutions.aemorchestrator;

import java.util.Queue;

import javax.annotation.Resource;
import javax.jms.Message;
import javax.jms.MessageConsumer;

import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit4.SpringRunner;

@RunWith(SpringRunner.class)
@SpringBootTest
@ActiveProfiles("test")
@Ignore
public class AemOrchestratorTest {
    
    @Value("${aws.autoscale.group.name.publisherDispatcher}")
    private String publisherDispatcherGroupName;

    @Value("${aws.autoscale.group.name.publisher}")
    private String publisherGroupName;

    @Value("${aws.autoscale.group.name.authorDispatcher}")
    private String authorDispatcherGroupName;
    
    @Resource
    private ThreadPoolTaskExecutor taskExecutor;

    @Resource
    private MessageConsumer mockMessageConsumer;
    
    private Queue<Message> mockMessageQueue;
    
    
    @Before
    @SuppressWarnings("unchecked")
    public void setup() throws Exception {
        mockMessageQueue = (Queue<Message>)mockMessageConsumer;

    }
    
    @Test
    public void testScaleDownAuthorDispatcher() {
        //Tests that the spring wiring is all correct
    }

}
