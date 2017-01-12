package com.shinesolutions.aemorchestrator.handler;

import com.shinesolutions.aemorchestrator.model.EventMessage;

public interface EventHandler {

    void handleEvent(EventMessage message);

}
