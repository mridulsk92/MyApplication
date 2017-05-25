package com.example.xts015.myapplication;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.os.AsyncTask;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
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

public class RegisterActivity extends AppCompatActivity {

    Button signup_btn;
    EditText name, phone, email, password;
    String name_st, phone_st, email_st, password_st;
    ProgressDialog pDialog;
    PreferencesHelper pref;
    String message, coupSuccess, coupon;
    String success,source;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);

        //Get Intent
        Intent i = getIntent();
        source = i.getStringExtra("From");

        //Initialise
        pref = new PreferencesHelper(RegisterActivity.this);
        name = (EditText) findViewById(R.id.name_text);
        phone = (EditText) findViewById(R.id.phone_text);
        email = (EditText) findViewById(R.id.email_text);
        password = (EditText) findViewById(R.id.password_text);
        signup_btn = (Button) findViewById(R.id.button_signup);

        //Save Source
        pref.SavePreferences("Source", source);

        //button OnClick
        signup_btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                name_st = name.getText().toString();
                phone_st = phone.getText().toString();
                email_st = email.getText().toString();
                password_st = password.getText().toString();
                new PostRegister().execute();
            }
        });
    }

    private class PostRegister extends AsyncTask<Object, Object, String> {

        @Override
        protected void onPreExecute() {
            super.onPreExecute();

            // Showing progress dialog
            pDialog = new ProgressDialog(RegisterActivity.this);
            pDialog.setMessage("Please Wait");
            pDialog.setCancelable(false);
            pDialog.show();
        }

        @Override
        protected String doInBackground(Object... params) {

            try {

                URL url = new URL("http://shop.irinerose.com/api/user/register");

                JSONObject postDataParams = new JSONObject();
                postDataParams.put("name", name_st);
                postDataParams.put("phone", phone_st);
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
                    success = obj.getString("success");

                    if (success.equals("true")) {

                        //Save token and success value
                        String token = obj.getString("token");
                        pref.SavePreferences("Login", success);
                        pref.SavePreferences("Token", token);

                        //Get Message
                        message = obj.getString("message");

                        //Get Coupon
                        JSONObject couponObj = obj.getJSONObject("coupon");
                        coupSuccess = couponObj.getString("success");
                        coupon = couponObj.getString("coupon");

                        //Get and Save User Details
                        JSONObject userObj = obj.getJSONObject("data");
                        String id = userObj.getString("id");
                        String name = userObj.getString("name");
                        String email = userObj.getString("email");
                        String currency = userObj.getString("currency");

                        pref.SavePreferences("UserName", name);
                        pref.SavePreferences("UserId", id);
                        pref.SavePreferences("UserEmail", email);
                        pref.SavePreferences("UserCurrency", currency);
                    }

                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }

            if (success.equals("true")) {
                Intent i = new Intent(RegisterActivity.this, WelcomeActivity.class);
                i.putExtra("Message", message);
                i.putExtra("Coupon", coupon);
                startActivity(i);
            } else {
                try {
                    JSONObject logObj = new JSONObject(result);
                    String log = logObj.getString("log");
                    Toast.makeText(RegisterActivity.this, log, Toast.LENGTH_SHORT).show();
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

    @Override
    protected void onPause() {
        super.onPause();
        super.finish();
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        if(source.equals("HomeActivity")){
            Intent i = new Intent(RegisterActivity.this, HomeActivity.class);
            startActivity(i);
        }else{
            String product_id = pref.GetPreferences("ProductId");
            Intent i = new Intent(RegisterActivity.this, ProductActivity.class);
            i.putExtra("Product Id", product_id);
            startActivity(i);
        }
    }
}
