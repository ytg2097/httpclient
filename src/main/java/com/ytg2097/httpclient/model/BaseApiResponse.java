package com.ytg2097.httpclient.model;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.TypeReference;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.List;
import java.util.Map;

/**
 * @description:
 * @author: yangtg
 * @create: 2020-09-03
 **/
@Getter
@Setter
@ToString
public abstract class BaseApiResponse<T> {

    private int statusCode;
    private String contentType;
    private Map<String, String> headers;
    private byte[] byteBody;
    private ApiResBody<T> entity;
    private Type bodyType;
    public BaseApiResponse(){
        Type superClass = this.getClass().getGenericSuperclass();
        this.bodyType = ((ParameterizedType)superClass).getActualTypeArguments()[0];
    }

    public ApiResBody<T> body(){

        System.out.println(new String(byteBody));
        if (byteBody == null || byteBody.length == 0){
            return null;
        }
        return ResponseBodyReader.readValue(byteBody, bodyType);
    }

    public ApiResBody<List<T>> iterableBody(){

        System.out.println(new String(byteBody));
        if (byteBody == null || byteBody.length == 0){
            return null;
        }
        return ResponseBodyReader.readArrayValue(byteBody, bodyType);
    }

    public static class ResponseBodyReader {

        static <T> ApiResBody<T> readValue(byte[] bytes, Type type){

            ApiResBody entity = JSON.parseObject(bytes, ApiResBody.class);
            if (entity.isOk()){

                return JSON.parseObject(JSON.parse(bytes).toString(), new TypeReference<ApiResBody<T>>(type){});
            }
            return entity;
        }

        static <T> ApiResBody<List<T>> readArrayValue(byte[] bytes, Type type){

            return JSON.parseObject(JSON.parse(bytes).toString(), new TypeReference<ApiResBody<List<T>>>(type){});
        }

        public static byte[] writeValueAsBytes(Object obj){

            return JSON.toJSONString(obj).getBytes();
        }

        public static <T> T readObject(byte[] bytes, Class<T> type) {

            return JSON.parseObject(bytes,type);
        }
    }


}
