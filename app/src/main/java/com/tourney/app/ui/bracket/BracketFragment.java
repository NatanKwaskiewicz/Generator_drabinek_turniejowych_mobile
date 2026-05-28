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
import com.tourney.app.utils.RoundRobinScheduler;
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
            if ("Round Robin".equals(formatName)) {
                generateRoundRobinLocally();
            } else if ("Swiss".equals(formatName)) {
                callGenerateApi(
                        RetrofitClient.getInstance().getApi().generateSwissMatches(tournamentId));
            } else {
                callGenerateApi(
                        RetrofitClient.getInstance().getApi().generateMatches(tournamentId));
            }
        });
    }

    /**
     * Generuje Round Robin lokalnie (poprawny algorytm circle/rotation),
     * następnie wysyła gotowe mecze do backendu endpointem bulk-save.
     * Double leg = zawsze true dla RR.
     */
    private void generateRoundRobinLocally() {
        if (tournament.getTournamentTeams() == null
                || tournament.getTournamentTeams().size() < 2) {
            Toast.makeText(getContext(), "Need at least 2 teams", Toast.LENGTH_SHORT).show();
            return;
        }

        List<Integer> teamIds = new ArrayList<>();
        for (Tournament.TournamentTeam tt : tournament.getTournamentTeams()) {
            teamIds.add(tt.getTeamId());
        }

        List<RoundRobinScheduler.ScheduledMatch> schedule =
                RoundRobinScheduler.generate(teamIds, true);

        String validation = RoundRobinScheduler.validate(schedule, teamIds, true);
        if (!validation.isEmpty()) {
            Toast.makeText(getContext(), "Schedule error: " + validation, Toast.LENGTH_LONG).show();
            return;
        }

        List<com.tourney.app.models.CreateMatchRequest> requests = new ArrayList<>();
        for (RoundRobinScheduler.ScheduledMatch sm : schedule) {
            requests.add(new com.tourney.app.models.CreateMatchRequest(
                    tournamentId, sm.teamAId, sm.teamBId, sm.round));
        }

        binding.btnGenerateMatches.setEnabled(false);
        RetrofitClient.getInstance().getApi()
                .createMatches(requests)
                .enqueue(new Callback<>() {
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
                                Toast.makeText(getContext(), "Failed to save matches", Toast.LENGTH_SHORT).show();
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
    }

    private void callGenerateApi(Call<List<Match>> call) {
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
    }

    private void renderRoundRobin(List<Match> matches) {
        binding.layoutRounds.removeAllViews();

        int maxRound = 0;
        for (Match m : matches) if (m.getRound() > maxRound) maxRound = m.getRound();

        int teamCount = tournament.getTournamentTeams() != null
                ? tournament.getTournamentTeams().size() : 0;

        int n = teamCount % 2 == 0 ? teamCount : teamCount + 1;
        int leg1Rounds = n - 1;

        for (int round = 1; round <= maxRound; round++) {
            final List<Match> roundMatches = new ArrayList<>();
            for (Match m : matches) if (m.getRound() == round) roundMatches.add(m);
            if (roundMatches.isEmpty()) continue;

            View roundView = LayoutInflater.from(getContext()).inflate(
                    R.layout.item_round_section, binding.layoutRounds, false);
            androidx.appcompat.widget.AppCompatTextView roundTitle =
                    roundView.findViewById(R.id.text_round_title);

            boolean isSecondLeg = (round > leg1Rounds);
            int matchday = isSecondLeg ? (round - leg1Rounds) : round;
            String legSuffix = isSecondLeg ? " (2nd Leg)" : "";
            roundTitle.setText("Matchday " + matchday + legSuffix);

            androidx.recyclerview.widget.RecyclerView rv =
                    roundView.findViewById(R.id.recycler_round_matches);
            rv.setLayoutManager(new LinearLayoutManager(getContext()));
            rv.setAdapter(new RoundRobinAdapter(roundMatches, tournament,
                    match -> showScoreDialog(match)));
            binding.layoutRounds.addView(roundView);
        }
    }

    private void renderElimination(List<Match> matches) {
        binding.layoutRounds.removeAllViews();

        int maxRound = 0;
        for (Match m : matches) if (m.getRound() > maxRound) maxRound = m.getRound();

        for (int round = 1; round <= maxRound; round++) {
            final List<Match> roundMatches = new ArrayList<>();
            for (Match m : matches) if (m.getRound() == round) roundMatches.add(m);
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
                btn.setOnClickListener(v -> advanceRound(finalRound));
            }
            binding.layoutRounds.addView(btnView);
        }
    }

    private void renderSwiss(List<Match> matches) {
        binding.layoutRounds.removeAllViews();

        int maxRound = matches.stream().mapToInt(Match::getRound).filter(m -> m >= 0).max().orElse(0);

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
            rv.setLayoutManager(new LinearLayoutManager(getContext()));
            rv.setAdapter(new MatchAdapter(roundMatches, match -> showScoreDialog(match)));
            binding.layoutRounds.addView(roundView);
        }

        View btnView = LayoutInflater.from(getContext()).inflate(
                R.layout.item_advance_button, binding.layoutRounds, false);
        com.google.android.material.button.MaterialButton btn =
                btnView.findViewById(R.id.btn_advance);
        btn.setText("Advance Swiss Round");
        btn.setOnClickListener(v -> advanceSwissRound(maxRound));
        binding.layoutRounds.addView(btnView);
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

    private void advanceRound(int round) {
        RetrofitClient.getInstance().getApi()
                .advanceRound(tournamentId, round)
                .enqueue(new Callback<>() {
                    @Override
                    public void onResponse(@NonNull Call<List<Match>> call,
                                           @NonNull Response<List<Match>> response) {
                        if (!isAdded()) return;
                        if (response.isSuccessful()) {
                            loadTournament();
                        } else {
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
                        Toast.makeText(getContext(), "Connection error", Toast.LENGTH_SHORT).show();
                    }
                });
    }

    private void advanceSwissRound(int round) {
        RetrofitClient.getInstance().getApi()
                .advanceSwissRound(tournamentId, round)
                .enqueue(new Callback<>() {
                    @Override
                    public void onResponse(@NonNull Call<List<Match>> call,
                                           @NonNull Response<List<Match>> response) {
                        if (!isAdded()) return;
                        if (response.isSuccessful()) loadTournament();
                        else Toast.makeText(getContext(), "Cannot advance Swiss round",
                                Toast.LENGTH_SHORT).show();
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
