package com.tourney.app.models;

public class CreateMatchRequest {
    private int tournamentId;
    private int teamAId;
    private int teamBId;
    private int round;

    public CreateMatchRequest(int tournamentId, int teamAId, int teamBId, int round) {
        this.tournamentId = tournamentId;
        this.teamAId = teamAId;
        this.teamBId = teamBId;
        this.round = round;
    }

    public int getTournamentId() { return tournamentId; }
    public int getTeamAId() { return teamAId; }
    public int getTeamBId() { return teamBId; }
    public int getRound() { return round; }
}
