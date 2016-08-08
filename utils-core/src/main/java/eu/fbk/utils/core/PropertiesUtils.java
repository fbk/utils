package eu.fbk.utils.core;

import javax.annotation.Nullable;

/**
 * Created by alessio on 05/08/16.
 */

public class PropertiesUtils {

    private static String[] booleanTrue = new String[] { "yes", "1", "y", "true" };
    private static String[] booleanFalse = new String[] { "no", "0", "n", "false" };

    public static Integer getInteger(@Nullable String value, int defaultValue) {
        try {
            return Integer.parseInt(value);
        } catch (Exception e) {
            return defaultValue;
        }
    }

    public static Boolean getBoolean(@Nullable String value, boolean defaultValue) {
        if (value != null) {
            for (String s : booleanTrue) {
                if (value.equalsIgnoreCase(s)) {
                    return true;
                }
            }
            for (String s : booleanFalse) {
                if (value.equalsIgnoreCase(s)) {
                    return false;
                }
            }
        }
        try {
            return Boolean.parseBoolean(value);
        } catch (Exception e) {
            return defaultValue;
        }
    }

    public static Double getDouble(@Nullable String value, double defaultValue) {
        try {
            return Double.parseDouble(value);
        } catch (Exception e) {
            return defaultValue;
        }
    }
}
