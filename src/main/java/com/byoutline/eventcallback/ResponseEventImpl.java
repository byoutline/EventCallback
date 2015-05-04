package com.byoutline.eventcallback;

/**
 * Default implementation of {@link ResponseEvent}.
 *
 * @author Sebastian Kacprzak <sebastian.kacprzak at byoutline.com>
 */
public class ResponseEventImpl<R> implements ResponseEvent<R> {
    private R response;

    /**
     * @return body of response returned from server
     */
    public R getResponse() {
        return response;
    }

    @Override
    public void setResponse(R response) {
        this.response = response;
    }
}
