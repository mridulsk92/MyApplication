package com.example.xts015.myapplication;

import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.Typeface;
import android.os.AsyncTask;
import android.os.Bundle;
import android.preference.PreferenceActivity;
import android.support.design.widget.NavigationView;
import android.support.design.widget.TabLayout;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.view.ViewPager;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.text.Spannable;
import android.text.SpannableString;
import android.util.Log;
import android.view.Gravity;
import android.view.Menu;
import android.view.MenuItem;
import android.view.SubMenu;
import android.view.View;
import android.view.Window;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

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

import java.net.InetAddress;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import uk.co.chrisjenx.calligraphy.CalligraphyContextWrapper;

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
    LinearLayout dotsLayout, shop_footer;
    TextView shopLabel, appLabel, homeShopLabel, hybridLabel;
    View line;
    private DrawerLayout drawerLayout;
    Toolbar toolbar;

    JSONParser jParser = new JSONParser();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home);

        //Initialise
        Typeface font = Typeface.createFromAsset(HomeActivity.this.getAssets(), getString(R.string.font_bold));
        textList = new ArrayList<>();
        homeData = new Bundle();
        shopData = new Bundle();
        hybridLabel = (TextView) findViewById(R.id.hybrid_view);
        line = (View) findViewById(R.id.vertical_line);
        pager = (ViewPager) findViewById(R.id.viewPager);
        tabLayout = (TabLayout) findViewById(R.id.tabDots);
        dotsLayout = (LinearLayout) findViewById(R.id.dots_layout);
        shop_footer = (LinearLayout) findViewById(R.id.shop_footer_sub);
        shopLabel = (TextView) findViewById(R.id.shop_label);
        appLabel = (TextView) findViewById(R.id.label_apps);
        homeShopLabel = (TextView) findViewById(R.id.label_shops_home);

        //Toolbar
        toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        toolbar.setTitle("Title");

        initNavigationDrawer();

        new GetImages().execute();

        pager.setOnPageChangeListener(new ViewPager.OnPageChangeListener() {
            @Override
            public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {

            }

            @Override
            public void onPageSelected(int position) {

                if (position == 0) {
                    appLabel.setVisibility(View.GONE);
                    homeShopLabel.setVisibility(View.GONE);

                    hybridLabel.setVisibility(View.GONE);
                    line.setVisibility(View.GONE);
                    shop_footer.setVisibility(View.GONE);
                    shopLabel.setVisibility(View.GONE);
                    dotsLayout.setGravity(Gravity.RIGHT);
                } else if (position == 1) {
                    appLabel.setVisibility(View.VISIBLE);
                    homeShopLabel.setVisibility(View.VISIBLE);

                    hybridLabel.setVisibility(View.GONE);
                    line.setVisibility(View.GONE);
                    shop_footer.setVisibility(View.GONE);
                    shopLabel.setVisibility(View.GONE);
                    dotsLayout.setGravity(Gravity.CENTER);
                } else {
                    appLabel.setVisibility(View.GONE);
                    homeShopLabel.setVisibility(View.GONE);

                    hybridLabel.setVisibility(View.VISIBLE);
//                    line.setVisibility(View.VISIBLE);
                    dotsLayout.setGravity(Gravity.LEFT);
//                    shop_footer.setVisibility(View.VISIBLE);
                    shopLabel.setVisibility(View.VISIBLE);
                }
            }

            @Override
            public void onPageScrollStateChanged(int state) {

            }
        });

    }

    private class GetImages extends AsyncTask<Void, Void, Void> {

        @Override
        protected Void doInBackground(Void... params) {


            JSONObject json = jParser.getJSONFromUrlByGet(url);
            try {

                if (json != null) {
                    JSONObject shop_obj = json.getJSONObject(TAG_SHOP);

                    JSONArray shopContents = shop_obj.getJSONArray(TAG_CONTENTS);
                    for (int j = 0; j < shopContents.length(); j++) {
                        JSONObject contents_obj = shopContents.getJSONObject(j);
                        JSONObject data_obj = contents_obj.getJSONObject(TAG_DATA);

                        JSONArray imgs = data_obj.getJSONArray(TAG_IMGS);
                        for (int i = 0; i < imgs.length(); i++) {
                            String str_imgs = imgs.getString(i);
                            Log.i("TAG_imgs", str_imgs);
                            shopImages.add(i, str_imgs);
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
            tabLayout.setupWithViewPager(pager);

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

    public void initNavigationDrawer() {

        NavigationView navigationView = (NavigationView) findViewById(R.id.navigation_view);
        Menu m = navigationView.getMenu();
        for (int i = 0; i < m.size(); i++) {
            MenuItem mi = m.getItem(i);

            //for aapplying a font to subMenu ...
            SubMenu subMenu = mi.getSubMenu();
            if (subMenu != null && subMenu.size() > 0) {
                for (int j = 0; j < subMenu.size(); j++) {
                    MenuItem subMenuItem = subMenu.getItem(j);
                    applyFontToMenuItem(subMenuItem);
                }
            }

            //the method we have create in activity
            applyFontToMenuItem(mi);
        }
        navigationView.setNavigationItemSelectedListener(new NavigationView.OnNavigationItemSelectedListener() {

            @Override
            public boolean onNavigationItemSelected(MenuItem menuItem) {

                int id = menuItem.getItemId();

                switch (id) {
                    case R.id.new_arrival:
                        Intent newArrivalIntent = new Intent(HomeActivity.this, NewArrivalActivity.class);
                        newArrivalIntent.putExtra("Source", "New Arrival");
                        startActivity(newArrivalIntent);
                        break;
                    case R.id.collection:
                        Intent collectionIntent = new Intent(HomeActivity.this, CollectionActivity.class);
                        startActivity(collectionIntent);
                        break;
                    case R.id.account:
                        Intent accountIntent = new Intent(HomeActivity.this, LoginActivity.class);
                        startActivity(accountIntent);

                }
                return true;
            }
        });
        View header = navigationView.getHeaderView(0);
        TextView signIn = (TextView) header.findViewById(R.id.signin_view);
        signIn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent i = new Intent(HomeActivity.this, LoginActivity.class);
                startActivity(i);
            }
        });
        drawerLayout = (DrawerLayout) findViewById(R.id.drawer);

        ActionBarDrawerToggle actionBarDrawerToggle = new ActionBarDrawerToggle(this, drawerLayout, toolbar, R.string.drawer_open, R.string.drawer_close) {

            @Override
            public void onDrawerClosed(View v) {
                super.onDrawerClosed(v);
            }

            @Override
            public void onDrawerOpened(View v) {
                super.onDrawerOpened(v);
            }
        };
        drawerLayout.addDrawerListener(actionBarDrawerToggle);
        actionBarDrawerToggle.syncState();
    }

    private void applyFontToMenuItem(MenuItem mi) {
        Typeface font = Typeface.createFromAsset(HomeActivity.this.getAssets(), getString(R.string.font));
        SpannableString mNewTitle = new SpannableString(mi.getTitle());
        mNewTitle.setSpan(new CustomTypefaceSpan("", font), 0, mNewTitle.length(), Spannable.SPAN_INCLUSIVE_INCLUSIVE);
        mi.setTitle(mNewTitle);
    }

    @Override
    protected void attachBaseContext(Context context) {
        super.attachBaseContext(CalligraphyContextWrapper.wrap(context));
    }
}
