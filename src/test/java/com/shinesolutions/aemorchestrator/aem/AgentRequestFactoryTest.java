package com.shinesolutions.aemorchestrator.aem;

import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import com.shinesolutions.aemorchestrator.aem.AgentRequestFactory;

@RunWith(MockitoJUnitRunner.class)
public class AgentRequestFactoryTest {

  @Test
  public void testGetPauseReplicationAgentRequest() {

    PostAgentWithHttpInfoRequest request = new PostAgentWithHttpInfoRequest();
    PostAgentWithHttpInfoRequest spy = spy(request);

    when(spy.withRunMode(AgentRunMode.AUTHOR.getValue())).thenReturn(request);
    verify(spy, times(1)).withRunMode(AgentRunMode.AUTHOR.getValue());

    when(spy.withName("some-agent")).thenReturn(request);
    verify(spy, times(1)).withName("some-agent");

    when(spy.withJcrPrimaryType("cq:Page")).thenReturn(request);
    verify(spy, times(1)).withJcrPrimaryType("cq:Page");

    when(spy.withJcrContentJcrTitle("some-agent")).thenReturn(request);
    verify(spy, times(1)).withJcrContentJcrTitle("some-agent");

    when(spy.withJcrContentSlingResourceType("cq/replication/components/agent")).thenReturn(request);
    verify(spy, times(1)).withJcrContentSlingResourceType("cq/replication/components/agent");

    when(spy.withJcrContentTransportUser("orchestrator-pause")).thenReturn(request);
    verify(spy, times(1)).withJcrContentTransportUser("orchestrator-pause");

    when(spy.withJcrContentCqTemplate("/libs/cq/replication/templates/agent")).thenReturn(request);
    verify(spy, times(1)).withJcrContentCqTemplate("/libs/cq/replication/templates/agent");

    when(spy.withJcrContentEnabled(true)).thenReturn(request);
    verify(spy, times(1)).withJcrContentEnabled(true);

    AgentRequestFactory factory = new AgentRequestFactory(request);
    factory.getPauseReplicationAgentRequest(AgentRunMode.AUTHOR, "some-agent");
  }
}
