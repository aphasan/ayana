package net.medinacom.ayana.bluetooth;

import android.bluetooth.BluetoothDevice;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.ParcelUuid;
import android.os.Parcelable;

public class BluetoothFetchUuidsReceiver extends BroadcastReceiver {

    private BluetoothFetchUuidsListener listener;

    @Override
    public void onReceive(Context context, Intent intent) {
        String action = intent.getAction();
        if(listener != null && action.equals(BluetoothDevice.ACTION_UUID) ) {
            BluetoothDevice device = intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE);
            Parcelable[] objs = (Parcelable[]) intent.getExtras().get(BluetoothDevice.EXTRA_UUID);
            for(Parcelable obj : objs) {
                ParcelUuid uuid = (ParcelUuid) obj;
                listener.onFetchUuid(device, uuid.toString());
            }
        }
    }

    public void addListener(BluetoothFetchUuidsListener listener) {
        this.listener = listener;
    }
}
