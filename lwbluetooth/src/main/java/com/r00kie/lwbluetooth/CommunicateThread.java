package com.r00kie.lwbluetooth;

import android.bluetooth.BluetoothSocket;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

/**
 * @author Leo
 * @time 2016-04-20
 * @describe 与蓝牙进行数据交互的线程 处理 发送数据 和 接收数据
 */
public class CommunicateThread extends Thread {
    private Handler mHandler;//用于向上传递收到的信息
    private boolean isRunning;//循环标志位
    private InputStream in;//读
    private OutputStream out;//写
    private String deviceAddress;

    public CommunicateThread(Handler mHandler, BluetoothSocket socket, String deviceAddress) {
        //super();
        this.deviceAddress = deviceAddress;
        this.mHandler = mHandler;
        isRunning = false;
        try {
            if (socket != null) {
                in = socket.getInputStream();
                out = socket.getOutputStream();
                isRunning = true;//如果正确建立通道就是赋为true
            }
        } catch (IOException e) {
            //e.printStackTrace();
            System.out.println("Error-CommunicateThread()");
        }
    }

    public void run() {
        byte[] buffer;//用于读数据的缓存区
        int size;//长度
        String data;//数据
        while (isRunning) {
            try {
                if (in != null) {
                    size = in.available();
                    //有数据就去拿出来
                    if (size > 0) {
                        buffer = new byte[size];
                        in.read(buffer);
                        data = new String(buffer);
                        //能过Handler把数据传出去
                        Message msg = mHandler.obtainMessage();
                        msg.what = BluetoothConstant.WHAT_RECEIVED_DATA;
                        Bundle bundle = new Bundle();
                        bundle.putString(BluetoothConstant.KEY_DEVICE_ADDRESS, deviceAddress);
                        bundle.putString(BluetoothConstant.KEY_RECEIVED_MESSAGE, data);
                        msg.setData(bundle);
                        mHandler.sendMessage(msg);
                    }
                }
            } catch (IOException e) {
                e.printStackTrace();
                //isRunning = false;
                System.out.println("Error-CommunicateThread:run()");
            }
        }
    }

    /**
     * 向设备写数据
     *
     * @param message
     * @return boolean为了反馈写的状态 有没有写出去
     */
    public synchronized boolean write(String message) {
        try {
            if (out != null) {
                out.write(message.getBytes());
                return true;
            }
        } catch (IOException e) {
            e.printStackTrace();
//            isRunning = false;
            System.out.println("Error-CommunicateThread:write()");
        }
        return false;
    }

    /**
     * 关闭通信通道
     */
    public void close() {
        isRunning = false;
        if (in != null) {
            try {
                in.close();
                in = null;//释放资源可能不必要 因为close里就有这个操作
            } catch (IOException e) {
                e.printStackTrace();
                System.out.println("Error-CommunicateThread:close()");
            }
        }
        if (out != null) {
            try {
                out.close();
                out = null;//释放资源 可能不必要 因为close里就有这个操作
            } catch (IOException e) {
                e.printStackTrace();
                System.out.println("Error-CommunicateThread:close()");
            }
        }
    }
}
