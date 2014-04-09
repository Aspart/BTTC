package com.teslacoil.bttc;

import android.util.Log;

import java.io.File;
import java.io.IOException;

import com.leff.midi.MidiFile;
import com.leff.midi.event.MidiEvent;
import com.leff.midi.event.NoteOn;
import com.leff.midi.util.MidiEventListener;
import com.leff.midi.util.MidiProcessor;

public class MIDIPlayer implements MidiEventListener {
    MidiProcessor mProcessor;
    private String mLabel;
    public static double getFreqFromMidiNote(int d) {
        return Math.pow(2, (d-69)/12)*440;
    }

    public MIDIPlayer(String label)
    {
        mLabel = label;
    }

    // 0. Implement the listener functions that will be called by the
    // MidiProcessor
    @Override
    public void onStart(boolean fromBeginning)
    {
        if(fromBeginning)
        {
            Log.w("myApp",mLabel + " Started!");
        }
        else
        {
            Log.w("myApp",mLabel + " resumed");
        }
    }

    @Override
    public void onEvent(MidiEvent event, long ms)
    {
        NoteOn noteOn = (NoteOn)event;
        int value = noteOn.getNoteValue();
        int velocity = noteOn.getVelocity();
        int channel = noteOn.getChannel();
        int type = noteOn.getType();
        Log.w("myApp",mLabel + " received event: freq " + getFreqFromMidiNote(value) + " velocity " +velocity + " channel " + channel);

    }

    @Override
    public void onStop(boolean finished)
    {
        if(finished)
        {
            Log.w("myApp", mLabel + " finished!");
        }
        else
        {
            Log.w("myApp", mLabel + " paused");
        }
    }

    void start() {
        mProcessor.start();
    }

    void stop() {
        mProcessor.stop();

    }

    void reset() {
        mProcessor.reset();
    }

    String getTrackName() {
        return mLabel;
    }

    boolean isRunning() {
        return mProcessor.isRunning();
    }
    public void run(String midiFile)
    {
        // 1. Read in a MidiFile
        MidiFile midi = null;
        try
        {
            midi = new MidiFile(new File(midiFile));
        }
        catch(IOException e)
        {
            return;
        }

        // 2. Create a MidiProcessor
        mProcessor = new MidiProcessor(midi);

        // 3. Register listeners for the events you're interested in
//        EventPrinter ep = new EventPrinter("Individual Listener");
//        processor.registerEventListener(ep, Tempo.class);
//        processor.registerEventListener(ep, NoteOn.class);

        // or listen for all events:
        MIDIPlayer ep = new MIDIPlayer("Listener For All");
        mProcessor.registerEventListener(ep, NoteOn.class);
    }
}
