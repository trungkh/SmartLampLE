/*
 * Copyright 2015 Trung Huynh
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package le.bluetooth;

import java.util.List;

import android.bluetooth.BluetoothGattCharacteristic;
import android.bluetooth.BluetoothGattService;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.ServiceConnection;
import android.os.IBinder;
import android.util.Log;
import le.bluetooth.smartgattlib.Characteristic;
import le.colorpicker.ColorPicker;
import le.smartlamp.ColorPickerActivity;
import le.smartlamp.utils.MessageCodes;

public class BluetoothLeController {
    private final static String TAG = BluetoothLeController.class.getSimpleName();

    private boolean mConnected = false;

    //private String mDeviceName;
    private String mDeviceAddress;
    private BluetoothLeService mBluetoothLeService;

    private BluetoothGattCharacteristic characteristicTX = null;
    private BluetoothGattCharacteristic characteristicRX = null;
    
    private OnReceivedDataListener onReceivedListener;

    public interface OnReceivedDataListener{
        void onReceivedData(byte[] bytes);
    }

    public void setOnRecievedDataListener(OnReceivedDataListener listener){
        onReceivedListener = listener;
    }
    
    private final ServiceConnection mServiceConnection = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName componentName, IBinder service) {
            mBluetoothLeService = ((BluetoothLeService.LocalBinder) service).getService();
            if (!mBluetoothLeService.initialize()) {
                Log.e(TAG, "Unable to initialize Bluetooth");
            }
            // Automatically connects to the device upon successful start-up initialization.
            mBluetoothLeService.connect(mDeviceAddress);
        }

        @Override
        public void onServiceDisconnected(ComponentName componentName) {
            mBluetoothLeService = null;
        }
    };
    
    private final BroadcastReceiver mGattUpdateReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            final String action = intent.getAction();
            if (BluetoothLeService.ACTION_GATT_CONNECTED.equals(action)) {
                mConnected = true;
            } else if (BluetoothLeService.ACTION_GATT_DISCONNECTED.equals(action)) {
                mConnected = false;
            } else if (BluetoothLeService.ACTION_GATT_SERVICES_DISCOVERED.equals(action)) {
                List<BluetoothGattService> gattServices = mBluetoothLeService.getSupportedGattServices();

                for (BluetoothGattService gattService : gattServices) {
                    // get characteristic when UUID matches RX/TX UUID
                    characteristicTX = gattService.getCharacteristic(Characteristic.HM10_TRANSMISSION);
                    characteristicRX = gattService.getCharacteristic(Characteristic.HM10_TRANSMISSION);
                    //mBluetoothLeService.setCharacteristicNotification(characteristicRX, true);
                }

                if(characteristicTX != null && characteristicRX != null) {
                    if (ColorPickerActivity.isSentTimer == false) {
                        makeChange("*255|255|255|" + MessageCodes.TIMER_REQ.toString()
                                + "|" + ColorPickerActivity.pref.getString("timer", "0") + "#");
                        ColorPickerActivity.isSentTimer = true;
                    }

                    makeChange("*255|255|255|" + MessageCodes.VERS_REQ.toString() + "#");
                }

            } else if (BluetoothLeService.ACTION_DATA_NOTIFY.equals(action)) {
                byte[] bytes = intent.getByteArrayExtra(BluetoothLeService.EXTRA_DATA);
                if(onReceivedListener != null && bytes != null)
                    onReceivedListener.onReceivedData(bytes);
            } else if (BluetoothLeService.ACTION_DATA_READ.equals(action)) {
                byte[] bytes = intent.getByteArrayExtra(BluetoothLeService.EXTRA_DATA);
                if(onReceivedListener != null && bytes != null)
                    onReceivedListener.onReceivedData(bytes);
            }
        }
    };
    
    public BluetoothLeController(/*String deviceName,*/ String deviceAddress) {
        //mDeviceName = deviceName;
        mDeviceAddress = deviceAddress;
    }

    public void bindService(Context context) {
        Intent gattServiceIntent = new Intent(context, BluetoothLeService.class);
        context.bindService(gattServiceIntent, mServiceConnection, Context.BIND_AUTO_CREATE);
    }

    public void unbindService(Context context) {
        context.unbindService(mServiceConnection);
    }
    
    public boolean connect() {
        boolean result = false;
        if (mBluetoothLeService != null)
            result = mBluetoothLeService.connect(mDeviceAddress);
        return result;
    }

    public void disconnect() {
        if (mBluetoothLeService != null) {
            mBluetoothLeService.disconnect();
            mBluetoothLeService = null;
        }
    }

    public boolean getConnectingState() {
        return mConnected;
    }

    public void registerReceiver(Context context, boolean request) {
        if (request)
            context.registerReceiver(mGattUpdateReceiver, makeGattUpdateIntentFilter());
        else
            context.unregisterReceiver(mGattUpdateReceiver);
    }

    private IntentFilter makeGattUpdateIntentFilter() {
        final IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction(BluetoothLeService.ACTION_GATT_CONNECTED);
        intentFilter.addAction(BluetoothLeService.ACTION_GATT_DISCONNECTED);
        intentFilter.addAction(BluetoothLeService.ACTION_GATT_SERVICES_DISCOVERED);
        intentFilter.addAction(BluetoothLeService.ACTION_DATA_NOTIFY);
        intentFilter.addAction(BluetoothLeService.ACTION_DATA_WRITE);
        intentFilter.addAction(BluetoothLeService.ACTION_DATA_READ);
        return intentFilter;
    }
    
    public void makeChange(String str) {
        Log.d(TAG, "Sending result=" + str);
        final byte[] tx = str.getBytes();
        if(mConnected) {
            characteristicTX.setValue(tx);
            mBluetoothLeService.writeCharacteristic(characteristicTX);
            mBluetoothLeService.setCharacteristicNotification(characteristicRX, true);
        }
    }
}
