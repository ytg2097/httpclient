package com.ytg2097.httpclient;

import io.ac.usrsdk.core.constant.ParamPosition;
import io.ac.usrsdk.core.exception.UsrsdkException;
import io.ac.usrsdk.core.http.HttpClientFactory;
import io.ac.usrsdk.core.model.ApiCallBack;
import io.ac.usrsdk.core.model.BaseApiRequest;
import io.ac.usrsdk.core.model.BaseApiResponse;
import io.ac.usrsdk.core.model.BuilderParams;

import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.concurrent.Future;

/**
 * @description:
 * @author: yangtg
 * @create: 2020-09-03
 **/
public abstract class BaseApiClient {

    private final HttpClient httpClient;
    private final String host;
    private final String scheme;

    public BaseApiClient(BuilderParams builderParams) {

        this.httpClient = HttpClientFactory.buildClient(builderParams);
        this.host = builderParams.getHost();
        this.scheme = builderParams.getScheme().getValue();
    }


    private <REQ extends BaseApiRequest> REQ buildSDKRequest(REQ apiReq) {

        String pathWithPathParameter = this.combinePathParam(apiReq.getPath(), apiReq.getParam(ParamPosition.PATH));
        apiReq.setPath(pathWithPathParameter);
        apiReq.getGlobalParam().put("Content-Type", apiReq.getMethod().getContentType().toString());
        return apiReq;
    }

    private String combinePathParam(String path, Map<String, List<String>> pathParams) {

        if (pathParams == null) {
            return path;
        } else {
            String key;
            for (Iterator iterator = pathParams.keySet().iterator();
                 iterator.hasNext();
                 path = path.replace("{" + key + "}", (CharSequence) ((List) pathParams.get(key)).iterator().next())) {
                 key = (String) iterator.next();
            }

            return path;
        }
    }

    private String getHttpDateHeaderValue(Date date) {
        SimpleDateFormat dateFormat = new SimpleDateFormat("EEE, dd MMM yyyy HH:mm:ss z", Locale.US);
        dateFormat.setTimeZone(TimeZone.getTimeZone("GMT"));
        return dateFormat.format(date);
    }

    public final <R extends BaseApiResponse> R syncInvoke(BaseApiRequest apiRequest) {

        try {
            apiRequest = this.buildSDKRequest(apiRequest);
            return (R) this.httpClient.syncInvoke(apiRequest);
        } catch (IOException e) {
            throw new UsrsdkException(e);
        }
    }

    public final <REQ extends BaseApiRequest, RESP extends BaseApiResponse> Future<RESP> asyncInvoke(REQ apiRequest, ApiCallBack<REQ, RESP> callback) {
        try {
            apiRequest = this.buildSDKRequest(apiRequest);
            return this.httpClient.asyncInvoke(apiRequest, callback);
        } catch (Exception e) {
            throw new UsrsdkException(e);
        }
    }

    public void shutdown() {
        try {
            if (this.httpClient != null) {
                this.httpClient.close();
            }
        } catch (Exception e) {
        }

    }
}
