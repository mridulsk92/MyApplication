package com.example.xts015.myapplication;

import android.app.ProgressDialog;
import android.content.Context;
import android.os.AsyncTask;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.GridView;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.Switch;
import android.widget.TextView;

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

    GridView productView;
    ImageButton viewSwitch;
    Button sort;
    Button filter;
    ProgressDialog pDialog;
    JSONParser jParser = new JSONParser();
    String url = "http://shop.irinerose.com/api/shop/all/0";
    ArrayList<HashMap<String, Object>> dataList = new ArrayList<>();
    LayoutInflater inflater;
    StaggeredGridView gridView;
    Boolean clicked;
    String status;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_new_arrival);

        //Initialise
//        productView = (GridView) findViewById(R.id.products_view);
        viewSwitch = (ImageButton) findViewById(R.id.button_switch);
        sort = (Button) findViewById(R.id.button_sort);
        filter = (Button) findViewById(R.id.button_filter);
        gridView = (StaggeredGridView) findViewById(R.id.product_view);

        //Switch view button onClickListener
        clicked = true;
        viewSwitch.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                if(clicked){
                    gridView.setColumnCount(2);
                    clicked = false;
                }else{
                    gridView.setColumnCount(1);
                    clicked = true;
                }
            }
        });

        new GetProducts().execute();
    }

    private class GetProducts extends AsyncTask<Void, Void, Void> {

        @Override
        protected void onPreExecute() {
            super.onPreExecute();

            // Showing progress dialog
            pDialog = new ProgressDialog(NewArrivalActivity.this);
            pDialog.setMessage("Please wait...");
            pDialog.setCancelable(false);
            pDialog.show();
        }

        @Override
        protected Void doInBackground(Void... params) {

            JSONObject json = jParser.getJSONFromUrlByGet(url);
            try {

                String page = json.getString("page");
                String success = json.getString("success");

                JSONArray productDetails = json.getJSONArray("data");
                for (int j = 0; j < productDetails.length(); j++) {
                    JSONObject productObj = productDetails.getJSONObject(j);

                    String id = productObj.getString("id");
                    String price = productObj.getString("price");
                    String name = productObj.getString("name");
                    int availability = productObj.getInt("availibility");
                    String thumbnail = productObj.getString("thumb");

                    switch (availability){
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

            CustomAdapter adapter = new CustomAdapter(NewArrivalActivity.this, R.layout.product_layout, dataList);
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
}
