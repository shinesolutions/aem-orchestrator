package com.shinesolutions.aemorchestrator.util;

import org.springframework.stereotype.Component;

import com.shinesolutions.aemorchestrator.model.AlarmMessage;

@Component
public class AlarmMessageExtractor extends MessageExtractor<AlarmMessage> {

    public AlarmMessageExtractor() {
        super(AlarmMessage.class);
    }

}
