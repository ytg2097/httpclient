package com.ytg2097.httpclient.exception;

import lombok.Getter;

/**
 * @description:
 * @author: yangtg
 * @create: 2021-01-29
 **/
@Getter
public class CmdResponseException extends Exception{

    private String cmdCode;
    private String cmdNum;
    private String errorCode;
    private String errorMsg;

    public CmdResponseException(String cmdCode, String cmdNum, String errorCode, String errorMsg) {

        this.cmdCode = cmdCode;
        this.cmdNum = cmdNum;
        this.errorCode = errorCode;
        this.errorMsg = errorMsg;
    }
}
