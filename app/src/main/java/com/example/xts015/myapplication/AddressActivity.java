package com.example.xts015.myapplication;

import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.os.AsyncTask;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.RadioButton;
import android.widget.TextView;
import android.widget.Toast;

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
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;

import javax.net.ssl.HttpsURLConnection;

public class AddressActivity extends AppCompatActivity {

    ListView addressList;
    String fname_st, lname_st, address1_st, address2_st, city_st, pin_st, state_st, country_st, phone_st;
    ProgressDialog pDialog;
    PreferencesHelper pref;
    JSONParser jParser = new JSONParser();
    ArrayList<HashMap<String, Object>> itemDataList = new ArrayList<>();
    LayoutInflater inflater;
    Button addAddressBtn;
    String type, locale;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_address);

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
        addressList = (ListView) findViewById(R.id.address_list);
        pref = new PreferencesHelper(AddressActivity.this);
        addAddressBtn = (Button) findViewById(R.id.add_address);
        locale = pref.GetPreferences("Location");

        //Add Address onClick
        addAddressBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                ShowDialog();
            }
        });

        new GetAddress().execute("http://shop.irinerose.com/api/user/address");
    }

    private void ShowDialog() {

        //Show Address Dialog
        final Dialog dialog = new Dialog(AddressActivity.this);
        dialog.setContentView(R.layout.address_dialog);
        final EditText fname = (EditText) dialog.findViewById(R.id.firstname_view);
        final EditText lname = (EditText) dialog.findViewById(R.id.secondName_view);
        final EditText address1 = (EditText) dialog.findViewById(R.id.address1_view);
        final EditText address2 = (EditText) dialog.findViewById(R.id.address2_view);
        final EditText city = (EditText) dialog.findViewById(R.id.city_view);
        final EditText pin = (EditText) dialog.findViewById(R.id.pincode_view);
        final EditText state = (EditText) dialog.findViewById(R.id.state_view);
        final EditText country = (EditText) dialog.findViewById(R.id.country_view);
        final EditText phone = (EditText) dialog.findViewById(R.id.phone_view);
        Button submit = (Button) dialog.findViewById(R.id.submit_address_btn);

        //Submit
        submit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                fname_st = fname.getText().toString();
                lname_st = lname.getText().toString();
                address1_st = address1.getText().toString();
                address2_st = address2.getText().toString();
                city_st = city.getText().toString();
                pin_st = pin.getText().toString();
                state_st = state.getText().toString();
                country_st = country.getText().toString();
                phone_st = phone.getText().toString();

                new SubmitAddress().execute();
                dialog.cancel();

            }
        });
        dialog.show();
    }

    private class GetAddress extends AsyncTask<String, Void, Void> {

        @Override
        protected void onPreExecute() {
            super.onPreExecute();

            //Showing progress dialog
            itemDataList.clear();
            pDialog = new ProgressDialog(AddressActivity.this);
            pDialog.setMessage("Please wait...");
            pDialog.setCancelable(false);
            pDialog.show();
        }

        @Override
        protected Void doInBackground(String... params) {

            String url_final = params[0];
            Log.d("Url Final", url_final);
            String token = pref.GetPreferences("Token");
            JSONObject json = jParser.getJSONFromUrlByGet(url_final, token, locale);
            Log.d("Json", String.valueOf(json));

            if (json != null) {

                try {
                    String success = json.getString("success");

                    if (success.equals("true")) {

                        JSONArray addressArray = json.getJSONArray("data");
                        for (int j = 0; j < addressArray.length(); j++) {

                            JSONObject obj = addressArray.getJSONObject(j);

                            String id = obj.getString("aid");
                            String fname = obj.getString("firstname");
                            String lname = obj.getString("lastname");
                            String line1 = obj.getString("line1");
                            String line2 = obj.getString("line2");
                            String city = obj.getString("city");
                            String state = obj.getString("state");
                            String country = obj.getString("country");
                            String phone = obj.getString("phone");
                            String pin = obj.getString("pin");
                            String selected = obj.getString("selected");

                            // adding each child node to HashMap key => value
                            HashMap<String, Object> productMap = new HashMap<String, Object>();
                            productMap.put("Id", id);
                            productMap.put("First Name", fname);
                            productMap.put("Last Name", lname);
                            productMap.put("Line1", line1);
                            productMap.put("Line2", line2);
                            productMap.put("City", city);
                            productMap.put("Pin", pin);
                            productMap.put("State", state);
                            productMap.put("Country", country);
                            productMap.put("Phone", phone);
                            productMap.put("Selected", selected);
                            itemDataList.add(productMap);
                        }

                    }
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }

            return null;
        }

        @Override
        protected void onPostExecute(Void aVoid) {
            super.onPostExecute(aVoid);

            // Dismiss the progress dialog
            if (pDialog.isShowing())
                pDialog.dismiss();

            //Set Delivery Options
            CustomAdapter adapter = new CustomAdapter(AddressActivity.this, R.layout.address_item, itemDataList);
            addressList.setAdapter(adapter);

        }
    }

    private class CustomAdapter extends ArrayAdapter<HashMap<String, Object>> {


        public CustomAdapter(Context context, int textViewResourceId, ArrayList<HashMap<String, Object>> Strings) {

            //let android do the initializing :)
            super(context, textViewResourceId, Strings);
        }

        //class for caching the views in a row
        private class ViewHolder {

            RadioButton select_button;
            TextView fname, lname, line1, line2, city, pin, state, country, phone, id;

        }

        //Initialise
        ViewHolder viewHolder;

        @Override
        public View getView(final int position, View convertView, ViewGroup parent) {

            if (convertView == null) {

                //inflate the custom layout
                convertView = inflater.from(parent.getContext()).inflate(R.layout.address_item, parent, false);
                viewHolder = new ViewHolder();

                //cache the views
                viewHolder.id = (TextView) convertView.findViewById(R.id.address_id);
                viewHolder.fname = (TextView) convertView.findViewById(R.id.firstname_view);
                viewHolder.lname = (TextView) convertView.findViewById(R.id.secondName_view);
                viewHolder.line1 = (TextView) convertView.findViewById(R.id.address1_view);
                viewHolder.line2 = (TextView) convertView.findViewById(R.id.address2_view);
                viewHolder.city = (TextView) convertView.findViewById(R.id.city_view);
                viewHolder.pin = (TextView) convertView.findViewById(R.id.pincode_view);
                viewHolder.state = (TextView) convertView.findViewById(R.id.state_view);
                viewHolder.country = (TextView) convertView.findViewById(R.id.country_view);
                viewHolder.phone = (TextView) convertView.findViewById(R.id.phone_view);
                viewHolder.select_button = (RadioButton) convertView.findViewById(R.id.selected_btn);

                //link the cached views to the convertView
                convertView.setTag(viewHolder);

            } else {
                viewHolder = (ViewHolder) convertView.getTag();
            }

            //set Values
            viewHolder.id.setText(itemDataList.get(position).get("Id").toString());
            viewHolder.fname.setText(itemDataList.get(position).get("First Name").toString());
            viewHolder.lname.setText(itemDataList.get(position).get("Last Name").toString());
            viewHolder.line1.setText(itemDataList.get(position).get("Line1").toString());
            viewHolder.line2.setText(itemDataList.get(position).get("Line2").toString());
            viewHolder.city.setText(itemDataList.get(position).get("City").toString());
            viewHolder.pin.setText(itemDataList.get(position).get("Pin").toString());
            viewHolder.state.setText(itemDataList.get(position).get("State").toString());
            viewHolder.country.setText(itemDataList.get(position).get("Country").toString());
            viewHolder.phone.setText(itemDataList.get(position).get("Phone").toString());

            String selected_st = itemDataList.get(position).get("Selected").toString();
            if (selected_st.equals("1")) {
                viewHolder.select_button.setChecked(true);
            } else {
                viewHolder.select_button.setChecked(false);
            }

            viewHolder.select_button.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {

                    String address_id = itemDataList.get(position).get("Id").toString();
                    new SetAddress().execute(address_id);
                }
            });

            return convertView;
        }
    }

    private class SubmitAddress extends AsyncTask<Void, Void, String> {

        @Override
        protected void onPreExecute() {
            super.onPreExecute();

            //Showing progress dialog
            pDialog = new ProgressDialog(AddressActivity.this);
            pDialog.setMessage("Please wait...");
            pDialog.setCancelable(false);
            pDialog.show();
        }

        @Override
        protected String doInBackground(Void... params) {
            try {

                String token = pref.GetPreferences("Token");
                URL url = new URL("http://shop.irinerose.com/api/user/address/add");
                Log.d("Address", String.valueOf(url));

                JSONObject postDataParams = new JSONObject();
                postDataParams.put("firstname", fname_st);
                postDataParams.put("lastname", lname_st);
                postDataParams.put("line1", address1_st);
                postDataParams.put("line2", address2_st);
                postDataParams.put("phone", phone_st);
                postDataParams.put("city", city_st);
                postDataParams.put("pin", pin_st);
                postDataParams.put("country", country_st);
                postDataParams.put("state", state_st);
                Log.e("params", postDataParams.toString());

                HttpURLConnection conn = (HttpURLConnection) url.openConnection();
                conn.setReadTimeout(15000);     //milliseconds
                conn.setConnectTimeout(15000);  //milliseconds
                conn.setRequestMethod("POST");
                conn.setRequestProperty("token", token);
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
                        new GetAddress().execute("http://shop.irinerose.com/api/user/address");
                    }

                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    private class SetAddress extends AsyncTask<String, Void, String> {

        @Override
        protected void onPreExecute() {
            super.onPreExecute();

            //Showing progress dialog
            pDialog = new ProgressDialog(AddressActivity.this);
            pDialog.setMessage("Please wait...");
            pDialog.setCancelable(false);
            pDialog.show();
        }

        @Override
        protected String doInBackground(String... params) {
            try {

                String id = params[0];
                String token = pref.GetPreferences("Token");
                URL url = new URL("http://shop.irinerose.com/api/user/address/set");
                Log.d("Address", String.valueOf(url));

                JSONObject postDataParams = new JSONObject();
                postDataParams.put("aid", id);
                Log.e("params", postDataParams.toString());

                HttpURLConnection conn = (HttpURLConnection) url.openConnection();
                conn.setReadTimeout(15000);     //milliseconds
                conn.setConnectTimeout(15000);  //milliseconds
                conn.setRequestMethod("POST");
                conn.setRequestProperty("token", token);
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
                        new GetAddress().execute("http://shop.irinerose.com/api/user/address");
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
