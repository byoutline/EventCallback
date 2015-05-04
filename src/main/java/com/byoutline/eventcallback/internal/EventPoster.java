package com.byoutline.eventcallback.internal;

import com.byoutline.eventcallback.IBus;
import com.byoutline.eventcallback.ResponseEvent;
import com.byoutline.eventcallback.RetrofitResponseEvent;
import com.byoutline.eventcallback.internal.actions.AtomicBooleanSetter;
import com.byoutline.eventcallback.internal.actions.CreateEvents;
import com.byoutline.eventcallback.internal.actions.ResultEvents;
import com.byoutline.eventcallback.internal.actions.ScheduledActions;
import retrofit.client.Response;

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

    public <R> void executeResponseActions(ScheduledActions<ResultEvents<R>> actions, R result, Response response,
                                           boolean sameSession, boolean postNullResponse) {
        executeCommonActions(actions, sameSession);
        if (result == null && !postNullResponse) {
            return;
        }
        postResponseEvents(sameSession, postNullResponse, result, response, actions);
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

    private <R> void postResponseEvents(boolean sameSession, boolean postNullResponse, R result, Response response, ScheduledActions<ResultEvents<R>> actions) {
        if (sameSession) {
            postResponseEvents(result, response, actions.sessionOnlyEvents.resultEvents);
        }
        postResponseEvents(result, response, actions.multiSessionEvents.resultEvents);
    }

    private <R> void postResponseEvents(R result, Response response, Iterable<ResponseEvent<R>> events) {
        for (ResponseEvent<R> event : events) {
            event.setResponse(result);
            if(event instanceof RetrofitResponseEvent) {
                RetrofitResponseEvent retrofitEvent = (RetrofitResponseEvent) event;
                retrofitEvent.setHeaders(response.getHeaders());
                retrofitEvent.setStatus(response.getStatus());
            }
            bus.post(event);
        }
    }
}
