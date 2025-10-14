package com.dahoomy.gradebook;

import java.util.*;

public class Main {
    private final Map<String, Course> courses = new HashMap<>();

    public static void main(String[] args) {
        new Main().run();
    }

    private void run() {
        System.out.println("Gradebook CLI — type 'help' to begin.");
        try (Scanner scanner = new Scanner(System.in)) {
            while (true) {
                System.out.print("> ");
                if (!scanner.hasNextLine()) break;
                String line = scanner.nextLine().trim();
                if (line.isEmpty()) continue;

                String[] parts = line.split("\\s+", 2);
                String cmd = parts[0].toLowerCase(Locale.ROOT);
                String rest;
                if (parts.length > 1) {
                    rest = parts[1];
                } else {
                    rest = "";
                }

                switch (cmd) {
                    case "help" -> printHelp();
                    case "add-course" -> cmdAddCourse(rest);
                    case "set-credits" -> cmdSetCredits(rest);
                    case "list-courses" -> listCourses();
                    case "add-assign" -> cmdAddAssign(rest);
                    case "list-assign" -> cmdListAssign(rest);
                    case "grade" -> cmdGrade(rest);
                    case "gpa" -> cmdGpa();
                    case "exit", "quit" -> { System.out.println("Bye!"); return; }
                    default -> System.out.println("Unknown command. Type 'help'.");
                }
            }
        }
    }

    private void printHelp() {
        System.out.println("""
                Commands:
                  add-course <code> <name>              — add a course (e.g., CMPS251 Programming Concepts)
                  set-credits <code> <credits>          — set course credits (e.g., 3)
                  list-courses                          — list all courses
                  add-assign <code> <name> <e>/<m> <w>  — add assignment: earned/max weight%
                                                        e.g., add-assign CMPS251 Quiz1 18/20 10
                  list-assign <code>                    — list assignments of a course
                  grade <code>                          — show weighted grade for course
                  gpa                                   — compute GPA across all courses with credits
                  exit                                  — quit
                """);
    }

    private void cmdAddCourse(String rest) {
        String[] parts = rest.split("\\s+", 2);
        if (parts.length < 2) {
            System.out.println("Usage: add-course <code> <name>");
            return;
        }
        String code = parts[0];
        String name = parts[1];
        if (courses.containsKey(code)) {
            System.out.println("Course already exists.");
            return;
        }
        courses.put(code, new Course(code, name));
        System.out.println("Added: " + code + " — " + name);
    }

    private void cmdSetCredits(String rest) {
        String[] parts = rest.split("\\s+");
        if (parts.length != 2) {
            System.out.println("Usage: set-credits <code> <credits>");
            return;
        }
        Course c = courses.get(parts[0]);
        if (c == null) {
            System.out.println("No such course.");
            return;
        }
        try {
            int credits = Integer.parseInt(parts[1]);
            if (credits <= 0) {
                System.out.println("Credits must be positive.");
                return;
            }
            c.setCredits(credits);
            System.out.println("Set " + c.code() + " credits to " + credits);
        } catch (NumberFormatException e) {
            System.out.println("Credits must be an integer.");
        }
    }

    private void listCourses() {
        if (courses.isEmpty()) {
            System.out.println("No courses yet.");
            return;
        }
        courses.values().stream()
                .sorted(Comparator.comparing(Course::code))
                .forEach(c -> System.out.printf(
                        "%s — %s [credits=%d, weight%% total=%.2f]\n",
                        c.code(), c.name(), c.credits(), c.totalWeight()));
    }

