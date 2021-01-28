package org.github.iamxwaa.metric;

import java.util.concurrent.atomic.AtomicInteger;

public class Counter {
    private AtomicInteger inCounter = new AtomicInteger(0);

    private AtomicInteger outCounter = new AtomicInteger(0);

    public AtomicInteger getInCounter() {
        return inCounter;
    }

    public void setInCounter(AtomicInteger inCounter) {
        this.inCounter = inCounter;
    }

    public AtomicInteger getOutCounter() {
        return outCounter;
    }

    public void setOutCounter(AtomicInteger outCounter) {
        this.outCounter = outCounter;
    }
}
