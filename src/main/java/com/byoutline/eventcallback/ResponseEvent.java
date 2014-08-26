package com.byoutline.eventcallback;

/**
 * Event that will have server response set.
 *
 * @author Sebastian Kacprzak <sebastian.kacprzak at byoutline.com> on 24.06.14.
 */
public interface ResponseEvent<R> {

    void setResponse(R response);
}
