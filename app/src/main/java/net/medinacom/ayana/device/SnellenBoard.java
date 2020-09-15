package net.medinacom.ayana.device;

import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.os.Parcel;
import android.os.Parcelable;
import android.util.Log;

import androidx.appcompat.app.AppCompatActivity;
import androidx.versionedparcelable.NonParcelField;

import java.io.IOException;
import java.util.Formatter;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

import static net.medinacom.ayana.device.Commands.CONNECTED;
import static net.medinacom.ayana.device.Commands.CONNECTING;
import static net.medinacom.ayana.device.Commands.DISCONNECTED;
import static net.medinacom.ayana.device.Commands.DISCONNECTING;
import static net.medinacom.ayana.device.Commands.ERROR;
import static net.medinacom.ayana.device.Commands.LAMPOFF;
import static net.medinacom.ayana.device.Commands.LAMPON;
import static net.medinacom.ayana.device.Commands.POINTEROFF;
import static net.medinacom.ayana.device.Commands.POINTERON;
import static net.medinacom.ayana.device.Commands.SERVOCALIB;
import static net.medinacom.ayana.device.Commands.SERVODECPOS;
import static net.medinacom.ayana.device.Commands.SERVOINCPOS;
import static net.medinacom.ayana.device.Commands.SERVOROTATE;
import static net.medinacom.ayana.device.Commands.SWITCHMAP;

public class SnellenBoard implements Parcelable {

    private String version = "0.0";

    private boolean connected = false;

    private Integer prevBlock = 0;

    private Integer activeBlock = 0;

    private boolean pointerOn = false;

    private int pointerPosition = -1;

    private int calibStep = 20;

    @NonParcelField
    private DeviceManager manager;

    @NonParcelField
    private AppCompatActivity context;

    @NonParcelField
    private SnellenBoardListener listener;

    @NonParcelField
    private CommandLooper commandLooper;

    public SnellenBoard(AppCompatActivity context) {
        this.context = context;
        manager = DeviceManager.getInstance().init();
    }

    public SnellenBoard init() {
        manager.setOnReceiveListener(this::onReceive);
        commandLooper = new CommandLooper();
        return this;
    }

    public void start() {
        manager.putInContext(context);
        if (!commandLooper.isAlive())
            commandLooper.start();
    }

    public void connect() {
        Message msg = commandHandler.obtainMessage(CONNECTING);
        commandHandler.sendMessage(msg);
    }

    public void disconnect() {
        if (activeBlock > 0) turnBlockOff(activeBlock);
        if (pointerPosition > 0) shiftPointer(0);
        if (isPointerOn()) turnPointerOff();

        Message msg = commandHandler.obtainMessage(DISCONNECTING);
        commandHandler.sendMessage(msg);
    }

    public void stop() {
        if (isConnected()) {
            if (activeBlock > 0) {
                turnBlockOff(activeBlock);
            }
            if (pointerPosition > 0) {
                shiftPointer(0);
            }
            if (isPointerOn()) turnPointerOff();
            disconnect();
        }
    }

    public void turnBlockOn(int newNum, int prevNum) {
        putCommand(pack(LAMPON, newNum, prevNum));
    }

    public void turnBlockOff(int num) {
        putCommand(pack(LAMPOFF, num, 0));
    }

    public void shiftPointer(int position) {
        putCommand(pack(SERVOROTATE, position, 0));
    }

    public void incPointer() {
        putCommand(pack(SERVOINCPOS, pointerPosition, calibStep));
    }

    public void decPointer() {
        putCommand(pack(SERVODECPOS, pointerPosition, calibStep));
    }

    public void saveCalib() {
        putCommand(pack(SERVOCALIB, 1));
    }

    public void swapBlockMapping(int src, int dst) {
        putCommand(pack(SWITCHMAP, src, dst));
    }

    public void turnPointerOn() {
        putCommand(pack(POINTERON, 1));
    }

    public void turnPointerOff() {
        putCommand(pack(POINTEROFF, 1));
    }

