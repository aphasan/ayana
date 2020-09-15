package net.medinacom.ayana.bluetooth;

import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;
import android.os.Parcel;
import android.os.Parcelable;
import android.util.Log;

import java.io.IOException;
import java.util.UUID;

import androidx.annotation.Nullable;
import androidx.versionedparcelable.NonParcelField;

public class BluetoothDeviceWrapper implements Parcelable {

    private android.bluetooth.BluetoothDevice device;

    private String name;
    private String address;

    @NonParcelField
    private BluetoothSocket socket = null;

    public BluetoothDeviceWrapper(String name, String address) {
        this.name = name;
        this.address = address;
    }

    public BluetoothDeviceWrapper(BluetoothDevice device) {
        this.device = device;
    }

    public String getName() {
        String queriedName = device.getName();
        if(queriedName == null) return name;
        else return queriedName;
    }

    public String getAddress() {
        if (device != null) return device.getAddress();
        else return address;
    }

    public BluetoothDevice getWrapped() {
        return device;
    }

    public BluetoothDeviceWrapper wrap(BluetoothDevice device) {
        this.device = device;
        String queriedName = device.getName();
        if(name != null) this.name = queriedName;
        return this;
    }

    public BluetoothSocket getSocket() {
        if(socket == null || !socket.isConnected()) {
            try {
                socket = device.createRfcommSocketToServiceRecord(UUID.fromString("00001101-0000-1000-8000-00805F9B34FB"));
                } catch (IOException e) {
                Log.e(getClass().getCanonicalName(), e.getMessage());
            }
        }

        return socket;
    }

    @Override
    public boolean equals(@Nullable Object obj) {
        ((BluetoothDeviceWrapper) obj).getAddress();
        return this.getAddress().equals(((BluetoothDeviceWrapper) obj).getAddress());
    }

    public static final Creator<BluetoothDeviceWrapper> CREATOR = new Creator<BluetoothDeviceWrapper>() {
        @Override
        public BluetoothDeviceWrapper createFromParcel(Parcel in) {
            return new BluetoothDeviceWrapper(in);
        }

        @Override
        public BluetoothDeviceWrapper[] newArray(int size) {
            return new BluetoothDeviceWrapper[size];
        }
    };

    protected BluetoothDeviceWrapper(Parcel in) {
        device = in.readParcelable(BluetoothDevice.class.getClassLoader());
        name = in.readString();
        address = in.readString();
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeParcelable(device, flags);
        dest.writeString(name);
        dest.writeString(address);
    }
}
