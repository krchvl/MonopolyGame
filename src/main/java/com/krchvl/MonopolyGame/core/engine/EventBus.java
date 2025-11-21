package com.krchvl.MonopolyGame.core.engine;

import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.function.Consumer;

public class EventBus {
    private final Map<Class<?>, List<Consumer<?>>> listeners = new ConcurrentHashMap<>();

    public <T> void on(Class<T> type, Consumer<T> handler) {
        listeners.computeIfAbsent(type, k -> new CopyOnWriteArrayList<>()).add(handler);
    }

    public void publish(Object event) {
        if (event == null) return;
        Class<?> type = event.getClass();
        List<Consumer<?>> handlers = listeners.get(type);
        if (handlers != null) {
            for (Consumer<?> h : handlers) {
                ((Consumer<Object>) h).accept(event);
            }
        }
    }
}