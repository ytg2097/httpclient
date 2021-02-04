package com.ytg2097.httpclient.model;

import lombok.Setter;
import lombok.ToString;

import java.io.Serializable;

/**
 * @description:
 * @author: yangtg
 * @create: 2020-09-04
 **/
@Setter
@ToString
public class ApiResBody<T> implements Serializable {

    private final static Integer SUCCESS_CODE = 0;

    private Integer status;

    private String info;

    private String keyDesp;

    private String account;

    private T data;

    public boolean isOk(){

        return SUCCESS_CODE .equals(status);
    }

    public Integer status(){

        return status;
    }

    public String info(){

        if (!isOk()){
            if (data != null){

                return info + "_" + data.toString();
            }else {
                return info;
            }
        }
        return info;
    }

    public String keyDesp(){

        return keyDesp;
    }

    public T data(){

        if (isOk()){
            return data;
        }
        return null;
    }


}
