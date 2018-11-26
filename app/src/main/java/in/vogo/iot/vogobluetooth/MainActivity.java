package in.vogo.iot.vogobluetooth;

import android.Manifest;
import android.app.Activity;
import android.app.PendingIntent;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.location.Location;
import android.location.LocationManager;
import android.os.Build;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.os.Handler;
import android.provider.Settings;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.telephony.SmsManager;
import android.telephony.TelephonyManager;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.io.IOException;
import java.lang.reflect.Method;
import java.util.Date;
import java.util.Set;
import java.util.UUID;

import in.vogo.iot.vogobluetooth.vo.AppStatus;

import static android.R.attr.name;


public class MainActivity extends AppCompatActivity {
    private static final int MY_PERMISSIONS_REQUEST_SEND_SMS = 0;
    private static final int MY_PERMISSIONS_REQUEST_READ_STATE = 1;
    private static final UUID BTMODULEUUID = UUID.fromString("00001101-0000-1000-8000-00805F9B34FB");
    private final static int REQUEST_ENABLE_BT = 1;
    private final static int MESSAGE_READ = 2;
    private final static int CONNECTING_STATUS = 3;
    private final static int UNPAIRED_DEVICE = 4;
    // final String address = "BC:F5:AC:7B:65:0D";
    final String address = "12:B9:2E:BB:62:61";
    private final int REQUEST_FINE_LOCATION = 1234;
    TextView smsStatus, BTStatus, BTTimeRemaining, locationtv;
    Button unlockButton, sendButton;
    String phoneNo = "+917022018457";
    Util util;
    AppStatus appStatus;
    String TAG = "VOGO_BLUETOOTH";
    GPSTracker gpsTracker;
    //String phoneNo = "+918015339336";
    String message = "test sms";
    CountDownTimer retryTimer;
    FirebaseDatabase firebaseDatabase;
    DatabaseReference databaseReference;
    private LocationManager locationManager;
    private Location onlyOneLocation;
    private BluetoothAdapter mBTAdapter;
    private Set<BluetoothDevice> mPairedDevices;
    private Handler mHandler;
    private BluetoothSocket mBTSocket = null;
    private int btOtp = 1233;
    private int retryLimit = 10;
    private int count = 0;
    private boolean isConnected = false;
    private boolean retriable = true;

