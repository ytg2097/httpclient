package com.ytg2097.httpclient.model;

import com.alibaba.fastjson.JSON;

/**
 * @description:
 * @author: yangtg
 * @create: 2021-01-26
 **/
public abstract class AbstractRequestBody {

    public byte[] toBytes(){
        return JSON.toJSONString(this).getBytes();
    }
}
