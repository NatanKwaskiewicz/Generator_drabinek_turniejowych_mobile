package com.tourney.app.models;

import com.google.gson.annotations.SerializedName;
import java.util.List;

public class Tournament {
    private int id;
    private String name;
    private String date;
    private String createdAt;
    private int formatId;
    private Format format;
    @SerializedName("TournamentTeam")
    private List<TournamentTeam> tournamentTeams;
    @SerializedName("Match")
    private List<Match> matches;

    public int getId() { return id; }
    public String getName() { return name; }
    public String getDate() { return date; }
    public String getCreatedAt() { return createdAt; }
    public int getFormatId() { return formatId; }
    public Format getFormat() { return format; }
    public List<TournamentTeam> getTournamentTeams() { return tournamentTeams; }
    public List<Match> getMatches() { return matches; }

    public static class TournamentTeam {
        private int teamId;
        private Team team;
        public int getTeamId() { return teamId; }
        public Team getTeam() { return team; }
    }
}
