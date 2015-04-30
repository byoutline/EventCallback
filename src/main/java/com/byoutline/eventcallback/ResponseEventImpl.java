package com.byoutline.eventcallback;

import retrofit.client.Header;

import java.util.List;

/**
 * Default implementation of {@link ResponseEvent}.
 *
 * @author Sebastian Kacprzak <sebastian.kacprzak at byoutline.com>
 */
public class ResponseEventImpl<R> implements ResponseEvent<R> {
    private R response;
    private List<Header> headers;
    private int status;

    public R getResponse() {
        return response;
    }

    @Override
    public void setResponse(R response) {
        this.response = response;
    }

    @Override
    public void setHeaders(List<Header> headers) {
        this.headers = headers;
    }

    @Override
    public void setStatus(int status) {
        this.status = status;
    }

}
