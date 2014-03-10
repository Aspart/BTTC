package com.teslacoil.bttc;

import android.app.Activity;
import android.app.AlertDialog;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.Set;
import java.util.UUID;

/**
 * Created by Aspart on 08.03.14.
 */
public class BluetoothConnector {
    private BluetoothAdapter mAdapter;  // this device adapter
    private BluetoothDevice mDevice;    // device to recieve data
    private BluetoothSocket mSocket;    // and its interface
    private OutputStream mOutputStream;
    private InputStream mInputStream;

    private ArrayList<String> mArrayAdapter;

    public BluetoothConnector(BluetoothAdapter bluetoothAdapter) {
        if(bluetoothAdapter == null)
            return;
        mAdapter = bluetoothAdapter;
    }

    boolean isBTEnabled() {
        return mAdapter.isEnabled();
    }

    public ArrayList<String> getPaired() throws RuntimeException
    {
        mArrayAdapter = new ArrayList<String>();
        if(mAdapter == null)
            return mArrayAdapter;
        Set<BluetoothDevice> pairedDevices = mAdapter.getBondedDevices();
        // If there are paired devices
        if (pairedDevices.size() > 0) {
            // Loop through paired devices
            for (BluetoothDevice device : pairedDevices) {
                if("linvor".equals(device.getName())) {
                    mDevice = device;
                }
            }
        }
        return mArrayAdapter;
    }

    void connect() throws IOException
    {
        UUID uuid = UUID.fromString("00001101-0000-1000-8000-00805f9b34fb");
        if (mDevice == null)
            return;
        mSocket = mDevice.createRfcommSocketToServiceRecord(uuid);
        try {
            mSocket.connect();
        }
        catch (IOException connectException) {
            // Unable to connect; close the socket and get out
            try {
                mSocket.close();
            }
            catch (IOException closeException) {

            }
        }
        mOutputStream = mSocket.getOutputStream();
    }

    void close() throws IOException
    {
        if(mOutputStream != null)
            mOutputStream.close();
        if(mSocket != null)
            mSocket.close();
    }

    void send(byte[] msg) throws IOException, InterruptedException {
        if(mOutputStream != null) {
            int c = 1;
            while(c<10) {
//                mOutputStream.write(msg);
//                Thread.sleep(1, 0);
//                mOutputStream.write(msg);
//                Thread.sleep(2, 0);
//                mOutputStream.write(msg);
//                Thread.sleep(5, 0);
                mOutputStream.write(msg);
//                mOutputStream.write(msg);
//                Thread.sleep(20, 0);
//                mOutputStream.write(msg);
//                Thread.sleep(50, 0);
//                mOutputStream.write(msg);
//                Thread.sleep(100, 0);
//                mOutputStream.write(msg);
//                Thread.sleep(1000);
                c++;
            }
        }
    }


}
