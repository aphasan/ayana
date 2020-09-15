package net.medinacom.ayana.bluetooth;

import android.view.View;
import android.widget.TextView;

import net.medinacom.ayana.R;
import net.medinacom.ayana.device.DeviceManager;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.recyclerview.selection.ItemDetailsLookup;
import androidx.recyclerview.widget.RecyclerView;

public class BluetoothDeviceHolder extends RecyclerView.ViewHolder {

    public BluetoothDeviceHolder(@NonNull View itemView) {
        super(itemView);
    }

    public void bindModel(BluetoothDeviceWrapper device) {
        TextView deviceName = itemView.findViewById(R.id.txt_device_name);
        deviceName.setText(device.getName());

        TextView deviceHardwareAddress = itemView.findViewById(R.id.txt_device_hardware_address);
        deviceHardwareAddress.setText(device.getAddress());

        TextView deviceClass = itemView.findViewById(R.id.txt_device_class);
        String bondingState = this.itemView.getResources()
                .getString(DeviceManager.getInstance().isEngaged(device) ? R.string.device_bonded : R.string.device_unbonded);
        deviceClass.setText(bondingState);

        itemView.setTag(device);
    }

    public ItemDetailsLookup.ItemDetails<BluetoothDeviceWrapper> getItemDetails() {
        return new ItemDetailsLookup.ItemDetails<BluetoothDeviceWrapper>() {
            @Override
            public int getPosition() {
                return getAdapterPosition();
            }

            @Nullable
            @Override
            public BluetoothDeviceWrapper getSelectionKey() {
                return (BluetoothDeviceWrapper) itemView.getTag();
            }
        };
    }
}
