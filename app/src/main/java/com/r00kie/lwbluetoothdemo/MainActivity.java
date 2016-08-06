package com.r00kie.lwbluetoothdemo;

import android.app.Activity;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.graphics.Color;
import android.os.Bundle;
import android.os.IBinder;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.text.TextUtils;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import com.r00kie.lwbluetooth.BluetoothConstant;
import com.r00kie.lwbluetooth.BluetoothService;
import com.r00kie.lwbluetooth.IBluetoothService;
import com.r00kie.lwbluetooth.ScanActivity;

public class MainActivity extends AppCompatActivity {
    // 1 定义蓝牙服务变量
    private IBluetoothService mBluetoothService;

    private TextView info;
    private Button scan;
    private Button connect;
    private Button disconnect;

    private String deviceName;
    private String deviceAddress;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Snackbar.make(view, "Replace with your own action", Snackbar.LENGTH_LONG)
                        .setAction("Action", null).show();
            }
        });
        init();
    }

    private void findViewsByIds() {
        info = (TextView) findViewById(R.id.info);
        scan = (Button) findViewById(R.id.scan);
        connect = (Button) findViewById(R.id.connect);
        disconnect = (Button) findViewById(R.id.disconnect);
        scan.setOnClickListener(mOnClickListener);
        connect.setOnClickListener(mOnClickListener);
        disconnect.setOnClickListener(mOnClickListener);
    }

    private View.OnClickListener mOnClickListener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            int id = v.getId();
            switch (id) {
                case R.id.scan: {
                    info.setText(info.getText().toString() + "\n" + "scanning");
                    // 4 调用扫描Activity进行扫描并通过onActivityResult进行数据的接收
                    Intent intent = new Intent(MainActivity.this, ScanActivity.class);
                    /*可以通过以下方式设置主题，包括头，背景颜色，扫描button的文字*/
                    intent.putExtra(BluetoothConstant.EXTRA_THEME_COLOR, Color.RED);
                    intent.putExtra(BluetoothConstant.EXTRA_THEME_TITLE, "Bluetooth");
                    intent.putExtra(BluetoothConstant.EXTRA_THEME_SCAN, "Start Scan");
                    intent.putExtra(BluetoothConstant.EXTRA_THEME_SCANNING, "Scanning...");
                    startActivityForResult(intent, 0);
                }
                break;
                case R.id.connect: {
                    if (mBluetoothService != null && !TextUtils.equals(deviceAddress, "")) {
                        info.setText(info.getText().toString() + "\n" + "connecting:" + deviceName);
                        mBluetoothService.connect(deviceAddress);
                    }
                }
                break;
                case R.id.disconnect: {
                    if (mBluetoothService != null && !TextUtils.equals(deviceAddress, "")) {
                        info.setText(info.getText().toString() + "\n" + "disconnect:" + deviceName);
                        mBluetoothService.disconnect(deviceAddress);
                    }
                }
                break;
                default:
                    break;
            }
        }
    };

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
//        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode == Activity.RESULT_OK) {
            if (requestCode == 0) {
                // 5 接收选择的设备的名字和地址
                deviceName = data.getStringExtra(BluetoothConstant.EXTRA_DEVICE_NAME);
                deviceAddress = data.getStringExtra(BluetoothConstant.EXTRA_DEVICE_ADDRESS);
                info.setText(info.getText().toString() + "\n" + deviceName + ":" + deviceAddress);
            }
        }
    }

    private void init() {
        findViewsByIds();
        // 2 绑定蓝牙操作服务 用于连接断开发送数据等操作
        bindBluetoothService();
        //registerBluetoothReceiver();
    }

    /*private void registerBluetoothReceiver() {
        IntentFilter mFilter = new IntentFilter();
        mFilter.addAction(BluetoothConstant.ACTION_RECEIVED_DATA);
        mFilter.addAction(BluetoothConstant.ACTION_CONNECT_FAIL);
        mFilter.addAction(BluetoothConstant.ACTION_CONNECT_SUCCESS);
        mFilter.setPriority(Integer.MAX_VALUE);

    }

    private BroadcastReceiver mBluetoothReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            if (TextUtils.equals(action, BluetoothConstant.ACTION_RECEIVED_DATA)) {
                info.setText(info.getText().toString() + "\n" + "received:" + intent.getStringExtra(BluetoothConstant.EXTRA_RECEIVED_MESSAGE));
            } else if (TextUtils.equals(action, BluetoothConstant.ACTION_CONNECT_FAIL)) {
                info.setText(info.getText().toString() + "\n" + "connect failed:" + intent.getStringExtra(BluetoothConstant.EXTRA_DEVICE_ADDRESS));
            } else if (TextUtils.equals(action, BluetoothConstant.ACTION_CONNECT_SUCCESS)) {
                info.setText(info.getText().toString() + "\n" + "connect success:" + intent.getStringExtra(BluetoothConstant.EXTRA_DEVICE_ADDRESS));
            }
        }
    };*/

    private void bindBluetoothService() {
        Intent service = new Intent(this, BluetoothService.class);
        bindService(service, mServiceConnection, Context.BIND_AUTO_CREATE);
    }


    private ServiceConnection mServiceConnection = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName name, IBinder service) {
            mBluetoothService = (IBluetoothService) service;
            // 3 绑定数据接收和转台接收监听器
            if (mBluetoothService != null) {
                mBluetoothService.setOnDataReceivedListener(mOnDataReceivedListener);
                mBluetoothService.setOnStateChangedListener(mOnStateChangedListener);
            }
        }

        /**
         * 用于接收数据
         */
        private BluetoothService.OnDataReceivedListener mOnDataReceivedListener = new BluetoothService.OnDataReceivedListener() {
            @Override
            public void onDataReceived(String deviceAddress, String message) {
                info.setText(info.getText().toString() + "\n" + "received:" + message + "-" + deviceAddress);
            }
        };

        /**
         * 用于接收连接状态
         */
        private BluetoothService.OnStateChangedListener mOnStateChangedListener = new BluetoothService.OnStateChangedListener() {
            @Override
            public void onStateChanged(String deviceAddress, int state) {
                if (state == BluetoothConstant.STATE_CONNECT_FAIL) {
                    info.setText(info.getText().toString() + "\n" + "connect failed:" + deviceAddress);
                } else if (state == BluetoothConstant.STATE_CONNECT_SUCCESS) {
                    info.setText(info.getText().toString() + "\n" + "received succeed:" + deviceAddress);
                }
            }
        };

        @Override
        public void onServiceDisconnected(ComponentName name) {
            mBluetoothService = null;
        }
    };


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_other) {
            Intent intent = new Intent(MainActivity.this, OtherActivity.class);
            startActivity(intent);
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
//        unregisterReceiver(mBluetoothReceiver);
        unbindService(mServiceConnection);
    }
}
