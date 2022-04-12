package com.example.mobilelocationapp.fzy;

import static android.service.controls.ControlsProviderService.TAG;

import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.os.Binder;
import android.os.IBinder;
import android.util.Log;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class MyService extends Service {

    TCPServer tcpServer;
    ExecutorService exec;
    TcpSlaveServer tcpSlaveServer1;
    TcpSlaveServer tcpSlaveServer2;
    TcpSlaveServer tcpSlaveServer3;
    TcpMasterServer tcpMasterServer;
    String[] status;
    String onLine = "在线";
    String offLine = "离线";

    public class MyBinder extends Binder{
        public MyService getService() {
            return MyService.this;
        }
        public void sendMessageBind(String message, int currRadio, Context context) {
            sendMessage(message, currRadio, context);
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

        tcpMasterServer = new TcpMasterServer(this);
        tcpSlaveServer1 = new TcpSlaveServer(1102, this);
        tcpSlaveServer2 = new TcpSlaveServer(1103, this);
        tcpSlaveServer3 = new TcpSlaveServer(1104, this);
        exec = Executors.newCachedThreadPool();
        exec.execute(tcpMasterServer);
        exec.execute(tcpSlaveServer1);
        exec.execute(tcpSlaveServer2);
        exec.execute(tcpSlaveServer3);
    }

    //获取连接状态
    public String[] getStatus() {
        status = new String[4];
        String master, slave1, slave2, slave3;
        if (tcpMasterServer.getStatus()) {
            master = onLine;
        } else {
            master = offLine;
        }
        if (tcpSlaveServer1.getStatus()) {
            slave1 = onLine;
        } else {
            slave1 = offLine;
        }
        if (tcpSlaveServer2.getStatus()) {
            slave2 = onLine;
        } else {
            slave2 = offLine;
        }
        if (tcpSlaveServer3.getStatus()) {
            slave3 = onLine;
        } else {
            slave3 = offLine;
        }

        status[0] = master;
        status[1] = slave1;
        status[2] = slave2;
        status[3] = slave3;
        return status;
    }

    //发送信息
    private void sendMessage(String message, int currRadio, Context context) {
        switch (currRadio) {
            case 0:
                if (tcpMasterServer.inputThread != null) {
                    exec.execute(() -> tcpMasterServer.inputThread.sendData(message));
                } else {
                    Toast.makeText(context, "主车未连接", Toast.LENGTH_SHORT).show();
                }
                break;
            case 1:
                if (tcpSlaveServer1.inputThread != null) {
                    exec.execute(() -> tcpSlaveServer1.inputThread.sendData(message));
                } else {
                    Toast.makeText(context, "从车一未连接", Toast.LENGTH_SHORT).show();
                }
                break;
            case 2:
                if (tcpSlaveServer2.inputThread != null) {
                    exec.execute(() -> tcpSlaveServer2.inputThread.sendData(message));
                } else {
                    Toast.makeText(context, "从车二未连接", Toast.LENGTH_SHORT).show();
                }
                break;
            case 3:
                if (tcpSlaveServer3.getStatus()) {
                    exec.execute(() -> tcpSlaveServer3.inputThread.sendData(message));
                } else {
                    Toast.makeText(context, "从车三未连接", Toast.LENGTH_SHORT).show();
                }
                break;
            default:
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