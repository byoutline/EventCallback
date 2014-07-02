package com.byoutline.eventcallback.internal.actions;

import com.byoutline.eventcallback.EventCallback;
import java.util.List;
import javax.annotation.Nonnull;
import org.apache.commons.lang3.Validate;

/**
 * Storage of actions that will be invoked in one of {@link EventCallback} steps.
 * 
 * @param <T> Type of events that can be scheduled.
 * @author Sebastian Kacprzak <nait at naitbit.com>
 */
public class ScheduledActions<T extends CreateEvents> {

    public final T sessionOnlyEvents;
    public final T multiSessionEvents;
    public final List<AtomicBooleanSetter> boolsToSet;

    public ScheduledActions(@Nonnull T sessionOnlyEvents, @Nonnull T multiSessionEvents, @Nonnull List<AtomicBooleanSetter> boolsToSet) {
        this.sessionOnlyEvents = sessionOnlyEvents;
        this.multiSessionEvents = multiSessionEvents;
        this.boolsToSet = boolsToSet;
    }

    public void validate() {
        sessionOnlyEvents.validate();
        multiSessionEvents.validate();
        Validate.noNullElements(boolsToSet);
    }

    @Override
    public String toString() {
        return "ScheduledActions{" + "sessionOnlyEvents=" + sessionOnlyEvents + ", multiSessionEvents=" + multiSessionEvents + ", boolsToSet=" + boolsToSet + '}';
    }
}
