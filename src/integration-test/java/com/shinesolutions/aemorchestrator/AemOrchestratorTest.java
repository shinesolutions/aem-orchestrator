package com.shinesolutions.aemorchestrator;

import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.util.Queue;

import javax.annotation.Resource;
import javax.jms.Message;
import javax.jms.MessageConsumer;

import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;

@RunWith(SpringRunner.class)
@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@Ignore
public class AemOrchestratorTest {
    
    @Autowired
    private MockMvc mvc;

    @Resource
    private MessageConsumer mockMessageConsumer;
    
    private Queue<Message> mockMessageQueue;
    
    
    @Before
    @SuppressWarnings("unchecked")
    public void setup() throws Exception {
        mockMessageQueue = (Queue<Message>)mockMessageConsumer;

    }
    
    @Test
    public void testHealthCheck() throws Exception {
        //Tests that the spring wiring is all correct
        mvc.perform(MockMvcRequestBuilders.get("/health").accept(MediaType.APPLICATION_JSON))
        .andExpect(status().isOk());
    }
    
    @Test
    public void receiveNullMessage() throws Exception {
        mockMessageQueue.add(null);
    }

}
