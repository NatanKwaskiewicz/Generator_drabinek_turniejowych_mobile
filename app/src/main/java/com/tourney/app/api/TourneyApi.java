package com.tourney.app.api;

import com.tourney.app.models.CreateMatchRequest;
import com.tourney.app.models.CreateTeamRequest;
import com.tourney.app.models.CreateTournamentRequest;
import com.tourney.app.models.Format;
import com.tourney.app.models.Match;
import com.tourney.app.models.Team;
import com.tourney.app.models.Tournament;
import com.tourney.app.models.UpdateScoreRequest;
import java.util.List;
import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.DELETE;
import retrofit2.http.GET;
import retrofit2.http.PATCH;
import retrofit2.http.POST;
import retrofit2.http.Path;

public interface TourneyApi {
    @GET("tournaments")
    Call<List<Tournament>> getTournaments();

    @GET("tournaments/{id}")
    Call<Tournament> getTournament(@Path("id") int id);

    @POST("tournaments")
    Call<Tournament> createTournament(@Body CreateTournamentRequest request);

    @DELETE("tournaments/{id}")
    Call<Void> deleteTournament(@Path("id") int id);

    @GET("teams")
    Call<List<Team>> getTeams();

    @GET("teams/{id}")
    Call<Team> getTeam(@Path("id") int id);

    @POST("teams")
    Call<Team> createTeam(@Body CreateTeamRequest request);

    @GET("formats")
    Call<List<Format>> getFormats();

    @POST("matches/generate/{tournamentId}")
    Call<List<Match>> generateMatches(@Path("tournamentId") int tournamentId);

    @POST("matches/generate-swiss/{tournamentId}")
    Call<List<Match>> generateSwissMatches(@Path("tournamentId") int tournamentId);

    @POST("matches/advance/{tournamentId}/{round}")
    Call<List<Match>> advanceRound(@Path("tournamentId") int tournamentId,
                                   @Path("round") int round);

    @POST("matches/advance-swiss/{tournamentId}/{round}")
    Call<List<Match>> advanceSwissRound(@Path("tournamentId") int tournamentId,
                                        @Path("round") int round);

    @PATCH("matches/{id}")
    Call<Match> updateMatchScore(@Path("id") int id, @Body UpdateScoreRequest request);

    @POST("matches/bulk")
    Call<List<Match>> createMatches(@Body List<CreateMatchRequest> matches);
}
