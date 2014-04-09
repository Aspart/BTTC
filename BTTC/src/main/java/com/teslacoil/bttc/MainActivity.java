package com.teslacoil.bttc;

import java.io.IOException;
import java.util.Locale;
import java.util.Set;

import android.app.AlertDialog;
import android.app.Dialog;
import android.app.DialogFragment;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.PorterDuff;
import android.graphics.drawable.Drawable;
import android.os.Environment;
import android.support.v4.app.FragmentManager;
import android.support.v7.app.ActionBarActivity;
import android.support.v7.app.ActionBar;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.app.FragmentPagerAdapter;
import android.os.Bundle;
import android.support.v4.view.ViewPager;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;

public class MainActivity extends ActionBarActivity implements ActionBar.TabListener {
    private final static int REQUEST_ENABLE_BT = 1;

    /**
     * The {@link android.support.v4.view.PagerAdapter} that will provide
     * fragments for each of the sections. We use a
     * {@link FragmentPagerAdapter} derivative, which will keep every
     * loaded fragment in memory. If this becomes too memory intensive, it
     * may be best to switch to a
     * {@link android.support.v4.app.FragmentStatePagerAdapter}.
     */
    SectionsPagerAdapter mSectionsPagerAdapter;

    /**
     * The {@link ViewPager} that will host the section contents.
     */
    ViewPager mViewPager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // Set up the action bar.
        final ActionBar actionBar = getSupportActionBar();
        actionBar.setNavigationMode(ActionBar.NAVIGATION_MODE_TABS);

        // Create the adapter that will return a fragment for each of the three
        // primary sections of the activity.
        mSectionsPagerAdapter = new SectionsPagerAdapter(getSupportFragmentManager());

        // Set up the ViewPager with the sections adapter.
        mViewPager = (ViewPager) findViewById(R.id.pager);
        mViewPager.setAdapter(mSectionsPagerAdapter);

        // When swiping between different sections, select the corresponding
        // tab. We can also use ActionBar.Tab#select() to do this if we have
        // a reference to the Tab.
        mViewPager.setOnPageChangeListener(new ViewPager.SimpleOnPageChangeListener() {
            @Override
            public void onPageSelected(int position) {
                actionBar.setSelectedNavigationItem(position);
            }
        });

