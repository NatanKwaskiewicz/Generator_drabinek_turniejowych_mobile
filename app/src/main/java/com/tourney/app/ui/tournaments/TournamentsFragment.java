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
import com.tourney.app.adapters.TournamentAdapter;
import com.tourney.app.api.RetrofitClient;
import com.tourney.app.databinding.FragmentTournamentsBinding;
import com.tourney.app.models.Tournament;
import java.util.List;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class TournamentsFragment extends Fragment {
    private FragmentTournamentsBinding binding;
    private TournamentAdapter adapter;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        binding = FragmentTournamentsBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        adapter = new TournamentAdapter(
            tournament -> {
                Bundle args = new Bundle();
                args.putInt("tournamentId", tournament.getId());
                Navigation.findNavController(view).navigate(R.id.action_tournaments_to_bracket, args);
            },
            tournament -> deleteTournament(tournament, view)
        );
        binding.recyclerTournaments.setLayoutManager(new LinearLayoutManager(getContext()));
        binding.recyclerTournaments.setAdapter(adapter);
        binding.fabCreateTournament.setOnClickListener(v ->
            Navigation.findNavController(view).navigate(R.id.action_tournaments_to_createTournament));
        loadTournaments();
    }

    private void loadTournaments() {
        binding.progressBar.setVisibility(View.VISIBLE);
        binding.recyclerTournaments.setVisibility(View.GONE);
        binding.textEmpty.setVisibility(View.GONE);
        RetrofitClient.getInstance().getApi().getTournaments().enqueue(new Callback<List<Tournament>>() {
            @Override
            public void onResponse(@NonNull Call<List<Tournament>> call, @NonNull Response<List<Tournament>> response) {
                if (!isAdded()) return;
                binding.progressBar.setVisibility(View.GONE);
                if (response.isSuccessful() && response.body() != null) {
                    List<Tournament> tournaments = response.body();
                    adapter.setTournaments(tournaments);
                    binding.recyclerTournaments.setVisibility(View.VISIBLE);
                    binding.textEmpty.setVisibility(tournaments.isEmpty() ? View.VISIBLE : View.GONE);
                }
            }
            @Override
            public void onFailure(@NonNull Call<List<Tournament>> call, @NonNull Throwable t) {
                if (!isAdded()) return;
                binding.progressBar.setVisibility(View.GONE);
                binding.textEmpty.setVisibility(View.VISIBLE);
                binding.textEmpty.setText("Connection error. Is the server running?");
            }
        });
    }

    private void deleteTournament(Tournament tournament, View view) {
        new androidx.appcompat.app.AlertDialog.Builder(requireContext())
            .setTitle("Delete Tournament")
            .setMessage("Delete \"" + tournament.getName() + "\"? This cannot be undone.")
            .setPositiveButton("Delete", (dialog, which) -> {
                RetrofitClient.getInstance().getApi().deleteTournament(tournament.getId()).enqueue(new Callback<Void>() {
                    @Override
                    public void onResponse(@NonNull Call<Void> call, @NonNull Response<Void> response) {
                        if (!isAdded()) return;
                        if (response.isSuccessful()) {
                            loadTournaments();
                        } else {
                            Toast.makeText(getContext(), "Failed to delete", Toast.LENGTH_SHORT).show();
                        }
                    }
                    @Override
                    public void onFailure(@NonNull Call<Void> call, @NonNull Throwable t) {
                        if (!isAdded()) return;
                        Toast.makeText(getContext(), "Connection error", Toast.LENGTH_SHORT).show();
                    }
                });
            })
            .setNegativeButton("Cancel", null)
            .show();
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}
