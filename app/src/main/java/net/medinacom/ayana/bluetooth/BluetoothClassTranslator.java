package net.medinacom.ayana.bluetooth;

import android.bluetooth.BluetoothClass.Device;

import java.util.HashMap;
import java.util.Map;

public final class BluetoothClassTranslator {

    private static final Map<Integer, String> classMap = new HashMap<>();

    static {
        classMap.put(Device.AUDIO_VIDEO_CAMCORDER, "AUDIO VIDEO CAMCORDER");
        classMap.put(Device.COMPUTER_LAPTOP, "COMPUTER LAPTOP");
        classMap.put(Device.PHONE_SMART, "PHONE SMART");
    }

    public static String translate(int num) {
        String desc = classMap.get(num);
        return desc != null ? desc : "Unknown";
    }
}
