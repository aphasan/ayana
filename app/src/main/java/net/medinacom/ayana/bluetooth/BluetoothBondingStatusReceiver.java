package net.medinacom.ayana.bluetooth;

import android.bluetooth.BluetoothDevice;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

public class BluetoothBondingStatusReceiver extends BroadcastReceiver {

    private BluetoothBondingStatusListener listener;

    @Override
    public void onReceive(Context context, Intent intent) {
        String action = intent.getAction();
        if(listener != null && action.equals(BluetoothDevice.ACTION_BOND_STATE_CHANGED) ) {
            BluetoothDevice device = intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE);
            int bondState = intent.getIntExtra(BluetoothDevice.EXTRA_BOND_STATE, -1);
            switch (bondState) {
                case BluetoothDevice.BOND_BONDING:
                    listener.onBonding(device);
                    break;
                case BluetoothDevice.BOND_BONDED:
                    listener.onBonded(device);
                    break;
                case BluetoothDevice.BOND_NONE:
                    listener.onBondingFailed(device);
                    break;
                default:
                    break;
            }
        }
    }

    public void addListener(BluetoothBondingStatusListener listener) {
        this.listener = listener;
    }
}
