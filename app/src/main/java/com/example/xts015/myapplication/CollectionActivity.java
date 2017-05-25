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

public class CollectionActivity extends AppCompatActivity {

    ListView collectionList;
    ProgressDialog pDialog;
    JSONParser jParser = new JSONParser();
    String success;
    ArrayList<HashMap<String, Object>> dataList = new ArrayList<>();
    LayoutInflater inflater;
    PreferencesHelper pref;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_collection);

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
                Intent i = new Intent(CollectionActivity.this, HomeActivity.class);
                startActivity(i);
            }
        });

        //Initialise
        collectionList = (ListView) findViewById(R.id.collection_item_list);
        pref = new PreferencesHelper(CollectionActivity.this);

        //Load Data
        String url = "http://shop.irinerose.com/api/collections/all";
        new GetCollections().execute(url);

    }

    private class GetCollections extends AsyncTask<String, Void, Void> {

        @Override
        protected void onPreExecute() {
            super.onPreExecute();

            //Showing progress dialog
            pDialog = new ProgressDialog(CollectionActivity.this);
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

                    success = json.getString("success");

                    if (success.equals("true")) {

                        JSONArray productDetails = json.getJSONArray("data");
                        for (int j = 0; j < productDetails.length(); j++) {
                            JSONObject productObj = productDetails.getJSONObject(j);

                            String id = productObj.getString("id");
                            String name = productObj.getString("name");
                            String description = productObj.getString("description");
                            String banner = productObj.getString("banner");

                            // adding each child node to HashMap key => value
                            HashMap<String, Object> productMap = new HashMap<String, Object>();
                            productMap.put("Id", id);
                            productMap.put("Name", name);
                            productMap.put("Description", description);
                            productMap.put("Banner", banner);
                            dataList.add(productMap);

                        }
                    }
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            } else {
                Toast.makeText(CollectionActivity.this, "Nothing more to show", Toast.LENGTH_SHORT).show();
            }
            return null;
        }

        @Override
        protected void onPostExecute(Void aVoid) {
            super.onPostExecute(aVoid);

            // Dismiss the progress dialog
            if (pDialog.isShowing())
                pDialog.dismiss();

            CustomAdapter adapter = new CustomAdapter(CollectionActivity.this, R.layout.collection_item, dataList);
            collectionList.setAdapter(adapter);
        }
    }

    private class CustomAdapter extends ArrayAdapter<HashMap<String, Object>> {

        public CustomAdapter(Context context, int textViewResourceId, ArrayList<HashMap<String, Object>> Strings) {

            //let android do the initializing :)
            super(context, textViewResourceId, Strings);
        }

        //class for caching the views in a row
        private class ViewHolder {

            ImageView banner;
            TextView view_id;
            TextView view_name;
            TextView view_description;
            Button discoverButton;
        }

        //Initialise
        ViewHolder viewHolder;

        @Override
        public View getView(final int position, View convertView, ViewGroup parent) {

            if (convertView == null) {

                //inflate the custom layout
                convertView = inflater.from(parent.getContext()).inflate(R.layout.collection_item, parent, false);
                viewHolder = new ViewHolder();

                //cache the views
                viewHolder.view_id = (TextView) convertView.findViewById(R.id.item_id);
                viewHolder.view_name = (TextView) convertView.findViewById(R.id.item_name);
                viewHolder.view_description = (TextView) convertView.findViewById(R.id.item_description);
                viewHolder.banner = (ImageView) convertView.findViewById(R.id.banner_image);
                viewHolder.discoverButton = (Button) convertView.findViewById(R.id.discover_button);

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
            Picasso.with(CollectionActivity.this)
                    .load(dataList.get(position).get("Banner").toString())
                    .into(viewHolder.banner);

            //Button onClick
            viewHolder.discoverButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {

                    LinearLayout rl = (LinearLayout) v.getParent();
                    TextView tv = (TextView)rl.findViewById(R.id.item_id);
                    String text = tv.getText().toString();
                    Intent i = new Intent(CollectionActivity.this, CollectionDetailsActivity.class);
                    i.putExtra("Id", text);
                    startActivity(i);
                }
            });

            return convertView;
        }
    }

    @Override
    protected void attachBaseContext(Context context) {
        super.attachBaseContext(CalligraphyContextWrapper.wrap(context));
    }
}
