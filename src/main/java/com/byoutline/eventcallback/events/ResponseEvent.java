package com.byoutline.eventcallback.events;

/**
 * Event that will have server response set.
 *
 * @author Sebastian Kacprzak <nait at naitbit.com> on 24.06.14.
 */
public interface ResponseEvent<R> {

    void setResponse(R response);
}
