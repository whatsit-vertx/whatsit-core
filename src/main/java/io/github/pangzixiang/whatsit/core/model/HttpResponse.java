package io.github.pangzixiang.whatsit.core.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import io.netty.handler.codec.http.HttpResponseStatus;
import lombok.Getter;

import java.util.Date;


@Getter
public class HttpResponse {

    private final Date date = new Date(System.currentTimeMillis());

    @JsonIgnore
    private final HttpResponseStatus status;

    private final int code;

    private final Object data;

    HttpResponse(HttpResponseStatus status, Object data) {
        this.status = status;
        this.data = data;
        this.code = status.code();
    }

    public static HttpResponseBuilder builder() {
        return new HttpResponseBuilder();
    }

    public static class HttpResponseBuilder {
        private HttpResponseStatus status;
        private Object data;

        HttpResponseBuilder() {
        }

        public HttpResponseBuilder status(HttpResponseStatus status) {
            this.status = status;
            return this;
        }

        public HttpResponseBuilder data(Object data) {
            this.data = data;
            return this;
        }

        public HttpResponse build() {
            return new HttpResponse(status, data);
        }

        public String toString() {
            return "HttpResponse.HttpResponseBuilder(status=" + this.status + ", data=" + this.data + ")";
        }
    }
}
