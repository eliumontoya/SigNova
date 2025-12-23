package info.eliumontoyasadec.signova.ui.dashboard;
// GenerationAdapter.java
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.text.DateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import info.eliumontoyasadec.signova.R;
import info.eliumontoyasadec.signova.bd.GenerationEntity;

public class GenerationAdapter extends RecyclerView.Adapter<GenerationAdapter.VH> {

    public interface OnItemClick {
        void onClick(GenerationEntity item);
    }

    private final List<GenerationEntity> data = new ArrayList<>();
    private final OnItemClick onItemClick;

    public GenerationAdapter(OnItemClick onItemClick) {
        this.onItemClick = onItemClick;
    }

    public void setData(List<GenerationEntity> items) {
        data.clear();
        if (items != null) data.addAll(items);
        notifyDataSetChanged();
    }

    static class VH extends RecyclerView.ViewHolder {
        TextView tvPrompt, tvDate;

        VH(@NonNull View itemView) {
            super(itemView);
            tvPrompt = itemView.findViewById(R.id.tvPrompt);
            tvDate = itemView.findViewById(R.id.tvDate);
        }
    }

    @NonNull
    @Override
    public VH onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_generation, parent, false);
        return new VH(v);
    }

    @Override
    public void onBindViewHolder(@NonNull VH holder, int position) {
        GenerationEntity item = data.get(position);

        holder.tvPrompt.setText(item.prompt);

        Date d = new Date(item.createdAtMillis);
        holder.tvDate.setText(DateFormat.getDateTimeInstance().format(d));

        holder.itemView.setOnClickListener(v -> onItemClick.onClick(item));
    }

    @Override
    public int getItemCount() {
        return data.size();
    }
}