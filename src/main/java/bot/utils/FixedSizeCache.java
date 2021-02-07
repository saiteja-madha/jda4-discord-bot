package bot.utils;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

public class FixedSizeCache<K, V> {

    private final Map<K, V> map;
    private final K[] keys;
    private int currIndex = 0;

    @SuppressWarnings("unchecked")
    public FixedSizeCache(int size) {
        this.map = new HashMap<>();
        if (size < 1)
            throw new IllegalArgumentException("Cache size must be at least 1!");
        this.keys = (K[]) new Object[size];
    }

    public void put(K key, V value) {
        if (map.containsKey(key)) {
            map.put(key, value);
            return;
        }

        if (keys[currIndex] != null) {
            map.remove(keys[currIndex]);
        }

        keys[currIndex] = key;
        currIndex = (currIndex + 1) % keys.length;
        map.put(key, value);
    }

    public void remove(K key) {
        map.remove(key);
    }

    public V get(K key) {
        return map.get(key);
    }

    public boolean contains(K key) {
        return map.containsKey(key);
    }

    public Collection<V> getValues() {
        return map.values();
    }

    public int size() {
        return map.size();
    }

}
