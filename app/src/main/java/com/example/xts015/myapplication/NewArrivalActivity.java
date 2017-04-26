package com.example.xts015.myapplication;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.os.AsyncTask;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AbsListView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.GridView;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.PopupMenu;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.Toast;

import com.etsy.android.grid.StaggeredGridView;
import com.squareup.picasso.Picasso;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;

import static com.example.xts015.myapplication.HomeActivity.TAG_DATA;
import static com.example.xts015.myapplication.HomeActivity.TAG_IMGS;
import static com.example.xts015.myapplication.HomeActivity.TAG_TARGET;
import static com.example.xts015.myapplication.HomeActivity.TAG_URL;

public class NewArrivalActivity extends AppCompatActivity {

    ImageButton viewSwitch;
    Button sort;
    Button filter;
    ProgressDialog pDialog;
    JSONParser jParser = new JSONParser();
    ArrayList<HashMap<String, Object>> dataList = new ArrayList<>();
    LayoutInflater inflater;
    StaggeredGridView gridView;
    Boolean clicked;
    String status, success;
    int page, index = 0;
    String order_val, filter_val = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_new_arrival);

        //Initialise
        Toolbar toolbar_nologo = (Toolbar) findViewById(R.id.toolbar_nologo);
        viewSwitch = (ImageButton) findViewById(R.id.button_switch);
        sort = (Button) findViewById(R.id.button_sort);
        filter = (Button) findViewById(R.id.button_filter);
        gridView = (StaggeredGridView) findViewById(R.id.product_view);
        gridView.setOnScrollListener(new EndlessScrollListener());

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
        viewSwitch.setImageResource(R.drawable.test2);
        viewSwitch.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                if (clicked) {
                    viewSwitch.setImageResource(R.drawable.list_ic);
                    gridView.setColumnCount(2);
                    clicked = false;
                } else {
                    viewSwitch.setImageResource(R.drawable.test2);
                    gridView.setColumnCount(1);
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

                        filter_val = (String) item.getTitle();
                        filter.setText("FILTER - " + item.getTitle());
                        String filter_url = "http://shop.irinerose.com/api/shop/all/0?order=&style=" + item.getTitle();
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

                        order_val = (String) item.getTitle();
                        sort.setText("SORT - " + item.getTitle());
                        String order_url = "http://shop.irinerose.com/api/shop/all/0?order=" + item.getTitle() + "&style=";
                        new GetProducts().execute(order_url);
                        return false;
                    }
                });

                popup.show();
            }
        });

        //Load 1st set of images
        String url_initial = "http://shop.irinerose.com/api/shop/all/0?order=&style=";
        new GetProducts().execute(url_initial);
    }

    private class GetProducts extends AsyncTask<String, Void, Void> {

        @Override
        protected void onPreExecute() {
            super.onPreExecute();

            // Showing progress dialog
//            pDialog = new ProgressDialog(NewArrivalActivity.this);
//            pDialog.setMessage("Please wait...");
//            pDialog.setCancelable(false);
//            pDialog.show();
        }

        @Override
        protected Void doInBackground(String... params) {

            String url_final = params[0];
            Log.d("Url Final", url_final);
            JSONObject json = jParser.getJSONFromUrlByGet(url_final);
            Log.d("Json", String.valueOf(json));
            try {

                page = json.getInt("page");
                success = json.getString("success");

                if (success.equals("true")) {
                    JSONArray productDetails = json.getJSONArray("data");
                    for (int j = 0; j < productDetails.length(); j++) {
                        JSONObject productObj = productDetails.getJSONObject(j);

                        String id = productObj.getString("id");
                        String price = productObj.getString("price");
                        String name = productObj.getString("name");
                        int availability = productObj.getInt("availibility");
                        String thumbnail = productObj.getString("thumb");

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

                        // adding each child node to HashMap key => value
                        HashMap<String, Object> productMap = new HashMap<String, Object>();
                        productMap.put("Id", id);
                        productMap.put("Name", name);
                        productMap.put("Price", price);
                        productMap.put("Availability", status);
                        productMap.put("Thumbnail", thumbnail);
                        dataList.add(productMap);

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
//            if (pDialog.isShowing())
//                pDialog.dismiss();

            CustomAdapter adapter = new CustomAdapter(NewArrivalActivity.this, R.layout.product_layout, dataList);
            gridView.setAdapter(adapter);
            gridView.setVerticalScrollbarPosition(index);

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
            TextView view_availability;
        }

        //Initialise
        ViewHolder viewHolder;

        @Override
        public View getView(final int position, View convertView, ViewGroup parent) {

            View v;
            if (convertView == null) {

                LayoutInflater li = getLayoutInflater();
                v = li.inflate(R.layout.product_layout, null);
            } else {
                v = convertView;
            }

            //inflate the custom layout
            viewHolder = new ViewHolder();

            //cache the views
            ImageView view_thumbnail = (ImageView) v.findViewById(R.id.thumbnail);
            TextView view_id = (TextView) v.findViewById(R.id.textView_id);
            TextView view_name = (TextView) v.findViewById(R.id.textView_name);
            TextView view_price = (TextView) v.findViewById(R.id.textView_price);
            TextView view_availability = (TextView) v.findViewById(R.id.textView_availability);

            //set Values
            view_id.setText(dataList.get(position).get("Id").toString());
            view_name.setText(dataList.get(position).get("Name").toString());
            view_price.setText(dataList.get(position).get("Price").toString());
            view_availability.setText(dataList.get(position).get("Availability").toString());

            //Load Thumbnail
            Picasso.with(NewArrivalActivity.this)
                    .load(dataList.get(position).get("Thumbnail").toString())
                    .into(view_thumbnail);

            //link the cached views to the convertview
//            convertView.setTag(viewHolder);

            return v;
        }
    }

    public class EndlessScrollListener implements AbsListView.OnScrollListener {

        private int visibleThreshold = 5;
        private int currentPage = 0;
        private int previousTotal = 0;
        private boolean loading = true;

        public EndlessScrollListener() {
        }

        public EndlessScrollListener(int visibleThreshold) {
            this.visibleThreshold = visibleThreshold;
        }

        @Override
        public void onScroll(AbsListView view, int firstVisibleItem,
                             int visibleItemCount, int totalItemCount) {
            if (loading) {
                if (totalItemCount > previousTotal) {
                    loading = false;
                    previousTotal = totalItemCount;
                    currentPage++;
                }
            }
            if (!loading && (totalItemCount - visibleItemCount) <= (firstVisibleItem + visibleThreshold)) {
                // I load the next page of gigs using a background task,
                // but you can call any function here.
                page++;
                String url_latest = "http://shop.irinerose.com/api/shop/all/" + page + "?order=" + order_val + "&style=" + filter_val;
                new GetProducts().execute(url_latest);
                loading = true;
            }
        }

        @Override
        public void onScrollStateChanged(AbsListView view, int scrollState) {
        }
    }
}
