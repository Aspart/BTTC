package com.teslacoil.bttc;

import android.app.Activity;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;
import android.util.Log;

import java.io.IOException;
import java.io.OutputStream;
import java.util.Set;
import java.util.UUID;

public class BluetoothConnector {
    final private static UUID DEVICE_UUID = UUID.fromString("00001101-0000-1000-8000-00805f9b34fb");

    private BluetoothAdapter mAdapter = null;  // this device adapter
    private BluetoothDevice mDevice = null;    // device to recieve data
    private BluetoothSocket mSocket = null;    // and its interface
    private OutputStream mOutputStream = null; // and data stream

    public BluetoothConnector(BluetoothAdapter bluetoothAdapter) {
        if(bluetoothAdapter == null)
            return;
        mAdapter = bluetoothAdapter;
    }

    boolean isEnabled() {
        if(mAdapter != null)
            return mAdapter.isEnabled();
        return false;
    }

    boolean isConnected() {
        return mSocket == null;
    }

    public Set<BluetoothDevice> getPaired()
    {
        if(mAdapter == null)
            return null;
        return mAdapter.getBondedDevices();
    }

    public boolean connectDevice(BluetoothDevice device) throws IOException {
        // If there are paired devices
        if(mAdapter==null)
            return false;
        Set<BluetoothDevice> devices = mAdapter.getBondedDevices();
        for (BluetoothDevice d : devices) {
            if(d.equals(device)) {
                mDevice = device;
                break;
            }
        }

        if (mDevice == null)
            return false;

        mSocket = mDevice.createRfcommSocketToServiceRecord(DEVICE_UUID);

        try {
            if(mAdapter.isDiscovering())
                mAdapter.cancelDiscovery();
            mSocket.connect();
            mOutputStream = mSocket.getOutputStream();
            return true;
        }
        catch (IOException connectException) {
            // Unable to connect; close the socket and get out
            try {
                mSocket.close();
                mSocket = null;
                return false;
            }
            catch (IOException closeException) {
            }
        }
        return false;
    }

    void close() throws IOException
    {
        if(mOutputStream != null)
            mOutputStream.close();
        mOutputStream = null;
        if(mSocket != null)
            mSocket.close();
        mSocket = null;
    }

    void send(byte[] msg) {
        if(mOutputStream != null) {
            for(byte b : msg) {
                try {
                    mOutputStream.write(b);
                } catch (IOException e) {
                    Log.e("BluetoothConnector", e.getMessage());
                }
                try {
                    Thread.sleep(0, 20000);
                } catch (InterruptedException e) {
                    Log.e("BluetoothConnector", e.getMessage());
                }
            }
        }
    }


}
