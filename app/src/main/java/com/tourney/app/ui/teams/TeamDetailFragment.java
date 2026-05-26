package com.tourney.app.ui.teams;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import com.tourney.app.adapters.TeamMemberAdapter;
import com.tourney.app.api.RetrofitClient;
import com.tourney.app.databinding.FragmentTeamDetailBinding;
import com.tourney.app.models.Team;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class TeamDetailFragment extends Fragment {
    private FragmentTeamDetailBinding binding;
    private int teamId;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        binding = FragmentTeamDetailBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        if (getArguments() != null) teamId = getArguments().getInt("teamId", -1);
        if (teamId == -1) return;
        loadTeam();
    }

    private void loadTeam() {
        binding.progressBar.setVisibility(View.VISIBLE);
        RetrofitClient.getInstance().getApi().getTeam(teamId).enqueue(new Callback<Team>() {
            @Override
            public void onResponse(@NonNull Call<Team> call, @NonNull Response<Team> response) {
                if (!isAdded()) return;
                binding.progressBar.setVisibility(View.GONE);
                if (response.isSuccessful() && response.body() != null) {
                    Team team = response.body();
                    binding.textTeamName.setText(team.getName());
                    int memberCount = team.getTeamMembers() != null ? team.getTeamMembers().size() : 0;
                    binding.textMemberCount.setText(memberCount + " members");
                    TeamMemberAdapter adapter = new TeamMemberAdapter(team.getTeamMembers());
                    binding.recyclerMembers.setLayoutManager(new LinearLayoutManager(getContext()));
                    binding.recyclerMembers.setAdapter(adapter);
                    binding.textEmpty.setVisibility(memberCount == 0 ? View.VISIBLE : View.GONE);
                }
            }
            @Override
            public void onFailure(@NonNull Call<Team> call, @NonNull Throwable t) {
                if (!isAdded()) return;
                binding.progressBar.setVisibility(View.GONE);
                Toast.makeText(getContext(), "Connection error", Toast.LENGTH_SHORT).show();
            }
        });
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}
