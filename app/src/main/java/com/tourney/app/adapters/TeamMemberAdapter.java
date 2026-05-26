package com.tourney.app.adapters;

import android.view.LayoutInflater;
import android.view.ViewGroup;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.tourney.app.databinding.ItemTeamMemberBinding;
import com.tourney.app.models.Team;
import java.util.ArrayList;
import java.util.List;

public class TeamMemberAdapter extends RecyclerView.Adapter<TeamMemberAdapter.ViewHolder> {
    private final List<Team.TeamMember> members;

    public TeamMemberAdapter(List<Team.TeamMember> members) {
        this.members = members != null ? members : new ArrayList<>();
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        ItemTeamMemberBinding binding = ItemTeamMemberBinding.inflate(
            LayoutInflater.from(parent.getContext()), parent, false);
        return new ViewHolder(binding);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        holder.bind(members.get(position), position + 1);
    }

    @Override
    public int getItemCount() { return members.size(); }

    static class ViewHolder extends RecyclerView.ViewHolder {
        private final ItemTeamMemberBinding binding;
        ViewHolder(ItemTeamMemberBinding b) { super(b.getRoot()); binding = b; }
        void bind(Team.TeamMember m, int index) {
            binding.textIndex.setText(String.valueOf(index));
            binding.textMemberName.setText(m.getName() + " " + m.getSurname());
            if (m.getNickname() != null && !m.getNickname().isEmpty()) {
                binding.textNickname.setVisibility(android.view.View.VISIBLE);
                binding.textNickname.setText("\"" + m.getNickname() + "\"");
            } else {
                binding.textNickname.setVisibility(android.view.View.GONE);
            }
        }
    }
}
