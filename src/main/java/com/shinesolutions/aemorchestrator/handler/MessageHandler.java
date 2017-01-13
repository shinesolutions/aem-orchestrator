package com.shinesolutions.aemorchestrator.handler;

import com.amazonaws.services.sqs.model.Message;

public interface MessageHandler {

	boolean handleMessage(Message message);
}
