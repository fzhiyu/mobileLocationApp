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

    public TcpSlaveServer(int port, Context context) {
        this.host = host;
        this.port = port;
        this.context = context;
        isOpen = true;
    }

    ServerSocket serverSocket;

    @Override
    public void run() {
        try {
            serverSocket = new ServerSocket(port);
            while (isOpen) {
                Socket socket = getSocket(serverSocket);
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
    public class InputThread extends Thread {
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
            printWriter.print(msg);
            printWriter.flush();
        }

        @Override
        public void run() {
            byte[] buff = new byte[100];
            LinkedList<Long> timeList= new LinkedList<>();
            boolean flag = true;

            while (!socket.isClosed() && flag) {
                String str = null;
                try {
                    if ((str = br.readLine()) != null) {
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

                } catch (IOException e) {
                    e.printStackTrace();
                }
//                try {
//                    int messageLen = inputStream.read(buff);
//                    if(messageLen != -1) {
//                        String message = new String(buff, 0, messageLen);
//                        Log.e(Tag, "接收消息: " + message);
//                        if (message.equals("AT+CIPSTATUS\r\n")) {
//                            inputThread.sendData("OK\r\n");
//                            long currentTime = System.currentTimeMillis();
//                            timeList.add(currentTime);
//                            int len = timeList.size();
//                            if (len > 1) {
//                                if (timeList.get(len - 1) - timeList.get(len - 2) > 2500) {
//                                    Log.e(TAG, "连接断开");
//                                    flag = false;
//                                }
//                            }
//                            if (len == 3) {
//                                timeList.removeFirst();
//                            }
//                        }
//                        if (message.charAt(0) == 'V') {
//                            Intent intent = new Intent();
//                            intent.setAction("getV");
//                            intent.putExtra("V_actual", message);
//                            context.sendBroadcast(intent);
//                        }
//                    }
//                } catch (IOException e) {
//                    e.printStackTrace();
//                }
            }

            try {
                inputThread = null;
                inputStream.close();
                socket.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
            super.run();
        }
    }



}
