package com.xminnov.bu01.demo;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.PowerManager;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;


import com.xminnov.ble.BU01_Reader;
import com.xminnov.ble.BU01_Service;
import com.xminnov.ble.BU01_Factory;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;
public class BleServiceActivity extends AppCompatActivity {

    private BU01_Service service;
    private Adapter adapter;
    private PowerManager.WakeLock wakeLock;
    private Handler handler = new Handler();
    private Runnable stopScanRunnable = this::stopScan;
    private Runnable notifyRunnable = new Runnable() {
        @Override
        public void run() {
            adapter.notifyDataSetChanged();
            handler.postDelayed(this, 500);
        }
    };

    @BindView(R.id.swipe)
    SwipeRefreshLayout swipe;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_service);
        setSupportActionBar(findViewById(R.id.toolbar));
        ButterKnife.bind(this);
        PowerManager pm = (PowerManager) getSystemService(Context.POWER_SERVICE);
        wakeLock = pm.newWakeLock(PowerManager.PARTIAL_WAKE_LOCK, "rfid_check:my activity");

        wakeLock.acquire();

        try {
            service = BU01_Factory.bu01(this, reader -> adapter.addReader(reader));
        } catch (Exception e) {
            Toast.makeText(this, e.getMessage(), Toast.LENGTH_LONG).show();
            finish();
            return;
        }

        RecyclerView list = findViewById(R.id.list);
        list.setLayoutManager(new LinearLayoutManager(this));
        list.setAdapter((adapter = new Adapter()));
        list.getItemAnimator().setAddDuration(0);
        list.getItemAnimator().setChangeDuration(0);
        list.getItemAnimator().setMoveDuration(0);
        list.getItemAnimator().setRemoveDuration(0);

        adapter.setOnItemClickListener(reader -> {
            ((BleApplication) getApplication()).setReader(reader);
            startActivity(new Intent(this, BleMainActivity.class));
        });

        swipe.setColorSchemeResources(R.color.colorAccent);
        swipe.setOnRefreshListener(this::scanForBleReader);
        swipe.setRefreshing(true);

        scanForBleReader();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        wakeLock.release();
        wakeLock = null;
    }

    private void scanForBleReader() {
        adapter.clearReaders();
        service.scanForBU01BleReader();
        handler.postDelayed(stopScanRunnable, 10000);
        handler.postDelayed(notifyRunnable, 500);
    }

    private void stopScan() {
        service.stopScan();
        handler.removeCallbacks(stopScanRunnable);
        handler.removeCallbacks(notifyRunnable);
        swipe.setRefreshing(false);
    }

    static class Adapter extends RecyclerView.Adapter<Adapter.ViewHolder> {

        private List<BU01_Reader> readers = new ArrayList<>();
        private OnItemClickListener listener;

        void addReader(BU01_Reader reader) {
            Log.e("reader test", reader.toString());
            if (readers.contains(reader)) {
                readers.set(readers.indexOf(reader), reader);
            } else {
                readers.add(reader);
            }
            Collections.sort(readers, (o1, o2) -> o2.getRssi() - o1.getRssi());
        }

        void clearReaders() {
            readers.clear();
            notifyDataSetChanged();
        }

        void setOnItemClickListener(OnItemClickListener listener) {
            this.listener = listener;
        }

        @Override
        public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            return new ViewHolder(LayoutInflater.from(parent.getContext()).inflate(R.layout.item_device, parent, false));
        }

        @SuppressLint("SetTextI18n")
        @Override
        public void onBindViewHolder(ViewHolder holder, int position) {
            BU01_Reader reader = readers.get(position);
            holder.name.setText(reader.getName());
            holder.mac.setText(reader.getAddress());
            holder.rssi.setText(reader.getRssi() + "dB");
            holder.itemView.setOnClickListener(v -> {
                if (listener != null) {
                    listener.onItemClick(readers.get(holder.getAdapterPosition()));
                }
            });
        }

        @Override
        public int getItemCount() {
            return readers.size();
        }

        static class ViewHolder extends RecyclerView.ViewHolder {

            @BindView(R.id.name)
            TextView name;
            @BindView(R.id.mac)
            TextView mac;
            @BindView(R.id.rssi)
            TextView rssi;

            ViewHolder(View itemView) {
                super(itemView);
                ButterKnife.bind(this, itemView);
            }

        }

        interface OnItemClickListener {
            void onItemClick(BU01_Reader reader);
        }

    }


}
