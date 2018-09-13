package com.kienht.retrofitdynamicbaseurl.cache;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Set;

public class LruCache<K, V> implements Cache<K, V> {
    private final LinkedHashMap<K, V> cache = new LinkedHashMap<>(100, 0.75f, true);
    private final int initialMaxSize;
    private int maxSize;
    private int currentSize = 0;

    public LruCache(int size) {
        this.initialMaxSize = size;
        this.maxSize = size;
    }

    public synchronized void setSizeMultiplier(float multiplier) {
        if (multiplier < 0) {
            throw new IllegalArgumentException("Multiplier must be >= 0");
        }
        maxSize = Math.round(initialMaxSize * multiplier);
        evict();
    }

    protected int getItemSize(V item) {
        return 1;
    }

    protected void onItemEvicted(K key, V value) {
        // optional override
    }

    @Override
    public synchronized int getMaxSize() {
        return maxSize;
    }

    @Override
    public synchronized int size() {
        return currentSize;
    }

    @Override
    public synchronized boolean containsKey(K key) {
        return cache.containsKey(key);
    }

    @Override
    public synchronized Set<K> keySet() {
        return cache.keySet();
    }

    @Override
    public synchronized V get(K key) {
        return cache.get(key);
    }

    @Override
    public synchronized V put(K key, V value) {
        final int itemSize = getItemSize(value);
        if (itemSize >= maxSize) {
            onItemEvicted(key, value);
            return null;
        }

        final V result = cache.put(key, value);
        if (value != null) {
            currentSize += getItemSize(value);
        }
        if (result != null) {
            currentSize -= getItemSize(result);
        }
        evict();

        return result;
    }

    @Override
    public synchronized V remove(K key) {
        final V value = cache.remove(key);
        if (value != null) {
            currentSize -= getItemSize(value);
        }
        return value;
    }

    @Override
    public void clear() {
        trimToSize(0);
    }

    protected synchronized void trimToSize(int size) {
        Map.Entry<K, V> last;
        while (currentSize > size) {
            last = cache.entrySet().iterator().next();
            final V toRemove = last.getValue();
            currentSize -= getItemSize(toRemove);
            final K key = last.getKey();
            cache.remove(key);
            onItemEvicted(key, toRemove);
        }
    }

    private void evict() {
        trimToSize(maxSize);
    }
}

