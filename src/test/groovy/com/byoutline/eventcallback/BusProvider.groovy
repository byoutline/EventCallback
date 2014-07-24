
package com.byoutline.eventcallback

/**
 *
 * @author Sebastian Kacprzak <nait at naitbit.com>
 */
class BusProvider implements IBus {
    IBus impl;
    void post(Object o) {
        impl.post(o)
    }
}

