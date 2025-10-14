package com.dahoomy.gradebook;

public final class GpaScale {
    private GpaScale() {}

    // Simple default scale (adjustable)
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

    public static double pointsFor(double percent) {
        // Map letters to 4.0 scale (tweak to match your university exactly)
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
