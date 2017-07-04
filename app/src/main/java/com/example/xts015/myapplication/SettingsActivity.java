package com.example.xts015.myapplication;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

public class SettingsActivity extends AppCompatActivity {

    RelativeLayout hkgLayout, inrLayout, usdLayout, uaeLayout;
    PreferencesHelper pref;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings);

        //Toolbar set title
        Toolbar toolbar_nologo = (Toolbar) findViewById(R.id.toolbar_nologo);
        TextView toolbar_title = (TextView) toolbar_nologo.findViewById(R.id.title_toolbar);
        toolbar_title.setText("");

        //Count Badge and Cart Button
        TextView badge = (TextView) toolbar_nologo.findViewById(R.id.textOne);
        ImageView cart = (ImageView) toolbar_nologo.findViewById(R.id.cart_view);
        badge.setVisibility(View.GONE);
        cart.setVisibility(View.GONE);

        //Initialise
        pref = new PreferencesHelper(SettingsActivity.this);
        hkgLayout = (RelativeLayout) findViewById(R.id.hkg_layout);
        inrLayout = (RelativeLayout) findViewById(R.id.india_layout);
        usdLayout = (RelativeLayout) findViewById(R.id.usa_layout);
        uaeLayout = (RelativeLayout) findViewById(R.id.uae_layout);

        //onClick of Hkg
        hkgLayout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                pref.SavePreferences("Location", "HKG");
                Toast.makeText(SettingsActivity.this, "Locale Changed", Toast.LENGTH_SHORT).show();
                Intent i = new Intent(SettingsActivity.this, HomeActivity.class);
                startActivity(i);
            }
        });

        //onClick of India
        inrLayout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                pref.SavePreferences("Location", "INR");
                Toast.makeText(SettingsActivity.this, "Locale Changed", Toast.LENGTH_SHORT).show();
                Intent i = new Intent(SettingsActivity.this, HomeActivity.class);
                startActivity(i);
            }
        });

        //onClick of USA
        usdLayout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                pref.SavePreferences("Location", "USD");
                Toast.makeText(SettingsActivity.this, "Locale Changed", Toast.LENGTH_SHORT).show();
                Intent i = new Intent(SettingsActivity.this, HomeActivity.class);
                startActivity(i);

            }
        });

        //onClick of UAE
        uaeLayout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                pref.SavePreferences("Location", "AED");
                Toast.makeText(SettingsActivity.this, "Locale Changed", Toast.LENGTH_SHORT).show();
                Intent i = new Intent(SettingsActivity.this, HomeActivity.class);
                startActivity(i);

            }
        });
    }
}
