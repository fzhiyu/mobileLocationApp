package com.example.mobilelocationapp;

import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.app.Activity;
import android.content.Context;
import android.graphics.Color;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.View;
import android.widget.CompoundButton;
import android.widget.LinearLayout;
import android.widget.Switch;
import android.widget.Toast;

import com.example.mobilelocationapp.chart.CarList;
import com.example.mobilelocationapp.chart.ChartService;
import com.example.mobilelocationapp.chart.RealPoint;
import com.example.mobilelocationapp.chart.TargetPoint;
import com.example.mobilelocationapp.utils.Tools;

import org.achartengine.GraphicalView;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.Iterator;
import java.util.List;
import java.util.Random;
import java.util.SimpleTimeZone;
import java.util.Timer;
import java.util.TimerTask;

public class ThirdActivity extends AppCompatActivity {

    private LinearLayout lLayout_X_Error, lLayout_Y_Error;//存放表格的线性布局
    private GraphicalView XView, YView;//X，Y图表
    private ChartService XService, YService;
    private Timer timer;

    private Switch sw_slave_1, sw_slave_2, sw_slave_3;

    private CarList[] carLists;
    private List<Double> xList;

    private Context mContext;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.third_layout);

        mContext = this;

        initView();

        test();//测试, 用完删除

        setChart();
    }

    public void initView(){
        //布局
        lLayout_X_Error = findViewById(R.id.llayout_X_error);
        lLayout_Y_Error = findViewById(R.id.llayout_Y_error);

        //开关
        sw_slave_1 = findViewById(R.id.sw_slave_1);
        sw_slave_2 = findViewById(R.id.sw_slave_2);
        sw_slave_3 = findViewById(R.id.sw_slave_3);
    }

    public void test(){
        carLists = new CarList[2];

        carLists[0] = new CarList(1102);
        carLists[1] = new CarList(1103);

        for (int i = 0; i < 1000; i++) {
            double x = Math.random() * 4 + 1;
            Random random = new Random(1000000202);
            double y = random.nextDouble();
            //double y = Math.random() * 4 + 1;

            carLists[0].addRealPoint(x, y);
            carLists[0].addTargetPoint(x, y);

            carLists[1].addRealPoint(x, y);
            carLists[1].addTargetPoint(x, y);
        }

        xList = new ArrayList<>();
        for (int i = 0; i < carLists[0].getXError().size(); i++) {
            xList.add(i * 1.0);
        }
    }

    public void setChart(){
        //误差曲线
        XService = new ChartService(this);
        XService.setMultipleSeriesDataset("X轴实时误差");
        XService.setMultipleSeriesRenderer(50, 5, "x轴实时误差", "时间", "误差",
                Color.RED, Color.RED, Color.RED, Color.BLACK);
        XView = XService.getGraphicalView();

        YService = new ChartService(this);
        YService.setMultipleSeriesDataset("Y轴实时误差");
        YService.setMultipleSeriesRenderer(50, 5, "y轴实时误差", "时间", "误差",
                Color.RED, Color.RED, Color.RED, Color.BLACK);
        YView = YService.getGraphicalView();

        lLayout_X_Error.addView(XView);
        lLayout_Y_Error.addView(YView);

        XService.updateChart(xList, carLists[0].getXError());
        YService.updateChart(xList, carLists[0].getYError());

//        timer = new Timer();
//        timer.schedule(new TimerTask() {
//            @Override
//            public void run() {
//                handler.sendMessage(handler.obtainMessage());
//            }
//        }, 10, 1000);


    }

//    private int t = 0;
//    private Handler handler = new Handler(){
//        @Override
//        public void handleMessage(@NonNull Message msg) {
//            XService.updateChart(t, Math.random() * 10 - 5);
//            YService.updateChart(t, Math.random() * 10 - 5);
//            t+=1;
//        }
//    };

    public void setSwitch(){
        sw_slave_1.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton compoundButton, boolean b) {
                if (b){
                    //添加曲线
                }else {
                    //关闭曲线
                }
            }
        });
    }

    public void onClick(View view){
        switch (view.getId()){
            case R.id.btn_save:

                String dirPath = setDir(ThirdActivity.this);

                //保存数据到本地
                boolean b = saveFile(dirPath);

                if (b){
                    Toast.makeText(this, "保存成功", Toast.LENGTH_SHORT).show();
                }
                break;
        }
    }

    public String setDir(Activity activity){
        String path = "";
        if (Environment.getExternalStorageState().equals(Environment.MEDIA_MOUNTED)){//有sd卡
            path = Environment.getExternalStorageDirectory() + "/";
        }else {
            path = Environment.getDataDirectory() + "/";
        }

        path = path + "com.carData/";

        Tools.verifyStoragePermissions(ThirdActivity.this);//请求写入的权限

        createDir(path);//创建文件夹

        return path;//返回绝对路径, /storage/emulated/0/com.carData
    }

    public boolean createDir(String path){
        File dir = new File(path);
        if (!dir.exists() || !dir.isDirectory()){
            boolean isCreate = dir.mkdir();
            return  isCreate;
        }

        return true;
    }

    public boolean saveFile(String path){
        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy_MM_dd HH_mm_ss");
        Date date = new Date();
        String fileName = dateFormat.format(date) + ".dat";

        File file = new File(path, fileName);

        try (
                BufferedWriter out = new BufferedWriter(
                        new OutputStreamWriter(
                                new FileOutputStream(file), "UTF-8"));
        ) {
            for (int i = 0; i < carLists.length; i++) {
                out.write(carLists[i].getPort() + "\r\n");
                out.write("RealDate: ");

                for (Iterator<RealPoint> iterator = carLists[i].getRealPointList().iterator(); iterator.hasNext(); ) {
                    String s = iterator.next().toString();
                    out.write(s);
                    out.write(" ");
                }

                out.write("\r\n");
                out.write("Target: ");
                for (Iterator<TargetPoint> iterator = carLists[i].getTargetPointList().iterator(); iterator.hasNext(); ) {
                    String s = iterator.next().toString();
                    out.write(s);
                    out.write(" ");
                }

                out.write("\r\n");
                out.write("XError: ");
                for (Double d: carLists[i].getXError()) {
                    out.write(d + " ");
                }

                out.write("\r\n");
                out.write("YError: ");
                for (Double d: carLists[i].getYError()) {
                    out.write(d + " ");
                }

                out.write("\r\n");
                out.write("\r\n");
            }

            return true;

        } catch (FileNotFoundException e) {
            e.printStackTrace();
            return false;
        } catch (IOException e) {
            e.printStackTrace();
            return false;
        }
    }
}