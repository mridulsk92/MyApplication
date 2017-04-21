package com.example.xts015.myapplication;

import android.graphics.Color;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.design.widget.TabLayout;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.View;
import android.view.Window;

import com.mikepenz.materialdrawer.AccountHeader;
import com.mikepenz.materialdrawer.AccountHeaderBuilder;
import com.mikepenz.materialdrawer.Drawer;
import com.mikepenz.materialdrawer.DrawerBuilder;
import com.mikepenz.materialdrawer.model.DividerDrawerItem;
import com.mikepenz.materialdrawer.model.PrimaryDrawerItem;
import com.mikepenz.materialdrawer.model.ProfileDrawerItem;
import com.mikepenz.materialdrawer.model.SecondaryDrawerItem;
import com.mikepenz.materialdrawer.model.interfaces.IDrawerItem;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class HomeActivity extends AppCompatActivity {

    private Drawer result = null;
    public static final String url = "http://shop.irinerose.com/api/android/home";
    public static final String TAG_SHOP = "shop";
    public static final String TAG_CONTENTS = "contents";
    public static final String TAG_DATA = "data";
    public static final String TAG_IMGS = "imgs";
    public static final String TAG_URL = "url";
    public static final String TAG_TARGET = "target";
    public static final String TAG_HOME = "home";
    public static final String TAG_HOME_OBJ_CONTENTS = "contents";
    public static final String TAG_HOME_OBJ_CONTENTS_OBJ_DATA = "data";
    public static final String TAG_IMG = "img";
    public static final String TAG_HOME_OBJ_CONTENTS_OBJ_URL = "url";
    public static final String TAG_HOME_OBJ_CONTENTS_OBJ_TARGET = "target";

    ArrayList<HashMap<String, Object>> textList;
    ArrayList<HashMap<String, Object>> carouselList = new ArrayList<>();

    ArrayList<String> bannerImages = new ArrayList<String>();
    ArrayList<String> shopImages = new ArrayList<String>();

    ViewPager pager;
    Bundle homeData, shopData;
    TabLayout tabLayout;

    JSONParser jParser = new JSONParser();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home);

        //Initialise
        textList = new ArrayList<>();
        homeData = new Bundle();
        shopData = new Bundle();
        pager = (ViewPager) findViewById(R.id.viewPager);
        tabLayout = (TabLayout) findViewById(R.id.tabDots);

        //Toolbar
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        toolbar.setTitle("Title");

        //Add header to navigation drawer
        AccountHeader headerResult = new AccountHeaderBuilder()
                .withActivity(this)
                .withTextColor(Color.BLACK)
                .withHeightPx(20)
                .withSelectionListEnabledForSingleProfile(false)
                .withHeaderBackground(R.drawable.header_white)
                .addProfiles(
                        new ProfileDrawerItem().withName("Mridul S Kumar").withIcon(getResources().getDrawable(R.drawable.profile))
                ).build();

        //Drawer
        result = new DrawerBuilder()
                .withActivity(this)
//                .withAccountHeader(headerResult)
                .withSelectedItem(-1)
                .withAccountHeader(headerResult)
                .withToolbar(toolbar)

