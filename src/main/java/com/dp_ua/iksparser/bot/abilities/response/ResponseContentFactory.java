package com.dp_ua.iksparser.bot.abilities.response;

import java.util.Map;

import org.springframework.context.ApplicationContext;
import org.springframework.stereotype.Component;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Component
@Slf4j
@RequiredArgsConstructor
public class ResponseContentFactory {

    private final ApplicationContext context;
    private final Map<ResponseType, Class<? extends ResponseContentGenerator>> contentMap;

    public ResponseContentGenerator getContentForResponse(ResponseType type) {
        Class<? extends ResponseContentGenerator> responseContentClass = contentMap.get(type);

        if (responseContentClass == null) {
            log.error("ResponseContentFactory: No ResponseContent class found for type: {}", type);
            throw new IllegalArgumentException("ResponseContentFactory: No ResponseContent class found for type: %s".formatted(type));
        }

        ResponseContentGenerator contentGenerator = context.getBean(responseContentClass);
        log.debug("ResponseContentFactory: type: {}, responseContent: {}", type, contentGenerator);
        return contentGenerator;
    }
}
