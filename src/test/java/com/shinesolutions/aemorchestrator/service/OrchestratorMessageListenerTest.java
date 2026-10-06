package com.shinesolutions.aemorchestrator.service;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import jakarta.jms.Message;
import jakarta.jms.MessageConsumer;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.amazon.sqs.javamessaging.SQSConnection;

import com.shinesolutions.aemorchestrator.handler.SqsMessageHandler;
import static org.mockito.Mockito.lenient;

@ExtendWith(MockitoExtension.class)
class OrchestratorMessageListenerTest {

    @Mock
    private SQSConnection connection;

    @Mock
    private MessageConsumer consumer;

    @Mock
    private SqsMessageHandler messageHandler;

    @InjectMocks
    private OrchestratorMessageListener messageReceiver;

    private Message message;

    @BeforeEach
    void setUp() throws Exception {
        message = mock(Message.class);
        lenient().when(message.getJMSMessageID()).thenReturn("test1234");
    }

    @Test
    void testReceiveNullMessage() {
        assertDoesNotThrow(() -> messageReceiver.onMessage(null),
                "Should not throw exception on null message");
    }

    @Test
    void testReceiveValidMessageButLeaveOnQueue() throws Exception {
        when(messageHandler.handleMessage(message)).thenReturn(false);

        messageReceiver.onMessage(message);

        verify(message, never()).acknowledge();
        verify(messageHandler, times(1)).handleMessage(message);
    }

    @Test
    void testReceiveValidMessageAndRemoveFromQueue() throws Exception {
        when(messageHandler.handleMessage(message)).thenReturn(true);

        messageReceiver.onMessage(message);

        verify(message, times(1)).acknowledge();
        verify(messageHandler, times(1)).handleMessage(message);
    }

    @Test
    void testReceiveValidMessageHandlerThrowsException() throws Exception {
        when(messageHandler.handleMessage(message)).thenThrow(new RuntimeException("Test"));

        assertDoesNotThrow(() -> messageReceiver.onMessage(message),
                "Should not throw exception if handler throws exception");

        verify(message, never()).acknowledge();
        verify(messageHandler, times(1)).handleMessage(message);
    }

    @Test
    void testStart() throws Exception {
        messageReceiver.start();

        verify(consumer, times(1)).setMessageListener(messageReceiver);
        verify(connection, times(1)).start();
    }

    @Test
    void testCleanUp() throws Exception {
        messageReceiver.cleanUp();

        verify(connection, times(1)).stop();
    }
}