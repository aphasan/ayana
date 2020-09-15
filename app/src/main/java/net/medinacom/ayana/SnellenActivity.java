package net.medinacom.ayana;

import android.content.ClipData;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.DragEvent;
import android.view.View;
import android.view.WindowManager;
import android.widget.ImageButton;
import android.widget.SeekBar;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.ToggleButton;

import androidx.appcompat.app.AppCompatActivity;

import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.snackbar.Snackbar;

import net.medinacom.ayana.device.SnellenBoard;
import net.medinacom.ayana.device.SnellenBoardListener;

import java.util.HashMap;
import java.util.Map;

public class SnellenActivity extends AppCompatActivity
        implements SeekBar.OnSeekBarChangeListener,
        SnellenBoardListener {

    public static String MAPPING_BLOCK_FROM = "mapping_block_from";

    public static final int DIRECT_ORIENTATION = 0;
    public static final int DIRECT_MULTIPLIER = 0;
    public static final int DIRECT_SIGN = 1;

    public static final int MIRROR_ORIENTATION = 1;
    public static final int MIRROR_MULTIPLIER = 12;
    public static final int MIRROR_SIGN = -1;

    SnellenBoard snellenBoard;

    FloatingActionButton buttonConnect;
    FloatingActionButton buttonBluetooth;

    ImageButton buttonPointer;

    View panelPointer;

    View panelCalibration;

    Switch swCalibration;

    ToggleButton swMirror;

    SeekBar seekPointer;

    TextView txtPointerPosition;

    Snackbar snackbar;

    View activeButton = null;

    ImageButton button1;
    ImageButton button2;
    ImageButton button3;
    ImageButton button4;
    ImageButton button5;
    ImageButton button6;
    ImageButton button7;
    ImageButton button8;
    ImageButton button9;
    ImageButton button10;
    ImageButton button11;

    Map<Integer, View> mapView = new HashMap<>();

    int multiplier = DIRECT_MULTIPLIER;
    int sign = DIRECT_SIGN;
    int orientation = DIRECT_ORIENTATION;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.fragment_snellen);

        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);

        buttonConnect = findViewById(R.id.button_connect);
        buttonConnect.bringToFront();

        buttonBluetooth = findViewById(R.id.button_bluetooth);
        buttonBluetooth.bringToFront();

        buttonPointer = findViewById(R.id.button_pointer);

        panelPointer = findViewById(R.id.panel_pointer);
        panelPointer.setVisibility(View.GONE);

        panelCalibration = findViewById(R.id.panel_calibration);
        panelCalibration.setVisibility(View.GONE);

        seekPointer = findViewById(R.id.seek_pointer);
        seekPointer.setOnSeekBarChangeListener(this);
        seekPointer.setLayoutDirection(View.LAYOUT_DIRECTION_LTR);

        swCalibration = findViewById(R.id.sw_calibration);
        swCalibration.setOnCheckedChangeListener((buttonView, isChecked) -> {
            if (isChecked) panelCalibration.setVisibility(View.VISIBLE);
            else {
                panelCalibration.setVisibility(View.GONE);
                panelCalibration.bringToFront();
            }
        });

        swMirror = findViewById(R.id.sw_mirror);
        swMirror.setOnCheckedChangeListener((buttonView, isChecked) -> {
            toggleOrientation();
        });
        swMirror.setVisibility(View.GONE);

        txtPointerPosition = findViewById(R.id.txt_pointer_position);

        button1 = findViewById(R.id.button1);
        button1.setTag(1);
        button1.setOnLongClickListener(this::onSwapBlockStarted);
        button1.setOnDragListener(this::onSwapBlock);
        mapView.put(1, button1);

        button2 = findViewById(R.id.button2);
        button2.setTag(2);
        button2.setOnLongClickListener(this::onSwapBlockStarted);
        button2.setOnDragListener(this::onSwapBlock);
        mapView.put(2, button2);

        button3 = findViewById(R.id.button3);
        button3.setTag(3);
        button3.setOnLongClickListener(this::onSwapBlockStarted);
        button3.setOnDragListener(this::onSwapBlock);
        mapView.put(3, button3);

        button4 = findViewById(R.id.button4);
        button4.setTag(4);
        button4.setOnLongClickListener(this::onSwapBlockStarted);
        button4.setOnDragListener(this::onSwapBlock);
        mapView.put(4, button4);

        button5 = findViewById(R.id.button5);
        button5.setTag(5);
        button5.setOnLongClickListener(this::onSwapBlockStarted);
        button5.setOnDragListener(this::onSwapBlock);
        mapView.put(5, button5);

        button6 = findViewById(R.id.button6);
        button6.setTag(6);
        button6.setOnLongClickListener(this::onSwapBlockStarted);
        button6.setOnDragListener(this::onSwapBlock);
        mapView.put(6, button6);
        button6.setImageResource(getButton6ImageRes(-1));

        button7 = findViewById(R.id.button7);
        button7.setTag(7);
        button7.setOnLongClickListener(this::onSwapBlockStarted);
        button7.setOnDragListener(this::onSwapBlock);
        mapView.put(7, button7);

        button8 = findViewById(R.id.button8);
        button8.setTag(8);
        button8.setOnLongClickListener(this::onSwapBlockStarted);
        button8.setOnDragListener(this::onSwapBlock);
        mapView.put(8, button8);

        button9 = findViewById(R.id.button9);
        button9.setTag(9);
        button9.setOnLongClickListener(this::onSwapBlockStarted);
        button9.setOnDragListener(this::onSwapBlock);
        mapView.put(9, button9);

        button10 = findViewById(R.id.button10);
        button10.setTag(10);
        button10.setOnLongClickListener(this::onSwapBlockStarted);
        button10.setOnDragListener(this::onSwapBlock);
        mapView.put(10, button10);

        button11 = findViewById(R.id.button11);
        button11.setTag(11);
        button11.setOnLongClickListener(this::onSwapBlockStarted);
        button11.setOnDragListener(this::onSwapBlock);
        mapView.put(11, button11);

        snellenBoard = new SnellenBoard(this).init();
        snellenBoard.registerListener(this);

    }

    @Override
    protected void onStart() {
        super.onStart();
        seekPointer.setProgress(0);
    }

    @Override
    protected void onResume() {
        super.onResume();
        snellenBoard.start();
    }

    @Override
    protected void onPause() {
        super.onPause();
        if (activeButton != null) onToggleBlock(activeButton);
        snellenBoard.stop();
    }

    @Override
    protected void onStop() {
        super.onStop();
        if(activeButton != null)
        onToggleBlock(activeButton);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
    }

    public void onDeviceSetting(View view) {
        Intent settingIntent = new Intent(this, BluetoothActivity.class);
        startActivity(settingIntent);
    }

    public void toggleOrientation() {
        switch(orientation) {
            case DIRECT_ORIENTATION:
                orientation = MIRROR_ORIENTATION;
                multiplier = MIRROR_MULTIPLIER;
                sign = MIRROR_SIGN;
                seekPointer.setProgress(0);
                break;
            case MIRROR_ORIENTATION:
                orientation = DIRECT_ORIENTATION;
                multiplier = DIRECT_MULTIPLIER;
                sign = DIRECT_SIGN;
                seekPointer.setProgress(12);
                break;
        }
    }

    public void onToggleConnection(View view) {
        if (!snellenBoard.isConnected()) {
            snellenBoard.connect();
        } else {
            if(activeButton != null) {
                activeButton.setBackgroundColor(getResources().getColor(R.color.lamp_off));
                activeButton = null;
            }
            buttonConnect.setImageResource(R.drawable.ic_remote_off);
            snellenBoard.disconnect();
        }
    }

    public void onToggleBlock(View view) {
        if (activeButton != null) {
            if (view.equals(activeButton)) {
                snellenBoard.turnBlockOff((Integer) activeButton.getTag());
            } else if (!view.equals(activeButton)) {
                snellenBoard.turnBlockOn((Integer) view.getTag(), (Integer) activeButton.getTag());
            }
        } else {
            snellenBoard.turnBlockOn((Integer) view.getTag(), 0);
        }
    }

    public void onTogglePointer(View view) {
        if (!snellenBoard.isPointerOn()) {
            snellenBoard.turnPointerOn();
        } else {
            snellenBoard.turnPointerOff();
        }
    }

    public void onCalibInc(View view) {
        snellenBoard.incPointer();
    }

    public void onCalibDec(View view) {
        snellenBoard.decPointer();
    }

    public void onCalibSave(View view) {
        snellenBoard.saveCalib();
    }

    public boolean onSwapBlockStarted(View view) {

       if(panelCalibration.isShown()) {
            panelCalibration.setVisibility(View.GONE);
        }

        if(panelPointer.isShown()) {
            panelPointer.setVisibility(View.GONE);
        }

        Intent intentClipdata = new Intent();
        intentClipdata.putExtra(MAPPING_BLOCK_FROM, (Integer) view.getTag());

        ClipData clipData = ClipData.newIntent(MAPPING_BLOCK_FROM, intentClipdata);
        View.DragShadowBuilder shadow = new View.DragShadowBuilder(view);
        view.startDrag(clipData, shadow, Boolean.TRUE, 0);

        return true;
    }

    public boolean onSwapBlock(View view, DragEvent event) {

        switch (event.getAction()) {
            case DragEvent.ACTION_DROP:
                ClipData.Item clipdataItem = event.getClipData().getItemAt(0);
                Intent intentClipdata = clipdataItem.getIntent();
                int src = intentClipdata.getIntExtra(MAPPING_BLOCK_FROM, -1);
                int dst = (int) view.getTag();
                snellenBoard.swapBlockMapping(src, dst);
                break;
            default:
                break;
        }

        return true;
    }

    @Override
    public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
        snellenBoard.shiftPointer(multiplier + (sign * progress));
        txtPointerPosition.setText(String.valueOf(progress * 15));
        if(!snellenBoard.isPointerOn())
            button6.setImageResource(getButton6ImageRes(-1));
        else
            button6.setImageResource(getButton6ImageRes(12 - progress));
    }

    @Override
    public void onStartTrackingTouch(SeekBar seekBar) {
    }

    @Override
    public void onStopTrackingTouch(SeekBar seekBar) {

    }

    @Override
    public void onConnecting() {
        showInfo(getResources().getString(R.string.connecting_label), null, null, 0);
    }

    @Override
    public void onConnected() {
        buttonConnect.setImageResource(R.drawable.ic_remote_on);
        snackbar.dismiss();
        showInfo(getResources().getString(R.string.connected_label), null, null, Snackbar.LENGTH_SHORT);
    }

    @Override
    public void onConnectingFailed() {
        showInfo(getResources().getString(R.string.connecting_failed_label), null, null, Snackbar.LENGTH_SHORT);
    }

    @Override
    public void onDisconnecting() {
        showInfo(getResources().getString(R.string.disconnecting_label), null, null, 0);
    }

    @Override
    public void onDisconnected() {
        snackbar.dismiss();
        showInfo(getResources().getString(R.string.disconnected_label), null, null, Snackbar.LENGTH_SHORT);
        buttonBluetooth.setEnabled(false);
    }

    @Override
    public void onBlockOn(int newNum, int prevNum) {
        if (prevNum != 0) {
            onBlockOff(prevNum);
        }

        View view = mapView.get(newNum);
        view.setBackgroundColor(getResources().getColor(R.color.lamp_on));
        if (view.getTag().equals(6)) {
            panelPointer.setVisibility(View.VISIBLE);
            panelPointer.bringToFront();
            swMirror.setVisibility(View.VISIBLE);
            snellenBoard.shiftPointer(0);
            button6.setImageResource(getButton6ImageRes(-1));
            seekPointer.setProgress(0);
            button1.setVisibility(View.GONE);
            button2.setVisibility(View.GONE);
            button3.setVisibility(View.GONE);
            button4.setVisibility(View.GONE);
            button5.setVisibility(View.GONE);
            button7.setVisibility(View.GONE);
            button8.setVisibility(View.GONE);
            button9.setVisibility(View.GONE);
            button10.setVisibility(View.GONE);
            button11.setVisibility(View.GONE);
            buttonBluetooth.setEnabled((false));
            buttonConnect.setEnabled(false);
        }
        activeButton = view;
    }

    @Override
    public void onBlockOff(int num) {
        View view = mapView.get(num);

        view.setBackgroundColor(getResources().getColor(R.color.lamp_off));

        if (view.getTag().equals(6) && panelPointer.isShown()) {
            if(snellenBoard.isPointerOn()) onTogglePointer(null);
            snellenBoard.shiftPointer(0);
            button6.setImageResource(getButton6ImageRes(-1));
            seekPointer.setProgress(0);
            swMirror.setVisibility(View.GONE);
            panelPointer.setVisibility(View.GONE);
            panelCalibration.setVisibility(View.GONE);
            swCalibration.setChecked(false);
            button1.setVisibility(View.VISIBLE);
            button2.setVisibility(View.VISIBLE);
            button3.setVisibility(View.VISIBLE);
            button4.setVisibility(View.VISIBLE);
            button5.setVisibility(View.VISIBLE);
            button7.setVisibility(View.VISIBLE);
            button8.setVisibility(View.VISIBLE);
            button9.setVisibility(View.VISIBLE);
            button10.setVisibility(View.VISIBLE);
            button11.setVisibility(View.VISIBLE);
            buttonBluetooth.setEnabled((true));
            buttonConnect.setEnabled(true);
        }
        activeButton = null;
    }

    @Override
    public void onPointerMoved(int position) {
    }

    @Override
    public void onCommandError(String message) {
        snackbar.dismiss();
        showInfo(message, null, null, Snackbar.LENGTH_SHORT);
    }

    @Override
    public void onPointerOn(int arg1) {
        buttonPointer.setImageResource(R.drawable.ic_pointer_on);
        button6.setImageResource(getButton6ImageRes(12 - snellenBoard.getPointerPosition()));
    }

    @Override
    public void onPointerOff(int arg1) {
        buttonPointer.setImageResource(R.drawable.ic_pointer_off);
        button6.setImageResource(getButton6ImageRes(-1));
    }

    @Override
    public void onCalibSaved() {
        showInfo(getResources().getString(R.string.calibration_saved_label), null, null, Snackbar.LENGTH_SHORT);
    }

    @Override
    public void onSwitchMapped(int arg1, int arg2) {
        showInfo(getResources().getString(R.string.mapping_saved_info), null, null, Snackbar.LENGTH_SHORT);
    }

    public void showInfo(String msg, String actionTitle, View.OnClickListener action, int duration) {
        snackbar = Snackbar.make(findViewById(R.id.coordinator_snellen), msg, duration == 0 ? Snackbar.LENGTH_INDEFINITE : duration);
        if (action != null)
            snackbar.setAction(actionTitle, action);
        snackbar.show();
    }

    private int getButton6ImageRes(int seekPointePosition) {
        switch (seekPointePosition) {
            case 12:
                return R.drawable.ic_61;
            case 11:
                return R.drawable.ic_62;
            case 10:
                return R.drawable.ic_63;
            case 9:
                return R.drawable.ic_64;
            case 8:
                return R.drawable.ic_65;
            case 7:
                return R.drawable.ic_66;
            case 6:
                return R.drawable.ic_67;
            case 5:
                return R.drawable.ic_68;
            case 4:
                return R.drawable.ic_69;
            case 3:
                return R.drawable.ic_610;
            case 2:
                return R.drawable.ic_611;
            case 1:
                return R.drawable.ic_612;
            case 0:
                return R.drawable.ic_613;
            default:
                return R.drawable.ic_6;
        }
    }
}
