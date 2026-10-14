package com.shinesolutions.aemorchestrator.util;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;

import com.shinesolutions.aemorchestrator.model.SnsMessage;
import java.io.File;
import java.util.Scanner;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import tools.jackson.databind.JsonNode;
import tools.jackson.databind.ObjectMapper;

class SnsMessageExtractorTest {

  private SnsMessageExtractor extractor;

  @BeforeEach
  void setUp() throws Exception {
    extractor = new SnsMessageExtractor();
  }

  @Test
  @SuppressWarnings("resource")
  void testExtractEventMessage() throws Exception {
    File sampleFileMessageOnly =
        new File(getClass().getResource("/sample-sqs-event-message-2.json").getFile());
    String sampleFileContent = new Scanner(sampleFileMessageOnly).useDelimiter("\\Z").next();

    ObjectMapper mapper = new ObjectMapper();
    JsonNode root = mapper.readTree(sampleFileMessageOnly);

    SnsMessage message = extractor.extractMessage(sampleFileContent);

    assertThat(message.getType(), equalTo(root.path("Type").asString()));
    assertThat(message.getMessageId(), equalTo(root.path("MessageId").asString()));
    assertThat(message.getTopicArn(), equalTo(root.path("TopicArn").asString()));
    assertThat(message.getSubject(), equalTo(root.path("Subject").asString()));
    assertThat(message.getMessage(), equalTo(root.path("Message").asString()));
    assertThat(message.getTimestamp(), equalTo(root.path("Timestamp").asString()));
    assertThat(message.getSignatureVersion(), equalTo(root.path("SignatureVersion").asString()));
    assertThat(message.getSignature(), equalTo(root.path("Signature").asString()));
    assertThat(message.getSigningCertUrl(), equalTo(root.path("SigningCertURL").asString()));
    assertThat(message.getUnsubscribeUrl(), equalTo(root.path("UnsubscribeURL").asString()));
  }
}
