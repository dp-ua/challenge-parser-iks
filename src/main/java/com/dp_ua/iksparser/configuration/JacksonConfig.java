package com.dp_ua.iksparser.configuration;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.converter.json.Jackson2ObjectMapperBuilder;

import com.fasterxml.jackson.databind.SerializationFeature;

@Configuration
public class JacksonConfig {
    @Bean
    public Jackson2ObjectMapperBuilder objectMapperBuilder() {
        return new Jackson2ObjectMapperBuilder()
                .failOnEmptyBeans(false)
                .failOnUnknownProperties(false)
                .featuresToDisable(SerializationFeature.FAIL_ON_EMPTY_BEANS);
    }
}