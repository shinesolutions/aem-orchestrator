package com.shinesolutions.aemorchestrator.handler;

import javax.jms.Message;

public interface MessageHandler {
    
    boolean handleMessage(Message message);
    
}
