
package com.byoutline.eventcallback

/**
 *
 * @author Sebastian Kacprzak <nait at naitbit.com>
 */
class BusProvider implements Bus {
    Bus impl;
    void post(Object o) {
        impl.post(o)
    }
    
    void postSticky(Object o) {
        impl.postSticky(o)
    }
}

