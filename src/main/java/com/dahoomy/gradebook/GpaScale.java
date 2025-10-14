package com.dahoomy.gradebook;

/**
 * Converts a final % grade into a letter and a 4.0-scale point value.
 * This is a generic scale; change it to match your university exactly.
 */
public final class GpaScale {
    private GpaScale() {} // utility class: no instances

    /** Map percentage to a letter grade. */
    public static String letterFor(double percent) {
        if (percent >= 90) return "A";
        if (percent >= 85) return "B+";
        if (percent >= 80) return "B";
        if (percent >= 75) return "C+";
        if (percent >= 70) return "C";
        if (percent >= 65) return "D+";
        if (percent >= 60) return "D";
        return "F";
    }

    /** Map percentage to 4.0 scale points. Adjust thresholds as needed. */
    public static double pointsFor(double percent) {
        if (percent >= 90) return 4.0;
        if (percent >= 85) return 3.5;
        if (percent >= 80) return 3.0;
        if (percent >= 75) return 2.5;
        if (percent >= 70) return 2.0;
        if (percent >= 65) return 1.5;
        if (percent >= 60) return 1.0;
        return 0.0;
    }
}
