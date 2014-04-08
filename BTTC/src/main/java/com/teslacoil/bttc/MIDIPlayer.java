package com.teslacoil.bttc;

import java.io.File;

public class MIDIPlayer {
        public static final int NOTE_ON = 0x90;
        public static final int NOTE_OFF = 0x80;
        public static final String[] NOTE_NAMES = {"C", "C#", "D", "D#", "E", "F", "F#", "G", "G#", "A", "A#", "B"};

    public static double getFreqFromMidiNote(int d) {
        return Math.pow(2, (d-69)/12)*440;
    }

}
