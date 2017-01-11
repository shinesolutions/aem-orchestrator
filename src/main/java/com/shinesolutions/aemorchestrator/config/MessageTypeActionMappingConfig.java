package com.shinesolutions.aemorchestrator.config;

import java.util.HashMap;
import java.util.Map;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class MessageTypeActionMappingConfig {

	@Bean
	public Map<String, String> messageTypeActionMapping() {
		Map <String, String> mappings = new HashMap<String, String>() {{
		     put("create-replication-agent", "CreateReplicationAgent");
		     put("delete-replication-agent", "DeleteReplicationAgent");
		     put("pause-replication-agent", "PauseReplicationAgent");
		     put("resume-replication-agent", "ResumeReplicationAgent");
		     put("create-flush-agent", "CreateFlushAgent");
		     put("delete-flush-agent", "DeleteFlushAgent");
		}};
		
		return mappings;
	}
	
}
