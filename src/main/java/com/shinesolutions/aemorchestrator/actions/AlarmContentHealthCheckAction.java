package com.shinesolutions.aemorchestrator.actions;

import javax.annotation.Resource;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import com.shinesolutions.aemorchestrator.service.AwsHelperService;

@Component
public class AlarmContentHealthCheckAction implements Action {

    private final Logger logger = LoggerFactory.getLogger(this.getClass());

    @Value("${alarm.content.health.check.terminate.instance.enable}")
    private boolean terminateInstanceEnable;


    @Resource
    private AwsHelperService awsHelperService;

    /*
     * Expects the instanceId to be the Publish instance ID
     */
    @Override
    public boolean execute(String instanceId) {
        logger.info("Executing AlarmContentHealthCheckAction");

        try {

            if(terminateInstanceEnable) {
                terminate(instanceId);
            }else {
                notify(instanceId);
            }

        } catch (Exception e) {
            logger.error("Unable to terminate publish instance: " + awsHelperService + ". It may not be running?");
        }

        return true;
    }

    // Publish instance is in an unhealthy state, terminate it
    private void terminate(String instanceId) {
        awsHelperService.terminateInstance(instanceId);
        logger.info("Terminated publish instance " + instanceId);
    }

    // Publish instance is in an unhealthy state, notify
    private void notify(String instanceId) {
        String msg = String.format("Publish instance %s is in an unhealthy state", instanceId);
        logger.error(msg);
    }
}
