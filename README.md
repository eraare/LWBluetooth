# LWBluetooth
Provide Android Bluetooth serial communication service. As the Client(the mobile) can communicate with Multi Server(the bluetooth equipment) at the same time.

Feature
So easy to use.
One to Many(<=7). One Client can communicate with multi Server at the same time.
Two mode. Use the Service(BluetoothService) or No Service(BluetoothCenter).
Rich Method interface. openBluetooth\closeBluetooth\isSupportBluetooth\unpair and so on.

Simple Usage
Download this library and then import into your workspace and include to your project. Library named lwbluetooth in the LWBluetoothDemo.

No Service Mode (BluetoothCenter)

Declare permission
<uses-permission android:name="android.permission.BLUETOOTH" />
<uses-permission android:name="android.permission.BLUETOOTH_ADMIN" />

Remember to Open Bluetooth

Declare BluetoothCenter
// 1 Declare BluetoothCenter
private BluetoothCenter mCenter = BluetoothCenter.createCenter();

Set Listener
// 2 Set the OnDataReceivedListener and OnStateChangedListener to receive Data and Connect State
mCenter.setOnDataReceivedListener(new BluetoothCenter.OnDataReceivedListener() {
    @Override
    public void onDataReceived(String deviceAddress, String message) {
        // message is the received data
    }
});
mCenter.setmOnStateChangedListener(new BluetoothCenter.OnStateChangedListener() {
    @Override
    public void onStateChanged(String deviceAddress, int state) {
        if (state == BluetoothConstant.STATE_CONNECT_FAIL) {
            // connect failed
        } else if (state == BluetoothConstant.STATE_CONNECT_SUCCESS) {
            // connect succeed
        }
    }
});

Scan Bluetooth equipment
// 3 Scan the bluetooth equipment
Intent intent = new Intent(OtherActivity.this, ScanActivity.class);
/*use the extras to set the theme of the ScanActivity*/
intent.putExtra(BluetoothConstant.EXTRA_THEME_COLOR, Color.BLUE);
intent.putExtra(BluetoothConstant.EXTRA_THEME_TITLE, "蓝牙设备");
intent.putExtra(BluetoothConstant.EXTRA_THEME_SCAN, "开始扫描");
intent.putExtra(BluetoothConstant.EXTRA_THEME_SCANNING, "扫描中...");
startActivityForResult(intent, 1);

Receive Selected Device
protected void onActivityResult(int requestCode, int resultCode, Intent data) {
//        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode == Activity.RESULT_OK) {
            if (requestCode == 1) {
                // 4 Received the selected device
                deviceName = data.getStringExtra(BluetoothConstant.EXTRA_DEVICE_NAME);
                deviceAddress = data.getStringExtra(BluetoothConstant.EXTRA_DEVICE_ADDRESS);
            }
        }
    }

Connect the Device
// 5 Connect the bluetooth device
if (mCenter != null && !TextUtils.equals(deviceAddress, "")) {
    mCenter.connect(deviceAddress);
}

Disconnect the Device
// 6 Disconnect the bluetooth device or disconnect all
if (mCenter != null && !TextUtils.equals(deviceAddress, "")) {
    mCenter.disconnect(deviceAddress);
    // disconnectAll();
}

Send Message to the Device
// 7 Send message to the device or send to all
mCenter.send(deviceAddress, message);
mCenter.sendAll(message);

Destroy and Exit
// 7 sucide
// #warnning Only used at the stage of Exit. Wherever will use the BluetoothCenter please don’t Destroy and exit
if (mCenter != null) {
    mCenter.sucide();
}

Other
public BluetoothAdapter getBluetoothAdapter();
public boolean openBluetooth();
public boolean closeBluetooth();
public boolean isSupportBluetooth();
public void unpair(String deviceAddress);
public void unpair(BluetoothDevice device);

Service Mode (BluetoothService)

Declare IBluetoothService
private IBluetoothService mBluetoothService;

Bind Service
private void bindBluetoothService() {
    Intent service = new Intent(this, BluetoothService.class);
    bindService(service, mServiceConnection, Context.BIND_AUTO_CREATE);
}

private ServiceConnection mServiceConnection = new ServiceConnection() {
    @Override
    public void onServiceConnected(ComponentName name, IBinder service) {
        mBluetoothService = (IBluetoothService) service;
        // 3 set listener to receive data and connect state
        if (mBluetoothService != null) {
            mBluetoothService.setOnDataReceivedListener(mOnDataReceivedListener);
            mBluetoothService.setOnStateChangedListener(mOnStateChangedListener);
        }
    }

    @Override
    public void onServiceDisconnected(ComponentName name) {
        mBluetoothService = null;
    }
};

private BluetoothService.OnDataReceivedListener mOnDataReceivedListener = new BluetoothService.OnDataReceivedListener() {
        @Override
        public void onDataReceived(String deviceAddress, String message) {
                    }
    };

private BluetoothService.OnStateChangedListener mOnStateChangedListener = new BluetoothService.OnStateChangedListener() {
        @Override
        public void onStateChanged(String deviceAddress, int state) {
            if (state == BluetoothConstant.STATE_CONNECT_FAIL) {

            } else if (state == BluetoothConstant.STATE_CONNECT_SUCCESS) {

            }
        }
    };

Scan Bluetooth equipment
As the No Service Mode.

Connect the Device
As the No Service Mode.

Disconnect the Device
As the No Service Mode.

Send Message
As the No Service Mode.

Other
As the No Service Mode.

Destroy and Exit
unbindService(mServiceConnection);

Thank you for reading.
Blog:http://blog.csdn.net/zhaicaixiansheng
