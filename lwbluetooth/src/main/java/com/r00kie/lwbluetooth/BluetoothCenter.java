package com.r00kie.lwbluetooth;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.text.TextUtils;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

/**
 * @author Leo
 * @time 2016-08-05
 * @detail 蓝牙通信控制中心
 */
public class BluetoothCenter {
    // 单例模式
    private static BluetoothCenter mCenter = null;

    public static BluetoothCenter createCenter() {
        if (mCenter == null) {
            synchronized (BluetoothCenter.class) {
                if (mCenter == null) {
                    mCenter = new BluetoothCenter();
                }
            }
        }
        return mCenter;
    }

    private MHandler mHandler;//用于与线程进行数据通信
    private BluetoothAdapter mAdapter;//蓝牙设置的管理
    private Map<String, ConnectThread> mConnectThreadMap;//所有的连接信息，以device address为key

    private BluetoothDevice mBluetoothDevice;//要连接的设备
    private ConnectThread mConnectThread;//连接线程临时变量

    private BluetoothCenter() {
        init();//初始化
    }

    /**
     * 初始化数据
     */
    private void init() {
        mAdapter = BluetoothAdapter.getDefaultAdapter();
        mHandler = new MHandler();
        mConnectThreadMap = new HashMap<>();
    }

    private class MHandler extends Handler {
        public MHandler() {
            //super();
        }

        @Override
        public void handleMessage(Message msg) {
            //super.handleMessage(msg);
            int what = msg.what;
            switch (what) {
                case BluetoothConstant.WHAT_RECEIVED_DATA: {
                    Bundle data = msg.getData();
                    String dAddress = data.getString(BluetoothConstant.KEY_DEVICE_ADDRESS, "unknown");
                    String mMessage = data.getString(BluetoothConstant.KEY_RECEIVED_MESSAGE, "unknown");
                    System.out.println("received:" + mMessage + "-" + dAddress);
                    if (mOnDataReceivedListener != null) {
                        mOnDataReceivedListener.onDataReceived(dAddress, mMessage);
                    }
                }
                break;
                case BluetoothConstant.WHAT_CONNECT_STATE: {
                    int state = msg.arg1;
                    System.out.println("connect state changed:" + state);
                    String dAddress = msg.obj.toString();
                    if (mOnStateChangedListener != null) {
                        mOnStateChangedListener.onStateChanged(dAddress, state);
                    }
                }
                break;
                default:
                    break;
            }
        }
    }

    /**
     * 获取蓝牙适配器
     *
     * @return 蓝牙适配器
     */
    public BluetoothAdapter getBluetoothAdapter() {
        return this.mAdapter;
    }

    /**
     * 打开蓝牙
     */
    public boolean openBluetooth() {
        if (mAdapter != null) {
            if (!mAdapter.isEnabled()) {
                return mAdapter.enable();
            }
        }
        return false;
    }

    /**
     * 关闭蓝牙
     */
    public boolean closeBluetooth() {
        if (mAdapter != null) {
            if (mAdapter.isEnabled()) {
                return mAdapter.disable();
            }
        }
        return false;
    }

    /**
     * 判断是否支持蓝牙
     *
     * @return boolean
     */
    public boolean isSupportBluetooth() {
        if (mAdapter == null) {
            mAdapter = BluetoothAdapter.getDefaultAdapter();
            if (mAdapter == null) {
                return false;
            }
        }
        return true;
    }

    /**
     * 根据地址连接设备
     *
     * @param deviceAddress addres
     */
    public boolean connect(String deviceAddress) {
        //地址为空就退出 别烦我
        if (deviceAddress == null || TextUtils.equals("", deviceAddress)) {
            return false;
        }
        //不支持蓝牙或者蓝牙关闭就退出 别烦我
        if (mAdapter == null || !mAdapter.isEnabled()) {
            return false;
        }

        if (isConnected(deviceAddress)) {
            return true;//如果连接就不再连接
        }

        if (mAdapter.isDiscovering())
            mAdapter.cancelDiscovery();//连接前一家要取消扫描

        mConnectThread = mConnectThreadMap.get(deviceAddress);//查看设备是否存在
        //存在但是没有连接我就关了你
        if (mConnectThread != null) {
            mConnectThread.close();
            mConnectThread = null;
            mConnectThreadMap.remove(deviceAddress);
        }
        mBluetoothDevice = mAdapter.getRemoteDevice(deviceAddress);
        mConnectThread = new ConnectThread(mHandler, mBluetoothDevice);
        mConnectThreadMap.put(deviceAddress, mConnectThread);
        mConnectThread.start();
        return true;
    }

