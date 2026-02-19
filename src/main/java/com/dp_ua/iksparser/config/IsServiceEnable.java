package com.dp_ua.iksparser.config;

import java.util.Optional;

import org.apache.commons.lang3.BooleanUtils;
import org.springframework.boot.autoconfigure.condition.ConditionOutcome;
import org.springframework.boot.autoconfigure.condition.SpringBootCondition;
import org.springframework.boot.context.properties.bind.Binder;
import org.springframework.context.annotation.ConditionContext;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.type.AnnotatedTypeMetadata;

import lombok.extern.slf4j.Slf4j;

@Configuration
@Slf4j
public class IsServiceEnable extends SpringBootCondition {

    private static final String SERVICE_SWITCH_PROPERTY_NAME = "app.enable.service";
    private static final Boolean MATCH_IF_ABSENT = Boolean.FALSE;

    @Override
    public ConditionOutcome getMatchOutcome(ConditionContext context, AnnotatedTypeMetadata metadata) {
        return Optional.of(isServiceEnable(context))
                .filter(BooleanUtils::isTrue)
                .map(enabled -> {
                    log.info("[Service MOD] is enabled");
                    return ConditionOutcome.match("Service MOD is enabled");
                })
                .orElseGet(() -> {
                    log.info("[Service MOD] is disabled");
                    return ConditionOutcome.noMatch("Service MOD is disabled");
                });
    }

    private boolean isServiceEnable(ConditionContext context) {
        return Binder.get(context.getEnvironment())
                .bind(SERVICE_SWITCH_PROPERTY_NAME, Boolean.class)
                .orElse(MATCH_IF_ABSENT);
    }

}