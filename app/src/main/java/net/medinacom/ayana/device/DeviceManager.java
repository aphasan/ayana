package net.medinacom.ayana.device;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.util.Log;

import net.medinacom.ayana.Application;
import net.medinacom.ayana.R;
import net.medinacom.ayana.bluetooth.BluetoothDeviceWrapper;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Formatter;
import java.util.List;

import androidx.appcompat.app.AppCompatActivity;

import static net.medinacom.ayana.device.Commands.CHANGENAME;
import static net.medinacom.ayana.device.Commands.CHANGEPSWD;
import static net.medinacom.ayana.device.Commands.CONNECTED;

public class DeviceManager extends BroadcastReceiver {

    public static final int ENGAGING = 1;
    public static final int ENGAGED = 2;
    public static final int DISENGAGED = 3;

    private AppCompatActivity context;
    private BluetoothAdapter adapter;
    private BluetoothDeviceWrapper defaultRemote;
    private List<BluetoothDeviceWrapper> neighbours = new ArrayList<>();
    private int status = DISENGAGED;
    private boolean listening = false;

    private DeviceManagerListener managerListener;

    private DeviceManager() {
        this.adapter = BluetoothAdapter.getDefaultAdapter();
    }

    public DeviceManager putInContext(AppCompatActivity context) {
        if (this.context != null && !this.context.equals(context)) {

            stopDiscovery();

            if (listening) {
                if (this.context != null & listening) {
                    this.context.unregisterReceiver(this);
                    listening = false;
                }
            }

            removePrevContext();

        }

        this.context = context;

        defaultRemote = ((Application) context.getApplication()).getDefaultRemote();
        if (defaultRemote != null) {
            BluetoothDevice device = adapter.getRemoteDevice(defaultRemote.getAddress());
            defaultRemote.wrap(device);
            status = ENGAGED;
        }

        return this;
    }

    public void removePrevContext() {
        status = DISENGAGED;

        neighbours.clear();

    }

    private void connectToDefault() throws IOException {
        BluetoothSocket remoteSocket = defaultRemote.getSocket();
        remoteSocket.connect();
    }

    public void connect() throws IOException {
        if(defaultRemote == null)  throw new IOException(context.getResources().getString(R.string.no_connecting_device));
        stopDiscovery();
        connectToDefault();
    }

    public void disconnect() throws IOException {
        BluetoothSocket remoteSocket = defaultRemote.getSocket();

        if (remoteSocket != null && remoteSocket.isConnected())
            remoteSocket.close();
    }

    private static DeviceManager instance;

    public static DeviceManager getInstance() {
        if (instance == null) instance = new DeviceManager();
        return instance;
    }

    public DeviceManager listenAdapterEvents() {
        context.registerReceiver(this, new IntentFilter(BluetoothAdapter.ACTION_STATE_CHANGED));
        listening = true;
        return this;
    }

    public DeviceManager listenDiscoveryEvents() {
        context.registerReceiver(this, new IntentFilter(BluetoothAdapter.ACTION_DISCOVERY_STARTED));
        context.registerReceiver(this, new IntentFilter(BluetoothDevice.ACTION_FOUND));
        context.registerReceiver(this, new IntentFilter(BluetoothAdapter.ACTION_DISCOVERY_FINISHED));
        listening = true;
        return this;
    }

    public DeviceManager listenBondStateEvents() {
        context.registerReceiver(this, new IntentFilter(BluetoothDevice.ACTION_BOND_STATE_CHANGED));
        listening = true;
        return this;
    }

    public DeviceManager init() {
        if (adapter != null && !adapter.isEnabled()) {
            adapter.enable();
        }
        return this;
    }

    public void startDiscovery() {
        if(adapter == null) return;
        if (!adapter.isDiscovering())
            adapter.startDiscovery();
    }

    public void stopDiscovery() {
        if(adapter == null) return;
        if (adapter.isDiscovering()) {
            adapter.cancelDiscovery();
        }
    }

    public void pair(BluetoothDeviceWrapper device) {
        device.getWrapped().createBond();
    }

    public void unpair(BluetoothDeviceWrapper device) {
        try {
            Method m = BluetoothDevice.class.getMethod("removeBond", new Class<?>[]{});
            m.invoke(device.getWrapped(), new Object[]{});
        } catch (IllegalAccessException e) {
            e.printStackTrace();
        } catch (InvocationTargetException e) {
            e.printStackTrace();
        } catch (NoSuchMethodException e) {
            e.printStackTrace();
        }
    }

    public void engage(BluetoothDeviceWrapper device) {
        int bondState = device.getWrapped().getBondState();
        switch (bondState) {
            case BluetoothDevice.BOND_NONE:
                status = ENGAGING;
                pair(device);
                return;
            case BluetoothDevice.BOND_BONDED:
                defaultRemote = device;
                ((Application) context.getApplication()).setDefaultRemote(defaultRemote);
                status = ENGAGED;
                managerListener.onBonded();
                return;
        }
    }

    public void disengage() {
        defaultRemote = null;
        ((Application) this.context.getApplication()).setDefaultRemote(null);
    }

