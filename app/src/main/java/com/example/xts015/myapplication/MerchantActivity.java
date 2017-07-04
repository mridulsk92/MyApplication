package com.example.xts015.myapplication;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Intent;
import android.os.AsyncTask;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.widget.Toast;

import com.razorpay.Checkout;
import com.razorpay.PaymentResultListener;

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

public class MerchantActivity extends Activity implements PaymentResultListener {

    String amount, ship_method, locale;
    ProgressDialog pDialog;
    PreferencesHelper pref;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_merchant);

        //Get Intent
        Intent i = getIntent();
        amount = i.getStringExtra("Cart Hash");
        ship_method = i.getStringExtra("ship_method");

        //Initialise
        pref = new PreferencesHelper(MerchantActivity.this);
        locale = pref.GetPreferences("Location");

        startPayment();
    }

    @Override
    public void onPaymentSuccess(String s) {

        Log.d("Payment", s);
        new ConfirmOrder().execute(s);
    }

    @Override
    public void onPaymentError(int i, String s) {

        Toast.makeText(MerchantActivity.this, "Payment Failed", Toast.LENGTH_SHORT).show();
        Log.d("Payment", "Error");

    }

    public void startPayment() {

        //Instantiate Checkout
        Checkout checkout = new Checkout();


        //Set your logo here
        checkout.setImage(R.drawable.profile);

        //Reference to current activity
        final Activity activity = this;

        //Pass your payment options to the Razorpay Checkout as a JSONObject
        try {
            JSONObject options = new JSONObject();

            options.put("name", "Irine Rose New York");
            options.put("description", "Checkout");
            options.put("currency", "INR");
            options.put("amount", amount);

            checkout.open(activity, options);
        } catch(Exception e) {
            Log.e("Error : ", "Error in starting Razorpay Checkout", e);
        }
    }

    private class ConfirmOrder extends AsyncTask<String, Void, String> {

        @Override
        protected void onPreExecute() {
            super.onPreExecute();

            //Showing progress dialog
            pDialog = new ProgressDialog(MerchantActivity.this);
            pDialog.setMessage("Please wait...");
            pDialog.setCancelable(false);
            pDialog.show();
        }

        @Override
        protected String doInBackground(String... params) {
            try {

                String razorT = params[0];
                String token = pref.GetPreferences("Token");
                URL url = new URL("http://shop.irinerose.com/api/cart/confirm");
                Log.d("Address", String.valueOf(url));

                String billing = pref.GetPreferences("billing");

                String fname_st = pref.GetPreferences("fname_b");
                String lname_st = pref.GetPreferences("lname_b");
                String address1_st =pref.GetPreferences("line1_b");
                String address2_st =pref.GetPreferences("line2_b");
                String city_st =pref.GetPreferences("city_b");
                String pin_st =pref.GetPreferences("pin_b");
                String state_st =pref.GetPreferences("state_b");
                String country_st =pref.GetPreferences("country_b");
                String phone_st =pref.GetPreferences("phone_b");

                JSONObject postDataParams = new JSONObject();

                if(billing.equals("1")) {
                    postDataParams.put("ship_method", ship_method);
                    postDataParams.put("billing", 1);
                    postDataParams.put("firstname", fname_st);
                    postDataParams.put("lastname", lname_st);
                    postDataParams.put("line1", address1_st);
                    postDataParams.put("line2", address2_st);
                    postDataParams.put("phone", phone_st);
                    postDataParams.put("city", city_st);
                    postDataParams.put("pin", pin_st);
                    postDataParams.put("country", country_st);
                    postDataParams.put("state", state_st);
                    postDataParams.put("razorToken", razorT);
                }else{
                    postDataParams.put("razorToken", razorT);
                    postDataParams.put("ship_method", ship_method);
                    postDataParams.put("billing", 0);
                }
                Log.e("params", postDataParams.toString());

                HttpURLConnection conn = (HttpURLConnection) url.openConnection();
                conn.setReadTimeout(15000);     //milliseconds
                conn.setConnectTimeout(15000);  //milliseconds
                conn.setRequestMethod("POST");
                conn.setRequestProperty("token", token);
                conn.setRequestProperty("locale", locale);
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
        protected void onPostExecute(String aVoid) {
            super.onPostExecute(aVoid);

            // Dismiss the progress dialog
            if (pDialog.isShowing())
                pDialog.dismiss();

            Log.d("Test Address", aVoid);
            if (aVoid != null) {
                try {

                    JSONObject obj = new JSONObject(aVoid);
                    String success = obj.getString("success");

                    if (success.equals("true")) {
                        Toast.makeText(MerchantActivity.this, "Order Placed Succesfully", Toast.LENGTH_SHORT).show();
                        pref.SavePreferences("Cart Count", "0");
                        Intent i = new Intent(MerchantActivity.this, HomeActivity.class);
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

}
