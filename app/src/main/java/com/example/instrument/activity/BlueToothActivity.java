package com.example.instrument.activity;

import android.Manifest;
import android.bluetooth.BluetoothDevice;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.os.Message;
import android.util.Log;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentPagerAdapter;
import androidx.viewpager.widget.ViewPager;

import com.example.instrument.R;
import com.example.instrument.bluetooth.BlueToothManager;
import com.example.instrument.util.EpicParams;
import com.google.android.material.tabs.TabLayout;

import java.util.ArrayList;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

public class BlueToothActivity extends AppCompatActivity {

    final String TAG = "BlueToothActivity";

    TabLayout      tabLayout;
    ViewPager      viewPager;
    MyPagerAdapter pagerAdapter;
    String[]       titleList    = new String[]{"设备列表", "数据传输"};
    List<Fragment> fragmentList = new ArrayList<>();

    DeviceListFragment deviceListFragment;
    DataTransFragment  dataTransFragment;

    public void handleMsg(Message msg) {
        runOnUiThread(() -> {
            switch (msg.what) {
                case EpicParams.MSG_REV_A_CLIENT:
                    Log.e(TAG, "--------- set device name, go to data frag");
                    BluetoothDevice clientDevice = (BluetoothDevice) msg.obj;
                    if ((dataTransFragment != null))
                        dataTransFragment.receiveClient(clientDevice);
                    viewPager.setCurrentItem(1);
                    break;
                case EpicParams.MSG_CONNECT_TO_SERVER:
                    Log.e(TAG, "--------- set device name, go to data frag");
                    BluetoothDevice serverDevice = (BluetoothDevice) msg.obj;
                    dataTransFragment.connectServer(serverDevice);
                    viewPager.setCurrentItem(1);
                    break;
                case EpicParams.MSG_SERVER_REV_NEW:
                case EpicParams.MSG_CLIENT_REV_NEW:
                    String newMsgFromClient = msg.obj.toString();
                    if ((dataTransFragment != null))
                        dataTransFragment.updateDataView(newMsgFromClient, EpicParams.REMOTE);
                    break;
                case EpicParams.MSG_WRITE_DATA:
                    String dataSend = msg.obj.toString();
                    if ((dataTransFragment != null))
                        dataTransFragment.updateDataView(dataSend, EpicParams.ME);
                    if (deviceListFragment != null)
                        deviceListFragment.writeData(dataSend);
                    break;
                case EpicParams.MSG_CONNECT_FAILED:
                    toast("蓝牙连接失败！");
                    break;
                case EpicParams.MSG_CONNECT_SUCCEED:
                    String name = BlueToothManager.getInstance().getDeviceName();
                    toast("蓝牙连接成功，设备" + name);
                    Log.i(TAG, "handleMessage: 蓝牙连接成功，设备" + name);
                    if (name.equals("RC1033")) {
                        new Timer().schedule(new TimerTask() {
                            @Override
                            public void run() {
                                Log.i(TAG, "发送 START CODE");
                                BlueToothManager.getInstance().sendByteArray(EpicParams.START_BYTE_ARRAY);
                            }
                        }, 1000);
                        break;
                    }
            }
        });
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        switch (requestCode) {
            case EpicParams.MY_PERMISSION_REQUEST_CONSTANT:
                if (grantResults[0] == PackageManager.PERMISSION_GRANTED && grantResults[1] == PackageManager.PERMISSION_GRANTED) {
                    // Permission Granted 授予权限
                    //处理授权之后逻辑
                } else {
                    // Permission Denied 权限被拒绝
                    Toast.makeText(this, "权限被禁用", Toast.LENGTH_SHORT).show();
                }
        }
    }

    public void checkBlePermission() {
        if (ContextCompat.checkSelfPermission(this,
                Manifest.permission.ACCESS_COARSE_LOCATION)
                != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this,
                    new String[]{Manifest.permission.ACCESS_COARSE_LOCATION},
                    1);
        } else {
            Log.i("tag", "已申请权限");
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_bluetooth);
        initUI();
        checkBlePermission();
    }


    private void initUI() {
        tabLayout = findViewById(R.id.tab_layout);
        viewPager = findViewById(R.id.view_pager);

        tabLayout.addTab(tabLayout.newTab().setText(titleList[0]));
        tabLayout.addTab(tabLayout.newTab().setText(titleList[1]));

        deviceListFragment = new DeviceListFragment();
        dataTransFragment = new DataTransFragment();
        fragmentList.add(deviceListFragment);
        fragmentList.add(dataTransFragment);

        pagerAdapter = new MyPagerAdapter(getSupportFragmentManager());
        viewPager.setAdapter(pagerAdapter);
        tabLayout.setupWithViewPager(viewPager);
    }

    public void toast(String str) {
        Toast.makeText(this, str, Toast.LENGTH_SHORT).show();
    }

    public class MyPagerAdapter extends FragmentPagerAdapter {

        public MyPagerAdapter(FragmentManager fm) {
            super(fm);
        }

        @Override
        public Fragment getItem(int position) {
            return fragmentList.get(position);
        }

        @Override
        public int getCount() {
            return fragmentList.size();
        }

        @Override
        public CharSequence getPageTitle(int position) {
            return titleList[position];
        }
    }
}
