package com.example.nthucs.phonelocation;

import android.Manifest;
import android.annotation.TargetApi;
import android.content.Context;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.View;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.Button;

public class MainActivity extends AppCompatActivity {

    @TargetApi(23)
    public void requestLocationPermisson(){
        requestPermissions(new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, 1000);
    }

        @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Snackbar.make(view, "Replace with your own action", Snackbar.LENGTH_LONG)
                        .setAction("Action", null).show();
            }
        });

        final MainActivity theMainActivity = this;
        Button button = (Button) findViewById(R.id.button);
        button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                LocationManager manager = (LocationManager) theMainActivity.getSystemService(Context.LOCATION_SERVICE);

                try {
                    manager.requestSingleUpdate(LocationManager.GPS_PROVIDER, new MyLocationListener(), null);
                } catch (SecurityException ex){
                   ex.printStackTrace();
                }
            }
        });
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }
}

class MyLocationListener implements LocationListener {
    @Override
    public void onLocationChanged(Location location) {
        Log.i("The location", location.toString());
        double latitude = location.getLatitude();
        double longitude = location.getLongitude();

        String latitudeString = Double.toString(latitude);
        String longitudeString = Double.toString(longitude);

        // only x.xxxxxx
        int index;
        index = latitudeString.indexOf(".");
        double twlatitude = Double.parseDouble(latitudeString.substring(index - 1));
        index = longitudeString.indexOf(".");
        double twlongitude = Double.parseDouble(longitudeString.substring(index - 1));

        Log.d("twlatitude", twlatitude + "");
        Log.d("twlongitude", twlongitude + "");
        int [][] rect = Encode.encode(twlatitude, twlongitude);
        for (int i = 0; i < rect.length; i++)
            for (int j = 0; j < rect[0].length; j++)
                System.out.println(rect[i][j]);
    }

    @Override
    public void onStatusChanged(String provider, int status, Bundle extras) {

    }

    @Override
    public void onProviderEnabled(String provider) {

    }

    @Override
    public void onProviderDisabled(String provider) {

    }
}

