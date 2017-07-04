package com.example.xts015.myapplication;

import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.os.AsyncTask;
import android.support.v4.view.PagerAdapter;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListAdapter;
import android.widget.ListView;
import android.widget.RadioButton;
import android.widget.TextView;
import android.widget.Toast;

import com.razorpay.Checkout;
import com.squareup.picasso.Picasso;

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

public class CheckoutActivity extends AppCompatActivity {

    String url = "http://shop.irinerose.com/api/cart/validate";
    ProgressDialog pDialog;
    PreferencesHelper pref;
    JSONParser jParser = new JSONParser();
    ArrayList<HashMap<String, Object>> itemDataList = new ArrayList<>();
    ArrayList<HashMap<String, Object>> shippingList = new ArrayList<>();
    String cartCount, subTotal, cartTotal, coupon, tax, discount, shipCharge;
    ViewPager pager;
    TextView itemCount_view, subTotal_view, tax_view, discount_view, cartTotal_view, charge_view, bottomSubTotal_view, coupon_view;
    ImageButton promocode_btn;
    LayoutInflater inflater;
    ListView deliveryList;
    LinearLayout homeAddressLayout, billingAddressLayout;
    TextView noHomeAddressView, noBillingAddressView;
    Boolean noAddress;
    Button addHomeA, changeHomeA, addBillA, changeBillA, payBtn;
    TextView fname, lname, address1, address2, city, pin, state, country, phone;
    TextView fname_b, lname_b, address1_b, address2_b, city_b, pin_b, state_b, country_b, phone_b;
    String fname_json, lname_json, line1_json, line2_json, city_json, pin_json, state_json, country_json, phone_json;
    String ship_method = "nil";
    String cart_hash, locale;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_checkout);

        //Toolbar set title
        Toolbar toolbar_nologo = (Toolbar) findViewById(R.id.toolbar_nologo);
        TextView toolbar_title = (TextView) toolbar_nologo.findViewById(R.id.title_toolbar);
        toolbar_title.setText("");

        //Count Badge and Cart Button
        TextView badge = (TextView) toolbar_nologo.findViewById(R.id.textOne);
        ImageView cart = (ImageView) toolbar_nologo.findViewById(R.id.cart_view);
        badge.setVisibility(View.GONE);
        cart.setVisibility(View.GONE);

        //Preload
        Checkout.preload(getApplicationContext());

        //Initialise
        pref = new PreferencesHelper(CheckoutActivity.this);
        pager = (ViewPager) findViewById(R.id.viewpager_preview);
        itemCount_view = (TextView) findViewById(R.id.itemcount_view);
        subTotal_view = (TextView) findViewById(R.id.totalView);
        payBtn = (Button) findViewById(R.id.pay_btn);
        locale = pref.GetPreferences("Location");

        deliveryList = (ListView) findViewById(R.id.list_options);
        deliveryList.setChoiceMode(ListView.CHOICE_MODE_SINGLE);

        charge_view = (TextView) findViewById(R.id.delivery_view);
        tax_view = (TextView) findViewById(R.id.tax_view);
        discount_view = (TextView) findViewById(R.id.discount_view);
        cartTotal_view = (TextView) findViewById(R.id.total_view);
        bottomSubTotal_view = (TextView) findViewById(R.id.subtotal_view);
        promocode_btn = (ImageButton) findViewById(R.id.promocodeButton);
        coupon_view = (TextView) findViewById(R.id.coupon_status);
        homeAddressLayout = (LinearLayout) findViewById(R.id.homme_address_layout);
        billingAddressLayout = (LinearLayout) findViewById(R.id.billing_address_layout);
        noHomeAddressView = (TextView) findViewById(R.id.no_home_address_view);
        noBillingAddressView = (TextView) findViewById(R.id.no_billing_address_view);
        addHomeA = (Button) findViewById(R.id.add_button);
        changeHomeA = (Button) findViewById(R.id.change_button);
        addBillA = (Button) findViewById(R.id.add_buttonB);
        changeBillA = (Button) findViewById(R.id.change_buttonB);

        fname = (TextView) findViewById(R.id.firstname_view);
        lname = (TextView) findViewById(R.id.secondName_view);
        address1 = (TextView) findViewById(R.id.address1_view);
        address2 = (TextView) findViewById(R.id.address2_view);
        city = (TextView) findViewById(R.id.city_view);
        pin = (TextView) findViewById(R.id.pincode_view);
        state = (TextView) findViewById(R.id.state_view);
        country = (TextView) findViewById(R.id.country_view);
        phone = (TextView) findViewById(R.id.phone_view);

        fname_b = (TextView) findViewById(R.id.firstname_viewB);
        lname_b = (TextView) findViewById(R.id.secondName_viewB);
        address1_b = (TextView) findViewById(R.id.address1_viewB);
        address2_b = (TextView) findViewById(R.id.address2_viewB);
        city_b = (TextView) findViewById(R.id.city_viewB);
        pin_b = (TextView) findViewById(R.id.pincode_viewB);
        state_b = (TextView) findViewById(R.id.state_viewB);
        country_b = (TextView) findViewById(R.id.country_viewB);
        phone_b = (TextView) findViewById(R.id.phone_viewB);


        //onClick of Promocode Button
        promocode_btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                final Dialog dialog = new Dialog(CheckoutActivity.this);
                dialog.setContentView(R.layout.coupon_dialog);

                final EditText couponBox = (EditText) dialog.findViewById(R.id.coupn_text);
                Button applyBtn = (Button) dialog.findViewById(R.id.apply_btn);

                applyBtn.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {

                        String couponStr = couponBox.getText().toString();
                        new ApplyCoupon().execute(couponStr);
                        dialog.cancel();
                    }
                });
                dialog.show();
            }
        });

        //onClick Add Home Address
        addHomeA.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                Intent i = new Intent(CheckoutActivity.this, AddressActivity.class);
                startActivity(i);
            }
        });

        //onClick Change Billing Address
        changeBillA.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                ShowDialog();

            }
        });

        //onClick of Pay Button
        payBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                if(ship_method.equals("nil")){
                    Toast.makeText(CheckoutActivity.this, "Please Select any delivery Option", Toast.LENGTH_SHORT).show();
                }else{
                    if(cartTotal.equals("$0")){
                        new ConfirmOrder().execute();
                    }else{
                        Intent i = new Intent(CheckoutActivity.this, MerchantActivity.class);
                        i.putExtra("Cart Hash", cart_hash);
                        i.putExtra("ship_method", ship_method);
                        startActivity(i);
                    }
                }
            }
        });

        new GetProducts().execute(url);

    }

    private void ShowDialog() {

        //Show Address Dialog
        final Dialog dialog = new Dialog(CheckoutActivity.this);
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

        //Set Current values
        fname.setText(fname_json);
        lname.setText(lname_json);
        address1.setText(line1_json);
        address2.setText(line2_json);
        city.setText(city_json);
        pin.setText(pin_json);
        state.setText(state_json);
        country.setText(country_json);
        phone.setText(phone_json);

        //Submit
        submit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                //Get New Values
                String fname_st = fname.getText().toString();
                String lname_st = lname.getText().toString();
                String address1_st = address1.getText().toString();
                String address2_st = address2.getText().toString();
                String city_st = city.getText().toString();
                String pin_st = pin.getText().toString();
                String state_st = state.getText().toString();
                String country_st = country.getText().toString();
                String phone_st = phone.getText().toString();

                //Save New Values
                pref.SavePreferences("billing","1");
                pref.SavePreferences("fname_b", fname_st);
                pref.SavePreferences("lname_b", lname_st);
                pref.SavePreferences("line1_b", address1_st);
                pref.SavePreferences("line2_b", address2_st);
                pref.SavePreferences("city_b", city_st);
                pref.SavePreferences("pin_b", pin_st);
                pref.SavePreferences("state_b", state_st);
                pref.SavePreferences("country_b", country_st);
                pref.SavePreferences("phone_b", phone_st);

                //Set New Values
                fname_b.setText(fname_st);
                lname_b.setText(lname_st);
                address1_b.setText(address1_st);
                address2_b.setText(address2_st);
                city_b.setText(city_st);
                pin_b.setText(pin_st);
                state_b.setText(state_st);
                country_b.setText(country_st);
                phone_b.setText(phone_st);

                dialog.cancel();

            }
        });
        dialog.show();
    }

    private class ConfirmOrder extends AsyncTask<Void, Void, String> {

        @Override
        protected void onPreExecute() {
            super.onPreExecute();

            //Showing progress dialog
            pDialog = new ProgressDialog(CheckoutActivity.this);
            pDialog.setMessage("Please wait...");
            pDialog.setCancelable(false);
            pDialog.show();
        }

        @Override
        protected String doInBackground(Void... params) {
            try {

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
                }else{
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
                        Toast.makeText(CheckoutActivity.this, "Order Placed Succesfully", Toast.LENGTH_SHORT).show();
                        pref.SavePreferences("Cart Count", "0");
                        Intent i = new Intent(CheckoutActivity.this, HomeActivity.class);
                        startActivity(i);
                    }

                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    private class ApplyCoupon extends AsyncTask<String, Object, String> {

        @Override
        protected void onPreExecute() {
            super.onPreExecute();

            //Showing progress dialog
            pDialog = new ProgressDialog(CheckoutActivity.this);
            pDialog.setMessage("Please wait...");
            pDialog.setCancelable(false);
            pDialog.show();
        }

        @Override
        protected String doInBackground(String... params) {
            try {

                String code = params[0];
                String token = pref.GetPreferences("Token");
                URL url = new URL("http://shop.irinerose.com/api/cart/coupon/apply");

                JSONObject postDataParams = new JSONObject();
                postDataParams.put("code", code);
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

            Log.d("Test Coupon", aVoid);
            if (aVoid != null) {
                try {

                    JSONObject obj = new JSONObject(aVoid);
                    String success = obj.getString("success");

                    if (success.equals("true")) {
                        Toast.makeText(CheckoutActivity.this, "Coupon Applied", Toast.LENGTH_SHORT).show();
                        itemDataList.clear();
                        shippingList.clear();
                        new GetProducts().execute(url);
                    }

                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    private class GetProducts extends AsyncTask<String, Void, Void> {

        @Override
        protected void onPreExecute() {
            super.onPreExecute();

            //Showing progress dialog
            pDialog = new ProgressDialog(CheckoutActivity.this);
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
                        JSONArray itemsArray = json.getJSONArray("items");
                        for (int i = 0; i < itemsArray.length(); i++) {

                            JSONObject itemObj = itemsArray.getJSONObject(i);

                            String id = itemObj.getString("id");
                            String thumb = itemObj.getString("thumb");

                            // adding each child node to HashMap key => value
                            HashMap<String, Object> productMap = new HashMap<String, Object>();
                            productMap.put("Id", id);
                            productMap.put("Thumbnail", thumb);
                            itemDataList.add(productMap);
                        }

                        cart_hash = json.getString("cart_hash");
                        cartCount = json.getString("cart_count");
                        subTotal = json.getString("sub_total");
                        cartTotal = json.getString("cart_total");
                        coupon = json.getString("coupon");
                        tax = json.getString("tax");
                        discount = json.getString("discount");
                        shipCharge = json.getString("ship_charge");

                        JSONArray shippingArray = json.getJSONArray("ship_method");
                        for (int j = 0; j < shippingArray.length(); j++) {

                            JSONObject obj = shippingArray.getJSONObject(j);

                            String id = obj.getString("id");
                            String name = obj.getString("name");
                            String charge = obj.getString("charge");
                            String desc = obj.getString("description");

                            // adding each child node to HashMap key => value
                            HashMap<String, Object> productMap = new HashMap<String, Object>();
                            productMap.put("Id", id);
                            productMap.put("Name", name);
                            productMap.put("Charge", charge);
                            productMap.put("Desc", desc);
                            shippingList.add(productMap);
                        }

                        JSONObject shipAddress = json.getJSONObject("ship_address");
                        String addressSuccess = shipAddress.getString("success");
                        if (addressSuccess.equals("false")) {
                            noAddress = true;
                        } else {
                            noAddress = false;
                            JSONObject dataObj = shipAddress.getJSONObject("data");
                            fname_json = dataObj.getString("firstname");
                            lname_json = dataObj.getString("lastname");
                            line1_json = dataObj.getString("line1");
                            line2_json = dataObj.getString("line2");
                            city_json = dataObj.getString("city");
                            pin_json = dataObj.getString("pin");
                            state_json = dataObj.getString("state");
                            country_json = dataObj.getString("country");
                            phone_json = dataObj.getString("phone");

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

            //Set Preview thumbnails
            CheckListAdapter mAdapter = new CheckListAdapter(CheckoutActivity.this);
            pager.setAdapter(mAdapter);
            pager.setPageMargin(4);

            //Set Address
            if (noAddress) {
                homeAddressLayout.setVisibility(View.GONE);
                billingAddressLayout.setVisibility(View.GONE);
                noHomeAddressView.setVisibility(View.VISIBLE);
                noBillingAddressView.setVisibility(View.VISIBLE);
                changeHomeA.setEnabled(false);
                changeBillA.setEnabled(false);
            } else {
                homeAddressLayout.setVisibility(View.VISIBLE);
                billingAddressLayout.setVisibility(View.VISIBLE);
                noHomeAddressView.setVisibility(View.GONE);
                noBillingAddressView.setVisibility(View.GONE);
                changeHomeA.setEnabled(true);
                changeBillA.setEnabled(true);

                //Set TextView values
                fname.setText(fname_json);
                lname.setText(lname_json);
                address1.setText(line1_json);
                address2.setText(line2_json);
                city.setText(city_json);
                pin.setText(pin_json);
                state.setText(state_json);
                country.setText(country_json);
                phone.setText(phone_json);

                fname_b.setText(fname_json);
                lname_b.setText(lname_json);
                address1_b.setText(line1_json);
                address2_b.setText(line2_json);
                city_b.setText(city_json);
                pin_b.setText(pin_json);
                state_b.setText(state_json);
                country_b.setText(country_json);
                phone_b.setText(phone_json);

            }

            //Set Button Text
            if (cartTotal.equals("$0")) {
                payBtn.setText("Confirm Order");
            } else {
                payBtn.setText("Pay Securely");
            }

            //Set TextView values
            itemCount_view.setText(cartCount + " item");
            subTotal_view.setText(subTotal);
            charge_view.setText(shipCharge);
            tax_view.setText(tax);
            discount_view.setText(discount);
            cartTotal_view.setText(cartTotal);
            bottomSubTotal_view.setText(subTotal);
            if (coupon.equals("0")) {
                coupon_view.setVisibility(View.GONE);
            } else {
                coupon_view.setVisibility(View.VISIBLE);
                coupon_view.setText("Coupon Applied : " + coupon);
            }

            //Set Delivery Options
            CustomAdapter adapter = new CustomAdapter(CheckoutActivity.this, R.layout.delivery_layout, shippingList);
            deliveryList.setAdapter(adapter);
            UIUtils.setListViewHeightBasedOnItems(deliveryList);

        }
    }

    class CheckListAdapter extends PagerAdapter {

        Context mContext;
        LayoutInflater mLayoutInflater;

        public CheckListAdapter(Context context) {
            mContext = context;
            mLayoutInflater = (LayoutInflater) mContext.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        }

        @Override
        public int getCount() {
            return itemDataList.size();
        }

        @Override
        public int getItemPosition(Object object) {
            return super.getItemPosition(object);
        }

        @Override
        public boolean isViewFromObject(View view, Object object) {
            return view == ((LinearLayout) object);
        }

        @Override
        public Object instantiateItem(ViewGroup container, final int position) {

            View v = mLayoutInflater.inflate(R.layout.similar_product_layout, container, false);
            ImageView view_thumbnail = (ImageView) v.findViewById(R.id.thumbnail);
            final TextView view_id = (TextView) v.findViewById(R.id.textView_id);
            TextView view_name = (TextView) v.findViewById(R.id.textView_name);
            TextView view_price = (TextView) v.findViewById(R.id.textView_price);

            //set Values
            view_id.setVisibility(View.GONE);
            view_name.setVisibility(View.GONE);
            view_price.setVisibility(View.GONE);

            //Load Thumbnail
            Picasso.with(CheckoutActivity.this)
                    .load(itemDataList.get(position).get("Thumbnail").toString())
                    .into(view_thumbnail);
            container.addView(v);

            return v;
        }

        @Override
        public void destroyItem(ViewGroup container, int position, Object object) {
            container.removeView((LinearLayout) object);
        }
    }

    private class CustomAdapter extends ArrayAdapter<HashMap<String, Object>> {

        private RadioButton mSelectedRB;
        private int mSelectedPosition = -1;

        public CustomAdapter(Context context, int textViewResourceId, ArrayList<HashMap<String, Object>> Strings) {

            //let android do the initializing :)
            super(context, textViewResourceId, Strings);
        }

        //class for caching the views in a row
        private class ViewHolder {

            RadioButton select_button;
            TextView view_method;
            TextView view_name;
            TextView view_desc, view_id;
        }

        //Initialise
        ViewHolder viewHolder;

        @Override
        public View getView(final int position, View convertView, ViewGroup parent) {

            if (convertView == null) {

                //inflate the custom layout
                convertView = inflater.from(parent.getContext()).inflate(R.layout.delivery_layout, parent, false);
                viewHolder = new ViewHolder();

                //cache the views
                viewHolder.view_id = (TextView) convertView.findViewById(R.id.shipping_id);
                viewHolder.view_method = (TextView) convertView.findViewById(R.id.method_view);
                viewHolder.view_desc = (TextView) convertView.findViewById(R.id.desc_view);
                viewHolder.view_name = (TextView) convertView.findViewById(R.id.name_view);
                viewHolder.select_button = (RadioButton) convertView.findViewById(R.id.radioButton);

                //link the cached views to the convertView
                convertView.setTag(viewHolder);

            } else {
                viewHolder = (ViewHolder) convertView.getTag();
            }

            //set Values
            viewHolder.view_id.setText(shippingList.get(position).get("Id").toString());
            viewHolder.view_desc.setText(shippingList.get(position).get("Desc").toString());
            viewHolder.view_name.setText(shippingList.get(position).get("Name").toString());
            viewHolder.view_method.setText(shippingList.get(position).get("Charge").toString());

            viewHolder.select_button.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {

                    Log.d("Id Shipping", shippingList.get(position).get("Id").toString());
                    ship_method = shippingList.get(position).get("Id").toString();
                    if (position != mSelectedPosition && mSelectedRB != null) {
                        mSelectedRB.setChecked(false);
                    }

                    mSelectedPosition = position;
                    mSelectedRB = (RadioButton) v;
                }
            });


            if (mSelectedPosition != position) {
                viewHolder.select_button.setChecked(false);
            } else {
                viewHolder.select_button.setChecked(true);
                if (mSelectedRB != null && viewHolder.select_button != mSelectedRB) {
                    mSelectedRB = viewHolder.select_button;
                }
            }

            return convertView;
        }
    }

    public static class UIUtils {

        /**
         * Sets ListView height dynamically based on the height of the items.
         *
         * @param listView to be resized
         * @return true if the listView is successfully resized, false otherwise
         */
        public static boolean setListViewHeightBasedOnItems(ListView listView) {

            ListAdapter listAdapter = listView.getAdapter();
            if (listAdapter != null) {

                int numberOfItems = listAdapter.getCount();

                // Get total height of all items.
                int totalItemsHeight = 0;
                for (int itemPos = 0; itemPos < numberOfItems; itemPos++) {
                    View item = listAdapter.getView(itemPos, null, listView);
                    item.measure(0, 0);
                    totalItemsHeight += item.getMeasuredHeight();
                }

                // Get total height of all item dividers.
                int totalDividersHeight = listView.getDividerHeight() *
                        (numberOfItems - 1);

                // Set list height.
                ViewGroup.LayoutParams params = listView.getLayoutParams();
                params.height = totalItemsHeight + totalDividersHeight;
                listView.setLayoutParams(params);
                listView.requestLayout();

                return true;

            } else {
                return false;
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


