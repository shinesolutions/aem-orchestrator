package com.shinesolutions.aemorchestrator.actions;

import javax.annotation.Resource;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import com.shinesolutions.aemorchestrator.service.AwsHelperService;

@Component
public class AlarmContentHealthCheckAction implements Action {

    private final Logger logger = LoggerFactory.getLogger(this.getClass());

    @Resource
    private AwsHelperService awsHelperService;

    /*
     * Expects the instanceId to be the Publish instance ID
     */
    @Override
    public boolean execute(String instanceId) {
        logger.info("Executing AlarmContentHealthCheckAction");

        try {
            // Publish instance is in an unhealthy sate, so terminate it
            awsHelperService.terminateInstance(instanceId);
            logger.info("Terminated publish instance " + instanceId);
        } catch (Exception e) {
            logger.error("Unable to terminate publish instance: " + awsHelperService + ". It may not be running?");
        }

        return true;
    }

}
