package com.tourney.app.adapters;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.tourney.app.databinding.ItemTournamentBinding;
import com.tourney.app.models.Tournament;
import java.util.ArrayList;
import java.util.List;

public class TournamentAdapter extends RecyclerView.Adapter<TournamentAdapter.ViewHolder> {
    public interface OnTournamentClick { void onClick(Tournament t); }
    public interface OnTournamentDelete { void onDelete(Tournament t); }

    private List<Tournament> tournaments = new ArrayList<>();
    private final OnTournamentClick clickListener;
    private final OnTournamentDelete deleteListener;

    public TournamentAdapter(OnTournamentClick click, OnTournamentDelete delete) {
        this.clickListener = click;
        this.deleteListener = delete;
    }

    public void setTournaments(List<Tournament> list) {
        this.tournaments = list;
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        ItemTournamentBinding binding = ItemTournamentBinding.inflate(
            LayoutInflater.from(parent.getContext()), parent, false);
        return new ViewHolder(binding);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        holder.bind(tournaments.get(position));
    }

    @Override
    public int getItemCount() { return tournaments.size(); }

    class ViewHolder extends RecyclerView.ViewHolder {
        private final ItemTournamentBinding binding;
        ViewHolder(ItemTournamentBinding b) {
            super(b.getRoot());
            binding = b;
        }
        void bind(Tournament t) {
            binding.textTournamentName.setText(t.getName());
            binding.textFormat.setText(t.getFormat() != null ? t.getFormat().getName() : "");
            int teamCount = t.getTournamentTeams() != null ? t.getTournamentTeams().size() : 0;
            binding.textTeamCount.setText(teamCount + " teams");
            binding.getRoot().setOnClickListener(v -> clickListener.onClick(t));
            binding.btnDelete.setOnClickListener(v -> deleteListener.onDelete(t));
        }
    }
}
