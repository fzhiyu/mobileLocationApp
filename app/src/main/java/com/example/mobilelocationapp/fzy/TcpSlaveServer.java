package com.example.mobilelocationapp.fzy;

import static android.service.controls.ControlsProviderService.TAG;

import android.content.Context;
import android.content.Intent;
import android.os.Handler;
import android.util.Log;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.net.ServerSocket;
import java.net.Socket;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.LinkedList;
import java.util.List;
import java.util.Stack;

public class TcpSlaveServer implements Runnable{

    //设置服务器ip地址
    String host = "10.112.248.255";
    //设置端口
    private int port;
    boolean isOpen = false;
    private Context context;
    String Tag = "fzy";
    InputThread inputThread;
    BufferedReader br;
    private PrintWriter printWriter;
    ServerSocket serverSocket;
    Socket socket;
    boolean flag = false;
    long currHeart = 0;

    public TcpSlaveServer(int port, Context context) {
        this.host = host;
        this.port = port;
        this.context = context;
        isOpen = true;
    }

    //获取连接状态
    public boolean getStatus() {
        boolean status;
        if (socket != null) {
            // 在线为true, 离线为false
            status = !socket.isClosed();
//            Log.e(TAG, "getStatus: " + flag );
        } else {
            status = false;
        }
        return flag;
    }

    @Override
    public void run() {
        try {
            serverSocket = new ServerSocket(port);
            while (isOpen) {
                socket = getSocket(serverSocket);
                if(socket != null) {
                    inputThread = new InputThread(socket, context);
//                    new HeartThread();
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private Socket getSocket(ServerSocket serverSocket) {
        try {
            return serverSocket.accept();
        } catch (IOException e) {
            e.printStackTrace();
//            Log.e(TAG, "更新状态");
            return null;
        }
    }

    //获取最新的心跳的时间
    public long getHeart() {
        return currHeart;
    }

    //关闭socket
    public void closeSocket() {
        flag = false;
    }

    //线程 处理输入
    public class InputThread extends Thread{
        private final Socket socket;
        private final Context context;

        InputThread(Socket socket, Context context) {
            this.socket = socket;
            this.context = context;

            try {
                OutputStream outputStream = socket.getOutputStream();
                InputStream inputStream = socket.getInputStream();
                printWriter = new PrintWriter(outputStream, true);
                br = new BufferedReader(new InputStreamReader(inputStream));
                start();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        public void sendData(String msg){
            if (printWriter != null) {
                printWriter.print(msg);
                printWriter.flush();
            }
        }

        @Override
        public void run() {
            process();

            try {
                socket.close();
                flag = false;
                Log.e(TAG, "run: 与从车客户端断开连接" );
            } catch (IOException e) {
                e.printStackTrace();
            }
            super.run();
        }

        private void process() {
            flag = true;
            while (!socket.isClosed() && flag) {
                String str = null;
                try {
                    socket.setSoTimeout(5000);
                    str = br.readLine();
                } catch (IOException e) {
                    flag = false;
                    Log.e(TAG, "process: 客户端异常关闭，与服务端断开连接" );
                    try {
                        br.close();
//                        flag = false;
                    } catch (IOException ioException) {
                        ioException.printStackTrace();
                    }
                    e.printStackTrace();
                }
                if (str != null && str.equals("AT+CIPSTATUS")) {
                    currHeart = System.currentTimeMillis();
                    //                    Log.e(TAG, "run: " + str );
                    inputThread.sendData("OK\r\n");
                }
                if (System.currentTimeMillis() - currHeart > 2500) {
                    flag = false;
                    Log.e(TAG, "process: 超时2.5秒，与客户端断开连接" );
                }
                if (str != null && str.charAt(0) == 'V') {
                    Intent intent = new Intent();
                    String action = "get" + port;
                    intent.setAction(action);
                    intent.putExtra("V_actual", str);
                    intent.putExtra("port", port);
                    context.sendBroadcast(intent);
                }

                if (str != null && !str.equals("")) {
                    String[] strings = str.split(" "); //将字符串切分开

                    if (strings[0].compareToIgnoreCase("target") == 0) {//收到类型为target的消息
                        Intent intent = new Intent();
                        String action = "get" + port;
                        intent.setAction(action);
                        intent.putExtra("target", str);
                        intent.putExtra("port", port);
                        context.sendBroadcast(intent);
                    }
                }
            }
        }
    }



}
