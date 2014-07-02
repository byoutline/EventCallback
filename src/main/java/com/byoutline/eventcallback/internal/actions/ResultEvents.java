package com.byoutline.eventcallback.internal.actions;

import com.byoutline.eventcallback.EventCallback;
import com.byoutline.eventcallback.events.ResponseEvent;
import java.util.ArrayList;
import java.util.List;
import javax.annotation.Nonnull;
import org.apache.commons.lang3.Validate;

/**
 * Stores events that can be invoked during {@link EventCallback} onSuccess
 * and onError steps.
 * 
 * @author Sebastian Kacprzak <nait at naitbit.com>
 */
public class ResultEvents<R> extends CreateEvents {
    public final List<ResponseEvent<R>> resultEvents;
    public final List<ResponseEvent<R>> resultStickyEvents;

    public ResultEvents() {
        this.resultEvents = new ArrayList<>();
        this.resultStickyEvents = new ArrayList<>();
    }

    public ResultEvents(@Nonnull List events, @Nonnull List stickyEvents, @Nonnull List<ResponseEvent<R>> resultEvents, @Nonnull List<ResponseEvent<R>> resultStickyEvents) {
        super(events, stickyEvents);
        this.resultEvents = resultEvents;
        this.resultStickyEvents = resultStickyEvents;
    }

    @Override
    void validate() {
        super.validate();
        Validate.noNullElements(resultEvents);
        Validate.noNullElements(resultStickyEvents);
    }
    
}
