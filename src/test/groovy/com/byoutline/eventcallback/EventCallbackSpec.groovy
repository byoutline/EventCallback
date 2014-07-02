package com.byoutline.eventcallback

import com.byoutline.eventcallback.events.ResponseEvent
import javax.inject.Provider
import retrofit.Callback
import retrofit.RetrofitError

/**
 *
 * @author Sebastian Kacprzak <nait at naitbit.com> on 27.06.14.
 */
class EventCallbackSpec extends spock.lang.Specification {
    Bus bus;
    def setup() {
        bus = Mock()
    }
       
    def "onSuccess should post sessionOnly events in same session"() {
        given:
        Callback<String, String> cb = MockFactory.getSameSessionBuilder(bus)
        .postSessionOnlyEvents("success")
        .build();
        
        when:
        cb.success("s", null)
        
        then:
        1 * bus.post("success")
        0 * bus.postSticky(_)
    }
    
    def "onSuccess should post multisession events in same session"() {
        given:
        Callback<String, String> cb = MockFactory.getSameSessionBuilder(bus)
        .postMultiSessionEvents("success")
        .build();
        
        when:
        cb.success("s", null)
        
        then:
        1 * bus.post("success")
        0 * bus.postSticky(_)
    }
    
    def "onSuccess should post multisession events between sessions"() {
        given:
        Callback<String, String> cb = MockFactory.getMultiSessionBuilder(bus)
        .postMultiSessionEvents("success")
        .build();
        
        when:
        cb.success("s", null)
        
        then:
        1 * bus.post("success")
        0 * bus.postSticky(_)
    }
    
    def "onSuccess should NOT post sessionOnly events between sessions"() {
        given:
        Callback<String, String> cb = MockFactory.getMultiSessionBuilder(bus)
        .postSessionOnlyEvents("success")
        .build();
        
        when:
        cb.success("s", null)
        
        then:
        0 * bus.post(_)
        0 * bus.postSticky(_)
    }
    
    def "onSuccess should post sticky events between sessions"() {
        given:
        Callback<String, String> cb = MockFactory.getMultiSessionBuilder(bus)
        .postStickyEvents("success")
        .build();
        
        when:
        cb.success("s", null)
        
        then:
        0 * bus.post(_)
        1 * bus.postSticky("success")
    }
    
    def "onError should post validationErrorEvent during same session"() {
        given:
        RetrofitError retrofitError = GroovyMock(RetrofitError)
        retrofitError.isNetworkError() >> false
        retrofitError.getBodyAs(_) >> "error"
        retrofitError.getBody() >> "error"
        
        StringResponseEvent event = new StringResponseEvent()
        
        Callback<String, String> cb = MockFactory.getSameSessionBuilder(bus)
        .onErrorPostValidationErrorEvent(event)
        .build();
        
        when:
        cb.failure(retrofitError)
        
        then:
        event.response == "error"
        0 * bus.post(_)
        1 * bus.postSticky(event)
    }
    
    def "onError should NOT post validationErrorEvent between sessions"() {
        given:
        RetrofitError retrofitError = GroovyMock(RetrofitError)
        retrofitError.isNetworkError() >> false
        retrofitError.getBodyAs(_) >> "error"
        retrofitError.getBody() >> "error"
        
        StringResponseEvent event = new StringResponseEvent()
        
        Callback<String, String> cb = MockFactory.getMultiSessionBuilder(bus)
        .onErrorPostValidationErrorEvent(event)
        .build();
        
        when:
        cb.failure(retrofitError)
        
        then:
        event.response == null
        0 * bus.post(_)
        0 * bus.postSticky(_)
    }
}

class StringResponseEvent implements ResponseEvent<String> {
    String response;
            
    void setResponse(String response) {
        this.response = response;
    }
}