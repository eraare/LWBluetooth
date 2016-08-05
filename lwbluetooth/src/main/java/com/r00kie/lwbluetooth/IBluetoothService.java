package com.r00kie.lwbluetooth;

/**
 * @author Leo
 * @time 2016-08-05
 * @detail 服务操作接口
 */
public interface IBluetoothService {
    /**
     * 根据设备地址进行设备的连接
     *
     * @param deviceAddress
     * @return
     */
    boolean connect(String deviceAddress);

    /**
     * 根据设备地址发送数据
     *
     * @param deviceAddress
     * @param message
     * @return
     */
    boolean send(String deviceAddress, String message);

    /**
     * 向所有设备进行数据发送
     *
     * @param message
     * @return
     */
    boolean sendAll(String message);

    /**
     * 断开某个设备
     *
     * @param deviceAddress
     */
    void disconnect(String deviceAddress);

    /**
     * 断开所有设备
     */
    void disconnectAll();

    /**
     * 判断某个设备是否已连接
     *
     * @param deviceAddress
     * @return
     */
    boolean isConnected(String deviceAddress);

    /**
     * 绑定接收数据的监听器
     * @param onDataReceivedListener
     */
    void setOnDataReceivedListener(BluetoothService.OnDataReceivedListener onDataReceivedListener);

    /**
     * 绑定状态改变监听器
     * @param onStateChangedListener
     */
    void setOnStateChangedListener(BluetoothService.OnStateChangedListener onStateChangedListener);
}
