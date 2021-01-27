package com.xminnov.bu01.ui;

import android.content.Context;
import android.support.v7.app.AlertDialog;

//import com.xminnov.bu01.demo.BleTagOperationActivity;
import com.xminnov.bu01.demo.R;

/**
 * Created by liym on 2018/9/13.
 */

public class CustomDialog {
    public static void showLowPower(Context context) {
        AlertDialog.Builder dialog = new AlertDialog.Builder(context);
        dialog.setMessage(context.getResources().getString(R.string.text_dialog_low_power));
        dialog.setPositiveButton(context.getResources().getString(R.string.text_dialog_btn_confirm), (dialog1, which) -> {
        });
        dialog.show();
    }
}
