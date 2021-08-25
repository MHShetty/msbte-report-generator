package org.tpoly.msbte;

// The Marks class is used to store the constriants (maximum and minimum) and obtained marks for a course head/sub-section (TH/PR) of a course
// Note: A sub-section of a course will not have it's minimum marks set by default (as it is not specified by default 0)
public class Marks
{
    private String max;
    private String min;
    private String value;

    Marks() {}

    Marks(String max, String min, String value) {
        this.max = max;
        this.min = min;
        this.value = value;
    }

    Marks(String max, String value) {
        this.max = max;
        this.value = value;
    }

    String getMaximum() {
        return max;
    }

    String getMinimum() {
        return min;
    }

    String getValue() {
        return value;
    }

    int getMaximumAsInt() {
        return Integer.parseInt(max);
    }

    int getMinimumAsInt() {
        return Integer.parseInt(min);
    }

    int getValueAsInt() {
        return Integer.parseInt(value);
    }

    public String toString() {
        StringBuilder s = new StringBuilder("  Maximum - " + max + "\n");
        if (min != null) s.append("  Minimum - " + min + "\n");
        s.append("  Obtained - " + value + "\n");
        return s.toString();
    }
}