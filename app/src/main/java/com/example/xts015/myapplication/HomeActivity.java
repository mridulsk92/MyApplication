package com.example.xts015.myapplication;

import android.app.ProgressDialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.graphics.Typeface;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.preference.PreferenceActivity;
import android.support.design.widget.NavigationView;
import android.support.design.widget.TabLayout;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.view.ViewPager;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AlertDialog;
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
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
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
import com.yc.peddemo.sdk.BLEServiceOperate;
import com.yc.peddemo.sdk.BluetoothLeService;
import com.yc.peddemo.sdk.DataProcessing;
import com.yc.peddemo.sdk.ICallback;
import com.yc.peddemo.sdk.ICallbackStatus;
import com.yc.peddemo.sdk.ServiceStatusCallback;
import com.yc.peddemo.sdk.StepChangeListener;
import com.yc.peddemo.sdk.UTESQLOperate;
import com.yc.peddemo.sdk.WriteCommandToBLE;
import com.yc.peddemo.utils.CalendarUtils;
import com.yc.peddemo.utils.GlobalVariable;
import com.yc.pedometer.info.SwimInfo;
import com.yc.pedometer.update.Updates;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.net.InetAddress;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.List;

import uk.co.chrisjenx.calligraphy.CalligraphyContextWrapper;

public class HomeActivity extends AppCompatActivity implements ServiceStatusCallback, ICallback {

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
    Bundle homeData, shopData, appData;
    TabLayout tabLayout;
    LinearLayout dotsLayout, shop_footer;
    TextView shopLabel, appLabel, homeShopLabel, hybridLabel, connectLabel;
    View line;
    private DrawerLayout drawerLayout;
    Toolbar toolbar;
    String deviceName, deviceAddress, deviceFound = "No";

    JSONParser jParser = new JSONParser();
    PreferencesHelper pref;
    String isLogged;
    ProgressDialog pDialog;
    String success_logout, bannerApps;

    /////////////////////////////////////////
    private TextView connect_status, rssi_tv, tv_steps, tv_distance,
            tv_calorie, tv_sleep, tv_deep, tv_light, tv_awake, show_result,
            tv_rate, tv_lowest_rate, tv_verage_rate, tv_highest_rate;
    private EditText et_height, et_weight, et_sedentary_period;
    private Button btn_confirm, btn_sync_step, btn_sync_sleep, update_ble,
            read_ble_version, read_ble_battery, set_ble_time,
            bt_sedentary_open, bt_sedentary_close, btn_sync_rate,
            btn_rate_start, btn_rate_stop, unit, push_message_content;
    private DataProcessing mDataProcessing;
    //    private CustomProgressDialog mProgressDialog;
    private UTESQLOperate mySQLOperate;
    // private PedometerUtils mPedometerUtils;
    private WriteCommandToBLE mWriteCommand;
    private Context mContext;
    private SharedPreferences sp;
    private SharedPreferences.Editor editor;

    private final int UPDATE_STEP_UI_MSG = 0;
    private final int UPDATE_SLEEP_UI_MSG = 1;
    private final int DISCONNECT_MSG = 18;
    private final int CONNECTED_MSG = 19;
    private final int UPDATA_REAL_RATE_MSG = 20;
    private final int RATE_SYNC_FINISH_MSG = 21;
    private final int OPEN_CHANNEL_OK_MSG = 22;
    private final int CLOSE_CHANNEL_OK_MSG = 23;
    private final int TEST_CHANNEL_OK_MSG = 24;
    private final int OFFLINE_SWIM_SYNC_OK_MSG = 25;
    private final int UPDATA_REAL_BLOOD_PRESSURE_MSG = 29;
    private final int OFFLINE_BLOOD_PRESSURE_SYNC_OK_MSG = 30;

    private final long TIME_OUT = 120000;
    private boolean isUpdateSuccess = false;
    private int mSteps = 0;
    private float mDistance = 0f;
    private int mCalories = 0;

    private int mlastStepValue;
    private int stepDistance = 0;
    private int lastStepDistance = 0;

