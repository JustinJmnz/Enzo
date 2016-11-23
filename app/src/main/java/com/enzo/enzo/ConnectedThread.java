package com.enzo.enzo;

import android.bluetooth.BluetoothSocket;
import android.os.Handler;
import android.util.Log;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

/**
 * Created by Justin on 11/23/2016.
 */

public class ConnectedThread extends Thread {
    private final BluetoothSocket mmSocket;
    private final InputStream mmInStream;
    private final OutputStream mmOutStream;
    private final Handler bluetoothHandler;

    private String messageFromEnzo = null;

    public ConnectedThread(BluetoothSocket socket, Handler handler) {
        mmSocket = socket;
        InputStream tmpIn = null;
        OutputStream tmpOut = null;
        bluetoothHandler = handler;

        // Get the input and output streams, using temp objects because
        // member streams are final
        try {
            tmpIn = mmSocket.getInputStream();
            tmpOut = mmSocket.getOutputStream();
            Log.i("ConnectedThread", "Successfully got Input and Output streams");
        } catch (IOException e) {
            Log.i("ConnectedThread", "Error in ConnectedThread(): " + e.getMessage());
        }

        mmInStream = tmpIn;
        mmOutStream = tmpOut;
    }

    public void run() {
        byte[] buffer = new byte[1024];  // buffer store for the stream
        int bytes = 0; // bytes returned from read()

        // Keep listening to the InputStream until an exception occurs
        while (true) {
            try {
                // Read from the InputStream
                bytes = mmInStream.read(buffer);
                messageFromEnzo = new String(buffer, 0 , bytes);
                // Send the obtained bytes to the main activity handler, 1 is for case statement in handler
                bluetoothHandler.obtainMessage(1, messageFromEnzo).sendToTarget();
                Log.i("ConnectedThread", "Read from InputStream: " + messageFromEnzo);
            } catch (IOException e) {
                break;
            }
        }
    }

    /* Call this from the main activity to send data to the remote device */
    public void write(byte[] bytes) {
        try {
            mmOutStream.write(bytes);
        } catch (IOException e) { }
    }

    /* Call this from the main activity to shutdown the connection */
    public void cancel() {
        try {
            mmSocket.close();
        } catch (IOException e) { }
    }

    public String getMessageFromEnzo() {
        return this.messageFromEnzo;
    }
}