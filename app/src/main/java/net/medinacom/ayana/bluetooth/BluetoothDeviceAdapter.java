package net.medinacom.ayana.bluetooth;

import android.bluetooth.BluetoothDevice;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import net.medinacom.ayana.R;

import java.util.ArrayList;
import java.util.List;

import androidx.annotation.NonNull;
import androidx.recyclerview.selection.SelectionTracker;
import androidx.recyclerview.widget.RecyclerView;

public class BluetoothDeviceAdapter extends RecyclerView.Adapter<BluetoothDeviceHolder> {

    private List<BluetoothDevice> devices = new ArrayList<>();
    private SelectionTracker<BluetoothDevice> tracker = null;

    @NonNull
    @Override
    public BluetoothDeviceHolder onCreateViewHolder(@NonNull ViewGroup viewGroup, int i) {
        View view = LayoutInflater.from(viewGroup.getContext()).inflate(R.layout.card_bluetooth_device, viewGroup, false);
        return new BluetoothDeviceHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull BluetoothDeviceHolder bluetoothDeviceHolder, int i) {
        bluetoothDeviceHolder.bindModel(devices.get(i));
        bluetoothDeviceHolder.itemView.setActivated(tracker.isSelected((BluetoothDevice) bluetoothDeviceHolder.itemView.getTag()));
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    public BluetoothDevice getItem(int position) {
        return devices.get(position);
    }

    public int getPosition(String key) {
        int i = 0;
        for(BluetoothDevice device : devices) {
            if(device.getAddress().equals(key)) {
                break;
            }
            i++;
        }
        return i;
    }

    @Override
    public int getItemCount() {
        return devices.size();
    }

    public void addDevice(BluetoothDevice bluetoothDevice) {
        if (!devices.contains(bluetoothDevice)) {
            if (devices.add(bluetoothDevice))
                notifyDataSetChanged();
        }
    }

    public void removeDevice(BluetoothDevice bluetoothDevice) {
        if (devices.remove(bluetoothDevice))
            notifyDataSetChanged();
    }

    public void clearDevice() {
        devices.clear();
        notifyDataSetChanged();
    }

    public void setTracker(SelectionTracker<BluetoothDevice> tracker) {
        this.tracker = tracker;
    }
}
