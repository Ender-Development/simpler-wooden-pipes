package xyz.dogboy.swp.utils;

import java.util.ArrayList;
import java.util.Set;
import java.util.TreeMap;

public class PipePriorityMap<K extends Comparable<Integer>, V> {
    TreeMap<K, ArrayList<V>> map = new TreeMap<>();

    public void put(K key, V value){
        ArrayList<V> list = map.computeIfAbsent(key, k -> new ArrayList<>());
        list.add(value);
    }

    public ArrayList<V> get(K key) {
        return map.get(key);
    }

    public Set<K> keySet() {
        return map.keySet();
    }
}
