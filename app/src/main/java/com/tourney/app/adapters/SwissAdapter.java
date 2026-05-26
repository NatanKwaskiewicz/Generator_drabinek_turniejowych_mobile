package com.tourney.app.adapters;

import android.view.LayoutInflater;
import android.view.ViewGroup;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.tourney.app.databinding.ItemMatchBinding;
import com.tourney.app.models.Match;
import java.util.List;

public class SwissAdapter extends RecyclerView.Adapter<SwissAdapter.ViewHolder> {
    public interface OnMatchClick { void onClick(Match m); }
    private final List<Match> matches;
    private final OnMatchClick clickListener;

    public SwissAdapter(List<Match> matches, OnMatchClick click) {
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
            binding.textTeamA.setAlpha(m.isPlayed() && m.getTeamAScore() < m.getTeamBScore() ? 0.4f : 1f);
            binding.textTeamB.setAlpha(m.isPlayed() && m.getTeamBScore() < m.getTeamAScore() ? 0.4f : 1f);
            binding.getRoot().setOnClickListener(v -> clickListener.onClick(m));
        }
    }
}
