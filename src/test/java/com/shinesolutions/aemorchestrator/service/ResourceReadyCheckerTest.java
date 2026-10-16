package com.shinesolutions.aemorchestrator.service;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.util.ReflectionTestUtils.setField;

import java.io.IOException;
import org.apache.hc.client5.http.ClientProtocolException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class ResourceReadyCheckerTest {

  @Mock private AemInstanceHelperService aemInstanceHelperService;

  @InjectMocks private ResourceReadyChecker startupManager;

  @BeforeEach
  void setUp() throws Exception {
    setFields(1, 1, 1, 1);
  }

  @Test
  void testSuccess() throws Exception {
    when(aemInstanceHelperService.isAuthorElbHealthy()).thenReturn(true);

    boolean result = startupManager.isResourcesReady();

    assertThat(result, equalTo(true));
  }

  @Test
  void testElbNotHealthyStateOneRetry() throws Exception {
    when(aemInstanceHelperService.isAuthorElbHealthy()).thenReturn(false);

    boolean result = startupManager.isResourcesReady();

    assertThat(result, equalTo(false));
  }

  @Test
  void testManyFailsThenSucceed() throws Exception {
    when(aemInstanceHelperService.isAuthorElbHealthy()).thenReturn(false, false, false, true);

    setFields(5, 1, 1, 1);

    boolean result = startupManager.isResourcesReady();

    assertThat(result, equalTo(true));
  }

  @Test
  void testElbNotHealthyStateManyRetries() throws Exception {
    int numberOfRetries = 5;
    when(aemInstanceHelperService.isAuthorElbHealthy()).thenReturn(false);

    setFields(numberOfRetries, 1, 1, 1);

    boolean result = startupManager.isResourcesReady();

    verify(aemInstanceHelperService, times(numberOfRetries)).isAuthorElbHealthy();

    assertThat(result, equalTo(false));
  }

  @Test
  void testHealthCheckThrowsException() throws Exception {

    when(aemInstanceHelperService.isAuthorElbHealthy()).thenThrow(new IOException());

    boolean result = startupManager.isResourcesReady();

    assertThat(result, equalTo(false));
  }

  @Test
  void testManyReturnOptionsBeforeSuccess() throws Exception {

    when(aemInstanceHelperService.isAuthorElbHealthy())
        .thenThrow(new IOException())
        .thenThrow(new ClientProtocolException())
        .thenReturn(false)
        .thenReturn(true);

    setFields(5, 1, 1, 1);

    boolean result = startupManager.isResourcesReady();

    assertThat(result, equalTo(true));
  }

  private void setFields(
      int waitForAuthorElbMaxAttempts,
      long waitForAuthorBackOffPeriod,
      long waitForAuthorMaxBackOffPeriod,
      long waitForAuthorBackOffPeriodMultiplier) {
    setField(startupManager, "waitForAuthorElbMaxAttempts", waitForAuthorElbMaxAttempts);
    setField(startupManager, "waitForAuthorBackOffPeriod", waitForAuthorBackOffPeriod);
    setField(startupManager, "waitForAuthorMaxBackOffPeriod", waitForAuthorMaxBackOffPeriod);
    setField(
        startupManager,
        "waitForAuthorBackOffPeriodMultiplier",
        waitForAuthorBackOffPeriodMultiplier);
  }
}
