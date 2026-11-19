package com.shinesolutions.aemorchestrator.actions;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.shinesolutions.aemorchestrator.service.AemInstanceHelperService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class ScaleUpPublishDispatcherActionTest {

  @Mock private AemInstanceHelperService aemHelperService;

  @InjectMocks private ScaleUpPublishDispatcherAction action;

  private String instanceId;

  @BeforeEach
  void setUp() throws Exception {
    instanceId = "i-4398603686";
  }

  @Test
  void testSameDesiredCapacity() {
    setupAemInstanceHelperServiceGetAutoScalingGroupDesiredAnd1MoreStubs(2, 2);

    boolean success = action.execute(instanceId);

    verify(aemHelperService, times(0)).setAutoScalingGroupDesiredCapacityForPublish(anyInt());

    assertThat(success, equalTo(true));
  }

  @Test
  void testHigherDesiredCapacity() {
    int dispatcherDesiredCapcity = 4;
    setupAemInstanceHelperServiceGetAutoScalingGroupDesiredAnd1MoreStubs(
        dispatcherDesiredCapcity + 1, dispatcherDesiredCapcity);

    boolean success = action.execute(instanceId);

    verify(aemHelperService, times(1))
        .setAutoScalingGroupDesiredCapacityForPublish(dispatcherDesiredCapcity);

    assertThat(success, equalTo(true));
  }

  @Test
  void testLowerDesiredCapacity() {
    int dispatcherDesiredCapcity = 3;
    setupAemInstanceHelperServiceGetAutoScalingGroupDesiredAnd1MoreStubs(
        dispatcherDesiredCapcity - 1, dispatcherDesiredCapcity);

    boolean success = action.execute(instanceId);

    verify(aemHelperService, times(1))
        .setAutoScalingGroupDesiredCapacityForPublish(dispatcherDesiredCapcity);

    assertThat(success, equalTo(true));
  }

  private void setupAemInstanceHelperServiceGetAutoScalingGroupDesiredAnd1MoreStubs(
      int getAutoScalingGroupDesiredCapacityForPublishReturnValue,
      int getAutoScalingGroupDesiredCapacityForPublishDispatcherReturnValue) {
    when(aemHelperService.getAutoScalingGroupDesiredCapacityForPublishDispatcher())
        .thenReturn(getAutoScalingGroupDesiredCapacityForPublishDispatcherReturnValue);
    when(aemHelperService.getAutoScalingGroupDesiredCapacityForPublish())
        .thenReturn(getAutoScalingGroupDesiredCapacityForPublishReturnValue);
  }
}
