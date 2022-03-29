package com.example.alarm.activity;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;

import androidx.appcompat.app.AppCompatActivity;

import com.baidu.location.LocationClient;
import com.baidu.location.LocationClientOption;
import com.example.alarm.R;
import com.example.alarm.location.MyLocationListener;

public class MainActivity extends AppCompatActivity {

    private static final String             TAG             = "MainActivity";
    private static       MainActivity       Instance;
    private final        MyLocationListener myListener      = new MyLocationListener();
    public               LocationClient     mLocationClient = null;

    public static MainActivity getInstance() {
        return Instance;
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        Log.i("MainActivity", "onCreateOptionsMenu");
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.main_menu, menu);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        Log.i(TAG, "onOptionsItemSelected: " + item.getTitle());
        String title = item.getTitle().toString();
        if (title.equals(getString(R.string.menu_bluetooth))) {
            Intent intent = new Intent(this, BlueToothActivity.class);
            startActivity(intent);
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Instance = this;

        //声明LocationClient类
        mLocationClient = new LocationClient(getApplicationContext());
        //注册监听函数
        mLocationClient.registerLocationListener(myListener);
        //设置定位参数
        setLocationOption();

        findViewById(R.id.button110).setOnClickListener(v -> startAlarm("110"));
        findViewById(R.id.button119).setOnClickListener(v -> startAlarm("119"));
        findViewById(R.id.button120).setOnClickListener(v -> startAlarm("120"));
    }

    void setLocationOption() {
        LocationClientOption option = new LocationClientOption();
        option.setLocationMode(LocationClientOption.LocationMode.Hight_Accuracy);
        //可选，设置定位模式，默认高精度
        //LocationMode.Hight_Accuracy：高精度；
        //LocationMode.Battery_Saving：低功耗；
        //LocationMode.Device_Sensors：仅使用设备；
        //LocationMode.Fuzzy_Locating, 模糊定位模式；v9.2.8版本开始支持，可以降低API的调用频率，但同时也会降低定位精度；
        option.setCoorType("bd09ll");
        //可选，设置返回经纬度坐标类型，默认GCJ02
        //GCJ02：国测局坐标；
        //BD09ll：百度经纬度坐标；
        //BD09：百度墨卡托坐标；
        //海外地区定位，无需设置坐标类型，统一返回WGS84类型坐标
        option.setScanSpan(0);
        //可选，设置发起定位请求的间隔，int类型，单位ms
        //如果设置为0，则代表单次定位，即仅定位一次，默认为0
        //如果设置非0，需设置1000ms以上才有效
        option.setOpenGps(true);
        //可选，设置是否使用gps，默认false
        //使用高精度和仅用设备两种定位模式的，参数必须设置为true
        option.setLocationNotify(false);
        //可选，设置是否当GPS有效时按照1S/1次频率输出GPS结果，默认false
        option.setIgnoreKillProcess(true);
        //可选，定位SDK内部是一个service，并放到了独立进程。
        //设置是否在stop的时候杀死这个进程，默认（建议）不杀死，即setIgnoreKillProcess(true)
        option.SetIgnoreCacheException(false);
        //可选，设置是否收集Crash信息，默认收集，即参数为false
        option.setWifiCacheTimeOut(5 * 60 * 1000);
        //可选，V7.2版本新增能力
        //如果设置了该接口，首次启动定位时，会先判断当前Wi-Fi是否超出有效期，若超出有效期，会先重新扫描Wi-Fi，然后定位
        option.setEnableSimulateGps(false);
        //可选，设置是否需要过滤GPS仿真结果，默认需要，即参数为false
        option.setIsNeedAddress(true);
        //可选，是否需要地址信息，默认为不需要，即参数为false
        //如果开发者需要获得当前点的地址信息，此处必须为true
        option.setNeedNewVersionRgc(true);
        //可选，设置是否需要最新版本的地址信息。默认需要，即参数为true
        option.setIsNeedLocationDescribe(true);
        //可选，是否需要位置描述信息，默认为不需要，即参数为false
        //如果开发者需要获得当前点的位置信息，此处必须为true
        mLocationClient.setLocOption(option);
        //mLocationClient为第二步初始化过的LocationClient对象
        //需将配置好的LocationClientOption对象，通过setLocOption方法传递给LocationClient对象使用
        //更多LocationClientOption的配置，请参照类参考中LocationClientOption类的详细说明
    }

    private void startAlarm(String number) {
        myListener.setNumber(number);
        if (mLocationClient.isStarted())
            mLocationClient.restart();
        else
            mLocationClient.start();
    }

    public void sendMsg(String number, String msg) {
        Intent sendIntent = new Intent(Intent.ACTION_SENDTO);
        sendIntent.setData(Uri.parse("smsto:" + number));
        sendIntent.putExtra("sms_body", msg);
        startActivity(sendIntent);
    }
}