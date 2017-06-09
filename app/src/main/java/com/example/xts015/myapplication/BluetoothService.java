package com.example.xts015.myapplication;

import android.app.Service;
import android.content.Intent;
import android.os.IBinder;
import android.util.Log;

import com.yc.peddemo.sdk.BLEServiceOperate;
import com.yc.peddemo.sdk.BluetoothLeService;
import com.yc.peddemo.sdk.ICallback;
import com.yc.peddemo.sdk.ICallbackStatus;
import com.yc.peddemo.sdk.ServiceStatusCallback;

public class BluetoothService extends Service implements ServiceStatusCallback, ICallback {

    BLEServiceOperate mBLEServiceOperate;
    BluetoothLeService mBluetoothLeService;
    PreferencesHelper pref;

    public BluetoothService() {
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {

        Log.d("Bluetooth Service :", "Started");

        pref = new PreferencesHelper(BluetoothService.this);
        mBLEServiceOperate = BLEServiceOperate.getInstance(getApplicationContext());
        mBLEServiceOperate.setServiceStatusCallback(BluetoothService.this);

        mBluetoothLeService = mBLEServiceOperate.getBleService();
        if (mBluetoothLeService!=null) {
            mBluetoothLeService.setICallback(BluetoothService.this);
        }

        String address = pref.GetPreferences("Mac Address");
        if(!address.isEmpty()){
            mBLEServiceOperate.connect(address);
        }

        return super.onStartCommand(intent, flags, startId);
    }

    @Override
    public IBinder onBind(Intent intent) {
        // TODO: Return the communication channel to the service.
        throw new UnsupportedOperationException("Not yet implemented");
    }


    @Override
    public void OnServiceStatuslt(int status) {
        if (status== ICallbackStatus.BLE_SERVICE_START_OK) {
            if (mBluetoothLeService==null) {
                mBluetoothLeService = mBLEServiceOperate.getBleService();
                mBluetoothLeService.setICallback(this);
            }
        }
    }

    @Override
    public void OnResult(boolean b, int i) {

    }

    @Override
    public void OnDataResult(boolean b, int i, byte[] bytes) {

    }

//    @Override
//    public void OnResult(boolean result, int status) {
//        // TODO Auto-generated method stub
//        Log.i("TAG", "result=" + result + ",status=" + status);
//        if (status == ICallbackStatus.OFFLINE_STEP_SYNC_OK) {
//            // step snyc complete
//        } else if (status == ICallbackStatus.OFFLINE_SLEEP_SYNC_OK) {
//            // sleep snyc complete
//        } else if (status == ICallbackStatus.SYNC_TIME_OK) {// after set time
//            // finish, then(or delay 20ms) send
//            // to read
//            // localBleVersion
//            // mWriteCommand.sendToReadBLEVersion();
//        } else if (status == ICallbackStatus.GET_BLE_VERSION_OK) {// after read
//            // localBleVersion
//            // finish,
//            // then sync
//            // step
//            // mWriteCommand.syncAllStepData();
//        } else if (status == ICallbackStatus.DISCONNECT_STATUS) {
//            mHandler.sendEmptyMessage(DISCONNECT_MSG);
//        } else if (status == ICallbackStatus.CONNECTED_STATUS) {
//
//            mHandler.sendEmptyMessage(CONNECTED_MSG);
//            mHandler.postDelayed(new Runnable() {
//
//                @Override
//                public void run() {
//                    mWriteCommand.sendToQueryPasswardStatus();
//
//                }
//            }, 150);
//        } else if (status == ICallbackStatus.DISCOVERY_DEVICE_SHAKE) {
//            // Discovery device Shake
//        } else if (status == ICallbackStatus.OFFLINE_RATE_SYNC_OK) {
//            mHandler.sendEmptyMessage(RATE_SYNC_FINISH_MSG);
//        } else if (status == ICallbackStatus.SET_METRICE_OK) {// 设置公制单位成功
//
//        } else if (status == ICallbackStatus.SET_METRICE_OK) {// 设置英制单位成功
//
//        } else if (status == ICallbackStatus.SET_FIRST_ALARM_CLOCK_OK) {// 设置第1个闹钟OK
//
//        } else if (status == ICallbackStatus.SET_SECOND_ALARM_CLOCK_OK) {// 设置第2个闹钟OK
//
//        } else if (status == ICallbackStatus.SET_THIRD_ALARM_CLOCK_OK) {// 设置第3个闹钟OK
//
//        }else if (status == ICallbackStatus.SEND_PHONE_NAME_NUMBER_OK) {//
//            mWriteCommand.sendQQWeChatVibrationCommand(5);
//
//        }else if (status == ICallbackStatus.SEND_QQ_WHAT_SMS_CONTENT_OK) {//
//            mWriteCommand.sendQQWeChatVibrationCommand(1);
//
//        }else if (status==ICallbackStatus.PASSWORD_SET) {
//            Log.d(TAG, "没设置过密码，请设置4位数字密码");
////            mHandler.sendEmptyMessage(SHOW_SET_PASSWORD_MSG);
//
//        }else if (status==ICallbackStatus.PASSWORD_INPUT) {
//            Log.d(TAG, "已设置过密码，请输入已设置的4位数字密码");
////            mHandler.sendEmptyMessage(SHOW_INPUT_PASSWORD_MSG);
//
//        }else if (status==ICallbackStatus.PASSWORD_AUTHENTICATION_OK) {
//            Log.d(TAG, "验证成功或者设置密码成功");
//
//        }else if (status==ICallbackStatus.PASSWORD_INPUT_AGAIN) {
//            Log.d(TAG, "验证失败或者设置密码失败，请重新输入4位数字密码，如果已设置过密码，请输入已设置的密码");
////            mHandler.sendEmptyMessage(SHOW_INPUT_PASSWORD_AGAIN_MSG);
//
//        }else if (status==ICallbackStatus.OFFLINE_SWIM_SYNCING) {
//            Log.d(TAG, "游泳数据同步中");
//        }else if (status==ICallbackStatus.OFFLINE_SWIM_SYNC_OK) {
//            Log.d(TAG, "游泳数据同步完成");
//            mHandler.sendEmptyMessage(OFFLINE_SWIM_SYNC_OK_MSG);
//        }else if (status==ICallbackStatus.OFFLINE_BLOOD_PRESSURE_SYNCING) {
//            Log.d(TAG, "血压数据同步中");
//        }else if (status==ICallbackStatus.OFFLINE_BLOOD_PRESSURE_SYNC_OK) {
//            Log.d(TAG, "血压数据同步完成");
//            mHandler.sendEmptyMessage(OFFLINE_BLOOD_PRESSURE_SYNC_OK_MSG);
//        }
//    }
//
//    @Override
//    public void OnDataResult(boolean result, int status, byte[] data) {
//        StringBuilder stringBuilder = null;
//        if (data != null && data.length > 0) {
//            stringBuilder = new StringBuilder(data.length);
//            for (byte byteChar : data) {
//                stringBuilder.append(String.format("%02X", byteChar));
//            }
//            Log.i("testChannel", "BLE---->APK data =" + stringBuilder.toString());
//        }
//        if (status == ICallbackStatus.OPEN_CHANNEL_OK) {//打开通道OK
//            mHandler.sendEmptyMessage(OPEN_CHANNEL_OK_MSG);
//        } else if (status == ICallbackStatus.CLOSE_CHANNEL_OK) {//关闭通道OK
//            mHandler.sendEmptyMessage(CLOSE_CHANNEL_OK_MSG);
//        }else if (status == ICallbackStatus.BLE_DATA_BACK_OK) {//测试通道OK，通道正常
//            mHandler.sendEmptyMessage(TEST_CHANNEL_OK_MSG);
//        }
    }



