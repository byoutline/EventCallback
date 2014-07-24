package com.byoutline.eventcallback

import com.google.gson.reflect.TypeToken
import javax.inject.Provider

static CallbackConfig getSameSessionConfig(IBus bus) {
    def sessionIdProvider = { return "sessionId" } as Provider<String>
    return getConfig(sessionIdProvider, bus)
}

static CallbackConfig getMultiSessionConfig(IBus bus) {
    int i = 1;
    def sessionIdProvider = { return "sessionId" + i++ } as Provider<String>
    return getConfig(sessionIdProvider, bus)
}

static CallbackConfig getConfig(boolean sameSession, IBus bus) {
    return sameSession ? getSameSessionConfig(bus): getMultiSessionConfig(bus);
}

static CallbackConfig getConfig(Provider<String> sessionIdProvider, IBus bus) {
    def sharedSuccessHandlers = [:]
    return new CallbackConfig(true, bus, sessionIdProvider, sharedSuccessHandlers)
}
static EventCallbackBuilder<String, String> getSameSessionBuilder(IBus bus) {
    return getEventCallbackBuilder(getSameSessionConfig(bus))
}

static EventCallbackBuilder<String, String> getMultiSessionBuilder(IBus bus) {
    return getEventCallbackBuilder(getMultiSessionConfig(bus))
}

static EventCallbackBuilder<String, String> getEventCallbackBuilder(CallbackConfig config) {
    return EventCallback.builder(config, new TypeToken<String>() {})
}

class StubBus implements IBus {
    int postCount = 0
    
    void post(Object o) {
        postCount++
    }
}

