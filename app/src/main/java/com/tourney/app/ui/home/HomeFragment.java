package com.tourney.app.ui.home;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.navigation.Navigation;
import com.google.android.material.button.MaterialButton;
import com.tourney.app.R;
import com.tourney.app.api.RetrofitClient;
import com.tourney.app.models.Team;
import com.tourney.app.models.Tournament;
import java.util.List;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class HomeFragment extends Fragment {

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_home, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        TextView textTournamentCount = view.findViewById(R.id.text_tournament_count);
        TextView textTeamCount = view.findViewById(R.id.text_team_count);
        MaterialButton btnGoTournaments = view.findViewById(R.id.btn_go_tournaments);
        MaterialButton btnGoTeams = view.findViewById(R.id.btn_go_teams);

        btnGoTournaments.setOnClickListener(v ->
            Navigation.findNavController(v).navigate(R.id.tournamentsFragment));
        btnGoTeams.setOnClickListener(v ->
            Navigation.findNavController(v).navigate(R.id.teamsFragment));

        RetrofitClient.getInstance().getApi().getTournaments().enqueue(new Callback<List<Tournament>>() {
            @Override
            public void onResponse(@NonNull Call<List<Tournament>> call, @NonNull Response<List<Tournament>> response) {
                if (!isAdded()) return;
                if (response.isSuccessful() && response.body() != null) {
                    textTournamentCount.setText(String.valueOf(response.body().size()));
                }
            }
            @Override
            public void onFailure(@NonNull Call<List<Tournament>> call, @NonNull Throwable t) {}
        });

        RetrofitClient.getInstance().getApi().getTeams().enqueue(new Callback<List<Team>>() {
            @Override
            public void onResponse(@NonNull Call<List<Team>> call, @NonNull Response<List<Team>> response) {
                if (!isAdded()) return;
                if (response.isSuccessful() && response.body() != null) {
                    textTeamCount.setText(String.valueOf(response.body().size()));
                }
            }
            @Override
            public void onFailure(@NonNull Call<List<Team>> call, @NonNull Throwable t) {}
        });
    }
}
