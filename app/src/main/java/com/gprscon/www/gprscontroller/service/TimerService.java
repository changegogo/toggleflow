package com.gprscon.www.gprscontroller.service;

import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.os.IBinder;
import android.util.Log;

import com.gprscon.www.gprscontroller.MainActivity;
import com.gprscon.www.gprscontroller.network.ConManager;

import java.util.Timer;
import java.util.TimerTask;

public class TimerService extends Service {
    private final static String TAG = "TimerService";
    private Timer mTimer;
    private ConManager mConnManager;
    private CommandReceiver cmdReceiver;
    private boolean flag;
    public TimerService() {
    }

    @Override
    public IBinder onBind(Intent intent) {
        Log.e(TAG, "onBind: " );
        // TODO: Return the communication channel to the service.
        throw new UnsupportedOperationException("Not yet implemented");
    }

    @Override
    public void onCreate() {
        super.onCreate();
        mConnManager = ConManager.getInstance(getApplicationContext());
        Log.e(TAG, "onCreate: " );

        cmdReceiver =new CommandReceiver();
        flag=true;
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        Log.e(TAG, "onStartCommand:" );
        IntentFilter intentFilter=new IntentFilter();
        intentFilter.addAction("com.stop.service");
        registerReceiver(cmdReceiver, intentFilter);
        doJob();//
        return super.onStartCommand(intent, flags, startId);
    }

    private void doJob() {
        SharedPreferences sp = getSharedPreferences("sp_time", Context.MODE_PRIVATE);
        int after = sp.getInt("afterInt",0);
        int interval = sp.getInt("intervalInt",0);
        if(mTimer != null){
            mTimer.cancel();
        }
        mTimer = new Timer("after");
        TimerTask task = new TimerTask() {
            @Override
            public void run() {
                boolean isOpen = mConnManager.gprsSetter();
                Log.e(TAG, "run: "+isOpen);
            }
        };
        mTimer.schedule(task,after*1000,interval*1000);
    }

    @Override
    public void onStart(Intent intent, int startId) {
        super.onStart(intent, startId);
        Log.e(TAG, "onStart: " );
    }

    //接受广播
    private class CommandReceiver extends BroadcastReceiver {
        @Override
        public void onReceive(Context context, Intent intent) {
            Log.e(TAG, "onReceive: "+ intent.getAction());
            int cmd=intent.getIntExtra("cmd", -1);
            if(cmd== MainActivity.CMD_STOP_SERVICE){//如果等于0
                flag =false;//停止线程
                stopSelf();//停止服务
            }
        }
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        if(mTimer != null){
            mTimer.cancel();
        }
        this.unregisterReceiver(cmdReceiver);// 取消BroadcastReceiver
        Log.e(TAG, "service onDestroy: " );
    }
}
