package com.tourney.app.adapters;

import android.view.LayoutInflater;
import android.view.ViewGroup;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.tourney.app.databinding.ItemMatchBinding;
import com.tourney.app.models.Match;
import com.tourney.app.models.Tournament;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class RoundRobinAdapter extends RecyclerView.Adapter<RoundRobinAdapter.ViewHolder> {
    public interface OnMatchClick { void onClick(Match m); }
    private final List<Match> matches;
    private final OnMatchClick clickListener;

    public RoundRobinAdapter(List<Match> matches, Tournament tournament, OnMatchClick click) {
        this.matches = matches;
        this.clickListener = click;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        ItemMatchBinding binding = ItemMatchBinding.inflate(
            LayoutInflater.from(parent.getContext()), parent, false);
        return new ViewHolder(binding);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        holder.bind(matches.get(position));
    }

    @Override
    public int getItemCount() { return matches.size(); }

    class ViewHolder extends RecyclerView.ViewHolder {
        private final ItemMatchBinding binding;
        ViewHolder(ItemMatchBinding b) { super(b.getRoot()); binding = b; }
        void bind(Match m) {
            binding.textTeamA.setText(m.getTeamAName());
            binding.textTeamB.setText(m.getTeamBName());
            binding.textScoreA.setText(String.valueOf(m.getTeamAScore()));
            binding.textScoreB.setText(String.valueOf(m.getTeamBScore()));
            if (m.isPlayed()) {
                boolean aWins = m.getTeamAScore() > m.getTeamBScore();
                binding.textTeamA.setAlpha(aWins ? 1f : 0.4f);
                binding.textTeamB.setAlpha(aWins ? 0.4f : 1f);
            } else {
                binding.textTeamA.setAlpha(1f);
                binding.textTeamB.setAlpha(1f);
            }
            binding.getRoot().setOnClickListener(v -> clickListener.onClick(m));
        }
    }
}
