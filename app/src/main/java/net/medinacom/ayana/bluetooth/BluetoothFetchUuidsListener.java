package net.medinacom.ayana.bluetooth;

import android.bluetooth.BluetoothDevice;

public interface BluetoothFetchUuidsListener {
    void onFetchUuid(BluetoothDevice device, String uuid);
}
