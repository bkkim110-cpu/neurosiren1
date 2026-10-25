package com.example.nuerosiren;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.google.android.material.textfield.TextInputLayout;
import com.google.android.material.textfield.TextInputEditText;
import java.util.ArrayList;
import java.util.List;

public class EditInfoActivity extends AppCompatActivity {

    private RecyclerView rvGridFields;
    private TextInputLayout tilFullName, tilAddress;
    private EditText etMedicalNotes;
    private final List<GridFieldModel> gridFieldsList = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_edit_info);

        if (getSupportActionBar() != null) {
            getSupportActionBar().hide();
        }

        SharedPreferences prefs = getSharedPreferences("UserProfilePrefs", Context.MODE_PRIVATE);
        String name = prefs.getString("full_name", "Jane Doe");
        String age = prefs.getString("age", "45");
        String gender = prefs.getString("gender", "Female");
        String height = prefs.getString("height", "165 cm");
        String weight = prefs.getString("weight", "60 kg");
        String address = prefs.getString("address", "123 Health Ave, Medical City");
        String conditions = prefs.getString("conditions", "None");

        // Initialize UI
        View btnBack = findViewById(R.id.btnBack);
        if (btnBack != null) {
            btnBack.setOnClickListener(v -> finish());
        }

        tilFullName = findViewById(R.id.tilFullName);
        if (tilFullName != null && tilFullName.getEditText() != null) {
            tilFullName.getEditText().setText(name);
        }

        tilAddress = findViewById(R.id.tilAddress);
        if (tilAddress != null && tilAddress.getEditText() != null) {
            tilAddress.getEditText().setText(address);
        }

        etMedicalNotes = findViewById(R.id.etMedicalNotes);
        if (etMedicalNotes != null) {
            etMedicalNotes.setText(conditions);
        }

        rvGridFields = findViewById(R.id.rvGridFields);
        setupGridFields(age, gender, height, weight);

        findViewById(R.id.btnSaveChanges).setOnClickListener(v -> {
            String updatedName = (tilFullName != null && tilFullName.getEditText() != null)
                    ? tilFullName.getEditText().getText().toString() : name;
            String updatedAddress = (tilAddress != null && tilAddress.getEditText() != null)
                    ? tilAddress.getEditText().getText().toString() : address;
            String updatedConditions = (etMedicalNotes != null)
                    ? etMedicalNotes.getText().toString() : conditions;

            String updatedAge = gridFieldsList.size() > 0 ? gridFieldsList.get(0).value : age;
            String updatedGender = gridFieldsList.size() > 1 ? gridFieldsList.get(1).value : gender;
            String updatedHeight = gridFieldsList.size() > 2 ? gridFieldsList.get(2).value : height;
            String updatedWeight = gridFieldsList.size() > 3 ? gridFieldsList.get(3).value : weight;

            SharedPreferences.Editor editor = prefs.edit();
            editor.putString("full_name", updatedName);
            editor.putString("age", updatedAge);
            editor.putString("gender", updatedGender);
            editor.putString("height", updatedHeight);
            editor.putString("weight", updatedWeight);
            editor.putString("address", updatedAddress);
            editor.putString("conditions", updatedConditions);
            editor.apply();

            setResult(RESULT_OK);
            finish();
        });
    }

    private void setupGridFields(String age, String gender, String height, String weight) {
        if (rvGridFields != null) {
            gridFieldsList.clear();
            gridFieldsList.add(new GridFieldModel("Age", age));
            gridFieldsList.add(new GridFieldModel("Gender", gender));
            gridFieldsList.add(new GridFieldModel("Height", height));
            gridFieldsList.add(new GridFieldModel("Weight", weight));

            GridFieldAdapter adapter = new GridFieldAdapter(gridFieldsList);
            rvGridFields.setLayoutManager(new GridLayoutManager(this, 2));
            rvGridFields.setAdapter(adapter);
        }
    }

    // ==========================================
    // DATA MODEL & ADAPTER
    // ==========================================
    private static class GridFieldModel {
        String label;
        String value;

        public GridFieldModel(String label, String value) {
            this.label = label;
            this.value = value;
        }
    }

    private static class GridFieldAdapter extends RecyclerView.Adapter<GridFieldAdapter.ViewHolder> {
        private final List<GridFieldModel> fields;

        public GridFieldAdapter(List<GridFieldModel> fields) {
            this.fields = fields;
        }

        @NonNull
        @Override
        public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_grid_info_field, parent, false);
            return new ViewHolder(view);
        }

        @Override
        public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
            GridFieldModel field = fields.get(position);
            holder.tilWrapper.setHint(field.label);
            holder.etInput.setText(field.value);

            holder.etInput.addTextChangedListener(new TextWatcher() {
                @Override public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
                @Override public void onTextChanged(CharSequence s, int start, int before, int count) {
                    field.value = s.toString();
                }
                @Override public void afterTextChanged(Editable s) {}
            });
        }

        @Override
        public int getItemCount() {
            return fields.size();
        }

        static class ViewHolder extends RecyclerView.ViewHolder {
            TextInputLayout tilWrapper;
            TextInputEditText etInput;

            public ViewHolder(@NonNull View itemView) {
                super(itemView);
                tilWrapper = itemView.findViewById(R.id.tilGridInputWrapper);
                etInput = itemView.findViewById(R.id.etGridInput);
            }
        }
    }
}