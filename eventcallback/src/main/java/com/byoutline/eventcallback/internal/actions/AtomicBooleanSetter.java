package com.byoutline.eventcallback.internal.actions;

import javax.annotation.Nonnull;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * Sets passed {@link AtomicBoolean} to requested value.
 *
 * @author Sebastian Kacprzak <sebastian.kacprzak at byoutline.com>
 */
public class AtomicBooleanSetter {
    public final AtomicBoolean bool;
    public final boolean requestedValue;

    public AtomicBooleanSetter(@Nonnull AtomicBoolean bool, boolean requestedValue) {
        this.bool = bool;
        this.requestedValue = requestedValue;
    }

    public void setRequestedValue() {
        bool.set(requestedValue);
    }

    @Override
    public String toString() {
        return "AtomicBooleanSetter{" + "bool=" + bool + ", requestedValue=" + requestedValue + '}';
    }
}
