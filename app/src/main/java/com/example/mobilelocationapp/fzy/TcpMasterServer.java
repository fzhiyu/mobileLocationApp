package com.example.mobilelocationapp.fzy;

import static android.service.controls.ControlsProviderService.TAG;

import android.content.Context;
import android.util.Log;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.net.ServerSocket;
import java.net.Socket;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.LinkedList;
import java.util.List;

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
                try {
                    int messageLen = -2;
                    try {
                        messageLen = inputStream.read(buff);
                    } catch (IOException e) {
                        inputStream.close();
                        e.printStackTrace();
                    }

                    if(messageLen != -1) {
                        String message = new String(buff, 0, messageLen);

                        if (message.equals("AT+CIPSTATUS")) {
                            inputThread.sendData("OK\r\n");
                            long currentTime = System.currentTimeMillis();
                            timeList.add(currentTime);
                            int len = timeList.size();
                            if (len > 1) {
                                if (timeList.get(len - 1) - timeList.get(len - 2) > 2500) {
                                    Log.e(TAG, "连接断开");
                                    flag = false;
                                }
                            }
                            if (len == 3) {
                                timeList.removeFirst();
                            }
                        }
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }

            try {
                socket.close();
                inputThread = null;
            } catch (IOException e) {
                e.printStackTrace();
            }
            super.run();
        }
    }

}
