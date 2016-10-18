package com.byoutline.ottoeventcallback;

import com.byoutline.eventcallback.IBus;
import com.squareup.otto.Bus;

/**
 * Simple wrapper of {@link Bus} to implement {@link IBus} interface.
 *
 * @author Sebastian Kacprzak <sebastian.kacprzak at byoutline.com>
 */
public class OttoBus extends Bus implements IBus {
}
