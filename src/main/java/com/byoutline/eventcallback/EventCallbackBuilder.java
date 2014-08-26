package com.byoutline.eventcallback;

import com.byoutline.eventcallback.internal.actions.AtomicBooleanSetter;
import com.byoutline.eventcallback.internal.actions.CreateEvents;
import com.byoutline.eventcallback.internal.actions.ResultEvents;
import com.byoutline.eventcallback.internal.actions.ScheduledActions;
import com.google.gson.reflect.TypeToken;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicBoolean;
import javax.annotation.Nonnull;

/**
 * Creates complete instance of {@link EventCallback} using fluent syntax.
 *
 * @author Sebastian Kacprzak <sebastian.kacprzak at byoutline.com> on 17.06.14.
 * @param <S> onSuccess result type
 * @param <E> onError result type
 */
public class EventCallbackBuilder<S, E> {

    private final CallbackConfig config;
    private final TypeToken<E> validationErrorTypeToken;
    private final String callbackStartSessionId;

    private final ScheduledActions<CreateEvents> onCreateActions = getCreateSheduledActionInstance();
    private final ScheduledActions<ResultEvents<S>> onSuccessActions = new ScheduledActions<>(new ResultEvents<S>(), new ResultEvents<S>(), new ArrayList<AtomicBooleanSetter>());
    private final ScheduledActions<ResultEvents<E>> onErrorActions = new ScheduledActions<>(new ResultEvents<E>(), new ResultEvents<E>(), new ArrayList<AtomicBooleanSetter>());
    private final Map<Integer, ScheduledActions<CreateEvents>> onStatusCodeActions = new HashMap<>();

    public EventCallbackBuilder(@Nonnull CallbackConfig config,
            @Nonnull TypeToken<E> validationErrorTypeToken) {
        this.config = config;
        this.validationErrorTypeToken = validationErrorTypeToken;
        this.callbackStartSessionId = config.sessionIdProvider.get();
    }

    private static ScheduledActions<CreateEvents> getCreateSheduledActionInstance() {
        return new ScheduledActions<>(new CreateEvents(), new CreateEvents(), new ArrayList<AtomicBooleanSetter>());
    }

    public ActionsSetter<S, E> onCreate() {
        return new ActionsSetter<>(this, onCreateActions);
    }

    public ResultEventsSetter<S, S, E> onSuccess() {
        return new ResultEventsSetter<>(this, onSuccessActions);
    }

    public ResultEventsSetter<E, S, E> onError() {
        return new ResultEventsSetter<>(this, onErrorActions);
    }

    public ActionsSetter<S, E> onStatusCodes(Integer... statusCodes) {
        List<ScheduledActions<? extends CreateEvents>> actions = new ArrayList<>(statusCodes.length);
        for (Integer statusCode : statusCodes) {
            if (!onStatusCodeActions.containsKey(statusCode)) {
                onStatusCodeActions.put(statusCode, getCreateSheduledActionInstance());
            }
            actions.add(onStatusCodeActions.get(statusCode));
        }
        return new ActionsSetter<>(this, actions.toArray(new ScheduledActions[statusCodes.length]));
    }

    public static class ActionsSetter<S, E> {

        private final ScheduledActions<? extends CreateEvents>[] actions;
        protected final EventCallbackBuilder<S, E> builder;

        private ActionsSetter(EventCallbackBuilder<S, E> builder, ScheduledActions<? extends CreateEvents>... actions) {
            this.actions = actions;
            this.builder = builder;
        }

        public ExpireSetter<Object, S, E> postEvents(Object... events) {
            return new ExpireSetter(Arrays.asList(events), builder, actions);
        }

        public BoolSetter<S, E> setAtomicBooleans(AtomicBoolean... booleans) {
            return new BoolSetter<>(Arrays.asList(booleans), builder, actions);
        }
    }

    public static class ResultEventsSetter<R, S, E> extends ActionsSetter {

        private final ScheduledActions<ResultEvents<R>> actions;

        private ResultEventsSetter(EventCallbackBuilder<S, E> builder, ScheduledActions<ResultEvents<R>> actions) {
            super(builder, actions);
            this.actions = actions;
        }

        public ResultExpireSetter<R, S, E> postResponseEvents(ResponseEvent<R>... events) {
            return new ResultExpireSetter(Arrays.asList(events), builder, actions);
        }
    }

    public static class ExpireSetter<R, S, E> {

        private final ScheduledActions<? extends CreateEvents>[] actions;
        private final List<R> argEvents;
        private final EventCallbackBuilder<S, E> builder;

        private ExpireSetter(List<R> argEvents, EventCallbackBuilder<S, E> builder, ScheduledActions<? extends CreateEvents>... actions) {
            this.actions = actions;
            this.argEvents = argEvents;
            this.builder = builder;
        }

        public EventCallbackBuilder<S, E> validThisSessionOnly() {
            for (ScheduledActions<? extends CreateEvents> action : actions) {
                addEvents(action.sessionOnlyEvents.events);
            }
            return builder;
        }

        public EventCallbackBuilder<S, E> validBetweenSessions() {
            for (ScheduledActions<? extends CreateEvents> action : actions) {
                addEvents(action.multiSessionEvents.events);
            }
            return builder;
        }

        private void addEvents(List<R> events) {
            events.addAll(argEvents);
        }
    }

    public static class ResultExpireSetter<R, S, E> {

        private final ScheduledActions<ResultEvents<R>> actions;
        private final List<ResponseEvent<R>> argEvents;
        private final EventCallbackBuilder<S, E> builder;

        private ResultExpireSetter(List<ResponseEvent<R>> argEvents, EventCallbackBuilder<S, E> builder, ScheduledActions<ResultEvents<R>> actions) {
            this.actions = actions;
            this.argEvents = argEvents;
            this.builder = builder;
        }

        public EventCallbackBuilder<S, E> validThisSessionOnly() {
            return addEvents(actions.sessionOnlyEvents.resultEvents);
        }

        public EventCallbackBuilder<S, E> validBetweenSessions() {
            return addEvents(actions.multiSessionEvents.resultEvents);
        }

        private EventCallbackBuilder<S, E> addEvents(List<ResponseEvent<R>> events) {
            events.addAll(argEvents);
            return builder;
        }
    }

    public static class BoolSetter<S, E> {

        private final ScheduledActions<? extends CreateEvents>[] actions;
        private final List<AtomicBoolean> booleans;
        private final EventCallbackBuilder<S, E> builder;

        private BoolSetter(List<AtomicBoolean> booleans, EventCallbackBuilder<S, E> builder, ScheduledActions<? extends CreateEvents>... actions) {
            this.actions = actions;
            this.booleans = booleans;
            this.builder = builder;
        }

        private void addAll(boolean value) {
            for (ScheduledActions<? extends CreateEvents> action : actions) {
                for (AtomicBoolean bool : booleans) {
                    action.boolsToSet.add(new AtomicBooleanSetter(bool, value));
                }
            }
        }

        public EventCallbackBuilder<S, E> toFalse() {
            return toValue(false);
        }

        public EventCallbackBuilder<S, E> toTrue() {
            return toValue(true);
        }

        public EventCallbackBuilder<S, E> toValue(boolean value) {
            addAll(value);
            return builder;
        }
    }

    public EventCallback<S, E> build() {
        return new EventCallback(config, validationErrorTypeToken,
                callbackStartSessionId,
                onCreateActions, onSuccessActions, 
                onErrorActions, onStatusCodeActions);
    }
}
