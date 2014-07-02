EventCallback
=============
EventCallback allows creating instances of [Retrofit](http://square.github.io/retrofit/) [callbacks](http://square.github.io/retrofit/javadoc/retrofit/Callback.html) using short readable syntax.

Instead of creating anonymous classes manually (where you have to take care of not using parent class fields that can change by the time server response arrives)
```code java
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
        bus.postSticky(new LoginValidationFailedEvent(restErrorWithMsg));
    }
};
```
you can use EventCallback like this:
```code java
EventCallback.<SuccessDTO>builder(config, new TypeToken<RestErrorWithMsg>(){})
    .onSuccess().postEvents(new MyEvent(), new SuccessEvent()).validThisSessionOnly().notSticky()
    .onError().postResponseEvents(new LoginValidationFailedEvent()).validBetweenSessions().asSticky()
    .build();
``` 

