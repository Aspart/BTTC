package com.teslacoil.bttc;

import android.bluetooth.BluetoothAdapter;

import java.io.IOException;

public class Interruptor {
    public final static int MIN_VOLUME = 0;
    public final static int MAX_VOLUME = 255;
    private final static int MIN_PERIOD =500;
    private final static int MAX_PERIOD = 62500;
    public final static int MIN_TIME = 50;
    public final static int MAX_TIME = 1500;
    public final static int MIN_FREQUENCY = 15;
    public final static int MAX_FREQUENCY = 2000;
    private int volume = 128; // 1byte
    private int period = 2560; // 2-3byte
    private int time = 100; // delay time
    private boolean active = false;

    BluetoothConnector btc = null;
    private byte[] pack = new byte[7];

    public Interruptor() {
        pack[0] = (byte)0xFF;
        pack[6] = (byte)0xFF;
    }

    public void setActive(boolean status) {active = status;}

    public boolean isActive() {return active;}

    // use only this to set volume
    protected void setVolume(int vol) {
        if((double)period/vol < 10)
            volume = (int)Math.ceil((double)period/10);
        else if(vol > MAX_VOLUME)
            volume = MAX_VOLUME;
        else if (vol < MIN_VOLUME)
            volume = MIN_VOLUME;
        else
            volume = vol;
        if(isActive()) {
            try {
                send();
            }
            catch (Exception e) {

            }
        }
    }

    public void setBluetoothConnector(BluetoothConnector c) {
        btc = c;
    }

    protected void setTime(int t) {
        time = t;
        if(time < MIN_TIME)
            time = MIN_TIME;
        else if (time > MAX_TIME)
            time = MAX_TIME;
    }

    // use only this to set frequence
    protected void setFrequency(int freq) {
        period = (int)Math.ceil(1.0/freq*1000000);
        if(period < MIN_PERIOD)
            period = MIN_PERIOD;
        else if(period >MAX_PERIOD)
            period = MAX_PERIOD;
        if((double)period/volume < 10)
            volume = (int)Math.ceil((double)period/10);
        if(isActive()) {
            try {
                send();
            }
            catch (Exception e) {

            }
        }
    }

    public void set(int vol, int freq) {
        setVolume(vol);
        setFrequency(freq);
    }

    public void send(int vol, int freq) throws IOException, InterruptedException {
        setVolume(vol);
        setFrequency(freq);
        send();
    }

    public void send() throws IOException, InterruptedException {
        if(btc != null) {
            pack();
            btc.send(pack);
            if (!isActive())
                off();
        }
    }

    protected void off() throws IOException, InterruptedException {
        if(btc != null) {
            int tmpVolume = volume;
            volume = 0;
            pack();
            try {
                Thread.sleep(time);
            } catch(InterruptedException ex) {
                Thread.currentThread().interrupt();
            }
            btc.send(pack);
            volume = tmpVolume;
        }
    }

    protected void pack() {
        byte packageVolLow = (byte) (volume & 0x7F);
        byte packageVolHigh = (byte) ((volume & 0x80) >>> 7);

        byte packageT1 = (byte)(period & 0x7F);
        byte packageT2 = (byte)((period >>> 7 ) & 0x7F);
        byte packageT3 = (byte)((period >>> 14) & 0x7F);

        pack[1] = packageVolLow;
        pack[2] = packageVolHigh;
        pack[3] = packageT1;
        pack[4] = packageT2;
        pack[5] = packageT3;
    }
}