    private boolean isFirstOpenAPK = false;
    private int currentDay = 1;
    private int lastDay = 0;
    private String currentDayString = "20101202";
    private String lastDayString = "20101201";
    private static final int NEW_DAY_MSG = 3;
    protected static final String TAG = "MainActivity";
    private Updates mUpdates;
    private BLEServiceOperate mBLEServiceOperate;
    private BluetoothLeService mBluetoothLeService;
    // caicai add for sdk
    public static final String EXTRAS_DEVICE_NAME = "device_name";
    public static final String EXTRAS_DEVICE_ADDRESS = "device_address";
    private final int CONNECTED = 1;
    private final int CONNECTING = 2;
    private final int DISCONNECTED = 3;
    private int CURRENT_STATUS = DISCONNECTED;

    private String mDeviceName;
    private String mDeviceAddress;

    private int tempRate = 70;
    private int tempStatus;
    private long mExitTime = 0;

    private Button test_channel;
    private StringBuilder resultBuilder = new StringBuilder();

    private TextView swim_time, swim_stroke_count, swim_calorie, tv_low_pressure, tv_high_pressure;
    private Button btn_sync_swim, btn_sync_pressure, btn_start_pressure, btn_stop_pressure;

    private int high_pressure, low_pressure;
    private int tempBloodPressureStatus;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home);

        //Initialise
        Typeface font = Typeface.createFromAsset(HomeActivity.this.getAssets(), getString(R.string.font_bold));
        pref = new PreferencesHelper(HomeActivity.this);
        textList = new ArrayList<>();
        homeData = new Bundle();
        shopData = new Bundle();
        appData = new Bundle();
        hybridLabel = (TextView) findViewById(R.id.hybrid_view);
        line = (View) findViewById(R.id.vertical_line);
        pager = (ViewPager) findViewById(R.id.viewPager);
        tabLayout = (TabLayout) findViewById(R.id.tabDots);
        dotsLayout = (LinearLayout) findViewById(R.id.dots_layout);
        shop_footer = (LinearLayout) findViewById(R.id.shop_footer_sub);
        shopLabel = (TextView) findViewById(R.id.shop_label);
        appLabel = (TextView) findViewById(R.id.label_apps);
        homeShopLabel = (TextView) findViewById(R.id.label_shops_home);
        connectLabel = (TextView) findViewById(R.id.connect_view);

        Intent i = new Intent(HomeActivity.this, BluetoothLeService.class);
        startService(i);

        ////////////////////BLUETOOTH/////////////////////
        mContext = getApplicationContext();
        sp = mContext.getSharedPreferences(GlobalVariable.SettingSP, 0);
        editor = sp.edit();
        mySQLOperate = new UTESQLOperate(mContext);
        mBLEServiceOperate = BLEServiceOperate.getInstance(mContext);
        mBLEServiceOperate.setServiceStatusCallback(this);

        Boolean ble_connect = sp.getBoolean(GlobalVariable.BLE_CONNECTED_SP, false);
        Log.d("TEST CONNETION", String.valueOf(ble_connect));


        mDataProcessing = DataProcessing.getInstance(mContext);
        mDataProcessing.setOnStepChangeListener(mOnStepChangeListener);

        mBluetoothLeService = mBLEServiceOperate.getBleService();
        if (mBluetoothLeService != null) {
            mBluetoothLeService.setICallback(this);
        }

        mRegisterReceiver();
        mWriteCommand = WriteCommandToBLE.getInstance(mContext);
        mUpdates = Updates.getInstance(mContext);
        mUpdates.setHandler(mHandler);// 获取升级操作信息
        mUpdates.registerBroadcastReceiver();
        Log.d("onServerDiscorver", "MainActivity_onCreate   mUpdates  =" + mUpdates);
        Intent intent = getIntent();
        mDeviceName = intent.getStringExtra("Device Name");
        mDeviceAddress = intent.getStringExtra("Device Address");

//        Log.d("HomeActivity", mDeviceAddress);
//        Toast.makeText(HomeActivity.this, "Address"+mDeviceAddress, Toast.LENGTH_SHORT).show();

        mBLEServiceOperate.connect(mDeviceAddress);

        CURRENT_STATUS = CONNECTING;
        upDateTodaySwimData();

        //////////////////////////////////////////////////////////////

        //Toolbar
        toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        toolbar.setTitle("Title");

        //Check If Logged In
        isLogged = pref.GetPreferences("Login");

        //Count Badge and Cart Button
        TextView badge = (TextView) toolbar.findViewById(R.id.textOne);
        ImageView cart = (ImageView) toolbar.findViewById(R.id.cart_view);
        String count = pref.GetPreferences("Cart Count");
        if (isLogged.equals("true")) {
            if (count.equals("0")) {
                badge.setVisibility(View.GONE);
            } else {
                badge.setText(count);
            }
            //onClick of Cart View
            cart.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Intent i = new Intent(HomeActivity.this, CartActivity.class);
                    i.putExtra("Id", "Home");
                    startActivity(i);
                }
            });
            badge.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Intent i = new Intent(HomeActivity.this, CartActivity.class);
                    i.putExtra("Id", "Home");
                    startActivity(i);
                }
            });
        } else {
            badge.setVisibility(View.GONE);
        }

        //Connect onClick
        connectLabel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent i = new Intent(HomeActivity.this, DeviceScanActivity.class);
                startActivity(i);
            }
        });


