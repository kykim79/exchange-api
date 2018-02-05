package com.cf.data.handler.poloniex;

import org.slf4j.LoggerFactory;
import rx.functions.Action1;

/**
 * @author David
 */
public class PoloniexSubscriptionExceptionHandler implements Action1<Throwable> {
    protected final static org.slf4j.Logger LOG = LoggerFactory.getLogger(PoloniexSubscriptionExceptionHandler.class);
    private final String name;

    public PoloniexSubscriptionExceptionHandler(String name) {
        this.name = name;
    }

    @Override
    public void call(Throwable t) {
        LOG.warn("{} handler encountered exception - {}", name, t.getMessage());

    }

}
