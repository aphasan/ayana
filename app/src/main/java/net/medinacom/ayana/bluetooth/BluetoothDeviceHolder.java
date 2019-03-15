package net.medinacom.ayana.bluetooth;

import android.bluetooth.BluetoothDevice;
import android.view.View;
import android.widget.TextView;

import net.medinacom.ayana.R;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.recyclerview.selection.ItemDetailsLookup;
import androidx.recyclerview.widget.RecyclerView;

public class BluetoothDeviceHolder extends RecyclerView.ViewHolder {

    public BluetoothDeviceHolder(@NonNull View itemView) {
        super(itemView);
    }

    public void bindModel(BluetoothDevice device) {
        TextView deviceName = itemView.findViewById(R.id.txt_device_name);
        deviceName.setText(device.getName());

        TextView deviceHardwareAddress = itemView.findViewById(R.id.txt_device_hardware_address);
        deviceHardwareAddress.setText(device.getAddress());

        TextView deviceClass = itemView.findViewById(R.id.txt_device_class);
        deviceClass.setText(BluetoothClassTranslator.translate(device.getBluetoothClass().getDeviceClass()));

        itemView.setTag(device);
    }

    public ItemDetailsLookup.ItemDetails<BluetoothDevice> getItemDetails() {
        return new ItemDetailsLookup.ItemDetails<BluetoothDevice>() {
            @Override
            public int getPosition() {
                return getAdapterPosition();
            }

            @Nullable
            @Override
            public BluetoothDevice getSelectionKey() {
                return (BluetoothDevice) itemView.getTag();
            }
        };
    }
}
