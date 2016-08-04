package eu.fbk.utils.core;

import com.google.common.collect.Lists;
import com.google.common.collect.Ordering;

import javax.annotation.Nullable;
import java.util.Collections;
import java.util.List;

/**
 * A non-empty, closed non-negative integer range.
 */
public final class Range implements Comparable<Range> {

    private final int begin;

    private final int end;

    @Nullable
    public static Range valueOf(final String string) {
        final String trimmedSpan = string.trim();
        final int delimiter = trimmedSpan.indexOf(',');
        final int begin = Integer.parseInt(trimmedSpan.substring(0, delimiter));
        final int end = Integer.parseInt(trimmedSpan.substring(delimiter + 1));
        return create(begin, end);
    }

    @Nullable
    public static Range create(final int singletonValue) {
        return singletonValue < 0 ? null : new Range(singletonValue, singletonValue + 1);
    }

    @Nullable
    public static Range create(final int begin, final int end) {
        if (begin < 0 || begin >= end) {
            return null;
        }
        return new Range(begin, end);
    }

    @Nullable
    public static Range intersection(final Iterable<Range> ranges) {
        int begin = Integer.MIN_VALUE;
        int end = Integer.MAX_VALUE;
        for (final Range range : ranges) {
            begin = Math.max(begin, range.begin);
            end = Math.min(end, range.end);
            if (begin >= end) {
                return null;
            }
        }
        return begin > Integer.MIN_VALUE ? new Range(begin, end) : null;
    }

    @Nullable
    public static Range enclose(final int... values) {
        int begin = Integer.MAX_VALUE;
        int end = Integer.MIN_VALUE;
        for (final int value : values) {
            if (value < 0) {
                return null;
            }
            begin = Math.min(begin, value);
            end = Math.max(end, value + 1);
        }
        return begin >= end ? null : new Range(begin, end);
    }

    @Nullable
    public static Range enclose(final Iterable<Range> ranges) {
        int begin = Integer.MAX_VALUE;
        int end = Integer.MIN_VALUE;
        for (final Range range : ranges) {
            begin = Math.min(range.begin, begin);
            end = Math.max(range.end, end);
        }
        return begin >= end ? null : new Range(begin, end);
    }

    public static List<Range> merge(final Iterable<Range> ranges) {
        final List<Range> sortedRanges = Ordering.natural().sortedCopy(ranges);
        int i = 0;
        while (i < sortedRanges.size() - 1) {
            final Range range1 = sortedRanges.get(i);
            final Range range2 = sortedRanges.get(i + 1);
            if (range1.end >= range2.begin) {
                final Range merged = new Range(Math.min(range1.begin, range2.begin), Math.max(
                        range1.end, range2.end));
                sortedRanges.set(i, merged);
                sortedRanges.remove(i + 1);
            } else {
                ++i;
            }
        }
        return sortedRanges;
    }

    public static List<Range> separate(final Iterable<Range> ranges) {
        final List<Range> sortedRanges = Lists.newArrayList(ranges);
        boolean overlaps = true;
        while (overlaps) {
            overlaps = false;
            Collections.sort(sortedRanges);
            for (int i = 0; i < sortedRanges.size() - 1; ++i) {
                final Range range1 = sortedRanges.get(i);
                final Range range2 = sortedRanges.get(i + 1);
                if (range1.end > range2.begin) {
                    sortedRanges.remove(i);
                    if (range1.begin < range2.begin) {
                        sortedRanges.add(new Range(range1.begin, range2.begin));
                    }
                    if (range1.end < range2.end) {
                        sortedRanges.remove(i); // former i + 1
                        sortedRanges.add(new Range(range2.begin, range1.end));
                        sortedRanges.add(new Range(range1.end, range2.end));
                    } else if (range1.end > range2.end) {
                        sortedRanges.add(new Range(range2.end, range1.end));
                    }
                    overlaps = true;
                    break;
                }
            }
        }
        return sortedRanges;
    }

    private Range(final int begin, final int end) {
        this.begin = begin;
        this.end = end;
    }

    public int begin() {
        return this.begin;
    }

    public int end() {
        return this.end;
    }

    public int length() {
        return this.end - this.begin;
    }

    public boolean contains(final int value) {
        return value >= this.begin && value < this.end;
    }

    public boolean contains(final Range range) {
        return this.begin <= range.begin && this.end >= range.end;
    }

    public boolean containedIn(final Iterable<Range> ranges) {
        for (final Range range : ranges) {
            if (range.contains(this)) {
                return true;
            }
        }
        return false;
    }

    public boolean overlaps(final Range range) {
        return this.end > range.begin && this.begin < range.end;
    }

    public boolean overlaps(final Iterable<Range> ranges) {
        for (final Range range : ranges) {
            if (overlaps(range)) {
                return true;
            }
        }
        return false;
    }

    public boolean connectedWith(final Range range) {
        return this.end >= range.begin && this.begin <= range.end;
    }

    public boolean connectedWith(final Iterable<Range> ranges) {
        for (final Range range : ranges) {
            if (connectedWith(range)) {
                return true;
            }
        }
        return false;
    }

    public int distance(final Range range) {
        if (range.begin > this.end) {
            return range.begin - this.end;
        } else if (range.end < this.begin) {
            return this.begin - range.end;
        } else {
            return 0;
        }
    }

    public List<Range> split(final Iterable<Range> ranges) {
        final List<Range> sortedRanges = separate(ranges);
        final List<Range> result = Lists.newArrayList();
        int index = this.begin;
        for (final Range range : sortedRanges) {
            if (range.begin >= this.end) {
                break;
            }
            if (range.begin > index) {
                result.add(new Range(index, range.begin));
                index = range.begin;
            }
            if (range.end > index) {
                final int end = Math.min(range.end, this.end);
                result.add(index == range.begin && end == range.end ? range
                        : new Range(index, end));
                index = end;
            }
        }
        if (index < this.end) {
            result.add(new Range(index, this.end));
        }
        return result;
    }

    @Override
    public int compareTo(final Range range) {
        int result = this.begin - range.begin;
        if (result == 0) {
            result = range.end - this.end;
        }
        return result;
    }

    @Override
    public boolean equals(final Object object) {
        if (object == this) {
            return true;
        }
        if (!(object instanceof Range)) {
            return false;
        }
        final Range other = (Range) object;
        return this.begin == other.begin && this.end == other.end;
    }

    @Override
    public int hashCode() {
        return this.begin * 37 + this.end;
    }

    @Override
    public String toString() {
        return this.begin + "," + this.end;
    }

}