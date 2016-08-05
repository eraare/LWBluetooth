package com.r00kie.lwbluetooth;

import android.app.Activity;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.Color;
import android.os.Bundle;
import android.provider.Settings;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.google.android.gms.common.api.GoogleApiClient;

import java.util.ArrayList;
import java.util.Set;

/**
 * @author Leo
 * @time 2016-08-05
 * @detail scan the bluetooth devices and return the clicked device
 */
public class ScanActivity extends Activity {
    private BluetoothAdapter mBtAdapter;//蓝牙适配器用于扫描
    private DeviceAdapter mDeviceAdapter;//蓝牙设备显示适配器
    private Button scanButton;//扫描按钮
    private RelativeLayout navigation;//导航
    private TextView title;//导航标题
    private String scanString;//还位开始扫描
    private String scanningString;//扫描中
    /**
     * 监听器
     */
    private final BroadcastReceiver mReceiver = new BroadcastReceiver() {
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();

            if (TextUtils.equals(BluetoothDevice.ACTION_FOUND, action)) {
                BluetoothDevice device = intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE);
                mDeviceAdapter.addDevice(device);
                mDeviceAdapter.notifyDataSetChanged();
            } else if (TextUtils.equals(BluetoothAdapter.ACTION_DISCOVERY_FINISHED, action)) {
                scanButton.setText(scanString);
            }
        }
    };
    private ListView deviceListView;//设备显示列表
    /**
     * 设备单机事件事件 点击连接设备
     */
    private OnItemClickListener mDeviceClickListener = new OnItemClickListener() {
        public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
            setResultToConnect(position);
        }
    };
    /**
     * ATTENTION: This was auto-generated to implement the App Indexing API.
     * See https://g.co/AppIndexing/AndroidStudio for more information.
     */
    private GoogleApiClient client;

    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_scan);
        init();
    }

    private void setTheme() {
        Intent intent = getIntent();
        String titleString = intent.getStringExtra(BluetoothConstant.EXTRA_THEME_TITLE);
        scanString = intent.getStringExtra(BluetoothConstant.EXTRA_THEME_SCAN);
        scanningString = intent.getStringExtra(BluetoothConstant.EXTRA_THEME_SCANNING);
        int colorInt = intent.getIntExtra(BluetoothConstant.EXTRA_THEME_COLOR, getResources().getColor(R.color.main));
        // 设置背景颜色
        navigation.setBackgroundColor(colorInt);
        // 设置标题
        if (titleString != null && !TextUtils.equals("", titleString)) {
            title.setText(titleString);
        }
        // 未扫描
        if (scanString == null || TextUtils.equals("", scanString)) {
            scanString = getString(R.string.scan_start);
        }
        // 开始扫描
        if (scanningString == null || TextUtils.equals("", scanningString)) {
            scanString = getString(R.string.scan_scanning);
        }
        scanButton.setText(scanString);
    }

    /**
     * 初始化数据
     */
    private void init() {
        findViewsByIds();
        // 设置主题
        setTheme();
        //新建设备列表适配器
        mDeviceAdapter = new DeviceAdapter(this);
        deviceListView.setAdapter(mDeviceAdapter);
        // 初始化蓝牙适配器
        mBtAdapter = BluetoothAdapter.getDefaultAdapter();
        // 初始化已配对蓝牙设备
        initBondedDevices();
    }

    /**
     * 初始化已绑定配对设备
     */
    private void initBondedDevices() {
        Set<BluetoothDevice> btDevices = mBtAdapter.getBondedDevices();
        if (btDevices == null) {
            return;
        }
        for (BluetoothDevice device : btDevices) {
            mDeviceAdapter.addDevice(device);
        }
        mDeviceAdapter.notifyDataSetChanged();
    }

    /**
     * 获取控件
     */
    private void findViewsByIds() {
        navigation = (RelativeLayout) findViewById(R.id.rl_navigation_scan);
        title = (TextView) findViewById(R.id.tv_title_scan);
        scanButton = (Button) findViewById(R.id.btn_scan_scan);
        scanButton.setOnClickListener(new OnClickListener() {
            public void onClick(View v) {
                doDiscovery();
            }
        });
        scanButton.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View v) {
                Intent intent = new Intent(Settings.ACTION_BLUETOOTH_SETTINGS);
                startActivityForResult(intent, 0);
                return true;
            }
        });
        deviceListView = (ListView) findViewById(R.id.lv_devices_scan);
        deviceListView.setOnItemClickListener(mDeviceClickListener);
    }

    private void setResultToConnect(int position) {
        if (mBtAdapter.isDiscovering())
            mBtAdapter.cancelDiscovery();

        BluetoothDevice device = mDeviceAdapter.getDevice(position);
        String name = device.getName().trim();
        String address = device.getAddress().trim();

        Intent intent = new Intent();
        intent.putExtra(BluetoothConstant.EXTRA_DEVICE_ADDRESS, address);
        intent.putExtra(BluetoothConstant.EXTRA_DEVICE_NAME, name);

        setResult(Activity.RESULT_OK, intent);
        finish();
    }

    /**
     * 查找设备
     */
    private void doDiscovery() {
        if (mBtAdapter.isDiscovering()) {
            mBtAdapter.cancelDiscovery();
            scanButton.setText(scanString);
        } else {
            mDeviceAdapter.clear();
            mDeviceAdapter.notifyDataSetChanged();
            mBtAdapter.startDiscovery();
            scanButton.setText(scanningString);
        }
    }

    @Override
    protected void onStart() {
        super.onStart();
        registerTheReceiver();
    }

    @Override
    protected void onStop() {
        super.onStop();
        if (mBtAdapter != null && mBtAdapter.isDiscovering()) {
            mBtAdapter.cancelDiscovery();
        }
        unregisterReceiver(mReceiver);
    }

    /**
     * 注册监听器
     */
    private void registerTheReceiver() {
        //注册监听器监听设备扫描以及扫描完成
        IntentFilter filter = new IntentFilter();
        filter.addAction(BluetoothDevice.ACTION_FOUND);
        filter.addAction(BluetoothAdapter.ACTION_DISCOVERY_FINISHED);
        this.registerReceiver(mReceiver, filter);
    }

    /**
     * 返回按钮返回
     *
     * @param view
     */
    public void back(View view) {
        this.finish();
    }

    /**
     * 蓝牙设备的展示列表适配器类
     */
    private class DeviceAdapter extends BaseAdapter {
        private LayoutInflater mInflater;//用于布局
        private ArrayList<BluetoothDevice> devices;//数据源
        private int BONDED_COLOR;

        public DeviceAdapter(Context context) {
            mInflater = LayoutInflater.from(context);//取得布局器
            devices = new ArrayList<>();//初始化
            BONDED_COLOR = Color.argb(255, 37, 155, 36);
        }

        @Override
        public int getCount() {
            return devices.size();
        }

        @Override
        public Object getItem(int position) {
            return devices.get(position);
        }

        @Override
        public long getItemId(int position) {
            return position;
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            ViewHolder viewHolder;
            if (convertView == null) {
                convertView = mInflater.inflate(R.layout.item_devices, null);

                viewHolder = new ViewHolder();
                viewHolder.deviceName = (TextView) convertView.findViewById(R.id.device_name);
                viewHolder.deviceAddress = (TextView) convertView.findViewById(R.id.device_address);

                convertView.setTag(viewHolder);
            } else {
                viewHolder = (ViewHolder) convertView.getTag();
            }

            BluetoothDevice device = devices.get(position);
            viewHolder.deviceName.setText(device.getName());
            viewHolder.deviceAddress.setText(device.getAddress());
            int state = device.getBondState();
            if (BluetoothDevice.BOND_BONDED == state) {
                viewHolder.deviceName.setTextColor(BONDED_COLOR);
                viewHolder.deviceAddress.setTextColor(BONDED_COLOR);
            }
            return convertView;
        }

        public void addDevice(BluetoothDevice device) {
            if (!devices.contains(device)) {
                this.devices.add(device);
            }
        }

        public void removeDevice(int position) {
            devices.remove(position);
        }

        public BluetoothDevice getDevice(int position) {
            return devices.get(position);
        }

        public void clear() {
            devices.clear();
        }

        /**
         * 控件类
         */
        private class ViewHolder {
            public TextView deviceName;
            public TextView deviceAddress;
        }
    }
}
