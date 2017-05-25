package com.example.xts015.myapplication;

import android.content.Context;
import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import uk.co.chrisjenx.calligraphy.CalligraphyContextWrapper;

public class WelcomeActivity extends AppCompatActivity {

    PreferencesHelper pref;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_welcome);

        //Get Intent
        Intent i = getIntent();
        String message = i.getStringExtra("Message");
        String coupon = i.getStringExtra("Coupon");

        //Initialise
        pref = new PreferencesHelper(WelcomeActivity.this);
        TextView messageView = (TextView) findViewById(R.id.message);
        TextView couponView = (TextView) findViewById(R.id.coupon);
        Button continueButton = (Button) findViewById(R.id.continue_btn);

        //Get Source
        final String source = pref.GetPreferences("Source");

        //Set Message
        messageView.setText(message);
        couponView.setText(coupon);

        //onClick of Continue Button
        continueButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                if(source.equals("HomeActivity")){
                    Intent i = new Intent(WelcomeActivity.this, HomeActivity.class);
                    startActivity(i);
                }else{
                    String product_id = pref.GetPreferences("ProductId");
                    Intent i = new Intent(WelcomeActivity.this, ProductActivity.class);
                    i.putExtra("Product Id", product_id);
                    startActivity(i);
                }
            }
        });
    }

    @Override
    protected void attachBaseContext(Context context) {
        super.attachBaseContext(CalligraphyContextWrapper.wrap(context));
    }

    @Override
    protected void onPause() {
        super.onPause();
        super.finish();
    }

}
