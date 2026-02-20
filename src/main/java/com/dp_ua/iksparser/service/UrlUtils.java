package com.dp_ua.iksparser.service;

import lombok.experimental.UtilityClass;

@UtilityClass
public class UrlUtils {

    public static String encodeUrl(String url) {
        return url
                .replace(" ", "%20")
                .replace("+", "%2B")
                .replace("(", "%28")
                .replace(")", "%29")
                .replace("'", "%27")
                .replace("'", "%27")
                .replace("\"", "%22")
                ;
    }

}
