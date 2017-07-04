package com.example.xts015.myapplication;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.os.AsyncTask;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.RadioButton;
import android.widget.TextView;
import android.widget.Toast;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;
import java.util.Iterator;

import javax.net.ssl.HttpsURLConnection;

import uk.co.chrisjenx.calligraphy.CalligraphyContextWrapper;

public class AccountActivity extends AppCompatActivity {

    EditText nameText, emailText, passwordText, numberText;
    Button saveBtn, addressBtn;
    RadioButton maleBtn, femaleBtn;

    String namrePref, emailPref, numberPref, passwordPref;
    String name_st, email_st, number_st, password_st;
    PreferencesHelper pref;

    ProgressDialog pDialog;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_account);

        //Toolbar set title
        Toolbar toolbar_nologo = (Toolbar) findViewById(R.id.toolbar_nologo);
        TextView toolbar_title = (TextView) toolbar_nologo.findViewById(R.id.title_toolbar);
        toolbar_title.setText("My Account");

        //Count Badge and Cart Button
        TextView badge = (TextView) toolbar_nologo.findViewById(R.id.textOne);
        ImageView cart = (ImageView) toolbar_nologo.findViewById(R.id.cart_view);
        badge.setVisibility(View.GONE);
        cart.setVisibility(View.GONE);

        //Toolbar back Button
        ImageButton back = (ImageButton) toolbar_nologo.findViewById(R.id.button_back);
        back.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent i = new Intent(AccountActivity.this, HomeActivity.class);
                startActivity(i);
            }
        });

        //Initialise
        pref = new PreferencesHelper(AccountActivity.this);
        nameText = (EditText) findViewById(R.id.name_text);
        emailText = (EditText) findViewById(R.id.email_text);
        passwordText = (EditText) findViewById(R.id.password_text);
        numberText = (EditText) findViewById(R.id.mobile_text);

        saveBtn = (Button) findViewById(R.id.button_save);
        addressBtn = (Button) findViewById(R.id.address_button);

        maleBtn = (RadioButton) findViewById(R.id.radio_male);
        femaleBtn = (RadioButton) findViewById(R.id.radio_female);

        //Get Preference Values
        namrePref = pref.GetPreferences("UserName");
        emailPref = pref.GetPreferences("UserEmail");
//        passwordPref = pref.GetPreferences("Password");
        numberPref = pref.GetPreferences("Phone");

        //Set Text Values
        nameText.setText(namrePref);
        emailText.setText(emailPref);
        numberText.setText(numberPref);

        //Save Button onClick
        saveBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                name_st = nameText.getText().toString();
                email_st = emailText.getText().toString();
                number_st = numberText.getText().toString();
                password_st = passwordText.getText().toString();

                new SaveAddress().execute();
            }
        });

        //Address Button onClick
        addressBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                Intent i = new Intent(AccountActivity.this, AddressActivity.class);
                startActivity(i);
            }
        });
    }

    private class SaveAddress extends AsyncTask<Object, Object, String> {

        @Override
        protected void onPreExecute() {
            super.onPreExecute();

            // Showing progress dialog
            pDialog = new ProgressDialog(AccountActivity.this);
            pDialog.setMessage("Please Wait");
            pDialog.setCancelable(false);
            pDialog.show();
        }

        @Override
        protected String doInBackground(Object... params) {

            try {

                URL url = new URL("http://shop.irinerose.com/api/user/profile/update");

                JSONObject postDataParams = new JSONObject();
                postDataParams.put("name", name_st);
                postDataParams.put("phone", number_st);
                postDataParams.put("password", password_st);
                postDataParams.put("email", email_st);
                Log.e("params", postDataParams.toString());

                HttpURLConnection conn = (HttpURLConnection) url.openConnection();
                conn.setReadTimeout(15000 /* milliseconds */);
                conn.setConnectTimeout(15000 /* milliseconds */);
                conn.setRequestMethod("POST");
                conn.setDoInput(true);
                conn.setDoOutput(true);
                OutputStream os = conn.getOutputStream();
                BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(os, "UTF-8"));
                writer.write(getPostDataString(postDataParams));

                writer.flush();
                writer.close();
                os.close();

                int responseCode = conn.getResponseCode();

                if (responseCode == HttpsURLConnection.HTTP_OK) {

                    BufferedReader in = new BufferedReader(new InputStreamReader(conn.getInputStream()));

                    StringBuffer sb = new StringBuffer("");
                    String line = "";

                    while ((line = in.readLine()) != null) {

                        sb.append(line);
                        break;
                    }

                    in.close();
                    return sb.toString();

                } else {
                    return new String("false : " + responseCode);
                }
            } catch (Exception e) {
                return new String("Exception: " + e.getMessage());
            }

        }

        @Override
        protected void onPostExecute(String result) {

            if (pDialog.isShowing()) {
                pDialog.dismiss();
            }

            Log.d("Test Login", result);

            if (result != null && !result.contains("Exception")) {
                try {
                    JSONObject obj = new JSONObject(result);
                    String success = obj.getString("success");

                    if (success.equals("true")) {
                        Toast.makeText(AccountActivity.this, "Account Updated", Toast.LENGTH_SHORT).show();
                        Intent i = new Intent(AccountActivity.this, HomeActivity.class);
                        startActivity(i);

                    }

                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    public String getPostDataString(JSONObject params) throws Exception {

        StringBuilder result = new StringBuilder();
        boolean first = true;

        Iterator<String> itr = params.keys();

        while (itr.hasNext()) {

            String key = itr.next();
            Object value = params.get(key);

            if (first)
                first = false;
            else
                result.append("&");

            result.append(URLEncoder.encode(key, "UTF-8"));
            result.append("=");
            result.append(URLEncoder.encode(value.toString(), "UTF-8"));

        }
        return result.toString();
    }

    @Override
    protected void attachBaseContext(Context context) {
        super.attachBaseContext(CalligraphyContextWrapper.wrap(context));
    }
}
