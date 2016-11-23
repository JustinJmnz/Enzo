package com.enzo.enzo;

import android.Manifest;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.BroadcastReceiver;
import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.net.Uri;
import android.os.Build;
import android.os.Handler;
import android.os.Message;
import android.provider.ContactsContract;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.telephony.SmsManager;
import android.util.Log;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.Set;
import java.util.concurrent.ExecutionException;

public class MainActivity extends AppCompatActivity {

    // Permission request codes, defined by me
    private static final int MY_PERMISSIONS_REQUEST_READ_CONTACTS = 100;
    private static final int MY_PERMISSIONS_REQUEST_SEND_SMS = 101;
    private static final int MY_PERMISSIONS_REQUEST_CALL_PHONE = 102;
    private static final int MY_PERMISSIONS_REQUEST_SYSTEM_ALERT_WINDOW = 103;
    private static final int MY_PERMISSIONS_REQUEST_BLUETOOTH = 104;
    private static final int MY_PERMISSIONS_REQUEST_READ_PHONE_STATE = 105;
    // Request for Bluetooth
    private static final int REQUEST_ENABLE_BT = 1;

    private ConnectThread connectThread;

    // Projections (Columns) to get from Contact Info Query
    private String[] contactProjection = {
            ContactsContract.CommonDataKinds.Phone.NUMBER,
            ContactsContract.CommonDataKinds.Phone.DISPLAY_NAME
    };