//                .withTranslucentStatusBar(true)
                .withDisplayBelowStatusBar(true)
                .addDrawerItems(
                        new SecondaryDrawerItem().withName("New Arrival").withSelectable(false),
                        new DividerDrawerItem(),
                        new SecondaryDrawerItem().withName("Collection").withSelectable(false),
                        new DividerDrawerItem(),
                        new SecondaryDrawerItem().withName("Smart Modules").withSelectable(false),
                        new DividerDrawerItem(),
                        new SecondaryDrawerItem().withName("Gift Cards").withSelectable(false),
                        new DividerDrawerItem(),
                        new SecondaryDrawerItem().withName("Look Book").withSelectable(false),
                        new DividerDrawerItem(),
                        new SecondaryDrawerItem().withName("Smart Modules").withSelectable(false),
                        new DividerDrawerItem(),
                        new SecondaryDrawerItem().withName("Smart Modules").withSelectable(false),
                        new DividerDrawerItem(),
                        new SecondaryDrawerItem().withName("Smart Modules").withSelectable(false)

                ).withOnDrawerItemClickListener(new Drawer.OnDrawerItemClickListener() {
                    @Override
                    public boolean onItemClick(View view, int position, IDrawerItem drawerItem) {

                        if (drawerItem != null) {
                            if (drawerItem.getIdentifier() == 1) {

                                //Clicked About

                            } else if (drawerItem.getIdentifier() == 2) {

                                //Clicked LogOut

                            } else if (drawerItem.getIdentifier() == 3) {

                            }
                        }
                        return false;
                    }
                })
                .build();

        //Add ToggleButton to ToolBar
        getSupportActionBar().setDisplayHomeAsUpEnabled(false);
        result.getActionBarDrawerToggle().setDrawerIndicatorEnabled(true);

        //Get Json
        new GetImages().execute();

    }

    private class GetImages extends AsyncTask<Void, Void, Void> {

        @Override
        protected Void doInBackground(Void... params) {


            JSONObject json = jParser.getJSONFromUrlByGet(url);
            try {

                JSONObject shop_obj = json.getJSONObject(TAG_SHOP);

                JSONArray shopContents = shop_obj.getJSONArray(TAG_CONTENTS);
                for (int j = 0; j < shopContents.length(); j++) {
                    JSONObject contents_obj = shopContents.getJSONObject(j);
                    JSONObject data_obj = contents_obj.getJSONObject(TAG_DATA);

                    JSONArray imgs = data_obj.getJSONArray(TAG_IMGS);
                    for (int i = 0; i < imgs.length(); i++) {
                        String str_imgs = imgs.getString(i);
                        Log.i("TAG_imgs", str_imgs);
                        shopImages.add(i,str_imgs);
                    }
                    String str_url = contents_obj.getString(TAG_URL);

                    Log.i("TAG_url", str_url);
                    String str_target = contents_obj.getString(TAG_TARGET);

                    Log.i("TAG_target", str_target);
                }
                JSONObject home = json.getJSONObject(TAG_HOME);

                JSONArray homeContents = home.getJSONArray(TAG_HOME_OBJ_CONTENTS);
                for (int i = 0; i < homeContents.length(); i++) {
                    JSONObject home_obj = homeContents.getJSONObject(i);
                    JSONObject home_data = home_obj.getJSONObject(TAG_HOME_OBJ_CONTENTS_OBJ_DATA);

                    String img = home_data.getString(TAG_IMG);
                    bannerImages.add(i, img);
                    Log.i("Image", img);

//                    String home_url = home_data.getString(TAG_HOME_OBJ_CONTENTS_OBJ_URL);
//
//                    Log.i("TAG_home_obj_contents_obj_url", home_url);
//                    String str_home_obj_contents_obj_target = home_data.getString(TAG_HOME_OBJ_CONTENTS_OBJ_TARGET);
//
//                    Log.i("TAG_home_obj_contents_obj_target", str_home_obj_contents_obj_target);
                }

            } catch (JSONException e) {
            }
            return null;
        }

        @Override
        protected void onPostExecute(Void aVoid) {
            super.onPostExecute(aVoid);

            homeData.putStringArrayList("Banner Images", bannerImages);
            shopData.putStringArrayList("Shop Images", shopImages);
            pager.setAdapter(new MyPagerAdapter(getSupportFragmentManager()));
            pager.setCurrentItem(1);
            pager.setPageTransformer(true, new DepthPageTransformer());
//            tabLayout.setupWithViewPager(pager);

        }
    }


    class MyPagerAdapter extends FragmentPagerAdapter {

        public MyPagerAdapter(FragmentManager fragmentManager) {
            super(fragmentManager);

        }

        @Override
        public android.support.v4.app.Fragment getItem(int position) {
            switch (position) {
                case 0:
                    return new AppsFragment();
                case 1:
                    HomeFragment hf = new HomeFragment();
                    hf.setArguments(homeData);
                    return hf;
                case 2:
                    StoreFragment sf = new StoreFragment();
                    sf.setArguments(shopData);
                    return sf;
            }
            return null;
        }

        @Override
        public int getCount() {
            return 3;
        }
    }

    public class DepthPageTransformer implements ViewPager.PageTransformer {
        private static final float MIN_SCALE = 0.75f;

        public void transformPage(View view, float position) {
            int pageWidth = view.getWidth();

            if (position < -1) { // [-Infinity,-1)
                // This page is way off-screen to the left.
                view.setAlpha(0);

            } else if (position <= 0) { // [-1,0]
                // Use the default slide transition when moving to the left page
                view.setAlpha(1);
                view.setTranslationX(0);
                view.setScaleX(1);
                view.setScaleY(1);

            } else if (position <= 1) { // (0,1]
                // Fade the page out.
                view.setAlpha(1 - position);

                // Counteract the default slide transition
                view.setTranslationX(pageWidth * -position);

                // Scale the page down (between MIN_SCALE and 1)
                float scaleFactor = MIN_SCALE
                        + (1 - MIN_SCALE) * (1 - Math.abs(position));
                view.setScaleX(scaleFactor);
                view.setScaleY(scaleFactor);

            } else { // (1,+Infinity]
                // This page is way off-screen to the right.
                view.setAlpha(0);
            }
        }
    }
}
