package eu.fbk.utils.core;

/**
 * Created by alessio on 11/05/15.
 */

public class ArrayUtils {

    public static <T> String implode(String glue, T[] array) {
        // array is empty, return empty string
        if (array == null || array.length == 0) {
            return "";
        }

        // init the builder with the first element
        StringBuilder sb = new StringBuilder();
        sb.append(array[0]);

        // concat each element
        // begin at 1 to avoid duplicating first element
        for (int i = 1; i < array.length; i++) {
            sb.append(glue).append(array[i]);
        }

        // return the result
        return sb.toString();
    }
}
