package com.byoutline.eventcallback.internal;

import com.google.gson.reflect.TypeToken;
import java.util.logging.Level;
import java.util.logging.Logger;
import retrofit.RetrofitError;

/**
 * Helper class with static methods that try to convert response from
 * {@link RetrofitError} to requested type.
 *
 * @author Sebastian Kacprzak <sebastian.kacprzak at byoutline.com> on 26.06.14.
 */
public final class RetrofitErrorConverter {
    private final static Logger LOGGER = Logger.getLogger(RetrofitErrorConverter.class.getName());

    private RetrofitErrorConverter() {
    }
    
    public static <E> E getAsClassOrFail(TypeToken<E> typeToken, RetrofitError error) throws RuntimeException {
        return (E) error.getBodyAs(typeToken.getType());
    }
    
    public static <E> E getAsClassOrNull(TypeToken<E> typeToken, RetrofitError error) throws RuntimeException {
        if (error.isNetworkError()) {
            return null;
        }
        try {
            return getAsClassOrFail(typeToken, error);
        } catch (RuntimeException ex) {
            LOGGER.log(Level.FINE, "Could not parse: " + error, ex);
        }
        return null;
    }
}
