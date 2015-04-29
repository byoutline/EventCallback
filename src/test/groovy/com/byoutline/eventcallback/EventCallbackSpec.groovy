package com.byoutline.eventcallback

import com.byoutline.eventcallback.ResponseEvent
import com.byoutline.eventcallback.internal.EventPoster
import com.byoutline.eventcallback.internal.actions.AtomicBooleanSetter
import com.byoutline.eventcallback.internal.actions.CreateEvents
import com.byoutline.eventcallback.internal.actions.ResultEvents
import com.byoutline.eventcallback.internal.actions.ScheduledActions
import retrofit.client.Header

import javax.inject.Provider
import com.google.gson.reflect.TypeToken
import retrofit.Callback
import retrofit.RetrofitError
import retrofit.client.Response
import retrofit.mime.TypedInput
import spock.lang.Shared
import spock.lang.Unroll

/**
 *
 * @author Sebastian Kacprzak <sebastian.kacprzak at byoutline.com> on 27.06.14.
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
        1  | MockFactory.getSameSessionBuilder(new BusProvider()).onCreate().postEvents(event).validBetweenSessions()
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
        1  | MockFactory.getSameSessionBuilder(new BusProvider()).onSuccess().postEvents(event).validBetweenSessions().build()
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
        1  | MockFactory.getSameSessionBuilder(new BusProvider()).onError().postEvents(event).validBetweenSessions().build()
        1  | MockFactory.getMultiSessionBuilder(new BusProvider()).onError().postEvents(event).validBetweenSessions().build()
        0  | MockFactory.getMultiSessionBuilder(new BusProvider()).onError().postEvents(event).validThisSessionOnly().build()
    }
    
    @Unroll
    def "onStatusCodes shold post #pc times on 200 success for callback: #cb"() {
        given:
        Response response = new Response("url", 200, "reason", [], null)
        
        when:
        cb.config.bus.impl = bus
        cb.success("s", response)
        
        then:
        pC  * bus.post(_) >> { assert it[0] == event }
        
        where:
        pC | cb
        1  | MockFactory.getSameSessionBuilder(new BusProvider()).onStatusCodes(200).postEvents(event).validThisSessionOnly().build()
        1  | MockFactory.getSameSessionBuilder(new BusProvider()).onStatusCodes(200).postEvents(event).validBetweenSessions().build()
        1  | MockFactory.getMultiSessionBuilder(new BusProvider()).onStatusCodes(200).postEvents(event).validBetweenSessions().build()
        0  | MockFactory.getMultiSessionBuilder(new BusProvider()).onStatusCodes(200).postEvents(event).validThisSessionOnly().build()
        2  | MockFactory.getSameSessionBuilder(new BusProvider())
        .onStatusCodes(200).postEvents(event).validThisSessionOnly()
        .onStatusCodes(200).postEvents(event).validThisSessionOnly().build()
    }
    
    @Unroll
    def "onStatusCodes shold post #pc times on 400 failure for callback: #cb"() {
        given:
        RetrofitError retrofitError = GroovyMock(RetrofitError)
        Response response = new Response("url", 400, "reason", [], null)
        retrofitError.getResponse() >> response
        
        when:
        cb.config.bus.impl = bus
        cb.failure(retrofitError)
        
        then:
        pC  * bus.post(_) >> { assert it[0] == event }
        
        where:
        pC | cb
        1  | MockFactory.getSameSessionBuilder(new BusProvider()).onStatusCodes(400).postEvents(event).validThisSessionOnly().build()
        1  | MockFactory.getSameSessionBuilder(new BusProvider()).onStatusCodes(400).postEvents(event).validBetweenSessions().build()
        1  | MockFactory.getMultiSessionBuilder(new BusProvider()).onStatusCodes(400).postEvents(event).validBetweenSessions().build()
        0  | MockFactory.getMultiSessionBuilder(new BusProvider()).onStatusCodes(400).postEvents(event).validThisSessionOnly().build()
        
        2  | MockFactory.getSameSessionBuilder(new BusProvider())
        .onStatusCodes(400).postEvents(event).validThisSessionOnly()
        .onStatusCodes(400).postEvents(event).validThisSessionOnly().build()
        
        0  | MockFactory.getSameSessionBuilder(new BusProvider()).onStatusCodes(401).postEvents(event).validThisSessionOnly().build()
        0  | MockFactory.getSameSessionBuilder(new BusProvider()).onStatusCodes(401).postEvents(event).validBetweenSessions().build()
        0  | MockFactory.getMultiSessionBuilder(new BusProvider()).onStatusCodes(401).postEvents(event).validBetweenSessions().build()
        0  | MockFactory.getMultiSessionBuilder(new BusProvider()).onStatusCodes(401).postEvents(event).validThisSessionOnly().build()
    }
    
    
    @Unroll
    def "shared success handlers should be called #callCount when server returns #response"() {
        given:
        SuccessHandler<String> handler = Mock()
        def cb = MockFactory.getEventCallbackWithSucccessHandler(bus, handler)
        
        when:
        cb.success(response, null)
        
        then:
        callCount * handler.onCallSuccess(_)
        
        where:
        callCount   | response
        0           | null
        0           | 32
        1           | "string response"
    }

    @Unroll
    def "should EventPoster executeResponseActions as events with response, headers and status"() {
        given:
        def headers = [new Header("Pragma", "no-cache")]
        def response = new Response("url", 202, "reason", headers, null)
        def responseEvent = new StringResponseEvent("body", response.headers, response.status)

        def sessionOnlyEvents = new ResultEvents([responseEvent], []);
        def multiSessionEvents = new ResultEvents([], []);
        ScheduledActions<ResultEvents<String>> actions = new ScheduledActions(sessionOnlyEvents, multiSessionEvents, [])

        when:
        boolean sameSession = true
        boolean postNullResponse = true
        new EventPoster(bus).executeResponseActions(actions, responseEvent.response, response, sameSession, postNullResponse)

        then:
        pC * bus.post(_) >> {
            def e = ((StringResponseEvent) it[0])
            assert e.response == responseEvent.response
            assert e.headers == response.headers
            assert e.status == response.status
        }

        where:
        pC | cb
        1  | MockFactory.getSameSessionBuilder(new BusProvider()).onSuccess().postEvents().validThisSessionOnly().build()
    }
}

class StringResponseEvent implements ResponseEvent<String> {
    String response
    List<Header> headers
    int status

    public StringResponseEvent(String response, List<Header> headers, int status) {
        this.response = response
        this.headers = headers
        this.status = status
    }

    @Override
    void setResponse(String response) {
        this.response = response
    }

    @Override
    void setHeaders(List<Header> headers) {
        this.headers = headers
    }

    @Override
    void setStatus(int status) {
        this.status = status
    }
}