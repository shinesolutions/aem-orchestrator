package com.shinesolutions.aemorchestrator.service;

import javax.annotation.Resource;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.retry.backoff.FixedBackOffPolicy;
import org.springframework.retry.policy.SimpleRetryPolicy;
import org.springframework.retry.support.RetryTemplate;
import org.springframework.stereotype.Component;

import com.shinesolutions.aemorchestrator.AemOrchestrator;

@Component
public class ResourceReadyChecker {
    
    private final static Logger logger = LoggerFactory.getLogger(AemOrchestrator.class);
    
    @Value("${startup.waitForAuthorElb.maxAttempts}")
    private int waitForAuthorElbMaxAttempts;
    
    @Value("${startup.waitForAuthorElb.backOffPeriod}")
    private long waitForAuthorBackOffPeriod;

    @Resource
    private AemInstanceHelperService aemInstanceHelperService;
    
    /**
     * Ensures external dependencies are available before starting to read messages from the queue
     * @return true if startup successful, false if not
     */
    public boolean isResourcesReady() {
        boolean isStartupOk = false;
        
        FixedBackOffPolicy backOffPolicy = new FixedBackOffPolicy();
        backOffPolicy.setBackOffPeriod(waitForAuthorBackOffPeriod);

        SimpleRetryPolicy retryPolicy = new SimpleRetryPolicy();
        retryPolicy.setMaxAttempts(waitForAuthorElbMaxAttempts);

        RetryTemplate retryTemplate = new RetryTemplate();
        retryTemplate.setRetryPolicy(retryPolicy);
        retryTemplate.setBackOffPolicy(backOffPolicy);
        retryTemplate.setThrowLastExceptionOnExhausted(true);

        logger.info("Waiting for Author ELB to be in healthy state");
        
        try {
            isStartupOk = retryTemplate.execute(c -> {
                boolean result = aemInstanceHelperService.isAuthorElbHealthy();
                if(!result) //Needs to be healthy
                    throw new Exception("Author ELB does not appear to be in a healthy state");
                return result;
            });
        } catch (Exception e) {
            logger.error("Failed to receive healthy state from Author ELB", e);
        }
        
        return isStartupOk;
    }
    
    
}
