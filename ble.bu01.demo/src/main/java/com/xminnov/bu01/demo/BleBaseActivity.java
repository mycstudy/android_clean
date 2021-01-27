package com.xminnov.bu01.demo;

import android.content.Context;
import android.content.pm.ActivityInfo;
import android.media.MediaPlayer;
import android.os.Bundle;
import android.os.PowerManager;
import android.support.annotation.Nullable;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Toast;

import com.xminnov.ble.BU01_Reader;
import com.xminnov.ble.impl.EpcReply;


/**
 * Created by liym on 2018/8/3.
 */

public class BleBaseActivity extends AppCompatActivity {
    protected BU01_Reader reader;
    private PowerManager.WakeLock wakeLock;
    private MediaPlayer mediaPlayer;



    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        reader = ((BleApplication) getApplication()).getReader();
        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null){
            actionBar.setDisplayHomeAsUpEnabled(true);
        }

        PowerManager pm = (PowerManager) getSystemService(Context.POWER_SERVICE);
        wakeLock = pm.newWakeLock(PowerManager.PARTIAL_WAKE_LOCK, "rfid_check:my activity");

        wakeLock.acquire();
        ActivityCollector.addActivity(this);
    }



    @Override
    protected void onDestroy() {
        super.onDestroy();
        ActivityCollector.removeActivity(this);
        wakeLock.release();
        wakeLock = null;
    }

    @Override
    protected void onResume() {
        super.onResume();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        if (reader != null){
            getMenuInflater().inflate(R.menu.ble_reader, menu);
            MenuItem connectItem = menu.findItem(R.id.connect);
            if (reader.isConnecting()) {
                connectItem.setActionView(R.layout.actionbar_indeterminate_progress);
            } else {
                connectItem.setActionView(null);
            }
            if (reader.isConnected()) {
                connectItem.setTitle(getString(R.string.disconnect));
            } else {
                connectItem.setTitle(getString(R.string.connect));
            }
        }
        return true;
    }



    public void playHint(int resId) {
        if (mediaPlayer != null) {
            if(mediaPlayer.isPlaying())
                mediaPlayer.stop();
            mediaPlayer.reset();
            mediaPlayer.release();
            mediaPlayer = null;
        }
        mediaPlayer = MediaPlayer.create(this, resId);
        mediaPlayer.start(); // no need to call prepare(); create() does that for you
    }


    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                finish();
                break;
            case R.id.connect:
                if (item.getTitle().equals(getString(R.string.connect))) {
                    connect();
                } else {
                    disconnect();
                }
                break;
        }
        return super.onOptionsItemSelected(item);
    }

    protected void connect() {
        ActivityCollector.invalidateOptionsMenu();
        System.out.println("dddddddddd");
        if (reader != null){
            reader.connect(BleBaseActivity.this, new BU01_Reader.Callback() {
                @Override
                public void onConnect() {
                    System.out.println("eeeeeeeeeeee");
                    ActivityCollector.invalidateOptionsMenu();
                    ActivityCollector.onReaderConnect();
                }

                @Override
                public void onDisconnect() {
                    ActivityCollector.invalidateOptionsMenu();
                    ActivityCollector.onReaderDisconnect();
                    System.out.println("fffffffff");
                    if (!isFinishing()) {
                        System.out.println("gggggggggg");
                        Toast.makeText(BleBaseActivity.this, getResources().getString(R.string.toast_err_disconnect), Toast.LENGTH_SHORT).show();
                        for(int i=0;i<5;i++){
                            playHint(R.raw.disconnect);
                        }
                        System.out.println("hhhhhhhhh");
                        connect();
                        System.out.println("iiiiiiiii");
                    }
                }
            });
        }
    }

    protected void disconnect() {
        if (reader != null){
            reader.disconnect();
        }
    }

    protected void showToast(String message){
        Toast.makeText(BleBaseActivity.this, message, Toast.LENGTH_SHORT).show();
    }

    protected void onReaderConnect(){

    }

    protected void onReaderDisconnect(){

    }
}
