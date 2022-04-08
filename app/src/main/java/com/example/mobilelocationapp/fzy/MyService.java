package com.example.mobilelocationapp.fzy;

import android.app.Service;
import android.content.Intent;
import android.os.Binder;
import android.os.IBinder;
import android.util.Log;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class MyService extends Service {

    TCPServer tcpServer;
    ExecutorService exec;
    TcpSlaveServer tcpSlaveServer1;
    TcpSlaveServer tcpSlaveServer2;
    TcpSlaveServer tcpSlaveServer3;

    public class MyBinder extends Binder{
        public MyService getService() {
            return MyService.this;
        }
        public void sendMessageBind(String message, int currRadio) {
            sendMessage(message, currRadio);
        }
        public void createTcpBind() {
            createTcp();
        }
    }

    private void createTcp(){
        Log.v("tag","执行MyService中的createTcp()方法");
//        tcpServer = new TCPServer(port, this);
//        exec = Executors.newCachedThreadPool();
//        exec.execute(tcpServer);

        TcpMasterServer tcpMasterServer = new TcpMasterServer(this);
        tcpSlaveServer1 = new TcpSlaveServer(1102, this);
        tcpSlaveServer2 = new TcpSlaveServer(1103, this);
        tcpSlaveServer3 = new TcpSlaveServer(1104, this);
        exec = Executors.newCachedThreadPool();
        exec.execute(tcpMasterServer);
        exec.execute(tcpSlaveServer1);
        exec.execute(tcpSlaveServer2);
        exec.execute(tcpSlaveServer3);
    }

    private void sendMessage(String message, int currRadio) {
        switch (currRadio) {
            case 1:
                exec.execute(() -> tcpSlaveServer1.inputThread.sendData(message));
                break;
            case 2:
                exec.execute(() -> tcpSlaveServer2.inputThread.sendData(message));
                break;
            case 3:
                exec.execute(() -> tcpSlaveServer3.inputThread.sendData(message));
                break;
        }
        exec.execute(() -> tcpSlaveServer1.inputThread.sendData(message));
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