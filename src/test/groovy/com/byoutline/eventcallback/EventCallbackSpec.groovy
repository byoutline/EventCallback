package com.byoutline.eventcallback

import com.byoutline.eventcallback.ResponseEvent
import com.byoutline.eventcallback.internal.actions.AtomicBooleanSetter
import com.byoutline.eventcallback.internal.actions.CreateEvents
import com.byoutline.eventcallback.internal.actions.ResultEvents
import com.byoutline.eventcallback.internal.actions.ScheduledActions
import javax.inject.Provider
import com.google.gson.reflect.TypeToken
import retrofit.Callback
import retrofit.RetrofitError
import spock.lang.Shared
import spock.lang.Unroll

/**
 *
 * @author Sebastian Kacprzak <nait at naitbit.com> on 27.06.14.
 */
class EventCallbackSpec extends spock.lang.Specification {
    @Shared
    String event = "event"
    IBus bus
    
    def setup() {
        bus = Mock()
    }
    
    @Unroll
    def "onCreate should post #pC times for callback: #cbb"() {
        when:
        cbb.config.bus.impl = bus
        cbb.build()
        
        then:
        pC  * bus.post(_) >> { assert it[0] == event }
        
        where:
        pC | cbb
        1  | MockFactory.getSameSessionBuilder(new BusProvider()).onCreate().postEvents(event).validThisSessionOnly()
        1  | MockFactory.getMultiSessionBuilder(new BusProvider()).onCreate().postEvents(event).validBetweenSessions()
        0  | MockFactory.getMultiSessionBuilder(new BusProvider()).onCreate().postEvents(event).validThisSessionOnly()
    }
    
    @Unroll
    def "onSuccess should post ion#pC times for callback: #cb"() {
        when:
        cb.config.bus.impl = bus
        cb.success("s", null)
        
        then:
        pC  * bus.post(_) >> { assert it[0] == event }
        
        where:
        pC | cb
        1  | MockFactory.getSameSessionBuilder(new BusProvider()).onSuccess().postEvents(event).validThisSessionOnly().build()
        1  | MockFactory.getMultiSessionBuilder(new BusProvider()).onSuccess().postEvents(event).validBetweenSessions().build()
        0  | MockFactory.getMultiSessionBuilder(new BusProvider()).onSuccess().postEvents(event).validThisSessionOnly().build()
    }
    
    @Unroll
    def "onError should post #pC times for callback: #cb"() {
        given:
        RetrofitError retrofitError = GroovyMock(RetrofitError)
        retrofitError.isNetworkError() >> false
        retrofitError.getBodyAs(_) >> event
        retrofitError.getBody() >> event
        
        when:
        cb.config.bus.impl = bus
        cb.failure(retrofitError)
        
        then:
        pC  * bus.post(_) >> { assert it[0] == event }
        
        where:
        pC | cb
        1  | MockFactory.getSameSessionBuilder(new BusProvider()).onError().postEvents(event).validThisSessionOnly().build()
        1  | MockFactory.getMultiSessionBuilder(new BusProvider()).onError().postEvents(event).validBetweenSessions().build()
        0  | MockFactory.getMultiSessionBuilder(new BusProvider()).onError().postEvents(event).validThisSessionOnly().build()
    }
}

class StringResponseEvent implements ResponseEvent<String> {
    String response;
            
    void setResponse(String response) {
        this.response = response;
    }
}