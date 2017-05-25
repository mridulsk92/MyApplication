package com.example.xts015.myapplication;

import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.content.res.ColorStateList;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Typeface;
import android.os.AsyncTask;
import android.os.Environment;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.graphics.drawable.DrawableCompat;
import android.support.v4.view.PagerAdapter;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.daimajia.slider.library.Indicators.PagerIndicator;
import com.daimajia.slider.library.SliderLayout;
import com.daimajia.slider.library.SliderTypes.DefaultSliderView;
import com.daimajia.slider.library.SliderTypes.TextSliderView;
import com.mikepenz.materialize.color.Material;
import com.squareup.picasso.Picasso;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import uk.co.chrisjenx.calligraphy.CalligraphyContextWrapper;

public class ProductActivity extends AppCompatActivity {

    TextView price, product_name, description, materials, dimensions, subPrice, goldLabel, silverLabel, offer_view, noStock_view;
    private SliderLayout mSlider;
    String product_id, product_name_st, price_st, description_st;
    ProgressDialog pDialog;
    JSONParser jParser = new JSONParser();

    ArrayList<HashMap<String, Object>> similarDataList = new ArrayList<>();
    ArrayList<HashMap<String, Object>> metalDataList = new ArrayList<>();

    String url = "http://shop.irinerose.com/api/products/";
    ArrayList<String> shopImages = new ArrayList<String>();
    ArrayList<String> bannerImages = new ArrayList<String>();
    Button gold, silver, addToBag, addToWishList;
    String size, material, category, style, collection, subprice, type, offer;
    int availability;
    ListView similarList;
    ViewPager pager;
    LinearLayout metalLayout;
    Typeface font, font_bold;
    PagerIndicator pageIndicator;
    ImageView banner1, banner2;
    String banner1_st, banner2_st;
    PreferencesHelper pref;
    String isLogged;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_product);

        //Initialise
        font = Typeface.createFromAsset(ProductActivity.this.getAssets(), getString(R.string.font));
        font_bold = Typeface.createFromAsset(ProductActivity.this.getAssets(), getString(R.string.font_bold));
        pref = new PreferencesHelper(ProductActivity.this);
        metalLayout = (LinearLayout) findViewById(R.id.metal_view);
        offer_view = (TextView) findViewById(R.id.offer_text);
        price = (TextView) findViewById(R.id.text_price);
        subPrice = (TextView) findViewById(R.id.sub_price);
        product_name = (TextView) findViewById(R.id.text_productname);
        description = (TextView) findViewById(R.id.text_description);
        materials = (TextView) findViewById(R.id.materials_view);
        dimensions = (TextView) findViewById(R.id.dimensions_view);
        banner1 = (ImageView) findViewById(R.id.banner1);
        banner2 = (ImageView) findViewById(R.id.banner2);

        mSlider = (SliderLayout) findViewById(R.id.slider);
        mSlider.stopAutoCycle();

        Toolbar toolbar_nologo = (Toolbar) findViewById(R.id.toolbar_nologo);
        gold = (Button) findViewById(R.id.gold);
        silver = (Button) findViewById(R.id.silver);
        pager = (ViewPager) findViewById(R.id.viewpager_similar);
        goldLabel = (TextView) findViewById(R.id.gold_label);
        silverLabel = (TextView) findViewById(R.id.silver_label);
        addToBag = (Button) findViewById(R.id.add_to_bag);
        addToWishList = (Button) findViewById(R.id.button_addtowishList);
        noStock_view = (TextView) findViewById(R.id.nostock_label);
        pageIndicator = (PagerIndicator) findViewById(R.id.slider_indicator);

        //Check If Logged In
        isLogged = pref.GetPreferences("Login");

        gold.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                ViewGroup.LayoutParams params_g = gold.getLayoutParams();
                //Button new width
                params_g.width = 100;
                params_g.height = 100;
                gold.setLayoutParams(params_g);

