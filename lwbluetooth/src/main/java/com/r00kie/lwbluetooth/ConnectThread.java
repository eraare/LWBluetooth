package com.r00kie.lwbluetooth;

import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;
import android.os.Handler;
import android.os.Message;

import java.io.IOException;
import java.lang.reflect.Method;

/**
 * @author Leo
 * @time 2016-04-20
 * @describe 负责与蓝牙设备进行连接 然后抛出通信的线程
 */
public class ConnectThread extends Thread {
    private BluetoothSocket mBluetoothSocket;//通信的Socket
    private CommunicateThread mCommunicateThread;//通信数据交互线程
    private Handler mHandler;//向上传递数据
    private String deviceAddress;

    public ConnectThread(Handler mHandler, BluetoothDevice mBluetoothDevice) {
        //super();
        this.mHandler = mHandler;
        this.deviceAddress = mBluetoothDevice.getAddress();
        BluetoothSocket tmp = null;//临时变量
        /*try {
            //去连接通道
            tmp = mBluetoothDevice.createRfcommSocketToServiceRecord(BluetoothUUID.UUID_SPP);
        } catch (IOException e) {
            e.printStackTrace();
            System.out.println("Error-ConnectThread()");
        }*/
        /**
         * 这样建立连接可以同时和HID 和 SPP通信
         */
        try {
            Method m = mBluetoothDevice.getClass().getMethod("createRfcommSocket", new Class[]{int.class});
            tmp = (BluetoothSocket) m.invoke(mBluetoothDevice, 1);
        } catch (Exception e) {
            System.out.print(e.getMessage());
            System.out.println("Error-ConnectThread()");
        }

        mBluetoothSocket = tmp;//引用
    }

    @Override
    public void run() {
        //super.run();
        try {
            if (mBluetoothSocket != null) {
                //1、去连接
                mBluetoothSocket.connect();
                if (mBluetoothSocket.isConnected()) {
                    //2、去通信
                    mCommunicateThread = new CommunicateThread(mHandler, mBluetoothSocket, deviceAddress);
                    mCommunicateThread.start();

                    Message msg = mHandler.obtainMessage();
                    msg.what = BluetoothConstant.WHAT_CONNECT_STATE;
                    msg.obj = deviceAddress;
                    msg.arg1 = BluetoothConstant.STATE_CONNECT_SUCCESS;
                    mHandler.sendMessage(msg);
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
            Message msg = mHandler.obtainMessage();
            msg.what = BluetoothConstant.WHAT_CONNECT_STATE;
            msg.obj = deviceAddress;
            msg.arg1 = BluetoothConstant.STATE_CONNECT_FAIL;
            mHandler.sendMessage(msg);
            if (mBluetoothSocket != null) {
                try {
                    mBluetoothSocket.close();
                } catch (IOException e1) {
                    e1.printStackTrace();
                }
            }
            System.out.println("Error-ConnectThread:run()");
        }
    }

    /**
     * 向设备写数据 其实就是一个傀儡 要去调用CommunicateThread的write()方法
     *
     * @param message
     * @return
     */
    public boolean write(String message) {
        if (mCommunicateThread != null) {
            return mCommunicateThread.write(message);
        }
        return false;
    }

    /**
     * 关闭退出
     */
    public void close() {
        if (mCommunicateThread != null) {
            mCommunicateThread.close();
        }
        try {
            if (mBluetoothSocket != null) {
                mBluetoothSocket.close();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * 判断是否已连接
     *
     * @return
     */
    public boolean isConnected() {
        if (mBluetoothSocket != null) {
            if (mBluetoothSocket.isConnected()) {
                return true;
            }
        }
        return false;
    }
}