    private void cmdAddAssign(String rest) {
        String[] p = rest.split("\\s+", 4);
        if (p.length < 4) {
            System.out.println("Usage: add-assign <code> <name> <earned>/<max> <weight%>");
            return;
        }
        String code = p[0];
        Course c = courses.get(code);
        if (c == null) {
            System.out.println("No such course.");
            return;
        }
        String name = p[1];
        String ratio = p[2];
        String weightStr = p[3];

        double earned, max;
        if (!ratio.contains("/")) {
            System.out.println("Use <earned>/<max> like 18/20.");
            return;
        }
        String[] em = ratio.split("/");
        try {
            earned = Double.parseDouble(em[0]);
            max = Double.parseDouble(em[1]);
            if (max <= 0) {
                System.out.println("Max points must be > 0.");
                return;
            }
            if (earned < 0) {
                System.out.println("Earned must be between 0 and max.");
                return;
            }
        } catch (NumberFormatException e) {
            System.out.println("Invalid earned/max numbers.");
            return;
        }

        double weight;
        try {
            weight = Double.parseDouble(weightStr);
            if (weight <= 0) {
                System.out.println("Weight% must be > 0.");
                return;
            }
        } catch (NumberFormatException e) {
            System.out.println("Invalid weight%.");
            return;
        }

        double newTotal = c.totalWeight() + weight;
        if (newTotal > 100.00001) {
            System.out.printf("Adding this would exceed 100%% (current %.2f%%).%n", c.totalWeight());
            return;
        }

        c.addAssignment(new Assignment(name, earned, max, weight));
        System.out.printf("Added assignment '%s' to %s (%.2f/%.2f, weight %.2f%%).%n",
                name, c.code(), earned, max, weight);
        if (Math.abs(newTotal - 100.0) < 1e-6) {
            System.out.println("Heads up: weights now total 100% for this course.");
        } else {
            System.out.printf("weights total: %.2f%%%n", newTotal);
        }
    }

    private void cmdListAssign(String rest) {
        if (rest.isBlank()) {
            System.out.println("Usage: list-assign <code>");
            return;
        }
        Course c = courses.get(rest.split("\\s+")[0]);
        if (c == null) {
            System.out.println("No such course.");
            return;
        }
        if (c.assignments().isEmpty()) {
            System.out.println("No assignments yet.");
            return;
        }
        int i = 1;
        for (Assignment a : c.assignments()) {
            System.out.printf("%d) %s  earned/max=%.2f/%.2f  weight=%.2f%%%n",
                    i++, a.name(), a.earned(), a.max(), a.weightPercent());
        }
        System.out.printf("Weight total: %.2f%%%n", c.totalWeight());
    }

    private void cmdGrade(String rest) {
        if (rest.isBlank()) {
            System.out.println("Usage: grade <code>");
            return;
        }
        Course c = courses.get(rest.split("\\s+")[0]);
        if (c == null) {
            System.out.println("No such course.");
            return;
        }
        if (Math.abs(c.totalWeight() - 100.0) > 1e-6) {
            System.out.printf("Weights must total 100%% to compute final grade (current %.2f%%).%n", c.totalWeight());
            return;
        }

        double pct = c.weightedPercentage();
        System.out.printf("%s - %s%nFinal: %.2f%%%n", c.code(), c.name(), pct);
        System.out.printf("Letter: %s (4.0 scale %.2f)%n",
                GpaScale.letterFor(pct), GpaScale.pointsFor(pct));
    }

    private void cmdGpa() {
        // GPA uses courses that have credits > 0 AND whose weights sum to 100%
        double totalPointsTimesCredits = 0.0;
        int totalCredits = 0;
        for (Course c : courses.values()) {
            if (c.credits() <= 0) continue;
            if (Math.abs(c.totalWeight() - 100.0) > 1e-6) continue;
            double pct = c.weightedPercentage();
            double pts = GpaScale.pointsFor(pct);
            totalPointsTimesCredits += pts * c.credits();
            totalCredits += c.credits();
        }
        if (totalCredits == 0) {
            System.out.println("No GPA yet. Ensure courses have credits and weights sum to 100%.");
            return;
        }
        double gpa = totalPointsTimesCredits / totalCredits;
        System.out.printf("GPA across %d credits: %.3f%n", totalCredits, gpa);
    }
}
