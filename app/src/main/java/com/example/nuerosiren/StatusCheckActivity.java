package com.example.nuerosiren;

import android.os.Bundle;
import androidx.appcompat.app.AppCompatActivity;

public class StatusCheckActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_status_check);

        if (getSupportActionBar() != null) {
            getSupportActionBar().hide();
        }

        // "Yes, I'm okay" Button closes this screen and goes back
        findViewById(R.id.btnYesOkay).setOnClickListener(v -> finish());

        // "No, continue test" Button also closes this screen to resume evaluation
        findViewById(R.id.btnNoContinue).setOnClickListener(v -> finish());
    }
}