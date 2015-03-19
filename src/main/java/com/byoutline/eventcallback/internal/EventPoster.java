package com.byoutline.eventcallback.internal;

import com.byoutline.eventcallback.IBus;
import com.byoutline.eventcallback.ResponseEvent;
import com.byoutline.eventcallback.internal.actions.AtomicBooleanSetter;
import com.byoutline.eventcallback.internal.actions.CreateEvents;
import com.byoutline.eventcallback.internal.actions.ResultEvents;
import com.byoutline.eventcallback.internal.actions.ScheduledActions;

/**
 * @author Sebastian Kacprzak <sebastian.kacprzak at byoutline.com> on 26.06.14.
 */
public class EventPoster {

    private final IBus bus;

    public EventPoster(IBus bus) {
        this.bus = bus;
    }

    public void executeCommonActions(ScheduledActions actions, boolean isSameSession) {
        Iterable<AtomicBooleanSetter> boolsToSet = actions.boolsToSet;
        setBools(boolsToSet);
        if (isSameSession) {
            postAll(actions.sessionOnlyEvents);
        }
        postAll(actions.multiSessionEvents);
    }

    public <R> void executeResponseActions(ScheduledActions<ResultEvents<R>> actions, R response,
                                           boolean sameSession, boolean postNullResponse) {
        executeCommonActions(actions, sameSession);
        if (response == null && !postNullResponse) {
            return;
        }
        postResponseEvents(sameSession, postNullResponse, response, actions);
    }

    private static void setBools(Iterable<AtomicBooleanSetter> boolsToSet) {
        for (AtomicBooleanSetter boolToSet : boolsToSet) {
            boolToSet.setRequestedValue();
        }
    }

    private void postAll(CreateEvents events) {
        postAll(events.events);
    }

    private void postAll(Iterable events) {
        for (Object event : events) {
            bus.post(event);
        }
    }

    private <R> void postResponseEvents(boolean sameSession, boolean postNullResponse, R response, ScheduledActions<ResultEvents<R>> actions) {
        if (sameSession) {
            postResponseEvents(response, actions.sessionOnlyEvents.resultEvents);
        }
        postResponseEvents(response, actions.multiSessionEvents.resultEvents);
    }

    private <R> void postResponseEvents(R response, Iterable<ResponseEvent<R>> events) {
        for (ResponseEvent<R> event : events) {
            event.setResponse(response);
            bus.post(event);
        }
    }
}
