package com.example.nuerosiren;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.google.android.material.card.MaterialCardView;
import com.google.android.material.slider.Slider;
import com.google.android.material.switchmaterial.SwitchMaterial;
import java.util.ArrayList;
import java.util.List;

public class EditEmergencyActivity extends AppCompatActivity {

    private RecyclerView rvEmergencyActions;
    private RecyclerView rvPermissionsSharing;
    private TextView tvDelayBadge;
    private Slider delaySlider;
    private List<ActionModel> actionsList = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_edit_emergency);

        if (getSupportActionBar() != null) {
            getSupportActionBar().hide();
        }

        SharedPreferences prefs = getSharedPreferences("EmergencyPrefs", Context.MODE_PRIVATE);
        String currentAction = prefs.getString("primary_action", "Automated Voice Call");
        int currentDelay = prefs.getInt("alert_delay", 30);
        

        // Slider logic
        delaySlider = findViewById(R.id.delaySlider);
        tvDelayBadge = findViewById(R.id.tvDelayBadge);
        if (delaySlider != null && tvDelayBadge != null) {
            delaySlider.setValue(Math.min(Math.max((float) currentDelay, 5f), 60f));
            tvDelayBadge.setText(currentDelay + "s");
            delaySlider.addOnChangeListener((slider, value, fromUser) -> {
                tvDelayBadge.setText((int) value + "s");
            });
        }

        // Emergency Actions List
        rvEmergencyActions = findViewById(R.id.rvEmergencyActions);
        setupEmergencyActions(currentAction);

        // Permissions & Sharing List
        rvPermissionsSharing = findViewById(R.id.rvPermissionsSharing);
        setupPermissionsSharing();

        // Footer Action - Save Changes
        findViewById(R.id.btnSaveChanges).setOnClickListener(v -> {
            String selectedAction = "Automated Voice Call";
            for (ActionModel action : actionsList) {
                if (action.selected) {
                    selectedAction = action.title;
                    break;
                }
            }

            int delayVal = delaySlider != null ? (int) delaySlider.getValue() : 30;

            SharedPreferences.Editor editor = getSharedPreferences("EmergencyPrefs", Context.MODE_PRIVATE).edit();
            editor.putString("primary_action", selectedAction);
            editor.putInt("alert_delay", delayVal);
            editor.apply();

            Intent resultIntent = new Intent();
            resultIntent.putExtra("primary_action", selectedAction);
            resultIntent.putExtra("alert_delay", delayVal);
            setResult(RESULT_OK, resultIntent);
            finish();
        });
    }

    private void setupEmergencyActions(String currentSelectedTitle) {
        if (rvEmergencyActions != null) {
            actionsList.clear();
            boolean isVoiceCall = !currentSelectedTitle.equalsIgnoreCase("Silent Alert");
            actionsList.add(new ActionModel("Automated Voice Call", "AI-generated call to emergency services", isVoiceCall));
            actionsList.add(new ActionModel("Silent Alert", "SMS message sent to contacts without sound", !isVoiceCall));

            rvEmergencyActions.setLayoutManager(new LinearLayoutManager(this));
            rvEmergencyActions.setAdapter(new ActionAdapter(actionsList));
        }
    }

    private void setupPermissionsSharing() {
        if (rvPermissionsSharing != null) {
            List<PermissionModel> permissions = new ArrayList<>();
            permissions.add(new PermissionModel("GPS Location Sharing", "Send live location to responders", R.drawable.right_arrow_, true));
            permissions.add(new PermissionModel("Health Data sharing", "Send vitals from connected devices", R.drawable.profile, true));
            permissions.add(new PermissionModel("Microphone Access", "Allow responders to hear the environment", R.drawable.speak, false));

            rvPermissionsSharing.setLayoutManager(new LinearLayoutManager(this));
            rvPermissionsSharing.setAdapter(new PermissionAdapter(permissions));
        }
    }

    // ==========================================
    // INNER CLASSES (Models & Adapters)
    // ==========================================

    private static class ActionModel {
        String title, desc;
        boolean selected;
        ActionModel(String title, String desc, boolean selected) {
            this.title = title; this.desc = desc; this.selected = selected;
        }
    }

    private static class ActionAdapter extends RecyclerView.Adapter<ActionAdapter.ViewHolder> {
        private final List<ActionModel> list;
        ActionAdapter(List<ActionModel> list) { this.list = list; }

        @NonNull @Override
        public ViewHolder onCreateViewHolder(@NonNull ViewGroup p, int vt) {
            View v = LayoutInflater.from(p.getContext()).inflate(R.layout.item_centered_action_card, p, false);
            return new ViewHolder(v);
        }

        @Override
        public void onBindViewHolder(@NonNull ViewHolder h, int pos) {
            ActionModel m = list.get(pos);
            h.title.setText(m.title);
            h.desc.setText(m.desc);

            MaterialCardView cardView = (MaterialCardView) h.itemView;
            if (m.selected) {
                cardView.setStrokeColor(Color.parseColor("#266153"));
                cardView.setStrokeWidth(6);
            } else {
                cardView.setStrokeColor(Color.parseColor("#BEC5C2"));
                cardView.setStrokeWidth(3);
            }

            h.itemView.setOnClickListener(v -> {
                int adapterPos = h.getBindingAdapterPosition();
                if (adapterPos != RecyclerView.NO_POSITION) {
                    for (int i = 0; i < list.size(); i++) {
                        list.get(i).selected = (i == adapterPos);
                    }
                    notifyDataSetChanged();
                }
            });
        }

        @Override public int getItemCount() { return list.size(); }

        static class ViewHolder extends RecyclerView.ViewHolder {
            TextView title, desc;
            ViewHolder(@NonNull View v) {
                super(v);
                title = v.findViewById(R.id.tvActionCardTitle);
                desc = v.findViewById(R.id.tvActionCardDesc);
            }
        }
    }

    private static class PermissionModel {
        String title, desc; int icon; boolean isChecked;
        PermissionModel(String t, String d, int i, boolean c) { title = t; desc = d; icon = i; isChecked = c; }
    }

    private static class PermissionAdapter extends RecyclerView.Adapter<PermissionAdapter.ViewHolder> {
        private final List<PermissionModel> list;
        PermissionAdapter(List<PermissionModel> list) { this.list = list; }

        @NonNull @Override
        public ViewHolder onCreateViewHolder(@NonNull ViewGroup p, int vt) {
            View v = LayoutInflater.from(p.getContext()).inflate(R.layout.item_permission_sharing_card, p, false);
            return new ViewHolder(v);
        }

        @Override
        public void onBindViewHolder(@NonNull ViewHolder h, int pos) {
            PermissionModel m = list.get(pos);
            h.title.setText(m.title);
            h.desc.setText(m.desc);
            h.icon.setImageResource(m.icon);
            h.toggle.setChecked(m.isChecked);
        }

        @Override public int getItemCount() { return list.size(); }

        static class ViewHolder extends RecyclerView.ViewHolder {
            TextView title, desc; ImageView icon; SwitchMaterial toggle;
            ViewHolder(@NonNull View v) {
                super(v);
                title = v.findViewById(R.id.tvPermissionTitle);
                desc = v.findViewById(R.id.tvPermissionDesc);
                icon = v.findViewById(R.id.ivPermissionIcon);
                toggle = v.findViewById(R.id.switchPermissionToggle);
            }
        }
    }
}