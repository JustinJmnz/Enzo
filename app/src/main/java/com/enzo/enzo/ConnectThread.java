package com.enzo.enzo;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;
import android.os.ParcelUuid;
import android.util.Log;

import java.io.IOException;
import java.util.UUID;

/**
 * Created by Justin on 11/21/2016.
 */

class ConnectThread extends Thread {
    private final BluetoothSocket mmSocket;
    private final BluetoothDevice mmDevice;
    private final BluetoothAdapter mBluetoothAdapter;

    public ConnectThread(BluetoothDevice device, BluetoothAdapter adapter) {
        // Use a temporary object that is later assigned to mmSocket,
        // because mmSocket is final
        BluetoothSocket tmp = null;
        mmDevice = device;
        mBluetoothAdapter = adapter;
        // Get a BluetoothSocket to connect with the given BluetoothDevice
        try {
            // MY_UUID is the app's UUID string, also used by the server code
            // UUID used in bluetooth() 94f39d29-7d6d-437d-973b-fba39e49d4ee
            // Default UUID? 00001101-0000-1000-8000-00805F9B34FB
            tmp = device.createRfcommSocketToServiceRecord(UUID.fromString("94f39d29-7d6d-437d-973b-fba39e49d4ee"));
            Log.i("MY INFO", "Created RFCOMM Socket");
        } catch (IOException e) {
            Log.i("MY INFO", "Error in ConnectThread(): " + e.getMessage());
        }
        mmSocket = tmp;
    }

    public void run() {
        // Cancel discovery because it will slow down the connection
        mBluetoothAdapter.cancelDiscovery();

        try {
            // Connect the device through the socket. This will block
            // until it succeeds or throws an exception
            Log.i("MY INFO", "Waiting for connection to be accepted");
            mmSocket.connect();
            Log.i("MY INFO", "Device Connected!!");
            manageSocketConnection();
        } catch (IOException connectException) {
            // Unable to connect; close the socket and get out
            Log.i("MY INFO", "Connection Exception in run() in ConnectThread(): " + connectException.getMessage());
            try {
                mmSocket.close();
            } catch (IOException closeException) { }
            return;
        }

        // Do work to manage the connection (in a separate thread)
        //manageConnectedSocket(mmSocket);
    }

    private void manageSocketConnection() {
        ConnectedThread connectionThread = new ConnectedThread(mmSocket);
        Log.i("MY INFO", "Created ConnectionThread");
        connectionThread.start();
        Log.i("MY INFO", "Started ConnectionThread");
        connectionThread.write("I got your message".getBytes());
    }

    /** Will cancel an in-progress connection, and close the socket */
    public void cancel() {
        try {
            mmSocket.close();
        } catch (IOException e) { }
    }

    public BluetoothSocket getSocket() {
        return this.mmSocket;
    }
}