package com.shinesolutions.aemorchestrator.config;

import java.util.LinkedList;
import java.util.Queue;

import javax.jms.JMSException;
import javax.jms.Message;
import javax.jms.MessageConsumer;
import javax.jms.MessageListener;

public class MockMessageConsumer extends LinkedList<Message> implements MessageConsumer, Queue<Message> {

    private static final long serialVersionUID = 1L;

    @Override
    public Message receive() throws JMSException {
        return this.poll();
    }

    @Override
    public Message receive(long timeout) throws JMSException {
        if (this.isEmpty()) {
            try {
                Thread.sleep(timeout);
            } catch (InterruptedException e) {
                System.err.println(e);
                throw new JMSException("Mock queue threading error occured");
            }
        }
        return this.poll();
    }

    @Override
    public Message receiveNoWait() throws JMSException {
        return this.poll();
    }

    /*
     * Unused mocked methods
     */

    @Override
    public void close() throws JMSException {
    }

    @Override
    public String getMessageSelector() throws JMSException {
        return null;
    }

    @Override
    public MessageListener getMessageListener() throws JMSException {
        return null;
    }

    @Override
    public void setMessageListener(MessageListener listener) throws JMSException {
    }

}
