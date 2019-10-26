package com.mk.playAndLearn.activity;

import android.content.Intent;
import android.graphics.PorterDuff;
import android.graphics.drawable.Drawable;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.widget.RelativeLayout;
import android.widget.Toast;

import com.mk.enjoylearning.R;

public class SettingsActivity extends AppCompatActivity {
    RelativeLayout editProfileView, languageView, contactUsView, rateUsView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings);
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        ActionBar actionBar = getSupportActionBar();
        final Drawable upArrow = getResources().getDrawable(R.drawable.backf);
        upArrow.setColorFilter(getResources().getColor(R.color.white), PorterDuff.Mode.SRC_ATOP);
        getSupportActionBar().setHomeAsUpIndicator(upArrow);
        assert actionBar != null;
        actionBar.setDisplayHomeAsUpEnabled(true);
        actionBar.setDisplayShowTitleEnabled(false);

        editProfileView = findViewById(R.id.editProfileView);
        languageView = findViewById(R.id.languageView);
        contactUsView = findViewById(R.id.contactUsView);
        rateUsView = findViewById(R.id.rateUsView);

        editProfileView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
               // startActivity(new Intent(SettingsActivity.this, EditProfileActivity.class));
                Toast.makeText(SettingsActivity.this, "سيتم إضافتها قريبا", Toast.LENGTH_SHORT).show();
            }
        });

        languageView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Toast.makeText(SettingsActivity.this, "سيتم إضافتها قريبا", Toast.LENGTH_SHORT).show();
            }
        });

        contactUsView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startActivity(new Intent(SettingsActivity.this, ContactUsActivity.class));
            }
        });

        rateUsView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Toast.makeText(SettingsActivity.this, "سيتم إضافتها قريبا", Toast.LENGTH_SHORT).show();

            }
        });

    }
}
