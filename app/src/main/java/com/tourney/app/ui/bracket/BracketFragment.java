package com.tourney.app.ui.bracket;

import android.app.AlertDialog;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import com.tourney.app.R;
import com.tourney.app.adapters.MatchAdapter;
import com.tourney.app.adapters.RoundRobinAdapter;
import com.tourney.app.adapters.SwissAdapter;
import com.tourney.app.api.RetrofitClient;
import com.tourney.app.databinding.FragmentBracketBinding;
import com.tourney.app.models.Match;
import com.tourney.app.models.Tournament;
import com.tourney.app.models.UpdateScoreRequest;
import java.util.ArrayList;
import java.util.List;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class BracketFragment extends Fragment {
    private FragmentBracketBinding binding;
    private int tournamentId;
    private Tournament tournament;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        binding = FragmentBracketBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        if (getArguments() != null) {
            tournamentId = getArguments().getInt("tournamentId", -1);
        }
        if (tournamentId == -1) {
            Toast.makeText(getContext(), "Invalid tournament", Toast.LENGTH_SHORT).show();
            return;
        }
        loadTournament();
    }

    private void loadTournament() {
        binding.progressBar.setVisibility(View.VISIBLE);
        binding.contentLayout.setVisibility(View.GONE);
        RetrofitClient.getInstance().getApi().getTournament(tournamentId).enqueue(new Callback<Tournament>() {
            @Override
            public void onResponse(@NonNull Call<Tournament> call, @NonNull Response<Tournament> response) {
                if (!isAdded()) return;
                binding.progressBar.setVisibility(View.GONE);
                if (response.isSuccessful() && response.body() != null) {
                    tournament = response.body();
                    renderTournament();
                } else {
                    Toast.makeText(getContext(), "Failed to load tournament", Toast.LENGTH_SHORT).show();
                }
            }
            @Override
            public void onFailure(@NonNull Call<Tournament> call, @NonNull Throwable t) {
                if (!isAdded()) return;
                binding.progressBar.setVisibility(View.GONE);
                Toast.makeText(getContext(), "Connection error", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void renderTournament() {
        binding.contentLayout.setVisibility(View.VISIBLE);
        binding.textTournamentName.setText(tournament.getName());
        String formatName = tournament.getFormat() != null ? tournament.getFormat().getName() : "";
        binding.textFormatName.setText(formatName);
        int teamCount = tournament.getTournamentTeams() != null ? tournament.getTournamentTeams().size() : 0;
        binding.textTeamCount.setText(teamCount + " teams");
        List<Match> matches = tournament.getMatches();
        if (matches == null || matches.isEmpty()) {
            binding.layoutNoMatches.setVisibility(View.VISIBLE);
            binding.layoutMatches.setVisibility(View.GONE);
            setupGenerateButtons(formatName);
        } else {
            binding.layoutNoMatches.setVisibility(View.GONE);
            binding.layoutMatches.setVisibility(View.VISIBLE);
            if ("Round Robin".equals(formatName)) {
                renderRoundRobin(matches);
            } else if ("Swiss".equals(formatName)) {
                renderSwiss(matches);
            } else {
                renderElimination(matches);
            }
        }
    }

    private void setupGenerateButtons(String formatName) {
        binding.btnGenerateMatches.setOnClickListener(v -> {
            Call<List<Match>> call;
            if ("Round Robin".equals(formatName)) {
                call = RetrofitClient.getInstance().getApi().generateRoundRobinMatches(tournamentId);
            } else if ("Swiss".equals(formatName)) {
                call = RetrofitClient.getInstance().getApi().generateSwissMatches(tournamentId);
            } else {
                call = RetrofitClient.getInstance().getApi().generateMatches(tournamentId);
            }
            binding.btnGenerateMatches.setEnabled(false);
            call.enqueue(new Callback<List<Match>>() {
                @Override
                public void onResponse(@NonNull Call<List<Match>> call, @NonNull Response<List<Match>> response) {
                    if (!isAdded()) return;
                    binding.btnGenerateMatches.setEnabled(true);
                    if (response.isSuccessful()) {
                        loadTournament();
                    } else {
                        try {
                            String err = response.errorBody() != null ? response.errorBody().string() : "Error";
                            Toast.makeText(getContext(), err, Toast.LENGTH_LONG).show();
                        } catch (Exception e) {
                            Toast.makeText(getContext(), "Failed to generate matches", Toast.LENGTH_SHORT).show();
                        }
                    }
                }
                @Override
                public void onFailure(@NonNull Call<List<Match>> call, @NonNull Throwable t) {
                    if (!isAdded()) return;
                    binding.btnGenerateMatches.setEnabled(true);
                    Toast.makeText(getContext(), "Connection error", Toast.LENGTH_SHORT).show();
                }
            });
        });
    }

    private void renderElimination(List<Match> matches) {
        binding.layoutRounds.removeAllViews();
        int maxRound = 0;
        for (Match m : matches) if (m.getRound() > maxRound) maxRound = m.getRound();
        for (int round = 1; round <= maxRound; round++) {
            List<Match> roundMatches = new ArrayList<>();
            for (Match m : matches) if (m.getRound() == round) roundMatches.add(m);
            if (roundMatches.isEmpty()) continue;
            View roundView = LayoutInflater.from(getContext()).inflate(R.layout.item_round_section, binding.layoutRounds, false);
            androidx.appcompat.widget.AppCompatTextView roundTitle = roundView.findViewById(R.id.text_round_title);
            roundTitle.setText("Round " + round);
            androidx.recyclerview.widget.RecyclerView rv = roundView.findViewById(R.id.recycler_round_matches);
            rv.setLayoutManager(new LinearLayoutManager(getContext()));
            int finalRound = round;
            MatchAdapter adapter = new MatchAdapter(roundMatches, match -> showScoreDialog(match));
            rv.setAdapter(adapter);
            binding.layoutRounds.addView(roundView);
            if (round < maxRound) continue;
            // advance button for last round
            View btnAdvance = LayoutInflater.from(getContext()).inflate(R.layout.item_advance_button, binding.layoutRounds, false);
            com.google.android.material.button.MaterialButton btn = btnAdvance.findViewById(R.id.btn_advance);
            btn.setText("Advance to Round " + (round + 1));
            int finalRound1 = round;
            btn.setOnClickListener(v -> advanceRound(finalRound1));
            binding.layoutRounds.addView(btnAdvance);
        }
    }

    private void renderRoundRobin(List<Match> matches) {
        binding.layoutRounds.removeAllViews();
        View rrView = LayoutInflater.from(getContext()).inflate(R.layout.layout_round_robin, binding.layoutRounds, false);
        androidx.recyclerview.widget.RecyclerView rv = rrView.findViewById(R.id.recycler_rr_matches);
        rv.setLayoutManager(new LinearLayoutManager(getContext()));
        RoundRobinAdapter adapter = new RoundRobinAdapter(matches, tournament, match -> showScoreDialog(match));
        rv.setAdapter(adapter);
        binding.layoutRounds.addView(rrView);
    }

    private void renderSwiss(List<Match> matches) {
        binding.layoutRounds.removeAllViews();
        int maxRound = matches.stream().mapToInt(Match::getRound).filter(m -> m >= 0).max().orElse(0);
        for (int round = 1; round <= maxRound; round++) {
            List<Match> roundMatches = new ArrayList<>();
            for (Match m : matches) if (m.getRound() == round) roundMatches.add(m);
            if (roundMatches.isEmpty()) continue;
            View roundView = LayoutInflater.from(getContext()).inflate(R.layout.item_round_section, binding.layoutRounds, false);
            androidx.appcompat.widget.AppCompatTextView roundTitle = roundView.findViewById(R.id.text_round_title);
            roundTitle.setText("Swiss Round " + round);
            androidx.recyclerview.widget.RecyclerView rv = roundView.findViewById(R.id.recycler_round_matches);
            rv.setLayoutManager(new LinearLayoutManager(getContext()));
            MatchAdapter adapter = new MatchAdapter(roundMatches, match -> showScoreDialog(match));
            rv.setAdapter(adapter);
            binding.layoutRounds.addView(roundView);
        }
        View btnAdvance = LayoutInflater.from(getContext()).inflate(R.layout.item_advance_button, binding.layoutRounds, false);
        com.google.android.material.button.MaterialButton btn = btnAdvance.findViewById(R.id.btn_advance);
        btn.setText("Advance Swiss Round");
        btn.setOnClickListener(v -> advanceSwissRound(maxRound));
        binding.layoutRounds.addView(btnAdvance);
    }

    private void showScoreDialog(Match match) {
        View dialogView = LayoutInflater.from(getContext()).inflate(R.layout.dialog_score, null);
        EditText editScoreA = dialogView.findViewById(R.id.edit_score_a);
        EditText editScoreB = dialogView.findViewById(R.id.edit_score_b);
        androidx.appcompat.widget.AppCompatTextView textTeamA = dialogView.findViewById(R.id.text_team_a);
        androidx.appcompat.widget.AppCompatTextView textTeamB = dialogView.findViewById(R.id.text_team_b);
        textTeamA.setText(match.getTeamAName());
        textTeamB.setText(match.getTeamBName());
        editScoreA.setText(String.valueOf(match.getTeamAScore()));
        editScoreB.setText(String.valueOf(match.getTeamBScore()));
        new AlertDialog.Builder(requireContext())
            .setTitle("Update Score")
            .setView(dialogView)
            .setPositiveButton("Save", (dialog, which) -> {
                try {
                    int scoreA = Integer.parseInt(editScoreA.getText().toString());
                    int scoreB = Integer.parseInt(editScoreB.getText().toString());
                    updateScore(match, scoreA, scoreB);
                } catch (NumberFormatException e) {
                    Toast.makeText(getContext(), "Invalid score", Toast.LENGTH_SHORT).show();
                }
            })
            .setNegativeButton("Cancel", null)
            .show();
    }

    private void updateScore(Match match, int scoreA, int scoreB) {
        RetrofitClient.getInstance().getApi()
            .updateMatchScore(match.getId(), new UpdateScoreRequest(scoreA, scoreB))
            .enqueue(new Callback<Match>() {
                @Override
                public void onResponse(@NonNull Call<Match> call, @NonNull Response<Match> response) {
                    if (!isAdded()) return;
                    if (response.isSuccessful()) {
                        loadTournament();
                    } else {
                        Toast.makeText(getContext(), "Failed to update score", Toast.LENGTH_SHORT).show();
                    }
                }
                @Override
                public void onFailure(@NonNull Call<Match> call, @NonNull Throwable t) {
                    if (!isAdded()) return;
                    Toast.makeText(getContext(), "Connection error", Toast.LENGTH_SHORT).show();
                }
            });
    }

    private void advanceRound(int round) {
        RetrofitClient.getInstance().getApi().advanceRound(tournamentId, round).enqueue(new Callback<List<Match>>() {
            @Override
            public void onResponse(@NonNull Call<List<Match>> call, @NonNull Response<List<Match>> response) {
                if (!isAdded()) return;
                if (response.isSuccessful()) {
                    loadTournament();
                } else {
                    try {
                        String err = response.errorBody() != null ? response.errorBody().string() : "Cannot advance";
                        Toast.makeText(getContext(), err, Toast.LENGTH_LONG).show();
                    } catch (Exception e) {
                        Toast.makeText(getContext(), "Cannot advance round", Toast.LENGTH_SHORT).show();
                    }
                }
            }
            @Override
            public void onFailure(@NonNull Call<List<Match>> call, @NonNull Throwable t) {
                if (!isAdded()) return;
                Toast.makeText(getContext(), "Connection error", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void advanceSwissRound(int round) {
        RetrofitClient.getInstance().getApi().advanceSwissRound(tournamentId, round).enqueue(new Callback<List<Match>>() {
            @Override
            public void onResponse(@NonNull Call<List<Match>> call, @NonNull Response<List<Match>> response) {
                if (!isAdded()) return;
                if (response.isSuccessful()) {
                    loadTournament();
                } else {
                    try {
                        String err = response.errorBody() != null ? response.errorBody().string() : "Cannot advance";
                        Toast.makeText(getContext(), err, Toast.LENGTH_LONG).show();
                    } catch (Exception e) {
                        Toast.makeText(getContext(), "Cannot advance Swiss round", Toast.LENGTH_SHORT).show();
                    }
                }
            }
            @Override
            public void onFailure(@NonNull Call<List<Match>> call, @NonNull Throwable t) {
                if (!isAdded()) return;
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
