package com.xminnov.bu01.demo;

import android.app.Application;

import com.xminnov.ble.BU01_Reader;

/**
 * Created by xminnov on 18/7/29.
 */

public class BleApplication extends Application {

    private BU01_Reader reader;

    public BU01_Reader getReader() {
        return reader;
    }

    public void setReader(BU01_Reader reader) {
        this.reader = reader;
    }
}
