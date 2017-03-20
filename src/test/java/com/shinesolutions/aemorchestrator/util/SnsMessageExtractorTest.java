package com.shinesolutions.aemorchestrator.util;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;

import java.io.File;
import java.util.Scanner;

import org.junit.Before;
import org.junit.Test;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.shinesolutions.aemorchestrator.model.SnsMessage;

public class SnsMessageExtractorTest {
    
    private SnsMessageExtractor extractor;

    @Before
    public void setUp() throws Exception {
        extractor = new SnsMessageExtractor();
    }

    @Test
    @SuppressWarnings("resource")
    public void testExtractEventMessage() throws Exception {
        File sampleFileMessageOnly = new File(getClass().getResource("/sample-sqs-event-message-2.json").getFile());
        String sampleFileContent = new Scanner(sampleFileMessageOnly).useDelimiter("\\Z").next();

        ObjectMapper mapper = new ObjectMapper();
        JsonNode root = mapper.readTree(sampleFileMessageOnly);
        
        SnsMessage message = extractor.extractMessage(sampleFileContent);
        
        assertThat(message.getType(), equalTo(root.path("Type").asText()));
        assertThat(message.getMessageId(), equalTo(root.path("MessageId").asText()));
        assertThat(message.getTopicArn(), equalTo(root.path("TopicArn").asText()));
        assertThat(message.getSubject(), equalTo(root.path("Subject").asText()));
        assertThat(message.getMessage(), equalTo(root.path("Message").asText()));
        assertThat(message.getTimestamp(), equalTo(root.path("Timestamp").asText()));
        assertThat(message.getSignatureVersion(), equalTo(root.path("SignatureVersion").asText()));
        assertThat(message.getSignature(), equalTo(root.path("Signature").asText()));
        assertThat(message.getSigningCertURL(), equalTo(root.path("SigningCertURL").asText()));
        assertThat(message.getUnsubscribeURL(), equalTo(root.path("UnsubscribeURL").asText()));
        
    }

}
