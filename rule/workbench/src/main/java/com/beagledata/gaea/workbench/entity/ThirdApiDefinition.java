package com.beagledata.gaea.workbench.entity;

import com.beagledata.gaea.common.BaseEntity;
import com.fasterxml.jackson.annotation.JsonIgnore;

import java.util.ArrayList;
import java.util.List;

/**
 * 自定义第三方接口
 *
 * Created by liulu on 2020/6/14.
 */
public class ThirdApiDefinition extends BaseEntity {
    private static final long serialVersionUID = -6307233642898597092L;

    /**
     * 接口名称
     */
    private String name;
    /**
     * 请求地址
     */
    private String url;
    /**
     * 请求方法
     */
    private Method method = Method.POST;
    /**
     * POST请求时候的Content-Type
     */
    private RequestContentType requestContentType = RequestContentType.X_WWW_FORM_URLENCODED;
    /**
     * 请求头部列表
     */
    private List<Header> headers = new ArrayList<>();
    /**
     * 请求头部JSON串
     */
    @JsonIgnore
    private String headersJson;
    /**
     * 响应JSON查询path
     */
    private String responseJsonPath;
    /**
     * 创建人
     */
    private User creator;

    public enum Method {
        GET, POST
    }

    public enum RequestContentType {
        X_WWW_FORM_URLENCODED, JSON
    }

    public static class Header {
        private String name;
        private String value;

        public String getName() {
            return name;
        }

        public void setName(String name) {
            this.name = name;
        }

        public String getValue() {
            return value;
        }

        public void setValue(String value) {
            this.value = value;
        }

        @Override
        public String toString() {
            StringBuilder sb = new StringBuilder("Header{");
            sb.append("name='").append(name).append('\'');
            sb.append(", value='").append(value).append('\'');
            sb.append('}');
            return sb.toString();
        }
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    public Method getMethod() {
        return method;
    }

    public void setMethod(Method method) {
        this.method = method;
    }

    public List<Header> getHeaders() {
        return headers;
    }

    public void setHeaders(List<Header> headers) {
        this.headers = headers;
    }

    public String getHeadersJson() {
        return headersJson;
    }

    public void setHeadersJson(String headersJson) {
        this.headersJson = headersJson;
    }

    public User getCreator() {
        return creator;
    }

    public void setCreator(User creator) {
        this.creator = creator;
    }

    public RequestContentType getRequestContentType() {
        return requestContentType;
    }

    public void setRequestContentType(RequestContentType requestContentType) {
        this.requestContentType = requestContentType;
    }

    public String getResponseJsonPath() {
        return responseJsonPath;
    }

    public void setResponseJsonPath(String responseJsonPath) {
        this.responseJsonPath = responseJsonPath;
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder("ThirdApiDefinition{");
        sb.append("name='").append(name).append('\'');
        sb.append(", url='").append(url).append('\'');
        sb.append(", method=").append(method);
        sb.append(", requestContentType=").append(requestContentType);
        sb.append(", headers=").append(headers);
        sb.append(", headersJson='").append(headersJson).append('\'');
        sb.append(", responseJsonPath='").append(responseJsonPath).append('\'');
        sb.append(", creator=").append(creator);
        sb.append('}');
        sb.append(" ").append(super.toString());
        return sb.toString();
    }
}
