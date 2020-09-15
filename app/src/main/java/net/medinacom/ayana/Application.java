package net.medinacom.ayana;

import android.content.SharedPreferences;

import net.medinacom.ayana.bluetooth.BluetoothDeviceWrapper;

import java.util.Map;

public class Application extends android.app.Application {

    public String getName() {
        return "Ayana";
    }

    public void setDefaultRemote(BluetoothDeviceWrapper device) {
        SharedPreferences prefs = getSharedPreferences(getName() + ".general", MODE_PRIVATE);
        if(device != null) {
            prefs.edit()
                    .putString("default_remote_name", device.getName())
                    .putString("default_remote_address", device.getAddress())
                    .apply();
        } else {
            prefs.edit()
                    .putString("default_remote_name", null)
                    .putString("default_remote_address", null)
                    .apply();
        }
    }

    public BluetoothDeviceWrapper getDefaultRemote() {
        SharedPreferences prefs = getSharedPreferences(getName() + ".general", MODE_PRIVATE);
        String name = prefs.getString("default_remote_name", null);
        String address = prefs.getString("default_remote_address", null);

        return name == null || address == null ? null : new BluetoothDeviceWrapper(name, address);
    }

    public void setBlockMap(String block, int num) {
        SharedPreferences prefs = getSharedPreferences(getName() + ".map", MODE_PRIVATE);
        prefs.edit().putInt(block, num).apply();
    }

    public int getBlockMap(String block) {
        SharedPreferences prefs = getSharedPreferences(getName() + ".map", MODE_PRIVATE);
        return prefs.getInt(block, -1);
    }

    public Map<String, Integer> getBlockMaps() {
        SharedPreferences prefs = getSharedPreferences(getName() + ".map", MODE_PRIVATE);
        return (Map<String, Integer>) prefs.getAll();
    }

    public void onSharedPreferencesChanged(SharedPreferences.OnSharedPreferenceChangeListener listener) {
        SharedPreferences prefs = getSharedPreferences(getName() + ".map", MODE_PRIVATE);
        prefs.registerOnSharedPreferenceChangeListener(listener);
    }

}
