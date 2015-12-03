package com.byoutline.eventcallback;

import com.byoutline.eventcallback.util.StubSessionIdProvider;

import javax.annotation.Nonnull;
import javax.inject.Provider;
import java.util.Collections;
import java.util.Map;

/**
 * Callback that stores project wide settings. It is suggested to Inject it into
 * classes that need ability to create callbacks.
 *
 * @author Sebastian Kacprzak <sebastian.kacprzak at byoutline.com> on 26.06.14.
 */
public class CallbackConfig {

    final boolean debug;
    final IBus bus;
    final Provider<String> sessionIdProvider;
    final Map<Class, SuccessHandler> sharedSuccessHandlers;

    /**
     * Creates instance of default config for callbacks. Uses session provider
     * that always return same session, so there will be no difference between
     * sameSessionOnly and multiSessions events. Debug checks will be turned
     * off.
     *
     * @param bus bus on which callback events will be posted.
     */
    public CallbackConfig(@Nonnull IBus bus) {
        this(false, bus);
    }

    /**
     * Creates instance of default config for callbacks. Uses session provider
     * that always return same session, so there will be no difference between
     * sameSessionOnly and multiSessions events.
     *
     * @param debug true if extra checks should be on.
     * @param bus   bus on which callback events will be posted.
     */
    public CallbackConfig(boolean debug, @Nonnull IBus bus) {
        this(debug, bus, new StubSessionIdProvider());
    }

    /**
     * Creates instance of default config for callbacks.
     *
     * @param debug             true if extra checks should be on.
     * @param bus               bus on which callback events will be posted.
     * @param sessionIdProvider provides information about current session.
     *                          If same string is returned from two calls it is considered to be same session.
     */
    public CallbackConfig(boolean debug, @Nonnull IBus bus,
                          @Nonnull Provider<String> sessionIdProvider) {
        this(debug, bus, sessionIdProvider, Collections.EMPTY_MAP);
    }

    /**
     * Creates instance of default config for callbacks.
     *
     * @param debug                 true if extra checks should be on.
     * @param bus                   bus on which callback events will be posted.
     * @param sessionIdProvider     provides information about current session.
     *                              If same string is returned from two calls it is considered to be same session.
     * @param sharedSuccessHandlers maps success responses from server with
     *                              {@link SuccessHandler}s so common operation for given result type can
     *                              be handled globally in whole project.
     */
    public CallbackConfig(boolean debug, @Nonnull IBus bus,
                          @Nonnull Provider<String> sessionIdProvider,
                          @Nonnull Map<Class, SuccessHandler> sharedSuccessHandlers) {
        this.debug = debug;
        this.bus = bus;
        this.sessionIdProvider = sessionIdProvider;
        this.sharedSuccessHandlers = sharedSuccessHandlers;
    }

    @Override
    public String toString() {
        return "CallbackConfig{" + "debug=" + debug + ", bus=" + bus + ", sessionIdProvider=" + sessionIdProvider + ", sharedSuccessHandlers=" + sharedSuccessHandlers + '}';
    }
}