//                goldLabel.setVisibility(View.VISIBLE);
//                silverLabel.setVisibility(View.GONE);

                ViewGroup.LayoutParams params_s = silver.getLayoutParams();
                //Button new width
                params_s.width = 50;
                params_s.height = 50;
                silver.setLayoutParams(params_s);
            }
        });

        silver.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                ViewGroup.LayoutParams params_s = silver.getLayoutParams();
                //Button new width
                params_s.width = 100;
                params_s.height = 100;
                silver.setLayoutParams(params_s);
                goldLabel.setVisibility(View.GONE);
                silverLabel.setVisibility(View.VISIBLE);

                ViewGroup.LayoutParams params_g = gold.getLayoutParams();
                //Button new width
                params_g.width = 50;
                params_g.height = 50;
                gold.setLayoutParams(params_g);

            }
        });

        //Toolbar set title
        TextView toolbar_title = (TextView) toolbar_nologo.findViewById(R.id.title_toolbar);
        toolbar_title.setText("");

        //Toolbar back Button
        ImageButton back = (ImageButton) toolbar_nologo.findViewById(R.id.button_back);
        back.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent i = new Intent(ProductActivity.this, NewArrivalActivity.class);
                i.putExtra("Source", "New Arrival");
                startActivity(i);
            }
        });

        //Get Intent
        Intent i = getIntent();
        product_id = i.getStringExtra("Product Id");
        pref.SavePreferences("ProductId", product_id);

        //Load Product Data
        new LoadProduct().execute(url + product_id);

        //Add to Bag onClickListener
        addToBag.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {


                if (isLogged.equals("true")) {
                    Toast.makeText(ProductActivity.this, "Logged In", Toast.LENGTH_SHORT).show();
                } else {

                    final Dialog dialog = new Dialog(ProductActivity.this);
                    dialog.setContentView(R.layout.popup_layout);
                    Button btnCancel = (Button) dialog.findViewById(R.id.button_cancel);
                    Button btnJoin = (Button) dialog.findViewById(R.id.button_join);
                    Button btnLogin = (Button) dialog.findViewById(R.id.button_login);

                    //Cancel
                    btnCancel.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {

                            dialog.dismiss();
                        }
                    });

                    //Join
                    btnJoin.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {

                            Intent i = new Intent(ProductActivity.this, RegisterActivity.class);
                            i.putExtra("From", "ProductActivity");
                            startActivity(i);

                        }
                    });

                    //Log In
                    btnLogin.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {

                            Intent i = new Intent(ProductActivity.this, LoginActivity.class);
                            i.putExtra("From", "ProductActivity");
                            startActivity(i);
                        }
                    });
                    dialog.show();
                }
            }
        });

        //Add To Wishlist onClickListener
        addToWishList.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                if (isLogged.equals("true")) {
                    Toast.makeText(ProductActivity.this, "Logged In", Toast.LENGTH_SHORT).show();
                } else {

                    final Dialog dialog = new Dialog(ProductActivity.this);
                    dialog.setContentView(R.layout.popup_layout);
                    Button btnCancel = (Button) dialog.findViewById(R.id.button_cancel);
                    Button btnJoin = (Button) dialog.findViewById(R.id.button_join);
                    Button btnLogin = (Button) dialog.findViewById(R.id.button_login);

                    //Cancel
                    btnCancel.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {

                            dialog.dismiss();
                        }
                    });

                    //Join
                    btnJoin.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {

                            Intent i = new Intent(ProductActivity.this, RegisterActivity.class);
                            i.putExtra("From", "ProductActivity");
                            startActivity(i);

                        }
                    });

                    //Log In
                    btnLogin.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {

                            Intent i = new Intent(ProductActivity.this, LoginActivity.class);
                            i.putExtra("From", "ProductActivity");
                            startActivity(i);
                        }
                    });
                    dialog.show();
                }
            }
        });
    }

    private class LoadProduct extends AsyncTask<String, Void, Void> {

        @Override
        protected void onPreExecute() {
            super.onPreExecute();

            // Showing progress dialog
            pDialog = new ProgressDialog(ProductActivity.this);
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
            try {

                String success = json.getString("success");

                if (success.equals("true")) {

                    JSONObject productObj = json.getJSONObject("data");
                    product_name_st = productObj.getString("name");
                    price_st = productObj.getString("price");
                    description_st = productObj.getString("description");
                    size = productObj.getString("size");
                    material = productObj.getString("material");
                    category = productObj.getString("category");
                    style = productObj.getString("style");
                    collection = productObj.getString("collection");
                    availability = productObj.getInt("availibility");
                    offer = productObj.getString("offer");
                    subprice = productObj.getString("subprice");
//                    type = productObj.getString("type");

                    JSONArray imgs = productObj.getJSONArray("images");
                    for (int i = 0; i < imgs.length(); i++) {
                        String str_imgs = imgs.getString(i);
                        Log.i("TAG_imgs", str_imgs);
                        shopImages.add(i, str_imgs);
                    }

                    JSONArray banners = productObj.getJSONArray("banner");
                    for (int i = 0; i < banners.length(); i++) {
                        String banner_img = banners.getString(i);
                        Log.d("Banner", banner_img);
                        bannerImages.add(i, banner_img);
                    }

                    JSONArray similar = productObj.getJSONArray("similar");
                    for (int j = 0; j < similar.length(); j++) {
                        JSONObject similarObj = similar.getJSONObject(j);

                        String id = similarObj.getString("id");
                        String name = similarObj.getString("name");
                        String thumb = similarObj.getString("thumb");
                        String price = similarObj.getString("price");

                        // adding each child node to HashMap key => value
                        HashMap<String, Object> productMap = new HashMap<String, Object>();
                        productMap.put("Id", id);
                        productMap.put("Name", name);
                        productMap.put("Price", price);
                        productMap.put("Thumbnail", thumb);
                        similarDataList.add(productMap);

                    }

                    JSONArray metalObj = productObj.getJSONArray("metal");
                    for (int k = 0; k < metalObj.length(); k++) {
                        JSONObject metObj = metalObj.getJSONObject(k);
                        String name = metObj.getString("name");
                        String hex = metObj.getString("hex");

                        HashMap<String, Object> metalMap = new HashMap<String, Object>();
                        metalMap.put("Hex", hex);
                        metalMap.put("Name", name);
                        metalDataList.add(metalMap);
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

            product_name.setText(product_name_st);
            price.setText(price_st);
            subPrice.setText(subprice);
            description.setText(description_st);
            materials.setText(material);
            dimensions.setText(size);

            if (bannerImages.size() != 0) {
                //banner images
                banner1_st = bannerImages.get(0);
                banner2_st = bannerImages.get(1);
                Picasso.with(ProductActivity.this)
                        .load(banner1_st)
                        .into(banner1);
                Picasso.with(ProductActivity.this)
                        .load(banner2_st)
                        .into(banner2);
            }

            //Check Offer
            if (offer.equals("0")) {
                price.setVisibility(View.GONE);
                offer_view.setVisibility(View.GONE);
            } else {
                subPrice.setPaintFlags(price.getPaintFlags() | Paint.STRIKE_THRU_TEXT_FLAG);
                offer_view.setText(offer + " off");
            }

            //Check Availability
            if (availability == 0) {
                addToBag.setVisibility(View.GONE);
                noStock_view.setVisibility(View.VISIBLE);
            } else if (availability == 2) {
                addToBag.setText("Pre Order Now");
            } else {

            }

            CheckListAdapter mAdapter = new CheckListAdapter(ProductActivity.this);
            pager.setAdapter(mAdapter);
            pager.setPageMargin(4);

            for (int i = 0; i < shopImages.size(); i++) {
                Log.d("Test", shopImages.get(i));
                DefaultSliderView textSliderView = new DefaultSliderView(ProductActivity.this);
                textSliderView.image(shopImages.get(i));
                mSlider.setCustomIndicator(pageIndicator);
                mSlider.addSlider(textSliderView);
            }

            for (int j = 0; j < metalDataList.size(); j++) {

                if (metalDataList.get(j).get("Name").equals("Gold")) {
                    goldLabel.setText("Gold");
                    gold.setVisibility(View.VISIBLE);
                    goldLabel.setVisibility(View.VISIBLE);
                    silver.setVisibility(View.GONE);
                    silverLabel.setVisibility(View.GONE);
                    metalLayout.setGravity(Gravity.CENTER);
                } else if (metalDataList.get(j).get("Name").equals("Silver")) {
                    silverLabel.setText("Silver");
                    gold.setVisibility(View.GONE);
                    goldLabel.setVisibility(View.GONE);
                    silver.setVisibility(View.VISIBLE);
                    silverLabel.setVisibility(View.VISIBLE);
                    metalLayout.setGravity(Gravity.CENTER);
                }
            }
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
            return similarDataList.size();
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
            view_id.setText(similarDataList.get(position).get("Id").toString());
            view_name.setText(similarDataList.get(position).get("Name").toString());
            view_price.setText(similarDataList.get(position).get("Price").toString());
            view_name.setTypeface(font);
            view_price.setTypeface(font_bold);

            //Load Thumbnail
            Picasso.with(ProductActivity.this)
                    .load(similarDataList.get(position).get("Thumbnail").toString())
                    .into(view_thumbnail);
            container.addView(v);

            view_thumbnail.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Intent i = new Intent(ProductActivity.this, ProductActivity.class);
                    i.putExtra("Product Id", view_id.getText());
                    startActivity(i);
                }
            });

            return v;
        }

        @Override
        public void destroyItem(ViewGroup container, int position, Object object) {
            container.removeView((LinearLayout) object);
        }
    }

    @Override
    protected void attachBaseContext(Context context) {
        super.attachBaseContext(CalligraphyContextWrapper.wrap(context));
    }

    @Override
    protected void onPause() {
        super.onPause();
        super.finish();
    }
}
