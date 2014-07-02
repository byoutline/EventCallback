package com.byoutline.eventcallback.internal.actions;

import com.byoutline.eventcallback.EventCallback;
import java.util.ArrayList;
import java.util.List;
import javax.annotation.Nonnull;
import org.apache.commons.lang3.Validate;

/**
 * Stores events that can be invoked on {@link EventCallback} creation.
 * 
 * @author Sebastian Kacprzak <nait at naitbit.com>
 */
public class CreateEvents {
    public final List events;
    public final List stickyEvents;

    public CreateEvents() {
        this(new ArrayList<>(), new ArrayList<>());
    }

    public CreateEvents(@Nonnull List events, @Nonnull List stickyEvents) {
        this.events = events;
        this.stickyEvents = stickyEvents;
    }

    void validate() {
        Validate.noNullElements(events);
        Validate.noNullElements(stickyEvents);
    }

    @Override
    public String toString() {
        return "CreateEvents{" + "events=" + events + ", stickyEvents=" + stickyEvents + '}';
    }
}
