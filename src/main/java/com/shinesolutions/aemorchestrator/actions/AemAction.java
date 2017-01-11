package com.shinesolutions.aemorchestrator.actions;

import com.amazonaws.services.sqs.model.Message;

public interface AemAction {

	void execute(Message message);
	
}
