package com.tourney.app.ui.bracket;

import android.app.AlertDialog;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import com.tourney.app.R;
import com.tourney.app.adapters.MatchAdapter;
import com.tourney.app.adapters.RoundRobinAdapter;
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

    private boolean isAdvancing = false;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
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
        RetrofitClient.getInstance().getApi().getTournament(tournamentId)
                .enqueue(new Callback<>() {
                    @Override
                    public void onResponse(@NonNull Call<Tournament> call,
                                           @NonNull Response<Tournament> response) {
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
        int teamCount = tournament.getTournamentTeams() != null
                ? tournament.getTournamentTeams().size() : 0;
        binding.textTeamCount.setText(teamCount + " teams");

        List<Match> matches = tournament.getMatches();
        if (matches == null || matches.isEmpty()) {
            binding.layoutNoMatches.setVisibility(View.VISIBLE);
            binding.layoutMatches.setVisibility(View.GONE);
            setupGenerateButton(formatName);
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

    private void setupGenerateButton(String formatName) {
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
            call.enqueue(new Callback<>() {
                @Override
                public void onResponse(@NonNull Call<List<Match>> call,
                                       @NonNull Response<List<Match>> response) {
                    if (!isAdded()) return;
                    binding.btnGenerateMatches.setEnabled(true);
                    if (response.isSuccessful()) {
                        loadTournament();
                    } else {
                        try {
                            String err = response.errorBody() != null
                                    ? response.errorBody().string() : "Error";
                            Toast.makeText(getContext(), err, Toast.LENGTH_LONG).show();
                        } catch (Exception e) {
                            Toast.makeText(getContext(), "Failed to generate", Toast.LENGTH_SHORT).show();
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

    private void renderRoundRobin(List<Match> matches) {
        binding.layoutRounds.removeAllViews();

        List<Match> sorted = new ArrayList<>(matches);
        sorted.sort((a, b) -> {
            if (a.getRound() != b.getRound()) return Integer.compare(a.getRound(), b.getRound());
            return Integer.compare(a.getId(), b.getId());
        });

        int maxRound = 0;
        for (Match m : sorted) if (m.getRound() > maxRound) maxRound = m.getRound();
        if (maxRound == 0) return;

        int teamCount = tournament.getTournamentTeams() != null
                ? tournament.getTournamentTeams().size() : 0;
        int numRoundsPerLeg;
        if (teamCount < 2) {
            numRoundsPerLeg = maxRound / 2;
        } else if (teamCount % 2 == 0) {
            numRoundsPerLeg = teamCount - 1;
        } else {
            numRoundsPerLeg = teamCount;
        }

        for (int round = 1; round <= maxRound; round++) {
            final List<Match> roundMatches = new ArrayList<>();
            for (Match m : sorted) if (m.getRound() == round) roundMatches.add(m);
            if (roundMatches.isEmpty()) continue;

            View roundView = LayoutInflater.from(getContext()).inflate(
                    R.layout.item_round_section, binding.layoutRounds, false);
            androidx.appcompat.widget.AppCompatTextView roundTitle =
                    roundView.findViewById(R.id.text_round_title);

            boolean isSecondLeg = (round > numRoundsPerLeg);
            int matchday = isSecondLeg ? (round - numRoundsPerLeg) : round;
            String label = "Matchday " + matchday + (isSecondLeg ? " (2nd Leg)" : "");
            roundTitle.setText(label);

            androidx.recyclerview.widget.RecyclerView rv =
                    roundView.findViewById(R.id.recycler_round_matches);
            rv.setNestedScrollingEnabled(true);
            rv.setHasFixedSize(false);
            rv.setLayoutManager(new LinearLayoutManager(getContext()));
            rv.setAdapter(new RoundRobinAdapter(roundMatches, tournament,
                    match -> showScoreDialog(match)));
            binding.layoutRounds.addView(roundView);
        }
    }

    private void renderElimination(List<Match> matches) {
        binding.layoutRounds.removeAllViews();

        List<Match> sorted = new ArrayList<>(matches);
        sorted.sort((a, b) -> {
            if (a.getRound() != b.getRound()) return Integer.compare(a.getRound(), b.getRound());
            return Integer.compare(a.getId(), b.getId());
        });

        int maxRound = 0;
        for (Match m : sorted) if (m.getRound() > maxRound) maxRound = m.getRound();

        for (int round = 1; round <= maxRound; round++) {
            final List<Match> roundMatches = new ArrayList<>();
            for (Match m : sorted) if (m.getRound() == round) roundMatches.add(m);
            if (roundMatches.isEmpty()) continue;

            View roundView = LayoutInflater.from(getContext()).inflate(
                    R.layout.item_round_section, binding.layoutRounds, false);
            androidx.appcompat.widget.AppCompatTextView roundTitle =
                    roundView.findViewById(R.id.text_round_title);

            boolean isLast = (round == maxRound);
            boolean isFinal = isLast && roundMatches.size() == 1;
            boolean isSemiFinal = isLast && roundMatches.size() == 2;

            if (isFinal)           roundTitle.setText("Final");
            else if (isSemiFinal)  roundTitle.setText("Semi-Finals");
            else                   roundTitle.setText("Round " + round);

            androidx.recyclerview.widget.RecyclerView rv =
                    roundView.findViewById(R.id.recycler_round_matches);
            rv.setNestedScrollingEnabled(true);
            rv.setHasFixedSize(false);
            rv.setLayoutManager(new LinearLayoutManager(getContext()));
            rv.setAdapter(new MatchAdapter(roundMatches, match -> showScoreDialog(match)));
            binding.layoutRounds.addView(roundView);

            if (!isLast) continue;

            View btnView = LayoutInflater.from(getContext()).inflate(
                    R.layout.item_advance_button, binding.layoutRounds, false);
            com.google.android.material.button.MaterialButton btn =
                    btnView.findViewById(R.id.btn_advance);

            if (isFinal) {
                btn.setText("Tournament Complete");
                btn.setEnabled(false);
                btn.setAlpha(0.5f);
            } else {
                btn.setText("Advance to Round " + (round + 1));
                int finalRound = round;
                btn.setOnClickListener(v -> {
                    if (isAdvancing) return;
                    advanceRound(finalRound, btn);
                });
            }
            binding.layoutRounds.addView(btnView);
        }
    }

    private void renderSwiss(List<Match> matches) {
        binding.layoutRounds.removeAllViews();

        int maxRound = matches.stream().mapToInt(Match::getRound).filter(m -> m >= 0).max().orElse(0);
        int teamCount = tournament.getTournamentTeams() != null
                ? tournament.getTournamentTeams().size()
                : 0;

        int totalRounds = teamCount > 1
                ? (int) Math.ceil(Math.log(teamCount) / Math.log(2))
                : 1;

        for (int round = 1; round <= maxRound; round++) {
            final List<Match> roundMatches = new ArrayList<>();
            for (Match m : matches) if (m.getRound() == round) roundMatches.add(m);
            if (roundMatches.isEmpty()) continue;

            View roundView = LayoutInflater.from(getContext()).inflate(
                    R.layout.item_round_section, binding.layoutRounds, false);
            ((android.widget.TextView)
                    roundView.findViewById(R.id.text_round_title))
                    .setText("Swiss Round " + round);

            androidx.recyclerview.widget.RecyclerView rv =
                    roundView.findViewById(R.id.recycler_round_matches);
            rv.setNestedScrollingEnabled(true);
            rv.setHasFixedSize(false);
            rv.setLayoutManager(new LinearLayoutManager(getContext()));
            rv.setAdapter(new MatchAdapter(roundMatches, match -> showScoreDialog(match)));
            binding.layoutRounds.addView(roundView);

            boolean isLast = (round == maxRound);

            if(!isLast) continue;

            boolean currentRoundAllPlayed = true;

            for (Match m : roundMatches) {
                if (!m.isPlayed()) {
                    currentRoundAllPlayed = false;
                    break;
                }
            }

            boolean tournamentFinished =
                    round >= totalRounds && currentRoundAllPlayed;

            View btnView = LayoutInflater.from(getContext()).inflate(
                    R.layout.item_advance_button, binding.layoutRounds, false);
            com.google.android.material.button.MaterialButton btn =
                    btnView.findViewById(R.id.btn_advance);

            if (tournamentFinished) {
                btn.setText("Tournament Complete");
                btn.setEnabled(false);
                btn.setAlpha(0.5f);
            } else {
                btn.setText("Advance Swiss Round");
                btn.setOnClickListener(v -> {
                    if (isAdvancing) return;
                    advanceSwissRound(maxRound, btn);
                });
            }
            binding.layoutRounds.addView(btnView);
        }
    }


    private void showScoreDialog(Match match) {
        View dialogView = LayoutInflater.from(getContext())
                .inflate(R.layout.dialog_score, null);
        EditText editScoreA = dialogView.findViewById(R.id.edit_score_a);
        EditText editScoreB = dialogView.findViewById(R.id.edit_score_b);
        ((android.widget.TextView)
                dialogView.findViewById(R.id.text_team_a)).setText(match.getTeamAName());
        ((android.widget.TextView)
                dialogView.findViewById(R.id.text_team_b)).setText(match.getTeamBName());
        editScoreA.setText(String.valueOf(match.getTeamAScore()));
        editScoreB.setText(String.valueOf(match.getTeamBScore()));

        new AlertDialog.Builder(requireContext())
                .setTitle("Update Score")
                .setView(dialogView)
                .setPositiveButton("Save", (dialog, which) -> {
                    try {
                        int a = Integer.parseInt(editScoreA.getText().toString());
                        int b = Integer.parseInt(editScoreB.getText().toString());
                        updateScore(match, a, b);
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
                .enqueue(new Callback<>() {
                    @Override
                    public void onResponse(@NonNull Call<Match> call,
                                           @NonNull Response<Match> response) {
                        if (!isAdded()) return;
                        if (response.isSuccessful()) loadTournament();
                        else Toast.makeText(getContext(), "Failed to update score",
                                Toast.LENGTH_SHORT).show();
                    }

                    @Override
                    public void onFailure(@NonNull Call<Match> call, @NonNull Throwable t) {
                        if (!isAdded()) return;
                        Toast.makeText(getContext(), "Connection error", Toast.LENGTH_SHORT).show();
                    }
                });
    }

    private void advanceRound(int round, com.google.android.material.button.MaterialButton btn) {
        isAdvancing = true;
        btn.setEnabled(false);
        btn.setAlpha(0.5f);

        RetrofitClient.getInstance().getApi()
                .advanceRound(tournamentId, round)
                .enqueue(new Callback<>() {
                    @Override
                    public void onResponse(@NonNull Call<List<Match>> call,
                                           @NonNull Response<List<Match>> response) {
                        if (!isAdded()) return;
                        isAdvancing = false;
                        if (response.isSuccessful()) {
                            loadTournament();
                        } else {
                            btn.setEnabled(true);
                            btn.setAlpha(1f);
                            try {
                                String err = response.errorBody() != null
                                        ? response.errorBody().string() : "Cannot advance";
                                Toast.makeText(getContext(), err, Toast.LENGTH_LONG).show();
                            } catch (Exception e) {
                                Toast.makeText(getContext(), "Cannot advance round",
                                        Toast.LENGTH_SHORT).show();
                            }
                        }
                    }

                    @Override
                    public void onFailure(@NonNull Call<List<Match>> call, @NonNull Throwable t) {
                        if (!isAdded()) return;
                        isAdvancing = false;
                        btn.setEnabled(true);
                        btn.setAlpha(1f);
                        Toast.makeText(getContext(), "Connection error", Toast.LENGTH_SHORT).show();
                    }
                });
    }

    private void advanceSwissRound(int round, com.google.android.material.button.MaterialButton btn) {
        isAdvancing = true;
        btn.setEnabled(false);
        btn.setAlpha(0.5f);

        RetrofitClient.getInstance().getApi()
                .advanceSwissRound(tournamentId, round)
                .enqueue(new Callback<>() {
                    @Override
                    public void onResponse(@NonNull Call<List<Match>> call,
                                           @NonNull Response<List<Match>> response) {
                        if (!isAdded()) return;
                        isAdvancing = false;
                        if (response.isSuccessful()) loadTournament();
                        else {
                            btn.setEnabled(true);
                            btn.setAlpha(1f);
                            Toast.makeText(getContext(), "Cannot advance Swiss round",
                                    Toast.LENGTH_SHORT).show();
                        }
                    }

                    @Override
                    public void onFailure(@NonNull Call<List<Match>> call, @NonNull Throwable t) {
                        if (!isAdded()) return;
                        isAdvancing = false;
                        btn.setEnabled(true);
                        btn.setAlpha(1f);
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