package io.github.mundanej.mlog.slf4j;

import java.util.ArrayDeque;
import java.util.Collections;
import java.util.Deque;
import java.util.LinkedHashMap;
import java.util.Map;
import org.slf4j.spi.MDCAdapter;

final class MundaneMdcAdapter implements MDCAdapter {
  private final ThreadLocal<LinkedHashMap<String, String>> context =
      ThreadLocal.withInitial(LinkedHashMap::new);
  private final ThreadLocal<LinkedHashMap<String, Deque<String>>> deques =
      ThreadLocal.withInitial(LinkedHashMap::new);

  @Override
  public void put(String key, String value) {
    if (key == null) {
      throw new IllegalArgumentException("MDC key must not be null");
    }
    context.get().put(key, value);
  }

  @Override
  public String get(String key) {
    return context.get().get(key);
  }

  @Override
  public void remove(String key) {
    LinkedHashMap<String, String> map = context.get();
    map.remove(key);
    if (map.isEmpty()) {
      context.remove();
    }
  }

  @Override
  public void clear() {
    context.remove();
  }

  @Override
  public Map<String, String> getCopyOfContextMap() {
    LinkedHashMap<String, String> map = context.get();
    if (map.isEmpty()) {
      return null;
    }
    return Collections.unmodifiableMap(new LinkedHashMap<>(map));
  }

  @Override
  public void setContextMap(Map<String, String> contextMap) {
    LinkedHashMap<String, String> replacement = new LinkedHashMap<>();
    if (contextMap != null) {
      replacement.putAll(contextMap);
    }
    context.set(replacement);
  }

  @Override
  public void pushByKey(String key, String value) {
    if (key == null) {
      throw new IllegalArgumentException("MDC key must not be null");
    }
    deques.get().computeIfAbsent(key, ignored -> new ArrayDeque<>()).push(value);
  }

  @Override
  public String popByKey(String key) {
    Deque<String> deque = deques.get().get(key);
    if (deque == null || deque.isEmpty()) {
      return null;
    }
    String value = deque.pop();
    if (deque.isEmpty()) {
      deques.get().remove(key);
    }
    return value;
  }

  @Override
  public Deque<String> getCopyOfDequeByKey(String key) {
    Deque<String> deque = deques.get().get(key);
    return deque == null ? null : new ArrayDeque<>(deque);
  }

  @Override
  public void clearDequeByKey(String key) {
    LinkedHashMap<String, Deque<String>> map = deques.get();
    map.remove(key);
    if (map.isEmpty()) {
      deques.remove();
    }
  }
}
