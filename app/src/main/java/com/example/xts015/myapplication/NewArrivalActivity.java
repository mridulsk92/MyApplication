package com.example.xts015.myapplication;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.graphics.Paint;
import android.graphics.Typeface;
import android.os.AsyncTask;
import android.os.Handler;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.AbsListView;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.GridView;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.PopupMenu;
import android.widget.RelativeLayout;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.Toast;

import com.etsy.android.grid.StaggeredGridView;
import com.squareup.picasso.Picasso;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;

import uk.co.chrisjenx.calligraphy.CalligraphyContextWrapper;


public class NewArrivalActivity extends AppCompatActivity {

    ImageButton viewSwitch;
    Button sort;
    Button filter;
    ProgressDialog pDialog;
    JSONParser jParser = new JSONParser();
    ArrayList<HashMap<String, Object>> dataList = new ArrayList<>();
    ArrayList<HashMap<String, Object>> moreDataList = new ArrayList<>();
    LayoutInflater inflater;
    StaggeredGridView gridView;
    Boolean clicked;
    String status, success;
    int page, index = 0;
    String order_val, filter_val = null;
    String order_st, filter_st;
    CustomAdapter adapter;
    RelativeLayout progressLayout;
    String loadMore = "false";
    TextView empty;

    int mVisibleThreshold = 5;
    int mCurrentPage = 0;
    int mPreviousTotal = 0;
    boolean mLoading = true;
    boolean mLastPage = false;
    boolean userScrolled = false;
    Typeface font, font_bold;
    int myLastVisiblePos;
    LinearLayout button_layout;

