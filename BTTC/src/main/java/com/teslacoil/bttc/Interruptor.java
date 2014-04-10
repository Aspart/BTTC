package com.teslacoil.bttc;

public class Interruptor {
    public final static int MIN_VOLUME = 0;
    public final static int MAX_VOLUME = 255;
    private final static int MIN_PERIOD =500;
    private final static int MAX_PERIOD = 62500;
    public final static int MIN_TIME = 50;
    public final static int MAX_TIME = 1500;
    public final static int MIN_FREQUENCY = 15;
    public final static int MAX_FREQUENCY = 2000;
    private int mVolume = 128; // 1byte
    private int mPeriod = 2560; // 2-3byte
    private int mTime = 100; // delay time
    private boolean mActive = false;
    private byte[] mPack = new byte[7];

    BluetoothConnector btc = null;

    public Interruptor() {
        mPack[0] = (byte)0xFF;
        mPack[6] = (byte)0xFF;
    }

    public void enable() {mActive = true;}
    public void disable() {mActive = false;}
    public boolean isEnabled() {return mActive;}

    // use only this to set volume
    protected void setVolume(int vol) {
        if((double) mPeriod /vol < 10)
            mVolume = (int)Math.ceil((double) mPeriod /10);
        else if(vol > MAX_VOLUME)
            mVolume = MAX_VOLUME;
        else if (vol < MIN_VOLUME)
            mVolume = MIN_VOLUME;
        else
            mVolume = vol;
        if(mActive) {
            send();
        }
    }

    public void setBluetoothConnector(BluetoothConnector c) {
        btc = c;
    }

    protected void setTime(int t) {
        mTime = t;
        if(mTime < MIN_TIME)
            mTime = MIN_TIME;
        else if (mTime > MAX_TIME)
            mTime = MAX_TIME;
    }

    // use only this to set frequence
    protected void setFrequency(int freq) {
        mPeriod = (int)Math.ceil(1.0/freq*1000000);
        if(mPeriod < MIN_PERIOD)
            mPeriod = MIN_PERIOD;
        else if(mPeriod >MAX_PERIOD)
            mPeriod = MAX_PERIOD;
        if((double) mPeriod / mVolume < 10)
            mVolume = (int)Math.ceil((double) mPeriod /10);
        if(mActive) {
            send();
        }
    }

    public void set(int vol, int freq) {
        setVolume(vol);
        setFrequency(freq);
    }

    public void send(int vol, int freq) {
        setVolume(vol);
        setFrequency(freq);
        send();
    }

    public void send() {
        if(btc != null) {
            pack();
            btc.send(mPack);
            if (!mActive)
                off();
        }
    }

    protected void off() {
        if(btc != null) {
            int tmpVolume = mVolume;
            mVolume = 0;
            pack();
            try {
                Thread.sleep(mTime);
            } catch(InterruptedException ex) {
                Thread.currentThread().interrupt();
            }
            btc.send(mPack);
            mVolume = tmpVolume;
        }
    }

    protected void pack() {
        byte packageVolLow = (byte) (mVolume & 0x7F);
        byte packageVolHigh = (byte) ((mVolume & 0x80) >>> 7);

        byte packageT1 = (byte)(mPeriod & 0x7F);
        byte packageT2 = (byte)((mPeriod >>> 7 ) & 0x7F);
        byte packageT3 = (byte)((mPeriod >>> 14) & 0x7F);

        mPack[1] = packageVolLow;
        mPack[2] = packageVolHigh;
        mPack[3] = packageT1;
        mPack[4] = packageT2;
        mPack[5] = packageT3;
    }
}