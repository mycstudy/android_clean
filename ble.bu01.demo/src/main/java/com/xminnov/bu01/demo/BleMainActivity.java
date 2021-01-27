package com.xminnov.bu01.demo;

import android.content.Context;
import android.content.pm.PackageManager;
import android.media.MediaPlayer;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.os.PowerManager;
import android.support.annotation.Nullable;
import android.support.v7.app.ActionBar;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;
import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.xminnov.ble.BU01_Reader;
import com.xminnov.ble.impl.EpcReply;
import com.xminnov.bu01.ui.CustomDialog;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import butterknife.BindView;
import butterknife.ButterKnife;

/**
 * Created by liym on 2018/8/2.
 */

public class BleMainActivity extends BleBaseActivity {

//    @BindView(R.id.btn_setting) Button btnSetting;
//    @BindView(R.id.btn_demo) Button btnDemo;
    @BindView(R.id.btn_clear) Button btnClear;
    @BindView(R.id.tv_total) TextView tv_total;
    @BindView(R.id.btn_inventory) Button btnInventory;
    @BindView(R.id.listView_epc) ListView lvEpc;
    private Thread upLoadThread;
    private PowerManager.WakeLock wakeLock;


//    @BindView(R.id.tv_debug) TextView tvDebug;

    private String sn = "";
    private EPCAdapter mEPCAdapter;
    private boolean isUpload=true;
    private int ismatch;
    private boolean uploadAgain=false;
    private List<String> labelIdList=new ArrayList<>();
    private List<String> dishclothIdList=new ArrayList<>();
    private List<String> epcList=new ArrayList<>();
    private String reader_name;

    private Handler mHandler;
    private boolean inventorying = false;
    private Runnable inventoryRunnable = new Runnable() {
        @Override
        public void run() {
            reader.singleInventory((status, list) -> {
                boolean allRag=true;
                boolean alarm=false;
                String max_rag="";
                float max_rag_rssi=-1000;
                String max_tag="";
                float max_tag_rssi=-1000;
                epcList.clear();
                mEPCAdapter.mEpcRecords.clear();

                if (status == 0){
                    for (EpcReply epcReply : list){
                        mEPCAdapter.addEpcRecord(ByteUtils.epcBytes2Hex(epcReply.getEpc()), epcReply.getRssi());

                        StringBuilder sb = new StringBuilder();
                        byte[] cur = epcReply.getEpc();
                        for (int i = 0; i < cur.length; i++) {
                            sb.append(String.format("%02X", cur[i]));
                        }
                        epcList.add(sb.toString());
                    }
                    final int total = mEPCAdapter.getTotal();
                    runOnUiThread(() -> {
                        tv_total.setText(String.valueOf(total));
                    });
                } else if (status == -205) {
                    //inventory tag or other tag operation maybe return -205
//                    System.out.println("aaaaaaaa");
                    playHint(R.raw.disconnect);
//                    System.out.println("bbbbbbbb");
                    connect();

//                    mHandler.removeCallbacks(inventoryRunnable);
//                    inventorying = false;
//                    btnInventory.setText(getResources().getString(R.string.text_btn_inventory_start));
//                    updateControls(true);
//                    CustomDialog.showLowPower(BleMainActivity.this);
                }

                for(EpcRecord r:mEPCAdapter.mEpcRecords){
//                    epcList.add(r.getEpc());
                    //选出信号最强的抹布和标，后面匹配就匹配最强的两个
                    if((r.getEpc().charAt(0)=='1'&&r.getEpc().charAt(1)=='2')||(r.getEpc().charAt(0)=='2'&&r.getEpc().charAt(1)=='3')){
                        if(max_rag=="") {
                            max_rag = r.getEpc();
                            max_rag_rssi = r.getRssi();
                        }
                        if(r.getRssi()>max_rag_rssi){
                            max_rag = r.getEpc();
                            max_rag_rssi = r.getRssi();
                        }
                    }else if((r.getEpc().charAt(0)=='3'&&r.getEpc().charAt(1)=='4')||(r.getEpc().charAt(0)=='4'&&r.getEpc().charAt(1)=='5')){
                        if(max_tag=="") {
                            max_tag = r.getEpc();
                            max_tag_rssi = r.getRssi();
                        }
                        if(r.getRssi()>max_tag_rssi){
                            max_tag = r.getEpc();
                            max_tag_rssi = r.getRssi();
                        }
                    }
                }

                //判断有没有除了抹布以外的标签
                if(max_tag!=""){
                    allRag=false;
                }

                //判断是否需要上传到平台
                if(epcList.isEmpty()||allRag){
                    isUpload=false;
                }else{
                    isUpload=true;
                }

                //判断匹不匹配
                if(max_rag!=""&&max_tag!=""){
                    if(max_rag.charAt(0)=='1'&&max_rag.charAt(1)=='2'){
                        if(max_tag.charAt(0)=='3'&max_tag.charAt(1)=='4'){
                            alarm=false;
                            ismatch=1;
                            playHint(R.raw.short_hint);
                        }
                        else if(max_tag.charAt(0)=='4'&&max_tag.charAt(1)=='5'){
                            alarm=true;
                            ismatch=0;
                            playHint(R.raw.long_hint);
                        }
                    }
                    else if(max_rag.charAt(0)=='2'&&max_rag.charAt(1)=='3'){
                        if(max_tag.charAt(0)=='3'&&max_tag.charAt(1)=='4'){
                            alarm=true;
                            ismatch=0;
                            playHint(R.raw.long_hint);
                        }
                        else if(max_tag.charAt(0)=='4'&&max_tag.charAt(1)=='5'){
                            alarm=false;
                            ismatch=1;
                            playHint(R.raw.short_hint);
                        }
                    }
                }

                //如果需要上传，调用函数
                if(isUpload){
                    uploadAgain=false;
                    upLoadThread.run();
                    while(uploadAgain){
                        upLoadThread.run();
                    }
                }

                if (inventorying){
                    mHandler.postDelayed(inventoryRunnable, 1000);
                }
            });
        }
    };

