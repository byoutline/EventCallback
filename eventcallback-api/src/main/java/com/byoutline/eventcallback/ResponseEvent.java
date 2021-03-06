package com.byoutline.eventcallback;

/**
 * Event that will have body of server response set. <br />
 * If you need response status or headers take a look at
 * {@code EventCallbackBuilder#onStatusCodes(Integer...)} or use
 * {@code RetrofitResponseEvent} instead.
 *
 * @author Sebastian Kacprzak <sebastian.kacprzak at byoutline.com> on 24.06.14.
 */
public interface ResponseEvent<R> {

    void setResponse(R response);
}
