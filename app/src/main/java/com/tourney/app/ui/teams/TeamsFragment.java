package com.tourney.app.ui.teams;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.navigation.Navigation;
import androidx.recyclerview.widget.LinearLayoutManager;
import com.tourney.app.R;
import com.tourney.app.adapters.TeamAdapter;
import com.tourney.app.api.RetrofitClient;
import com.tourney.app.databinding.FragmentTeamsBinding;
import com.tourney.app.models.Team;
import java.util.List;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class TeamsFragment extends Fragment {
    private FragmentTeamsBinding binding;
    private TeamAdapter adapter;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        binding = FragmentTeamsBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        adapter = new TeamAdapter(team -> {
            Bundle args = new Bundle();
            args.putInt("teamId", team.getId());
            Navigation.findNavController(view).navigate(R.id.action_teams_to_teamDetail, args);
        });
        binding.recyclerTeams.setLayoutManager(new LinearLayoutManager(getContext()));
        binding.recyclerTeams.setAdapter(adapter);
        binding.fabCreateTeam.setOnClickListener(v ->
            Navigation.findNavController(view).navigate(R.id.action_teams_to_createTeam));
        loadTeams();
    }

    private void loadTeams() {
        binding.progressBar.setVisibility(View.VISIBLE);
        binding.recyclerTeams.setVisibility(View.GONE);
        binding.textEmpty.setVisibility(View.GONE);
        RetrofitClient.getInstance().getApi().getTeams().enqueue(new Callback<List<Team>>() {
            @Override
            public void onResponse(@NonNull Call<List<Team>> call, @NonNull Response<List<Team>> response) {
                if (!isAdded()) return;
                binding.progressBar.setVisibility(View.GONE);
                if (response.isSuccessful() && response.body() != null) {
                    List<Team> teams = response.body();
                    adapter.setTeams(teams);
                    binding.recyclerTeams.setVisibility(View.VISIBLE);
                    binding.textEmpty.setVisibility(teams.isEmpty() ? View.VISIBLE : View.GONE);
                }
            }
            @Override
            public void onFailure(@NonNull Call<List<Team>> call, @NonNull Throwable t) {
                if (!isAdded()) return;
                binding.progressBar.setVisibility(View.GONE);
                binding.textEmpty.setVisibility(View.VISIBLE);
                binding.textEmpty.setText("Connection error");
            }
        });
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}
