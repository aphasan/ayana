package net.medinacom.ayana.bluetooth;

import android.bluetooth.BluetoothDevice;

public interface BluetoothAdapterListener {
    public void onDiscoveryStarted();
    public void onDeviceFound(BluetoothDevice bluetoothDevice);
    public void onDiscoveryFinished();
}
