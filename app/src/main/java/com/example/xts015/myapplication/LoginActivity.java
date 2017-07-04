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
import com.facebook.FacebookCallback;
import com.facebook.FacebookException;
import com.facebook.FacebookSdk;
import com.facebook.GraphRequest;
import com.facebook.GraphResponse;
import com.facebook.login.LoginResult;
import com.facebook.login.widget.LoginButton;

import org.json.JSONArray;
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
import java.util.Arrays;
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
    String source;
    PreferencesHelper pref;
    String success, message, coupSuccess, coupon;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        //Get Intent
        Intent i = getIntent();
        source = i.getStringExtra("From");

        //Initialise FB
        facebookSDKInitialize();

        //Set Content View
        setContentView(R.layout.activity_login);

        //Initialise
        pref = new PreferencesHelper(LoginActivity.this);
        loginButton = (LoginButton) findViewById(R.id.login_button);
        signIn = (Button) findViewById(R.id.signin_btn);
        emailView = (EditText) findViewById(R.id.email_text);
        passwordView = (EditText) findViewById(R.id.password_txt);
        forgotView = (TextView) findViewById(R.id.forgot_password_txt);
        signupView = (TextView) findViewById(R.id.signup_txt);

        //Save Source
        pref.SavePreferences("Source", source);

        //facebook login button add permissions
//        loginButton.setReadPermissions(Arrays.asList("public_profile"));
//        getLoginDetails(loginButton);

        //Sign In button click
        signIn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                email_st = emailView.getText().toString();
                password_st = passwordView.getText().toString();
//                Intent i = new Intent(LoginActivity.this, HomeActivity.class);
//                startActivity(i);
                new PostLogin().execute();
            }
        });

        //SignUp button Click
        signupView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent i = new Intent(LoginActivity.this, RegisterActivity.class);
                startActivity(i);
            }
        });
    }

    protected void getLoginDetails(LoginButton login_button) {
        // Callback registration
        login_button.registerCallback(callbackManager, new FacebookCallback<LoginResult>() {
            @Override
            public void onSuccess(LoginResult login_result) {

                // App code
                GraphRequest request = GraphRequest.newMeRequest(
                        login_result.getAccessToken(),
                        new GraphRequest.GraphJSONObjectCallback() {
                            @Override
                            public void onCompleted(JSONObject object, GraphResponse response) {
                                Log.v("LoginActivity", response.toString());

                                // Application code
                                try {
                                    String email = object.getString("email");
                                    String name = object.getString("name");
                                    String id = object.getString("id");

                                    Log.d("DATAAA", email + name + id);
                                } catch (JSONException e) {
                                    e.printStackTrace();
                                }
                            }
                        });
                Bundle parameters = new Bundle();
                parameters.putString("fields", "id, first_name, email");
                request.setParameters(parameters);
                request.executeAsync();
            }

            @Override
            public void onCancel() {
                // code for cancellation
                System.out.println("onCancel");
            }

            @Override
            public void onError(FacebookException exception) {
                //  code to handle error
                System.out.println("onError");
                Log.v("LoginActivity", exception.getCause().toString());
            }
        });
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        callbackManager.onActivityResult(requestCode, resultCode, data);
        Log.e("data", data.toString());
    }

    protected void facebookSDKInitialize() {
        FacebookSdk.sdkInitialize(getApplicationContext());
        callbackManager = CallbackManager.Factory.create();
    }

    private class PostLogin extends AsyncTask<Object, Object, String> {

        @Override
        protected void onPreExecute() {
            super.onPreExecute();

            // Showing progress dialog
            pDialog = new ProgressDialog(LoginActivity.this);
            pDialog.setMessage("Please Wait");
            pDialog.setCancelable(false);
            pDialog.show();
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
            if (pDialog.isShowing()) {
                pDialog.dismiss();
            }

            Log.d("Test Login", result);

            if (result != null) {
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
//                        JSONObject couponObj = obj.getJSONObject("coupon");
//                        coupSuccess = couponObj.getString("success");
//                        coupon = couponObj.getString("coupon");

                        //Get and Save User Details
                        JSONObject userObj = obj.getJSONObject("data");
//                        String id = userObj.getString("id");
                        String name = userObj.getString("name");
                        String email = userObj.getString("email");
                        String phone = userObj.getString("phone");
//                        String currency = userObj.getString("currency");
                        pref.SavePreferences("UserName", name);
                        pref.SavePreferences("Phone", phone);
                        pref.SavePreferences("UserEmail", email);
//                        pref.SavePreferences("UserCurrency", currency);

                        JSONObject cartObj = obj.getJSONObject("cart");
                        int count = cartObj.getInt("count");
                        pref.SavePreferences("Cart Count", String.valueOf(count));
                    }

                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }

            if (success.equals("true")) {

                if(source.equals("HomeActivity")){
                    Intent i = new Intent(LoginActivity.this, HomeActivity.class);
                    startActivity(i);
                }else{
                    String product_id = pref.GetPreferences("ProductId");
                    Intent i = new Intent(LoginActivity.this, ProductActivity.class);
                    i.putExtra("Product Id", product_id);
                    startActivity(i);
                }

            } else {
                try {
                    JSONObject logObj = new JSONObject(result);
                    String log = logObj.getString("log");
                    Toast.makeText(LoginActivity.this, log, Toast.LENGTH_SHORT).show();
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
    public void onBackPressed() {
        super.onBackPressed();
        if(source.equals("HomeActivity")){
            Intent i = new Intent(LoginActivity.this, HomeActivity.class);
            startActivity(i);
        }else{
            String product_id = pref.GetPreferences("ProductId");
            Intent i = new Intent(LoginActivity.this, ProductActivity.class);
            i.putExtra("Product Id", product_id);
            startActivity(i);
        }
    }

    //    @Override
//    protected void onPause() {
//        super.onPause();
//        super.finish();
//    }
}
