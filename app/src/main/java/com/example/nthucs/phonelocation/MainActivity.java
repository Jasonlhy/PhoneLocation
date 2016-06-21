package com.example.nthucs.phonelocation;

import android.Manifest;
import android.annotation.TargetApi;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
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

import java.util.ArrayList;

public class MainActivity extends AppCompatActivity {
    /**
     * Reqeust code used by Android system
     */
    private static final int REQUEST_CONTACT_CODE = 1;
    private static final int REQUEST_PERMISSION_CODE = 2;

    /**
     * The outgoing call duration
     */
    private static final int ZERO_BIT_DURATION = 1000;
    private static final int ONE_BIT_DURATION = 10000;
    private static final int NEXT_PHONE_CALL_DURATION = 2000;

    /**
     * Size of the encoded
     */
    private final int SIZE = 8;

    /**
     * Object used to handle incoming and outcoming call
     */
    private ScheduleCallManager scheduleCallManager;
    private MyOutCallPhoneListener myOutCallPhoneListener;

    @TargetApi(23)
    public void requestPermissions() {
        /**
         * The following codes are used to request permissions when under API 23
         * For API < 23, the permissions  don't need  to be requested,  they just needed to  be written  inside he AndroidManifest.xml
         * For API 23, the permissions are needed to be written inside the AndroidManifest.xml and request them in Runtime.
         * ActivityCompat is used to provide backward compatible with API < 23, it supposed to do the right thing and return PERMISSION_GRANTED
         */
        String[] permissions = new String[]{Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.CALL_PHONE, Manifest.permission.READ_CONTACTS, Manifest.permission.READ_PHONE_STATE};
        ArrayList<String> permissionNeeded = new ArrayList<String>();
        for (String pem : permissions) {
            if (ActivityCompat.checkSelfPermission(this, pem) == PackageManager.PERMISSION_DENIED) {
                permissionNeeded.add(pem);
            }
        }

        if (permissionNeeded.size() > 0) {
            String[] requestPermissions = new String[permissionNeeded.size()];
            permissionNeeded.toArray(requestPermissions);
            ActivityCompat.requestPermissions(this, requestPermissions, REQUEST_PERMISSION_CODE);
        }
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
        final Button sendLocationButton = (Button) findViewById(R.id.sendLocationButton);
        final Button receiveLocationButton = (Button) findViewById(R.id.receiveLocationButton);
        final TextView textView = (TextView) findViewById((R.id.textView));

        // request location
        this.requestPermissions();

        // listen to the phone call state
        final TelephonyManager telephonyManager = (TelephonyManager) getSystemService(Context.TELEPHONY_SERVICE);

        // my end call manager
        ScheduleCallManager scheduleCallManager = new ScheduleCallManager();
        if (!scheduleCallManager.initialize(telephonyManager, this)) {
            Toast.makeText(getApplicationContext(), "初始化EndCall Manager失敗", Toast.LENGTH_LONG).show();
        }
        this.scheduleCallManager = scheduleCallManager;

        sendLocationButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // This seem to work on nexus 5 simulator with API level 23 only
                // but not working on my mac emulator with api level kitkat 4.4
                // i can't make my mac emulator to receive any location message
                // please test it on real phone
                LocationManager manager = (LocationManager) theMainActivity.getSystemService(Context.LOCATION_SERVICE);
                if (manager.isProviderEnabled(LocationManager.GPS_PROVIDER)) {
                    try {
                        manager.requestSingleUpdate(LocationManager.GPS_PROVIDER, new MyLocationListener(), null);
                        textView.setText("請在等待GPS signal");
                        receiveLocationButton.setEnabled(false);

                        // listen out phone call
                        myOutCallPhoneListener = new MyOutCallPhoneListener();
                        telephonyManager.listen(myOutCallPhoneListener, PhoneStateListener.LISTEN_CALL_STATE);
                    } catch (SecurityException ex) {
                        ex.printStackTrace();
                    }
                } else {
                    Toast.makeText(getApplicationContext(), "請開啟GPS定位服務", Toast.LENGTH_LONG).show();
                }
            }
        });

        receiveLocationButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                textView.setText("請在等待電話打進來");
                sendLocationButton.setEnabled(false);
                telephonyManager.listen(new MyInCallPhoneListener(), PhoneStateListener.LISTEN_CALL_STATE);
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

            // stored the encoded location
            myOutCallPhoneListener.setEncoded(rect);
            pickContact();
        }

        public void pickContact() {
            Intent intent = new Intent(Intent.ACTION_PICK, ContactsContract.Contacts.CONTENT_URI);
            MainActivity.this.startActivityForResult(intent, REQUEST_CONTACT_CODE);
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
                    Log.d("Phone number: ", phoneNumber);
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
    public class MyOutCallPhoneListener extends PhoneStateListener {
        private int[][] encoded;
        private int nextBitIndex = 0;


        public void setEncoded(int[][] encoded) {
            if (encoded.length != SIZE && encoded[0].length != SIZE) {
                throw new IllegalArgumentException("Encoded should be " + SIZE + " * " + SIZE);
            }
            this.encoded = encoded;
            nextBitIndex = 0;
        }

        @Override
        public void onCallStateChanged(int state, String incomingNumber) {
            //  there are some inconsistent behaviours between different API
            // some API will send the signal when phone listener is first registered
            // some API will NOT send the signal when the phone listener is first registered
            // therefore it is better to avoid the situation when the signal is sent  when the listener have null encoded
            if (encoded == null) {
                Log.d("null encoded", "null encoded");
                switch (state) {
                    case TelephonyManager.CALL_STATE_RINGING:
                        Log.d("null encoded", "CALL_STATE_RINGIN");
                        break;
                    case TelephonyManager.CALL_STATE_OFFHOOK:
                        Log.d("null encoded", "CALL_STATE_OFFHOOK");
                        break;
                    case TelephonyManager.CALL_STATE_IDLE:
                        Log.d("null encoded", "CALL_STATE_IDLE");
                        break;
                }
                super.onCallStateChanged(state, incomingNumber);
                return;
            }

            Log.d("null encoded", "not null encoded");
            switch (state) {
                case TelephonyManager.CALL_STATE_RINGING:
                    Log.d("outcall phone listener", "CALL_STATE_RINGIN");
                    break;

                case TelephonyManager.CALL_STATE_OFFHOOK:
                    Log.d("outcall phone listener", "CALL_STATE_OFFHOOK");
                    int content = encoded[nextBitIndex / SIZE][nextBitIndex % SIZE];
                    if (content == 0) {
                        scheduleCallManager.endCall(ZERO_BIT_DURATION);
                    } else {
                        scheduleCallManager.endCall(ONE_BIT_DURATION);
                    }
                    break;
                case TelephonyManager.CALL_STATE_IDLE:
                    Log.d("outcall phone listener", "CALL_STATE_IDLE");
                    if (nextBitIndex < (SIZE * SIZE)) {
                        scheduleCallManager.makeCall(NEXT_PHONE_CALL_DURATION);
                    }
                    // successful deliveried a bit
                    nextBitIndex++;
            }

            super.onCallStateChanged(state, incomingNumber);
        }
    }

    /**
     * Phone Listener listen to the in coming called
     */
    public class MyInCallPhoneListener extends PhoneStateListener {
        private long startTime = 0;
        private long endTime = 0;
        private int nextBitIndex = 0;
        private int[][] encoded;

        public MyInCallPhoneListener() {
            nextBitIndex = SIZE * SIZE;
            encoded = new int[SIZE][SIZE];
        }

        @Override
        public void onCallStateChanged(int state, String incomingNumber) {
            switch (state) {
                case TelephonyManager.CALL_STATE_RINGING:
                    Log.d("Incall phone listener", "CALL_STATE_RINGIN");
                    startTime = System.currentTimeMillis();
                    break;
                case TelephonyManager.CALL_STATE_OFFHOOK:
                    Log.d("Incall phone listener", "CALL_STATE_OFFHOOK");
                case TelephonyManager.CALL_STATE_IDLE:
                    Log.d("Incall phone listener", "CALL_STATE_IDLE");
                    endTime = System.currentTimeMillis();

                    //  avoid the init signal sent by the system
                    if (startTime > 0) {
                        // not all are recieived yet
                        if (nextBitIndex < (SIZE * SIZE)) {
                            long durationMs = endTime - startTime;
                            // actually I don't think the duration of the caller is exactly the same as the sender in term of mill seconds
                            // if there are some difference between them, consider to add a aceeptable range.
                            Log.d("Incall phone listener", durationMs + "");
                            encoded[nextBitIndex / SIZE][nextBitIndex % SIZE] = (durationMs == ZERO_BIT_DURATION) ? 0 : 1;
                            nextBitIndex++;
                        } else {
                            // https://developer.android.com/guide/appendix/g-app-intents.html
                            // geo:latitude,longitude
                            Decoder decoder = new Decoder();
                            decoder.decode(encoded);
                            double longitude = decoder.getLongitude();
                            double latitude = decoder.getLatitude();

                            // show google map
                            String uriStr = "geo:" + latitude + "," + longitude;
                            Uri uri = Uri.parse(uriStr);
                            Intent it = new Intent(Intent.ACTION_VIEW, uri);
                            startActivity(it);
                        }
                    }
            }

            super.onCallStateChanged(state, incomingNumber);
        }
    }
}
