package com.r00kie.lwbluetooth;

/**
 * @author Leo
 * @time 2016-08-05
 * @detail Constants used by this Library Project
 */
public final class BluetoothConstant {
    // Extras of the device name and device address
    public static final String EXTRA_DEVICE_NAME = "extra_device_name";
    public static final String EXTRA_DEVICE_ADDRESS = "extra_device_address";
    public static final String EXTRA_RECEIVED_MESSAGE = "extra_received_message";
    public static final String EXTRA_THEME_TITLE = "extra_theme_title";
    public static final String EXTRA_THEME_SCAN = "extra_theme_scan";
    public static final String EXTRA_THEME_SCANNING = "extra_theme_scanning";
    public static final String EXTRA_THEME_COLOR = "extra_theme_color";
    // The flags of the handle message
    public static final int WHAT_RECEIVED_DATA = 0;
    //    public static final int WHAT_CONNECT_ERROR = 2;
//    public static final int WHAT_CONNECT_SUCCESS = 3;
    public static final int WHAT_CONNECT_STATE = 1;
    // Connect state's action
    public static final String ACTION_RECEIVED_DATA = "action.RECEIVED_DATA";
    public static final String ACTION_CONNECT_FAIL = "action.CONNECT_FAIL";
    public static final String ACTION_CONNECT_SUCCESS = "action.CONNECT_SUCCESS";
    // keys
    public static final String KEY_DEVICE_ADDRESS = "key_device_address";
    public static final String KEY_RECEIVED_MESSAGE = "key_received_message";

    public static final int STATE_CONNECT_SUCCESS = 100;
    public static final int STATE_CONNECT_FAIL = 101;
}