    @Override
    protected void connect() {
        ActivityCollector.invalidateOptionsMenu();
//        System.out.println("dddddddddd");
        if (reader != null){
            reader.connect(BleMainActivity.this, new BU01_Reader.Callback() {
                @Override
                public void onConnect() {
//                    System.out.println("eeeeeeeeeeee");
                    btnInventory.setText(getResources().getString(R.string.text_btn_inventory_start));
                    onInventoryAction(btnInventory);
//                    System.out.println("kkkkkkkkkk");
                    ActivityCollector.invalidateOptionsMenu();
                    ActivityCollector.onReaderConnect();
                }

                @Override
                public void onDisconnect() {
                    ActivityCollector.invalidateOptionsMenu();
                    ActivityCollector.onReaderDisconnect();
//                    System.out.println("fffffffff");
                    if (!isFinishing()) {
//                        System.out.println("gggggggggg");
                        Toast.makeText(BleMainActivity.this, getResources().getString(R.string.toast_err_disconnect), Toast.LENGTH_SHORT).show();
                        playHint(R.raw.disconnect);
//                        System.out.println("hhhhhhhhh");
                        onClear(btnClear);
                        btnInventory.setText(getResources().getString(R.string.text_btn_inventory_stop));
                        onInventoryAction(btnInventory);
//                        System.out.println("jjjjjjj");
                        connect();
//                        System.out.println("iiiiiiiii");
                    }
                }
            });
        }
    }


    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        ButterKnife.bind(this);
        reader=((BleApplication) getApplication()).getReader();
        reader_name=reader.getName();
        mHandler = new Handler(Looper.getMainLooper());
        initView();
        connect();
        PowerManager pm = (PowerManager) getSystemService(Context.POWER_SERVICE);
        wakeLock = pm.newWakeLock(PowerManager.PARTIAL_WAKE_LOCK, "rfid_check:my activity");

