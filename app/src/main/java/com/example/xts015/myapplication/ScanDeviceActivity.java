package com.example.xts015.myapplication;

import android.Manifest;
import android.app.Activity;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Handler;
import android.os.Message;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.yc.peddemo.sdk.BLEServiceOperate;
import com.yc.peddemo.sdk.BluetoothLeService;
import com.yc.peddemo.sdk.DeviceScanInterfacer;
import com.yc.peddemo.sdk.ICallback;
import com.yc.peddemo.sdk.ICallbackStatus;
import com.yc.peddemo.sdk.ServiceStatusCallback;
import com.yc.peddemo.sdk.WriteCommandToBLE;
import com.yc.peddemo.utils.CalendarUtils;
import com.yc.peddemo.utils.GlobalVariable;
import com.yc.pedometer.update.Updates;

import java.util.ArrayList;

public class ScanDeviceActivity extends AppCompatActivity implements DeviceScanInterfacer, ServiceStatusCallback, ICallback {

    private LeDeviceListAdapter mLeDeviceListAdapter;
    private boolean mScanning;
    private Updates mUpdates;


    private  Handler mHandlerBluetooth;

    private boolean isUpdateSuccess = false;

    private final int DISCONNECT_MSG = 18;
    private final int CONNECTED_MSG = 19;

    private final int REQUEST_ENABLE_BT = 1;
    // Stops scanning after 10 seconds.
    private final long SCAN_PERIOD = 10000;

    private BLEServiceOperate mBLEServiceOperate;
    private BluetoothLeService mBluetoothLeService;

    ListView deviceList;
    String deviceAddress, deviceName;

