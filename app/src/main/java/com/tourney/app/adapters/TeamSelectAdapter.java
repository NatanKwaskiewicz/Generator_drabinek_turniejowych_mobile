package com.tourney.app.adapters;

import android.view.LayoutInflater;
import android.view.ViewGroup;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.tourney.app.databinding.ItemTeamSelectBinding;
import com.tourney.app.models.Team;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class TeamSelectAdapter extends RecyclerView.Adapter<TeamSelectAdapter.ViewHolder> {
    private List<Team> teams = new ArrayList<>();
    private final Set<Integer> selectedIds = new HashSet<>();

    public void setTeams(List<Team> list) { this.teams = list; notifyDataSetChanged(); }

    public List<Integer> getSelectedTeamIds() { return new ArrayList<>(selectedIds); }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        ItemTeamSelectBinding binding = ItemTeamSelectBinding.inflate(
            LayoutInflater.from(parent.getContext()), parent, false);
        return new ViewHolder(binding);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        holder.bind(teams.get(position));
    }

    @Override
    public int getItemCount() { return teams.size(); }

    class ViewHolder extends RecyclerView.ViewHolder {
        private final ItemTeamSelectBinding binding;
        ViewHolder(ItemTeamSelectBinding b) { super(b.getRoot()); binding = b; }
        void bind(Team t) {
            binding.textTeamName.setText(t.getName());
            binding.checkTeam.setChecked(selectedIds.contains(t.getId()));
            binding.getRoot().setOnClickListener(v -> {
                if (selectedIds.contains(t.getId())) selectedIds.remove(t.getId());
                else selectedIds.add(t.getId());
                binding.checkTeam.setChecked(selectedIds.contains(t.getId()));
            });
            binding.checkTeam.setOnClickListener(v -> {
                if (selectedIds.contains(t.getId())) selectedIds.remove(t.getId());
                else selectedIds.add(t.getId());
            });
        }
    }
}
