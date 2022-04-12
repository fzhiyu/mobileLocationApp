package com.example.mobilelocationapp.fzy;

import static android.service.controls.ControlsProviderService.TAG;

import android.content.Context;
import android.content.Intent;
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
            status = !socket.isClosed();
//            Log.e(TAG, "getStatus: " + status );
        } else {
            status = false;
        }
        return status;
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
            Log.e(TAG, "更新状态");
            return null;
        }
    }

    //线程 处理输入
    public class InputThread extends Thread{
        private final Socket socket;
        private Context context;
        private InputStream inputStream;

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
            if (printWriter != null) {
                printWriter.print(msg);
                printWriter.flush();
            }
        }

        @Override
        public void run() {
            boolean flag = true;

            while (!socket.isClosed() && flag) {
                String str = null;
                try {
                    str = br.readLine();
                } catch (IOException e) {
                    try {
                        br.close();
                    } catch (IOException ioException) {
                        ioException.printStackTrace();
                    }
                    e.printStackTrace();
                }
                if (str != null && str.equals("AT+CIPSTATUS")) {
                    Log.e(TAG, "run: " + str );
                    inputThread.sendData("OK\r\n");
                }
                if (str != null && str.charAt(0) == 'V') {
                    Intent intent = new Intent();
                    String action = "get" + port;
                    intent.setAction(action);
                    intent.putExtra("V_actual", str);
                    intent.putExtra("port", port);
                    context.sendBroadcast(intent);
                }

                if (str != null && !str.equals("")){
                    String[] strings = str.split(" "); //将字符串切分开

                    if (strings[0].compareToIgnoreCase("target") == 0){//收到类型为target的消息
                        Intent intent = new Intent();
                        String action = "get" + port;
                        intent.setAction(action);
                        intent.putExtra("target", str);
                        intent.putExtra("port", port);
                        context.sendBroadcast(intent);
                    }
                }
            }

            try {
                socket.close();
                Log.e(TAG, "run: 从车服务器关闭" );
            } catch (IOException e) {
                e.printStackTrace();
            }
            super.run();
        }
    }



}
