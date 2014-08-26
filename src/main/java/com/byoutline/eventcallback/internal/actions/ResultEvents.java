package com.byoutline.eventcallback.internal.actions;

import com.byoutline.eventcallback.EventCallback;
import com.byoutline.eventcallback.ResponseEvent;
import java.util.ArrayList;
import java.util.List;
import javax.annotation.Nonnull;
import org.apache.commons.lang3.Validate;

/**
 * Stores events that can be invoked during {@link EventCallback} onSuccess
 * and onError steps.
 * 
 * @author Sebastian Kacprzak <sebastian.kacprzak at byoutline.com>
 */
public class ResultEvents<R> extends CreateEvents {
    public final List<ResponseEvent<R>> resultEvents;

    public ResultEvents() {
        this.resultEvents = new ArrayList<>();
    }

    public ResultEvents(@Nonnull List events, @Nonnull List<ResponseEvent<R>> resultEvents) {
        super(events);
        this.resultEvents = resultEvents;
    }

    @Override
    void validate() {
        super.validate();
        Validate.noNullElements(resultEvents);
    }

    @Override
    public String toString() {
        return "ResultEvents{" + "resultEvents=" + resultEvents + '}' + super.toString();
    }
}
