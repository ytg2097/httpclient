package com.ytg2097.httpclient;

import io.ac.usrsdk.core.HttpClient;
import io.ac.usrsdk.core.constant.ParamPosition;
import io.ac.usrsdk.core.exception.UsrsdkException;
import io.ac.usrsdk.core.model.ApiCallBack;
import io.ac.usrsdk.core.model.BaseApiRequest;
import io.ac.usrsdk.core.model.BaseApiResponse;
import io.ac.usrsdk.core.model.BuilderParams;
import org.apache.http.Header;
import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.client.config.RequestConfig;
import org.apache.http.client.entity.EntityBuilder;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpUriRequest;
import org.apache.http.client.methods.RequestBuilder;
import org.apache.http.client.utils.URIBuilder;
import org.apache.http.config.ConnectionConfig;
import org.apache.http.config.Registry;
import org.apache.http.config.RegistryBuilder;
import org.apache.http.conn.socket.ConnectionSocketFactory;
import org.apache.http.conn.socket.PlainConnectionSocketFactory;
import org.apache.http.conn.ssl.SSLConnectionSocketFactory;
import org.apache.http.conn.ssl.TrustStrategy;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.DefaultConnectionKeepAliveStrategy;
import org.apache.http.impl.client.HttpClientBuilder;
import org.apache.http.impl.conn.PoolingHttpClientConnectionManager;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.ssl.SSLContextBuilder;
import org.apache.http.util.EntityUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.net.ssl.SSLContext;
import java.io.IOException;
import java.net.URISyntaxException;
import java.nio.charset.Charset;
import java.security.KeyManagementException;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicInteger;

import static com.google.common.collect.Lists.newArrayList;
import static com.google.common.collect.Maps.newHashMap;
import static com.google.common.collect.Queues.newSynchronousQueue;

/**
 * @description:
 * @author: yangtg
 * @create: 2020-09-03
 **/
public class ApacheHttpClient extends HttpClient {

    private static final Logger log = LoggerFactory.getLogger(ApacheHttpClient.class);
    private static final String EXT_PARAM_KEY_BUILDER = "apache.httpclient.builder";
    private static final int DEFAULT_THREAD_KEEP_ALIVE_TIME = 60;
    private ExecutorService executorService;
    private CloseableHttpClient httpClient;
    private PoolingHttpClientConnectionManager connectionManager;
    private String host;
    private String scheme;

    public ApacheHttpClient(BuilderParams params) {
        super(params);
    }

    @Override
    protected void init(final BuilderParams params) {
        HttpClientBuilder builder;
        if (params.containsExtParam(EXT_PARAM_KEY_BUILDER)) {
            builder = (HttpClientBuilder)params.getExtParam(EXT_PARAM_KEY_BUILDER);
        } else {
            builder = HttpClientBuilder.create();
        }

        RequestConfig defaultConfig = RequestConfig.custom()
                .setConnectTimeout((int)params.getConnectionTimeoutMillis())
                .setCookieSpec("ignoreCookies")
                .setSocketTimeout((int)params.getReadTimeoutMillis())
                .setConnectionRequestTimeout((int)params.getWriteTimeoutMillis())
                .build();
        builder.setDefaultRequestConfig(defaultConfig);
        SSLContext sslContext = null;

        try {
            sslContext = new SSLContextBuilder().loadTrustMaterial(null, (TrustStrategy) (arg0, arg1) -> true).build();
            builder.setSSLSocketFactory(new SSLConnectionSocketFactory(sslContext, (s, sslSession) -> true));
        } catch (NoSuchAlgorithmException | KeyManagementException | KeyStoreException e) {
            e.printStackTrace();
            log.error(e.getLocalizedMessage());
        }

        RegistryBuilder<ConnectionSocketFactory> socketFactoryRegistryBuilder = RegistryBuilder.create();
        socketFactoryRegistryBuilder.register("http", PlainConnectionSocketFactory.getSocketFactory());
        socketFactoryRegistryBuilder .register("https", new SSLConnectionSocketFactory(Objects.requireNonNull(sslContext), (s, sslSession) -> true));
        Registry<ConnectionSocketFactory> socketFactoryRegistry = socketFactoryRegistryBuilder.build();
        this.connectionManager = new PoolingHttpClientConnectionManager(socketFactoryRegistry);
        this.connectionManager.setMaxTotal(params.getMaxRequests());
        this.connectionManager.setDefaultMaxPerRoute(params.getMaxRequestsPerHost());
        this.connectionManager.setDefaultConnectionConfig(ConnectionConfig.custom().setCharset(Charset.forName("utf-8")).build());
        builder.setConnectionManager(this.connectionManager);
        ApacheIdleConnectionCleaner.registerConnectionManager(this.connectionManager, params.getMaxIdleTimeMillis());
        if (params.getExecutorService() == null) {
            this.executorService = new ThreadPoolExecutor(0, params.getMaxRequests(), DEFAULT_THREAD_KEEP_ALIVE_TIME, TimeUnit.SECONDS, newSynchronousQueue(), new ApacheHttpClient.DefaultAsyncThreadFactory());
        } else {
            this.executorService = params.getExecutorService();
        }

        if (params.getKeepAliveDurationMillis() > 0L) {
            builder.setKeepAliveStrategy((response, context) -> {
                long duration = DefaultConnectionKeepAliveStrategy.INSTANCE.getKeepAliveDuration(response, context);
                return duration > 0L && duration < params.getKeepAliveDurationMillis() ? duration : params.getKeepAliveDurationMillis();
            });
        }

        this.httpClient = builder.build();
        this.host = params.getHost();
        this.scheme = params.getScheme().getValue();
    }

