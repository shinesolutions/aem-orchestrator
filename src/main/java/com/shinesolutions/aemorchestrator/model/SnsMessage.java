package com.shinesolutions.aemorchestrator.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * SNS notification received as the body of an SQS message.
 *
 * <p>Its {@code Subject} selects the message handler and its {@code Message} carries the event
 * payload as a JSON string; unknown fields are ignored.
 */
@JsonIgnoreProperties(ignoreUnknown = true)
public class SnsMessage {

  @JsonProperty("Type")
  private String type;

  @JsonProperty("MessageId")
  private String messageId;

  @JsonProperty("TopicArn")
  private String topicArn;

  @JsonProperty("Subject")
  private String subject;

  @JsonProperty("Message")
  private String message;

  @JsonProperty("Timestamp")
  private String timestamp;

  @JsonProperty("SignatureVersion")
  private String signatureVersion;

  @JsonProperty("Signature")
  private String signature;

  @JsonProperty("SigningCertURL")
  private String signingCertUrl;

  @JsonProperty("UnsubscribeURL")
  private String unsubscribeUrl;

  public String getType() {
    return type;
  }

  public void setType(String type) {
    this.type = type;
  }

  public String getMessageId() {
    return messageId;
  }

  public void setMessageId(String messageId) {
    this.messageId = messageId;
  }

  public String getTopicArn() {
    return topicArn;
  }

  public void setTopicArn(String topicArn) {
    this.topicArn = topicArn;
  }

  public String getSubject() {
    return subject;
  }

  public void setSubject(String subject) {
    this.subject = subject;
  }

  public String getMessage() {
    return message;
  }

  public void setMessage(String message) {
    this.message = message;
  }

  public String getTimestamp() {
    return timestamp;
  }

  public void setTimestamp(String timestamp) {
    this.timestamp = timestamp;
  }

  public String getSignatureVersion() {
    return signatureVersion;
  }

  public void setSignatureVersion(String signatureVersion) {
    this.signatureVersion = signatureVersion;
  }

  public String getSignature() {
    return signature;
  }

  public void setSignature(String signature) {
    this.signature = signature;
  }

  public String getSigningCertUrl() {
    return signingCertUrl;
  }

  public void setSigningCertUrl(String signingCertUrl) {
    this.signingCertUrl = signingCertUrl;
  }

  public String getUnsubscribeUrl() {
    return unsubscribeUrl;
  }

  public void setUnsubscribeUrl(String unsubscribeUrl) {
    this.unsubscribeUrl = unsubscribeUrl;
  }
}
