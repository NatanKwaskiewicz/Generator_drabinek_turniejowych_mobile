package com.tourney.app.adapters;

import android.view.LayoutInflater;
import android.view.ViewGroup;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.tourney.app.databinding.ItemTeamBinding;
import com.tourney.app.models.Team;
import java.util.ArrayList;
import java.util.List;

public class TeamAdapter extends RecyclerView.Adapter<TeamAdapter.ViewHolder> {
    public interface OnTeamClick { void onClick(Team t); }
    private List<Team> teams = new ArrayList<>();
    private final OnTeamClick clickListener;

    public TeamAdapter(OnTeamClick click) { this.clickListener = click; }

    public void setTeams(List<Team> list) { this.teams = list; notifyDataSetChanged(); }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        ItemTeamBinding binding = ItemTeamBinding.inflate(
            LayoutInflater.from(parent.getContext()), parent, false);
        return new ViewHolder(binding);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) { holder.bind(teams.get(position)); }

    @Override
    public int getItemCount() { return teams.size(); }

    class ViewHolder extends RecyclerView.ViewHolder {
        private final ItemTeamBinding binding;
        ViewHolder(ItemTeamBinding b) { super(b.getRoot()); binding = b; }
        void bind(Team t) {
            binding.textTeamName.setText(t.getName());
            binding.getRoot().setOnClickListener(v -> clickListener.onClick(t));
        }
    }
}
