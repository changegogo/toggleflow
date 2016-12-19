package com.gprscon.www.gprscontroller.network;

import android.content.Context;
import android.net.ConnectivityManager;

import java.lang.reflect.Method;

/**
 * Author: dlw on 2016/12/19 13:47
 * Email: dailongshao@126.com
 */
public class ConManager {
    private ConnectivityManager mCM;
    private static ConManager mInstance;
    private ConManager(Context context){
        mCM = (ConnectivityManager)context.getSystemService(Context.CONNECTIVITY_SERVICE);
    }
    public static synchronized ConManager getInstance(Context context){
        if(mInstance == null)
            mInstance = new ConManager(context);
        return mInstance;
    }

    //打开或关闭GPRS
    public boolean gprsSetter(){
        Object[] argObjects = null;
        boolean isOpen = this.gprsIsOpenMethod("getMobileDataEnabled");
        if(isOpen)
        {
            setGprsEnabled("setMobileDataEnabled", false);
            //System.out.println("关闭");
        }else{
            setGprsEnabled("setMobileDataEnabled", true);
            //System.out.println("开启");
        }

        return !isOpen;
    }

    //检测GPRS是否打开
    public boolean gprsIsOpenMethod(String methodName) {
        Class cmClass       = mCM.getClass();
        Class[] argClasses  = null;
        Object[] argObject  = null;

        Boolean isOpen = false;
        try
        {
            Method method = cmClass.getMethod(methodName, argClasses);

            isOpen = (Boolean) method.invoke(mCM, argObject);
        } catch (Exception e)
        {
            e.printStackTrace();
        }
        return isOpen;
    }

    //开启/关闭GPRS
    private void setGprsEnabled(String methodName, boolean isEnable) {
        Class cmClass       = mCM.getClass();
        Class[] argClasses  = new Class[1];
        argClasses[0]       = boolean.class;

        try
        {
            Method method = cmClass.getMethod(methodName, argClasses);
            method.invoke(mCM, isEnable);
        } catch (Exception e)
        {
            e.printStackTrace();
        }
    }
}
