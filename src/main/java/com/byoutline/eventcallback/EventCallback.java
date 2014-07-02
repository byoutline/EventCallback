package com.byoutline.eventcallback;

import com.byoutline.eventcallback.internal.actions.ScheduledActions;
import com.byoutline.eventcallback.internal.actions.CreateEvents;
import com.byoutline.eventcallback.internal.EventPoster;
import com.byoutline.eventcallback.internal.RetrofitErrorConverter;
import com.byoutline.eventcallback.internal.actions.ResultEvents;
import com.google.gson.reflect.TypeToken;
import java.util.Map;
import java.util.concurrent.atomic.AtomicBoolean;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import org.apache.commons.lang3.Validate;
import retrofit.Callback;
import retrofit.RetrofitError;
import retrofit.client.Response;

/**
 * <h2>Callback that can be safely called from fragments and activities.</h2>
 *
 * Can be configured to execute actions during following steps:
 * <ul>
 *   <li> onSuccess (when call was successful) </li>
 *   <li> onError (when call failed) </li>
 *   <li> onCreation (when this event is created) </li>
 * </ul>
 * <br/>
 * Supported actions:
 * <ul>
 *   <li> posting events (sticky and not sticky) to {@link Bus} </li>
 *   <li> posting {@link ResponseEvent}s that have server response set
 *   (available only during onSuccess and onError steps since there is nothing  
 *   to return on creation) </li>
 *   <li> setting {@link AtomicBoolean} to requested value </li>
 *   <li> executing different code from {@link SuccessHandler} for 
 *   all calls that return matched class </li>
 * </ul> 
 * Create instance by calling {@link #builder(com.byoutline.eventcallback.CallbackConfig, com.google.gson.reflect.TypeToken)}.
 *
 * @author Sebastian Kacprzak <nait at naitbit.com> on 17.06.14.
 * @param <S> Type of response returned by server on success.
 * @param <E> Type of response returned by server on error.
 */
public class EventCallback<S, E> implements Callback<S> {

    private final CallbackConfig config;
    private final TypeToken<E> validationErrorTypeToken;
    private final String callbackStartSessionId;

    private final ScheduledActions<CreateEvents> onCreateActions;
    private final ScheduledActions<ResultEvents<S>> onSuccessActions;
    private final ScheduledActions<ResultEvents<E>> onErrorActions;

    private final EventPoster postHelper;

    /**
     * Creates instance. For convenience use 
     * {@link #builder(com.byoutline.eventcallback.CallbackConfig, com.google.gson.reflect.TypeToken, com.google.gson.reflect.TypeToken)} 
     * instead of calling directly.
     */
    EventCallback(@Nonnull CallbackConfig config, @Nullable TypeToken<E> validationErrorTypeToken,
            @Nullable String currentSessionId,
            @Nonnull ScheduledActions<CreateEvents> onCreateActions, @Nonnull ScheduledActions<ResultEvents<S>> onSuccessActions, @Nonnull ScheduledActions<ResultEvents<E>> onErrorActions) {
        Validate.notNull(config);
        this.config = config;

        this.validationErrorTypeToken = validationErrorTypeToken;
        this.callbackStartSessionId = currentSessionId;
        this.onCreateActions = onCreateActions;
        this.onSuccessActions = onSuccessActions;
        this.onErrorActions = onErrorActions;

        if (config.debug) {
            validateArgs();
        }

        postHelper = new EventPoster(config.bus);
        postHelper.executeCommonActions(onCreateActions, isSameSession());
    }

    private void validateArgs() {
        Validate.notNull(validationErrorTypeToken);
        Validate.notNull(onCreateActions);
        Validate.notNull(onSuccessActions);
        Validate.notNull(onErrorActions);
        onCreateActions.validate();
        onSuccessActions.validate();
        onErrorActions.validate();
    }

    /**
     * Returns builder that creates {@link EventCallback}. 
     *
     * To avoid passing all arguments on each callback creation it is suggested
     * to wrap this call in project.
     * For example:
     * 
     * <pre><code class="java">
     * class MyEventCallback&lt;S&gt; {
     *     
     *     CallbackConfig config = injected;
     *     private EventCallbackBuilder&lt;S, MyHandledErrorMsg&gt; builder() {
     *         return EventCallback.builder(config, new TypeToken&lt;MyHandledErrorMsg&gt;(){});
     *     }
     * 
     *     public static &lt;S&gt; EventCallbackBuilder&lt;S, MyHandledErrorMsg&gt; ofType() {
     *         return new MyEventCallback&lt;S&gt;().builder(responseType);
     *     }
     * }
     * </code></pre>
     * 
     * @param <S> Type of response returned by server onSuccess
     * @param <E> Type of response returned by server onError
     * 
     * @param config Shared configuration
     * @param errorTypeToken TypeToken that provides information about expected
     *        response returned by server
     * @return Builder that assists in creating valid EventCallback in readable way.
     */
    public static <S, E> EventCallbackBuilder<S, E> builder(@Nonnull CallbackConfig config,
            @Nonnull TypeToken<E> errorTypeToken) {
        return new EventCallbackBuilder<>(config, errorTypeToken);
    }

    @Override
    public void success(S result, Response response) {
        boolean postNullResponse = true;
        informSharedSuccessHandlers(result);
        postHelper.executeResponseActions(onSuccessActions, result, isSameSession(), postNullResponse);
    }

    @Override
    public void failure(RetrofitError error) {
        boolean postNullResponse = false;
        E convertedError = RetrofitErrorConverter.<E>getAsClassOrNull(validationErrorTypeToken, error);
        postHelper.executeResponseActions(onErrorActions, convertedError, isSameSession(), postNullResponse);
    }

    /**
     * Checks if currently we are during same session that we were during callback
     * creation.
     *
     * @return True if we are still during same session, false otherwise. 
     */
    private boolean isSameSession() {
        String sessionId = config.sessionIdProvider.get();
        if (callbackStartSessionId == null) {
            return sessionId == null;
        }
        return callbackStartSessionId.equals(sessionId);
    }

    /**
     * Passes result to {@link SuccessHandler}s from {@link Callback}
     * @param result response from the server that should be passed to listeners
     */
    private void informSharedSuccessHandlers(S result) {
        for (Map.Entry<Class, SuccessHandler> handler : config.sharedSuccessHandlers.entrySet()) {
            if (handler.getKey().isAssignableFrom(result.getClass())) {
                handler.getValue().onCallSuccess(result);
            }
        }
    }

    @Override
    public String toString() {
        return "EventCallback{" + "config=" + config + ",\n"
                + "validationErrorTypeToken=" + validationErrorTypeToken + ",\n"
                + "callbackStartSessionId=" + callbackStartSessionId + ",\n"
                + "onCreateActions=" + onCreateActions + ",\n"
                + "onSuccessActions=" + onSuccessActions + ",\n"
                + "onErrorActions=" + onErrorActions + '}';
    }
}
