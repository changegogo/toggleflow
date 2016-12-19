package com.gprscon.www.gprscontroller;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Build;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.gprscon.www.gprscontroller.network.ConManager;
import com.gprscon.www.gprscontroller.service.TimerService;

public class MainActivity extends AppCompatActivity
implements View.OnClickListener{
    private final static String TAG = MainActivity.class.getSimpleName();
    private ConManager mConnManager;
    private Button mToggleBtn;//开关流量按钮
    private Button mStartBtn;//开始定时
    private EditText mAfterTimeEt;//开启或者关闭多少分钟之后关闭或者开启
    private EditText mIntervalTimeEt;//间隔多少分钟开启或者关闭
    private Intent serviceIntent;
    private Button mCloseBtn;
    public final static int CMD_STOP_SERVICE = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        if(Build.VERSION.SDK_INT < 21){
            setContentView(R.layout.activity_main);
            initNetwork();
            initView();
            initEvent();
        }else{
            setContentView(R.layout.activity_error);
        }

    }

    private void initEvent() {
        //获取流量是否开启
        boolean isOpen = mConnManager.gprsIsOpenMethod("getMobileDataEnabled");
        //设置控制按钮的显示状态
        setStateToggle(isOpen);
        mToggleBtn.setOnClickListener(this);
        //开始定时
        mStartBtn.setOnClickListener(this);
        //关闭定时
        mCloseBtn.setOnClickListener(this);
    }

    private void initView() {
        mToggleBtn = (Button) findViewById(R.id.togglebutton);
        mStartBtn = (Button) findViewById(R.id.starttimer);
        mAfterTimeEt = (EditText) findViewById(R.id.aftertime);
        mIntervalTimeEt = (EditText) findViewById(R.id.intervaltime);
        mCloseBtn = (Button) findViewById(R.id.closeTimer);

        SharedPreferences sp = getSharedPreferences("sp_time", Context.MODE_PRIVATE);
        int after = sp.getInt("afterInt",1);
        int interval = sp.getInt("intervalInt",1);
        mAfterTimeEt.setText(after+"");
        mIntervalTimeEt.setText(interval+"");
    }

    private void initNetwork() {
        mConnManager = ConManager.getInstance(this);
    }

    public void setStateToggle(boolean isOpen){
        if(isOpen){
            mToggleBtn.setText("关闭流量");
        }else{
            mToggleBtn.setText("开启流量");
        }
        Toast.makeText(MainActivity.this, isOpen?"流量打开":"流量关闭", Toast.LENGTH_SHORT).show();
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()){
            case R.id.togglebutton:
                setStateToggle(mConnManager.gprsSetter());
                break;
            case R.id.starttimer:
                startTimertask();
                break;
            case R.id.closeTimer:
                sendStopServiceBroadcast();
                break;
            default:
                break;
        }
    }

    private void sendStopServiceBroadcast() {
        Log.e(TAG, " sendStopServiceBroadcast" );
        Intent intent=new Intent();
        intent.setAction("com.stop.service");
        intent.putExtra("cmd",CMD_STOP_SERVICE);
        sendBroadcast(intent);
        Toast.makeText(MainActivity.this, "定时器关闭", Toast.LENGTH_SHORT).show();
    }

    /**
     * 开始定时服务
     * */
    private void startTimertask() {
        int afterInt = 0;
        int intervalInt = 0;
        try{
            //多少分钟之后
            String after = mAfterTimeEt.getText().toString().trim();
            afterInt = Integer.parseInt(after);
            //间隔多长时间
            String interval = mIntervalTimeEt.getText().toString().trim();
            intervalInt = Integer.parseInt(interval);
        }catch (IllegalArgumentException e){
            Toast.makeText(MainActivity.this, "请输入数值", Toast.LENGTH_SHORT).show();
            return;
        }

        //存储到本地
        SharedPreferences sp = getSharedPreferences("sp_time", Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sp.edit();
        editor.putInt("afterInt", afterInt);
        editor.putInt("intervalInt", intervalInt);
        editor.commit();

        serviceIntent = new Intent(MainActivity.this, TimerService.class);
        Bundle bundle = new Bundle();
        bundle.putInt("after",afterInt);
        bundle.putInt("interval",intervalInt);
        serviceIntent.putExtras(bundle);
        startService(serviceIntent);
        Toast.makeText(MainActivity.this, "定时器启动", Toast.LENGTH_SHORT).show();
    }

    @Override
    protected void onStart() {
        super.onStart();
    }

    @Override
    protected void onStop() {
        super.onStop();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        Log.e(TAG, "activity onDestroy: " );
    }
}