    private HttpUriRequest parseToHttpRequest(BaseApiRequest apiReq) {

        RequestBuilder builder = RequestBuilder.create(apiReq.getMethod().getName());

        try {
            URIBuilder uriBuilder = buildRequestUrl(apiReq);
            builder.setUri(uriBuilder.build());
        } catch (URISyntaxException e) {
            throw new UsrsdkException("build http request uri failed", e);
        }

        buildBody(apiReq, builder);

        buildHeader(apiReq, builder);

        return builder.build();
    }

    private void buildHeader(BaseApiRequest apiReq, RequestBuilder builder) {

        Map<String, List<String>> header = apiReq.getParam(ParamPosition.HEAD);

        header.forEach((key,values) -> values.forEach(val -> builder.addHeader(key, val)));

        apiReq.getGlobalParam().forEach(builder::addHeader);
    }

    private void buildBody(BaseApiRequest apiReq, RequestBuilder builder) {

        EntityBuilder bodyBuilder = EntityBuilder.create();
        bodyBuilder.setContentType(apiReq.getMethod().getContentType());
        Map<String, List<String>> body = apiReq.getParam(ParamPosition.BODY);

        if (body != null && !body.isEmpty()) {
            List<NameValuePair> paramList = newArrayList();

            body.forEach((key,values) -> values.forEach(val -> paramList.add(new BasicNameValuePair(key, val))));

            bodyBuilder.setParameters(paramList);
            builder.setEntity(bodyBuilder.build());
        } else if (apiReq.getBody() != null) {
            bodyBuilder.setBinary(apiReq.getBody());
            builder.setEntity(bodyBuilder.build());
        }
    }

    private URIBuilder buildRequestUrl(BaseApiRequest apiReq) {


        URIBuilder uriBuilder = new URIBuilder();
        uriBuilder.setScheme(apiReq.getScheme() != null ? apiReq.getScheme() : this.scheme);
        String host = apiReq.getHost() != null ? apiReq.getHost() : this.host;
        String[] split = host.split("/");
        uriBuilder.setHost(split[0]);
        apiReq.getGlobalParam().put("Host", uriBuilder.getHost());
        String path = (split.length > 1 ? String.join("/", Arrays.copyOfRange(split, 1, split.length)) : "") + apiReq.getPath();
        uriBuilder.setPath("/" + path);
        Map<String, List<String>> global = apiReq.getParam(ParamPosition.QUERY);
        if (global != null) {
            global.forEach((key,value) -> value.forEach(v -> uriBuilder.addParameter((String)key, (String)v)));
        }
        return uriBuilder;
    }

    private <T> BaseApiResponse<T> parseToApiResponse(HttpResponse httpResponse, BaseApiResponse<T> result) throws IOException {

        result.setStatusCode(httpResponse.getStatusLine().getStatusCode());
        Header message;
        if (httpResponse.getEntity() != null) {
            message = httpResponse.getEntity().getContentType();
            if (message != null) {
                result.setContentType(message.getValue());
            }

            result.setByteBody(EntityUtils.toByteArray(httpResponse.getEntity()));
        } else {
            message = httpResponse.getFirstHeader("Content-Type");
            result.setContentType(message.getValue());
        }

        result.setHeaders(newHashMap());
        Header[] headers = httpResponse.getAllHeaders();
        for (Header header : headers) {
            result.getHeaders().put(header.getName(), header.getValue());
        }

        return result;
    }

    @Override
    public final BaseApiResponse syncInvoke(BaseApiRequest apiRequest) {

        HttpUriRequest httpRequest = this.parseToHttpRequest(apiRequest);

        CloseableHttpResponse httpResponse = null;

        try {
            httpResponse = this.httpClient.execute(httpRequest);
            return this.parseToApiResponse(httpResponse, apiRequest.newResponse());
        } catch (IOException e) {
            throw new UsrsdkException(e);
        } finally {
            try {
                if (httpResponse != null) {
                    httpResponse.close();
                }
            } catch (Exception e) {
                log.error(e.getMessage());
            }
        }
    }

    @Override
    public <REQ extends BaseApiRequest, RESP extends BaseApiResponse> Future<RESP> asyncInvoke(final REQ request, final ApiCallBack<REQ, RESP> callback) {
        return this.executorService.submit(() -> {
            BaseApiResponse result;
            try {
                result = ApacheHttpClient.this.syncInvoke(request);
            } catch (Exception e) {
                if (callback != null) {
                    callback.onFailure(request, e);
                }

                throw e;
            }

            if (callback != null) {
                callback.onResponse(request, (RESP) result);
            }

            return (RESP) result;
        });
    }

    @Override
    public void shutdown() {

        this.executorService.shutdown();
        ApacheIdleConnectionCleaner.removeConnectionManager(this.connectionManager);
        this.connectionManager.shutdown();
        if (this.httpClient != null) {
            try {
                this.httpClient.close();
            } catch (Exception ignored) {
            }
        }

    }

    private class DefaultAsyncThreadFactory implements ThreadFactory {
        private AtomicInteger counter;

        private DefaultAsyncThreadFactory() {
            this.counter = new AtomicInteger(0);
        }

        @Override
        public Thread newThread(Runnable runnable) {
            return new Thread(runnable, "Machine vision Java SDK Async ThreadPool - " + this.counter.incrementAndGet());
        }
    }
}