        // For each of the sections in the app, add a tab to the action bar.
        for (int i = 0; i < mSectionsPagerAdapter.getCount(); i++) {
            // Create a tab with text corresponding to the page title defined by
            // the adapter. Also specify this Activity object, which implements
            // the TabListener interface, as the callback (listener) for when
            // this tab is selected.
            actionBar.addTab(
                    actionBar.newTab()
                            .setText(mSectionsPagerAdapter.getPageTitle(i))
                            .setTabListener(this));
        }
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();
        if (id == R.id.action_settings) {
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onTabSelected(ActionBar.Tab tab, FragmentTransaction fragmentTransaction) {
        // When the given tab is selected, switch to the corresponding page in
        // the ViewPager.
        mViewPager.setCurrentItem(tab.getPosition());
    }

    @Override
    public void onTabUnselected(ActionBar.Tab tab, FragmentTransaction fragmentTransaction) {
    }

    @Override
    public void onTabReselected(ActionBar.Tab tab, FragmentTransaction fragmentTransaction) {
    }

    /**
     * A {@link FragmentPagerAdapter} that returns a fragment corresponding to
     * one of the sections/tabs/pages.
     */
    public class SectionsPagerAdapter extends FragmentPagerAdapter {

        public SectionsPagerAdapter(FragmentManager fm) {
            super(fm);
        }

        @Override
        public Fragment getItem(int position) {
            // getItem is called to instantiate the fragment for the given page.
            // Return a PlaceholderFragment (defined as a static inner class below).
            switch (position) {
                case 0: return IterFragment.newInstance(position + 1);
                case 1: return MIDIFragment.newInstance(position + 1);
            }
            return null;
        }

        @Override
        public int getCount() {
            // Show 2 total pages.
            return 2;
        }

        @Override
        public CharSequence getPageTitle(int position) {
            Locale l = Locale.getDefault();
            switch (position) {
                case 0:
                    return getString(R.string.title_section1).toUpperCase(l);
                case 1:
                    return getString(R.string.title_section2).toUpperCase(l);
            }
            return null;
        }
    }

    /**
     * A placeholder fragment containing a simple view.
     */
    public static class IterFragment extends Fragment {
        /**
         * The fragment argument representing the section number for this
         * fragment.
         */
        private static final String ARG_SECTION_NUMBER = "section_number";
        private BluetoothConnector mBluetoothConnector;
        private Interruptor mInterruptor;
        /**
         * Returns a new instance of this fragment for the given section
         * number.
         */
        public static IterFragment newInstance(int sectionNumber) {
            IterFragment fragment = new IterFragment();
            Bundle args = new Bundle();
            args.putInt(ARG_SECTION_NUMBER, sectionNumber);
            fragment.setArguments(args);
            return fragment;
        }

        public IterFragment() {
        }

        double calcLogVal(double progress, double min, double max) {
            double minLog = Math.log(min);
            double maxLog = Math.log(max);
            double retval = Math.exp(minLog+((progress-min)*(maxLog-minLog)/(max-min)));
            return retval;
        }

        private void addFreqSeekBarListener (int seekBar, int textView) {
            final SeekBar sk=(SeekBar) getView().findViewById(seekBar);
            final TextView tv = (TextView) getView().findViewById(textView);
            double current = (double)(sk.getProgress()+1)/(sk.getMax()+1)*Interruptor.MAX_FREQUENCY;
            int val = (int)Math.ceil(calcLogVal(current, Interruptor.MIN_FREQUENCY, Interruptor.MAX_FREQUENCY));
            if(mInterruptor != null)
                mInterruptor.setFrequency(val);
            tv.setText(Integer.toString(val));
            if(sk != null && tv != null)
                sk.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
                    @Override
                    public void onStopTrackingTouch(SeekBar seekBar) {
                        // TODO Auto-generated method stub
                    }

                    @Override
                    public void onStartTrackingTouch(SeekBar seekBar) {
                        // TODO Auto-generated method stub
                    }

                    @Override
                    public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                        // TODO Auto-generated method stub
                        double current = (double)(progress+1)/(sk.getMax()+1)*Interruptor.MAX_FREQUENCY;
                        int val = (int)Math.ceil(calcLogVal(current, Interruptor.MIN_FREQUENCY, Interruptor.MAX_FREQUENCY));
                        tv.setText(Integer.toString(val));
                        if(mInterruptor != null) {
                            mInterruptor.setFrequency(val);
                        }
                    }
                });
        }

        private void addVolSeekBarListener (int seekBar, int textView) {
            final SeekBar sk=(SeekBar) getView().findViewById(seekBar);
            int vol = (int)Math.ceil((double)sk.getProgress()/sk.getMax()*Interruptor.MAX_VOLUME);
            if(mInterruptor != null)
                mInterruptor.setVolume(vol);
            final TextView tv = (TextView) getView().findViewById(textView);
            tv.setText(Integer.toString(sk.getProgress()));
            if(sk != null && tv != null)
                sk.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
                    @Override
                    public void onStopTrackingTouch(SeekBar seekBar) {
                        // TODO Auto-generated method stub
                    }

                    @Override
                    public void onStartTrackingTouch(SeekBar seekBar) {
                        // TODO Auto-generated method stub
                    }

                    @Override
                    public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                        // TODO Auto-generated method stub
                        int val = (int)Math.ceil((double)progress/sk.getMax()*Interruptor.MAX_VOLUME);
                        tv.setText(Integer.toString(progress));
                        if(mInterruptor != null) {
                            mInterruptor.setVolume(val);
                        }
                    }
                });
        }

        private void addTimeSeekBarListener (int seekBar, int textView) {
            final SeekBar sk=(SeekBar) getView().findViewById(seekBar);
            final TextView tv = (TextView) getView().findViewById(textView);
            double current = (double)(sk.getProgress()+1)/(sk.getMax()+1)*Interruptor.MAX_TIME;
            int val = (int)Math.ceil(calcLogVal(current, Interruptor.MIN_TIME, Interruptor.MAX_TIME));
            tv.setText(Integer.toString(val));
            if(mInterruptor != null)
                mInterruptor.setTime(val);
            if(sk != null && tv != null) {
                sk.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
                    @Override
                    public void onStopTrackingTouch(SeekBar seekBar) {
                        // TODO Auto-generated method stub
                    }

                    @Override
                    public void onStartTrackingTouch(SeekBar seekBar) {
                        // TODO Auto-generated method stub
                    }

                    @Override
                    public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                        // TODO Auto-generated method stub
                        double current = (double)(progress+1)/(sk.getMax()+1)*Interruptor.MAX_TIME;
                        int val = (int)Math.ceil(calcLogVal(current, Interruptor.MIN_TIME, Interruptor.MAX_TIME));
                        if(mInterruptor != null) {
                            mInterruptor.setTime(val);
                        }
                        tv.setText(Integer.toString(val));
                    }
                });
            }
        }

        private void addConnectButtonListener () {
            final Button button= (Button) getView().findViewById(R.id.connectButton);
            if(button != null)
                button.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        if (!mBluetoothConnector.isBTEnabled()) {
                            Intent enableBtIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
                            startActivityForResult(enableBtIntent, REQUEST_ENABLE_BT);
                        }
                        if(mBluetoothConnector.isActive()) {
                            try {
                                mBluetoothConnector.close();
                                button.setText("Connect");
                                Toast.makeText(getActivity(), "Disconnected", Toast.LENGTH_LONG).show();
                            }
                            catch(Exception e) {
                                throw new RuntimeException(e);
                            }
                        }
                        else {
                            final Set<BluetoothDevice> bondedDevices = mBluetoothConnector.getPaired();

                            final String[] mDevicesName = new String[bondedDevices.size()];
                            int i = 0;
                            for(BluetoothDevice d : bondedDevices) {
                                mDevicesName[i] = d.getName();
                                i++;
                            }

                            new AlertDialog.Builder(getActivity())
                                    .setTitle("Select device")
                                    .setItems(mDevicesName, new DialogInterface.OnClickListener() {
                                        @Override
                                        public void onClick(DialogInterface dialog, int item) {
                                            try {
                                                boolean ret = mBluetoothConnector.connectDevice((BluetoothDevice)bondedDevices.toArray()[item]);
                                                if(ret) {
                                                    button.setText("Disconnect");
                                                    Toast.makeText(getActivity(), "Conected", Toast.LENGTH_LONG).show();
                                                }
                                                else {
                                                    Toast.makeText(getActivity(), "Filed to connect", Toast.LENGTH_LONG).show();
                                                }
                                            } catch (IOException e) {
                                                throw new RuntimeException(e);
                                            }
                                        }
                                    }).setCancelable(true).setNegativeButton("Cancel", null).create().show();
                        }
                    }
                });
        }

        private void addSingleButtonListener () {
            Button button= (Button) getView().findViewById(R.id.singleButton);
            if(button != null)
                button.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        try {
                            mInterruptor.setActive(false);
                            mInterruptor.send();
                        }
                        catch(IOException e) {
                            System.out.println("IO error in setOnClickListener");
                        }
                        catch (InterruptedException e) {
                            System.out.println("Interrupted error in setOnClickListener");
                        }
                        catch (NullPointerException e) {
                            System.out.println("NULL interrupter error");
                        }
                    }
                });
        }

        private void addLoopButtonListener () {
            final Button button= (Button) getView().findViewById(R.id.loopButton);
            if(button != null)
                button.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        if(mInterruptor.isActive()) {
                            mInterruptor.setActive(false);
                            try {
                                mInterruptor.send();
                            }
                            catch (Exception e) {
                                throw new RuntimeException(e);
                            }
                            button.setText("Loop");
                            button.setBackgroundColor(Color.DKGRAY);
                        }
                        else {
                            mInterruptor.setActive(true);
                            try {
                                mInterruptor.send();
                            } catch (Exception e) {
                                throw new RuntimeException(e);
                            }
                            button.setText("Off");
                            button.setBackgroundColor(Color.RED);
                        }
                    }
                });
        }

        @Override
        public View onCreateView(LayoutInflater inflater, ViewGroup container,
                Bundle savedInstanceState) {
            return inflater.inflate(R.layout.fragment_iter, container, false);
        }

        private void getBluetoothConnector() {
            BluetoothAdapter bluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
            if (bluetoothAdapter == null) {
                return;
            }
            mBluetoothConnector = new BluetoothConnector(bluetoothAdapter);
        }

        @Override
        public void onActivityCreated(Bundle savedInstanceState) {
            super.onActivityCreated(savedInstanceState);
            getBluetoothConnector();
            mInterruptor = new Interruptor();
            mInterruptor.setBluetoothConnector(mBluetoothConnector);
            // seek bars listeners - send arguments to profiler class
            addVolSeekBarListener(R.id.volSeekBar, R.id.volView);
            addFreqSeekBarListener(R.id.freqSeekBar, R.id.freqView);
            addTimeSeekBarListener(R.id.timeSeekBar, R.id.timeView);
            // button listeners - provide BT interface
            addConnectButtonListener();
            addSingleButtonListener();
            addLoopButtonListener();

            }
    }

    public static class MIDIFragment extends Fragment {
        /**
         * The fragment argument representing the section number for this
         * fragment.
         */
        private static final String ARG_SECTION_NUMBER = "section_number";

        /**
         * Returns a new instance of this fragment for the given section
         * number.
         */
        public static MIDIFragment newInstance(int sectionNumber) {
            MIDIFragment fragment = new MIDIFragment();
            Bundle args = new Bundle();
            args.putInt(ARG_SECTION_NUMBER, sectionNumber);
            fragment.setArguments(args);
            return fragment;
        }

        public MIDIFragment() {

        }

        private void addOpenButtonListener () {
            Button openButton= (Button) getView().findViewById(R.id.openButton);
            openButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    OpenFileDialog fileDialog = new OpenFileDialog(getActivity())
                            .setOpenDialogListener(new OpenFileDialog.OpenDialogListener() {
                                @Override
                                public void OnSelectedFile(String fileName) {
                                    System.out.println(fileName);
                                }
                            });
                    fileDialog.show();
                }
            });
        }

        @Override
        public View onCreateView(LayoutInflater inflater, ViewGroup container,
                                 Bundle savedInstanceState) {
            return inflater.inflate(R.layout.fragment_midi, container, false);
        }

        @Override
        public void onActivityCreated(Bundle savedInstanceState) {
            super.onActivityCreated(savedInstanceState);
            addOpenButtonListener();
        }
    }
}
