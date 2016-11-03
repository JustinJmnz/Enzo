package com.enzo.enzo;

import android.Manifest;
import android.content.ContentResolver;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.os.Build;
import android.provider.ContactsContract;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.widget.Toast;

import java.util.ArrayList;

public class MainActivity extends AppCompatActivity {

    // Request code for READ_CONTACTS, any number > 0
    private static final int PERMISSIONS_REQUEST_READ_CONTACTS = 100;
    // Projections (Columns) to get from Contact Info Query
    private String[] contactProjection = {
            ContactsContract.CommonDataKinds.Phone.NUMBER,
            ContactsContract.CommonDataKinds.Phone.DISPLAY_NAME
    };
    // The Name to seach for, TODO: Replace this with data read from bluetooth from enzo
    private String[]  searchName = {"Enea"};
    private void getContacts() {
        /*
            ABOUT: Gets and prints all Contact information on user phone.
            OUTPUT: TODO:
         */
        // Check if permission is granted
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M && checkSelfPermission(Manifest.permission.READ_CONTACTS) != PackageManager.PERMISSION_GRANTED) {
            requestPermissions(new String[]{Manifest.permission.READ_CONTACTS}, PERMISSIONS_REQUEST_READ_CONTACTS);
        }
        ContentResolver resolver = getContentResolver();
        // SELECT (contactProjection) FROM PHONEDB WHERE DISPLAY_NAME = (searchName) ORDER BY (null)
        Cursor contact = resolver.query(ContactsContract.CommonDataKinds.Phone.CONTENT_URI, contactProjection,
                ContactsContract.CommonDataKinds.Phone.DISPLAY_NAME + "= (?)", searchName, null);
        // Get the column number of the DISPLAY_NAME and NUMBER from PHONEDB
        int indexName = contact.getColumnIndex(ContactsContract.CommonDataKinds.Phone.DISPLAY_NAME);
        int indexNumber = contact.getColumnIndex(ContactsContract.CommonDataKinds.Phone.NUMBER);
        // If a contact with criteria is found
        if(contact.moveToFirst()) {
            do { // Get name and number
                String name   = contact.getString(indexName);
                String number = contact.getString(indexNumber);
                Log.i("MY INFO", name);
                Log.i("MY INFO", number);
            } while (contact.moveToNext());
        } else { // No contact found with criteria
            // TODO: Send message to enzo saying error
            Log.e("MY INFO", "Nobody named " + searchName[0] + " in contacts");
        }
        contact.close();
    }
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        getContacts();
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions,
                                           int[] grantResults) {
        if (requestCode == PERMISSIONS_REQUEST_READ_CONTACTS) {
            if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                // Permission is granted
                getContacts();
            } else {
                Toast.makeText(this, "Until you grant the permission, we canot display the names", Toast.LENGTH_SHORT).show();
            }
        }
    }


}
