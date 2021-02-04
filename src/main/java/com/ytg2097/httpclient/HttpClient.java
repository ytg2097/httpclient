package com.ytg2097.httpclient;

import io.ac.usrsdk.core.model.ApiCallBack;
import io.ac.usrsdk.core.model.BaseApiRequest;
import io.ac.usrsdk.core.model.BaseApiResponse;
import io.ac.usrsdk.core.model.BuilderParams;

import java.io.IOException;
import java.util.concurrent.Future;

/**
 * @description:
 * @author: yangtg
 * @create: 2020-09-03
 **/
public abstract class HttpClient implements Cloneable{

    public HttpClient(BuilderParams builderParams) {
        this.init(builderParams);
    }

    protected abstract void init(BuilderParams params);

    public abstract BaseApiResponse syncInvoke(BaseApiRequest request) throws IOException;

    public abstract <REQ extends BaseApiRequest, RESP extends BaseApiResponse> Future<RESP> asyncInvoke(REQ req, ApiCallBack<REQ, RESP> callBack);

    public abstract void shutdown();

    public void close() throws IOException {
        this.shutdown();
    }
}
