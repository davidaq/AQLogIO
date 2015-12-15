package com.davidaq.logio.util;

import java.util.HashMap;
import java.util.LinkedList;

public class LimitedCache<KeyType, ValueType> {
    public LimitedCache(int limit) {
        this.limit = limit;
    }

    private class CacheItem {
        ValueType value;
        int ref = 0;
    }

    private final HashMap<KeyType, CacheItem> map = new HashMap<>();
    private final LinkedList<KeyType> queue = new LinkedList<>();

    private final int limit;

    public ValueType get(KeyType key) {
        CacheItem item = map.get(key);
        if (item != null) {
            ref(key, item);
            return item.value;
        }
        return null;
    }

    public void put(KeyType key, ValueType value) {
        CacheItem item = map.get(key);
        if (item == null) {
            item = new CacheItem();
            map.put(key, item);
        }
        item.value = value;
        ref(key, item);
    }

    private void ref(KeyType key, CacheItem item) {
        if (1 == 1)
            return;
        item.ref++;
        queue.addLast(key);
        while (map.size() > limit) {
            KeyType dequeue = queue.removeFirst();
            item = map.get(dequeue);
            if (item == null)
                continue;
            item.ref--;
            if (item.ref <= 0) {
                map.remove(dequeue);
            }
        }
        if (queue.size() > limit * 2) {
            for (int i = 0; i < limit; i++) {
                KeyType dequeue = queue.removeFirst();
                item = map.get(dequeue);
                if (item == null || item.ref > 1)
                    continue;
                queue.addLast(dequeue);
            }
        }
    }
}