    @Override
    public void onReceive(Context context, Intent intent) {
        String action = intent.getAction();
        switch (action) {
            case BluetoothAdapter.ACTION_DISCOVERY_STARTED:
                if (managerListener != null) managerListener.onDiscoveryStarted();
                break;
            case BluetoothDevice.ACTION_FOUND:
                BluetoothDevice neighbour = intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE);
                addDevice(neighbour);
                if (managerListener != null) managerListener.onDiscoveryFound();
                break;
            case BluetoothAdapter.ACTION_DISCOVERY_FINISHED:
                if (managerListener != null) managerListener.onDiscoveryFinished();
                break;
            case BluetoothDevice.ACTION_BOND_STATE_CHANGED:
                BluetoothDevice device = intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE);
                int bondState = intent.getIntExtra(BluetoothDevice.EXTRA_BOND_STATE, -1);
                switch (bondState) {
                    case BluetoothDevice.BOND_BONDING:
                        if (managerListener != null) managerListener.onBonding();
                        break;
                    case BluetoothDevice.BOND_BONDED:
                        if (status == ENGAGING) {
                            engage(new BluetoothDeviceWrapper(device));
                        }
                        break;
                    case BluetoothDevice.BOND_NONE:
                        if (managerListener != null) managerListener.onBondingFailed();
                        break;
                    default:
                        break;
                }
                break;
            case BluetoothAdapter.ACTION_STATE_CHANGED:
                int newState = intent.getExtras().getInt(BluetoothAdapter.EXTRA_STATE);
                int prevState = intent.getExtras().getInt(BluetoothAdapter.EXTRA_PREVIOUS_STATE);
                if (newState == BluetoothAdapter.STATE_ON || newState == BluetoothAdapter.STATE_OFF) {
//                    adapterStatus = newState;
                }
                onAdapterStateChangeEvent(newState, prevState);
                break;
        }
    }

    private void onAdapterStateChangeEvent(int newState, int prevState) {
        String newStatus = newState == CONNECTED ? "CONNECTED" : "DISCONNECTED";
        String prevStatus = prevState == CONNECTED ? "CONNECTED" : "DISCONNECTED";
    }

    public List<BluetoothDeviceWrapper> getNeighbours() {
        if (defaultRemote != null) {
            BluetoothDevice device = adapter.getRemoteDevice(defaultRemote.getAddress());
            addDevice(defaultRemote.wrap(device));
        }

        return neighbours;
    }

    public boolean addDevice(BluetoothDeviceWrapper bluetoothDeviceWrapper) {
        if (!neighbours.contains(bluetoothDeviceWrapper)) {
            return neighbours.add(bluetoothDeviceWrapper);
        } else {
            int indexOfExisting = neighbours.indexOf(bluetoothDeviceWrapper);
            BluetoothDeviceWrapper existing = neighbours.get(indexOfExisting);
            existing.wrap(bluetoothDeviceWrapper.getWrapped());
        }
        return false;
    }

    public boolean addDevice(BluetoothDevice bluetoothDevice) {
        BluetoothDeviceWrapper bluetoothDeviceWrapper = new BluetoothDeviceWrapper(bluetoothDevice);
        return addDevice(bluetoothDeviceWrapper);
    }

    public void registerListener(DeviceManagerListener listener) {
        this.managerListener = listener;
    }

    public boolean isEngaged(BluetoothDeviceWrapper device) {
        return defaultRemote != null && device.getAddress().equals(defaultRemote.getAddress());
    }

    public void send(byte[] bytes) throws IOException {
        BluetoothSocket socket = defaultRemote.getSocket();
        if (socket != null && socket.isConnected()) {
            socket.getOutputStream().write(bytes);
        }
    }

    public interface OnReceiveListener {
        void onReceive(String what, String... params);
    }

    private OnReceiveListener onReceiveListener;

    public void setOnReceiveListener(OnReceiveListener listener) {
        this.onReceiveListener = listener;
    }

    public void receive() {
        BluetoothSocket socket = defaultRemote.getSocket();
        if (socket != null && socket.isConnected()) {
            try {
                InputStream in = socket.getInputStream();
                int prevByteRead = -1;
                int byteRead;
                ByteArrayOutputStream out = new ByteArrayOutputStream(64);
                while ((byteRead = in.read()) != -1) {
                    if (byteRead != 0) {
                        if (byteRead == 0x0A && prevByteRead == 0x0D) {
                            String ret = out.toString();
                            if (ret != null && !ret.isEmpty()) {
                                String[] split1 = ret.split("=");
                                String cmd = split1[0];
                                String[] split2 = split1[1].split(",");
                                if (onReceiveListener != null)
                                    onReceiveListener.onReceive(cmd, split2);
                            }
                            out.reset();
                            prevByteRead = -1;
                        } else {
                            if (prevByteRead != -1) out.write(prevByteRead);
                            prevByteRead = byteRead;
                        }
                    }

                }
            } catch (IOException ex) {
                Log.e("ELOL", ex.getMessage());
            }
        }
    }

    public void changeName(String newName) {
        try {
            String cmd = toString(CHANGENAME, newName);
            connectToDefault();
            send(cmd.getBytes());
            disconnect();
        } catch (IOException ex) {
            Log.e(getClass().getCanonicalName(), ex.getMessage());
            onReceiveListener.onReceive("ERROR", "Communication problem");
        }
    }

    public void changePassword(String newPassword) {
        try {
            String cmd = toString(CHANGEPSWD, newPassword);
            connectToDefault();
            send(cmd.getBytes());
            disconnect();
        } catch (IOException ex) {
            Log.e(getClass().getCanonicalName(), ex.getMessage());
            onReceiveListener.onReceive("ERROR", "Communication problem");
        }
    }

    private String toString(int cmd, Object... params) {
        Formatter fmt = new Formatter();
        return fmt.format(Commands.get(cmd), params).toString();
    }
}
