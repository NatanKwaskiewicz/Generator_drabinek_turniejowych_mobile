package com.tourney.app.adapters;

import android.view.LayoutInflater;
import android.view.ViewGroup;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.tourney.app.databinding.ItemFormatBinding;
import com.tourney.app.models.Format;
import java.util.ArrayList;
import java.util.List;

public class FormatAdapter extends RecyclerView.Adapter<FormatAdapter.ViewHolder> {
    public interface OnFormatClick { void onClick(Format f); }
    private List<Format> formats = new ArrayList<>();
    private int selectedId = -1;
    private final OnFormatClick clickListener;

    public FormatAdapter(OnFormatClick click) { this.clickListener = click; }

    public void setFormats(List<Format> list) { this.formats = list; notifyDataSetChanged(); }

    public void setSelectedId(int id) { this.selectedId = id; notifyDataSetChanged(); }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        ItemFormatBinding binding = ItemFormatBinding.inflate(
            LayoutInflater.from(parent.getContext()), parent, false);
        return new ViewHolder(binding);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        holder.bind(formats.get(position), formats.get(position).getId() == selectedId);
    }

    @Override
    public int getItemCount() { return formats.size(); }

    class ViewHolder extends RecyclerView.ViewHolder {
        private final ItemFormatBinding binding;
        ViewHolder(ItemFormatBinding b) { super(b.getRoot()); binding = b; }
        void bind(Format f, boolean selected) {
            binding.textFormatName.setText(f.getName());
            binding.getRoot().setSelected(selected);
            binding.getRoot().setOnClickListener(v -> {
                selectedId = f.getId();
                notifyDataSetChanged();
                clickListener.onClick(f);
            });
        }
    }
}
