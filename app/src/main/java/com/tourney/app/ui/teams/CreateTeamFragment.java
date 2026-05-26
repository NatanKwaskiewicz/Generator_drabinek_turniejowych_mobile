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
import com.tourney.app.R;
import com.tourney.app.adapters.TeamMemberInputAdapter;
import com.tourney.app.api.RetrofitClient;
import com.tourney.app.databinding.FragmentCreateTeamBinding;
import com.tourney.app.models.CreateTeamRequest;
import com.tourney.app.models.Team;
import java.util.ArrayList;
import java.util.List;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class CreateTeamFragment extends Fragment {
    private FragmentCreateTeamBinding binding;
    private TeamMemberInputAdapter memberAdapter;
    private int memberCount = 2;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        binding = FragmentCreateTeamBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        memberAdapter = new TeamMemberInputAdapter(memberCount);
        binding.recyclerMembers.setLayoutManager(new androidx.recyclerview.widget.LinearLayoutManager(getContext()));
        binding.recyclerMembers.setAdapter(memberAdapter);
        binding.textMemberCount.setText(String.valueOf(memberCount));
        binding.btnMinus.setOnClickListener(v -> {
            if (memberCount > 1) {
                memberCount--;
                binding.textMemberCount.setText(String.valueOf(memberCount));
                memberAdapter.setCount(memberCount);
            }
        });
        binding.btnPlus.setOnClickListener(v -> {
            if (memberCount < 32) {
                memberCount++;
                binding.textMemberCount.setText(String.valueOf(memberCount));
                memberAdapter.setCount(memberCount);
            }
        });
        binding.btnCreateTeam.setOnClickListener(v -> createTeam(view));
    }

    private void createTeam(View view) {
        String teamName = binding.editTeamName.getText().toString().trim();
        if (teamName.isEmpty()) {
            binding.editTeamName.setError("Team name is required");
            return;
        }
        List<CreateTeamRequest.TeamMemberRequest> members = memberAdapter.getFilledMembers();
        CreateTeamRequest request = new CreateTeamRequest(teamName, members.isEmpty() ? null : members);
        binding.btnCreateTeam.setEnabled(false);
        binding.btnCreateTeam.setText("Creating...");
        RetrofitClient.getInstance().getApi().createTeam(request).enqueue(new Callback<Team>() {
            @Override
            public void onResponse(@NonNull Call<Team> call, @NonNull Response<Team> response) {
                if (!isAdded()) return;
                binding.btnCreateTeam.setEnabled(true);
                binding.btnCreateTeam.setText("Create Team");
                if (response.isSuccessful()) {
                    Toast.makeText(getContext(), "Team created!", Toast.LENGTH_SHORT).show();
                    Navigation.findNavController(view).popBackStack();
                } else {
                    Toast.makeText(getContext(), "Failed to create team", Toast.LENGTH_SHORT).show();
                }
            }
            @Override
            public void onFailure(@NonNull Call<Team> call, @NonNull Throwable t) {
                if (!isAdded()) return;
                binding.btnCreateTeam.setEnabled(true);
                binding.btnCreateTeam.setText("Create Team");
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