    /**
     * @param contactName Name of contact to find
     * @return The UserContact object containing the number associated with the name
     */
    private UserContact getContact(String contactName) {
        // Check if permission is granted
        UserContact contactFound = new UserContact();
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M && checkSelfPermission(Manifest.permission.READ_CONTACTS) != PackageManager.PERMISSION_GRANTED) {
            requestPermissions(new String[]{Manifest.permission.READ_CONTACTS}, MY_PERMISSIONS_REQUEST_READ_CONTACTS);
        }
        ContentResolver resolver = getContentResolver();
        // SELECT (contactProjection) FROM PHONEDB WHERE DISPLAY_NAME = (UserContact.getName()) ORDER BY (null)
        Cursor contact = resolver.query(ContactsContract.CommonDataKinds.Phone.CONTENT_URI, contactProjection,
                ContactsContract.CommonDataKinds.Phone.DISPLAY_NAME + "= (?)", new String[] { contactName }, null);
        // Get the column number of the DISPLAY_NAME and NUMBER from PHONEDB
        int indexName = contact.getColumnIndex(ContactsContract.CommonDataKinds.Phone.DISPLAY_NAME);
        int indexNumber = contact.getColumnIndex(ContactsContract.CommonDataKinds.Phone.NUMBER);
        // If a contact with criteria is found
        if(contact.moveToFirst()) {
            do { // Get name and number
                String name   = contact.getString(indexName);
                String number = contact.getString(indexNumber);
                contactFound.setName(name);
                contactFound.setNumber(number);
                Log.i("MY INFO", name);
                Log.i("MY INFO", number);
            } while (contact.moveToNext());
            contact.close();
            return contactFound;
        } else { // No contact found with criteria
            // TODO: Send message to enzo saying error
            Log.e("MY INFO", "Nobody named " + contactFound.getName() + " in contacts");
            contact.close();
            return null;
        }
    }

    /**
     * @param contact A UserContact Object containing a phone number
     * @param message The message to send to UserContact
     */
    private boolean textContact(UserContact contact, String message) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M && checkSelfPermission(Manifest.permission.SEND_SMS) != PackageManager.PERMISSION_GRANTED) {
            requestPermissions(new String[]{ Manifest.permission.SEND_SMS }, MY_PERMISSIONS_REQUEST_SEND_SMS);
        }
        try {
            SmsManager sms = SmsManager.getDefault();
            sms.sendTextMessage(contact.getNumber(), null, message, null, null);
            return true;
        } catch(Exception e) {
            Log.i("MY INFO", e.getMessage() + " " + contact.getNumber());
            return false;
        }
    }
    private boolean callContact(UserContact contact){
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M && checkSelfPermission(Manifest.permission.CALL_PHONE) != PackageManager.PERMISSION_GRANTED
                && checkSelfPermission(Manifest.permission.SYSTEM_ALERT_WINDOW) != PackageManager.PERMISSION_GRANTED) {
            requestPermissions(new String[] { Manifest.permission.CALL_PHONE }, MY_PERMISSIONS_REQUEST_CALL_PHONE);
            //requestPermissions(new String[] { Manifest.permission.SYSTEM_ALERT_WINDOW }, MY_PERMISSIONS_REQUEST_SCREEN_OVERLAY);
        }
        try {
            Intent callIntent = new Intent(Intent.ACTION_CALL, Uri.parse("tel:" + contact.getNumber()));
            startActivity(callIntent);
            return true;
        } catch(Exception e) {
            Log.i("MY INFO", e.getMessage() + " " + contact.getNumber());
            return false;
        }

    }

    private final BroadcastReceiver mReceiver = new BroadcastReceiver() {
        String list = "";
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            // When discovery finds a device
            if (BluetoothDevice.ACTION_FOUND.equals(action)) {
                // Get the BluetoothDevice object from the Intent
                BluetoothDevice device = intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE);
                // Add the name and address to an array adapter to show in a ListView
                list += device.getName() + "\n" + device.getAddress();
            }
        }
    };
    public Handler bluetoothHandler = new Handler(){

        @Override
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case 1: // Bluetooth Received from enzo, 1 is sent to handler from ConnectedThread
                    String text = (String) msg.obj;
                    String [] commands = text.split(" ");
                    UserContact userContact = getContact(commands[1]);
                    String action = commands[0].toLowerCase();

                    switch (action) {
                        case "call":
                            connectThread.writeToEnzo("Calling " + userContact.getName());
                            callContact(userContact);
                            break;
                        case "text":
                            StringBuilder message = new StringBuilder();
                            for (int i = 2; i < commands.length; i++) {
                                message.append(commands[i]).append(" ");
                            }
                            connectThread.writeToEnzo("Texting " + userContact.getName() + " with a message of, " + message.toString());
                            textContact(userContact, message.toString());
                            break;
                    }
                    Log.i("MainActivity", "Message received: " + text);
                    break;
            }
        }
  };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        BluetoothAdapter mBluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
        if (mBluetoothAdapter == null) {
            // Device does not support Bluetooth
        }
        if (!mBluetoothAdapter.isEnabled()) {
            Intent enableBtIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
            startActivityForResult(enableBtIntent, REQUEST_ENABLE_BT);
        }
        BluetoothDevice raspberrypi = null;
        Set<BluetoothDevice> pairedDevices = mBluetoothAdapter.getBondedDevices();
        // If there are paired devices
        if (pairedDevices.size() > 0) {
            for (BluetoothDevice device : pairedDevices) {
                if(device.getAddress().equals("B8:27:EB:A8:D8:2D")) { // Address of raspberrypi
                    Log.i("MY INFO", "Found device named: " + device.getName() + "\nWith Address: " + device.getAddress());
                    raspberrypi = device;
                    break;
                }
            }
        }
        // Thread that connects to the raspberry pi
        connectThread = new ConnectThread(raspberrypi, mBluetoothAdapter, bluetoothHandler);
        connectThread.start();
        Log.i("MY INFO", "Created ConnectedThread");
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        UserContact test = null;
        switch(requestCode){
            case MY_PERMISSIONS_REQUEST_READ_CONTACTS:
                if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    test = getContact("Enea");
                } else {
                    Toast.makeText(this, "Until you grant the permission, we cannot display the names", Toast.LENGTH_SHORT).show();
                }
                break;
            case MY_PERMISSIONS_REQUEST_SEND_SMS:
                if(grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    boolean sent = textContact(test, "Does this work"); // Possible null in test
                } else {
                    Toast.makeText(this, "Until you grant the permission, we cannot display text", Toast.LENGTH_SHORT).show();
                }
                break;
            case MY_PERMISSIONS_REQUEST_CALL_PHONE:
                if(grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    boolean called = callContact(test); // Possible null in test
                } else {
                    Log.i("MY INFO", "Until you grant the permission, we cannot call a contact");
                }
                break;
            case MY_PERMISSIONS_REQUEST_SYSTEM_ALERT_WINDOW:
                if(grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    Log.i("MY INFO", "Accepted Screen Overlay");
                } else {
                    Log.i("MY INFO", "Until you grant the permission, we cannot display a screen overlay");
                }
                break;
            case MY_PERMISSIONS_REQUEST_BLUETOOTH:
                if(grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    Log.i("MY INFO", "Accepted Bluetooth");
                } else {
                    Log.i("MY INFO", "Until you grant the permission, we cannot use bluetooth");
                }
                break;
            case MY_PERMISSIONS_REQUEST_READ_PHONE_STATE:
                if(grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    Log.i("MY INFO", "Accepted Read Phone State");
                } else {
                    Log.i("MY INFO", "Until you grant the permission, we cannot Read Phone State");
                }
                break;
        }
    }
}
