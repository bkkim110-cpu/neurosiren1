package com.example.nuerosiren;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.ImageView;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class HomeRiskFragment extends Fragment {

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        // Inflate the Potential Risk layout
        View view = inflater.inflate(R.layout.fragment_home_risk, container, false);

        // Set up the RecyclerView
        RecyclerView rvRiskMetrics = view.findViewById(R.id.rvRiskMetrics);
        rvRiskMetrics.setLayoutManager(new LinearLayoutManager(getContext()));

        // Populate your two dynamic warning cards
        List<RiskModel> riskList = new ArrayList<>();
        riskList.add(new RiskModel("Speech Pattern", "Irregular", R.drawable.profile)); // Swap with speech icon later
        riskList.add(new RiskModel("Gait & Balance", "Irregular", R.drawable.profile)); // Swap with walking icon later

        // Attach the adapter
        RiskAdapter adapter = new RiskAdapter(riskList);
        rvRiskMetrics.setAdapter(adapter);

        // Start Test Button Click Action
        view.findViewById(R.id.btnRiskStartTest).setOnClickListener(v -> {
            Intent intent = new Intent(getActivity(), EvaluationArmsActivity.class);
            startActivity(intent);
        });

        // Profile Icon Click Action
        View btnProfile = view.findViewById(R.id.btnProfile);
        if (btnProfile != null) {
            btnProfile.setOnClickListener(v -> {
                if (getActivity() instanceof DashboardActivity) {
                    com.google.android.material.bottomnavigation.BottomNavigationView nav = getActivity().findViewById(R.id.bottom_navigation_bar);
                    if (nav != null) {
                        nav.setSelectedItemId(R.id.nav_profile);
                    }
                }
            });
        }

        // Set risk percentage text
        TextView tvDescription = view.findViewById(R.id.tvRiskDescription);
        if (getArguments() != null) {
            float risk = getArguments().getFloat("risk_percentage", 0f);
            tvDescription.setText(String.format(Locale.getDefault(), "Irregularities detected in speech. Current Risk: %.1f%%. Please remain calm.", risk));
        }

        return view;
    }

    // ==========================================
    // RECYCLERVIEW ADAPTER (Bundled locally)
    // ==========================================
    private class RiskAdapter extends RecyclerView.Adapter<RiskAdapter.RiskViewHolder> {
        private final List<RiskModel> dataList;

        public RiskAdapter(List<RiskModel> dataList) {
            this.dataList = dataList;
        }

        @NonNull
        @Override
        public RiskViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_home_warning, parent, false);
            return new RiskViewHolder(view);
        }

        @Override
        public void onBindViewHolder(@NonNull RiskViewHolder holder, int position) {
            RiskModel item = dataList.get(position);
            holder.tvLabel.setText(item.getLabel());
            holder.tvStatus.setText(item.getStatus());
            holder.ivIcon.setImageResource(item.getIconRes());
        }

        @Override
        public int getItemCount() {
            return dataList.size();
        }

        class RiskViewHolder extends RecyclerView.ViewHolder {
            TextView tvLabel, tvStatus;
            ImageView ivIcon;

            public RiskViewHolder(@NonNull View itemView) {
                super(itemView);
                tvLabel = itemView.findViewById(R.id.tvWarningRowLabel);
                tvStatus = itemView.findViewById(R.id.tvWarningRowStatus);
                ivIcon = itemView.findViewById(R.id.ivWarningRowIcon);
            }
        }
    }

    // ==========================================
    // DATA MODEL (Bundled locally)
    // ==========================================
    private static class RiskModel {
        private final String label;
        private final String status;
        private final int iconRes;

        public RiskModel(String label, String status, int iconRes) {
            this.label = label;
            this.status = status;
            this.iconRes = iconRes;
        }

        public String getLabel() { return label; }
        public String getStatus() { return status; }
        public int getIconRes() { return iconRes; }
    }
}