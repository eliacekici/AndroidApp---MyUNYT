package com.example.myunyt;

public class Professor {
    private int id;
    private String name;
    private String courses;
    private String imageUrl;
    private String faculty;
    private float rating;
    private boolean hasVoted;

    public Professor(int id, String name, String courses, String imageUrl, String faculty, float rating) {
        this.id = id;
        this.name = name;
        this.courses = courses;
        this.imageUrl = imageUrl != null ? imageUrl : "";
        this.faculty = faculty;
        this.rating = rating;
        this.hasVoted = false;
    }

    public int getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public String getCourses() {
        return courses;
    }

    public String getImageUrl() {
        return imageUrl != null ? imageUrl : "";
    }
    public String getFaculty() { return faculty; }
    public float getRating() { return rating; }
    public void setRating(float rating) {
        this.rating = rating;
    }
    public boolean hasVoted() {
        return hasVoted;
    }
    public void setHasVoted(boolean hasVoted) {
        this.hasVoted = hasVoted;
    }
}