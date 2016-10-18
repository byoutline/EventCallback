package com.byoutline.ottoeventcallback;

import android.os.Handler;
import android.os.Looper;
import com.byoutline.eventcallback.IBus;
import com.squareup.otto.Bus;
import com.squareup.otto.ThreadEnforcer;

/**
 * Wraps {@link Bus} it so it implements {@link IBus} and always posts on
 * Android main thread.
 */
public class PostFromAnyThreadBus extends Bus implements IBus {

    public PostFromAnyThreadBus() {
        super(ThreadEnforcer.MAIN);
    }

    @Override
    public void post(final Object event) {
        runInMainThread(new Runnable() {
            @Override
            public void run() {
                // We're now in the main loop, we can post now
                PostFromAnyThreadBus.super.post(event);
            }
        });
    }

    public static void runInMainThread(Runnable runnable) {
        if (Looper.myLooper() != Looper.getMainLooper()) {
            // We're not in the main loop, so we need to get into it.
            (new Handler(Looper.getMainLooper())).post(runnable);
        } else {
            runnable.run();
        }
    }
}
