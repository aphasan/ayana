package net.medinacom.ayana.bluetooth;

import android.bluetooth.BluetoothDevice;

public interface BluetoothBondingStatusListener {
    void onBonding(BluetoothDevice device);
    void onBonded(BluetoothDevice device);
    void onBondingFailed(BluetoothDevice device);
}
