
package com.ytg2097.httpclient.exception;

/**
 * @description:
 * @author: yangtg
 * @create: 2020-02-18
 **/
public abstract class ErrorCodeRuntimeException extends RuntimeException {

    private int code;

    public ErrorCodeRuntimeException(int code, String message) {
        super(message);
        this.code = code;
    }

    public ErrorCodeRuntimeException(Throwable e){
        super(e);
    }

    public int getCode() {
        return code;
    }
}
