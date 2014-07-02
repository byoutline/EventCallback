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
    Bus bus
    
    def setup() {
        bus = Mock()
    }
    
    @Unroll
    def "onCreate should post #pC times and postSticky #pSC times for callback: #cb"() {
        when:
        cbb.config.bus.impl = bus
        cbb.build()
        
        then:
        pC  * bus.post(_) >> { assert it[0] == event }
        pSC * bus.postSticky(_) >> { assert it[0] == event }
        
        where:
        pC | pSC | cbb
        1  | 0   | MockFactory.getSameSessionBuilder(new BusProvider()).onCreate().postEvents(event).validThisSessionOnly().notSticky()
        1  | 0   | MockFactory.getMultiSessionBuilder(new BusProvider()).onCreate().postEvents(event).validBetweenSessions().notSticky()
        0  | 0   | MockFactory.getMultiSessionBuilder(new BusProvider()).onCreate().postEvents(event).validThisSessionOnly().notSticky()
        0  | 1   | MockFactory.getSameSessionBuilder(new BusProvider()).onCreate().postEvents(event).validThisSessionOnly().asSticky()
        0  | 1   | MockFactory.getMultiSessionBuilder(new BusProvider()).onCreate().postEvents(event).validBetweenSessions().asSticky()
        0  | 0   | MockFactory.getMultiSessionBuilder(new BusProvider()).onCreate().postEvents(event).validThisSessionOnly().asSticky()
    }
    
    @Unroll
    def "onSuccess should post ion#pC times and postSticky #pSC times for callback: #cb"() {
        when:
        cb.config.bus.impl = bus
        cb.success("s", null)
        
        then:
        pC  * bus.post(_) >> { assert it[0] == event }
        pSC * bus.postSticky(_) >> { assert it[0] == event }
        
        where:
        pC | pSC | cb
        1  | 0   | MockFactory.getSameSessionBuilder(new BusProvider()).onSuccess().postEvents(event).validThisSessionOnly().notSticky().build()
        1  | 0   | MockFactory.getMultiSessionBuilder(new BusProvider()).onSuccess().postEvents(event).validBetweenSessions().notSticky().build()
        0  | 0   | MockFactory.getMultiSessionBuilder(new BusProvider()).onSuccess().postEvents(event).validThisSessionOnly().notSticky().build()
        0  | 1   | MockFactory.getSameSessionBuilder(new BusProvider()).onSuccess().postEvents(event).validThisSessionOnly().asSticky().build()
        0  | 1   | MockFactory.getMultiSessionBuilder(new BusProvider()).onSuccess().postEvents(event).validBetweenSessions().asSticky().build()
        0  | 0   | MockFactory.getMultiSessionBuilder(new BusProvider()).onSuccess().postEvents(event).validThisSessionOnly().asSticky().build()
    }
    
    @Unroll
    def "onError should post #pC times and postSticky #pSC times for callback: #cb"() {
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
        pSC * bus.postSticky(_) >> { assert it[0] == event }
        
        where:
        pC | pSC | cb
        1  | 0   | MockFactory.getSameSessionBuilder(new BusProvider()).onError().postEvents(event).validThisSessionOnly().notSticky().build()
        1  | 0   | MockFactory.getMultiSessionBuilder(new BusProvider()).onError().postEvents(event).validBetweenSessions().notSticky().build()
        0  | 0   | MockFactory.getMultiSessionBuilder(new BusProvider()).onError().postEvents(event).validThisSessionOnly().notSticky().build()
        0  | 1   | MockFactory.getSameSessionBuilder(new BusProvider()).onError().postEvents(event).validThisSessionOnly().asSticky().build()
        0  | 1   | MockFactory.getMultiSessionBuilder(new BusProvider()).onError().postEvents(event).validBetweenSessions().asSticky().build()
        0  | 0   | MockFactory.getMultiSessionBuilder(new BusProvider()).onError().postEvents(event).validThisSessionOnly().asSticky().build()
    }
}

class StringResponseEvent implements ResponseEvent<String> {
    String response;
            
    void setResponse(String response) {
        this.response = response;
    }
}