package com.tourney.app.models;

public class UpdateScoreRequest {
    private int teamAScore;
    private int teamBScore;

    public UpdateScoreRequest(int teamAScore, int teamBScore) {
        this.teamAScore = teamAScore;
        this.teamBScore = teamBScore;
    }
}
