package com.tourney.app.ui.tournaments;

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
import com.tourney.app.adapters.FormatAdapter;
import com.tourney.app.adapters.TeamSelectAdapter;
import com.tourney.app.api.RetrofitClient;
import com.tourney.app.databinding.FragmentCreateTournamentBinding;
import com.tourney.app.models.CreateTournamentRequest;
import com.tourney.app.models.Format;
import com.tourney.app.models.Team;
import com.tourney.app.models.Tournament;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class CreateTournamentFragment extends Fragment {
    private FragmentCreateTournamentBinding binding;
    private FormatAdapter formatAdapter;
    private TeamSelectAdapter teamSelectAdapter;
    private List<Format> formats = new ArrayList<>();
    private int selectedFormatId = -1;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        binding = FragmentCreateTournamentBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        formatAdapter = new FormatAdapter(format -> {
            selectedFormatId = format.getId();
            binding.textSelectedFormat.setText(format.getName());
        });
        binding.recyclerFormats.setLayoutManager(new LinearLayoutManager(getContext(), LinearLayoutManager.HORIZONTAL, false));
        binding.recyclerFormats.setAdapter(formatAdapter);
        teamSelectAdapter = new TeamSelectAdapter();
        binding.recyclerTeams.setLayoutManager(new LinearLayoutManager(getContext()));
        binding.recyclerTeams.setAdapter(teamSelectAdapter);
        binding.btnCreate.setOnClickListener(v -> createTournament(view));
        loadFormats();
        loadTeams();
    }

    private void loadFormats() {
        RetrofitClient.getInstance().getApi().getFormats().enqueue(new Callback<List<Format>>() {
            @Override
            public void onResponse(@NonNull Call<List<Format>> call, @NonNull Response<List<Format>> response) {
                if (!isAdded()) return;
                if (response.isSuccessful() && response.body() != null) {
                    formats = response.body();
                    formatAdapter.setFormats(formats);
                    if (!formats.isEmpty()) {
                        selectedFormatId = formats.get(0).getId();
                        binding.textSelectedFormat.setText(formats.get(0).getName());
                        formatAdapter.setSelectedId(selectedFormatId);
                    }
                }
            }
            @Override
            public void onFailure(@NonNull Call<List<Format>> call, @NonNull Throwable t) {}
        });
    }

    private void loadTeams() {
        RetrofitClient.getInstance().getApi().getTeams().enqueue(new Callback<List<Team>>() {
            @Override
            public void onResponse(@NonNull Call<List<Team>> call, @NonNull Response<List<Team>> response) {
                if (!isAdded()) return;
                if (response.isSuccessful() && response.body() != null) {
                    teamSelectAdapter.setTeams(response.body());
                    binding.textNoTeams.setVisibility(response.body().isEmpty() ? View.VISIBLE : View.GONE);
                }
            }
            @Override
            public void onFailure(@NonNull Call<List<Team>> call, @NonNull Throwable t) {}
        });
    }

    private void createTournament(View view) {
        String name = binding.editTournamentName.getText().toString().trim();
        if (name.isEmpty()) {
            binding.editTournamentName.setError("Tournament name is required");
            return;
        }
        if (selectedFormatId == -1) {
            Toast.makeText(getContext(), "Please select a format", Toast.LENGTH_SHORT).show();
            return;
        }
        List<Integer> selectedTeamIds = teamSelectAdapter.getSelectedTeamIds();
        List<CreateTournamentRequest.TeamIdWrapper> teamWrappers = new ArrayList<>();
        for (int id : selectedTeamIds) teamWrappers.add(new CreateTournamentRequest.TeamIdWrapper(id));
        String date = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'", Locale.US).format(new Date());
        CreateTournamentRequest request = new CreateTournamentRequest(name, date, selectedFormatId, teamWrappers.isEmpty() ? null : teamWrappers);
        binding.btnCreate.setEnabled(false);
        binding.btnCreate.setText("Creating...");
        RetrofitClient.getInstance().getApi().createTournament(request).enqueue(new Callback<Tournament>() {
            @Override
            public void onResponse(@NonNull Call<Tournament> call, @NonNull Response<Tournament> response) {
                if (!isAdded()) return;
                binding.btnCreate.setEnabled(true);
                binding.btnCreate.setText("Create Tournament");
                if (response.isSuccessful() && response.body() != null) {
                    Bundle args = new Bundle();
                    args.putInt("tournamentId", response.body().getId());
                    Navigation.findNavController(view).navigate(R.id.action_createTournament_to_bracket, args);
                } else {
                    Toast.makeText(getContext(), "Failed to create tournament", Toast.LENGTH_SHORT).show();
                }
            }
            @Override
            public void onFailure(@NonNull Call<Tournament> call, @NonNull Throwable t) {
                if (!isAdded()) return;
                binding.btnCreate.setEnabled(true);
                binding.btnCreate.setText("Create Tournament");
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