    /**
     * 向蓝牙设备写数据
     *
     * @param deviceAddress address
     * @param message       message
     */
    public boolean send(String deviceAddress, String message) {
        //如果你没给我消息 就别来烦我
        if (message == null || TextUtils.equals("", message)) {
            return false;
        }
        if (deviceAddress == null || TextUtils.equals(deviceAddress, "")) {
            return false;
        }

        //哈哈 你给了我地址 最喜欢这样的了 我给查一下有没有 有的话就帮你捎个信 没有别怪我
        mConnectThread = mConnectThreadMap.get(deviceAddress);
        if (mConnectThread == null || !isConnected(deviceAddress)) {
            return false;
        }
        return mConnectThread.write(message);
    }

    public boolean sendAll(String message) {
        //遍历所有的连接线程去写数据
        boolean flag = true;//标志位
        Set<Map.Entry<String, ConnectThread>> mEnterySet = mConnectThreadMap.entrySet();
        for (Map.Entry<String, ConnectThread> entry : mEnterySet) {
            mConnectThread = entry.getValue();
            if (mConnectThread != null) {
                if (!mConnectThread.write(message)) {
                    flag = false;
                }
            }
        }
        //写完就走 别再烦我
        return flag;
    }

    /**
     * 根据设备地址进行断开连接
     *
     * @param deviceAddress
     */
    public void disconnect(String deviceAddress) {
        //你没给我地址 这是要作哪样 吼吼吼吼
        if (deviceAddress == null && TextUtils.equals("", deviceAddress)) {
            return;
        }
        mConnectThread = mConnectThreadMap.get(deviceAddress);
        if (mConnectThread != null) {
            mConnectThread.close();
            mConnectThreadMap.remove(deviceAddress);
        }
    }

    public void disconnectAll() {
        Set<Map.Entry<String, ConnectThread>> mEnterySet = mConnectThreadMap.entrySet();
        for (Map.Entry<String, ConnectThread> entry : mEnterySet) {
            mConnectThread = entry.getValue();
            if (mConnectThread != null) {
                mConnectThread.close();
            }
        }
        //断开完我再清理
        mConnectThreadMap.clear();
    }

    /**
     * 判断设备是否连接
     *
     * @param deviceAddress
     * @return
     */
    public boolean isConnected(String deviceAddress) {
        //你是敢给我空的地址我就敢给你false
        if (deviceAddress == null || TextUtils.equals("", deviceAddress)) {
            return false;
        }
        mConnectThread = mConnectThreadMap.get(deviceAddress);
        if (mConnectThread != null) {
            return mConnectThread.isConnected();
        }
        return false;
    }

    // 数据接收监听器
    private OnDataReceivedListener mOnDataReceivedListener;
    // 连接状态监听器
    private OnStateChangedListener mOnStateChangedListener;

    public interface OnDataReceivedListener {
        void onDataReceived(String deviceAddress, String message);
    }

    public interface OnStateChangedListener {
        void onStateChanged(String deviceAddress, int state);
    }

    /**
     * 数据监听器
     *
     * @param mOnDataReceivedListener
     */
    public void setOnDataReceivedListener(OnDataReceivedListener mOnDataReceivedListener) {
        this.mOnDataReceivedListener = mOnDataReceivedListener;
    }

    /**
     * 状态监听器
     *
     * @param mOnStateChangedListener
     */
    public void setmOnStateChangedListener(OnStateChangedListener mOnStateChangedListener) {
        this.mOnStateChangedListener = mOnStateChangedListener;
    }

    /**
     * 自杀函数
     * 我是隐藏的你敢要我死我就死给你看
     */
    public void sucide() {
        disconnectAll();
        mConnectThreadMap = null;
        mConnectThread = null;
        mBluetoothDevice = null;
        if (mAdapter != null) {
            mAdapter = null;
        }
        mHandler = null;
    }

    public void unpair(String deviceAddress) {
        BluetoothUtil.unpairDevice(deviceAddress);
    }

    public void unpair(BluetoothDevice device) {
        BluetoothUtil.unpairDevice(device);
    }
}