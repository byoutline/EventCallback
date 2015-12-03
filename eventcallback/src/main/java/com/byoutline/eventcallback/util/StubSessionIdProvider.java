package com.byoutline.eventcallback.util;

import javax.inject.Provider;

/**
 * Session id provider that always return same session. <br />
 * Can be useful for apps that do not have concept of session.
 *
 * @author Sebastian Kacprzak <sebastian.kacprzak at byoutline.com>
 */
public class StubSessionIdProvider implements Provider<String> {
    @Override
    public String get() {
        return "";
    }
}
