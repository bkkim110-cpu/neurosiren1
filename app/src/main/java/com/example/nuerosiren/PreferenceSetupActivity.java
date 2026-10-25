package com.example.nuerosiren;

import android.os.Bundle;
import androidx.appcompat.app.AppCompatActivity;
import com.example.nuerosiren.R;

public class PreferenceSetupActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_preference_setup);

        if (getSupportActionBar() != null) {
            getSupportActionBar().hide();
        }
    }
}