        wakeLock.acquire();

        upLoadThread=new Thread(new Runnable() {
            @Override
            public void run() {
                try{
                    upLoadFunction();
                }catch (JSONException e){
                    e.printStackTrace();
                }
            }
        });
        upLoadThread.start();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        wakeLock.release();
        wakeLock = null;
        if (reader.isConnected() && inventorying){
            mHandler.removeCallbacks(inventoryRunnable);
            inventorying = false;
        }
        if (reader.isConnected()){
            reader.disconnect();
        }
    }

    @Override
    protected void onReaderConnect() {
        super.onReaderConnect();
        reader.getSerialNumber((status, serialNumber)-> {
            if (status == 0){
                sn = String.format(Locale.getDefault(),"%010d", serialNumber);
                ActionBar actionBar = getSupportActionBar();
                if (actionBar != null){
                    actionBar.setTitle(sn);
                }
            }
//            updateControls(true);
            btnInventory.setEnabled(true);
        });
    }

    @Override
    protected void onReaderDisconnect() {
        super.onReaderDisconnect();
//        updateControls(false);
        btnInventory.setEnabled(false);
    }

    private void initView(){
        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null){
            actionBar.setDisplayHomeAsUpEnabled(true);
        }

        tv_total.setText("0");
        mEPCAdapter = new EPCAdapter(this);
        lvEpc.setAdapter(mEPCAdapter);

        String versionName = "";
        try {
            versionName = getPackageManager().getPackageInfo(getPackageName(), 0).versionName;
        } catch (PackageManager.NameNotFoundException ignored) {
        }
//        if (BuildConfig.DEBUG){
//            tvDebug.setText(String.format("Debug version: %s", versionName));
//        } else {
//            tvDebug.setText(String.format("Version: %s", versionName));
//        }

//        updateControls(false);
        btnInventory.setEnabled(false);
    }

//    private void updateControls(boolean enable){
//        btnSetting.setEnabled(enable);
//        btnDemo.setEnabled(enable);
//    }



