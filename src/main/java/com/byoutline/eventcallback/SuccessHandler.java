package com.byoutline.eventcallback;

/**
 * Receives all success responses that match type.
 *
 * @param <S> Type of response that should be passed to this handler.
 * @author Sebastian Kacprzak <nait at naitbit.com> on 26.06.14.
 */
public interface SuccessHandler<S> {
    void onCallSuccess(S response);
}
