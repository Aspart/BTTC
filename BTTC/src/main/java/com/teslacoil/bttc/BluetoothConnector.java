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
import android.os.AsyncTask;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.Set;
import java.util.UUID;

public class BluetoothConnector {

        // Send msg for time
        private class SendMsgForTimeTask extends AsyncTask<String, Integer, String> {
            int runningtime;
            byte[] msg;

            SendMsgForTimeTask(int runningtime, byte[] msg) {
                this.runningtime = runningtime;
                this.msg = msg;
            }

            @Override
            protected String doInBackground(String... params) {
                int loop = 1;
                long StartTime = System.currentTimeMillis() / 1000;
                for (int i = 0; i < loop; ++i) {
                    if(!isCancelled()) {
                        try {
                            if(mOutputStream != null) {
                                mOutputStream.write(msg);
                            }
                        }
                        catch (Exception e) {

                        }
                        loop++;
                        if (runningtime < ((System.currentTimeMillis() / 1000) - StartTime)) {
                            loop = 0;
                        }
                    }
                }
                return "";
            }
        }

    // Send single msg
    private class SendMsgTask extends AsyncTask<String, Integer, String> {
        byte[] msg;

        SendMsgTask(byte[] msg) {
            this.msg = msg;
        }

        @Override
        protected String doInBackground(String... params) {
            try {
                if(mOutputStream != null) {
                    mOutputStream.write(msg);
                }
            }
            catch (Exception e) {

            }
            return "";
        }
    }

    private BluetoothAdapter mAdapter;  // this device adapter
    private BluetoothDevice mDevice;    // device to recieve data
    private BluetoothSocket mSocket;    // and its interface
    private OutputStream mOutputStream;
    private SendMsgForTimeTask mCurrentTimeTask;

    public BluetoothConnector(BluetoothAdapter bluetoothAdapter) {
        if(bluetoothAdapter == null)
            return;
        mAdapter = bluetoothAdapter;
    }

    boolean isBTEnabled() {
        return mAdapter.isEnabled();
    }

    boolean isActive() {
        if(mSocket!=null)
            return mSocket.isConnected();
        else
            return false;
    }

    public ArrayList<String> getPaired() throws RuntimeException
    {
        ArrayList<String> mArrayAdapter = new ArrayList<String>();
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

    void cancelTask() {
        if(mCurrentTimeTask != null)
            mCurrentTimeTask.cancel(true);
    }

    void connect() throws IOException
    {
        UUID uuid = UUID.fromString("00001101-0000-1000-8000-00805f9b34fb");
        if (mDevice == null)
            return;
        mSocket = mDevice.createRfcommSocketToServiceRecord(uuid);
        try {
            //mAdapter.cancelDiscovery();
            mSocket.connect();
            mOutputStream = mSocket.getOutputStream();
        }
        catch (IOException connectException) {
            // Unable to connect; close the socket and get out
            try {
                mSocket.close();
            }
            catch (IOException closeException) {
            }
        }
    }

    void close() throws IOException
    {
        if(mOutputStream != null)
            mOutputStream.close();
        if(mSocket != null)
            mSocket.close();
    }

    void send(int time, byte[] msg) throws IOException, InterruptedException {
        if(time==0)
            send(msg);
        else {
            mCurrentTimeTask = new SendMsgForTimeTask(time, msg);
            mCurrentTimeTask.execute();
        }
    }
    void send(byte[] msg) throws IOException, InterruptedException {
        //new SendMsgTask(msg).execute();
        mOutputStream.write(msg);
    }


}
