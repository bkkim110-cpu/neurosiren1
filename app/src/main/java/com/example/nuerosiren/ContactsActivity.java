package com.example.nuerosiren;

import android.content.Intent;
import android.os.Bundle;
import androidx.appcompat.app.AppCompatActivity;
import androidx.constraintlayout.widget.ConstraintLayout;
import com.example.nuerosiren.R;

public class ContactsActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_contacts);

        if (getSupportActionBar() != null) {
            getSupportActionBar().hide();
        }

        findViewById(R.id.btnBack).setOnClickListener(v -> finish());

        ConstraintLayout btnAdd = findViewById(R.id.btnAddNewContact);
        if (btnAdd != null) {
            btnAdd.setOnClickListener(v -> {
                Intent intent = new Intent(ContactsActivity.this, AddContactActivity.class);
                startActivity(intent);
            });
        }
    }
}