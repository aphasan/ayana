package net.medinacom.ayana.bluetooth;

import android.bluetooth.BluetoothDevice;
import android.view.MotionEvent;
import android.view.View;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.recyclerview.selection.ItemDetailsLookup;
import androidx.recyclerview.widget.RecyclerView;

public class BluetoothDeviceDetailsLookup extends ItemDetailsLookup<BluetoothDevice> {

    private RecyclerView recyclerView;

    public BluetoothDeviceDetailsLookup(RecyclerView recyclerView) {
        super();
        this.recyclerView = recyclerView;
    }

    @Nullable
    @Override
    public ItemDetails<BluetoothDevice> getItemDetails(@NonNull MotionEvent e) {
        View view = recyclerView.findChildViewUnder(e.getX(), e.getY());
        if(view != null) {
            return ((BluetoothDeviceHolder) recyclerView.getChildViewHolder(view)).getItemDetails();
        }
        return null;
    }
}
