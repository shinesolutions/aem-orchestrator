package com.shinesolutions.aemorchestrator.util;

import org.springframework.stereotype.Component;

import com.shinesolutions.aemorchestrator.model.SnsMessage;

@Component
public class SnsMessageExtractor extends MessageExtractor<SnsMessage> {

    public SnsMessageExtractor() {
        super(SnsMessage.class);
    }

}
