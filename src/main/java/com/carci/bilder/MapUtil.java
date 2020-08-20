package com.carci.bilder;

import java.util.*;

/**
 * Created by carcinoma on 12.11.17.
 */
public class MapUtil {

    public static <K extends Comparable<? super K>, V> Map<K, V> sortByKey(Map<K, V> map) {

        List<Map.Entry<K, V>> list = new LinkedList<Map.Entry<K, V>>(map.entrySet());
        Collections.sort(list, new Comparator<Map.Entry<K, V>>() {
            public int compare(Map.Entry<K, V> o1, Map.Entry<K, V> o2) {
                if(o1 == null || o1.getKey() == null || o2 == null || o2.getKey() == null) {
                    return 0;
                }
                return (o1.getKey()).compareTo(o2.getKey());
            }
        });

        Map<K, V> result = new LinkedHashMap<K, V>();
        for (Map.Entry<K, V> entry : list) {
            result.put(entry.getKey(), entry.getValue());
        }
        return result;

    }

    public static <K extends Comparable<? super K>, V> Map<K, V> sortByKeyRev(Map<K, V> map) {

        List<Map.Entry<K, V>> list = new LinkedList<Map.Entry<K, V>>(map.entrySet());
        Collections.sort(list, new Comparator<Map.Entry<K, V>>() {
            public int compare(Map.Entry<K, V> o1, Map.Entry<K, V> o2) {
                if(o1 == null || o1.getKey() == null || o2 == null || o2.getKey() == null) {
                    return 0;
                }
                return (o2.getKey()).compareTo(o1.getKey());
            }
        });

        Map<K, V> result = new LinkedHashMap<K, V>();
        for (Map.Entry<K, V> entry : list) {
            result.put(entry.getKey(), entry.getValue());
        }
        return result;

    }


}
