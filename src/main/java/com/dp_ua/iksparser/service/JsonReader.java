package com.dp_ua.iksparser.service;

import lombok.extern.slf4j.Slf4j;
import org.json.JSONObject;
import org.springframework.stereotype.Component;

import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;

@Component
@Slf4j
public class JsonReader {
    private final Charset charset;

    public JsonReader() {
        charset = StandardCharsets.UTF_8;
    }

    public JSONObject read(String text) {
        return new JSONObject(text);
    }

    public String getVal(String text, String key) {
        return getVal(read(text), key);
    }

    public String getVal(JSONObject json, String key) {
        return json.getString(key);
    }

}
