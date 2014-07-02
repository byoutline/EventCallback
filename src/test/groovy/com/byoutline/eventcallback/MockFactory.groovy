package com.byoutline.eventcallback

import com.google.gson.reflect.TypeToken
import javax.inject.Provider

static CallbackConfig getSameSessionConfig(Bus bus) {
    def sessionIdProvider = { return "sessionId" } as Provider<String>
    return getConfig(sessionIdProvider, bus)
}

static CallbackConfig getMultiSessionConfig(Bus bus) {
    int i = 1;
    def sessionIdProvider = { return "sessionId" + i++ } as Provider<String>
    return getConfig(sessionIdProvider, bus)
}

static CallbackConfig getConfig(boolean sameSession, Bus bus) {
    return sameSession ? getSameSessionConfig(bus): getMultiSessionConfig(bus);
}

static CallbackConfig getConfig(Provider<String> sessionIdProvider, Bus bus) {
    def sharedSuccessHandlers = [:]
    return new CallbackConfig(true, bus, sessionIdProvider, sharedSuccessHandlers)
}
static EventCallbackBuilder<String, String> getSameSessionBuilder(Bus bus) {
    return getEventCallbackBuilder(getSameSessionConfig(bus))
}

static EventCallbackBuilder<String, String> getMultiSessionBuilder(Bus bus) {
    return getEventCallbackBuilder(getMultiSessionConfig(bus))
}

static EventCallbackBuilder<String, String> getEventCallbackBuilder(CallbackConfig config) {
    return EventCallback.builder(config, new TypeToken<String>() {})
}

class StubBus implements Bus {
    int postCount = 0
    int postStickyCount = 0
    
    void post(Object o) {
        postCount++
    }
    
    void postSticky(Object o) {
        postStickyCount++
    }
}

