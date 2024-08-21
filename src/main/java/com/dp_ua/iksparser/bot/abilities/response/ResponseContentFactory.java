package com.dp_ua.iksparser.bot.abilities.response;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.Map;

@Component
@Slf4j
public class ResponseContentFactory {
    private final Map<ResponseType, ResponseContent> contentMap;

    @Autowired
    public ResponseContentFactory(Map<ResponseType, ResponseContent> contentMap) {
        this.contentMap = contentMap;
    }

    public ResponseContent getContentForResponse(ResponseType type) {
        ResponseContent responseContent = contentMap.get(type);
        log.debug("ResponseContentFactory: type: {}, responseContent: {}", type, responseContent);
        return responseContent;
    }
}
