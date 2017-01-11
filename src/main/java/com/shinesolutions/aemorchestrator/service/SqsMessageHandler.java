package com.shinesolutions.aemorchestrator.service;

import com.amazonaws.services.sqs.model.Message;

public interface SqsMessageHandler {

	void handleMessgae(Message message);
}
