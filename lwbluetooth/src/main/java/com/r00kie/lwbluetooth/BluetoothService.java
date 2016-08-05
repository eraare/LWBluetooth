package com.r00kie.lwbluetooth;

import android.app.Service;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.Intent;
import android.os.Binder;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.text.TextUtils;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

/**
 * @author Leo
 * @time 2016-02-02
 * @detail 蓝牙通信服务
 */
public class BluetoothService extends Service {
    private MHandler mHandler;//用于与线程进行数据通信
    private BluetoothAdapter mAdapter;//蓝牙设置的管理
    private Map<String, ConnectThread> mConnectThreadMap;//所有的连接信息，以device address为key

    private BluetoothDevice mBluetoothDevice;//要连接的设备
    private ConnectThread mConnectThread;//连接线程临时变量

    private BluetoothService mContext;

    public BluetoothService() {
    }

    @Override
    public void onCreate() {
        super.onCreate();
        init();//初始化
    }

    /**
     * 初始化数据
     */
    private void init() {
        mContext = this;
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
     * 发送接收到的数据的广播
     *
     * @param //rcvMsg
     */
   /* private void sendDataBroadcast(String rcvMsg) {
        Intent intent = new Intent();
        intent.putExtra(BluetoothConstant.EXTRA_RECEIVED_MESSAGE, rcvMsg);
        intent.setAction(BluetoothConstant.ACTION_RECEIVED_DATA);
        sendBroadcast(intent);
    }*/

    /*private void sendConnectInfoBroadcast(int what, String message) {
        Intent intent = new Intent();
        if (message != null && !TextUtils.equals("", message)) {
            intent.putExtra(BluetoothConstant.EXTRA_DEVICE_ADDRESS, message);
        }
        switch (what) {
            case BluetoothConstant.WHAT_CONNECT_ERROR:
                intent.setAction(BluetoothConstant.ACTION_CONNECT_FAIL);
                break;
            case BluetoothConstant.WHAT_CONNECT_SUCCESS:
                intent.setAction(BluetoothConstant.ACTION_CONNECT_SUCCESS);
                break;
        }
        sendBroadcast(intent);
    }*/
    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        //return super.onStartCommand(intent, flags, startId);
        return START_STICKY;//粘性打开死而复生
    }

    @Override
    public IBinder onBind(Intent intent) {
        // TODO: Return the communication channel to the service.
        //throw new UnsupportedOperationException("Not yet implemented");
        return new ServiceBinder();
    }

    /**
     * 根据地址连接设备
     *
     * @param deviceAddress
     */
    private boolean connect(String deviceAddress) {
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
     * @param deviceAddress
     * @param message
     */
    private boolean send(String deviceAddress, String message) {
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

    private boolean sendAll(String message) {
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
    private void disconnect(String deviceAddress) {
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

    private void disconnectAll() {
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
    private boolean isConnected(String deviceAddress) {
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

    @Override
    public void onDestroy() {
        super.onDestroy();
        sucide();
    }

    /**
     * 自杀函数
     * 我是隐藏的你敢要我死我就死给你看
     */
    private void sucide() {
        disconnectAll();
        mConnectThreadMap = null;
        mConnectThread = null;
        mBluetoothDevice = null;
        if (mAdapter != null) {
            mAdapter = null;
        }
        mHandler = null;
    }

    private class ServiceBinder extends Binder implements IBluetoothService {
        @Override
        public boolean connect(String deviceAddress) {
            return mContext.connect(deviceAddress);
        }

        @Override
        public boolean send(String deviceAddress, String message) {
            return mContext.send(deviceAddress, message);
        }

        @Override
        public boolean sendAll(String message) {
            return sendAll(message);
        }

        @Override
        public void disconnect(String deviceAddress) {
            mContext.disconnect(deviceAddress);
        }

        @Override
        public void disconnectAll() {
            mContext.disconnectAll();
        }

        @Override
        public boolean isConnected(String deviceAddress) {
            return mContext.isConnected(deviceAddress);
        }

        @Override
        public void setOnDataReceivedListener(OnDataReceivedListener onDataReceivedListener) {
            mContext.mOnDataReceivedListener = onDataReceivedListener;
        }

        @Override
        public void setOnStateChangedListener(OnStateChangedListener onStateChangedListener) {
            mContext.mOnStateChangedListener = onStateChangedListener;
        }
    }

    private OnDataReceivedListener mOnDataReceivedListener;
    private OnStateChangedListener mOnStateChangedListener;

    public interface OnDataReceivedListener {
        void onDataReceived(String deviceAddress, String message);
    }

    public interface OnStateChangedListener {
        void onStateChanged(String deviceAddress, int state);
    }
}