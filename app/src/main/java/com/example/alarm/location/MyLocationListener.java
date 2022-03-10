package com.example.alarm.location;

import com.baidu.location.BDAbstractLocationListener;
import com.baidu.location.BDLocation;
import com.example.alarm.activity.MainActivity;

//BDAbstractLocationListener为7.2版本新增的Abstract类型的监听接口
//原有BDLocationListener接口暂时同步保留。具体介绍请参考后文第四步的说明
public class MyLocationListener extends BDAbstractLocationListener {
    private static final String TAG     = "MyLocationListener";
    private              String mNumber = null;

    public void setNumber(String number) {
        mNumber = number;
    }

    @Override
    public void onReceiveLocation(BDLocation location) {
        if (mNumber == null)
            return;
        //此处的BDLocation为定位结果信息类，通过它的各种get方法可获取定位相关的全部结果
        //以下只列举部分获取经纬度相关（常用）的结果信息
        //更多结果信息获取说明，请参照类参考中BDLocation类中的说明
        String address = location.getAddrStr();    //获取详细地址信息
        String describe = location.getLocationDescribe();
        String msg = "这是危急情况下自动发送的短信，我的位置是" + address + "，" + describe + "。现在情况紧急，收到请及时救援！";
        MainActivity.getInstance().sendMsg(mNumber, msg);
        mNumber = null;
    }
}

