package com.shinesolutions.aemorchestrator.util;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.io.File;
import java.util.Scanner;

import javax.jms.Message;
import javax.jms.TextMessage;

public class MockMessageUtil {

    @SuppressWarnings("resource")
    public static Message createMockMessageFor(String eventType, String autoScaleGroupName) throws Exception {
        // Read in file containing test example of message body
        File sampleFile = new File(MockMessageUtil.class.getResource("").getFile());
        String sampleFileContent = new Scanner(sampleFile).useDelimiter("\\Z").next();

        TextMessage textMessage = mock(TextMessage.class);
        when(textMessage.getText()).thenReturn(sampleFileContent);
        return textMessage;
    }
}
