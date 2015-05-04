package com.byoutline.eventcallback;

import retrofit.client.Header;

import java.util.List;

/**
 * Event that will have server response body, headers and status.
 */
public interface RetrofitResponseEvent<R> extends ResponseEvent<R> {

    void setHeaders(List<Header> headers);

    void setStatus(int status);
}
