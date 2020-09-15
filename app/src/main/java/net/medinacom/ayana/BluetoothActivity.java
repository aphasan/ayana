package net.medinacom.ayana;

import android.Manifest;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;

import com.google.android.material.snackbar.Snackbar;

import net.medinacom.ayana.bluetooth.BluetoothDeviceAdapter;
import net.medinacom.ayana.bluetooth.BluetoothDeviceDetailsLookup;
import net.medinacom.ayana.bluetooth.BluetoothDeviceWrapper;
import net.medinacom.ayana.common.TagKeyProvider;
import net.medinacom.ayana.device.DeviceManager;
import net.medinacom.ayana.device.DeviceManagerListener;

import androidx.annotation.NonNull;
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
        implements DeviceManagerListener, ActionMode.Callback {

    final int MY_PERMISSION_REQUEST_ACCESS_COARSE_LOCATION = 1;

    BluetoothDeviceAdapter bluetoothDeviceAdapter;

    SelectionTracker<BluetoothDeviceWrapper> tracker = null;

    SwipeRefreshLayout swipeRefreshLayout;

    DeviceManager deviceManager;

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

        deviceManager = DeviceManager.getInstance();
        deviceManager.putInContext(this);
        deviceManager.listenAdapterEvents()
                .listenDiscoveryEvents()
                .listenBondStateEvents()
                .init();
        deviceManager.registerListener(this);

        RecyclerView listDevices = findViewById(R.id.list_bluetooth);
        listDevices.setLayoutManager(new LinearLayoutManager(this));
        bluetoothDeviceAdapter = new BluetoothDeviceAdapter(deviceManager.getNeighbours());
        listDevices.setAdapter(bluetoothDeviceAdapter);

        tracker = new SelectionTracker.Builder<BluetoothDeviceWrapper>(
                "mySelection",
                listDevices,
                new TagKeyProvider<>(listDevices),
                new BluetoothDeviceDetailsLookup(listDevices),
                StorageStrategy.createParcelableStorage(BluetoothDeviceWrapper.class)
        ).withSelectionPredicate(SelectionPredicates.createSelectSingleAnything())
                .build();

        tracker.addObserver(new SelectionTracker.SelectionObserver() {
            @Override
            public void onSelectionChanged() {
                if (actionMode == null && tracker.hasSelection()) {
                    startSupportActionMode(BluetoothActivity.this);
                } else if (actionMode != null && !tracker.hasSelection()) {
                    actionMode.finish();
                } else if (actionMode != null && tracker.hasSelection()) {
                    actionMode.invalidate();
                }
            }
        });

        bluetoothDeviceAdapter.setTracker(tracker);

        swipeRefreshLayout = findViewById(R.id.swiperefresh_bluetooth);
        swipeRefreshLayout.setOnRefreshListener(this::startScan);
    }

    @Override
    protected void onStop() {
        super.onStop();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
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
                deviceManager.startDiscovery();
                return true;
            case R.id.action_bluetooth_clear:
                bluetoothDeviceAdapter.clearDevice();
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    @Override
    public boolean onCreateActionMode(ActionMode mode, Menu menu) {
        actionMode = mode;
        mode.getMenuInflater().inflate(R.menu.menu_context_bluetooth, menu);
        return true;
    }

    @Override
    public boolean onPrepareActionMode(ActionMode mode, Menu menu) {
        BluetoothDeviceWrapper device = tracker.getSelection().iterator().next();
        boolean isPaired = deviceManager.isEngaged(device);
        String actionLabel = getResources().getString(isPaired ? R.string.action_bluetooth_unpair_label : R.string.action_bluetooth_pair_label);
        menu.findItem(R.id.action_bluetooth_pair).setTitle(actionLabel);
        if (!isPaired) {
            MenuItem itemChangeName = menu.findItem(R.id.action_change_name);
            itemChangeName.setVisible(false);
            MenuItem itemChangePassword = menu.findItem(R.id.action_change_password);
            itemChangePassword.setVisible(false);
        }
        return true;
    }

    @Override
    public boolean onActionItemClicked(ActionMode mode, MenuItem item) {
        BluetoothDeviceWrapper device = tracker.getSelection().iterator().next();
        deviceManager.stopDiscovery();
        switch ((item.getItemId())) {
            case R.id.action_bluetooth_pair:
                if (deviceManager.isEngaged(device)) {
                    deviceManager.disengage();
                } else {
                    deviceManager.engage(device);
                }
                tracker.clearSelection();
                return true;
            case R.id.action_change_name:
                ChangeNameDialog changeNameDialog = new ChangeNameDialog();
                changeNameDialog.registerListener(this::onChangeName);
                changeNameDialog.show(getSupportFragmentManager(), "Change Name");
                return true;
            case R.id.action_change_password:
                ChangePasswordDialog changePasswordDialog = new ChangePasswordDialog();
                changePasswordDialog.registerListener(this::onChangePassword);
                changePasswordDialog.show(getSupportFragmentManager(), "Change Password");
                return true;
            default:
                return false;
        }
    }

    private void onReceive(String what, String... params) {
        Log.e(what, params[0]);
        switch (what) {
            case "AT+NAME":
                snackbar.dismiss();
                showInfo(getResources().getString(R.string.name_changed), null, null, 0);
                break;
            case "AT+PSWD":
                snackbar.dismiss();
                showInfo(getResources().getString(R.string.password_changed), null, null, 0);
                break;
            case "ERROR":
                snackbar.dismiss();
                showInfo(params[0], null, null, 0);
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        switch (requestCode) {
            case MY_PERMISSION_REQUEST_ACCESS_COARSE_LOCATION:
                if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED)
                    startScan();
                break;
        }
    }

    private void startScan() {
        int hasPermission = ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION);
        if (hasPermission == PackageManager.PERMISSION_DENIED) {
            swipeRefreshLayout.setRefreshing(false);
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.ACCESS_COARSE_LOCATION}, MY_PERMISSION_REQUEST_ACCESS_COARSE_LOCATION);
        } else {
            deviceManager.startDiscovery();
        }
    }

    public void showInfo(String msg, String actionTitle, View.OnClickListener action, int duration) {
        snackbar = Snackbar.make(findViewById(R.id.coordinator_bluetooth), msg, duration == 0 ? Snackbar.LENGTH_SHORT : duration);
        if (action != null)
            snackbar.setAction(actionTitle, action);
        snackbar.show();
    }

    @Override
    public void onAdapterEnabled() {

    }

    @Override
    public void onAdapterDisabled() {

    }

    @Override
    public void onDiscoveryStarted() {
        swipeRefreshLayout.setRefreshing(true);
        showInfo(getResources().getString(R.string.device_discovering_started),
                getResources().getString(R.string.device_stop_discovering),
                view -> deviceManager.stopDiscovery(),
                Snackbar.LENGTH_INDEFINITE);
    }

    @Override
    public void onDiscoveryFound() {
        bluetoothDeviceAdapter.notifyDataSetChanged();
    }

    @Override
    public void onDiscoveryFinished() {
        swipeRefreshLayout.setRefreshing(false);
        if (snackbar != null && snackbar.isShown()) snackbar.dismiss();
    }

    @Override
    public void onDestroyActionMode(ActionMode mode) {
        actionMode = null;
    }

    @Override
    public void onBonding() {
        showInfo(getResources().getString(R.string.device_bonding), null, null, Snackbar.LENGTH_INDEFINITE);
    }

    @Override
    public void onBonded() {
        bluetoothDeviceAdapter.notifyDataSetChanged();
        if (snackbar != null && snackbar.isShown())
            snackbar.dismiss();
    }

    @Override
    public void onBondingFailed() {
        bluetoothDeviceAdapter.notifyDataSetChanged();
    }

    private void onChangeName(String newName) {
        showInfo(getResources().getString(R.string.changing_name), null, null, 0);
        deviceManager.changeName(newName);
    }

    private void onChangePassword(String newPswd) {
        showInfo(getResources().getString(R.string.changing_password), null, null, 0);
        deviceManager.changePassword(newPswd);
    }
}
