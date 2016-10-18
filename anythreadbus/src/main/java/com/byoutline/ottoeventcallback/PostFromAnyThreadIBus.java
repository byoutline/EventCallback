package com.byoutline.ottoeventcallback;

import com.byoutline.eventcallback.IBus;
import com.squareup.otto.Bus;

/**
 * Wraps given {@link Bus} it so it implements {@link IBus} and always posts on
 * Android main thread.
 */
public class PostFromAnyThreadIBus implements IBus {
    
    private final Bus bus;
    
    public PostFromAnyThreadIBus(Bus bus) {
        this.bus = bus;
    }
    
    @Override
    public void post(final Object event) {
        PostFromAnyThreadBus.runInMainThread(new Runnable() {
            @Override
            public void run() {
                // We're now in the main loop, we can post now
                bus.post(event);
            }
        });
    }
    
    public void register(Object object) {
        bus.register(object);
    }
}
