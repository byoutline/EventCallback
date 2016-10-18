package com.byoutline.ottoeventcallback;

import com.byoutline.eventcallback.IBus;
import com.squareup.otto.Bus;

/**
 * Wraps given instance of {@link Bus} so it implements {@link IBus}.
 *
 * @author Sebastian Kacprzak <sebastian.kacprzak at byoutline.com>
 */
public class OttoIBus implements IBus {

    private final Bus bus;

    public OttoIBus(Bus bus) {
        this.bus = bus;
    }

    @Override
    public void post(Object event) {
        bus.post(event);
    }
    
    public void register(Object object) {
        bus.register(object);
    }
}
