package com.r00kie.lwbluetooth;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;

import java.lang.reflect.Method;

/**
 * @author Leo
 * @time 2016-08-05
 * @detail bluetooth utils: provide method unpairDevice() to unpair the bluetooth device
 */
public final class BluetoothUtil {
    /**
     * unpair bluetooth device
     *
     * @param device
     */
    public static void unpairDevice(BluetoothDevice device) {
        try {
            Class c = BluetoothDevice.class;
            Method m = c.getMethod("removeBond", (Class[]) null);
            m.invoke(device, (Object[]) null);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * unpair bluetooth device by address
     *
     * @param address
     */
    public static void unpairDevice(String address) {
        BluetoothAdapter mAdapter = BluetoothAdapter.getDefaultAdapter();
        try {
            BluetoothDevice device = mAdapter.getRemoteDevice(address);
            Class c = BluetoothDevice.class;
            Method m = c.getMethod("removeBond", (Class[]) null);
            m.invoke(device, (Object[]) null);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
