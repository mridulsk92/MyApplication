package com.example.xts015.myapplication;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.graphics.Typeface;
import android.os.AsyncTask;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

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

import me.himanshusoni.quantityview.QuantityView;
import uk.co.chrisjenx.calligraphy.CalligraphyContextWrapper;

public class CartActivity extends AppCompatActivity {

    ListView checkoutList;
    TextView countText, totalText;
    Button checkOutBtn;
    ProgressDialog pDialog;
    PreferencesHelper pref;
    JSONParser jParser = new JSONParser();
    String url = "http://shop.irinerose.com/api/user/cart";
    ArrayList<HashMap<String, Object>> itemDataList = new ArrayList<>();
    LayoutInflater inflater;
    Typeface font, font_bold;
    String total_count, total_amt;
    String id, locale;
    int qty_final;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_cart);

        //Toolbar set title
        Toolbar toolbar_nologo = (Toolbar) findViewById(R.id.toolbar_nologo);
        TextView toolbar_title = (TextView) toolbar_nologo.findViewById(R.id.title_toolbar);
        toolbar_title.setText("");

        //Get Intent
        Intent i = getIntent();
        id = i.getStringExtra("Id");

        //Toolbar back Button
        ImageButton back = (ImageButton) toolbar_nologo.findViewById(R.id.button_back);
        back.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (id.equals("Home")) {
                    Intent i = new Intent(CartActivity.this, HomeActivity.class);
                    startActivity(i);
                } else if (id.equals("NewArrival")) {
                    Intent i = new Intent(CartActivity.this, NewArrivalActivity.class);
                    i.putExtra("Source", "New Arrival");
                    startActivity(i);
                } else {
                    Intent i = new Intent(CartActivity.this, ProductActivity.class);
                    i.putExtra("Product Id", id);
                    startActivity(i);
                }
            }
        });

        //Count Badge and Cart Button
        TextView badge = (TextView) toolbar_nologo.findViewById(R.id.textOne);
        badge.setVisibility(View.GONE);

        //Initialise
        font = Typeface.createFromAsset(CartActivity.this.getAssets(), getString(R.string.font));
        font_bold = Typeface.createFromAsset(CartActivity.this.getAssets(), getString(R.string.font_bold));
        countText = (TextView) findViewById(R.id.item_count);
        checkoutList = (ListView) findViewById(R.id.cartitem_list);
        totalText = (TextView) findViewById(R.id.total_view);
        checkOutBtn = (Button) findViewById(R.id.btn_checkout);
        pref = new PreferencesHelper(CartActivity.this);
        locale = pref.GetPreferences("Location");

        //Access Cart
        new AccessCart().execute();

        //onClick of CheckOut Button
        checkOutBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                Intent i = new Intent(CartActivity.this, CheckoutActivity.class);
                startActivity(i);
            }
        });

    }

    private class AccessCart extends AsyncTask<Void, Void, Void> {

        @Override
        protected void onPreExecute() {
            super.onPreExecute();

            // Showing progress dialog
            pDialog = new ProgressDialog(CartActivity.this);
            pDialog.setMessage("Please wait...");
            pDialog.setCancelable(false);
            pDialog.show();
        }

        @Override
        protected Void doInBackground(Void... params) {

            String token = pref.GetPreferences("Token");
            JSONObject json = jParser.getJSONFromUrlByGet(url, token, locale);
            Log.d("Json", String.valueOf(json));

            try {
                String success = json.getString("success");
                total_amt = json.getString("cart_total");
                total_count = json.getString("cart_count");

                JSONArray itemArray = json.getJSONArray("items");
                for (int i = 0; i < itemArray.length(); i++) {

                    JSONObject itemObj = itemArray.getJSONObject(i);

                    String id = itemObj.getString("id");
                    String name = itemObj.getString("name");
                    String thumb = itemObj.getString("thumb");
                    String price = itemObj.getString("sub_total");
                    int qty = itemObj.getInt("qty");

                    // adding each child node to HashMap key => value
                    HashMap<String, Object> productMap = new HashMap<String, Object>();
                    productMap.put("Id", id);
                    productMap.put("Name", name);
                    productMap.put("Price", price);
                    productMap.put("Thumbnail", thumb);
                    productMap.put("Quantity", qty);
                    itemDataList.add(productMap);
                }

            } catch (JSONException e) {
                e.printStackTrace();
            }

            return null;
        }

        @Override
        protected void onPostExecute(Void aVoid) {
            super.onPostExecute(aVoid);

            if (pDialog.isShowing()) {
                pDialog.dismiss();
            }

            //SetText Values
            countText.setText("(" + total_count + ")");
            totalText.setText(total_amt);

            CustomAdapter adapter = new CustomAdapter(CartActivity.this, R.layout.checkout_item, itemDataList);
            checkoutList.setAdapter(adapter);
        }
    }

    private class UpdateQuantity extends AsyncTask<String, Object, String> {

        @Override
        protected void onPreExecute() {
            super.onPreExecute();

            // Showing progress dialog
            pDialog = new ProgressDialog(CartActivity.this);
            pDialog.setMessage("Please Wait");
            pDialog.setCancelable(false);
            pDialog.show();
        }

        @Override
        protected String doInBackground(String... params) {

            String id = params[0];
            try {

                URL url = new URL("http://shop.irinerose.com/api/user/cart/update");

                JSONObject postDataParams = new JSONObject();
                postDataParams.put("id", id);
                postDataParams.put("qty", qty_final);
                Log.e("params", postDataParams.toString());

                String token = pref.GetPreferences("Token");

                HttpURLConnection conn = (HttpURLConnection) url.openConnection();
                conn.setReadTimeout(15000 /* milliseconds */);
                conn.setConnectTimeout(15000 /* milliseconds */);
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

            if (pDialog.isShowing()) {
                pDialog.dismiss();
            }

            Log.d("Test Update", aVoid);
            if (aVoid != null) {
                try {

                    JSONObject obj = new JSONObject(aVoid);
                    String success = obj.getString("success");

                    JSONObject dataObj = obj.getJSONObject("data");
                    String count = dataObj.getString("count");
                    if (success.equals("true")) {
                        pref.SavePreferences("Cart Count", count);
                        new AccessCart().execute();
                    }

                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    private class DeleteItem extends AsyncTask<String, Object, String> {

        @Override
        protected void onPreExecute() {
            super.onPreExecute();

            // Showing progress dialog
            pDialog = new ProgressDialog(CartActivity.this);
            pDialog.setMessage("Please Wait");
            pDialog.setCancelable(false);
            pDialog.show();
        }

        @Override
        protected String doInBackground(String... params) {

            String id = params[0];
            try {

                URL url = new URL("http://shop.irinerose.com/api/user/cart/remove");

                JSONObject postDataParams = new JSONObject();
                postDataParams.put("id", id);
                Log.e("params", postDataParams.toString());

                String token = pref.GetPreferences("Token");

                HttpURLConnection conn = (HttpURLConnection) url.openConnection();
                conn.setReadTimeout(15000 /* milliseconds */);
                conn.setConnectTimeout(15000 /* milliseconds */);
                conn.setRequestMethod("DELETE");
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

            if (pDialog.isShowing()) {
                pDialog.dismiss();
            }

            Log.d("Test Delete", aVoid);
            if (aVoid != null) {
                try {

                    JSONObject obj = new JSONObject(aVoid);
                    String success = obj.getString("success");

                    JSONObject dataObj = obj.getJSONObject("data");
                    String count = dataObj.getString("count");
                    if (success.equals("true")) {
                        pref.SavePreferences("Cart Count", count);
                        new AccessCart().execute();
                    }

                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    private class MoveItem extends AsyncTask<String, Object, String> {

        @Override
        protected void onPreExecute() {
            super.onPreExecute();

            // Showing progress dialog
            pDialog = new ProgressDialog(CartActivity.this);
            pDialog.setMessage("Please Wait");
            pDialog.setCancelable(false);
            pDialog.show();
        }

        @Override
        protected String doInBackground(String... params) {

            String id = params[0];
            try {

                URL url = new URL("http://shop.irinerose.com/api/user/wishlist/move");

                JSONObject postDataParams = new JSONObject();
                postDataParams.put("id", id);
                Log.e("params", postDataParams.toString());

                String token = pref.GetPreferences("Token");

                HttpURLConnection conn = (HttpURLConnection) url.openConnection();
                conn.setReadTimeout(15000 /* milliseconds */);
                conn.setConnectTimeout(15000 /* milliseconds */);
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

            if (pDialog.isShowing()) {
                pDialog.dismiss();
            }

            Log.d("Test Login", aVoid);
            if (aVoid != null) {
                try {

                    JSONObject obj = new JSONObject(aVoid);
                    String success = obj.getString("success");

//                    JSONObject dataObj = obj.getJSONObject("data");
//                    String count = dataObj.getString("count");
                    if (success.equals("true")) {
//                        pref.SavePreferences("Cart Count", count);
                        new AccessCart().execute();
                    }

                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    private class CustomAdapter extends ArrayAdapter<HashMap<String, Object>> {

        public CustomAdapter(Context context, int textViewResourceId, ArrayList<HashMap<String, Object>> Strings) {

            //let android do the initializing
            super(context, textViewResourceId, Strings);
        }

        //class for caching the views in a row
        private class ViewHolder {

            ImageView view_thumbnail;
            TextView view_id;
            TextView view_name;
            TextView view_price;
            QuantityView qty;
            ImageView deleteItem;
            Button saveButton;
        }

        //Initialise
        ViewHolder viewHolder;

        @Override
        public View getView(final int position, View convertView, ViewGroup parent) {

            if (convertView == null) {

                //inflate the custom layout
                convertView = inflater.from(parent.getContext()).inflate(R.layout.checkout_item, parent, false);
                viewHolder = new ViewHolder();

                //cache the views
                viewHolder.view_thumbnail = (ImageView) convertView.findViewById(R.id.img_preview);
                viewHolder.view_id = (TextView) convertView.findViewById(R.id.id_view);
                viewHolder.view_name = (TextView) convertView.findViewById(R.id.name_view);
                viewHolder.view_price = (TextView) convertView.findViewById(R.id.price_view);
                viewHolder.qty = (QuantityView) convertView.findViewById(R.id.quantityView_default);
                viewHolder.deleteItem = (ImageView) convertView.findViewById(R.id.close_view);
                viewHolder.saveButton = (Button) convertView.findViewById(R.id.save_btn);

                //link the cached views to the convertview
                convertView.setTag(viewHolder);

            } else {
                viewHolder = (ViewHolder) convertView.getTag();
            }

            //set Values
            viewHolder.view_id.setText(itemDataList.get(position).get("Id").toString());
            viewHolder.view_name.setText(itemDataList.get(position).get("Name").toString());
            viewHolder.view_price.setText(itemDataList.get(position).get("Price").toString());
            viewHolder.qty.setQuantity((Integer) itemDataList.get(position).get("Quantity"));
            viewHolder.view_name.setTypeface(font);
            viewHolder.view_price.setTypeface(font_bold);
            viewHolder.qty.setMaxQuantity(5);

            //Update
            viewHolder.qty.setOnQuantityChangeListener(new QuantityView.OnQuantityChangeListener() {
                @Override
                public void onQuantityChanged(int oldQuantity, int newQuantity, boolean programmatically) {

                    String id = itemDataList.get(position).get("Id").toString();
                    qty_final = newQuantity;
                    Log.d("TEST CART", id + " : " + qty_final);
                    itemDataList.clear();
                    new UpdateQuantity().execute(id);
                }

                @Override
                public void onLimitReached() {

                }
            });

            //Delete
            viewHolder.deleteItem.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {

                    String id = itemDataList.get(position).get("Id").toString();
                    itemDataList.clear();
                    new DeleteItem().execute(id);

                }
            });

            //Move to Whishlist
            viewHolder.saveButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {

                    String id = itemDataList.get(position).get("Id").toString();
                    itemDataList.clear();
                    new MoveItem().execute(id);
                }
            });

            //Load Thumbnail
            Picasso.with(CartActivity.this)
                    .load(itemDataList.get(position).get("Thumbnail").toString())
                    .into(viewHolder.view_thumbnail);
//            view_thumbnail.setScaleType(ImageView.ScaleType.FIT_CENTER);


            //link the cached views to the convertview
//            convertView.setTag(viewHolder);

            return convertView;
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

        if (id.equals("Home")) {
            Intent i = new Intent(CartActivity.this, HomeActivity.class);
            startActivity(i);
        } else if (id.equals("NewArrival")) {
            Intent i = new Intent(CartActivity.this, NewArrivalActivity.class);
            i.putExtra("Source", "New Arrival");
            startActivity(i);
        } else {
            Intent i = new Intent(CartActivity.this, ProductActivity.class);
            i.putExtra("Product Id", id);
            startActivity(i);
        }
    }

//    @Override
//    protected void onPause() {
//        super.onPause();
//        super.finish();
//    }
}