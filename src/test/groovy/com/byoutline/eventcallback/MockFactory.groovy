package com.byoutline.eventcallback

import com.google.gson.reflect.TypeToken
import javax.inject.Provider
import retrofit.Callback

static Provider<String> getSameSessionIdProvider() {
    return { return "sessionId" } as Provider<String>
}

static CallbackConfig getSameSessionConfig(IBus bus) {
    def sessionIdProvider = getSameSessionIdProvider()
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

static Callback<String> getEventCallbackWithSucccessHandler(IBus bus, SuccessHandler<String> handler) {
        def sharedSuccessHandlers = [(String.class) : handler]
        def sessionIdProvider = getSameSessionIdProvider()
        def config = new CallbackConfig(true, bus, sessionIdProvider, sharedSuccessHandlers)
        return EventCallback.builder(config, new TypeToken<String>() {}).build()
}

class StubBus implements IBus {
    int postCount = 0
    int postStickyCount = 0
    
    void post(Object o) {
        postCount++
    }
    
    void postSticky(Object o) {
        postStickyCount++
    }
}