    private final int CONNECTED = 1;
    private final int CONNECTING = 2;
    private final int DISCONNECTED = 3;
    private int CURRENT_STATUS = DISCONNECTED;
    private WriteCommandToBLE mWriteCommand;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_scan_device);


        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            if (this.checkSelfPermission(Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                final AlertDialog.Builder builder = new AlertDialog.Builder(this);
                builder.setTitle("This app needs location access");
                builder.setMessage("Please grant location access so this app can detect beacons.");
                builder.setPositiveButton(android.R.string.ok, null);
                builder.setOnDismissListener(new DialogInterface.OnDismissListener() {
                    @Override
                    public void onDismiss(DialogInterface dialog) {
                        requestPermissions(new String[]{Manifest.permission.ACCESS_COARSE_LOCATION}, 101);
                    }
                });
                builder.show();
            }
        }

        deviceList = (ListView) findViewById(R.id.scan_list);
        mHandlerBluetooth = new Handler();

        mBLEServiceOperate = BLEServiceOperate.getInstance(getApplicationContext());
        mBLEServiceOperate.setServiceStatusCallback(ScanDeviceActivity.this);

        mWriteCommand = WriteCommandToBLE.getInstance(ScanDeviceActivity.this);
        mUpdates = Updates.getInstance(ScanDeviceActivity.this);
        mUpdates.setHandler(mHandler);

        mBluetoothLeService = mBLEServiceOperate.getBleService();
        if (mBluetoothLeService != null) {
            mBluetoothLeService.setICallback(ScanDeviceActivity.this);
        }

        // Checks if Bluetooth is supported on the device.
        if (!mBLEServiceOperate.isSupportBle4_0()) {
            Toast.makeText(this, "Bluetooth Not Supported", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        mBLEServiceOperate.setDeviceScanListener(this);

        deviceList.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {

                final BluetoothDevice device = mLeDeviceListAdapter.getDevice(position);
                if (device == null)
                    return;

                deviceAddress = device.getAddress();
                deviceName = device.getName();

//                final Intent intent = new Intent(ScanDeviceActivity.this, HomeActivity.class);
//                intent.putExtra("Device Found", "Yes");
//                intent.putExtra("Device Name", device.getName());
//                intent.putExtra("Device Address", device.getAddress());
                if (mScanning) {
                    mBLEServiceOperate.stopLeScan();
                    mScanning = false;
                }
//                startActivity(intent);
                mBLEServiceOperate.connect(deviceAddress);
                CURRENT_STATUS = CONNECTING;

            }
        });
    }

    @Override
    public void onRequestPermissionsResult(int requestCode,
                                           String permissions[],
                                           int[] grantResults) {
        switch (requestCode) {
            case 101: {
                if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    Log.d("TAG", "coarse location permission granted");
                } else {
                    final AlertDialog.Builder builder = new AlertDialog.Builder(this);
                    builder.setTitle("Functionality limited");
                    builder.setMessage("Since location access has not been granted, this app will not be able to discover beacons when in the background.");
                    builder.setPositiveButton(android.R.string.ok, null);
                    builder.setOnDismissListener(new DialogInterface.OnDismissListener() {
                        @Override
                        public void onDismiss(DialogInterface dialog) {
                        }
                    });
                    builder.show();
                }
                return;
            }
        }
    }

    @Override
    protected void onResume() {
        super.onResume();

        // Ensures Bluetooth is enabled on the device. If Bluetooth is not
        // currently enabled,
        // fire an intent to display a dialog asking the user to grant
        // permission to enable it.
        if (!mBLEServiceOperate.isBleEnabled()) {
            Intent enableBtIntent = new Intent(
                    BluetoothAdapter.ACTION_REQUEST_ENABLE);
            startActivityForResult(enableBtIntent, REQUEST_ENABLE_BT);
        }

        // Initializes list view adapter.
        mLeDeviceListAdapter = new LeDeviceListAdapter();
        deviceList.setAdapter(mLeDeviceListAdapter);
        scanLeDevice(true);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        // User chose not to enable Bluetooth.
        if (requestCode == REQUEST_ENABLE_BT
                && resultCode == Activity.RESULT_CANCELED) {
            finish();
            return;
        }
        super.onActivityResult(requestCode, resultCode, data);
    }

    @Override
    protected void onPause() {
        super.onPause();
        scanLeDevice(false);
        mLeDeviceListAdapter.clear();
    }

    @Override
    protected void onDestroy() {
        // TODO Auto-generated method stub
        super.onDestroy();
        mBLEServiceOperate.unBindService();// unBindService
    }

    private void scanLeDevice(final boolean enable) {
        if (enable) {
            // Stops scanning after a pre-defined scan period.
            mHandlerBluetooth.postDelayed(new Runnable() {
                @Override
                public void run() {
                    mScanning = false;
                    mBLEServiceOperate.stopLeScan();
                    invalidateOptionsMenu();
                }
            }, SCAN_PERIOD);

            mScanning = true;
            mBLEServiceOperate.startLeScan();
        } else {
            mScanning = false;
            mBLEServiceOperate.stopLeScan();
        }
        invalidateOptionsMenu();
    }

    @Override
    public void OnServiceStatuslt(int i) {

        if (i == ICallbackStatus.BLE_SERVICE_START_OK) {
            if (mBluetoothLeService == null) {
                mBluetoothLeService = mBLEServiceOperate.getBleService();
                mBluetoothLeService.setICallback(this);
            }
        }
    }

    @Override
    public void OnResult(boolean result, int status) {
        // TODO Auto-generated method stub
        Log.i("TAG", "result=" + result + ",status=" + status);
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
//            mHandler.sendEmptyMessage(RATE_SYNC_FINISH_MSG);
        } else if (status == ICallbackStatus.SET_METRICE_OK) {// 设置公制单位成功

        } else if (status == ICallbackStatus.SET_METRICE_OK) {// 设置英制单位成功

        } else if (status == ICallbackStatus.SET_FIRST_ALARM_CLOCK_OK) {// 设置第1个闹钟OK

        } else if (status == ICallbackStatus.SET_SECOND_ALARM_CLOCK_OK) {// 设置第2个闹钟OK

        } else if (status == ICallbackStatus.SET_THIRD_ALARM_CLOCK_OK) {// 设置第3个闹钟OK

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
//            mHandler.sendEmptyMessage(OPEN_CHANNEL_OK_MSG);
        } else if (status == ICallbackStatus.CLOSE_CHANNEL_OK) {//关闭通道OK
//            mHandler.sendEmptyMessage(CLOSE_CHANNEL_OK_MSG);
        }else if (status == ICallbackStatus.BLE_DATA_BACK_OK) {//测试通道OK，通道正常
//            mHandler.sendEmptyMessage(TEST_CHANNEL_OK_MSG);
        }
    }

    // Adapter for holding devices found through scanning.
    private class LeDeviceListAdapter extends BaseAdapter {
        private ArrayList<BluetoothDevice> mLeDevices;
        private LayoutInflater mInflator;

        public LeDeviceListAdapter() {
            super();
            mLeDevices = new ArrayList<BluetoothDevice>();
            mInflator = getLayoutInflater();
        }

        public void addDevice(BluetoothDevice device) {
            if (!mLeDevices.contains(device)) {
                mLeDevices.add(device);
            }
        }

        public BluetoothDevice getDevice(int position) {
            return mLeDevices.get(position);
        }

        public void clear() {
            mLeDevices.clear();
        }

        @Override
        public int getCount() {
            return mLeDevices.size();
        }

        @Override
        public Object getItem(int i) {
            return mLeDevices.get(i);
        }

        @Override
        public long getItemId(int i) {
            return i;
        }

        @Override
        public View getView(int i, View view, ViewGroup viewGroup) {
            ViewHolder viewHolder;
            // General ListView optimization code.
            if (view == null) {
                view = mInflator.inflate(R.layout.listitem_device, null);
                viewHolder = new ViewHolder();
                viewHolder.deviceName = (TextView) view
                        .findViewById(R.id.device_name);
                viewHolder.deviceAddress = (TextView) view
                        .findViewById(R.id.device_address);
                view.setTag(viewHolder);
            } else {
                viewHolder = (ViewHolder) view.getTag();
            }

            BluetoothDevice device = mLeDevices.get(i);
            final String deviceName = device.getName();
            if (deviceName != null && deviceName.length() > 0)
                viewHolder.deviceName.setText(deviceName);
            else
                viewHolder.deviceName.setText("Unkown Device");
            viewHolder.deviceAddress.setText(device.getAddress());

            return view;
        }
    }

    static class ViewHolder {
        TextView deviceName;
        TextView deviceAddress;
    }

    @Override
    public void LeScanCallback(final BluetoothDevice device, final int rssi) {
        // TODO Auto-generated method stub
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                mLeDeviceListAdapter.addDevice(device);
                mLeDeviceListAdapter.notifyDataSetChanged();
            }
        });
    }

    private Handler mHandler = new Handler() {
        public void handleMessage(Message msg) {
            switch (msg.what) {


                case DISCONNECT_MSG:
                    CURRENT_STATUS = DISCONNECTED;
                    Toast.makeText(ScanDeviceActivity.this, "disconnect or connect falie", 0)
                            .show();

//                    String lastConnectAddr0 = sp.getString(
//                            GlobalVariable.LAST_CONNECT_DEVICE_ADDRESS_SP, "00:00:00:00:00:00");
//                    boolean connectResute0 = mBLEServiceOperate
//                            .connect(lastConnectAddr0);
//                    Log.i(TAG, "connectResute0=" + connectResute0);

                    break;
                case CONNECTED_MSG:
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
                    Toast.makeText(ScanDeviceActivity.this, "connected", 0).show();
                    break;

                default:
                    break;
            }
        }
    };
}
