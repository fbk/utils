package eu.fbk.utils.core;

import java.io.Serializable;
import java.util.*;

/**
 * Created with IntelliJ IDEA.
 * User: aprosio
 * Date: 1/15/13
 * Time: 5:56 PM
 * To change this template use File | Settings | File Templates.
 */
public class FrequencyHashSet<K> implements Serializable {

    private HashMap<K, Integer> support = new HashMap<K, Integer>();

    static <K, V extends Comparable<? super V>>
    SortedSet<Map.Entry<K, V>> entriesSortedByValues(Map<K, V> map) {
        SortedSet<Map.Entry<K, V>> sortedEntries = new TreeSet<Map.Entry<K, V>>(
                new Comparator<Map.Entry<K, V>>() {

                    @Override
                    public int compare(Map.Entry<K, V> e1, Map.Entry<K, V> e2) {
                        int res = e2.getValue().compareTo(e1.getValue());
                        return res != 0 ? res : 1;
                    }
                }
        );
        sortedEntries.addAll(map.entrySet());
        return sortedEntries;
    }

    public SortedSet<Map.Entry<K, Integer>> getSorted() {
        return entriesSortedByValues(support);
    }

    public LinkedHashMap<K, Integer> getLinked() {
        SortedSet<Map.Entry<K, Integer>> sorted = entriesSortedByValues(support);
        LinkedHashMap<K, Integer> temp = new LinkedHashMap<K, Integer>();
        for (Object o : sorted) {
            Map.Entry entry = (Map.Entry) o;
            temp.put((K) entry.getKey(), (Integer) entry.getValue());
        }
        return temp;
    }

    public void add(K o, int quantity) {
        int count = support.containsKey(o) ? ((Integer) support.get(o)).intValue() + quantity : quantity;
        support.put(o, count);
    }

    public FrequencyHashSet<K> clone() {
        FrequencyHashSet<K> ret = new FrequencyHashSet<K>();
        for (K k : support.keySet()) {
            ret.add(k, support.get(k));
        }
        return ret;
    }

    public void addAll(Collection<K> collection) {
        for (K el : collection) {
            add(el);
        }
    }

    public void addAll(FrequencyHashSet<K> frequencyHashSet) {
        for (K el : frequencyHashSet.keySet()) {
            add(el, frequencyHashSet.get(el));
        }
    }

    public void remove(K o) {
        support.remove(o);
    }

    public void add(K o) {
        add(o, 1);
    }

    public int size() {
        return support.size();
    }

    public K mostFrequent() {
        Iterator it = support.keySet().iterator();
        Integer max = null;
        K o = null;
        while (it.hasNext()) {
            K index = (K) it.next();
            if (max == null || support.get(index) > max) {
                o = index;
                max = support.get(index);
            }
        }
        return o;
    }

    public Set<K> keySet() {
        return support.keySet();
    }

    public Integer get(K o) {
        return support.get(o);
    }

    public Integer getZero(K o) {
        return support.get(o) != null ? support.get(o) : 0;
    }

    public Integer sum() {
        int total = 0;
        for (K key : support.keySet()) {
            total += support.get(key);
        }
        return total;
    }

    public Set<K> keySetWithLimit(int limit) {
        HashSet<K> ret = new HashSet<K>();

        Iterator it = support.keySet().iterator();
        while (it.hasNext()) {
            K key = (K) it.next();
            // int value = ((Integer) support.get(key)).intValue();
            int value = support.get(key);
            if (value >= limit) {
                ret.add(key);
                // ret += key.toString() + "=" + value + "\n";
            }
        }

        // return ret.trim();
        return ret;
    }

    public String toString() {
        return support.toString();
    }
}