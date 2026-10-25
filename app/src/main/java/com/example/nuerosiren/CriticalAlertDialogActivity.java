package com.example.nuerosiren;

import android.content.res.ColorStateList;
import android.graphics.Color;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.google.android.material.button.MaterialButton;
import java.util.ArrayList;
import java.util.List;

// 💡 THE FIX: Explicitly forces Java to see your dialog_critical_alert layout template
import com.example.nuerosiren.R;

public class CriticalAlertDialogActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_critical_alert_dialog);

        if (getSupportActionBar() != null) {
            getSupportActionBar().hide();
        }

        // Connect and build the dynamic emergency action button list
        RecyclerView rvAlertActions = findViewById(R.id.rvAlertActions);
        rvAlertActions.setLayoutManager(new LinearLayoutManager(this));

        List<String> actions = new ArrayList<>();
        actions.add("Call 911 Immediately");
        actions.add("Confirm Emergency");
        actions.add("Start Self Evaluation");

        AlertActionAdapter adapter = new AlertActionAdapter(actions);
        rvAlertActions.setAdapter(adapter);

        // Cancel Alert dismiss hook
        findViewById(R.id.btnCancelAlert).setOnClickListener(v -> finish());
    }

    // ==========================================================
    // RECYCLERVIEW ADAPTER (Handles row styles dynamically)
    // ==========================================================
    private class AlertActionAdapter extends RecyclerView.Adapter<AlertActionAdapter.ActionViewHolder> {
        private final List<String> items;

        public AlertActionAdapter(List<String> items) {
            this.items = items;
        }

        @NonNull
        @Override
        public ActionViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            // Inflates the custom MaterialButton item template
            MaterialButton btn = (MaterialButton) LayoutInflater.from(parent.getContext())
                    .inflate(R.layout.item_alert_action_button, parent, false);
            return new ActionViewHolder(btn);
        }

        @Override
        public void onBindViewHolder(@NonNull ActionViewHolder holder, int position) {
            holder.button.setText(items.get(position));

            // Apply different design profiles depending on the button position
            if (position == 0) {
                // 🔴 1. Call 911 (Solid Crimson Red)
                holder.button.setBackgroundTintList(ColorStateList.valueOf(Color.parseColor("#A62626")));
                holder.button.setTextColor(Color.WHITE);
                holder.button.setIconTint(ColorStateList.valueOf(Color.WHITE));
                holder.button.setIconResource(R.drawable.play);
            } else if (position == 1) {
                // 🟢 2. Confirm Emergency (Solid Dark Teal)
                holder.button.setBackgroundTintList(ColorStateList.valueOf(Color.parseColor("#266153")));
                holder.button.setTextColor(Color.WHITE);
                holder.button.setIconTint(ColorStateList.valueOf(Color.WHITE));
                holder.button.setIconResource(R.drawable.check);
            } else if (position == 2) {
                // ⚪ 3. Self Evaluation (Outlined Hollow Style)
                holder.button.setStrokeColor(ColorStateList.valueOf(Color.parseColor("#266153")));
                holder.button.setStrokeWidth(4); // 2dp border layout translation
                holder.button.setBackgroundTintList(ColorStateList.valueOf(Color.TRANSPARENT));
                holder.button.setTextColor(Color.parseColor("#266153"));
                holder.button.setIconTint(ColorStateList.valueOf(Color.parseColor("#266153")));
                holder.button.setIconResource(R.drawable.edit_text_fill_box);
            }
        }

        @Override
        public int getItemCount() {
            return items.size();
        }

        class ActionViewHolder extends RecyclerView.ViewHolder {
            MaterialButton button;
            public ActionViewHolder(@NonNull MaterialButton itemView) {
                super(itemView);
                button = itemView;
            }
        }
    }
}