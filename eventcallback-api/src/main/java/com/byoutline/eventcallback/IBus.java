package com.byoutline.eventcallback;

/**
 * Bus interface.
 *
 * @author Sebastian Kacprzak <sebastian.kacprzak at byoutline.com>
 */
public interface IBus {

    void post(Object event);
}
