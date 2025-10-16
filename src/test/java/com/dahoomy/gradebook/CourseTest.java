package com.dahoomy.gradebook;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

/**Tests for Course math.*/
public class CourseTest {

    @Test
    void weightedPercentage_addsUpCorrectly() {
        // Set up a simple course
        Course c = new Course("CMPS251", "Programming Concepts");

        // 90% on a 10% quiz -> contributes 9% overall
        c.addAssignment(new Assignment("Quiz1", 18, 20, 10));

        // 80% on a 40% midterm -> contributes 32%
        c.addAssignment(new Assignment("Midterm", 80, 100, 40));

        // 75% on a 50% final -> contributes 37.5%
        c.addAssignment(new Assignment("Final", 75, 100, 50));

        // Total should be 9 + 32 + 37.5 = 78.5%
        assertEquals(78.5, c.weightedPercentage(), 1e-9);
        assertEquals(100.0, c.totalWeight(), 1e-9);
    }

    @Test
    void totalWeight_detectsOver100() {
        Course c = new Course("TEST", "Test Course");
        c.addAssignment(new Assignment("A", 10, 10, 60));
        c.addAssignment(new Assignment("B", 8, 10, 40));
        // Now exactly 100
        assertEquals(100.0, c.totalWeight(), 1e-9);

        // Adding more would go beyond 100 (we don't add here; Main prevents it).
        // This test just documents the behavior of totalWeight().
        c.addAssignment(new Assignment("Extra", 5, 10, 5));
        assertTrue(c.totalWeight() > 100.0);
    }
}
