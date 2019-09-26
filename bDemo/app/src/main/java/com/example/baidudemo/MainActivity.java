package com.example.baidudemo;


import android.Manifest;
import android.app.Activity;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.widget.EditText;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import com.baidu.speech.EventListener;
import com.baidu.speech.EventManager;
import com.baidu.speech.EventManagerFactory;
import com.baidu.speech.asr.SpeechConstant;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;

public class MainActivity extends Activity implements View.OnTouchListener, EventListener {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        initView();
        // 动态申请权限
        initPermission();
        initEventManager();
    }

    /***************************************************************************************************/
    /*******************************************  语音识别类型 ******************************************/
    /***************************************************************************************************/

    private EventManager mEventManager = null;

    private void initEventManager() {
        mEventManager = EventManagerFactory.create(this, "asr");
        mEventManager.registerListener(this); //  EventListener 中 onEvent方法
    }

    /**
     * 测试参数填在这里
     */
    private void start() {
        Toast.makeText(MainActivity.this, "开始", Toast.LENGTH_SHORT).show();
        txtLog.setText("");

        try {
            JSONObject obj = new JSONObject();
            obj.put(SpeechConstant.VAD_ENDPOINT_TIMEOUT, 2000);
            obj.put(SpeechConstant.ACCEPT_AUDIO_VOLUME, false);
            obj.put(SpeechConstant.OUT_FILE, "/sdcar/baidu/baiduASR/outfile.pcm");
            obj.put(SpeechConstant.ACCEPT_AUDIO_DATA, true);
            obj.put(SpeechConstant.DISABLE_PUNCTUATION, false);

            mEventManager.send(SpeechConstant.ASR_START, obj.toString(), null, 0, 0);

            //展示日志
            StringBuilder info = new StringBuilder();
            info.append("输入参数[").append(System.currentTimeMillis()).append("]").append("\n")
                    .append(obj.toString());
            Log.i("sanbo", info.toString());
            txtLog.append(info);

        } catch (JSONException e) {
            e.printStackTrace();
        }

    }

    private void stop() {
        Toast.makeText(MainActivity.this, "结束", Toast.LENGTH_SHORT).show();
        mEventManager.send(SpeechConstant.ASR_STOP, null, null, 0, 0);
    }


    /**
     * 回调处理
     *
     * @param name
     * @param params
     * @param data
     * @param offset
     * @param length
     */
    @Override
    public void onEvent(String name, String params, byte[] data, int offset, int length) {
        Toast.makeText(MainActivity.this, " 收到回调。。。", Toast.LENGTH_SHORT).show();
        String resultTxt = null;
        if (name.equals(SpeechConstant.CALLBACK_EVENT_ASR_PARTIAL)) {//识别结果参数
            if (params.contains("\"final_result\"")) {//语义结果值
                try {
                    JSONObject json = new JSONObject(params);
                    String result = json.getString("best_result");//取得key的识别结果
                    resultTxt = result;
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
        }
        if (resultTxt != null) {
            Toast.makeText(MainActivity.this, "结果:" + resultTxt, Toast.LENGTH_SHORT).show();
            //resultTxt += "\n";
            txtResult.append(resultTxt);
        }
    }
    /***************************************************************************************************/
    /*******************************************  UI会回调相关的 ******************************************/
    /***************************************************************************************************/

    // 解析结果
    private EditText txtResult;
    // 解析日志
    private TextView txtLog;
    // 说话按钮
    private RelativeLayout btnSpeek;

    private void initView() {
        txtResult = (EditText) findViewById(R.id.txtResult);
        txtLog = (TextView) findViewById(R.id.txtLog);
        btnSpeek = (RelativeLayout) findViewById(R.id.btnSpeek);
        btnSpeek.setOnTouchListener(this);
    }


    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.btnClear:
                txtResult.setText("请说话...");
                break;
            case R.id.btnSend:
                Toast.makeText(this, "还未实现..", Toast.LENGTH_SHORT).show();
                break;
            default:
                break;
        }
    }

    @Override
    public boolean onTouch(View v, MotionEvent event) {
        switch (v.getId()) {
            case R.id.btnSpeek:
                return processSpeak(event);
            default:
                break;
        }
        return false;
    }

    private boolean processSpeak(MotionEvent event) {
        switch (event.getAction()) {
            case MotionEvent.ACTION_DOWN:
                start();
                break;
            case MotionEvent.ACTION_UP:
                stop();
                break;
            default:
                return false;
        }
        return true;
    }

    /***************************************************************************************************/
    /*******************************************  权限申请相关的 ******************************************/
    /***************************************************************************************************/
    /**
     * android 6.0 以上需要动态申请权限
     */
    private void initPermission() {
        String permissions[] = {
                Manifest.permission.RECORD_AUDIO,
                Manifest.permission.ACCESS_NETWORK_STATE,
                Manifest.permission.INTERNET,
                Manifest.permission.READ_PHONE_STATE,
                Manifest.permission.WRITE_EXTERNAL_STORAGE
        };

        ArrayList<String> toApplyList = new ArrayList<String>();

        for (String perm : permissions) {
            if (PackageManager.PERMISSION_GRANTED != ContextCompat.checkSelfPermission(this, perm)) {
                toApplyList.add(perm);
                //进入到这里代表没有权限.

            }
        }
        String tmpList[] = new String[toApplyList.size()];
        if (!toApplyList.isEmpty()) {
            ActivityCompat.requestPermissions(this, toApplyList.toArray(tmpList), 123);
        }

    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        // 此处为android 6.0以上动态授权的回调，用户自行实现。
        for (int i = 0; i < grantResults.length; i++) {
            if (grantResults[i] == PackageManager.PERMISSION_GRANTED) {
                //已授权
                Toast.makeText(this, "授权成功", Toast.LENGTH_SHORT).show();
            } else {
                //拒绝
                Toast.makeText(this, "拒绝权限", Toast.LENGTH_SHORT).show();
            }
        }
    }


}
