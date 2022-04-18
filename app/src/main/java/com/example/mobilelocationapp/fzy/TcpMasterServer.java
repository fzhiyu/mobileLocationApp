package com.example.mobilelocationapp.fzy;

import static android.service.controls.ControlsProviderService.TAG;

import android.content.Context;
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

public class TcpMasterServer implements Runnable{

    //设置服务器ip地址
    String host = "10.112.248.255";
    //设置端口
    private final int port = 1101;
    boolean isOpen = false;
    private Context context;
    String Tag = "fzy";
    InputThread inputThread;
    Socket socket;
    BufferedReader br;
    long currHeart = 0;
    boolean flag;

    public TcpMasterServer(Context context) {
        this.host = host;

        this.context = context;
        isOpen = true;
    }

    ServerSocket serverSocket;

    @Override
    public void run() {
        try {
            serverSocket = new ServerSocket(port);
            while (isOpen) {
                socket = getSocket(serverSocket);
                if(socket != null) {
                    inputThread = new InputThread(socket, context);
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
            Log.e(TAG, "更新状态");
            return null;
        }
    }

    //获取状态
    public boolean getStatus() {
        boolean status;
        if (socket != null) {
            status = !socket.isClosed();
//            Log.e(TAG, "getStatus: " + status );
        } else {
            status = false;
        }
        return status;
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
    public class InputThread extends Thread {
        private final Socket socket;
        private Context context;
        private InputStream inputStream;
        private PrintWriter printWriter;

        InputThread(Socket socket, Context context) {
            this.socket = socket;
            this.context = context;

            try {
                OutputStream outputStream = socket.getOutputStream();
                inputStream = socket.getInputStream();
                printWriter = new PrintWriter(outputStream, true);
                br = new BufferedReader(new InputStreamReader(inputStream));
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
            flag = true;

            while (!socket.isClosed() && flag) {
                String str = null;
                try {
                    try {
                        str = br.readLine();
                    } catch (IOException e) {
                        inputStream.close();
                        e.printStackTrace();
                    }

                    if(str != null && str.equals("AT+CIPSTATUS")) {
                        currHeart = System.currentTimeMillis();
//                        Log.e(TAG, "run: " + str );
                        inputThread.sendData("OK\r\n");
                    }

                    if (System.currentTimeMillis() - currHeart > 2500) {
                        flag = false;
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }

            try {
                socket.close();
                Log.e(TAG, "run: 与客户端断开连接" );
                inputThread = null;
            } catch (IOException e) {
                e.printStackTrace();
            }
            super.run();
        }
    }

}
