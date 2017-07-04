package com.example.xts015.myapplication;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.graphics.Paint;
import android.os.AsyncTask;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.RadioButton;
import android.widget.TextView;
import android.widget.Toast;

import com.daimajia.slider.library.SliderTypes.DefaultSliderView;
import com.squareup.picasso.Picasso;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;

public class OrderActivity extends AppCompatActivity {

    ListView orderList;
    ProgressDialog pDialog;
    PreferencesHelper pref;
    JSONParser jParser = new JSONParser();
    ArrayList<HashMap<String, Object>> dataList = new ArrayList<>();
    ArrayList<HashMap<String, Object>> itemList = new ArrayList<>();
    String url = "http://shop.irinerose.com/api/user/orders";
    LayoutInflater inflater, inf;
    String locale;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_order);

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
        pref = new PreferencesHelper(OrderActivity.this);
        orderList = (ListView) findViewById(R.id.order_list);
        locale = pref.GetPreferences("Location");

        new LoadOrders().execute(url);

    }

    private class LoadOrders extends AsyncTask<String, Void, Void> {

        @Override
        protected void onPreExecute() {
            super.onPreExecute();

            // Showing progress dialog
            pDialog = new ProgressDialog(OrderActivity.this);
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
            try {

                String success = json.getString("success");

                if (success.equals("true")) {

                    JSONObject dataObj = json.getJSONObject("data");
                    JSONArray orderObjArray = dataObj.getJSONArray("orders");
                    for (int i = 0; i < orderObjArray.length(); i++) {
                        JSONObject orderObj = orderObjArray.getJSONObject(i);

                        String order_id = orderObj.getString("id");
                        String cart_total = orderObj.getString("cart_total");
                        String status_msg = orderObj.getString("status_msg");
                        String order_qty = orderObj.getString("qty");
                        String status = orderObj.getString("status");

                        // adding each child node to HashMap key => value
                        HashMap<String, Object> productMap = new HashMap<String, Object>();
                        productMap.put("Order Id", order_id);
                        productMap.put("Cart Total", cart_total);
                        productMap.put("Status Message", status_msg);
                        productMap.put("Order Qty", order_qty);
                        productMap.put("StatusId", status);
                        dataList.add(productMap);

                        JSONArray itemArray = orderObj.getJSONArray("items");
                        for (int j = 0; j < itemArray.length(); j++) {
                            JSONObject similarObj = itemArray.getJSONObject(j);

                            String id = similarObj.getString("id");
                            String name = similarObj.getString("name");
                            String thumb = similarObj.getString("thumb");
                            String price = similarObj.getString("sub_total");
                            String qty = similarObj.getString("qty");

                            // adding each child node to HashMap key => value
                            HashMap<String, Object> itemMap = new HashMap<String, Object>();
                            itemMap.put("Id", id);
                            itemMap.put("Name", name);
                            itemMap.put("Price", price);
                            itemMap.put("Thumbnail", thumb);
                            itemMap.put("Qty", qty);
                            itemList.add(itemMap);
                        }
                    }
                }
            } catch (JSONException e) {
                e.printStackTrace();
            }
            return null;
        }

        @Override
        protected void onPostExecute(Void aVoid) {
            super.onPostExecute(aVoid);

            // Dismiss the progress dialog
            if (pDialog.isShowing())
                pDialog.dismiss();

            CustomAdapter adapter = new CustomAdapter(OrderActivity.this, R.layout.order_layout, dataList);
            orderList.setAdapter(adapter);

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

            Button btn1, btn2, singleBtn;
            TextView orderId, cartTotal, statusMsg, orderQty, itemName, itemQty, itemPrice, itemId;
            ImageView imgView1, imgView2, imgView3, thumbnail;
            LinearLayout buttonLayout, circleLayout, itemLayout;
        }

        //Initialise
        ViewHolder viewHolder;

        @Override
        public View getView(final int position, View convertView, ViewGroup parent) {

            if (convertView == null) {

                //inflate the custom layout
                convertView = inflater.from(parent.getContext()).inflate(R.layout.order_layout, parent, false);
                viewHolder = new ViewHolder();

                //cache the views
                viewHolder.orderId = (TextView) convertView.findViewById(R.id.order_id);
                viewHolder.cartTotal = (TextView) convertView.findViewById(R.id.cart_total);
                viewHolder.statusMsg = (TextView) convertView.findViewById(R.id.status_msg);
                viewHolder.orderQty = (TextView) convertView.findViewById(R.id.order_qty);

//                viewHolder.itemId = (TextView) convertView.findViewById(R.id.item_id);
//                viewHolder.itemName = (TextView) convertView.findViewById(R.id.item_name);
//                viewHolder.itemQty = (TextView) convertView.findViewById(R.id.item_qty);
//                viewHolder.itemPrice = (TextView) convertView.findViewById(R.id.sub_total);
//                viewHolder.thumbnail = (ImageView) convertView.findViewById(R.id.thumbnail);

                viewHolder.btn1 = (Button) convertView.findViewById(R.id.button_1);
                viewHolder.btn2 = (Button) convertView.findViewById(R.id.button_2);
                viewHolder.singleBtn = (Button) convertView.findViewById(R.id.singlebtn);

                viewHolder.imgView1 = (ImageView) convertView.findViewById(R.id.circle_1);
                viewHolder.imgView2 = (ImageView) convertView.findViewById(R.id.circle_2);
                viewHolder.imgView3 = (ImageView) convertView.findViewById(R.id.circle_3);

                viewHolder.buttonLayout = (LinearLayout) convertView.findViewById(R.id.buttons_layout);
                viewHolder.circleLayout = (LinearLayout) convertView.findViewById(R.id.circle_layout);
                viewHolder.itemLayout = (LinearLayout) convertView.findViewById(R.id.item_layout);

                //link the cached views to the convertView
                convertView.setTag(viewHolder);

            } else {
                viewHolder = (ViewHolder) convertView.getTag();
            }

            //set Values
            viewHolder.orderId.setText(dataList.get(position).get("Order Id").toString());
            viewHolder.cartTotal.setText(dataList.get(position).get("Cart Total").toString());
            viewHolder.statusMsg.setText(dataList.get(position).get("Status Message").toString());
            viewHolder.orderQty.setText(dataList.get(position).get("Order Qty").toString());

            for (int i = 0; i < itemList.size(); i++) {
                View view = getLayoutInflater().inflate(R.layout.order_item_layout, null);

                TextView itemId = (TextView) view.findViewById(R.id.item_id);
                TextView itemName = (TextView) view.findViewById(R.id.item_name);
                TextView itemQty = (TextView) view.findViewById(R.id.item_qty);
                TextView itemPrice = (TextView) view.findViewById(R.id.sub_total);
                ImageView thumbnail = (ImageView) view.findViewById(R.id.thumbnail);

                itemId.setText(itemList.get(position).get("Id").toString());
                itemName.setText(itemList.get(position).get("Name").toString());
                itemQty.setText(itemList.get(position).get("Qty").toString());
                itemPrice.setText(itemList.get(position).get("Price").toString());

                //Load Thumbnail
                Picasso.with(OrderActivity.this)
                        .load(itemList.get(position).get("Thumbnail").toString())
                        .into(thumbnail);

                viewHolder.itemLayout.addView(view);
            }

            //onClick of Track Button
            viewHolder.singleBtn.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (viewHolder.singleBtn.getText().equals("Track Order")) {

                        String product_id = dataList.get(position).get("Order Id").toString();
                        Intent i = new Intent(OrderActivity.this, TrackOrderActivity.class);
                        i.putExtra("Order Id", product_id);
                        startActivity(i);
                    }
                }
            });

            viewHolder.btn2.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (viewHolder.btn2.getText().equals("Track Order")) {
                        String product_id = dataList.get(position).get("Order Id").toString();
                        Intent i = new Intent(OrderActivity.this, TrackOrderActivity.class);
                        i.putExtra("Product Id", product_id);
                        startActivity(i);
                    }
                }
            });


            if (dataList.get(position).get("StatusId").toString().equals("0")) {
                viewHolder.buttonLayout.setVisibility(View.GONE);
                viewHolder.singleBtn.setVisibility(View.VISIBLE);
                viewHolder.singleBtn.setText("Cancel Order");

                viewHolder.imgView1.setBackgroundResource(R.drawable.ico_status_mark_on);
                viewHolder.imgView2.setBackgroundResource(R.drawable.ico_status_mark_off);
                viewHolder.imgView3.setBackgroundResource(R.drawable.ico_status_mark_off);
            } else if (dataList.get(position).get("StatusId").toString().equals("1")) {
                viewHolder.buttonLayout.setVisibility(View.GONE);
                viewHolder.singleBtn.setVisibility(View.VISIBLE);
                viewHolder.singleBtn.setText("Track Order");

                viewHolder.imgView1.setBackgroundResource(R.drawable.ico_status_mark_on);
                viewHolder.imgView2.setBackgroundResource(R.drawable.ico_status_mark_on);
                viewHolder.imgView3.setBackgroundResource(R.drawable.ico_status_mark_off);
            } else if (dataList.get(position).get("StatusId").toString().equals("5")) {
                viewHolder.btn1.setVisibility(View.VISIBLE);
                viewHolder.btn2.setVisibility(View.VISIBLE);
                viewHolder.btn1.setText("Return Item");
                viewHolder.btn2.setText("Track Order");

                viewHolder.imgView1.setBackgroundResource(R.drawable.ico_status_mark_on);
                viewHolder.imgView2.setBackgroundResource(R.drawable.ico_status_mark_on);
                viewHolder.imgView3.setBackgroundResource(R.drawable.ico_status_mark_on);
            } else {
                viewHolder.buttonLayout.setVisibility(View.GONE);
                viewHolder.circleLayout.setVisibility(View.GONE);
                viewHolder.singleBtn.setVisibility(View.GONE);
            }

            return convertView;
        }
    }
}
