package com.byoutline.eventcallback;

import com.byoutline.eventcallback.internal.actions.AtomicBooleanSetter;
import com.byoutline.eventcallback.internal.actions.CreateEvents;
import com.byoutline.eventcallback.internal.actions.ResultEvents;
import com.byoutline.eventcallback.internal.actions.ScheduledActions;
import com.google.gson.reflect.TypeToken;

import javax.annotation.Nonnull;
import java.util.*;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * Creates complete instance of {@link EventCallback} using fluent syntax.
 *
 * @param <S> onSuccess result type
 * @param <E> onError result type
 * @author Sebastian Kacprzak <sebastian.kacprzak at byoutline.com> on 17.06.14.
 */
public class EventCallbackBuilder<S, E> {

    private final CallbackConfig config;
    private final TypeToken<E> validationErrorTypeToken;
    private final String callbackStartSessionId;

    private final ScheduledActions<CreateEvents> onCreateActions = getCreateSheduledActionInstance();
    private final ScheduledActions<ResultEvents<S>> onSuccessActions = new ScheduledActions<ResultEvents<S>>(new ResultEvents<S>(), new ResultEvents<S>(), new ArrayList<AtomicBooleanSetter>());
    private final ScheduledActions<ResultEvents<E>> onErrorActions = new ScheduledActions<ResultEvents<E>>(new ResultEvents<E>(), new ResultEvents<E>(), new ArrayList<AtomicBooleanSetter>());
    private final Map<Integer, ScheduledActions<CreateEvents>> onStatusCodeActions = new HashMap<Integer, ScheduledActions<CreateEvents>>();

    public EventCallbackBuilder(@Nonnull CallbackConfig config,
                                @Nonnull TypeToken<E> validationErrorTypeToken) {
        this.config = config;
        this.validationErrorTypeToken = validationErrorTypeToken;
        this.callbackStartSessionId = config.sessionIdProvider.get();
    }

    private static ScheduledActions<CreateEvents> getCreateSheduledActionInstance() {
        return new ScheduledActions<CreateEvents>(new CreateEvents(), new CreateEvents(), new ArrayList<AtomicBooleanSetter>());
    }

    public ActionsSetter<S, E> onCreate() {
        return new ActionsSetter<S, E>(this, onCreateActions);
    }

    public ResultEventsSetter<S, S, E> onSuccess() {
        return new ResultEventsSetter<S, S, E>(this, onSuccessActions);
    }

    public ResultEventsSetter<E, S, E> onError() {
        return new ResultEventsSetter<E, S, E>(this, onErrorActions);
    }

    public ActionsSetter<S, E> onStatusCodes(Integer... statusCodes) {
        List<ScheduledActions<? extends CreateEvents>> actions = new ArrayList<ScheduledActions<? extends CreateEvents>>(statusCodes.length);
        for (Integer statusCode : statusCodes) {
            if (!onStatusCodeActions.containsKey(statusCode)) {
                onStatusCodeActions.put(statusCode, getCreateSheduledActionInstance());
            }
            actions.add(onStatusCodeActions.get(statusCode));
        }
        return new ActionsSetter<S, E>(this, actions.toArray(new ScheduledActions[statusCodes.length]));
    }

    public static class ActionsSetter<S, E> {

        private final ScheduledActions<? extends CreateEvents>[] actions;
        protected final EventCallbackBuilder<S, E> builder;

        private ActionsSetter(EventCallbackBuilder<S, E> builder, ScheduledActions<? extends CreateEvents>... actions) {
            this.actions = actions;
            this.builder = builder;
        }

        public ExpireSetter<Object, S, E> postEvents(Object... events) {
            return new ExpireSetter<Object, S, E>(Arrays.asList(events), builder, actions);
        }

        public BoolSetter<S, E> setAtomicBooleans(AtomicBoolean... booleans) {
            return new BoolSetter<S, E>(Arrays.asList(booleans), builder, actions);
        }
    }

    public static class ResultEventsSetter<R, S, E> extends ActionsSetter<S, E> {

        private final ScheduledActions<ResultEvents<R>> actions;

        private ResultEventsSetter(EventCallbackBuilder<S, E> builder, ScheduledActions<ResultEvents<R>> actions) {
            super(builder, actions);
            this.actions = actions;
        }

        /**
         * @param events events to be posted with response body set. If you want to receive also
         *               status code and headers pass instances of {@link RetrofitResponseEvent}.
         * @return next stage of the builder
         */
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