    int PERMISSION_ALL = 1;
    String[] PERMISSIONS = {
            android.Manifest.permission.ACCESS_FINE_LOCATION,
            android.Manifest.permission.SEND_SMS,
            android.Manifest.permission.READ_PHONE_STATE,
            android.Manifest.permission.BLUETOOTH,
            android.Manifest.permission.INTERNET
    };
    final BroadcastReceiver mPairingRequestReceiver = new BroadcastReceiver() {
        public void onReceive(Context context, Intent intent) {
            boolean isPinAccepted = false;
            this.abortBroadcast();
            String action = intent.getAction();
            if (action.equals(BluetoothDevice.ACTION_PAIRING_REQUEST)) {
                try {
                    Log.d("BT", "HAI");
                    BluetoothDevice device = intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE);
                    Log.d("BT", device.getName());
                    Log.d("BT", device.getAddress());
                    if (device.getBondState() == BluetoothDevice.BOND_BONDED) {
                        Log.d("BT", "Device is already paired");
                    }

                    int pin = intent.getIntExtra("android.bluetooth.device.extra.PAIRING_KEY", 1233);

                    byte[] pinBytes;
                    pinBytes = ("" + pin).getBytes("UTF-8");
                    if (Build.VERSION.SDK_INT > Build.VERSION_CODES.JELLY_BEAN_MR2) {
                        device.setPin(pinBytes);
                        isPinAccepted = true;

                        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.N) {
                            Log.d(TAG, "Lower version of android Detected");
                            try {
                                Log.d(TAG, "confirming pin.");
                                device.setPairingConfirmation(true);
//                        device.getClass().getMethod("setPairingConfirmation", boolean.class).invoke(device, true);
                                Log.d(TAG, "Success to setPairingConfirmation.");
                                isPinAccepted = true;
                                Log.d("BT", "Is pin accepted" + String.valueOf(isPinAccepted));
                            } catch (Exception e) {
                                Log.d("BT", "ERROR to setPairingConfirmation.");
                                // TODO Auto-generated catch block

                            }
                        }
                    } else {
                        isPinAccepted = pairDevice(device);
                    }
                    if (isPinAccepted) {
                        Log.d("BT", "ACcepted PIN");
                        BTStatus.setText("Connected to Device");
                        appStatus.setBluetoothConnection("Connected");
                        isConnected = true;
                        retriable = false;
                        retryTimer.cancel();
                        unlockButton.setEnabled(true);
                        mBTAdapter.cancelDiscovery();
                        appStatus.setBtPairSuccessTime(new Date().toString());
                        new CountDownTimer(10000, 1000) {
                            public void onTick(long millisUntilFinished) {
                                String st = BTTimeRemaining.getText().toString();
                                BTTimeRemaining.setText(String.valueOf(Integer.valueOf(st) - 1));
                            }

                            public void onFinish() {
                                mPairedDevices = mBTAdapter.getBondedDevices();
                                if (mBTAdapter.isEnabled()) {

                                    for (BluetoothDevice device : mPairedDevices) {
                                        Log.d("BT Paired devices size", String.valueOf(mPairedDevices.size()));
                                        Log.d("BT devices Details", String.valueOf(device.getBondState() == BluetoothDevice.BOND_BONDED));
                                        try {
                                            if (device.getAddress().equalsIgnoreCase(address)) {
                                                Log.d("BT", "Unparing DEVICE " + device.getAddress());

                                                Method m = device.getClass()
                                                        .getMethod("removeBond", (Class[]) null);
                                                m.invoke(device, (Object[]) null);
                                                BTStatus.setText("Unpaired from device");

                                            }
                                            Log.d("BT devices unpairing", String.valueOf(mBTAdapter.getBondedDevices().size()));
                                        } catch (Exception e) {
                                            Log.e(TAG, "Unparing Failed");
                                        }
                                    }
                                    util.showToast(getApplicationContext(), "unPaired");
                                }
                                unlockButton.setEnabled(true);
                                BTTimeRemaining.setText("10");

                            }
                        }.start();

                    } else {
                        BTStatus.setText("Connection failed, device not found");
                        appStatus.setBluetoothConnection("Connection failed");
                    }


                } catch (Exception e) {
                    Log.e(TAG, "Error occurs when trying to auto pair");
                    e.printStackTrace();
                }
            }
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        gpsTracker = new GPSTracker(this);
        util = new Util();
//        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
//            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, REQUEST_FINE_LOCATION);
//        }

        if(!hasPermissions(this, PERMISSIONS)){
            ActivityCompat.requestPermissions(this, PERMISSIONS, PERMISSION_ALL);
        }

        unlockButton = (Button) findViewById(R.id.btn_unlock);
        sendButton = (Button) findViewById(R.id.btn_send_data);
        BTTimeRemaining = (TextView) findViewById(R.id.bt_connection_time_value);
        BTStatus = (TextView) findViewById(R.id.bt_status_value);
        smsStatus = (TextView) findViewById(R.id.sms_status_value);
        locationtv = (TextView) findViewById(R.id.tv_location_value);

