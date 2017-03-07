/*
 * Copyright 2017 Shine Solutions
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.shinesolutions.aemorchestrator;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.retry.annotation.EnableRetry;

import com.shinesolutions.aemorchestrator.service.MessageReceiver;
import com.shinesolutions.aemorchestrator.service.ResourceReadyChecker;

@SpringBootApplication
@ComponentScan
@EnableRetry
public class AemOrchestrator {

    private final static Logger logger = LoggerFactory.getLogger(AemOrchestrator.class);

    public static void main(String[] args) throws Exception {
        ConfigurableApplicationContext context = SpringApplication.run(AemOrchestrator.class, args);
        
        //Need to wait for Author ELB is be in a healthy state before reading messages from the SQS queue
        ResourceReadyChecker resourceReadyChecker = context.getBean(ResourceReadyChecker.class);
        
        boolean isStartupOk = false;
        
        if(resourceReadyChecker.isResourcesReady()) {
            MessageReceiver messageReceiver = context.getBean(MessageReceiver.class);
            
            try {
                messageReceiver.start();
                isStartupOk = true;
            } catch (Exception e) {
                logger.error("Failed to start message receiver", e);
            }
        }
        
        if(isStartupOk) {
            logger.info("AEM Orchestrator started");
        } else {
            logger.info("Failed to start AEM Orchestrator");
            context.close(); //Exit the application
        }
    }

}
