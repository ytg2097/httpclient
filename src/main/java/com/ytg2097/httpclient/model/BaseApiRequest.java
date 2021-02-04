package com.ytg2097.httpclient.model;

import io.ac.usrsdk.core.constant.ParamPosition;
import io.ac.usrsdk.core.exception.UsrsdkException;
import io.ac.usrsdk.core.http.RequestFormat;
import lombok.Getter;
import lombok.Setter;

import java.util.*;

import static com.google.common.collect.Lists.newArrayList;
import static com.google.common.collect.Maps.newHashMap;
import static com.google.common.collect.Maps.newLinkedHashMap;
import static com.google.common.collect.Sets.newHashSet;

/**
 * @description:
 * @author: yangtg
 * @create: 2020-09-03
 **/
@Getter
@Setter
public abstract class BaseApiRequest {

    private RequestFormat method;
    private String scheme;
    private String host;
    private String path;
    private Map<String, String> globalParam = newHashMap();
    private final Map<String, List<String>> param;
    private byte[] body;
    private Map<String, Meta> meta;



    public BaseApiRequest(RequestFormat method, BaseApiRequest.Meta... meta) {
        this.method = method;
        this.param = newLinkedHashMap();
        this.meta = newLinkedHashMap();

        for (Meta m : meta) {
            this.meta.put(m.getName(), m);
        }
    }

    public abstract BaseApiResponse newResponse();

    public List<String> getParams(String name) {

        BaseApiRequest.Meta m = this.meta.get(name);
        if (m == null) {
            throw new UsrsdkException(String.format("Param %s not available", name));
        } else {
            List<String> ls = this.param.get(name);
            if (ls == null) {
                synchronized (this.param) {
                    if ((ls = this.param.get(name)) == null) {
                        this.param.put(name, ls = newArrayList());
                    }
                }
            }
            return ls;
        }
    }

    public String getParam(String name) {

        List<String> ls = this.getParams(name);
        return ls.isEmpty() ? null : ls.iterator().next();
    }

    public List<String> addParams(String name, Iterable<?> values) {

        List<String> ls = this.getParams(name);
        if (values != null) {

            for (Object o : values) {
                this.addParam(name, o);
            }
        }

        return ls;
    }

    public List<String> addParam(String name, Object value) {

        List<String> ls = this.getParams(name);
        ls.add(value.toString());
        return ls;
    }

    public String setParam(String name, Object value) {

        List<String> ls = this.getParams(name);
        ls.clear();
        ls.add(value.toString());
        return value.toString();
    }

    public Map<String, List<String>> getParam(ParamPosition... position) {

        if (position.length == 0) {
            return this.param;
        } else {
            Set<ParamPosition> pos = newHashSet(Arrays.asList(position));
            Map<String, List<String>> res = newLinkedHashMap();

            this.param.forEach((key, value) -> {
                BaseApiRequest.Meta m = this.meta.get(key);
                if (Objects.nonNull(m) && pos.contains(m.getPosition())) {
                    res.put(key, value);
                }
            });

            return res;
        }
    }

    @Override
    public String toString() {
        return "BaseApiRequest: method=" + this.method + " , scheme=" + this.scheme + " , host=" + this.host + " , path='" + this.path + '\'' + " , globalParam=" + this.globalParam + ", meta=" + this.meta;
    }

    public static class Meta {
        private String name;
        private ParamPosition position;

        public Meta(String name, ParamPosition position) {
            this.name = name;
            this.position = position;
        }

        public String getName() {
            return this.name;
        }

        public ParamPosition getPosition() {
            return this.position;
        }

        @Override
        public String toString() {
            return "Meta: name=" + this.name + " , position=" + this.position;
        }
    }


}
