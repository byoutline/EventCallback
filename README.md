EventCallback
=============
[![Maven Central](https://maven-badges.herokuapp.com/maven-central/com.byoutline.eventcallback/eventcallback/badge.svg?style=flat)](http://mvnrepository.com/artifact/com.byoutline.eventcallback/eventcallback)
[![Build Status](https://travis-ci.org/byoutline/EventCallback.svg?branch=master)](https://travis-ci.org/byoutline/EventCallback) [![Coverage Status](https://coveralls.io/repos/byoutline/EventCallback/badge.svg?branch=master)](https://coveralls.io/r/byoutline/EventCallback?branch=master)

EventCallback allows creating instances of [Retrofit](http://square.github.io/retrofit/) [callbacks](http://square.github.io/retrofit/javadoc/retrofit/Callback.html) using short, fluent syntax.

Instead of creating classes manually (where you have to take care not to leak anything)
```java
new Callback<SuccessDTO>() {

    @Override
    public void success(SuccessDTO s, Response response) {
        boolean stillSameSession = myCodeCheckingIfItIsStillSameSession();
        if(stillSameSession) {
            bus.post(new MyEvent());
            bus.post(new SuccessEvent());
        }
    }

    @Override
    public void failure(RetrofitError error) {
        RestErrorWithMsg restErrorWithMsg = myCodeThatTriesToConvertRetrofitErrorToReasonCallFailed(error);
        bus.post(new LoginValidationFailedEvent(restErrorWithMsg));
    }
};
```
you can use EventCallback like this:
```java
EventCallback.<SuccessDTO>builder(config, new TypeToken<RestErrorWithMsg>(){})
    .onSuccess().postEvents(new MyEvent(), new SuccessEvent()).validThisSessionOnly()
    .onError().postResponseEvents(new LoginValidationFailedEvent()).validBetweenSessions()
    .build();
``` 

How to use
----------
##### Including dependency #####
Add to your ```build.gradle```:
```groovy
compile 'com.byoutline.eventcallback:eventcallback:1.3.0'
```

##### Init common settings #####
In many cases you may want to use same config and error message for single endpoint. 

```java
import com.byoutline.eventcallback.CallbackConfig;
import com.byoutline.eventcallback.EventCallbackBuilder;
import com.google.gson.reflect.TypeToken;

public class MyEventCallback<S> extends EventCallbackBuilder<S, RestErrorWithMsg> {

    public static CallbackConfig config;

    private MyEventCallback() {
        super(MyEventCallback.config, new TypeToken<RestErrorWithMsg>() {});
    }

    public static <S> MyEventCallback<S> builder() {
        return new MyEventCallback<>();
    }
}
```

##### Create callback where you need them #####
```java
MyEventCallback.<UserResponse>builder()
               .onSuccess().postResponseEvents(new MyEvent()).validBetweenSessions()
               .onError().postEvents(new LoginValidationFailedEvent()).validBetweenSessions()
               .build();
```

Latest Changes
--------------
* 1.3.1 StubSessionIdProvider - empty implementation of session id provider, for projects that do not have session.
* 1.3.0 Status code and response headers will be set for events that implement RetrofitResponseEvent interface.
