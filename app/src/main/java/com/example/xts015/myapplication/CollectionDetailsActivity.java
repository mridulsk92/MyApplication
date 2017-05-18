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
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.squareup.picasso.Picasso;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;

import uk.co.chrisjenx.calligraphy.CalligraphyContextWrapper;

public class CollectionDetailsActivity extends AppCompatActivity {

    String id;
    ListView collectionProducts;
    ProgressDialog pDialog;
    JSONParser jParser = new JSONParser();
    ArrayList<HashMap<String, Object>> dataList = new ArrayList<>();
    LayoutInflater inflater;
    String banner_st, name_st, description_st;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_collection_details);

        //Toolbar
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar_nologo);

        //Toolbar set title
        TextView toolbar_title = (TextView) toolbar.findViewById(R.id.title_toolbar);
        toolbar_title.setText("Collection");

        //Toolbar back Button
        ImageButton back = (ImageButton) toolbar.findViewById(R.id.button_back);
        back.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent i = new Intent(CollectionDetailsActivity.this, CollectionActivity.class);
                startActivity(i);
            }
        });

        //Get Intent
        Intent i = getIntent();
        id = i.getStringExtra("Id");

        //Initialise
        collectionProducts = (ListView) findViewById(R.id.collections_products);

        String url = "http://shop.irinerose.com/api/collections/" + id;
        new GetCollectionProducts().execute(url);

    }

    private class GetCollectionProducts extends AsyncTask<String, Void, Void> {

        @Override
        protected void onPreExecute() {
            super.onPreExecute();

            //Showing progress dialog
            pDialog = new ProgressDialog(CollectionDetailsActivity.this);
            pDialog.setMessage("Please wait...");
            pDialog.setCancelable(false);
            pDialog.show();
        }

        @Override
        protected Void doInBackground(String... params) {

            String url_final = params[0];
            Log.d("Url Final", url_final);
            JSONObject json = jParser.getJSONFromUrlByGet(url_final);
            Log.d("Json", String.valueOf(json));
            if (json != null) {
                try {

                    String success = json.getString("success");

                    if (success.equals("true")) {

                        JSONObject productDetails = json.getJSONObject("data");

                        String id = productDetails.getString("id");
                        name_st = productDetails.getString("name");
                        description_st = productDetails.getString("description");
                        banner_st = productDetails.getString("banner");

                        JSONArray collectionProducts = productDetails.getJSONArray("products");
                        for (int i = 0; i < collectionProducts.length(); i++) {
                            JSONObject collectionObj = collectionProducts.getJSONObject(i);

                            String id_c = collectionObj.getString("id");
                            String name_c = collectionObj.getString("name");
                            String description_c = collectionObj.getString("description");
                            String thumb_c = collectionObj.getString("thumb");
                            String price_c = collectionObj.getString("price");
                            String availability_c = collectionObj.getString("availibility");

                            // adding each child node to HashMap key => value
                            HashMap<String, Object> productMap = new HashMap<String, Object>();
                            productMap.put("Id", id_c);
                            productMap.put("Name", name_c);
                            productMap.put("Description", description_c);
                            productMap.put("Thumb", thumb_c);
                            productMap.put("Price", price_c);
                            productMap.put("Availability", availability_c);
                            dataList.add(productMap);

                        }
                    }
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            } else {
                Toast.makeText(CollectionDetailsActivity.this, "Nothing more to show", Toast.LENGTH_SHORT).show();
            }
            return null;
        }

        @Override
        protected void onPostExecute(Void aVoid) {
            super.onPostExecute(aVoid);

            // Dismiss the progress dialog
            if (pDialog.isShowing())
                pDialog.dismiss();

            CustomAdapter adapter = new CustomAdapter(CollectionDetailsActivity.this, R.layout.collection_product_item, dataList);
            collectionProducts.addHeaderView(AddHeader(CollectionDetailsActivity.this));
            collectionProducts.setDivider(CollectionDetailsActivity.this.getResources().getDrawable(R.drawable.transperent_color));
            collectionProducts.setDividerHeight(20);
            collectionProducts.setAdapter(adapter);
        }
    }

    private class CustomAdapter extends ArrayAdapter<HashMap<String, Object>> {

        public CustomAdapter(Context context, int textViewResourceId, ArrayList<HashMap<String, Object>> Strings) {

            //let android do the initializing :)
            super(context, textViewResourceId, Strings);
        }

        //class for caching the views in a row
        private class ViewHolder {

            ImageView thumbnail;
            TextView view_id;
            TextView view_name;
            TextView view_description;
            Button shopButton;
        }

        //Initialise
        ViewHolder viewHolder;

        @Override
        public View getView(final int position, View convertView, ViewGroup parent) {

            if (convertView == null) {

                //inflate the custom layout
                convertView = inflater.from(parent.getContext()).inflate(R.layout.collection_product_item, parent, false);
                viewHolder = new ViewHolder();

                //cache the views
                viewHolder.view_id = (TextView) convertView.findViewById(R.id.item_id);
                viewHolder.view_name = (TextView) convertView.findViewById(R.id.item_name);
                viewHolder.view_description = (TextView) convertView.findViewById(R.id.item_description);
                viewHolder.thumbnail = (ImageView) convertView.findViewById(R.id.banner_image);
                viewHolder.shopButton = (Button) convertView.findViewById(R.id.shop_button);

                //link the cached views to the convertview
                convertView.setTag(viewHolder);

            } else {
                viewHolder = (ViewHolder) convertView.getTag();
            }

            //set Values
            viewHolder.view_id.setText(dataList.get(position).get("Id").toString());
            viewHolder.view_name.setText(dataList.get(position).get("Name").toString());
            viewHolder.view_description.setText(dataList.get(position).get("Description").toString());

            //Load Thumbnail
            Picasso.with(CollectionDetailsActivity.this)
                    .load(dataList.get(position).get("Thumb").toString())
                    .into(viewHolder.thumbnail);

            //Button onClick
            viewHolder.shopButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {

                    LinearLayout rl = (LinearLayout) v.getParent();
                    TextView tv = (TextView) rl.findViewById(R.id.item_id);
                    String text = tv.getText().toString();
                    Intent i = new Intent(CollectionDetailsActivity.this, ProductActivity.class);
                    i.putExtra("Product Id", text);
                    startActivity(i);
                }
            });

            return convertView;
        }
    }

    public View AddHeader(Context c) {

        View v = View.inflate(c, R.layout.list_header, null);

        TextView name = (TextView) v.findViewById(R.id.item_name);
        ImageView banner = (ImageView) v.findViewById(R.id.banner);
        TextView description = (TextView) v.findViewById(R.id.item_description);

        name.setText(name_st);
        description.setText(description_st);

        //Load Banner
        Picasso.with(CollectionDetailsActivity.this)
                .load(banner_st)
                .into(banner);

        return v;
    }

    @Override
    protected void attachBaseContext(Context context) {
        super.attachBaseContext(CalligraphyContextWrapper.wrap(context));
    }
}
