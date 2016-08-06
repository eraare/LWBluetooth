package com.r00kie.lwbluetoothdemo;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import com.r00kie.lwbluetooth.BluetoothCenter;
import com.r00kie.lwbluetooth.BluetoothConstant;
import com.r00kie.lwbluetooth.ScanActivity;

public class OtherActivity extends AppCompatActivity {
    private TextView info;
    private Button scan;
    private Button connect;
    private Button disconnect;

    // 1 声明一个BluetoothCenter
    private BluetoothCenter mCenter = BluetoothCenter.createCenter();
    private String deviceName;
    private String deviceAddress;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_other);
        init();
    }

    private void init() {
        findViewsByIds();
        // 2 绑定数据接收监听器 和 状态切换监听器
        mCenter.setOnDataReceivedListener(new BluetoothCenter.OnDataReceivedListener() {
            @Override
            public void onDataReceived(String deviceAddress, String message) {
                info.setText(info.getText().toString() + "\n" + "received:" + message + "-" + deviceAddress);
            }
        });
        mCenter.setmOnStateChangedListener(new BluetoothCenter.OnStateChangedListener() {
            @Override
            public void onStateChanged(String deviceAddress, int state) {
                if (state == BluetoothConstant.STATE_CONNECT_FAIL) {
                    info.setText(info.getText().toString() + "\n" + "connect failed:" + deviceAddress);
                } else if (state == BluetoothConstant.STATE_CONNECT_SUCCESS) {
                    info.setText(info.getText().toString() + "\n" + "received succeed:" + deviceAddress);
                }
            }
        });
    }

    private void findViewsByIds() {
        info = (TextView) findViewById(R.id.info_other);
        scan = (Button) findViewById(R.id.scan_other);
        connect = (Button) findViewById(R.id.connect_other);
        disconnect = (Button) findViewById(R.id.disconnect_other);
        scan.setOnClickListener(mOnClickListener);
        connect.setOnClickListener(mOnClickListener);
        disconnect.setOnClickListener(mOnClickListener);
    }

    private View.OnClickListener mOnClickListener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            int id = v.getId();
            switch (id) {
                case R.id.scan_other: {
                    info.setText(info.getText().toString() + "\n" + "scanning");
                    // 3 扫描设备
                    Intent intent = new Intent(OtherActivity.this, ScanActivity.class);
                    /*可以通过以下方式设置主题，包括头，背景颜色，扫描button的文字*/
                    intent.putExtra(BluetoothConstant.EXTRA_THEME_COLOR, Color.BLUE);
                    intent.putExtra(BluetoothConstant.EXTRA_THEME_TITLE, "蓝牙设备");
                    intent.putExtra(BluetoothConstant.EXTRA_THEME_SCAN, "开始扫描");
                    intent.putExtra(BluetoothConstant.EXTRA_THEME_SCANNING, "扫描中...");
                    startActivityForResult(intent, 1);
                }
                break;
                case R.id.connect_other: {
                    // 5 连接设备
                    if (mCenter != null && !TextUtils.equals(deviceAddress, "")) {
                        info.setText(info.getText().toString() + "\n" + "connecting:" + deviceName);
                        mCenter.connect(deviceAddress);
                    }
                }
                break;
                case R.id.disconnect_other: {
                    // 6 断开连接
                    if (mCenter != null && !TextUtils.equals(deviceAddress, "")) {
                        info.setText(info.getText().toString() + "\n" + "disconnect:" + deviceName);
                        mCenter.disconnect(deviceAddress);
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
            if (requestCode == 1) {
                // 4 接收选择的设备的名字和地址
                deviceName = data.getStringExtra(BluetoothConstant.EXTRA_DEVICE_NAME);
                deviceAddress = data.getStringExtra(BluetoothConstant.EXTRA_DEVICE_ADDRESS);
                info.setText(info.getText().toString() + "\n" + deviceName + ":" + deviceAddress);
            }
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        // 7 最后结束时
        // #warnning 这个只在程序结束时需要 如果其他地方还需要使用蓝牙【请不要】销毁
        if (mCenter != null) {
            mCenter.sucide();
        }
    }
}
