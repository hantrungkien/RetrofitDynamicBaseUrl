package com.kienht.retrofitdynamicbaseurl.cache;

import java.util.Set;

public interface Cache<K, V> {

    int size();

    int getMaxSize();

    V get(K key);

    V put(K key, V value);

    V remove(K key);

    boolean containsKey(K key);

    Set<K> keySet();

    void clear();
}
