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
import android.widget.TextView;
import android.widget.Toast;

import com.facebook.CallbackManager;
import com.facebook.FacebookSdk;
import com.facebook.login.widget.LoginButton;

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

public class LoginActivity extends AppCompatActivity {

    Button signIn;
    ProgressDialog pDialog;
    EditText emailView, passwordView;
    String email_st, password_st;
    TextView forgotView, signupView;
    private LoginButton loginButton;
    private CallbackManager callbackManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        //Initialise FB
        FacebookSdk.sdkInitialize(getApplicationContext());
        callbackManager = CallbackManager.Factory.create();

        //Set Content View
        setContentView(R.layout.activity_login);

        loginButton = (LoginButton)findViewById(R.id.login_button);
        signIn = (Button) findViewById(R.id.signin_btn);
        emailView = (EditText) findViewById(R.id.email_text);
        passwordView = (EditText) findViewById(R.id.password_txt);
        forgotView = (TextView) findViewById(R.id.forgot_password_txt);
        signupView = (TextView) findViewById(R.id.signup_txt);

        //Sign In button click
        signIn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                email_st = emailView.getText().toString();
                password_st = passwordView.getText().toString();
//                Intent i = new Intent(LoginActivity.this, HomeActivity.class);
//                startActivity(i);
                new PostAttachment().execute();
            }
        });

    }

    private class PostAttachment extends AsyncTask<Object, Object, String> {

        @Override
        protected void onPreExecute() {
            super.onPreExecute();

//            // Showing progress dialog
//            pDialog = new ProgressDialog(LoginActivity.this);
//            pDialog.setMessage("Please Wait");
//            pDialog.setCancelable(false);
//            pDialog.show();
        }

        @Override
        protected String doInBackground(Object... params) {

            try {

                URL url = new URL("http://shop.irinerose.com/api/user/login");

                JSONObject postDataParams = new JSONObject();
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
            Toast.makeText(getApplicationContext(), result, Toast.LENGTH_LONG).show();
            Log.d("Test Login", result);
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
}
