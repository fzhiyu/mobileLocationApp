package com.example.mobilelocationapp.serviceTest;
import android.app.Service;
import android.content.Intent;
import android.os.Binder;
import android.os.Handler;
import android.os.IBinder;
import android.util.Log;

public class MyService extends Service {

    int count = 0;

    class MyBinder extends Binder{
        public void methodInBinder(){
            Log.v("chendandan","执行MyBinder中的methodBinder()方法");
            method();
        }
    }

    public void method(){
        Log.v("chendandan","执行MyService中的method()方法");
        count++;
        Log.e("de", "method: " + count );
    }

    @Override
    public void onCreate(){
        Log.v("chendandan","创建服务，执行onCreate()方法");
        super.onCreate();
    }
    @Override
    public IBinder onBind(Intent intent) {
        // TODO: Return the communication channel to the service.
        Log.v("chendandan","绑定服务,执行onBind()方法");
        return new MyBinder();
    }
    @Override
    public boolean onUnbind(Intent intent){
        Log.v("chendandan","解绑服务，执行onUnbind()方法");
        return super.onUnbind(intent);
    }
    @Override
    public int onStartCommand(Intent intent,int flags,int startId){
        Log.v("chendandan","call onStartCommand()");
        return super.onStartCommand(intent,flags,startId);
    }
    @Override
    public void onDestroy(){
        Log.v("chendandan","call Destroy...");
    }
}