package com.example.instrument.bluetooth;

import android.util.Log;

import com.example.instrument.util.EpicParams;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class BlueToothDataManager {
    private static BlueToothDataManager            _INSTANCE;
    private final  String                          TAG                 = "BlueToothDataManager";
    private final  int                             minValidStrLength   = 20;
    private final  int                             minProcessStrLength = 100;
    // capacity data
    private        int                             ChannelNum;
    private        boolean                         hasInit;
    private        ArrayList<ChannelDataProcessor> channelManagers;
    // string buffer data
    private        boolean                         startProcess;
    private        StringBuffer                    recvBuffer;

    public BlueToothDataManager() {
        ChannelNum = 0;
        hasInit = false;
    }

    public static synchronized BlueToothDataManager getInstance() {
        if (_INSTANCE == null) {
            _INSTANCE = new BlueToothDataManager();
        }
        return _INSTANCE;
    }

    public void init(int channelNum) {
        if (channelNum > 0) {
            hasInit = true;
            startProcess = true;
            ChannelNum = channelNum;
            recvBuffer = new StringBuffer();
            channelManagers = new ArrayList<>();
            for (int i = 0; i < channelNum; i++) {
                channelManagers.add(new ChannelDataProcessor());
            }
        } else {
            Log.e(TAG, "init: channel cannot be less than one");
        }
    }

    private void addString(String str) {
        if (!hasInit)
            return;
        if (recvBuffer.length() == 0) {
            int start = str.indexOf('#');
            if (start != -1) {
                str = str.substring(start);
            } else {
                return;
            }
        }
        recvBuffer.append(str);
    }

    public void processString(String str) {
        if (!hasInit || !startProcess)
            return;
        addString(str);
        while (recvBuffer.length() > minProcessStrLength) {
            ArrayList<Double> data;
            if ((data = getNextData()) != null) {
                for (int i = 0; i < ChannelNum; i++) {
                    channelManagers.get(i).addData(data.get(i));
                }
                // TODO 处理数据
            }
        }
    }

    private ArrayList<Double> formatString(String str) {
        try {
            ArrayList<Double> result = new ArrayList<>();
            List<String> tem = Arrays.asList(str.split(","));
            List<String> temValue = Arrays.asList(tem.get(2).split(" "));
            Double[] param = EpicParams.BtValueParams;
            boolean isInitial = true;
            for (String s : temValue.subList(0, temValue.size() - 1)) {
                Double val = Double.parseDouble(s);
                if (!val.equals(-1.36d)) {
                    isInitial = false;
                }
                result.add(val);
            }
            if (isInitial) {
                return null;
            } else
                return result;
        } catch (Exception e) {
            Log.e(TAG, "formatString: Error");
            e.printStackTrace();
            return null;
        }
    }

    private ArrayList<Double> getNextData() {
        int index;
        if (recvBuffer.length() > 0 && (index = recvBuffer.indexOf("#", 1)) != -1) {
            String str = recvBuffer.substring(0, index).replace("\n", "");
            ArrayList<Double> result;
            recvBuffer.delete(0, index);
            if (str.length() > minValidStrLength && (result = formatString(str)) != null && result.size() > 0) {
                return result;
            }
        }
        return null;
    }
}
