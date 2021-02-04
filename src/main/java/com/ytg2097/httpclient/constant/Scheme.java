package com.ytg2097.httpclient.constant;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

/**
 * @description:
 * @author: yangtg
 * @create: 2020-09-03
 **/
public enum  Scheme {

    HTTP("http"),
    HTTPS("https");

    private static final Map<String, Scheme> TYPES;
    private final String value;

    private Scheme(String value) {
        this.value = value;
    }

    public static Scheme of(String type) {
        return TYPES.get(type.toUpperCase());
    }

    public String getValue() {
        return this.value;
    }

    static {
        Map<String, Scheme> types = new HashMap(2);
        types.put("HTTP", HTTP);
        types.put("HTTPS", HTTPS);
        TYPES = Collections.unmodifiableMap(types);
    }
}
