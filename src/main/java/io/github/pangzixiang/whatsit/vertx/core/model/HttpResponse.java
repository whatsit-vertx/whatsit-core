package io.github.pangzixiang.whatsit.vertx.core.model;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.fasterxml.jackson.datatype.jsr310.ser.LocalDateTimeSerializer;
import io.netty.handler.codec.http.HttpResponseStatus;
import lombok.Getter;

import java.time.LocalDateTime;
import java.time.ZoneId;


@Getter
public class HttpResponse {

    @JsonSerialize(using = LocalDateTimeSerializer.class)
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private final LocalDateTime date = LocalDateTime.now(ZoneId.systemDefault());

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