//        //Get Intent
//        Intent i = getIntent();
//        deviceFound = i.getStringExtra("Device Found");
//        if (deviceFound != null && deviceFound.equals("Yes")) {
//            deviceName = i.getStringExtra("Device Name");
//            deviceAddress = i.getStringExtra("Device Address");
//            Toast.makeText(HomeActivity.this, deviceAddress, Toast.LENGTH_SHORT).show();
//            appData.putString("Address", deviceAddress);
//            appData.putString("Name", deviceName);
//            new GetImages().execute();
//        } else {
//            new GetImages().execute();
//        }

        new GetImages().execute();

        initNavigationDrawer();


        pager.setOnPageChangeListener(new ViewPager.OnPageChangeListener() {
            @Override
            public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {

            }

            @Override
            public void onPageSelected(int position) {

                if (position == 0) {
                    appLabel.setVisibility(View.VISIBLE);
                    homeShopLabel.setVisibility(View.GONE);
                    connectLabel.setVisibility(View.VISIBLE);

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
                    connectLabel.setVisibility(View.GONE);
                    shop_footer.setVisibility(View.GONE);
                    shopLabel.setVisibility(View.GONE);
                    dotsLayout.setGravity(Gravity.CENTER);
                } else {
                    appLabel.setVisibility(View.GONE);
                    homeShopLabel.setVisibility(View.GONE);

                    connectLabel.setVisibility(View.GONE);
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

    private void ConnectBluetooth() {


    }

    private void mRegisterReceiver() {
        IntentFilter mFilter = new IntentFilter();
        mFilter.addAction(GlobalVariable.READ_BATTERY_ACTION);
        mFilter.addAction(GlobalVariable.READ_BLE_VERSION_ACTION);
        registerReceiver(mReceiver, mFilter);
    }

    @Override
    protected void onResume() {
        // TODO Auto-generated method stub
        super.onResume();
        boolean ble_connecte = sp.getBoolean(GlobalVariable.BLE_CONNECTED_SP,
                false);
        if (ble_connecte) {
            Toast.makeText(HomeActivity.this, "Connected", Toast.LENGTH_SHORT).show();
//            connect_status.setText("COnnected");
        } else {
            Toast.makeText(HomeActivity.this, "Disconnected", Toast.LENGTH_SHORT).show();
//            connect_status.setText("Disconnected");
        }
        JudgeNewDayWhenResume();

    }

    private void JudgeNewDayWhenResume() {
        isFirstOpenAPK = sp.getBoolean(GlobalVariable.FIRST_OPEN_APK, true);
        editor.putBoolean(GlobalVariable.FIRST_OPEN_APK, false);
        editor.commit();
        lastDay = sp.getInt(GlobalVariable.LAST_DAY_NUMBER_SP, 0);
        lastDayString = sp.getString(GlobalVariable.LAST_DAY_CALLENDAR_SP,
                "20101201");
        Calendar c = Calendar.getInstance();
        currentDay = c.get(Calendar.DAY_OF_YEAR);
        currentDayString = CalendarUtils.getCalendar(0);

        if (isFirstOpenAPK) {
            lastDay = currentDay;
            lastDayString = currentDayString;
            editor = sp.edit();
            editor.putInt(GlobalVariable.LAST_DAY_NUMBER_SP, lastDay);
            editor.putString(GlobalVariable.LAST_DAY_CALLENDAR_SP,
                    lastDayString);
            editor.commit();
        } else {

            if (currentDay != lastDay) {
                if ((lastDay + 1) == currentDay || currentDay == 1) { // 连续的日期
                    mHandler.sendEmptyMessage(NEW_DAY_MSG);
                } else {
                    mySQLOperate.insertLastDayStepSQL(lastDayString);
                    mySQLOperate.updateSleepSQL();
//                    resetValues();
                }
                lastDay = currentDay;
                lastDayString = currentDayString;
                editor.putInt(GlobalVariable.LAST_DAY_NUMBER_SP, lastDay);
                editor.putString(GlobalVariable.LAST_DAY_CALLENDAR_SP,
                        lastDayString);
                editor.commit();
            } else {
                Log.d("b1offline", "currentDay == lastDay");
            }
        }

    }

    private BroadcastReceiver mReceiver = new BroadcastReceiver() {

        @Override
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            if (action.equals(GlobalVariable.READ_BLE_VERSION_ACTION)) {
                String version = intent
                        .getStringExtra(GlobalVariable.INTENT_BLE_VERSION_EXTRA);
                if (sp.getBoolean(BluetoothLeService.IS_RK_PLATFORM_SP, false)) {
                    show_result.setText("version=" + version + "," + sp.getString(GlobalVariable.PATH_LOCAL_VERSION_NAME_SP, ""));
                } else {
                    show_result.setText("version=" + version);
                }

            } else if (action.equals(GlobalVariable.READ_BATTERY_ACTION)) {
                int battery = intent.getIntExtra(
                        GlobalVariable.INTENT_BLE_BATTERY_EXTRA, -1);
                show_result.setText("battery=" + battery);

            }
        }
    };

    @Override
    protected void onDestroy() {
        super.onDestroy();
        Log.d("onServerDiscorver", "MainActivity_onDestroy");
        GlobalVariable.BLE_UPDATE = false;
        mUpdates.unRegisterBroadcastReceiver();
        try {
            unregisterReceiver(mReceiver);
        } catch (Exception e) {
            // TODO: handle exception
        }

//        if (mProgressDialog != null) {
//            mProgressDialog.dismiss();
//            mProgressDialog = null;
//        }
//        if (mDialogRunnable != null)
//            mHandler.removeCallbacks(mDialogRunnable);

        mBLEServiceOperate.disConnect();
    }

    @Override
    public void OnResult(boolean result, int status) {
        // TODO Auto-generated method stub
        Log.i(TAG, "result=" + result + ",status=" + status);
        if (status == ICallbackStatus.OFFLINE_STEP_SYNC_OK) {
            // step snyc complete
        } else if (status == ICallbackStatus.OFFLINE_SLEEP_SYNC_OK) {
            // sleep snyc complete
        } else if (status == ICallbackStatus.SYNC_TIME_OK) {// after set time
            // finish, then(or delay 20ms) send
            // to read
            // localBleVersion
            // mWriteCommand.sendToReadBLEVersion();
        } else if (status == ICallbackStatus.GET_BLE_VERSION_OK) {// after read
            // localBleVersion
            // finish,
            // then sync
            // step
            // mWriteCommand.syncAllStepData();
        } else if (status == ICallbackStatus.DISCONNECT_STATUS) {
            mHandler.sendEmptyMessage(DISCONNECT_MSG);
        } else if (status == ICallbackStatus.CONNECTED_STATUS) {

            mHandler.sendEmptyMessage(CONNECTED_MSG);
            mHandler.postDelayed(new Runnable() {

                @Override
                public void run() {
                    mWriteCommand.sendToQueryPasswardStatus();

                }
            }, 150);
        } else if (status == ICallbackStatus.DISCOVERY_DEVICE_SHAKE) {
            // Discovery device Shake
        } else if (status == ICallbackStatus.OFFLINE_RATE_SYNC_OK) {
            mHandler.sendEmptyMessage(RATE_SYNC_FINISH_MSG);
        } else if (status == ICallbackStatus.SET_METRICE_OK) {// 设置公制单位成功

        } else if (status == ICallbackStatus.SET_METRICE_OK) {// 设置英制单位成功

        } else if (status == ICallbackStatus.SET_FIRST_ALARM_CLOCK_OK) {// 设置第1个闹钟OK

        } else if (status == ICallbackStatus.SET_SECOND_ALARM_CLOCK_OK) {// 设置第2个闹钟OK

        } else if (status == ICallbackStatus.SET_THIRD_ALARM_CLOCK_OK) {// 设置第3个闹钟OK

        } else if (status == ICallbackStatus.SEND_PHONE_NAME_NUMBER_OK) {//
            mWriteCommand.sendQQWeChatVibrationCommand(5);

        } else if (status == ICallbackStatus.SEND_QQ_WHAT_SMS_CONTENT_OK) {//
            mWriteCommand.sendQQWeChatVibrationCommand(1);

        } else if (status == ICallbackStatus.PASSWORD_SET) {
            Log.d(TAG, "没设置过密码，请设置4位数字密码");
//            mHandler.sendEmptyMessage(SHOW_SET_PASSWORD_MSG);

        } else if (status == ICallbackStatus.PASSWORD_INPUT) {
            Log.d(TAG, "已设置过密码，请输入已设置的4位数字密码");
//            mHandler.sendEmptyMessage(SHOW_INPUT_PASSWORD_MSG);

        } else if (status == ICallbackStatus.PASSWORD_AUTHENTICATION_OK) {
            Log.d(TAG, "验证成功或者设置密码成功");

        } else if (status == ICallbackStatus.PASSWORD_INPUT_AGAIN) {
            Log.d(TAG, "验证失败或者设置密码失败，请重新输入4位数字密码，如果已设置过密码，请输入已设置的密码");
//            mHandler.sendEmptyMessage(SHOW_INPUT_PASSWORD_AGAIN_MSG);

        } else if (status == ICallbackStatus.OFFLINE_SWIM_SYNCING) {
            Log.d(TAG, "游泳数据同步中");
        } else if (status == ICallbackStatus.OFFLINE_SWIM_SYNC_OK) {
            Log.d(TAG, "游泳数据同步完成");
            mHandler.sendEmptyMessage(OFFLINE_SWIM_SYNC_OK_MSG);
        } else if (status == ICallbackStatus.OFFLINE_BLOOD_PRESSURE_SYNCING) {
            Log.d(TAG, "血压数据同步中");
        } else if (status == ICallbackStatus.OFFLINE_BLOOD_PRESSURE_SYNC_OK) {
            Log.d(TAG, "血压数据同步完成");
            mHandler.sendEmptyMessage(OFFLINE_BLOOD_PRESSURE_SYNC_OK_MSG);
        }
    }

    private final String testKey1 = "00a4040008A000000333010101";

    @Override
    public void OnDataResult(boolean result, int status, byte[] data) {
        StringBuilder stringBuilder = null;
        if (data != null && data.length > 0) {
            stringBuilder = new StringBuilder(data.length);
            for (byte byteChar : data) {
                stringBuilder.append(String.format("%02X", byteChar));
            }
            Log.i("testChannel", "BLE---->APK data =" + stringBuilder.toString());
        }
        if (status == ICallbackStatus.OPEN_CHANNEL_OK) {//打开通道OK
            mHandler.sendEmptyMessage(OPEN_CHANNEL_OK_MSG);
        } else if (status == ICallbackStatus.CLOSE_CHANNEL_OK) {//关闭通道OK
            mHandler.sendEmptyMessage(CLOSE_CHANNEL_OK_MSG);
        } else if (status == ICallbackStatus.BLE_DATA_BACK_OK) {//测试通道OK，通道正常
            mHandler.sendEmptyMessage(TEST_CHANNEL_OK_MSG);
        }
    }

    @Override
    public void OnServiceStatuslt(int status) {
        if (status == ICallbackStatus.BLE_SERVICE_START_OK) {
            if (mBluetoothLeService == null) {
                mBluetoothLeService = mBLEServiceOperate.getBleService();
                mBluetoothLeService.setICallback(this);
            }
        }
    }

    private class GetImages extends AsyncTask<Void, Void, Void> {

        @Override
        protected Void doInBackground(Void... params) {


            String token = pref.GetPreferences("Token");
            JSONObject json = jParser.getJSONFromUrlByGet(url, token);
            try {

                if (json != null) {

                    //Shop
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

                    //Home
                    JSONObject home = json.getJSONObject(TAG_HOME);
                    JSONArray homeContents = home.getJSONArray(TAG_HOME_OBJ_CONTENTS);
                    for (int i = 0; i < homeContents.length(); i++) {
                        JSONObject home_obj = homeContents.getJSONObject(i);
                        JSONObject home_data = home_obj.getJSONObject(TAG_HOME_OBJ_CONTENTS_OBJ_DATA);

                        String img = home_data.getString(TAG_IMG);
                        bannerImages.add(i, img);
                        Log.i("Image", img);
                    }

                    //Apps
                    JSONObject apps = json.getJSONObject("apps");
                    JSONObject appContent = apps.getJSONObject("contents");
                    JSONObject data = appContent.getJSONObject("data");
                    bannerApps = data.getString("banner");
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
            appData.putString("banner", bannerApps);
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
                    AppsFragment af = new AppsFragment();
                    af.setArguments(appData);
                    return af;
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

                        if (isLogged.equals("true")) {
                            Toast.makeText(HomeActivity.this, "Logged In", Toast.LENGTH_SHORT).show();
                        } else {
                            Intent accountIntent = new Intent(HomeActivity.this, LoginActivity.class);
                            accountIntent.putExtra("From", "HomeActivity");
                            startActivity(accountIntent);
                        }
                }
                return true;
            }
        });
        View header = navigationView.getHeaderView(0);
        TextView signIn = (TextView) header.findViewById(R.id.signin_view);
        TextView nameView = (TextView) header.findViewById(R.id.textView_name);

        if (pref.GetPreferences("Login").equals("true")) {
            nameView.setText(pref.GetPreferences("UserName"));
            signIn.setText("Log Out");
        } else {
            nameView.setText("I'm BAO");
            signIn.setText("Sign In");
        }
        signIn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                if (pref.GetPreferences("Login").equals("true")) {

                    AlertDialog.Builder dialog = new AlertDialog.Builder(HomeActivity.this);
                    dialog.setCancelable(false);
                    dialog.setTitle("Confirm");
                    dialog.setMessage("Are you sure you want to Log Out?");
                    dialog.setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int id) {
                            //Action for Yes
                            new LogOut().execute();
                        }
                    }).setNegativeButton("Cancel ", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            //Action for "Cancel".
                            dialog.cancel();
                        }
                    });

                    final AlertDialog alert = dialog.create();
                    alert.show();

                } else {
                    Intent i = new Intent(HomeActivity.this, LoginActivity.class);
                    i.putExtra("From", "HomeActivity");
                    startActivity(i);
                }

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

    private class LogOut extends AsyncTask<Void, Void, Void> {

        @Override
        protected void onPreExecute() {
            super.onPreExecute();

            // Showing progress dialog
            pDialog = new ProgressDialog(HomeActivity.this);
            pDialog.setMessage("Please Wait");
            pDialog.setCancelable(false);
            pDialog.show();
        }

        @Override
        protected Void doInBackground(Void... params) {

            String token = pref.GetPreferences("Token");
            String url_logout = "http://shop.irinerose.com/api/user/logout";
            JSONObject json = jParser.getJSONFromUrlByGet(url_logout, token);
            Log.d("TEST", json.toString());
            if (json != null) {
                try {

                    success_logout = json.getString("success");

                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }

            return null;

        }

        @Override
        protected void onPostExecute(Void aVoid) {
            super.onPostExecute(aVoid);

            if (pDialog.isShowing()) {
                pDialog.dismiss();
            }

            if (success_logout.equals("true")) {
                pref.ClearPreferences();
                Toast.makeText(HomeActivity.this, "You Have been Logged Out", Toast.LENGTH_SHORT).show();
                Intent i = new Intent(HomeActivity.this, HomeActivity.class);
                startActivity(i);
            } else {
                Toast.makeText(HomeActivity.this, "Log Out Failed. Please try again", Toast.LENGTH_SHORT).show();
            }
        }
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

//    @Override
//    protected void onPause() {
//        super.onPause();
//        super.finish();
//    }

    private void upDateTodaySwimData() {
        // TODO Auto-generated method stub
        SwimInfo mSwimInfo = mySQLOperate.querySwimData(CalendarUtils.getCalendar(0));//传入日期，0为今天，-1为昨天，-2为前天。。。。
        if (mSwimInfo != null) {
            swim_time.setText(mSwimInfo.getSwimTime() + "");
            swim_stroke_count.setText(mSwimInfo.getSwimStrokeCount() + "");
            swim_calorie.setText(mSwimInfo.getCalories() + "");
        }
    }

    ;

    private StepChangeListener mOnStepChangeListener = new StepChangeListener() {

        @Override
        public void onStepChange(int steps, float distance, int calories) {
            Log.d("onStepHandler", "steps =" + steps + ",distance =" + distance
                    + ",calories =" + calories);
            mSteps = steps;
            mDistance = distance;
            mCalories = calories;
            mHandler.sendEmptyMessage(UPDATE_STEP_UI_MSG);
        }
    };

    private Handler mHandler = new Handler() {
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case RATE_SYNC_FINISH_MSG:
//                    UpdateUpdataRateMainUI(CalendarUtils.getCalendar(0));
                    Toast.makeText(mContext, "Rate sync finish", 0).show();
                    break;
                case UPDATA_REAL_RATE_MSG:
                    tv_rate.setText(tempRate + "");// 实时跳变
                    if (tempStatus == GlobalVariable.RATE_TEST_FINISH) {
//                        UpdateUpdataRateMainUI(CalendarUtils.getCalendar(0));
                        Toast.makeText(mContext, "Rate test finish", 0).show();
                    }
                    break;
                case GlobalVariable.GET_RSSI_MSG:
                    Bundle bundle = msg.getData();
//                    rssi_tv.setText(bundle.getInt(GlobalVariable.EXTRA_RSSI) + "");
                    break;
                case UPDATE_STEP_UI_MSG:
//                    updateSteps(mSteps);
//                    updateCalories(mCalories);
//                    updateDistance(mDistance);
                    pref.SavePreferences("Steps", String.valueOf(mSteps));
                    pref.SavePreferences("Distance", String.valueOf(mDistance));
                    pref.SavePreferences("Calories", String.valueOf(mCalories));

//                    appData.putString("Steps", String.valueOf(mSteps));
//                    appData.putString("Distance", String.valueOf(mDistance));
//                    appData.putString("Calories", String.valueOf(mCalories));


                    Log.d("onStepHandler", "mSteps =" + mSteps + ",mDistance ="
                            + mDistance + ",mCalories =" + mCalories);
                    break;
                case UPDATE_SLEEP_UI_MSG:
//                    querySleepInfo();
                    Log.d("getSleepInfo", "UPDATE_SLEEP_UI_MSG");
                    break;
                case NEW_DAY_MSG:
                    mySQLOperate.updateStepSQL();
                    mySQLOperate.updateSleepSQL();
                    mySQLOperate.updateRateSQL();
                    mySQLOperate.isDeleteRefreshTable();
//                    resetValues();
                    break;
                case GlobalVariable.START_PROGRESS_MSG:
                    Log.i(TAG, "(Boolean) msg.obj=" + (Boolean) msg.obj);
                    isUpdateSuccess = (Boolean) msg.obj;
                    Log.i(TAG, "BisUpdateSuccess=" + isUpdateSuccess);
//                    startProgressDialog();
//                    mHandler.postDelayed(mDialogRunnable, TIME_OUT);
                    break;
                case GlobalVariable.DOWNLOAD_IMG_FAIL_MSG:
//                    Toast.makeText(MainActivity.this, R.string.download_fail, 1).show();
//                    if (mProgressDialog != null) {
//                        mProgressDialog.dismiss();
//                        mProgressDialog = null;
//                    }
//                    if (mDialogRunnable != null)
//                        mHandler.removeCallbacks(mDialogRunnable);
                    break;
                case GlobalVariable.DISMISS_UPDATE_BLE_DIALOG_MSG:
                    Log.i(TAG, "(Boolean) msg.obj=" + (Boolean) msg.obj);
                    isUpdateSuccess = (Boolean) msg.obj;
                    Log.i(TAG, "BisUpdateSuccess=" + isUpdateSuccess);
//                    if (mProgressDialog != null) {
//                        mProgressDialog.dismiss();
//                        mProgressDialog = null;
//                    }
//                    if (mDialogRunnable != null) {
//                        mHandler.removeCallbacks(mDialogRunnable);
//                    }
//
//                    if (isUpdateSuccess) {
//                        Toast.makeText(
//                                mContext,
//                                getResources().getString(
//                                        R.string.ble_update_successful), 0).show();
//                    }
                    break;
                case GlobalVariable.SERVER_IS_BUSY_MSG:
//                    Toast.makeText(mContext,
//                            getResources().getString(R.string.server_is_busy), 0)
//                            .show();
                    break;
                case DISCONNECT_MSG:
                    Toast.makeText(HomeActivity.this, "Disconnected", Toast.LENGTH_SHORT).show();
//                    connect_status.setText("Disconnected");
                    CURRENT_STATUS = DISCONNECTED;
                    Toast.makeText(mContext, "disconnect or connect falie", 0)
                            .show();

                    String lastConnectAddr0 = sp.getString(GlobalVariable.LAST_CONNECT_DEVICE_ADDRESS_SP, "00:00:00:00:00:00");
                    boolean connectResute0 = mBLEServiceOperate.connect(lastConnectAddr0);
                    Log.i(TAG, "connectResute0=" + connectResute0);

                    break;
                case CONNECTED_MSG:
                    Toast.makeText(HomeActivity.this, "Connected", Toast.LENGTH_SHORT).show();
//                    connect_status.setText("Connected");
                    mBluetoothLeService.setRssiHandler(mHandler);
                    new Thread(new Runnable() {
                        @Override
                        public void run() {
                            while (!Thread.interrupted()) {
                                try {
                                    Thread.sleep(1000);
                                } catch (InterruptedException e) {
                                    // TODO Auto-generated catch block
                                    e.printStackTrace();
                                }
                                if (mBluetoothLeService != null) {
                                    mBluetoothLeService.readRssi();
                                }
                            }
                        }
                    }).start();
                    CURRENT_STATUS = CONNECTED;
                    Toast.makeText(mContext, "connected", 0).show();
                    break;

                case GlobalVariable.UPDATE_BLE_PROGRESS_MSG: // (新) 增加固件升级进度
                    int schedule = msg.arg1;
                    Log.i("zznkey", "schedule =" + schedule);
//                    if (mProgressDialog == null) {
//                        startProgressDialog();
//                    }
//                    mProgressDialog.setSchedule(schedule);
                    break;
                case OPEN_CHANNEL_OK_MSG://打开通道OK
//                    test_channel.setText(getResources().getString(R.string.open_channel_ok));
//                    resultBuilder.append(getResources().getString(R.string.open_channel_ok)+",");
                    show_result.setText(resultBuilder.toString());

//                    mWriteCommand.sendAPDUToBLE(WriteCommandToBLE.hexString2Bytes(testKey1));
                    break;
                case CLOSE_CHANNEL_OK_MSG://关闭通道OK
//                    test_channel.setText(getResources().getString(R.string.close_channel_ok));
//                    resultBuilder.append(getResources().getString(R.string.close_channel_ok)+",");
//                    show_result.setText(resultBuilder.toString());
                    break;
                case TEST_CHANNEL_OK_MSG://通道测试OK
//                    test_channel.setText(getResources().getString(R.string.test_channel_ok));
//                    resultBuilder.append(getResources().getString(R.string.test_channel_ok)+",");
                    show_result.setText(resultBuilder.toString());
                    mWriteCommand.closeBLEchannel();
                    break;

//                case SHOW_SET_PASSWORD_MSG:
//                    showPasswordDialog(GlobalVariable.PASSWORD_TYPE_SET);
//                    break;
//                case SHOW_INPUT_PASSWORD_MSG:
//                    showPasswordDialog(GlobalVariable.PASSWORD_TYPE_INPUT);
//                    break;
//                case SHOW_INPUT_PASSWORD_AGAIN_MSG:
//                    showPasswordDialog(GlobalVariable.PASSWORD_TYPE_INPUT_AGAIN);
//                    break;
//                case OFFLINE_SWIM_SYNC_OK_MSG:
//                    upDateTodaySwimData();
//                    Toast.makeText(MainActivity.this,
//                            getResources().getString(R.string.sync_swim_finish), 0)
//                            .show();
//                    break;
//
//                case UPDATA_REAL_BLOOD_PRESSURE_MSG:
//                    tv_low_pressure.setText(low_pressure+"");// 实时跳变
//                    tv_high_pressure.setText(high_pressure+"");// 实时跳变
//                    if (tempBloodPressureStatus == GlobalVariable.BLOOD_PRESSURE_TEST_FINISH) {
//                        UpdateBloodPressureMainUI(CalendarUtils.getCalendar(0));
//                        Toast.makeText(mContext, getResources().getString(R.string.test_pressure_ok), 0).show();
//                    }
//                    break;
//                case OFFLINE_BLOOD_PRESSURE_SYNC_OK_MSG:
//                    UpdateBloodPressureMainUI(CalendarUtils.getCalendar(0));
//                    Toast.makeText(MainActivity.this,
//                            getResources().getString(R.string.sync_pressure_ok), 0)
//                            .show();
//                    break;
//                default:
//                    break;
            }
        }
    };
}
