package eu.fbk.utils.core;

import javax.annotation.Nullable;
import java.io.File;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.Objects;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicLong;

/**
 * Created by alessio on 03/08/16.
 */

public class Conversion {

    @SuppressWarnings("unchecked")
    @Nullable
    public static <T> T convert(@Nullable final Object object, final Class<T> clazz)
            throws IllegalArgumentException {
        if (object == null) {
            Objects.requireNonNull(clazz);
            return null;
        }
        if (clazz.isInstance(object)) {
            return (T) object;
        }
        final T result = (T) convertObject(object, clazz);
        if (result != null) {
            return result;
        }
        throw new IllegalArgumentException("Unsupported conversion of " + object + " to " + clazz);
    }

    @Nullable
    private static Object convertObject(final Object object, final Class<?> clazz) {
        if (object instanceof String) {
            return convertString((String) object, clazz);
        } else if (object instanceof Number) {
            return convertNumber((Number) object, clazz);
        } else if (object instanceof Boolean) {
            return convertBoolean((Boolean) object, clazz);
        } else if (object instanceof Enum<?>) {
            return convertEnum((Enum<?>) object, clazz);
        } else if (object instanceof File) {
            return convertFile((File) object, clazz);
        }
        return null;
    }

    @Nullable
    private static Object convertBoolean(final Boolean bool, final Class<?> clazz) {
        if (clazz == Boolean.class || clazz == boolean.class) {
            return bool;
        } else if (clazz.isAssignableFrom(String.class)) {
            return bool.toString();
        }
        return null;
    }

    @Nullable
    private static Object convertString(final String string, final Class<?> clazz) {
        if (clazz.isInstance(string)) {
            return string;
        } else if (clazz == Boolean.class || clazz == boolean.class) {
            return Boolean.valueOf(string);
        } else if (clazz == Integer.class || clazz == int.class) {
            return Integer.valueOf((int) toLong(string));
        } else if (clazz == Long.class || clazz == long.class) {
            return Long.valueOf(toLong(string));
        } else if (clazz == Double.class || clazz == double.class) {
            return Double.valueOf(string);
        } else if (clazz == Float.class || clazz == float.class) {
            return Float.valueOf(string);
        } else if (clazz == Short.class || clazz == short.class) {
            return Short.valueOf((short) toLong(string));
        } else if (clazz == Byte.class || clazz == byte.class) {
            return Byte.valueOf((byte) toLong(string));
        } else if (clazz == BigDecimal.class) {
            return new BigDecimal(string);
        } else if (clazz == BigInteger.class) {
            return new BigInteger(string);
        } else if (clazz == AtomicInteger.class) {
            return new AtomicInteger(Integer.parseInt(string));
        } else if (clazz == AtomicLong.class) {
            return new AtomicLong(Long.parseLong(string));
        } else if (clazz == Character.class || clazz == char.class) {
            return string.isEmpty() ? null : string.charAt(0);
        } else if (clazz.isEnum()) {
            for (final Object constant : clazz.getEnumConstants()) {
                if (string.equalsIgnoreCase(((Enum<?>) constant).name())) {
                    return constant;
                }
            }
            throw new IllegalArgumentException("Illegal " + clazz.getSimpleName() + " constant: "
                    + string);
        } else if (clazz == File.class) {
            return new File(string);
        }
        return null;
    }

    @Nullable
    private static Object convertNumber(final Number number, final Class<?> clazz) {
        if (clazz.isAssignableFrom(String.class)) {
            return number.toString();
        } else if (clazz == Integer.class || clazz == int.class) {
            return Integer.valueOf(number.intValue());
        } else if (clazz == Long.class || clazz == long.class) {
            return Long.valueOf(number.longValue());
        } else if (clazz == Double.class || clazz == double.class) {
            return Double.valueOf(number.doubleValue());
        } else if (clazz == Float.class || clazz == float.class) {
            return Float.valueOf(number.floatValue());
        } else if (clazz == Short.class || clazz == short.class) {
            return Short.valueOf(number.shortValue());
        } else if (clazz == Byte.class || clazz == byte.class) {
            return Byte.valueOf(number.byteValue());
        } else if (clazz == BigDecimal.class) {
            return toBigDecimal(number);
        } else if (clazz == BigInteger.class) {
            return toBigInteger(number);
        } else if (clazz == AtomicInteger.class) {
            return new AtomicInteger(number.intValue());
        } else if (clazz == AtomicLong.class) {
            return new AtomicLong(number.longValue());
        }
        return null;
    }

    @Nullable
    private static Object convertEnum(final Enum<?> constant, final Class<?> clazz) {
        if (clazz.isInstance(constant)) {
            return constant;
        } else if (clazz.isAssignableFrom(String.class)) {
            return constant.name();
        }
        return null;
    }

    @Nullable
    private static Object convertFile(final File file, final Class<?> clazz) {
        if (clazz.isInstance(file)) {
            return clazz.cast(file);
        } else if (clazz.isAssignableFrom(String.class)) {
            return file.getAbsolutePath();
        }
        return null;
    }

    private static BigDecimal toBigDecimal(final Number number) {
        if (number instanceof BigDecimal) {
            return (BigDecimal) number;
        } else if (number instanceof BigInteger) {
            return new BigDecimal((BigInteger) number);
        } else if (number instanceof Double || number instanceof Float) {
            final double value = number.doubleValue();
            return Double.isInfinite(value) || Double.isNaN(value) ? null : new BigDecimal(value);
        } else {
            return new BigDecimal(number.longValue());
        }
    }

    private static BigInteger toBigInteger(final Number number) {
        if (number instanceof BigInteger) {
            return (BigInteger) number;
        } else if (number instanceof BigDecimal) {
            return ((BigDecimal) number).toBigInteger();
        } else if (number instanceof Double || number instanceof Float) {
            return new BigDecimal(number.doubleValue()).toBigInteger();
        } else {
            return BigInteger.valueOf(number.longValue());
        }
    }

    private static long toLong(final String string) {
        long multiplier = 1;
        final char c = string.charAt(string.length() - 1);
        if (c == 'k' || c == 'K') {
            multiplier = 1024;
        } else if (c == 'm' || c == 'M') {
            multiplier = 1024 * 1024;
        } else if (c == 'g' || c == 'G') {
            multiplier = 1024 * 1024 * 1024;
        }
        return Long.parseLong(multiplier == 1 ? string : string.substring(0, string.length() - 1))
                * multiplier;
    }

}
