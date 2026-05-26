package com.tourney.app.models;

import java.util.List;

public class CreateTournamentRequest {
    private String name;
    private String date;
    private int formatId;
    private List<TeamIdWrapper> teams;

    public CreateTournamentRequest(String name, String date, int formatId, List<TeamIdWrapper> teams) {
        this.name = name;
        this.date = date;
        this.formatId = formatId;
        this.teams = teams;
    }

    public static class TeamIdWrapper {
        private int teamId;
        public TeamIdWrapper(int teamId) { this.teamId = teamId; }
    }
}
