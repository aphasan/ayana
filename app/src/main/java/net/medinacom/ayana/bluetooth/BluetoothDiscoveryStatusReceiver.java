package net.medinacom.ayana.bluetooth;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

public class BluetoothDiscoveryStatusReceiver extends BroadcastReceiver {

    private BluetoothDiscoveryStatusListener listener;

    @Override
    public void onReceive(Context context, Intent intent) {
        if (listener == null) return;
        String action = intent.getAction();
        switch (action) {
            case BluetoothAdapter.ACTION_DISCOVERY_STARTED:
                listener.onDiscoveryStarted();
                break;
            case BluetoothDevice.ACTION_FOUND:
                BluetoothDevice bluetoothDevice = intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE);
                listener.onDeviceFound(bluetoothDevice);
                break;
            case BluetoothAdapter.ACTION_DISCOVERY_FINISHED:
                listener.onDiscoveryFinished();
                break;
        }
    }

    public void addListener(BluetoothDiscoveryStatusListener listener) {
        this.listener = listener;
    }

    public void removeListener() {
        this.listener = null;
    }
}