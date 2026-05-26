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

        public TeamMemberRequest(String name, String surname, String nickname) {
            this.name = name;
            this.surname = surname;
            this.nickname = (nickname != null && !nickname.isEmpty()) ? nickname : null;
        }
    }
}
