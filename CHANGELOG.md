ottoeventcallback 1.3.2, anythreadbus 1.0.0 *(2016-03-16)*
----------------------------------------------------------
 * `PostFromAnyThreadBus` and `PostFromAnyThreadIBus` moved to `anythreadbus`, so they can be used without
   `EventCallback` dependency. No new functionality.
 
ottoeventcallback 1.3.1, ottoeventcallback-api 1.0.1 *(2015-12-03)*
-------------------------------------------------------------------
 * Refactor, No changes
   
ottoeventcallback 1.3.0, ottoeventcallback-api 1.0.0 *(2015-12-15)*
-------------------------------------------------------------------
 * `OttoBus` and `OttoIBus` moved to `ottoeventcallback-api` module, that does not depend on `Android`, or `EventCallback`
   does not have hard dependency on `Otto`. No new functionality.

ottoeventcallback 1.2.4 *(2015-12-14)*
--------------------------------------
* Refactor, No changes

ottoeventcallback 1.2.3 *(never released)*
------------------------------------------

ottoeventcallback 1.2.2 *(2014-12-01)*
--------------------------------------
 * `PostFromAnyThreadIBus` added - `IBus` implementation that uses provided `Otto` bus instance to post
   on `Android` main thread.
 
ottoeventcallback 1.2.1 *(2014-09-01)*
--------------------------------------
 * First public release (`OttoBus`, `OttoIBus`, `PostFromAnyThreadBus`)
 
Event Callback
--------------
 * 1.3.2 Split up interfaces to eventcallback-api, so projects or other libs that want to use them do not have to depend
   on full EventCallback and its depenedencies (Retrofit)
 * 1.3.1 StubSessionIdProvider - empty implementation of session id provider, for projects that do not have session.
 * 1.3.0 Status code and response headers will be set for events that implement RetrofitResponseEvent interface.