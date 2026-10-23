package com.Welsh.smartpantry;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.Welsh.smartpantry.data.Recipe;

import java.util.ArrayList;
import java.util.List;

/**
 * Custom adapter for the Suggested Recipes list. It uses TWO view types:
 * section headers ("Ready to cook", "Almost there") and recipe cards.
 * getItemViewType tells the RecyclerView which layout each position needs.
 */
public class RecipeAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

    private static final int TYPE_HEADER = 0;
    private static final int TYPE_RECIPE = 1;

    public interface OnRecipeClickListener {
        void onRecipeClick(Recipe recipe);
    }

    /** One position in the list: either a header or a match result. */
    private static final class Row {
        final String header;
        final IngredientMatcher.MatchResult result;

        Row(String header, IngredientMatcher.MatchResult result) {
            this.header = header;
            this.result = result;
        }
    }

    private final List<Row> rows = new ArrayList<>();
    private final OnRecipeClickListener listener;

    public RecipeAdapter(OnRecipeClickListener listener) {
        this.listener = listener;
    }

    public void clear() {
        rows.clear();
    }

    public void addHeader(String title) {
        rows.add(new Row(title, null));
    }

    public void addResults(List<IngredientMatcher.MatchResult> results) {
        for (IngredientMatcher.MatchResult result : results) {
            rows.add(new Row(null, result));
        }
    }

    public void refresh() {
        notifyDataSetChanged();
    }

    @Override
    public int getItemViewType(int position) {
        return rows.get(position).header != null ? TYPE_HEADER : TYPE_RECIPE;
    }

    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        LayoutInflater inflater = LayoutInflater.from(parent.getContext());
        if (viewType == TYPE_HEADER) {
            return new HeaderHolder(inflater.inflate(R.layout.item_section_header, parent, false));
        }
        return new RecipeHolder(inflater.inflate(R.layout.item_recipe, parent, false));
    }

    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position) {
        Row row = rows.get(position);
        if (holder instanceof HeaderHolder) {
            ((HeaderHolder) holder).textHeader.setText(row.header);
        } else {
            ((RecipeHolder) holder).bind(row.result);
        }
    }

    @Override
    public int getItemCount() {
        return rows.size();
    }

    static class HeaderHolder extends RecyclerView.ViewHolder {
        final TextView textHeader;

        HeaderHolder(@NonNull View itemView) {
            super(itemView);
            textHeader = itemView.findViewById(R.id.textHeader);
        }
    }

    class RecipeHolder extends RecyclerView.ViewHolder {
        private final TextView textName;
        private final TextView textInfo;
        private final TextView textMissing;

        RecipeHolder(@NonNull View itemView) {
            super(itemView);
            textName = itemView.findViewById(R.id.textRecipeName);
            textInfo = itemView.findViewById(R.id.textRecipeInfo);
            textMissing = itemView.findViewById(R.id.textMissing);
        }

        void bind(IngredientMatcher.MatchResult result) {
            Recipe recipe = result.recipe.recipe;
            textName.setText(recipe.getName());
            textInfo.setText(itemView.getContext().getString(R.string.recipe_info,
                    recipe.getPrepMinutes(), result.recipe.ingredients.size()));
            if (result.canCook()) {
                textMissing.setVisibility(View.GONE);
            } else {
                textMissing.setVisibility(View.VISIBLE);
                textMissing.setText(itemView.getContext().getString(R.string.missing_item,
                        result.missing.get(0)));
            }
            itemView.setOnClickListener(v -> listener.onRecipeClick(recipe));
        }
    }
}
