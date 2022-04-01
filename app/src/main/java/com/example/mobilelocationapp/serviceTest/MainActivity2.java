package com.example.mobilelocationapp.serviceTest;

import androidx.appcompat.app.AppCompatActivity;

import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.ServiceConnection;
import android.os.Bundle;
import android.os.IBinder;
import android.util.Log;
import android.view.View;
import android.widget.EditText;

import com.example.mobilelocationapp.R;

public class MainActivity2 extends AppCompatActivity {
    MyConn myConn;
    MyService.MyBinder myBinder;
    EditText edtReceiveMessage;
    StringBuffer stringBuffer = new StringBuffer();
    MyBroadcast myBroadcast = new MyBroadcast();
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main2);

        edtReceiveMessage = findViewById(R.id.edt_ReceiveMessage);
        IntentFilter intentFilter = new IntentFilter(TCPServer.RECEIVE_ACTION);
        registerReceiver(myBroadcast, intentFilter);
    }
    public  void toparent(View v){
        Log.v("chendandan","to parent page");
        Intent intent=new Intent(MainActivity2.this,MainActivity.class);
        startActivity(intent);
    }
    public void bindClick(View view){
        Log.v("chendandan","bindClick()");
        if(myConn==null){
            myConn=new MyConn();
        }
        Intent intent=new Intent(MainActivity2.this,MyService.class);
        intent.setType("child");
        bindService(intent,myConn,BIND_AUTO_CREATE);
    }

    public void callClick(View view){
        Log.v("chendandan","callClick()");
        myBinder.methodInBinder();
    }
    public void unbindClick(View view){
        Log.v("chendandan","unbindClick()");
        if(myConn!=null){
            unbindService(myConn);
            myConn=null;
        }
    }
    public class MyConn implements ServiceConnection {
        @Override
        public void onServiceConnected(ComponentName name, IBinder iBinder) {
            myBinder = (MyService.MyBinder) iBinder;
            Log.v("chendandan","服务成功绑定,内存地址为："+myBinder.toString());
        }

        @Override
        public void onServiceDisconnected(ComponentName name) {
            Log.v("chendandan","服务成功解绑");
        }
    }

    @Override
    public void onDestroy(){
        Log.v("chendandan","子页面被销毁");
        super.onDestroy();
    }

    private class MyBroadcast extends BroadcastReceiver {
        @Override
        public void onReceive(Context context, Intent intent) {
            String mAction = intent.getAction();
            assert mAction != null;
            /**接收來自UDP回傳之訊息*/
            switch (mAction) {
                case TCPServer.RECEIVE_ACTION:
                    String msg = intent.getStringExtra(TCPServer.RECEIVE_STRING);
                    byte[] bytes = intent.getByteArrayExtra(TCPServer.RECEIVE_BYTES);
                    stringBuffer.append("收到： ").append(msg).append("\n");
                    edtReceiveMessage.setText(stringBuffer);
                    break;

            }
        }
    }
}