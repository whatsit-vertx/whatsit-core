package io.github.pangzixiang.whatsit.vertx.core.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import io.netty.handler.codec.http.HttpResponseStatus;
import lombok.Getter;

import java.time.LocalDateTime;
import java.time.ZoneId;


/**
 * The type Http response.
 */
@Getter
public class HttpResponse {

    private final LocalDateTime date = LocalDateTime.now(ZoneId.systemDefault());

    @JsonIgnore
    private final HttpResponseStatus status;

    private final int code;

    private final Object data;

    /**
     * Instantiates a new Http response.
     *
     * @param status the status
     * @param data   the data
     */
    HttpResponse(HttpResponseStatus status, Object data) {
        this.status = status;
        this.data = data;
        this.code = status.code();
    }

    /**
     * Builder http response builder.
     *
     * @return the http response builder
     */
    public static HttpResponseBuilder builder() {
        return new HttpResponseBuilder();
    }

    /**
     * The type Http response builder.
     */
    public static class HttpResponseBuilder {
        private HttpResponseStatus status;
        private Object data;

        /**
         * Instantiates a new Http response builder.
         */
        HttpResponseBuilder() {
        }

        /**
         * Status http response builder.
         *
         * @param status the status
         * @return the http response builder
         */
        public HttpResponseBuilder status(HttpResponseStatus status) {
            this.status = status;
            return this;
        }

        /**
         * Data http response builder.
         *
         * @param data the data
         * @return the http response builder
         */
        public HttpResponseBuilder data(Object data) {
            this.data = data;
            return this;
        }

        /**
         * Build http response.
         *
         * @return the http response
         */
        public HttpResponse build() {
            return new HttpResponse(status, data);
        }

        public String toString() {
            return "HttpResponse.HttpResponseBuilder(status=" + this.status + ", data=" + this.data + ")";
        }
    }
}
