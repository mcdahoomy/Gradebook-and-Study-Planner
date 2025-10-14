package com.dahoomy.gradebook;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public final class Course {
    private final String code;
    private final String name;
    private int credits = 0;
    private final List<Assignment> assignments = new ArrayList<>();

    public Course(String code, String name) {
        this.code = code;
        this.name = name;
    }

    public String code() { return code; }
    public String name() { return name; }
    public int credits() { return credits; }
    public void setCredits(int credits) { this.credits = credits; }

    public List<Assignment> assignments() {
        return Collections.unmodifiableList(assignments);
    }

    public void addAssignment(Assignment a) {
        assignments.add(a);
    }

    public double totalWeight() {
        double sum = 0.0;
        for (Assignment a : assignments) sum += a.weightPercent();
        return sum;
    }

    public double weightedPercentage() {
        double total = 0.0;
        for (Assignment a : assignments) {
            double portion = (a.earned() / a.max()) * a.weightPercent();
            total += portion;
        }
        return total;
    }
}
