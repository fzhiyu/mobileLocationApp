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

public class TcpMasterServer implements Runnable{

    private boolean isOpen = false;
    private final Context context;
    private final String Tag = "fzy";
    private InputThread inputThread;
    private Socket socket;
    private BufferedReader br;
    private long currHeart = 0;
    private boolean flag = false;

    public TcpMasterServer(Context context) {
        //设置服务器ip地址

        this.context = context;
        isOpen = true;
    }

    public InputThread getInputThread() {
        return inputThread;
    }

    @Override
    public void run() {
        try {
            //设置端口
            int port = 1101;
            ServerSocket serverSocket = new ServerSocket(port);
            while (isOpen) {
                socket = getSocket(serverSocket);
                if(socket != null) {
                    inputThread = new InputThread(socket);
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
        return flag;
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
        private PrintWriter printWriter;

        InputThread(Socket socket) {
            this.socket = socket;

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
            flag = true;

            while (!socket.isClosed() && flag) {
                String str = null;

                try {
//                    socket.setSoTimeout(5000);
                    str = br.readLine();
                } catch (IOException e) {
                    flag = false;
                    Log.e(TAG, "process: 客户端异常关闭，与服务端断开连接" );
                    try {
                        br.close();
                    } catch (IOException ioException) {
                        ioException.printStackTrace();
                    }
                    e.printStackTrace();
                }

                if(str != null && str.equals("AT+CIPSTATUS")) {
                    currHeart = System.currentTimeMillis();
//                        Log.e(TAG, "run: " + str );
                    inputThread.sendData("OK\r\n");
                }

                if (System.currentTimeMillis() - currHeart > 4500) {
                    flag = false;
                    Log.e(TAG, "process: 超时2.5秒，与客户端断开连接" );
                }
            }

            try {
                socket.close();
                flag = false;
                Log.e(TAG, "run: 与主车客户端断开连接" );
//                inputThread = null;
            } catch (IOException e) {
                e.printStackTrace();
            }
            super.run();
        }
    }

}
