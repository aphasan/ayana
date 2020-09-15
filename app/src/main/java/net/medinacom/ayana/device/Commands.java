package net.medinacom.ayana.device;

public final class Commands {

    public static final int DISCONNECTED = -5;
    public static final int DISCONNECTING = -4;
    public static final int CONNECTED = -3;
    public static final int CONNECTING = -2;
    public static final int ERROR = -1;
    public static final int LAMPOFF = 0;
    public static final int LAMPON = 1;
    public static final int POINTEROFF = 2;
    public static final int POINTERON = 3;
    public static final int SERVOROTATE = 4;
    public static final int SERVOINCPOS = 5;
    public static final int SERVODECPOS = 6;
    public static final int SERVOCALIB = 7;
    public static final int SWITCHMAP = 8;
    public static final int CHANGENAME = 9;
    public static final int CHANGEPSWD = 10;

    private static final String[] list;

    static {
        list = new String[]{
                "XC+LAMPOFF=%d\r\n",
                "XC+LAMPON=%d,%d\r\n",
                "XC+POINTEROFF=%d\r\n",
                "XC+POINTERON=%d\r\n",
                "XC+SERVOROTATE=%d\r\n",
                "XC+SERVOINCPOS=%d,%d\r\n",
                "XC+SERVODECPOS=%d,%d\r\n",
                "XC+SERVOCALIB=%d\r\n",
                "XC+SWITCHMAP=%d,%d\r\n",
                "AT+NAME=%s\r\n",
                "AT+PSWD=%s\r\n"
        };
    }

    public static String get(int key) {
        if (key < 0 || key >= list.length) return null;
        return list[key];
    }
}
