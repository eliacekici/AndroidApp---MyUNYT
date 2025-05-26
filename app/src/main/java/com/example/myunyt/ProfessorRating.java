package com.example.myunyt;

public class ProfessorRating {
    private String professorId;
    private int rating;
    private String comment;

    public ProfessorRating(String professorId, int rating, String comment) {
        this.professorId = professorId;
        this.rating = rating;
        this.comment = comment;
    }

    // Getters
    public String getProfessorId() { return professorId; }
    public int getRating() { return rating; }
    public String getComment() { return comment; }
}