        mBTAdapter = BluetoothAdapter.getDefaultAdapter();
        firebaseDatabase = FirebaseDatabase.getInstance();
        databaseReference = firebaseDatabase.getReference("appdata");
        if (!mBTAdapter.isEnabled()) {
            Intent enableBtIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
            startActivityForResult(enableBtIntent, REQUEST_ENABLE_BT);
        }
        mHandler = new Handler() {
            public void handleMessage(android.os.Message msg) {

                if (msg.what == CONNECTING_STATUS) {

                    if (msg.arg1 == 1) {
                        BTStatus.setText("Connected to Device");
                        appStatus.setBluetoothConnection("Connected");
                        isConnected = true;
                    } else if (msg.arg1 == 2) {
                        BTStatus.setText("Already Connected to Device");
                        appStatus.setBluetoothConnection("already connected");
                        isConnected = true;
                        unlockButton.setEnabled(true);
                    } else {
                        if (!isConnected) {
                            BTStatus.setText("Failed to connect Device");
                            appStatus.setBluetoothConnection("failed to connect");
                            util.showToast(getApplicationContext(), "Failed to Connect");

                            if (!mBTAdapter.isEnabled()) {
                                Intent enableBtIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
                                startActivityForResult(enableBtIntent, REQUEST_ENABLE_BT);
                            } else {
                                if (retriable) {
                                    BTStatus.setText("Retrying..");
                                    connectToDevice();
                                }
                            }
                        }
                    }
                }
                if (msg.what == UNPAIRED_DEVICE) {
                    if (msg.arg1 == 1) {
                        BTStatus.setText("Device Unpaired: " + (String) (msg.obj));
                        unlockButton.setEnabled(true);
                    } else {
                        BTStatus.setText("unpair Failed");
                        unlockButton.setEnabled(true);
                    }
                }
            }
        };
        unlockButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                appStatus = new AppStatus();
                if (gpsTracker.getIsGPSTrackingEnabled()) {
                    isConnected = false;
                    retriable = true;
                    count = 0;
                    unlockButton.setEnabled(false);
                    String unlockLocation = gpsTracker.getLatitude() + "," + gpsTracker.getLongitude();
                    Log.d(TAG, unlockLocation);
                    appStatus.setUnlockLocation(unlockLocation);
                    locationtv.setText(unlockLocation);
                    util.showToast(getBaseContext(), "Unlocking device");
                    Date date = new Date();
                    appStatus.setUnlockStartTime(date.toString());
                    sendSMSMessage();
                    connectToDevice();
                    retryTimer = new CountDownTimer(1000 * 60 * 2, 1000) {

                        @Override
                        public void onTick(long millisUntilFinished) {
                            Log.v("Retry:", String.valueOf(millisUntilFinished));
                            if (millisUntilFinished % 5000 == 0) {
                                util.showToast(getApplicationContext(), "Retrying to Connect");
                            }
                        }

                        @Override
                        public void onFinish() {
                            retriable = false;
                            unlockButton.setEnabled(true);
                        }
                    };
                    retryTimer.start();

                } else {
                    gpsTracker.showSettingsAlert();
                }
            }
        });

        sendButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                sendData();
            }
        });

    }

    @Override
    protected void onPause() {
        BTStatus.setText("na");
        try {


            if (mPairingRequestReceiver != null) {
                unregisterReceiver(mPairingRequestReceiver);
            }
            if (mBTAdapter.isDiscovering()) {
                mBTAdapter.cancelDiscovery();
                BTStatus.setText("Discovery Stopped");
            }
            if (mBTSocket != null) {
                mBTSocket.close();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        super.onPause();
    }

    @Override
    protected void onResume() {
        super.onResume();
        gpsTracker.getLocation();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent Data) {
        if (requestCode == REQUEST_ENABLE_BT) {
            if (resultCode == RESULT_OK) {
                util.showToast(getApplicationContext(), "Bluetooth turned on");
                BTStatus.setText("Enabled");
            } else
                BTStatus.setText("Disabled");
        }
    }

    protected void sendSMSMessage() {

        if (ContextCompat.checkSelfPermission(this,
                Manifest.permission.SEND_SMS)
                != PackageManager.PERMISSION_GRANTED) {
            if (ActivityCompat.shouldShowRequestPermissionRationale(this,
                    Manifest.permission.SEND_SMS)) {
            } else {
                ActivityCompat.requestPermissions(this,
                        new String[]{Manifest.permission.SEND_SMS},
                        MY_PERMISSIONS_REQUEST_SEND_SMS);
            }
        } else {
            smsSend();
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String permissions[], int[] grantResults) {
        switch (requestCode) {
            case MY_PERMISSIONS_REQUEST_SEND_SMS: {
                if (grantResults.length > 0
                        && grantResults[0] == PackageManager.PERMISSION_GRANTED) {

                    smsSend();

                } else {
                    Toast.makeText(getApplicationContext(),
                            "SMS faild, please try again.", Toast.LENGTH_LONG).show();
                    if (appStatus != null) {
                        appStatus.setSmsStatus("Failed");
                    }
                    return;
                }
            }
        }

    }

    private void smsSend() {
        String SENT = "SMS_SENT";
        String DELIVERED = "SMS_DELIVERED";

        PendingIntent sentPI = PendingIntent.getBroadcast(this, 0,
                new Intent(SENT), 0);

        PendingIntent deliveredPI = PendingIntent.getBroadcast(this, 0,
                new Intent(DELIVERED), 0);

        //---when the SMS has been sent---
        registerReceiver(new BroadcastReceiver() {
            @Override
            public void onReceive(Context arg0, Intent arg1) {
                unlockButton.setEnabled(true);
                switch (getResultCode()) {
                    case Activity.RESULT_OK:
                        Log.d("SMS", "SENT");
                        if (appStatus != null) {
                            appStatus.setSmsStatus("SMS sent");
                            smsStatus.setText("SMS sent");
                        }
                        break;
                    case SmsManager.RESULT_ERROR_GENERIC_FAILURE:
                        Log.d("SMS", "Generic failure");
                        if (appStatus != null) {
                            appStatus.setSmsStatus("Generic failure");
                            smsStatus.setText("Generic failure");
                        }
                        break;
                    case SmsManager.RESULT_ERROR_NO_SERVICE:
                        Log.d("SMS", "No service");

                        if (appStatus != null) {
                            appStatus.setSmsStatus("No service");
                            smsStatus.setText("No service");
                        }
                        break;
                    case SmsManager.RESULT_ERROR_NULL_PDU:
                        Log.d("SMS", "Null PDU");

                        if (appStatus != null) {
                            appStatus.setSmsStatus("Null PDU");
                            smsStatus.setText("Null PDU");
                        }
                        break;
                    case SmsManager.RESULT_ERROR_RADIO_OFF:
                        Log.d("SMS", "Radio off");

                        if (appStatus != null) {
                            appStatus.setSmsStatus("Radio off");
                            smsStatus.setText("Radio off");
                        }
                        break;
                }
            }
        }, new IntentFilter(SENT));

        //---when the SMS has been delivered---
        registerReceiver(new BroadcastReceiver() {
            @Override
            public void onReceive(Context arg0, Intent arg1) {
                unlockButton.setEnabled(true);
                switch (getResultCode()) {
                    case Activity.RESULT_OK:
                        String receivedTime = new Date().toString();
                        appStatus.setSmsReceivedTime(receivedTime);
                        Toast.makeText(getApplicationContext(), "SMS delivered on " + receivedTime,
                                Toast.LENGTH_SHORT).show();
                        Log.d("SMS", "SMS delivered on " + receivedTime);
                        if (appStatus != null) {
                            appStatus.setSmsStatus("SMS delivered");
                        }
                        smsStatus.setText("SMS delivered");
                        break;
                    case Activity.RESULT_CANCELED:
                        Toast.makeText(getBaseContext(), "SMS not delivered",
                                Toast.LENGTH_SHORT).show();
                        Log.d("SMS", "SMS not delivered");

                        if (appStatus != null) {
                            appStatus.setSmsStatus("SMS not delivered");
                        }
                        break;
                }
            }
        }, new IntentFilter(DELIVERED));


        SmsManager smsManager = SmsManager.getDefault();
        smsManager.sendTextMessage(phoneNo, null, message, sentPI, deliveredPI);
    }

    void connectToDevice() {
        BTStatus.setText("Connecting");
        IntentFilter filter = new IntentFilter(BluetoothDevice.ACTION_PAIRING_REQUEST);
        registerReceiver(mPairingRequestReceiver, filter);

        new Thread() {
            public void run() {
                boolean fail = false;
                BluetoothDevice device = mBTAdapter.getRemoteDevice(address);
                boolean bonded = device.getBondState() == BluetoothDevice.BOND_BONDED;
                Log.d("BT", device.getName() + "is paired: " + String.valueOf(bonded));

                if (bonded) {
                    mHandler.obtainMessage(CONNECTING_STATUS, 2, -1, name)
                            .sendToTarget();

                }

                try {
                    mBTSocket = createBluetoothSocket(device);
                    if (mBTSocket != null) {
                        Log.d("BT", "SocketConnection Success " + device.getName());
                    }
                } catch (IOException e) {
                    fail = true;
                    Log.d("BT", "SocketConnection Failed " + device.getName());
                }
                Log.d("BT", String.valueOf(mBTAdapter.isDiscovering()));
                // Establish the Bluetooth socket connection.
                try {
                    mBTSocket.connect();
                } catch (IOException e) {
                    try {
                        fail = true;
                        mBTSocket.close();
                        mHandler.obtainMessage(CONNECTING_STATUS, -1, -1)
                                .sendToTarget();
                    } catch (IOException e2) {
                        //insert code to deal with this
                        Toast.makeText(getBaseContext(), "Socket creation failed", Toast.LENGTH_SHORT).show();
                    }
                }
                if (fail == false) {
                    mHandler.obtainMessage(CONNECTING_STATUS, 1, -1, name)
                            .sendToTarget();
                }
            }
        }.start();
    }


    private BluetoothSocket createBluetoothSocket(BluetoothDevice device) throws IOException {
        try {
            final Method m = device.getClass().getMethod("createInsecureRfcommSocketToServiceRecord", UUID.class);
            return (BluetoothSocket) m.invoke(device, BTMODULEUUID);
        } catch (Exception e) {
            Log.e(TAG, "Could not create Insecure RFComm Connection", e);
        }
        return device.createRfcommSocketToServiceRecord(BTMODULEUUID);
    }

    private boolean pairDevice(BluetoothDevice device) {
        try {
            Method method = device.getClass().getMethod("createBond", (Class[]) null);
            method.invoke(device, (Object[]) null);
            return true;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return false;
    }

    private void unpairDevice(BluetoothDevice device) {
        try {
            Method method = device.getClass().getMethod("removeBond", (Class[]) null);
            method.invoke(device, (Object[]) null);

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    void sendData() {
        Log.d("Collected Data", appStatus.toString());
        String uniqueCode = Settings.Secure.getString(this.getContentResolver(),
                Settings.Secure.ANDROID_ID);

        if (ContextCompat.checkSelfPermission(this,
                Manifest.permission.READ_PHONE_STATE)
                != PackageManager.PERMISSION_GRANTED) {
            if (ActivityCompat.shouldShowRequestPermissionRationale(this,
                    Manifest.permission.READ_PHONE_STATE)) {
            } else {
                ActivityCompat.requestPermissions(this,
                        new String[]{Manifest.permission.READ_PHONE_STATE},
                        MY_PERMISSIONS_REQUEST_READ_STATE);
            }
        } else {
            TelephonyManager telephonyManager = (TelephonyManager)getSystemService(Context.TELEPHONY_SERVICE);
            uniqueCode = telephonyManager.getDeviceId();
        }
        appStatus.setDevice(uniqueCode);
        databaseReference.push().setValue(appStatus);  // pushes the data into firebase with random key
    }

    public static boolean hasPermissions(Context context, String... permissions) {
        if (context != null && permissions != null) {
            for (String permission : permissions) {
                if (ActivityCompat.checkSelfPermission(context, permission) != PackageManager.PERMISSION_GRANTED) {
                    return false;
                }
            }
        }
        return true;
    }
}
