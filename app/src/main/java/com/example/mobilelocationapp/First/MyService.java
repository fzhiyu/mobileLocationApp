package com.example.mobilelocationapp.First;

import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.os.Binder;
import android.os.Handler;
import android.os.IBinder;
import android.os.Looper;
import android.util.Log;
import android.widget.Toast;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class MyService extends Service {

    private TCPServer tcpServer;
    private ExecutorService exec;
    private TcpSlaveServer tcpSlaveServer1;
    private TcpSlaveServer tcpSlaveServer2;
    private TcpSlaveServer tcpSlaveServer3;
    private TcpMasterServer tcpMasterServer;
    private final long[] heartTime = {0, 0, 0, 0};
    private int isCreate = 0;

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

    public int getIsCreate() {
        return isCreate;
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
        isCreate = 1;
    }

    //获取最新心跳信息
    public long[] getHeart() {
        heartTime[0] = tcpMasterServer.getHeart();
        heartTime[1] = tcpSlaveServer1.getHeart();
        heartTime[2] = tcpSlaveServer2.getHeart();
        heartTime[3] = tcpSlaveServer3.getHeart();
        return heartTime;
    }

    //获取连接状态
    public String[] getStatus() {
        String[] status = new String[4];
        String master, slave1, slave2, slave3;
        String onLine = "在线";
        String offLine = "离线";
        if (tcpMasterServer != null && tcpMasterServer.getStatus()) {
            master = onLine;
        } else {
            master = offLine;
        }
        if (tcpSlaveServer1 != null && tcpSlaveServer1.getStatus()) {
            slave1 = onLine;
        } else {
            slave1 = offLine;
        }
        if (tcpSlaveServer2 != null && tcpSlaveServer2.getStatus()) {
            slave2 = onLine;
        } else {
            slave2 = offLine;
        }
        if (tcpSlaveServer3 != null && tcpSlaveServer3.getStatus()) {
            slave3 = onLine;
        } else {
            slave3 = offLine;
        }

        status[0] = master;
        status[1] = slave1;
        status[2] = slave2;
        status[3] = slave3;
//        Log.e(TAG, "getStatus: " + status[0] );
        return status;
    }

    //接收关闭信息
    public void closeSocket(int curr) {
        switch (curr) {
            case 0:
                tcpMasterServer.closeSocket();
            case 1:
                tcpSlaveServer1.closeSocket();
            case 2:
                tcpSlaveServer2.closeSocket();
            case 3:
                tcpSlaveServer3.closeSocket();
        }
    }

    //发送信息
    private void sendMessage(String message, int currRadio, Context context) {
        switch (currRadio) {
            case 0:
                if (tcpMasterServer.getInputThread() != null) {
                    exec.execute(() -> tcpMasterServer.getInputThread().sendData(message));
                } else {
                    //避免在主线程之外使用Toast
                    new Handler(Looper.getMainLooper()).post(new Runnable() {
                        @Override
                        public void run() {
                            Toast.makeText(context, "主车未连接", Toast.LENGTH_SHORT).show();
                        }
                    });
                }
                break;
            case 1:
                if (tcpSlaveServer1.getInputThread() != null) {
                    exec.execute(() -> tcpSlaveServer1.getInputThread().sendData(message));
                } else {
                    new Handler(Looper.getMainLooper()).post(new Runnable() {
                        @Override
                        public void run() {
                            Toast.makeText(context, "从车一未连接", Toast.LENGTH_SHORT).show();
                        }
                    });
                }
                break;
            case 2:
                if (tcpSlaveServer2.getInputThread() != null) {
                    exec.execute(() -> tcpSlaveServer2.getInputThread().sendData(message));
                } else {
                    new Handler(Looper.getMainLooper()).post(new Runnable() {
                        @Override
                        public void run() {
                            Toast.makeText(context, "从车二未连接", Toast.LENGTH_SHORT).show();
                        }
                    });
                }
                break;
            case 3:
                if (tcpSlaveServer3.getInputThread() != null) {
                    exec.execute(() -> tcpSlaveServer3.getInputThread().sendData(message));
                } else {
                    new Handler(Looper.getMainLooper()).post(new Runnable() {
                        @Override
                        public void run() {
                            Toast.makeText(context, "从车三未连接", Toast.LENGTH_SHORT).show();
                        }
                    });
                }
                break;
            default:
                break;
        }
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