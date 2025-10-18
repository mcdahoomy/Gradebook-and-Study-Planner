package com.dahoomy.gradebook;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Course holds a list of assignments and some metadata.
 * It does NOT know about other courses. Keep it focused and small.
 */
public final class Course {
    private final String code;         // e.g., "CMPS251"
    private final String name;         // e.g., "Programming Concepts"
    private int credits = 0;           // set by user; used for GPA
    private final List<Assignment> assignments = new ArrayList<>();

    public Course(String code, String name) {
        this.code = code;
        this.name = name;
    }

    // Simple accessors (methods instead of public fields to keep control)
    public String getCode() { return code; }
    public String getName() { return name; }
    public int getCredits() { return credits; }
    public void setCredits(int credits) { this.credits = credits; }

    /**
     * Read-only view so callers can’t modify the internal list directly.
     * They must use addAssignment(), which lets us validate later if needed.
     */
    public List<Assignment> assignments() {
        return Collections.unmodifiableList(assignments);
    }

    public int getAssignmentsCount() {
        return assignments.size();
    }

    public Assignment getAssignment(int index) {
        return assignments.get(index);
    }

    /** Add one assignment to the course. Validation lives in Assignment itself. */
    public void addAssignment(Assignment a) {
        assignments.add(a);
    }

    public Assignment removeAssignment(int index) {
        return assignments.remove(index);
    }

    /** Sum of all assignment weights (should be 100 for a finished course). */
    public double totalWeight() {
        double sum = 0.0;
        for (Assignment a : assignments) sum += a.getWeightPercent();
        return sum;
    }

    /**
     * Final grade as a percentage (0..100).
     * Each assignment contributes: (earned/max) * weight%.
     * Example: 18/20 with weight 10% contributes 9%.
     */
    public double weightedPercentage() {
        double total = 0.0;
        for (Assignment a : assignments) {
            double portion = (a.getEarned() / a.getMax()) * a.getWeightPercent();
            total += portion;
        }
        return total;
    }
}
