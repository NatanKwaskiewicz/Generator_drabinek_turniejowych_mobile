package com.tourney.app.adapters;

import android.view.LayoutInflater;
import android.view.ViewGroup;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.tourney.app.databinding.ItemMemberInputBinding;
import com.tourney.app.models.CreateTeamRequest;
import java.util.ArrayList;
import java.util.List;

public class TeamMemberInputAdapter extends RecyclerView.Adapter<TeamMemberInputAdapter.ViewHolder> {
    private int count;
    private final List<String[]> data = new ArrayList<>();

    public TeamMemberInputAdapter(int count) {
        this.count = count;
        for (int i = 0; i < count; i++) data.add(new String[]{"", "", ""});
    }

    public void setCount(int newCount) {
        while (data.size() < newCount) data.add(new String[]{"", "", ""});
        while (data.size() > newCount) data.remove(data.size() - 1);
        this.count = newCount;
        notifyDataSetChanged();
    }

    public List<CreateTeamRequest.TeamMemberRequest> getFilledMembers() {
        List<CreateTeamRequest.TeamMemberRequest> result = new ArrayList<>();
        for (String[] row : data) {
            if (!row[0].isEmpty() || !row[1].isEmpty()) {
                result.add(new CreateTeamRequest.TeamMemberRequest(row[0], row[1], row[2]));
            }
        }
        return result;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        ItemMemberInputBinding binding = ItemMemberInputBinding.inflate(
            LayoutInflater.from(parent.getContext()), parent, false);
        return new ViewHolder(binding);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        holder.bind(position, data.get(position));
    }

    @Override
    public int getItemCount() { return count; }

    static class ViewHolder extends RecyclerView.ViewHolder {
        private final ItemMemberInputBinding binding;
        ViewHolder(ItemMemberInputBinding b) { super(b.getRoot()); binding = b; }
        void bind(int index, String[] row) {
            binding.textIndex.setText(String.valueOf(index + 1));
            binding.editName.setText(row[0]);
            binding.editSurname.setText(row[1]);
            binding.editNickname.setText(row[2]);
            binding.editName.addTextChangedListener(new android.text.TextWatcher() {
                public void beforeTextChanged(CharSequence s, int st, int c, int a) {}
                public void onTextChanged(CharSequence s, int st, int b, int c) { row[0] = s.toString(); }
                public void afterTextChanged(android.text.Editable s) {}
            });
            binding.editSurname.addTextChangedListener(new android.text.TextWatcher() {
                public void beforeTextChanged(CharSequence s, int st, int c, int a) {}
                public void onTextChanged(CharSequence s, int st, int b, int c) { row[1] = s.toString(); }
                public void afterTextChanged(android.text.Editable s) {}
            });
            binding.editNickname.addTextChangedListener(new android.text.TextWatcher() {
                public void beforeTextChanged(CharSequence s, int st, int c, int a) {}
                public void onTextChanged(CharSequence s, int st, int b, int c) { row[2] = s.toString(); }
                public void afterTextChanged(android.text.Editable s) {}
            });
        }
    }
}
