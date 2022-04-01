package com.example.mobilelocationapp.fzy;

import android.app.Service;
import android.content.Intent;
import android.os.Binder;
import android.os.IBinder;
import android.util.Log;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class MyService extends Service {

    int port = 8888;
    TCPServer tcpServer;
    ExecutorService exec;

    public class MyBinder extends Binder{
        MyService getService() {
            return MyService.this;
        }
        void sendMessageBind(String message) {
            sendMessage(message);
        }
        void createTcpBind() {
            createTcp();
        }
    }

    public void createTcp(){
        Log.v("tag","执行MyService中的createTcp()方法");
        tcpServer = new TCPServer(port, this);
        exec = Executors.newCachedThreadPool();
        exec.execute(tcpServer);
//        tcpServer.run();
    }

    private void sendMessage(String message) {
        exec.execute(() -> tcpServer.SST.get(0).sendData(message));
    }

    @Override
    public void onCreate(){
        Log.v("tag","创建服务，执行onCreate()方法");
        super.onCreate();
    }
    @Override
    public IBinder onBind(Intent intent) {
        Log.v("tag","绑定服务,执行onBind()方法");
        return new MyBinder();
    }
    @Override
    public boolean onUnbind(Intent intent){
        Log.v("tag","解绑服务，执行onUnbind()方法");
        tcpServer.closeServer();
        return super.onUnbind(intent);
    }
    @Override
    public int onStartCommand(Intent intent,int flags,int startId){
        Log.v("tag","call onStartCommand()");
        return super.onStartCommand(intent,flags,startId);
    }
    @Override
    public void onDestroy(){
        Log.v("tag","call Destroy...");
    }
}