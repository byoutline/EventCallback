package com.byoutline.eventcallback

import retrofit.RetrofitError
import retrofit.client.Header
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
        pC * bus.post(_) >> { assert it[0] == event }

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
        pC * bus.post(_) >> { assert it[0] == event }

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
        pC * bus.post(_) >> { assert it[0] == event }

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
        pC * bus.post(_) >> { assert it[0] == event }

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
        pC * bus.post(_) >> { assert it[0] == event }

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
        callCount | response
        0         | null
        0         | 32
        1         | "string response"
    }

    def "onSuccess should post events with response, headers and status set when passed RetrofitResponseEvent"() {
        given:
        TypedInput body = null
        def headers = [new Header("Pragma", "no-cache")]
        def response = new Response("url", 202, "reason", headers, body)

        def cb = MockFactory.getSameSessionBuilder(new BusProvider())
                .onSuccess().postResponseEvents(new RetrofitResponseEventImpl<String>()).validBetweenSessions()
                .build()
        cb.config.bus.impl = bus

        when:
        cb.success(body, response)

        then:
        1 * bus.post(_) >> {
            def result = ((RetrofitResponseEventImpl) it[0])
            assert result.getResponse() == response.getBody()
            assert result.getHeaders() == response.getHeaders()
            assert result.getStatus() == response.getStatus()
        }
    }
}
