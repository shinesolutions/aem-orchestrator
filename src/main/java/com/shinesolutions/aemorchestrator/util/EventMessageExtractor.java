package com.shinesolutions.aemorchestrator.util;

import org.springframework.stereotype.Component;

import com.shinesolutions.aemorchestrator.model.EventMessage;

@Component
public class EventMessageExtractor extends MessageExtractor<EventMessage> {

    public EventMessageExtractor() {
        super(EventMessage.class);
    }

}
