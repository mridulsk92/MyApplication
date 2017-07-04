package com.example.xts015.myapplication;

import android.app.ProgressDialog;
import android.content.Intent;
import android.graphics.Paint;
import android.os.AsyncTask;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Gravity;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.daimajia.slider.library.SliderTypes.DefaultSliderView;
import com.squareup.picasso.Picasso;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class TrackOrderActivity extends AppCompatActivity {

    String orderId;
    ProgressDialog pDialog;
    PreferencesHelper pref;
    JSONParser jParser = new JSONParser();
    private List<TimeLineModel> mDataList = new ArrayList<>();
    private RecyclerView mRecyclerView;
    private TimeLineAdapter mTimeLineAdapter;
    private Orientation mOrientation;
    String locale;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_track_order);

        //Get Intent
        Intent i = getIntent();
        orderId = i.getStringExtra("Order Id");

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
        pref = new PreferencesHelper(TrackOrderActivity.this);
        mOrientation = Orientation.VERTICAL;
        mRecyclerView = (RecyclerView) findViewById(R.id.recyclerView);
        mRecyclerView.setLayoutManager(getLinearLayoutManager());
        mRecyclerView.setHasFixedSize(true);
        locale = pref.GetPreferences("Location");

        new TrackOrder().execute(orderId);
//        initView();
    }

    private LinearLayoutManager getLinearLayoutManager() {
        if (mOrientation == Orientation.HORIZONTAL) {
            return new LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false);
        } else {
            return new LinearLayoutManager(this, LinearLayoutManager.VERTICAL, false);
        }
    }

    private void initView() {
        setDataListItems();
        mTimeLineAdapter = new TimeLineAdapter(mDataList, Orientation.VERTICAL, false);
        mRecyclerView.setAdapter(mTimeLineAdapter);
    }

    private void setDataListItems() {
        mDataList.add(new TimeLineModel("Item successfully delivered", "", OrderStatus.INACTIVE));
        mDataList.add(new TimeLineModel("Courier is out to delivery your order", "2017-02-12 08:00", OrderStatus.ACTIVE));
        mDataList.add(new TimeLineModel("Item has reached courier facility at New Delhi", "2017-02-11 21:00", OrderStatus.COMPLETED));
        mDataList.add(new TimeLineModel("Item has been given to the courier", "2017-02-11 18:00", OrderStatus.COMPLETED));
        mDataList.add(new TimeLineModel("Item is packed and will dispatch soon", "2017-02-11 09:30", OrderStatus.COMPLETED));
        mDataList.add(new TimeLineModel("Order is being readied for dispatch", "2017-02-11 08:00", OrderStatus.COMPLETED));
        mDataList.add(new TimeLineModel("Order processing initiated", "2017-02-10 15:00", OrderStatus.COMPLETED));
        mDataList.add(new TimeLineModel("Order confirmed by seller", "2017-02-10 14:30", OrderStatus.COMPLETED));
        mDataList.add(new TimeLineModel("Order placed successfully", "2017-02-10 14:00", OrderStatus.COMPLETED));
    }

    private class TrackOrder extends AsyncTask<String, Void, Void> {

        @Override
        protected void onPreExecute() {
            super.onPreExecute();

            // Showing progress dialog
            pDialog = new ProgressDialog(TrackOrderActivity.this);
            pDialog.setMessage("Please wait...");
            pDialog.setCancelable(false);
            pDialog.show();
        }

        @Override
        protected Void doInBackground(String... params) {

            String order_id = params[0];
            String token = pref.GetPreferences("Token");
            String url = "http://shop.irinerose.com/api/user/order/" + order_id + "/track";
            JSONObject json = jParser.getJSONFromUrlByGet(url, token, locale);
            Log.d("Json", String.valueOf(json));

            try {

                JSONObject data = json.getJSONObject("data");
                JSONObject tracking = data.getJSONObject("tracking");
                JSONArray checkpointsArray = tracking.getJSONArray("checkpoints");

                for (int i = 0; i < checkpointsArray.length(); i++) {

                    JSONObject obj = checkpointsArray.getJSONObject(i);
                    String message = obj.getString("message");
                    String time = obj.getString("checkpoint_time");

                    if (!message.equals("")) {
                        mDataList.add(new TimeLineModel(message, time, OrderStatus.COMPLETED));

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

            mTimeLineAdapter = new TimeLineAdapter(mDataList, Orientation.VERTICAL, false);
            mRecyclerView.setAdapter(mTimeLineAdapter);

        }
    }

}
