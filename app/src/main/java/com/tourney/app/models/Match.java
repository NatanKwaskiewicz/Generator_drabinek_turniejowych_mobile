package com.tourney.app.models;

public class Match {
    private int id;
    private int tournamentId;
    private int teamAId;
    private int teamBId;
    private int teamAScore;
    private int teamBScore;
    private int round;
    private boolean played;
    private Team teamA;
    private Team teamB;

    public int getId() { return id; }
    public int getTournamentId() { return tournamentId; }
    public int getTeamAId() { return teamAId; }
    public int getTeamBId() { return teamBId; }
    public int getTeamAScore() { return teamAScore; }
    public int getTeamBScore() { return teamBScore; }
    public int getRound() { return round; }
    public boolean isPlayed() { return played; }
    public Team getTeamA() { return teamA; }
    public Team getTeamB() { return teamB; }

    public void setTeamAScore(int score) { this.teamAScore = score; }
    public void setTeamBScore(int score) { this.teamBScore = score; }
    public void setPlayed(boolean played) { this.played = played; }

    public String getTeamAName() { return teamA != null ? teamA.getName() : "TBD"; }
    public String getTeamBName() { return teamB != null ? teamB.getName() : "TBD"; }
}
