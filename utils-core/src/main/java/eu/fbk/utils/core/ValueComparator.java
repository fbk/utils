package eu.fbk.utils.core;

import java.util.Comparator;
import java.util.Map;

/**
 * Created by alessio on 02/04/15.
 */

public class ValueComparator implements Comparator {

    Map map;
    boolean desc = false;

    public ValueComparator(Map map) {
        this.map = map;
    }

    public ValueComparator(Map map, boolean desc) {
        this.map = map;
        this.desc = desc;
    }

    @Override
    public int compare(Object keyA, Object keyB) {
        Comparable valueA = (Comparable) map.get(keyA);
        Comparable valueB = (Comparable) map.get(keyB);
        if (desc) {
            return valueB.compareTo(valueA);
        }
        return valueA.compareTo(valueB);
    }
}
