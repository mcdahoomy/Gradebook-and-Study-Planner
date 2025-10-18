package com.dahoomy.gradebook;

/**
 * A simple planner item connected to a course.
 * Keep it tiny: title, due date (ISO yyyy-MM-dd), and a status flag.
 */
public class StudyTask {
    private final String courseCode;
    private String title;
    private String due;   // date as text, e.g., 2025-10-16
    private String status;   // "TODO" or "DONE"

    public StudyTask(String courseCode, String title, String due, String status) {
        if (courseCode == null || courseCode.isBlank()) throw new IllegalArgumentException("courseCode blank");
        if (title == null || title.isBlank())           throw new IllegalArgumentException("title blank");
        if (due == null || !due.matches("\\d{4}-\\d{2}-\\d{2}")) throw new IllegalArgumentException("bad date");
        this.courseCode = courseCode;
        this.title = title;
        this.due = due;
        setStatus(status == null ? "TODO" : status);
    }

    public String getCourseCode() { return courseCode; }
    public String getTitle() { return title; }
    public String getDue() { return due; }
    public String getStatus() { return status; }

    public void setTitle(String title) {
        if (title == null || title.isBlank()) throw new IllegalArgumentException("title blank");
        this.title = title;
    }

    public void setDue(String due) {
        if (due == null || !due.matches("\\d{4}-\\d{2}-\\d{2}")) throw new IllegalArgumentException("bad date");
        this.due = due;
    }

    public void setStatus(String status) {
        String s = (status == null ? "TODO" : status.toUpperCase());
        if (!s.equals("TODO") && !s.equals("DONE")) s = "TODO";
        this.status = s;
    }
}
