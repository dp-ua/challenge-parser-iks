package com.dp_ua.iksparser.bot.abilities.response;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.stereotype.Component;

import java.util.Map;

@Component
@Slf4j
public class ResponseContentFactory {

    private final ApplicationContext context;
    private final Map<ResponseType, Class<? extends ResponseContentGenerator>> contentMap;

    @Autowired
    public ResponseContentFactory(ApplicationContext context, Map<ResponseType, Class<? extends ResponseContentGenerator>> contentMap) {
        this.context = context;
        this.contentMap = contentMap;
        contentMap.forEach((key, value) -> log.info("key: [{}], class: {}", key, value.getSimpleName()));
    }

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