    public void putCommand(String cmd) {
        if (!isConnected()) return;
        try {
            queue.put(cmd);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    public String getVersion() {
        return version;
    }

    public void registerListener(SnellenBoardListener listener) {
        this.listener = listener;
    }

    protected SnellenBoard(Parcel in) {
        version = in.readString();
        connected = in.readByte() != 0;
        pointerOn = in.readByte() != 0;
        pointerPosition = in.readInt();
    }

    public static final Creator<SnellenBoard> CREATOR = new Creator<SnellenBoard>() {
        @Override
        public SnellenBoard createFromParcel(Parcel in) {
            return new SnellenBoard(in);
        }

        @Override
        public SnellenBoard[] newArray(int size) {
            return new SnellenBoard[size];
        }
    };

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(version);
        dest.writeByte((byte) (connected ? 1 : 0));
        dest.writeByte((byte) (pointerOn ? 1 : 0));
        dest.writeInt(pointerPosition);
    }

    @NonParcelField
    private Handler commandHandler;

    private String pack(int cmd, Object... params) {
        Formatter fmt = new Formatter();
        return fmt.format(Commands.get(cmd), params).toString();
    }

    private byte[] toBytes(int cmd, Object... params) {
        String cmdString = pack(cmd, params);
        return cmdString.getBytes();
    }

    @NonParcelField
    private BlockingQueue<String> queue = new LinkedBlockingQueue<>(18);

    @NonParcelField
    private Lock lock = new ReentrantLock();

    @NonParcelField
    public Condition condition = lock.newCondition();

    @NonParcelField
    ExecutorService messageSenderExecutor = Executors.newSingleThreadExecutor();

    private void retrieveCmd() {
        while (true) {
            lock.lock();
            messageSenderExecutor.submit(() -> {
                try {
                    manager.send(queue.take().getBytes());
                } catch (IOException e) {
                    e.printStackTrace();
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            });
            try {
                condition.await(1000, TimeUnit.MILLISECONDS);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            lock.unlock();
        }
    }

    private boolean commandHandle(Message msg) {
        switch (msg.what) {
            case CONNECTING:
                uiHandler.sendMessage(uiHandler.obtainMessage(CONNECTING));

                try {
                    manager.connect();
                    connected = true;

                    ExecutorService executorService = Executors.newFixedThreadPool(2);
                    executorService.submit(manager::receive);
                    executorService.submit(this::retrieveCmd);
                    executorService.shutdown();

                    uiHandler.sendMessage(uiHandler.obtainMessage(CONNECTED));
                } catch (IOException e) {
                    e.printStackTrace();
                }
                break;
            case DISCONNECTING:
                uiHandler.sendMessage(uiHandler.obtainMessage(DISCONNECTING));
                try {
                    manager.disconnect();
                    connected = false;
                    uiHandler.sendMessage(uiHandler.obtainMessage(DISCONNECTED));
                } catch (IOException e) {
                    e.printStackTrace();
                }
                break;
        }
        return true;
    }

    public void onReceive(String what, String... params) {
        switch (what) {
            case "LAMPOFF":
                activeBlock = 0;
                prevBlock = Integer.parseInt(params[0]);
                uiHandler.sendMessage(uiHandler.obtainMessage(LAMPOFF, prevBlock, 0));
                break;
            case "LAMPON": {
                activeBlock = Integer.parseInt(params[0]);
                prevBlock = Integer.parseInt(params[1]);
                uiHandler.sendMessage(uiHandler.obtainMessage(LAMPON, activeBlock, prevBlock));
                break;
            }
            case "SERVOROTATE":
                pointerPosition = Integer.parseInt(params[0]);
                uiHandler.sendMessage(uiHandler.obtainMessage(SERVOROTATE, pointerPosition, 0));
                break;
            case "POINTERON":
                pointerOn = true;
                uiHandler.sendMessage(uiHandler.obtainMessage(POINTERON, Integer.parseInt(params[0]), 0));
                break;
            case "POINTEROFF":
                pointerOn = false;
                uiHandler.sendMessage(uiHandler.obtainMessage(POINTEROFF, Integer.parseInt(params[0]), 0));
                break;
            case "SERVOCALIB":
                uiHandler.sendMessage(uiHandler.obtainMessage(SERVOCALIB, Integer.parseInt(params[0]), 0));
                break;
            case "SWITCHMAP":
                uiHandler.sendMessage(uiHandler.obtainMessage(SWITCHMAP, Integer.parseInt(params[0]), Integer.parseInt(params[1])));
                break;
        }
        lock.lock();
        condition.signalAll();
        lock.unlock();
    }

    private class CommandLooper extends Thread {

        public void run() {
            Looper.prepare();
            commandHandler = new Handler(SnellenBoard.this::commandHandle);
            Looper.loop();
        }
    }

    private boolean uiHandleMessage(Message msg) {
        switch (msg.what) {
            case LAMPOFF:
                listener.onBlockOff(msg.arg1);
                break;
            case LAMPON:
                listener.onBlockOn(msg.arg1, msg.arg2);
                break;
            case POINTERON:
                listener.onPointerOn(msg.arg1);
                break;
            case POINTEROFF:
                listener.onPointerOff(msg.arg1);
                break;
            case SERVOROTATE:
                listener.onPointerMoved(msg.arg1);
                break;
            case ERROR:
                listener.onCommandError((String) msg.obj);
                break;
            case CONNECTING:
                listener.onConnecting();
                break;
            case CONNECTED:
                listener.onConnected();
                break;
            case DISCONNECTING:
                listener.onDisconnecting();
                break;
            case DISCONNECTED:
                listener.onDisconnected();
                break;
            case SERVOCALIB:
                listener.onCalibSaved();
                break;
            case SWITCHMAP:
                listener.onSwitchMapped(msg.arg1, msg.arg2);
                break;
        }
        return true;
    }

    @NonParcelField
    public Handler uiHandler = new Handler(Looper.getMainLooper(), SnellenBoard.this::uiHandleMessage);

    public int getActiveBlock() {
        return activeBlock;
    }

    public boolean isPointerOn() {
        return pointerOn;
    }

    public boolean isConnected() {
        return connected;
    }

    public int getPointerPosition() {
        return pointerPosition;
    }

    public void queryVersion() {

    }
}