    ArrayList<HashMap<String, Object>> dataListStrikes = new ArrayList<>();
    ArrayList<HashMap<String, Object>> moreDataListStrikes = new ArrayList<>();
    String strike;
    PreferencesHelper pref;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_new_arrival);

        //Initialise
        font = Typeface.createFromAsset(NewArrivalActivity.this.getAssets(), getString(R.string.font));
        font_bold = Typeface.createFromAsset(NewArrivalActivity.this.getAssets(), getString(R.string.font_bold));

        pref = new PreferencesHelper(NewArrivalActivity.this);
        Toolbar toolbar_nologo = (Toolbar) findViewById(R.id.toolbar_nologo);
        viewSwitch = (ImageButton) findViewById(R.id.button_switch);
        sort = (Button) findViewById(R.id.button_sort);
        filter = (Button) findViewById(R.id.button_filter);
        gridView = (StaggeredGridView) findViewById(R.id.product_view);
        progressLayout = (RelativeLayout) findViewById(R.id.progress_layout);
        empty = (TextView) findViewById(R.id.empty);
        button_layout = (LinearLayout) findViewById(R.id.buttons);

        //Get Intent
        Intent i = getIntent();
        String title = i.getStringExtra("Source");

        //Toolbar set title
        TextView toolbar_title = (TextView) toolbar_nologo.findViewById(R.id.title_toolbar);
        toolbar_title.setText(title);

        //Toolbar back Button
        ImageButton back = (ImageButton) toolbar_nologo.findViewById(R.id.button_back);
        back.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent i = new Intent(NewArrivalActivity.this, HomeActivity.class);
                startActivity(i);
            }
        });

        //Switch view button onClickListener
        clicked = true;
        viewSwitch.setImageResource(R.drawable.list_ic);
        viewSwitch.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                if (clicked) {
                    viewSwitch.setImageResource(R.drawable.test2);
                    gridView.setColumnCount(1);
                    clicked = false;
                } else {
                    viewSwitch.setImageResource(R.drawable.list_ic);
                    gridView.setColumnCount(2);
                    clicked = true;
                }
            }
        });

        //onClick of filter button
        filter.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                PopupMenu popup = new PopupMenu(NewArrivalActivity.this, filter);
                popup.getMenuInflater().inflate(R.menu.fillter_menu, popup.getMenu());
                popup.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {
                    @Override
                    public boolean onMenuItemClick(MenuItem item) {

                        if (item.getTitle().equals("Chic")) {
                            filter_st = "chic";
                        } else if (item.getTitle().equals("Active")) {
                            filter_st = "active";
                        } else if (item.getTitle().equals("Classic")) {
                            filter_st = "classic";
                        } else {
                            filter_st = null;
                        }
                        filter_val = (String) item.getTitle();
                        filter.setText(item.getTitle());
                        dataList.clear();

                        String filter_url = "http://shop.irinerose.com/api/shop/all/0?order=" + order_st + "&style=" + filter_st;
                        new GetProducts().execute(filter_url);
                        return false;
                    }
                });

                popup.show();
            }
        });

        //onClick of sort button
        sort.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                PopupMenu popup = new PopupMenu(NewArrivalActivity.this, sort);
                popup.getMenuInflater().inflate(R.menu.sort_menu, popup.getMenu());
                popup.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {
                    @Override
                    public boolean onMenuItemClick(MenuItem item) {

                        if (item.getTitle().equals("High")) {
                            order_st = "high";
                        } else if (item.getTitle().equals("Low")) {
                            order_st = "low";
                        } else {
                            order_st = "latest";
                        }
                        order_val = (String) item.getTitle();
                        sort.setText(item.getTitle());
                        dataList.clear();
                        String order_url = "http://shop.irinerose.com/api/shop/all/0?order=" + order_st + "&style=" + filter_st;
                        new GetProducts().execute(order_url);
                        return false;
                    }
                });

                popup.show();
            }
        });

        //GridView onClickItem
        gridView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {

                String product_id = ((TextView) view.findViewById(R.id.textView_id)).getText().toString();
                Intent i = new Intent(NewArrivalActivity.this, ProductActivity.class);
                i.putExtra("Product Id", product_id);
                startActivity(i);
            }
        });

        //Load 1st set of images
        String url_initial = "http://shop.irinerose.com/api/shop/all/0?order=" + order_st + "&style=" + filter_st;
        new GetProducts().execute(url_initial);
        implementScrollListener();

    }

    // Implement scroll listener
    private void implementScrollListener() {
        gridView.setOnScrollListener(new AbsListView.OnScrollListener() {

            @Override
            public void onScrollStateChanged(AbsListView arg0, int scrollState) {
                // If scroll state is touch scroll then set userScrolled
                // true
                if (scrollState == AbsListView.OnScrollListener.SCROLL_STATE_TOUCH_SCROLL) {
                    userScrolled = true;

                }
            }

            @Override
            public void onScroll(AbsListView view, int firstVisibleItem,
                                 int visibleItemCount, int totalItemCount) {
                // Now check if userScrolled is true and also check if
                // the item is end then update list view and set
                // userScrolled to false

                int currentFirstVisPos = view.getFirstVisiblePosition();
                if (currentFirstVisPos > myLastVisiblePos) {
                    //scroll down
                    Log.d("TEST", "DOWN");
//                    button_layout.setVisibility(View.GONE);
//                    button_layout.animate().alpha(0.0f);
                }
                if (currentFirstVisPos < myLastVisiblePos) {
                    //scroll up
                    Log.d("TEST", "UP");
//                    button_layout.setVisibility(View.VISIBLE);
//                    button_layout.animate().alpha(1.0f);
                }
                myLastVisiblePos = currentFirstVisPos;


                if (userScrolled && firstVisibleItem + visibleItemCount == totalItemCount) {

                    if (success.equals("true")) {
                        loadMore = "true";
                        page++;
                        userScrolled = false;
                        String url_latest = "http://shop.irinerose.com/api/shop/all/" + page + "?order=" + order_val + "&style=" + filter_val;
                        new LoadMoreProducts().execute(url_latest);
                    } else {
                        loadMore = "false";
                        moreDataList.clear();
                    }
                }
            }
        });
    }

    private class GetProducts extends AsyncTask<String, Void, Void> {

        @Override
        protected void onPreExecute() {
            super.onPreExecute();

            //Showing progress dialog
            pDialog = new ProgressDialog(NewArrivalActivity.this);
            pDialog.setMessage("Please wait...");
            pDialog.setCancelable(false);
            pDialog.show();
        }

        @Override
        protected Void doInBackground(String... params) {

            String url_final = params[0];
            Log.d("Url Final", url_final);
            String token = pref.GetPreferences("Token");
            JSONObject json = jParser.getJSONFromUrlByGet(url_final,token);
            Log.d("Json", String.valueOf(json));
            if (json != null) {
                try {

                    page = json.getInt("page");
                    success = json.getString("success");

                    if (success.equals("true")) {

                        JSONArray productDetails = json.getJSONArray("data");
                        for (int j = 0; j < productDetails.length(); j++) {
                            JSONObject productObj = productDetails.getJSONObject(j);

                            String id = productObj.getString("id");
                            String offer = productObj.getString("offer");
                            String subprice = productObj.getString("subprice");
                            String price = productObj.getString("price");
                            String name = productObj.getString("name");
                            int availability = productObj.getInt("availibility");
                            String thumbnail = productObj.getString("thumb");

                            if (offer.equals("0")) {
                                strike = "No";
                            } else {
                                strike = "Yes";
                            }

                            switch (availability) {
                                case 0:
                                    status = "Coming Soon";
                                    break;
                                case 1:
                                    status = "";
                                    break;
                                case 2:
                                    status = "Pre Order";
                                    break;
                                case 3:
                                    status = "Out of Stock";
                                    break;

                            }

                            //add Offer
                            HashMap<String, Object> strikeMap = new HashMap<String, Object>();
                            strikeMap.put("Strike", strike);
                            dataListStrikes.add(strikeMap);

                            // adding each child node to HashMap key => value
                            HashMap<String, Object> productMap = new HashMap<String, Object>();
                            productMap.put("Id", id);
                            productMap.put("Name", name);
                            productMap.put("Price", price);
                            productMap.put("Offer", offer);
                            productMap.put("Subprice", subprice);
                            productMap.put("Availability", status);
                            productMap.put("Thumbnail", thumbnail);
                            dataList.add(productMap);

                        }
                    }
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            } else {
                Toast.makeText(NewArrivalActivity.this, "Nothing more to show", Toast.LENGTH_SHORT).show();
            }
            return null;
        }

        @Override
        protected void onPostExecute(Void aVoid) {
            super.onPostExecute(aVoid);

            // Dismiss the progress dialog
            if (pDialog.isShowing())
                pDialog.dismiss();
            adapter = new CustomAdapter(NewArrivalActivity.this, R.layout.product_view_new, dataList);
            gridView.setAdapter(adapter);
        }
    }

    private class LoadMoreProducts extends AsyncTask<String, Void, Void> {

        @Override
        protected void onPreExecute() {
            super.onPreExecute();

            moreDataList.clear();
            if (progressLayout.getVisibility() == View.GONE) {
                progressLayout.setVisibility(View.VISIBLE);
                getWindow().setFlags(WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE, WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE);
            }
        }

        @Override
        protected Void doInBackground(String... params) {

            String url_final = params[0];
            Log.d("Url Final", url_final);
            String token = pref.GetPreferences("Token");
            JSONObject json = jParser.getJSONFromUrlByGet(url_final,token);
            Log.d("Json", String.valueOf(json));
            if (json != null) {
                try {

                    page = json.getInt("page");
                    success = json.getString("success");

                    JSONArray productDetails = json.getJSONArray("data");
                    if (productDetails != null) {
                        for (int j = 0; j < productDetails.length(); j++) {
                            JSONObject productObj = productDetails.getJSONObject(j);

                            String id = productObj.getString("id");
                            String offer = productObj.getString("offer");
                            String subprice = productObj.getString("subprice");
                            String price = productObj.getString("price");
                            String name = productObj.getString("name");
                            int availability = productObj.getInt("availibility");
                            String thumbnail = productObj.getString("thumb");

                            if (offer.equals("0")) {
                               strike = "No";
                            } else {
                                strike = "Yes";
                            }

                            switch (availability) {
                                case 0:
                                    status = "Coming Soon";
                                    break;
                                case 1:
                                    status = "";
                                    break;
                                case 2:
                                    status = "Pre Order";
                                    break;
                                case 3:
                                    status = "Out of Stock";
                                    break;
                            }

                            //add Offer
                            HashMap<String, Object> strikeMap = new HashMap<String, Object>();
                            strikeMap.put("Strike", strike);
                            moreDataListStrikes.add(strikeMap);

                            // adding each child node to HashMap key => value
                            HashMap<String, Object> productMap = new HashMap<String, Object>();
                            productMap.put("Id", id);
                            productMap.put("Name", name);
                            productMap.put("Offer", offer);
                            productMap.put("Subprice", subprice);
                            productMap.put("Price", price);
                            productMap.put("Availability", status);
                            productMap.put("Thumbnail", thumbnail);
                            moreDataList.add(productMap);

                        }
                    } else {
                        Toast.makeText(NewArrivalActivity.this, "Nothing more to show", Toast.LENGTH_SHORT).show();
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

            if (progressLayout.getVisibility() == View.VISIBLE) {
                progressLayout.setVisibility(View.GONE);
                getWindow().clearFlags(WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE);
            }

            dataListStrikes.addAll(moreDataListStrikes);

            adapter.addAll(moreDataList);
            adapter.notifyDataSetChanged();
            gridView.setAdapter(adapter);

        }
    }

    private class CustomAdapter extends ArrayAdapter<HashMap<String, Object>> {

        public CustomAdapter(Context context, int textViewResourceId, ArrayList<HashMap<String, Object>> Strings) {

            //let android do the initializing :)
            super(context, textViewResourceId, Strings);
        }

        //class for caching the views in a row
        private class ViewHolder {

            ImageView view_thumbnail;
            TextView view_id;
            TextView view_name;
            TextView view_price;
            TextView view_availability, view_subprice, view_offer;
        }

        //Initialise
        ViewHolder viewHolder;

        @Override
        public View getView(final int position, View convertView, ViewGroup parent) {

            if (convertView == null) {

                //inflate the custom layout
                convertView = inflater.from(parent.getContext()).inflate(R.layout.product_view_new, parent, false);
                viewHolder = new ViewHolder();

                //cache the views
                viewHolder.view_thumbnail = (ImageView) convertView.findViewById(R.id.thumbnail);
                viewHolder.view_subprice = (TextView) convertView.findViewById(R.id.textView_subprice);
                viewHolder.view_id = (TextView) convertView.findViewById(R.id.textView_id);
                viewHolder.view_name = (TextView) convertView.findViewById(R.id.textView_name);
                viewHolder.view_price = (TextView) convertView.findViewById(R.id.textView_price);
                viewHolder.view_availability = (TextView) convertView.findViewById(R.id.textView_availability);
                viewHolder.view_offer = (TextView) convertView.findViewById(R.id.offer_view);

                //link the cached views to the convertview
                convertView.setTag(viewHolder);

            } else {
                viewHolder = (ViewHolder) convertView.getTag();
            }

            //set Values
            viewHolder.view_id.setText(dataList.get(position).get("Id").toString());
            viewHolder.view_name.setText(dataList.get(position).get("Name").toString());
            viewHolder.view_price.setText(dataList.get(position).get("Price").toString());
//            viewHolder.view_subprice.setText(dataList.get(position).get("Subprice").toString());
            viewHolder.view_offer.setText(dataList.get(position).get("Offer").toString());
            viewHolder.view_availability.setText(dataList.get(position).get("Availability").toString());
            viewHolder.view_name.setTypeface(font);
            viewHolder.view_price.setTypeface(font_bold);
            viewHolder.view_subprice.setTypeface(font_bold);

            // Offerview
//            if(viewHolder.view_subprice.getText().equals((viewHolder.view_price.getText()))){
//                viewHolder.view_price.setVisibility(View.GONE);
//            }else{
//                viewHolder.view_subprice.setPaintFlags(viewHolder.view_price.getPaintFlags() | Paint.STRIKE_THRU_TEXT_FLAG);
//                viewHolder.view_price.setVisibility(View.VISIBLE);
//            }
//            if(dataListStrikes.get(position).get("Strike").equals("Yes")){
//                viewHolder.view_subprice.setPaintFlags(viewHolder.view_price.getPaintFlags() | Paint.STRIKE_THRU_TEXT_FLAG);
//                viewHolder.view_price.setVisibility(View.VISIBLE);
//            }else{
//                viewHolder.view_price.setVisibility(View.GONE);
//            }

            //Load Thumbnail
            Picasso.with(NewArrivalActivity.this)
                    .load(dataList.get(position).get("Thumbnail").toString())
                    .into(viewHolder.view_thumbnail);
//            view_thumbnail.setScaleType(ImageView.ScaleType.FIT_CENTER);


            //link the cached views to the convertview
//            convertView.setTag(viewHolder);

            return convertView;
        }
    }

    @Override
    protected void attachBaseContext(Context context) {
        super.attachBaseContext(CalligraphyContextWrapper.wrap(context));
    }
}
