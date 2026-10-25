package com.example.nuerosiren;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.google.android.material.switchmaterial.SwitchMaterial;
import com.google.android.material.textfield.TextInputLayout;
import java.util.ArrayList;
import java.util.List;

public class AddContactActivity extends AppCompatActivity {

    private AutoCompleteTextView actRelationship;
    private RecyclerView rvNotificationSettings;
    private TextInputLayout tilFullName, tilPhone;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_contact);

        if (getSupportActionBar() != null) {
            getSupportActionBar().hide();
        }

        // Initialize UI components
        ImageView btnBack = findViewById(R.id.btnBack);
        actRelationship = findViewById(R.id.actRelationship);
        rvNotificationSettings = findViewById(R.id.rvNotificationSettings);
        tilFullName = findViewById(R.id.tilFullName);
        tilPhone = findViewById(R.id.tilPhone);

        // Setup Back Button
        if (btnBack != null) {
            btnBack.setOnClickListener(v -> finish());
        }

        // Setup Relationship Dropdown
        setupRelationshipDropdown();

        // Setup Notification Settings RecyclerView
        setupNotificationSettings();

        // Setup Add Contact Button
        findViewById(R.id.btnAddContact).setOnClickListener(v -> {
            String name = tilFullName.getEditText() != null ? tilFullName.getEditText().getText().toString() : "";
            String relationship = actRelationship.getText().toString();
            String phone = tilPhone.getEditText() != null ? tilPhone.getEditText().getText().toString() : "";
            
            if (!name.isEmpty()) {
                Intent resultIntent = new Intent();
                resultIntent.putExtra("contact_name", name);
                resultIntent.putExtra("contact_relationship", relationship);
                resultIntent.putExtra("contact_phone", phone);
                setResult(RESULT_OK, resultIntent);
                finish();
            } else {
                tilFullName.setError("Name is required");
            }
        });
    }

    private void setupRelationshipDropdown() {
        String[] relationships = new String[]{"Family", "Friend", "Spouse", "Colleague", "Other"};
        ArrayAdapter<String> adapter = new ArrayAdapter<>(this, android.R.layout.simple_dropdown_item_1line, relationships);
        actRelationship.setAdapter(adapter);
    }

    private void setupNotificationSettings() {
        List<NotificationSetting> settings = new ArrayList<>();
        settings.add(new NotificationSetting("Immediate SMS Alert", "Send an SMS immediately when an emergency is detected.", true));
        settings.add(new NotificationSetting("Voice Call Alert", "Call this contact if the emergency is not acknowledged.", false));
        settings.add(new NotificationSetting("Live Location Sharing", "Share your real-time GPS coordinates during an active event.", true));

        NotificationAdapter adapter = new NotificationAdapter(settings);
        rvNotificationSettings.setLayoutManager(new LinearLayoutManager(this));
        rvNotificationSettings.setAdapter(adapter);
    }

    // ==========================================
    // DATA MODEL & ADAPTER
    // ==========================================
    private static class NotificationSetting {
        String title, description;
        boolean isEnabled;

        public NotificationSetting(String title, String description, boolean isEnabled) {
            this.title = title;
            this.description = description;
            this.isEnabled = isEnabled;
        }
    }

    private static class NotificationAdapter extends RecyclerView.Adapter<NotificationAdapter.ViewHolder> {
        private final List<NotificationSetting> settings;

        public NotificationAdapter(List<NotificationSetting> settings) {
            this.settings = settings;
        }

        @NonNull
        @Override
        public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_notification_setting, parent, false);
            return new ViewHolder(view);
        }

        @Override
        public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
            NotificationSetting setting = settings.get(position);
            holder.tvTitle.setText(setting.title);
            holder.tvDesc.setText(setting.description);
            holder.switchToggle.setChecked(setting.isEnabled);
        }

        @Override
        public int getItemCount() {
            return settings.size();
        }

        static class ViewHolder extends RecyclerView.ViewHolder {
            TextView tvTitle, tvDesc;
            SwitchMaterial switchToggle;

            public ViewHolder(@NonNull View itemView) {
                super(itemView);
                tvTitle = itemView.findViewById(R.id.tvSettingTitle);
                tvDesc = itemView.findViewById(R.id.tvSettingDesc);
                switchToggle = itemView.findViewById(R.id.switchSettingToggle);
            }
        }
    }
}