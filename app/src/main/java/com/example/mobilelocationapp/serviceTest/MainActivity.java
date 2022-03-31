package com.example.mobilelocationapp.serviceTest;

import androidx.appcompat.app.AppCompatActivity;

import android.content.ComponentName;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.Bundle;
import android.os.IBinder;
import android.util.Log;
import android.view.View;

import com.example.mobilelocationapp.R;

public class MainActivity extends AppCompatActivity {
    MyConn myConn;
    MyService.MyBinder myBinder;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_service);

    }
    //跳转到子页面
    public  void tochild(View v){
        Log.v("chendandan","to child page");
        Intent intent=new Intent(MainActivity.this,MainActivity2.class);
        startActivity(intent);
    }
    //绑定服务
    public void bindClick(View view){
        Log.v("chendandan","bindClick()");
        if(myConn==null){
            myConn=new MyConn();
        }
        Intent intent=new Intent(MainActivity.this,MyService.class);
        intent.setType("main");
        bindService(intent,myConn,BIND_AUTO_CREATE);
    }
    //调用服务中的方法
    public void callClick(View view){
        Log.v("chendandan","callClick()");
        myBinder.methodInBinder();
    }
    //解除绑定
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
}