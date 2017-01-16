package com.shinesolutions.aemorchestrator;

import javax.annotation.Resource;
import javax.jms.MessageConsumer;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit4.SpringRunner;

@RunWith(SpringRunner.class)
@SpringBootTest
@ActiveProfiles("test")
public class AemOrchestratorTest {

    @Resource
    private MessageConsumer mockMessageConsumer;
    
    @Test
    public void test() {
        //Tests that the spring wiring is all correct
    }

}
