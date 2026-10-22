package com.welsh.smartpantry;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.color.MaterialColors;
import com.welsh.smartpantry.data.PantryItem;

import java.util.ArrayList;
import java.util.List;

/**
 * Custom RecyclerView adapter for the pantry list.
 * The RecyclerView only creates enough row views to fill the screen
 * (onCreateViewHolder) and then reuses them, filling each one with a
 * different PantryItem as the user scrolls (onBindViewHolder).
 */
public class PantryAdapter extends RecyclerView.Adapter<PantryAdapter.PantryViewHolder> {

    /** Items expiring within this many days are highlighted. */
    public static final int EXPIRING_SOON_DAYS = 3;

    /** Lets the Activity decide what happens when a row is tapped. */
    public interface OnItemClickListener {
        void onItemClick(PantryItem item);
    }

    private final List<PantryItem> items = new ArrayList<>();
    private final OnItemClickListener listener;
    private boolean highlightExpiring = true;

    public PantryAdapter(OnItemClickListener listener) {
        this.listener = listener;
    }

    /** Called with fresh data from the database whenever it changes. */
    public void setItems(List<PantryItem> newItems) {
        items.clear();
        if (newItems != null) {
            items.addAll(newItems);
        }
        notifyDataSetChanged();
    }

    /** Turned on or off from the Settings screen. */
    public void setHighlightExpiring(boolean highlight) {
        highlightExpiring = highlight;
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public PantryViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_pantry, parent, false);
        return new PantryViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull PantryViewHolder holder, int position) {
        holder.bind(items.get(position));
    }

    @Override
    public int getItemCount() {
        return items.size();
    }

    /** Holds references to one row's views so findViewById runs only once per row. */
    class PantryViewHolder extends RecyclerView.ViewHolder {
        private final TextView textName;
        private final TextView textQuantity;
        private final TextView textExpiry;
        private final int normalColor;
        private final int warningColor;

        PantryViewHolder(@NonNull View itemView) {
            super(itemView);
            textName = itemView.findViewById(R.id.textName);
            textQuantity = itemView.findViewById(R.id.textQuantity);
            textExpiry = itemView.findViewById(R.id.textExpiry);
            normalColor = textExpiry.getCurrentTextColor();
            warningColor = MaterialColors.getColor(itemView,
                    com.google.android.material.R.attr.colorError);
        }

        void bind(PantryItem item) {
            textName.setText(item.getName());
            textQuantity.setText(AppUtils.formatQuantity(item.getQuantity()) + " " + item.getUnit());

            String expiry = item.getExpiryDate();
            if (expiry == null) {
                textExpiry.setText(R.string.no_expiry);
                textExpiry.setTextColor(normalColor);
            } else {
                long days = AppUtils.daysUntil(expiry);
                int label = days < 0 ? R.string.expired_on : R.string.expires_on;
                textExpiry.setText(itemView.getContext().getString(label, expiry));
                boolean warn = highlightExpiring && days <= EXPIRING_SOON_DAYS;
                textExpiry.setTextColor(warn ? warningColor : normalColor);
            }

            itemView.setOnClickListener(v -> listener.onItemClick(item));
        }
    }
}
