package com.byoutline.eventcallback;

import com.byoutline.eventcallback.internal.actions.AtomicBooleanSetter;
import com.byoutline.eventcallback.internal.actions.ScheduledActions;
import com.byoutline.eventcallback.internal.actions.CreateEvents;
import com.byoutline.eventcallback.internal.actions.ResultEvents;
import com.google.gson.reflect.TypeToken;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;
import javax.annotation.Nonnull;

/**
 * Creates complete instance of {@link EventCallback} using fluent syntax.
 * 
 * @author Sebastian Kacprzak <nait at naitbit.com> on 17.06.14.
 */
public class EventCallbackBuilder<S, E> {

    private final CallbackConfig config;
    private final TypeToken<E> validationErrorTypeToken;
    private final String callbackStartSessionId;

    private final ScheduledActions<CreateEvents> onCreateActions = new ScheduledActions<>(new CreateEvents(), new CreateEvents(), new ArrayList<AtomicBooleanSetter>());
    private final ScheduledActions<ResultEvents<S>> onSuccessActions = new ScheduledActions<>(new ResultEvents<S>(), new ResultEvents<S>(), new ArrayList<AtomicBooleanSetter>());
    private final ScheduledActions<ResultEvents<E>> onErrorActions = new ScheduledActions<>(new ResultEvents<E>(), new ResultEvents<E>(), new ArrayList<AtomicBooleanSetter>());

    public EventCallbackBuilder(@Nonnull CallbackConfig config,
            @Nonnull TypeToken<E> validationErrorTypeToken) {
        this.config = config;
        this.validationErrorTypeToken = validationErrorTypeToken;
        this.callbackStartSessionId = config.sessionIdProvider.get();
    }
    
    public ActionsSetter<S, E> onCreate() {
        return new ActionsSetter<>(onCreateActions, this);
    }
    
    public ResultEventsSetter<S, S, E> onSuccess() {
        return new ResultEventsSetter<>(onSuccessActions, this);
    }
    
    public ResultEventsSetter<E, S, E> onError() {
        return new ResultEventsSetter<>(onErrorActions, this);
    }
    
    public static class ActionsSetter<S, E> {
        private final ScheduledActions<? extends CreateEvents> actions;
        protected final EventCallbackBuilder<S, E> builder;

        private ActionsSetter(ScheduledActions<? extends CreateEvents> actions, EventCallbackBuilder<S, E> builder) {
            this.actions = actions;
            this.builder = builder;
        }

        public ExpireSetter<Object, S, E> postEvents(Object...events){
            return new ExpireSetter(actions, Arrays.asList(events), builder);
        }
        
        public BoolSetter<S,E> setAtomicBooleans(AtomicBoolean...booleans) {
            return new BoolSetter<>(actions, Arrays.asList(booleans), builder);
        }
    }
    
    public static class ResultEventsSetter<R, S, E> extends ActionsSetter {
        private final ScheduledActions<ResultEvents<R>> actions;

        private ResultEventsSetter(ScheduledActions<ResultEvents<R>> actions, EventCallbackBuilder<S, E> builder) {
            super(actions, builder);
            this.actions = actions;
        }
        
        public ResultExpireSetter<R, S, E> postResponseEvents(ResponseEvent<R>...events) {
            return new ResultExpireSetter(actions, Arrays.asList(events), builder);
        }
    }
    
    public static class ExpireSetter<R, S, E> {
        private final ScheduledActions<? extends CreateEvents> actions;
        private final List<R> argEvents;
        private final EventCallbackBuilder<S, E> builder;

        private ExpireSetter(ScheduledActions<? extends CreateEvents> actions, List<R> argEvents, EventCallbackBuilder<S, E> builder) {
            this.actions = actions;
            this.argEvents = argEvents;
            this.builder = builder;
        }

        public StickySetter<R, S, E> validThisSessionOnly() {
            return new StickySetter<>(argEvents, actions.sessionOnlyEvents.events, actions.sessionOnlyEvents.stickyEvents, builder);
        }
        public StickySetter<R, S, E>  validBetweenSessions(){
            return new StickySetter<>(argEvents, actions.multiSessionEvents.events, actions.multiSessionEvents.stickyEvents, builder);
        }
    }
    
    public static class ResultExpireSetter<R, S, E> {
        
        private final ScheduledActions<ResultEvents<R>> actions;
        private final List<ResponseEvent<R>> argEvents;
        private final EventCallbackBuilder<S, E> builder;

        private ResultExpireSetter(ScheduledActions<ResultEvents<R>> actions, List<ResponseEvent<R>> argEvents, EventCallbackBuilder<S, E> builder) {
            this.actions = actions;
            this.argEvents = argEvents;
            this.builder = builder;
        }

        public StickySetter<ResponseEvent<R>, S, E> validThisSessionOnly() {
            return new StickySetter<>(argEvents, actions.sessionOnlyEvents.resultEvents, actions.sessionOnlyEvents.resultStickyEvents, builder);
        }
        
        public StickySetter<ResponseEvent<R>, S, E> validBetweenSessions(){
            return new StickySetter<>(argEvents, actions.multiSessionEvents.resultEvents, actions.multiSessionEvents.resultStickyEvents, builder);
        }
    }
    
    public static class StickySetter<R, S, E> {
        private final List<R> argEvents;
        private final List<R> events;
        private final List<R> stickyEvents;
        private final EventCallbackBuilder<S, E> builder;

        private StickySetter(List<R> argEvents, List<R> events, List<R> stickyEvents, EventCallbackBuilder<S, E> builder) {
            this.argEvents = argEvents;
            this.events = events;
            this.stickyEvents = stickyEvents;
            this.builder = builder;
        }

        public EventCallbackBuilder<S, E> asSticky() {
            stickyEvents.addAll(argEvents);
            return builder;
        }
        
        public EventCallbackBuilder<S, E> notSticky() {
            events.addAll(argEvents);
            return builder;
        }
    }
    
    public static class BoolSetter<S, E> {
        private final ScheduledActions<? extends CreateEvents> actions;
        private final List<AtomicBoolean> booleans;
        private final EventCallbackBuilder<S, E> builder;

        private BoolSetter(ScheduledActions<? extends CreateEvents> actions, List<AtomicBoolean> booleans, EventCallbackBuilder<S, E> builder) {
            this.actions = actions;
            this.booleans = booleans;
            this.builder = builder;
        }
        
        private void addAll(boolean value) {
            for(AtomicBoolean bool: booleans) {
                actions.boolsToSet.add(new AtomicBooleanSetter(bool, value));
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
                onCreateActions, onSuccessActions, onErrorActions);
    }
}
