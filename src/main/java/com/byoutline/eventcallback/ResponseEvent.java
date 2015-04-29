package com.byoutline.eventcallback;

import retrofit.client.Header;

import java.util.List;

/**
 * Event that will have server response set.
 *
 * @author Sebastian Kacprzak <sebastian.kacprzak at byoutline.com> on 24.06.14.
 */
public interface ResponseEvent<R> {

    void setResponse(R response);

    void setHeaders(List<Header> headers);

    void setStatus(int status);
}
