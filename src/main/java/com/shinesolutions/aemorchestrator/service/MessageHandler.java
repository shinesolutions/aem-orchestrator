package com.shinesolutions.aemorchestrator.service;

import com.amazonaws.services.sqs.model.Message;

public interface MessageHandler {

	boolean handleMessage(Message message);
}
