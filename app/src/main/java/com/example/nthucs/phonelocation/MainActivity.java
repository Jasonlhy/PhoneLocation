package com.example.nthucs.phonelocation;

import android.Manifest;
import android.annotation.TargetApi;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.location.Location;
import android.location.LocationListener;
import android.net.Uri;
import android.os.Bundle;
import android.provider.ContactsContract;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.telephony.TelephonyManager;
import android.util.Log;
import android.view.View;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;
import android.telephony.PhoneStateListener;

public class MainActivity extends AppCompatActivity {

    private static final int REQUEST_CONTACT = 1;
    private ScheduleCallManager scheduleCallManager;

    @TargetApi(23)
    public void requestPermissions() {
        // not backward compatible with API < 23
        ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.CALL_PHONE}, 1000);
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

        this.requestPermissions();

        // listen to the phone state
        MyPhoneStateListener mPhoneStateListener = new MyPhoneStateListener();
        TelephonyManager telephonyManager = (TelephonyManager) getSystemService(Context.TELEPHONY_SERVICE);
        telephonyManager.listen(mPhoneStateListener, PhoneStateListener.LISTEN_CALL_STATE);

        // my end call manager
        ScheduleCallManager scheduleCallManager = new ScheduleCallManager();
        if (!scheduleCallManager.initialize(telephonyManager, this)){
            Toast.makeText(getApplicationContext(), "初始化EndCall Manager失敗", Toast.LENGTH_LONG).show();
        }
        this.scheduleCallManager = scheduleCallManager;

        button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(Intent.ACTION_PICK, ContactsContract.Contacts.CONTENT_URI);
                startActivityForResult(intent, REQUEST_CONTACT);


                // not working in my simulator comment out first
                /* LocationManager manager = (LocationManager) theMainActivity.getSystemService(Context.LOCATION_SERVICE);
              // List<String> providers =  manager.getAllProviders();


                List<String> providers = manager.getProviders(true);
                String bestProviders = null;
                if (providers.contains(LocationManager.GPS_PROVIDER)){
                    bestProviders = LocationManager.GPS_PROVIDER;
                } else if (providers.contains(LocationManager.NETWORK_PROVIDER)){
                    bestProviders = LocationManager.NETWORK_PROVIDER;
                }

                try {
                    manager.requestSingleUpdate(bestProviders, new MyLocationListener(), null);
                } catch (SecurityException ex) {
                    ex.printStackTrace();
                } */


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
            int[][] rect = Encode.encode(twlatitude, twlongitude);
            for (int i = 0; i < rect.length; i++) {
                for (int j = 0; j < rect[0].length; j++) {
                    System.out.println(rect[i][j] + " ");
                }
                System.out.println();
            }

            pickContact();
            // makePhoneCall();
        }

        public void pickContact() {
            Intent intent = new Intent(Intent.ACTION_PICK, ContactsContract.Contacts.CONTENT_URI);
            MainActivity.this.startActivityForResult(intent, REQUEST_CONTACT);
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

    @Override
    public void onActivityResult(int reqCode, int resultCode, Intent data) {
        super.onActivityResult(reqCode, resultCode, data);


        if (resultCode == Activity.RESULT_OK) {
            Uri contactData = data.getData();
            Cursor c = getContentResolver().query(contactData, null, null, null, null);

            // the table returned from the built in contact list do not contain the number
            // you have to retreive the phone number table using another uri...
            if (c.moveToFirst()) {
                String contactId = c.getString(c.getColumnIndex(ContactsContract.Contacts._ID));
                String contactName = c.getString(c.getColumnIndex(ContactsContract.Contacts.DISPLAY_NAME));
                int hasPhoneNumber = c.getInt(c.getColumnIndex(ContactsContract.Contacts.HAS_PHONE_NUMBER));

                if (hasPhoneNumber == 1) {
                    String[] selection = new String[]{ContactsContract.CommonDataKinds.Phone.NUMBER};
                    String filter = ContactsContract.CommonDataKinds.Phone.CONTACT_ID + "=" + contactId;
                    Cursor phoneCursor = getContentResolver().query(ContactsContract.CommonDataKinds.Phone.CONTENT_URI, null, filter, null, null);
                    phoneCursor.moveToNext();
                    String phoneNumber = phoneCursor.getString(phoneCursor.getColumnIndex(ContactsContract.CommonDataKinds.Phone.NUMBER));

                    TextView textView = (TextView) findViewById(R.id.textView);
                    textView.setText("正在聯絡 " + contactName + "...");
                    scheduleCallManager.setPhoneNumber(phoneNumber);
                    scheduleCallManager.makeCall();
                } else {
                    Toast.makeText(getApplicationContext(), "請點選有至少一位電話號碼的聯絡人", Toast.LENGTH_LONG).show();
                }
            }
            c.close();
        } else {
            Toast.makeText(getApplicationContext(), "請點選聯絡人", Toast.LENGTH_LONG).show();
        }
    }

    /**
     *
     */
    public class MyPhoneStateListener extends PhoneStateListener {
        private final int SIZE = 8;
        private int [][] encoded;
        private int i = 0;


        public void setEncoded(int [][] encoded){
            if (encoded.length != SIZE && encoded[0].length != SIZE){
                throw new IllegalArgumentException("Encoded should be " + SIZE + " * " + SIZE);
            }
            this.encoded = encoded;
            i = 0;
        }

        @Override
        public void onCallStateChanged(int state, String incomingNumber) {
            switch (state) {
                case TelephonyManager.CALL_STATE_RINGING:
                    Log.d("phone listener", "CALL_STATE_RINGIN");
                    break;

                case TelephonyManager.CALL_STATE_OFFHOOK:
                    Log.d("phone listener", "CALL_STATE_OFFHOOK");
                    int content = encoded[i / SIZE][i % SIZE];
                    if (content == 0) {
                        scheduleCallManager.endCall(1000);
                    } else {
                        scheduleCallManager.endCall(10000);
                    }
                    break;

                case TelephonyManager.CALL_STATE_IDLE:
                    Log.d("phone listener", "CALL_STATE_IDLE");
                    if (i < (SIZE * SIZE)){
                        scheduleCallManager.makeCall(2000);
                    }
                    // successful
                    i++;
            }

            super.onCallStateChanged(state, incomingNumber);
        }
    }

}
