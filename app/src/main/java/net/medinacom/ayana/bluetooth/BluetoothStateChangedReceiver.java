package net.medinacom.ayana.bluetooth;

import android.bluetooth.BluetoothAdapter;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.widget.Toast;

public class BluetoothStateChangedReceiver extends BroadcastReceiver {

    @Override
    public void onReceive(Context context, Intent intent) {
        String action = intent.getAction();
        if(action.equals(BluetoothAdapter.ACTION_STATE_CHANGED)) {
            int bluetoothState = intent.getExtras().getInt(BluetoothAdapter.EXTRA_STATE);
            switch (bluetoothState) {
                case BluetoothAdapter.STATE_TURNING_ON:
                    Toast.makeText(context, "Turning Bluetooth on", Toast.LENGTH_SHORT)
                            .show();
                    break;
                case BluetoothAdapter.STATE_ON:
                    Toast.makeText(context, "Bluetooth on", Toast.LENGTH_SHORT)
                            .show();
                    break;
                case BluetoothAdapter.STATE_TURNING_OFF:
                    Toast.makeText(context, "Turning Bluetooth off", Toast.LENGTH_SHORT)
                            .show();
                    break;
                case BluetoothAdapter.STATE_OFF:
                    Toast.makeText(context, "Bluetooth off", Toast.LENGTH_SHORT)
                            .show();
                    break;
                default:
                    break;
            }
        }
    }
}
