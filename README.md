EventCallback
=============
[![Maven Central](https://maven-badges.herokuapp.com/maven-central/com.byoutline.eventcallback/eventcallback/badge.svg?style=flat)](http://mvnrepository.com/artifact/com.byoutline.eventcallback/eventcallback)
[![Build Status](https://travis-ci.org/byoutline/EventCallback.svg?branch=master)](https://travis-ci.org/byoutline/EventCallback) [![Coverage Status](https://coveralls.io/repos/byoutline/EventCallback/badge.svg?branch=master)](https://coveralls.io/r/byoutline/EventCallback?branch=master) 

`EventCallback` allows creating instances of [Retrofit](http://square.github.io/retrofit/) [callbacks](http://square.github.io/retrofit/javadoc/retrofit/Callback.html) using short, fluent syntax. Wrapper for use with [Otto bus](https://github.com/square/otto) is also provided.

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
you can use `EventCallback` like this:
```java
MyEventCallback.<SuccessDTO>builder()
               .onSuccess().postResponseEvents(new MyEvent(), new SuccessEvent()).validThisSessionOnly()
               .onError().postEvents(new LoginValidationFailedEvent()).validBetweenSessions()
               .build();
``` 

How to use
----------
##### Including dependency #####
Add to your ```build.gradle```:
```groovy
compile 'com.byoutline.ottoeventcallback:ottoeventcallback:1.3.2' // If you want to use it with Otto
compile 'com.byoutline.eventcallback:eventcallback:1.3.2' // If you want to use it without otto, or force different eventcallback version
```

##### Init common settings #####
In many cases you may want to use same config and error message for single endpoint. To specify your generic error class you must extend ```EventCallbackBuilder```:

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


and init its default ```CallbackConfig``` in your ```Application``` ```onCreate``` method:
```java
MyEventCallback.config = new CallbackConfig(BuildConfig.DEBUG, bus, sessionIdProvider);
```

Usually dependency injection is used to create bus instance and `sessionIdProvider` and inject them into `Application`:
```java
public class App extends Application {
    public static final String App.INJECT_NAME_SESSION_ID = "INJECT_NAME_SESSION_ID";
    
    @Inject
    IBus bus;
    
    @Inject
    @Named(App.INJECT_NAME_SESSION_ID)
    Provider<String> sessionIdProvider;
    
    // onCreate
}
```

in module file (Dagger/Dagger 2 example):
```java
    @Provides
    @Singleton
    public IBus provideBus() {
        return new PostFromAnyThreadBus();
    }
    
    @Provides
    @Named(App.INJECT_NAME_SESSION_ID)
    public String providesSessionId(LoginManager loginManager) {
        return loginManager.getUserIdSha();
    }
```


##### Create callback where you need them #####
```java
MyEventCallback.<UserResponse>builder()
               .onSuccess().postResponseEvents(new MyEvent()).validBetweenSessions()
               .onError().postEvents(new LoginValidationFailedEvent()).validBetweenSessions()
               .build();
```


Available bus wrappers
----------------------

Depending on your application different Bus wrappers may be most suitable. If you use default ```Bus``` instance use ```OttoBus``` or ```PostFromAnyThreadBus```. If you initiated bus elsewhare or you have your custom ```Bus``` implementation ```..IBus``` classes may be better chocie. 

If you use events mainly to update UI state ```PostFromAnyThread...``` classes will be more convenient.

Class name            | requires passing bus instance | ensures that event is posted on android main thread
----------------------|-------------------------------|--------------------------------------
OttoBus               | No                            | No
OttoIBus              | Yes                           | No
PostFromAnyThreadBus  | No                            | Yes
PostFromAnyThreadIBus | Yes                           | Yes

By default Otto uses ThreadEnforcer.MAIN which will crash if you try to post event from different thread.
You can use `PostFromAnyThreadBus` and `PostFromAnyThreadIBus` without including rest of event callback by adding dependency:
```groovy
compile 'com.byoutline.ottoeventcallback:anythreadbus:1.0.0'
```


validThisSessionOnly vs validBetweenSessions
--------------------------------------------
`validThisSessionOnly` can prevent situation when event arrives when it is no longer needed/desired. For example if fetching some user data takes very long time and in the meantime he switches accounts, `EventCallback` can detect that and discard event. To do that you must setup `session id provider` in a way where it returns different values for different users. `validBetweenSessions` always delivers events and ignores `session id` value
