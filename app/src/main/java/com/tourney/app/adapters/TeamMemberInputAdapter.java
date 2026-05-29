package com.tourney.app.adapters;

import android.view.LayoutInflater;
import android.view.ViewGroup;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.tourney.app.databinding.ItemMemberInputBinding;
import com.tourney.app.models.CreateTeamRequest;
import java.util.ArrayList;
import java.util.List;
import android.widget.ArrayAdapter;
import com.tourney.app.models.Country;

public class TeamMemberInputAdapter extends RecyclerView.Adapter<TeamMemberInputAdapter.ViewHolder> {
    private int count;
    private final List<String[]> data = new ArrayList<>();

    private List<Country> countries;

    public TeamMemberInputAdapter(int count, List<Country> countries) {
        this.count = count;
        this.countries = countries;
        for (int i = 0; i < count; i++) {
            data.add(new String[]{"", "", "", ""});
        }
    }

    public void setCount(int newCount) {
        while (data.size() < newCount) data.add(new String[]{"", "", "", ""});
        while (data.size() > newCount) data.remove(data.size() - 1);
        this.count = newCount;
        notifyDataSetChanged();
    }

    public void setCountries(List<Country> countries) {
        this.countries = countries;
        notifyDataSetChanged();
    }

    public List<CreateTeamRequest.TeamMemberRequest> getFilledMembers() {
        List<CreateTeamRequest.TeamMemberRequest> result = new ArrayList<>();
        for (String[] row : data) {
            if (!row[0].isEmpty() || !row[1].isEmpty()) {
                result.add(new CreateTeamRequest.TeamMemberRequest(row[0], row[1], row[2], row[3]));
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
        holder.bind(position, data.get(position), countries);
    }

    @Override
    public int getItemCount() { return count; }

    static class ViewHolder extends RecyclerView.ViewHolder {
        private final ItemMemberInputBinding binding;
        ViewHolder(ItemMemberInputBinding b) { super(b.getRoot()); binding = b; }
        void bind(int index, String[] row, List<Country> countries) {

            binding.textIndex.setText(String.valueOf(index + 1));

            binding.editName.setText(row[0]);
            binding.editSurname.setText(row[1]);
            binding.editNickname.setText(row[2]);

            Country selectedCountry = null;

            for (Country c : countries) {
                if (c.getCode().equalsIgnoreCase(row[3])) {
                    selectedCountry = c;
                    break;
                }
            }

            if (selectedCountry != null) {
                binding.countryDropdown.setText(
                        selectedCountry.getDisplayLabel(),
                        false
                );
            } else {
                binding.countryDropdown.setText("", false);
            }

            binding.editName.addTextChangedListener(new android.text.TextWatcher() {
                public void beforeTextChanged(CharSequence s, int st, int c, int a) {}
                public void onTextChanged(CharSequence s, int st, int b, int c) {
                    row[0] = s.toString();
                }
                public void afterTextChanged(android.text.Editable s) {}
            });

            binding.editSurname.addTextChangedListener(new android.text.TextWatcher() {
                public void beforeTextChanged(CharSequence s, int st, int c, int a) {}
                public void onTextChanged(CharSequence s, int st, int b, int c) {
                    row[1] = s.toString();
                }
                public void afterTextChanged(android.text.Editable s) {}
            });

            binding.editNickname.addTextChangedListener(new android.text.TextWatcher() {
                public void beforeTextChanged(CharSequence s, int st, int c, int a) {}
                public void onTextChanged(CharSequence s, int st, int b, int c) {
                    row[2] = s.toString();
                }
                public void afterTextChanged(android.text.Editable s) {}
            });

            ArrayAdapter<Country> adapter = new ArrayAdapter<>(
                    binding.getRoot().getContext(),
                    android.R.layout.simple_dropdown_item_1line,
                    countries
            );

            binding.countryDropdown.setAdapter(adapter);

            binding.countryDropdown.setOnItemClickListener((parent, view, position, id) -> {

                Country selected = adapter.getItem(position);

                if (selected != null) {

                    row[3] = selected.getCode();

                    binding.countryDropdown.setText(
                            selected.getDisplayLabel(),
                            false
                    );
                }
            });
        }
    }
}
