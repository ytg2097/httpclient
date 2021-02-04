package com.ytg2097.httpclient.model;

import io.ac.usrsdk.core.BaseApiClient;
import io.ac.usrsdk.core.constant.Scheme;
import lombok.Getter;
import lombok.Setter;

import javax.net.ssl.HostnameVerifier;
import javax.net.ssl.KeyManager;
import javax.net.ssl.SSLSocketFactory;
import javax.net.ssl.X509TrustManager;
import java.security.SecureRandom;
import java.util.Map;
import java.util.concurrent.ExecutorService;

import static com.google.common.collect.Maps.newHashMap;

/**
 * @description:
 * @author: yangtg
 * @create: 2020-09-03
 **/
@Getter
@Setter
public final class BuilderParams implements Cloneable {
    private String appKey;
    private String appSecret;
    private int maxIdleConnections = 5;
    private long maxIdleTimeMillis = 60000L;
    private long keepAliveDurationMillis = 5000L;
    private long connectionTimeoutMillis = 15000L;
    private long readTimeoutMillis = 15000L;
    private long writeTimeoutMillis = 15000L;
    private SSLSocketFactory sslSocketFactory = null;
    private KeyManager[] keyManagers = null;
    private X509TrustManager[] x509TrustManagers = null;
    private SecureRandom secureRandom = null;
    private HostnameVerifier hostnameVerifier = null;
    private int maxRequests = 64;
    private int maxRequestsPerHost = 5;
    private Runnable idleCallback = null;
    private ExecutorService executorService = null;
    private Map<String, Object> extParams = newHashMap();
    private Class<? extends BaseApiClient> apiClientClass;
    private String host;
    private Scheme scheme;
    private boolean sandbox;
    private String server;


    public Object getExtParam(Object key) {
        return this.extParams.get(key);
    }

    public Object setExtParam(String key, Object value) {
        return this.extParams.put(key, value);
    }

    public boolean containsExtParam(Object key) {
        return this.extParams.containsKey(key);
    }

}
