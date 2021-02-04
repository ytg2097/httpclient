package com.ytg2097.httpclient.model;

/**
 * @description:
 * @author: yangtg
 * @create: 2020-09-03
 **/
public interface ApiCallBack <Q extends BaseApiRequest, P extends BaseApiResponse>{
    void onFailure(Q request, Exception exception);

    void onResponse(Q request, P response);
}
