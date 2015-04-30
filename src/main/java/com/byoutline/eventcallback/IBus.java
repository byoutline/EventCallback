package com.byoutline.eventcallback;

/**
 * Bus on which all events from {@link EventCallback} will be posted.
 *
 * @author Sebastian Kacprzak <sebastian.kacprzak at byoutline.com>
 */
public interface IBus {

    void post(Object event);
}
