package com.example.nuerosiren;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.bottomnavigation.BottomNavigationView;

public class ProfileFragment extends Fragment {

    private final ActivityResultLauncher<Intent> editPersonalLauncher = registerForActivityResult(
            new ActivityResultContracts.StartActivityForResult(),
            result -> {
                if (result.getResultCode() == Activity.RESULT_OK) {
                    updatePersonalInfoUI();
                }
            }
    );

    private final ActivityResultLauncher<Intent> editEmergencyLauncher = registerForActivityResult(
            new ActivityResultContracts.StartActivityForResult(),
            result -> {
                if (result.getResultCode() == Activity.RESULT_OK) {
                    updateEmergencySettingsUI();
                }
            }
    );

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_profile, container, false);

        // --- ADDED RECYCLERVIEW SETUP ---
        RecyclerView rvSettings = view.findViewById(R.id.rvNotificationSettings);
        if (rvSettings != null) {
            rvSettings.setLayoutManager(new LinearLayoutManager(getContext()));
            rvSettings.setAdapter(new DemoAdapter());
        }
        // --------------------------------

        // Personal Information Edit Icon (Pencil)
        View btnEditPersonal = view.findViewById(R.id.btnEditPersonalInfo);
        if (btnEditPersonal != null) {
            btnEditPersonal.setOnClickListener(v -> {
                Intent intent = new Intent(getActivity(), EditInfoActivity.class);
                editPersonalLauncher.launch(intent);
            });
        }

        // Emergency Settings Edit Icon (Pencil)
        View btnEditEmergency = view.findViewById(R.id.btnEditEmergencySettings);
        if (btnEditEmergency != null) {
            btnEditEmergency.setOnClickListener(v -> {
                Intent intent = new Intent(getActivity(), EditEmergencyActivity.class);
                editEmergencyLauncher.launch(intent);
            });
        }

        // Back button on the profile header (top left)
        View btnBack = view.findViewById(R.id.btnBack);
        if (btnBack != null) {
            btnBack.setOnClickListener(v -> {
                if (getActivity() instanceof DashboardActivity) {
                    BottomNavigationView nav = getActivity().findViewById(R.id.bottom_navigation_bar);
                    if (nav != null) {
                        nav.setSelectedItemId(R.id.nav_home);
                    }
                }
            });
        }

        return view;
    }

    @Override
    public void onResume() {
        super.onResume();
        updatePersonalInfoUI();
        updateEmergencySettingsUI();
    }

    private void updatePersonalInfoUI() {
        if (getView() == null || getContext() == null) return;
        SharedPreferences prefs = getContext().getSharedPreferences("UserProfilePrefs", Context.MODE_PRIVATE);
        String age = prefs.getString("age", "45");
        String gender = prefs.getString("gender", "Female");
        String height = prefs.getString("height", "165 cm");
        String weight = prefs.getString("weight", "60 kg");
        String address = prefs.getString("address", "123 Health Ave, Medical City");
        String conditions = prefs.getString("conditions", "None");

        TextView tvAgeValue = getView().findViewById(R.id.tvAgeValue);
        TextView tvGenderValue = getView().findViewById(R.id.tvGenderValue);
        TextView tvHeightValue = getView().findViewById(R.id.tvHeightValue);
        TextView tvWeightValue = getView().findViewById(R.id.tvWeightValue);
        TextView tvAddressValue = getView().findViewById(R.id.tvAddressValue);
        TextView tvConditionsValue = getView().findViewById(R.id.tvConditionsValue);

        if (tvAgeValue != null) tvAgeValue.setText(age);
        if (tvGenderValue != null) tvGenderValue.setText(gender);
        if (tvHeightValue != null) tvHeightValue.setText(height);
        if (tvWeightValue != null) tvWeightValue.setText(weight);
        if (tvAddressValue != null) tvAddressValue.setText(address);
        if (tvConditionsValue != null) tvConditionsValue.setText(conditions);
    }

    private void updateEmergencySettingsUI() {
        if (getView() == null || getContext() == null) return;
        SharedPreferences prefs = getContext().getSharedPreferences("EmergencyPrefs", Context.MODE_PRIVATE);
        String primaryAction = prefs.getString("primary_action", "Automated Voice Call");
        int alertDelay = prefs.getInt("alert_delay", 30);

        TextView tvVoiceSubtitle = getView().findViewById(R.id.tvVoiceSubtitle);
        TextView tvTimerSubtitle = getView().findViewById(R.id.tvTimerSubtitle);

        if (tvVoiceSubtitle != null) {
            tvVoiceSubtitle.setText(primaryAction);
        }
        if (tvTimerSubtitle != null) {
            tvTimerSubtitle.setText(String.format(java.util.Locale.getDefault(), "%d Seconds", alertDelay));
        }
    }

    // --- ADDED SIMPLE ADAPTER FOR DEMO DATA ---
    private static class DemoAdapter extends RecyclerView.Adapter<DemoAdapter.ViewHolder> {
        @NonNull
        @Override
        public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_notification_setting, parent, false);
            return new ViewHolder(view);
        }

        @Override
        public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
            // Fills out the visual items without needing real backend data yet
        }

        @Override
        public int getItemCount() {
            return 3; // Generates 3 fake items so the screen is tall enough to scroll
        }

        static class ViewHolder extends RecyclerView.ViewHolder {
            ViewHolder(View itemView) {
                super(itemView);
            }
        }
    }
}