package com.tourney.app.models;

import java.util.List;

public class Team {
    private int id;
    private String name;
    private List<TeamMember> teamMembers;

    public int getId() { return id; }
    public String getName() { return name; }
    public List<TeamMember> getTeamMembers() { return teamMembers; }

    public static class TeamMember {
        private int id;
        private String name;
        private String surname;
        private String nickname;
        private String countryCode;
        private int teamId;

        public int getId() { return id; }
        public String getName() { return name; }
        public String getSurname() { return surname; }
        public String getNickname() { return nickname; }
        public String getCountryCode() { return countryCode; }
        public int getTeamId() { return teamId; }

        public String getDisplayName() {
            if (nickname != null && !nickname.isEmpty()) {
                return name + " \"" + nickname + "\" " + surname;
            }
            return name + " " + surname;
        }
    }
}
