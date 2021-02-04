package com.ytg2097.httpclient.exception;

/**
 * @description:
 * @author: yangtg
 * @create: 2020-09-03
 **/
public class UsrsdkException extends RuntimeException {

    public UsrsdkException(String message) {
        super( message);
    }

    public UsrsdkException(String message, Throwable cause) {
        super(message, cause);
    }

    public UsrsdkException(Throwable cause) {
        super(cause);
    }
}
