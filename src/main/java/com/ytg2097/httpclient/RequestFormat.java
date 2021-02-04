package com.ytg2097.httpclient;

import org.apache.http.entity.ContentType;

/**
 * @description:
 * @author: yangtg
 * @create: 2020-09-03
 **/
public class RequestFormat {

    private String name;
    private ContentType contentType;

    public static RequestFormat GET() {
        return type("GET");
    }

    public static RequestFormat POST() {
        return type("POST");
    }

    public static RequestFormat PUT() {
        return type("PUT");
    }

    public static RequestFormat PATCH() {
        return type("PATCH");
    }

    public static RequestFormat DELETE() {
        return type("DELETE");
    }

    public static RequestFormat type(String method) {
        return type(method, ContentType.create(ContentType.APPLICATION_FORM_URLENCODED.getMimeType(), "utf-8"));
    }

    public static RequestFormat type(String method, String contentType) {
        return type(method, ContentType.parse(contentType));
    }

    public static RequestFormat type(String method, ContentType contentType) {
        return type(method, contentType, null);
    }

    public static RequestFormat type(String method, ContentType contentType, String charset) {
        if (method == null) {
            throw new IllegalArgumentException("Method cannot be null");
        } else if (contentType == null) {
            throw new IllegalArgumentException("ContentType cannot be null");
        } else {
            if (charset != null) {
                contentType = ContentType.create(contentType.getMimeType(), charset);
            }

            method = method.toUpperCase();
            RequestFormat type = new RequestFormat(method, contentType);
            return type;
        }
    }

    private RequestFormat(String name, ContentType contentType) {
        this.name = name;
        this.contentType = contentType;
    }

    public String name() {
        return this.name;
    }

    public String getName() {
        return this.name;
    }

    public ContentType getContentType() {
        return this.contentType;
    }

    public void setName(String name) {
        this.name = name;
    }

    public void setContentType(ContentType contentType) {
        this.contentType = contentType;
    }

    public RequestFormat name(String name) {
        this.name = name;
        return this;
    }

    public RequestFormat contentType(ContentType contentType) {
        this.contentType = contentType;
        return this;
    }

    public RequestFormat contentType(ContentType contentType, String charset) {
        if (charset != null) {
            contentType = ContentType.create(contentType.getMimeType(), charset);
        }

        this.contentType = contentType;
        return this;
    }

    @Override
    public int hashCode() {
        return this.name.hashCode() + this.contentType.hashCode();
    }

    @Override
    public boolean equals(Object obj) {
        if (!(obj instanceof RequestFormat)) {
            return false;
        } else {
            return this.name.equals(((RequestFormat)obj).name) && this.contentType.equals(((RequestFormat)obj).contentType);
        }
    }

    @Override
    public String toString() {
        return this.name + ": " + this.contentType;
    }
}
