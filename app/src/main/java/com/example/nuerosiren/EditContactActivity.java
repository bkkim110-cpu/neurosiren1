package com.example.nuerosiren;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.ImageView;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.google.android.material.switchmaterial.SwitchMaterial;
import com.google.android.material.textfield.TextInputEditText;
import java.util.ArrayList;
import java.util.List;

public class EditContactActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_edit_contact);

        if (getSupportActionBar() != null) {
            getSupportActionBar().hide();
        }

        // Initialize Views
        TextInputEditText etFullName = findViewById(R.id.etFullName);
        AutoCompleteTextView actRelationship = findViewById(R.id.actRelationship);
        TextInputEditText etPhoneNumber = findViewById(R.id.etPhoneNumber);
        ImageView btnBack = findViewById(R.id.btnBack);

        // Setup Relationship Dropdown
        String[] relationships = new String[]{"Family", "Friend", "Spouse", "Colleague", "Other"};
        ArrayAdapter<String> adapter = new ArrayAdapter<>(this, android.R.layout.simple_dropdown_item_1line, relationships);
        actRelationship.setAdapter(adapter);

        // Get Data from Intent
        String name = getIntent().getStringExtra("contact_name");
        String relationship = getIntent().getStringExtra("contact_relationship");
        String phone = getIntent().getStringExtra("contact_phone");
        int position = getIntent().getIntExtra("contact_position", -1);

        // Populate Fields
        if (name != null) etFullName.setText(name);
        if (relationship != null) actRelationship.setText(relationship, false);
        if (phone != null) etPhoneNumber.setText(phone);

        // Setup Alert Preferences RecyclerView
        RecyclerView rvAlertPreferences = findViewById(R.id.rvAlertPreferences);
        if (rvAlertPreferences != null) {
            List<AlertPrefModel> prefList = new ArrayList<>();
            prefList.add(new AlertPrefModel("SMS Notifications", "Send automated text on SOS", true));
            prefList.add(new AlertPrefModel("Phone Call Notifications", "Trigger voice call during emergency", true));
            prefList.add(new AlertPrefModel("Live Location Sharing", "Share real-time GPS coordinates", false));

            rvAlertPreferences.setLayoutManager(new LinearLayoutManager(this));
            rvAlertPreferences.setAdapter(new AlertPrefAdapter(prefList));
        }

        // Back Button Logic
        if (btnBack != null) {
            btnBack.setOnClickListener(v -> finish());
        }

        // Save Changes Logic
        findViewById(R.id.btnSaveChanges).setOnClickListener(v -> {
            String updatedName = etFullName.getText().toString();
            String updatedRelationship = actRelationship.getText().toString();
            String updatedPhone = etPhoneNumber.getText().toString();

            if (!updatedName.isEmpty()) {
                Intent resultIntent = new Intent();
                resultIntent.putExtra("contact_name", updatedName);
                resultIntent.putExtra("contact_relationship", updatedRelationship);
                resultIntent.putExtra("contact_phone", updatedPhone);
                resultIntent.putExtra("contact_position", position);
                resultIntent.putExtra("action", "update");
                setResult(RESULT_OK, resultIntent);
                finish();
            } else {
                etFullName.setError("Name is required");
            }
        });

        // Delete Contact Logic
        findViewById(R.id.btnDeleteContact).setOnClickListener(v -> {
            Intent resultIntent = new Intent();
            resultIntent.putExtra("contact_position", position);
            resultIntent.putExtra("action", "delete");
            setResult(RESULT_OK, resultIntent);
            finish();
        });
    }

    private static class AlertPrefModel {
        String title, subtitle;
        boolean enabled;
        AlertPrefModel(String title, String subtitle, boolean enabled) {
            this.title = title;
            this.subtitle = subtitle;
            this.enabled = enabled;
        }
    }

    private static class AlertPrefAdapter extends RecyclerView.Adapter<AlertPrefAdapter.ViewHolder> {
        private final List<AlertPrefModel> list;
        AlertPrefAdapter(List<AlertPrefModel> list) { this.list = list; }

        @NonNull
        @Override
        public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_alert_preference, parent, false);
            return new ViewHolder(v);
        }

        @Override
        public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
            AlertPrefModel item = list.get(position);
            holder.tvTitle.setText(item.title);
            holder.tvSubtitle.setText(item.subtitle);
            holder.switchToggle.setChecked(item.enabled);
        }

        @Override
        public int getItemCount() { return list.size(); }

        static class ViewHolder extends RecyclerView.ViewHolder {
            TextView tvTitle, tvSubtitle;
            SwitchMaterial switchToggle;
            ViewHolder(@NonNull View itemView) {
                super(itemView);
                tvTitle = itemView.findViewById(R.id.tvPreferenceTitle);
                tvSubtitle = itemView.findViewById(R.id.tvPreferenceSubtitle);
                switchToggle = itemView.findViewById(R.id.switchPreference);
            }
        }
    }
}