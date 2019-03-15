package net.medinacom.ayana;

import android.content.Intent;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

public class SnellenActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_snellen);

        Toolbar toolbar = findViewById(R.id.snellen_toolbar);
        setSupportActionBar(toolbar);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_snellen, menu);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.action_connect:
                return true;
            case R.id.action_calibration:
                Intent intentCalibration = new Intent(this, CalibrationActivity.class);
                startActivity(intentCalibration);
                return true;
            case R.id.action_bluetooth:
                Intent intentBluetooth = new Intent(this, BluetoothActivity.class);
                startActivity(intentBluetooth);
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }
}
