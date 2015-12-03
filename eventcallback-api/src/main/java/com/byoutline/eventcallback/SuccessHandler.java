package com.byoutline.eventcallback;

/**
 * Receives all success responses that match type.
 *
 * @param <S> Type of response that should be passed to this handler.
 * @author Sebastian Kacprzak <sebastian.kacprzak at byoutline.com> on 26.06.14.
 */
public interface SuccessHandler<S> {
    void onCallSuccess(S response);
}
