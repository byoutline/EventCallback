package com.byoutline.eventcallback.internal;

import javax.inject.Provider;

/**
 * Helper class that checks if it is still the same session;
 *
 * @author Sebastian Kacprzak <sebastian.kacprzak at byoutline.com>
 */
public class SessionChecker {

    private final Provider<String> sessionIdProvider;
    public final String callbackStartSessionId;

    public SessionChecker(Provider<String> sessionIdProvider, String callbackStartSessionId) {
        this.sessionIdProvider = sessionIdProvider;
        this.callbackStartSessionId = callbackStartSessionId;
    }

    /**
     * Checks if currently we are during same session that we were during
     * callback creation.
     *
     * @return True if we are still during same session, false otherwise.
     */
    public boolean isSameSession() {
        String sessionId = sessionIdProvider.get();
        if (callbackStartSessionId == null) {
            return sessionId == null;
        }
        return callbackStartSessionId.equals(sessionId);
    }
}
