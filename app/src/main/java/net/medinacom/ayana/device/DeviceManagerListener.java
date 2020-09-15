package net.medinacom.ayana.device;

import android.bluetooth.BluetoothDevice;

public interface DeviceManagerListener {
    void onAdapterEnabled();
    void onAdapterDisabled();
    void onDiscoveryStarted();
    void onDiscoveryFound();
    void onDiscoveryFinished();
    void onBonding();
    void onBonded();
    void onBondingFailed();
}
