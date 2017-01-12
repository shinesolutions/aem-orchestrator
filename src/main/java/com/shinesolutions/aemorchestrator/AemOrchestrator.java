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

import javax.annotation.Resource;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.ComponentScan;

import com.shinesolutions.aemorchestrator.service.MessageReceiver;

@SpringBootApplication
@ComponentScan
public class AemOrchestrator implements CommandLineRunner {

    @Resource
    private MessageReceiver messageReceiver;

    private final Logger logger = LoggerFactory.getLogger(this.getClass());

    public static void main(String[] args) throws Exception {
        SpringApplication.run(AemOrchestrator.class, args);
    }

    public void run(String... arg0) throws Exception {
        logger.info("AEM Orchestrator is running");
        
        //Begin polling for messages on the queue
        messageReceiver.receiveMessages();
    }

}
