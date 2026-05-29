package com.tourney.app.models;

import java.util.List;

public class CreateTeamRequest {
    private String name;
    private List<TeamMemberRequest> teamMember;

    public CreateTeamRequest(String name, List<TeamMemberRequest> teamMember) {
        this.name = name;
        this.teamMember = teamMember;
    }

    public static class TeamMemberRequest {
        private String name;
        private String surname;
        private String nickname;
        private String countryCode;

        public TeamMemberRequest(String name, String surname, String nickname, String countryCode) {
            this.name = name;
            this.surname = surname;
            this.nickname = (nickname != null && !nickname.isEmpty()) ? nickname : null;
            this.countryCode = (countryCode != null && !countryCode.isEmpty()) ? countryCode : null;
        }
    }
}
