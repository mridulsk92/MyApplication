package com.example.xts015.myapplication;

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
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListAdapter;
import android.widget.ListView;
import android.widget.RadioButton;
import android.widget.TextView;
import android.widget.Toast;

import com.squareup.picasso.Picasso;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class CheckoutActivity extends AppCompatActivity {

    ProgressDialog pDialog;
    PreferencesHelper pref;
    JSONParser jParser = new JSONParser();
    ArrayList<HashMap<String, Object>> itemDataList = new ArrayList<>();
    ArrayList<HashMap<String, Object>> shippingList = new ArrayList<>();
    String cartCount, subTotal, cartTotal, coupon, tax, discount, shipCharge;
    ViewPager pager;
    TextView itemCount_view, subTotal_view;
    LayoutInflater inflater;
    ListView deliveryList;

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

        //Initialise
        pref = new PreferencesHelper(CheckoutActivity.this);
        pager = (ViewPager) findViewById(R.id.viewpager_preview);
        itemCount_view = (TextView) findViewById(R.id.itemcount_view);
        subTotal_view = (TextView) findViewById(R.id.totalView);
        deliveryList = (ListView) findViewById(R.id.list_options);

        new GetProducts().execute("http://shop.irinerose.com/api/cart/validate");

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
            JSONObject json = jParser.getJSONFromUrlByGet(url_final, token);
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
                            //To Do when no address
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

            //Set TextView values
            itemCount_view.setText(cartCount + " item");
            subTotal_view.setText(subTotal);

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

        public CustomAdapter(Context context, int textViewResourceId, ArrayList<HashMap<String, Object>> Strings) {

            //let android do the initializing :)
            super(context, textViewResourceId, Strings);
        }

        //class for caching the views in a row
        private class ViewHolder {

            RadioButton select_button;
            TextView view_method;
            TextView view_name;
            TextView view_desc;
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
                viewHolder.view_method = (TextView) convertView.findViewById(R.id.method_view);
                viewHolder.view_desc = (TextView) convertView.findViewById(R.id.desc_view);
                viewHolder.view_name = (TextView) convertView.findViewById(R.id.name_view);
                viewHolder.select_button = (RadioButton) convertView.findViewById(R.id.radioButton);

                //link the cached views to the convertview
                convertView.setTag(viewHolder);

            } else {
                viewHolder = (ViewHolder) convertView.getTag();
            }

            //set Values
            viewHolder.view_desc.setText(shippingList.get(position).get("Desc").toString());
            viewHolder.view_name.setText(shippingList.get(position).get("Name").toString());
            viewHolder.view_method.setText(shippingList.get(position).get("Charge").toString());


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

}


