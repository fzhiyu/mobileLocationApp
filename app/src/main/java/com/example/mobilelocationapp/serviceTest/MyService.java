package com.example.mobilelocationapp.serviceTest;
import static android.content.ContentValues.TAG;

import android.app.Service;
import android.content.Intent;
import android.os.Binder;
import android.os.Handler;
import android.os.IBinder;
import android.util.Log;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.ServerSocket;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class MyService extends Service {

    int count = 0;
    int port = 8888;
    TCPServer tcpServer;

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
        tcpServer = new TCPServer(port, this);
        ExecutorService exec = Executors.newCachedThreadPool();
        exec.execute(tcpServer);
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
        tcpServer.closeServer();
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