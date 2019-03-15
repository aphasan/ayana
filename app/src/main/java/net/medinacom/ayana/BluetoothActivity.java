package net.medinacom.ayana;

import android.Manifest;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;

import com.google.android.material.snackbar.Snackbar;

import net.medinacom.ayana.bluetooth.BluetoothAdapterListener;
import net.medinacom.ayana.bluetooth.BluetoothDeviceAdapter;
import net.medinacom.ayana.bluetooth.BluetoothDeviceDetailsLookup;
import net.medinacom.ayana.bluetooth.BluetoothDeviceFoundReceiver;
import net.medinacom.ayana.bluetooth.NoBluetoothAdapterException;
import net.medinacom.ayana.common.TagKeyProvider;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.view.ActionMode;
import androidx.appcompat.widget.Toolbar;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.selection.SelectionPredicates;
import androidx.recyclerview.selection.SelectionTracker;
import androidx.recyclerview.selection.StorageStrategy;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

public class BluetoothActivity extends AppCompatActivity
        implements BluetoothAdapterListener, ActionMode.Callback {

    final int REQUEST_ENABLE_BLUETOOTH = 1;
    final int MY_PERMISSION_REQUEST_ACCESS_COARSE_LOCATION = 2;

    BluetoothAdapter bluetoothAdapter;
    BluetoothDeviceAdapter bluetoothDeviceAdapter;
    BluetoothDeviceFoundReceiver bluetoothDeviceFoundReceiver;

    SelectionTracker<BluetoothDevice> tracker = null;

    SwipeRefreshLayout swipeRefreshLayout;

    Snackbar snackbar;

    ActionMode actionMode = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_bluetooth);

        Toolbar toolbar = findViewById(R.id.bluetooth_toolbar);
        setSupportActionBar(toolbar);

        ActionBar actionBar = getSupportActionBar();

        actionBar.setDisplayHomeAsUpEnabled(true);

        RecyclerView listDevices = findViewById(R.id.list_bluetooth);

        listDevices.setLayoutManager(new LinearLayoutManager(this));

        bluetoothDeviceAdapter = new BluetoothDeviceAdapter();
        listDevices.setAdapter(bluetoothDeviceAdapter);

        tracker = new SelectionTracker.Builder<BluetoothDevice>(
                "mySelection",
                listDevices,
                new TagKeyProvider<BluetoothDevice>(listDevices),
                new BluetoothDeviceDetailsLookup(listDevices),
                StorageStrategy.createParcelableStorage(BluetoothDevice.class)
        ).withSelectionPredicate(SelectionPredicates.createSelectSingleAnything())
                .build();

        tracker.addObserver(new SelectionTracker.SelectionObserver() {
            @Override
            public void onSelectionChanged() {
                if (actionMode == null && tracker.hasSelection()) {
                    startSupportActionMode(BluetoothActivity.this);
                } else if (actionMode != null && !tracker.hasSelection()) {
                    actionMode.finish();
                }
            }
        });

        bluetoothDeviceAdapter.setTracker(tracker);

        bluetoothDeviceFoundReceiver = new BluetoothDeviceFoundReceiver();
        bluetoothDeviceFoundReceiver.addListener(this);

        swipeRefreshLayout = findViewById(R.id.swiperefresh_bluetooth);
        swipeRefreshLayout.setOnRefreshListener(this::startScan);
    }

    @Override
    protected void onStart() {
        super.onStart();
        try {
            setupBluetooth();
        } catch (NoBluetoothAdapterException e) {
            showInfo("Bluetooth adapter not found", "Error", null, 0);
        }
    }

    @Override
    protected void onStop() {
        super.onStop();
        unregisterReceiver(bluetoothDeviceFoundReceiver);
        if (bluetoothAdapter.isDiscovering())
            bluetoothAdapter.cancelDiscovery();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_bluetooth, menu);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.action_bluetooth_refresh:
                if (!bluetoothAdapter.isDiscovering()) {
                    startScan();
                }
                return true;
            case R.id.action_bluetooth_clear:
                bluetoothDeviceAdapter.clearDevice();
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        switch (requestCode) {
            case REQUEST_ENABLE_BLUETOOTH:
                if (resultCode == RESULT_OK) {
                    showInfo("Bluetooth has been enabled", "Info", null, 0);
                } else if (resultCode == RESULT_CANCELED) {
                    showInfo("Bluetooth can't be enabled", "Error", null, 0);
                }
                break;
            default:
                break;
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        switch (requestCode) {
            case MY_PERMISSION_REQUEST_ACCESS_COARSE_LOCATION:
                if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED)
                    startDeviceDiscovery();
                break;
        }
    }

    private void setupBluetooth() throws NoBluetoothAdapterException {

        // Try to find Bluetooth adapter
        bluetoothAdapter = BluetoothAdapter.getDefaultAdapter();

        // If it has been found
        if (bluetoothAdapter != null) {
            // But it is not enabled yet
            if (!bluetoothAdapter.isEnabled()) {
                // Request user to enable it
                Intent enableBluetoothIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
                startActivityForResult(enableBluetoothIntent, REQUEST_ENABLE_BLUETOOTH);
            }
        } else {
            throw new NoBluetoothAdapterException();
        }

    }

    private void startScan() {

        int hasPermission = ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION);
        if (hasPermission == PackageManager.PERMISSION_DENIED) {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.ACCESS_COARSE_LOCATION}, MY_PERMISSION_REQUEST_ACCESS_COARSE_LOCATION);
        } else {
            startDeviceDiscovery();
        }
    }

    private void startDeviceDiscovery() {
        if (bluetoothAdapter.isDiscovering()) return;

        IntentFilter discoveryStartedFilter = new IntentFilter(BluetoothAdapter.ACTION_DISCOVERY_STARTED);
        IntentFilter deviceFoundFilter = new IntentFilter(BluetoothDevice.ACTION_FOUND);
        IntentFilter discoveryFinishedFilter = new IntentFilter(BluetoothAdapter.ACTION_DISCOVERY_FINISHED);
        registerReceiver(bluetoothDeviceFoundReceiver, discoveryStartedFilter);
        registerReceiver(bluetoothDeviceFoundReceiver, deviceFoundFilter);
        registerReceiver(bluetoothDeviceFoundReceiver, discoveryFinishedFilter);

        bluetoothAdapter.startDiscovery();
    }

    public void showInfo(String msg, String actionTitle, View.OnClickListener action, int duration) {
        snackbar = Snackbar.make(findViewById(R.id.coordinator_bluetooth), msg, duration == 0 ? Snackbar.LENGTH_SHORT : duration);
        if (action != null)
            snackbar.setAction(actionTitle, action);

        snackbar.show();
    }

    @Override
    public void onDiscoveryStarted() {
        swipeRefreshLayout.setRefreshing(true);
    }

    @Override
    public void onDeviceFound(BluetoothDevice bluetoothDevice) {
        bluetoothDeviceAdapter.addDevice(bluetoothDevice);
    }

    @Override
    public void onDiscoveryFinished() {
        swipeRefreshLayout.setRefreshing(false);
    }

    @Override
    public boolean onCreateActionMode(ActionMode mode, Menu menu) {
        actionMode = mode;
        mode.getMenuInflater().inflate(R.menu.menu_context_bluetooth, menu);
        return true;
    }

    @Override
    public boolean onPrepareActionMode(ActionMode mode, Menu menu) {
        return false;
    }

    @Override
    public boolean onActionItemClicked(ActionMode mode, MenuItem item) {
        switch ((item.getItemId())) {
            case R.id.action_bluetooth_pair:
                return true;
            default:
                return false;
        }
    }

    @Override
    public void onDestroyActionMode(ActionMode mode) {
        actionMode = null;
    }
}
