package com.shinesolutions.aemorchestrator;

import org.junit.Ignore;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit4.SpringRunner;

@RunWith(SpringRunner.class)
@SpringBootTest
@ActiveProfiles("test")
@Ignore //Remove to test that the spring wiring is working
public class AemOrchestratorTest {

    @Test
    public void test() {
        //Tests that the spring wiring is all correct
    }

}