//    public void onSetting(View view){
//        BleSettingActivity.actionStart(this, sn);
//    }
//
//    public void onDemo(View view){
//        BleDemoActivity.actionStart(this, sn);
//    }

    public void onGetUII(View view){
        btnInventory.setEnabled(false);
        reader.singleInventory((status, epcReplies) -> {
            btnInventory.setEnabled(true);
            if (status == 0) {
                if (epcReplies.size() == 0) {
                    showToast(getResources().getString(R.string.toast_err_no_tag));
                    return;
                }
                for (EpcReply epcReply : epcReplies){
                    mEPCAdapter.addEpcRecord(ByteUtils.epcBytes2Hex(epcReply.getEpc()), epcReply.getRssi());
                }
                final int total = mEPCAdapter.getTotal();
            } else {
                //inventory tag or other tag operation maybe return -205
                if (status == -205) {
                    CustomDialog.showLowPower(BleMainActivity.this);
                } else {
                    showToast(getResources().getString(R.string.toast_err_single_inventory, status));
                }
            }
        });
    }

    public void onInventoryAction(View view){
        if (getResources().getString(R.string.text_btn_inventory_start).equals( btnInventory.getText().toString())){
            mHandler.post(inventoryRunnable);
            inventorying = true;
            btnInventory.setText(getResources().getString(R.string.text_btn_inventory_stop));
//            updateControls(false);
        } else {
            mHandler.removeCallbacks(inventoryRunnable);
            inventorying = false;
            btnInventory.setText(getResources().getString(R.string.text_btn_inventory_start));
//            updateControls(true);
        }
    }

    public void onClear(View view){
        mEPCAdapter.clearEpcRecord();
    }

    class EPCAdapter extends BaseAdapter {
        private Context mContext;
        private LayoutInflater mInflater;
        private List<EpcRecord> mEpcRecords;

        EPCAdapter(Context context) {
            mContext = context;
            mInflater = LayoutInflater.from(context);
            mEpcRecords = new ArrayList<>();
        }

        @Override
        public long getItemId(int position) {
            return 0;
        }

        @Override
        public Object getItem(int position) {
            return mEpcRecords.get(position);
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            View view = convertView;
            if (view == null){
                view = mInflater.inflate(R.layout.item_epc, parent, false);
                view.setOnClickListener(v -> {
//                    if (!inventorying){
//                        BleTagOperationActivity.actionStart(mContext, mEpcRecords.get(position).getEpc(), sn);
//                    }
                });
            }
            EpcRecord epcRecord = mEpcRecords.get(position);
            ((TextView)view.findViewById(R.id.tv_no)).setText(String.valueOf(epcRecord.getNo()));
            ((TextView)view.findViewById(R.id.tv_epc)).setText(epcRecord.getEpc());
            ((TextView)view.findViewById(R.id.tv_count)).setText(String.valueOf(epcRecord.getCount()));
            ((TextView)view.findViewById(R.id.tv_rssi)).setText(String.valueOf(epcRecord.getRssi()));
            return view;
        }

        @Override
        public int getCount() {
            return mEpcRecords.size();
        }

        void addEpcRecord(String epc, float rssi){
            EpcRecord epcRecord = null;
            for (EpcRecord record : mEpcRecords){
                if (record.getEpc().equals(epc)){
                    epcRecord = record;
                    record.setCount(record.getCount() + 1);
                    record.setRssi(rssi);
                    break;
                }
            }
            if (epcRecord == null){
                mEpcRecords.add(new EpcRecord(mEpcRecords.size() + 1, epc, rssi));
            }
            notifyDataSetChanged();
        }

        void clearEpcRecord(){
            mEpcRecords.clear();
            tv_total.setText("0");
            notifyDataSetChanged();
        }

        int getTotal(){
            return mEpcRecords.size();
        }
    }

    //将数据上传到服务器
    public void upLoadFunction() throws JSONException {
        labelIdList.clear();
        dishclothIdList.clear();

        for(String str:epcList){
            if((str.charAt(0)=='1'&&str.charAt(1)=='2')||(str.charAt(0)=='2'&&str.charAt(1)=='3')){
                dishclothIdList.add(str);
            }
            else{
                labelIdList.add(str);
            }
        }

        JSONObject data=new JSONObject();
        if(dishclothIdList.isEmpty()){
            data.put("braceletId",reader_name);
            data.put("labelId",labelIdList);
            data.put("dishclothId"," ");
            data.put("pairStatus",2);
        }else {
            data.put("braceletId",reader_name);
            data.put("labelId",labelIdList);
            data.put("dishclothId",dishclothIdList);
            data.put("pairStatus",ismatch);
        }

        httpRequest("/receive/stoolDishcloth", data);
    }

    public void httpRequest(String url,final JSONObject data){
        String base="http://118.31.175.105:9123";
        JsonObjectRequest jsonObjectRequest = new JsonObjectRequest
                (Request.Method.POST, base+url, data, new Response.Listener<JSONObject>() {

                    @Override
                    public void onResponse(JSONObject response) {
                        try {
                            Boolean statusCode = response.getBoolean("data");
                            if (statusCode) {
                                Toast.makeText(BleMainActivity.this, "上传已完成", Toast.LENGTH_SHORT).show();
                                uploadAgain=false;
                            } else {
                                Toast.makeText(BleMainActivity.this, "上传出错，请重试", Toast.LENGTH_SHORT).show();
//                                playHint(R.raw.net_err);
                                uploadAgain=true;
                            }
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                    }
                }, new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        Toast.makeText(BleMainActivity.this, "上传出错，请重试", Toast.LENGTH_SHORT).show();
//                        playHint(R.raw.net_err);
                        uploadAgain=true;
                    }
                });
        // 这里直接复制了官方文档的Volley例程的操作方法
        MySingleton.getInstance(BleMainActivity.this).addToRequestQueue(jsonObjectRequest);
        //mQueue.add(jsonObjectRequest);
    }
}
