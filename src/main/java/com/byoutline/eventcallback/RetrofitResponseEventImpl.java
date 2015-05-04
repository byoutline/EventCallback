package com.byoutline.eventcallback;

import retrofit.client.Header;

import java.util.List;

/**
 * Default implementation of {@link RetrofitResponseEvent}.
 *
 * @author Sebastian Kacprzak <sebastian.kacprzak at byoutline.com>
 */
public class RetrofitResponseEventImpl<R> extends ResponseEventImpl<R>
        implements RetrofitResponseEvent<R> {
    private List<Header> headers;
    private int status;

    @Override
    public void setHeaders(List<Header> headers) {
        this.headers = headers;
    }

    @Override
    public void setStatus(int status) {
        this.status = status;
    }

    public List<Header> getHeaders() {
        return headers;
    }

    public int getStatus() {
        return status;
    }
}
