package com.byoutline.eventcallback

/**
 *
 * @author Sebastian Kacprzak <sebastian.kacprzak at byoutline.com>
 */
class BusProvider implements IBus {
    IBus impl;

    void post(Object o) {
        impl.post(o)
    }
}

