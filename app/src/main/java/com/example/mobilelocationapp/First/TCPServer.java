package com.example.mobilelocationapp.First;

import android.content.Context;
import android.content.Intent;
import android.util.Log;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;

class TCPServer implements Runnable {
    public static final String TAG = "MyTCP";
    public static final String RECEIVE_ACTION = "GetTCPReceive";
    public static final String RECEIVE_STRING = "ReceiveString";
    public static final String RECEIVE_BYTES = "ReceiveBytes";
    private int port;
    private boolean isOpen;
    private Context context;
    public ArrayList<ServerSocketThread> SST = new ArrayList<>();

    public TCPServer(int port, Context context){
        this.port = port;
        isOpen = true;
        this.context = context;
    }
    //取得开启状态
    public boolean getStatus(){
        return isOpen;
    }
    //关闭服务器
    public void closeServer(){
        isOpen = false;
        for (ServerSocketThread s : SST){
            s.isRun = false;
        }
        SST.clear();
    }

    private Socket getSocket(ServerSocket serverSocket){
        try {
            return serverSocket.accept();
        } catch (IOException e) {
            e.printStackTrace();
            Log.e(TAG, "更新状态");
            return null;
        }
    }

    @Override
    public void run() {
        try {
            ServerSocket serverSocket = new ServerSocket(port);
            //設置Timeout，以便更新裝置連進來的狀況
//            serverSocket.setSoTimeout(2000);
            while (isOpen){
                Log.e(TAG, "检测client...");
                if (!isOpen) break;
                Socket socket = getSocket(serverSocket);
                if (socket != null){
                    //新建线程，监控输入
                    new ServerSocketThread(socket,context);
                }
            }

            serverSocket.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public class ServerSocketThread extends Thread{
        private final Socket socket;
        private PrintWriter printWriter;
        private InputStream inputStream;
        private boolean isRun = true;
        private final Context context;

        ServerSocketThread(Socket socket, Context context){
            this.socket = socket;
            this.context = context;
            String ip = socket.getInetAddress().toString();
            Log.d(TAG, "检测到新的Ip: " + ip);

            try {
//                socket.setSoTimeout(2000);
                OutputStream outputStream = socket.getOutputStream();
                inputStream = socket.getInputStream();
                printWriter = new PrintWriter(outputStream,true);
                start();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        public void sendData(String msg){
            printWriter.print(msg);
            printWriter.flush();
        }

        @Override
        public void run() {
            byte[] buff = new byte[100];
            SST.add(this);
            while (isOpen && !socket.isClosed() && !socket.isInputShutdown()){
                try {
                    int rcvLen;
                    if ((rcvLen = inputStream.read(buff)) != -1 ){
                        String string = new String(buff, 0, rcvLen);
                        Log.e(TAG, "收到消息: " + string);

                        Intent intent = new Intent();
                        intent.setAction(RECEIVE_ACTION);
                        intent.putExtra(RECEIVE_STRING,string);
                        intent.putExtra(RECEIVE_BYTES, buff);
                        context.sendBroadcast(intent);
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }

            try {
                socket.close();
                SST.clear();
                Log.e(TAG, "关闭server");
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }
}