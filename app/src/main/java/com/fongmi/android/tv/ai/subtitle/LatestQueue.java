package com.fongmi.android.tv.ai.subtitle;

import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicLong;

final class LatestQueue<T> {
    private final ArrayBlockingQueue<T> queue;
    private final AtomicLong dropped = new AtomicLong();

    LatestQueue(int capacity) {
        queue = new ArrayBlockingQueue<>(capacity);
    }

    boolean offerLatest(T value) {
        if (queue.offer(value)) return true;
        queue.poll();
        dropped.incrementAndGet();
        return queue.offer(value);
    }

    T poll(long timeout, TimeUnit unit) throws InterruptedException {
        return queue.poll(timeout, unit);
    }

    T poll() {
        return queue.poll();
    }

    T drainLatest(T first) {
        T latest = first;
        T next;
        while ((next = queue.poll()) != null) latest = next;
        return latest;
    }

    void clear() {
        queue.clear();
    }

    int size() {
        return queue.size();
    }

    long dropped() {
        return dropped.get();
    }
}
