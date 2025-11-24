package com.job.radar.config;

import com.job.radar.model.enums.statemachine.state.FormState;
import com.job.radar.service.processor.field.ResumeFieldProcessor;
import lombok.AllArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@AllArgsConstructor
@Configuration
public class FieldProcessorRegistry {
    private final List<ResumeFieldProcessor> resumeFieldProcessors;

    @Bean
    public Map<FormState, ResumeFieldProcessor> resumeFieldProcessorMap() {
        Map<FormState, ResumeFieldProcessor> map = new HashMap<>();
        for (ResumeFieldProcessor resumeFieldProcessor : resumeFieldProcessors) {
            map.put(resumeFieldProcessor.getCurrentState(), resumeFieldProcessor);
        }
        return map;
    }
}
