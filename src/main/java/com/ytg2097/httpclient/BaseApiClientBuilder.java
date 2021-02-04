package com.ytg2097.httpclient;

import io.ac.usrsdk.core.constant.Scheme;
import io.ac.usrsdk.core.model.BuilderParams;

import javax.net.ssl.HostnameVerifier;
import javax.net.ssl.KeyManager;
import javax.net.ssl.SSLSocketFactory;
import java.security.SecureRandom;
import java.util.Random;
import java.util.concurrent.ExecutorService;

/**
 * @description:
 * @author: yangtg
 * @create: 2020-09-03
 **/
public abstract class BaseApiClientBuilder< TypeToBuild extends BaseApiClient> {

    private Random random = new Random();
    private BuilderParams params = new BuilderParams();

    public BaseApiClientBuilder<TypeToBuild> maxIdleTimeMills(long maxIdleTimeMillis) {

        this.params.setMaxIdleTimeMillis(maxIdleTimeMillis);
        return this;
    }

    public BaseApiClientBuilder<TypeToBuild> keepAliveDurationMillis(long keepAliveDurationMillis) {
        this.params.setKeepAliveDurationMillis(keepAliveDurationMillis);
        return this;
    }

    public BaseApiClientBuilder<TypeToBuild> connectionTimeoutMillis(long connectionTimeoutMillis) {
        this.params.setConnectionTimeoutMillis(connectionTimeoutMillis);
        return this;
    }

    public BaseApiClientBuilder<TypeToBuild> readTimeoutMillis(long readTimeoutMillis) {
        this.params.setReadTimeoutMillis(readTimeoutMillis);
        return this;
    }

    public BaseApiClientBuilder<TypeToBuild> writeTimeoutMillis(long writeTimeoutMillis) {
        this.params.setWriteTimeoutMillis(writeTimeoutMillis);
        return this;
    }

    public BaseApiClientBuilder<TypeToBuild> executorService(ExecutorService executorService) {
        this.params.setExecutorService(executorService);
        return this;
    }

    public BaseApiClientBuilder<TypeToBuild> sslSocketFactory(SSLSocketFactory sslSocketFactory) {
        this.params.setSslSocketFactory(sslSocketFactory);
        return this;
    }

    public BaseApiClientBuilder<TypeToBuild> maxRequests(int maxRequests) {
        this.params.setMaxRequests(maxRequests);
        return this;
    }

    public BaseApiClientBuilder<TypeToBuild> maxRequestsPerHost(int maxRequestsPerHost) {
        this.params.setMaxRequestsPerHost(maxRequestsPerHost);
        return this;
    }

    public BaseApiClientBuilder<TypeToBuild> idleCallback(Runnable idleCallback) {
        this.params.setIdleCallback(idleCallback);
        return this;
    }


    public BaseApiClientBuilder<TypeToBuild> keyManagers(KeyManager[] keyManagers) {
        this.params.setKeyManagers(keyManagers);
        return this;
    }

    public BaseApiClientBuilder<TypeToBuild> secureRandom(SecureRandom secureRandom) {
        this.params.setSecureRandom(secureRandom);
        return this;
    }

    public BaseApiClientBuilder< TypeToBuild> hostnameVerifier(HostnameVerifier hostnameVerifier) {
        this.params.setHostnameVerifier(hostnameVerifier);
        return this;
    }

    public BaseApiClientBuilder<TypeToBuild> setExtParams(String key, Object value) {
        this.params.setExtParam(key, value);
        return this;
    }

    public BaseApiClientBuilder<TypeToBuild> scheme(Scheme scheme) {
        this.params.setScheme(scheme);
        return this;
    }


    public BaseApiClientBuilder<TypeToBuild> host(String host) {
        this.params.setHost(host);
        return this;
    }

    public BaseApiClientBuilder<TypeToBuild> server(String server) {
        this.params.setServer(server);
        return this;
    }

    public final TypeToBuild build() {

        if (this.params.getScheme() == null) {
            this.params.setScheme(Scheme.HTTPS);
        }

        boolean sandbox;
        if (this.params.getHost() == null) {
            sandbox = this.params.isSandbox();
            boolean ssl = this.params.getScheme() == Scheme.HTTPS;
            if (!sandbox && !ssl) {
                this.params.setHost(this.httpHost());
            } else if (sandbox && ssl) {
                this.params.setHost(this.sandboxSslHost());
            } else if (!sandbox) {
                this.params.setHost(this.sslHost());
            } else {
                this.params.setHost(this.sandboxHttpHost());
            }
        }

        if (this.params.getServer() == null) {
            sandbox = this.params.getScheme() == Scheme.HTTPS;
            if (sandbox) {
                this.params.setServer(this.serverSslHost());
            } else {
                this.params.setServer(this.serverHost());
            }
        }

        return this.build(this.params);
    }

    protected TypeToBuild build(BuilderParams params) {
        return (TypeToBuild) new BaseApiClient(params) {};
    }

    protected String serverHost() {
        throw new UnsupportedOperationException();
    }

    protected String serverSslHost() {
        throw new UnsupportedOperationException();
    }

    protected String httpHost() {
        throw new UnsupportedOperationException();
    }

    protected String sslHost() {
        throw new UnsupportedOperationException();
    }

    protected String sandboxHttpHost() {
        throw new UnsupportedOperationException();
    }

    protected String sandboxSslHost() {
        throw new UnsupportedOperationException();
    }

    protected String nextHost(String[] hosts) {
        if (hosts.length == 0) {
            throw new UnsupportedOperationException("No available host");
        } else {
            return hosts[this.random.nextInt(hosts.length)];
        }
    }
}
