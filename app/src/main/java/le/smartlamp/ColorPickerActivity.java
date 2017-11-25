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

package le.smartlamp;

import le.bluetooth.BluetoothLeController;
import le.bluetooth.BluetoothLeController.OnReceivedDataListener;
import le.bluetooth.DeviceScanActivity;
import le.colorpicker.ColorPicker;
import le.colorpicker.SaturationBar;
import le.colorpicker.ValueBar;
import le.colorpicker.ColorPicker.OnColorChangedListener;
import le.smartlamp.utils.MessageCodes;

import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.Configuration;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v4.app.ActionBarDrawerToggle;
import android.support.v4.widget.DrawerLayout;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ListView;
import android.widget.Toast;

@SuppressWarnings("deprecation")
public class ColorPickerActivity extends Activity
        implements OnColorChangedListener, OnClickListener{

    private ColorPicker picker;
    private ValueBar valueBar;
    private SaturationBar saturationBar;

    private DrawerLayout mDrawerLayout;
    private ListView mDrawerList;
    private ActionBarDrawerToggle mDrawerToggle;
    
    private CharSequence mDrawerTitle;
    private CharSequence mTitle;
    
    public static SharedPreferences pref;
    
    public static boolean isSentTimer = false;
    public static String firmwareVersion = "unknown";

    private static final int BLE_CONNECT_REQUEST = 1;
    public static BluetoothLeController bleController = null;
    
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_color_picker);

        Button rebButton = findViewById(R.id.reb_btn);
        Button yellowButton = findViewById(R.id.yellow_btn);
        Button greenButton = findViewById(R.id.green_btn);
        Button blueButton = findViewById(R.id.blue_btn);
        ImageButton colorButton = findViewById(R.id.color_btn);

        rebButton.setOnClickListener(this);
        yellowButton.setOnClickListener(this);
        greenButton.setOnClickListener(this);
        blueButton.setOnClickListener(this);
        colorButton.setOnClickListener(this);

        initActivity(savedInstanceState);
    }

    private void initActivity(Bundle savedInstanceState) {
        pref = PreferenceManager.getDefaultSharedPreferences(this);

        picker = findViewById(R.id.colorpicker);
        valueBar = findViewById(R.id.valuebar);
        saturationBar = findViewById(R.id.saturationbar);

        picker.addValueBar(valueBar);
        picker.addSaturationBar(saturationBar);

        //To get the color
        picker.getColor();

        //To set the old selected color u can do it like this
        picker.setOldCenterColor(picker.getColor());
        // adds listener to the colorpicker which is implemented
        //in the activity
        picker.setOnColorChangedListener(this);

        //to turn of showing the old color
        picker.setShowOldCenterColor(false);

        //to create navigation menu
        mTitle = mDrawerTitle = getTitle();
        mDrawerLayout = findViewById(R.id.drawer_layout);
        mDrawerList = findViewById(R.id.left_drawer);
        
        // set up the drawer's list view with items and click listener
        mDrawerList.setAdapter(new MenuListAdapter(getBaseContext(),
                getResources().getStringArray(R.array.menu_item_list)));
        mDrawerList.setOnItemClickListener(new DrawerItemClickListener());
        
        // enable ActionBar app icon to behave as action to toggle nav drawer
        getActionBar().setDisplayHomeAsUpEnabled(true);
        getActionBar().setHomeButtonEnabled(true);
        
        // ActionBarDrawerToggle ties together the the proper interactions
        // between the sliding drawer and the action bar app icon
        mDrawerToggle = new ActionBarDrawerToggle(this,mDrawerLayout,
                R.drawable.ic_drawer, R.string.application_name, R.string.application_name)
        {
            public void onDrawerClosed(View view) {
                getActionBar().setTitle(mTitle);
                invalidateOptionsMenu();
            }

            public void onDrawerOpened(View drawerView) {
                getActionBar().setTitle(mDrawerTitle);
                invalidateOptionsMenu();
            }
        };
        mDrawerLayout.setDrawerListener(mDrawerToggle);
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (bleController != null) {
            bleController.registerReceiver(this, true);
            if (bleController.connect() && isSentTimer == false) {
                bleController.makeChange("*255|255|255|"+ MessageCodes.TIMER_REQ.toString()
                        + "|" + pref.getString("timer", "0") + "#");
                isSentTimer = true;
            }
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        if (bleController != null) {
            bleController.registerReceiver(this, false);
        }
    }

    @Override
    protected void onStart() {
        super.onStart();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (bleController != null) {
            bleController.disconnect();
            bleController.unbindService(this);
        }
    }
    
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == BLE_CONNECT_REQUEST && resultCode == Activity.RESULT_OK) {
            bleController = new BluetoothLeController(
                    //data.getStringExtra("DEVICE_NAME"),
                    data.getStringExtra("DEVICE_ADDRESS"));
            bleController.bindService(this);
            bleController.registerReceiver(this, true);
            bleController.setOnRecievedDataListener(receivedListener);
        }
        super.onActivityResult(requestCode, resultCode, data);
    }
    
    @Override
    public void onClick(View v) {

        switch(v.getId()) {
            case R.id.reb_btn:
                if (bleController != null && bleController.getConnectingState())
                {
                    picker.setColor(0xFFFF0000);
                    Toast.makeText(getApplicationContext(), "Red selected", Toast.LENGTH_SHORT).show();
                }
                else
                    Toast.makeText(getApplicationContext(), "Not connected", Toast.LENGTH_SHORT).show();
                break;
            case R.id.yellow_btn:
                if (bleController != null && bleController.getConnectingState())
                {
                    picker.setColor(0xFFFFFF00);
                    Toast.makeText(getApplicationContext(), "Yellow selected", Toast.LENGTH_SHORT).show();
                }
                else
                    Toast.makeText(getApplicationContext(), "Not connected", Toast.LENGTH_SHORT).show();
                break;
            case R.id.green_btn:
                if (bleController != null && bleController.getConnectingState())
                {
                    picker.setColor(0xFF00FF00);
                    Toast.makeText(getApplicationContext(), "Green selected", Toast.LENGTH_SHORT).show();
                }
                else
                    Toast.makeText(getApplicationContext(), "Not connected", Toast.LENGTH_SHORT).show();
                break;
            case R.id.blue_btn:
                if (bleController != null && bleController.getConnectingState())
                {
                    picker.setColor(0xFF0000FF);
                    Toast.makeText(getApplicationContext(), "Blue selected", Toast.LENGTH_SHORT).show();
                }
                else
                    Toast.makeText(getApplicationContext(), "Not connected", Toast.LENGTH_SHORT).show();
                break;
            case R.id.color_btn:
                if (bleController != null && bleController.getConnectingState())
                {
                    if (pref.getBoolean("color_fade_checkbox", false))
                        bleController.makeChange("*255|255|255|" + MessageCodes.FADE_REQ.toString() + "#");
                    else
                        bleController.makeChange("*255|255|255|" + MessageCodes.VARY_REQ.toString() + "#");

                    Toast.makeText(getApplicationContext(), "Color auto changed", Toast.LENGTH_SHORT).show();
                }
                else
                    Toast.makeText(getApplicationContext(), "Not connected", Toast.LENGTH_SHORT).show();
                break;
        }
    }

    @Override
    public void onColorChanged(int newColor) {
        if (bleController != null && bleController.getConnectingState()) {
            bleController.makeChange("*" +
                    Integer.toString((newColor >> 16) & 0xFF) + "|" +
                    Integer.toString((newColor >> 8) & 0xFF) + "|" +
                    Integer.toString(newColor & 0xFF) + "#");
        }
    }

    /*@Override
    public boolean onCreateOptionsMenu(Menu menu) {
        return super.onCreateOptionsMenu(menu);
    }*/
    
    @Override
    public boolean onPrepareOptionsMenu(Menu menu) {
        return super.onPrepareOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
         if (mDrawerToggle.onOptionsItemSelected(item)) {
             return true;
         }
         return super.onOptionsItemSelected(item);
    }

    private class DrawerItemClickListener implements ListView.OnItemClickListener {
        @Override
        public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
            switch(position) {
            case 0:
                if (bleController == null)
                    startActivityForResult(new Intent(ColorPickerActivity.this, DeviceScanActivity.class), BLE_CONNECT_REQUEST);
                else
                    Toast.makeText(getApplicationContext(), "Disconnect before reconnect", Toast.LENGTH_SHORT).show();
                break;
            case 1:
                if (bleController != null)
                {
                    bleController.disconnect();
                    bleController.registerReceiver(ColorPickerActivity.this, false);
                    bleController.unbindService(ColorPickerActivity.this);
                    bleController = null;
                    firmwareVersion = "unknown";
                }
                else
                    Toast.makeText(getApplicationContext(), "Already disconnected", Toast.LENGTH_SHORT).show();
                break;
            case 2:
                /*if (bleController != null && bleController.getConnectingState())
                    bleController.makeChange("*255|255|255|" + MessageCodes.VERS_REQ.toString() + "#");
                else
                    firmwareVersion = "unknown";
                */
                startActivity(new Intent(ColorPickerActivity.this, SettingsActivity.class));
                break;
            }
            mDrawerList.setItemChecked(position, false);
            mDrawerLayout.closeDrawer(mDrawerList);
        }
    }
    
    private OnReceivedDataListener receivedListener = new OnReceivedDataListener() {
        @Override
        public void onReceivedData(byte[] bytes) {
            String data = new String(bytes);
            if (data.indexOf("*") >= 0 && data.indexOf("#") >= 0)
            {
                try {
                    String[] parts = data.split("\\D");
                    char index;
                    for (index = 0; index < parts.length; index++)
                        if(!parts[index].isEmpty())
                            break;

                    Integer msgCode = Integer.parseInt(parts[index]);
                    if (msgCode == MessageCodes.VERS_RES) {
                        firmwareVersion = parts[index + 1] + "." + parts[index + 2];
                    }
                }
                catch (Exception ex) {
                    firmwareVersion = "unknown";
                }
            }
            else
                Toast.makeText(getApplicationContext(), data, Toast.LENGTH_SHORT).show();
        }
    };

    @Override
    protected void onPostCreate(Bundle savedInstanceState) {
        super.onPostCreate(savedInstanceState);
        mDrawerToggle.syncState();
    }

    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
        mDrawerToggle.onConfigurationChanged(newConfig);
    }

    @Override
    public void onBackPressed() {
        if (mDrawerLayout.isDrawerOpen(mDrawerList))
            mDrawerLayout.closeDrawer(mDrawerList);
        else
            super.onBackPressed();
    }
}
