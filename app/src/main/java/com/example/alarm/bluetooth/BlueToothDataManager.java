package com.example.alarm.bluetooth;

import android.util.Log;

import com.example.alarm.util.EpicParams;

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
    private        boolean                         anyInAction;
    private        StringBuffer                    recvBuffer;
    // time
    private        long                            startTime, endTime;
    // alarm control
    private boolean activated;

    public BlueToothDataManager() {
        activated = false;
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
            anyInAction = false;
            startProcess = true;
            startTime = endTime = 0;
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

    private boolean anyInAction() {
        boolean inAction = false;
        for (ChannelDataProcessor channel : channelManagers) {
            inAction |= channel.isInAction();
        }
        return inAction;
    }

    private void startRecord() {
        for (ChannelDataProcessor channel : channelManagers) {
            channel.startRecord();
        }
    }

    private void clearState() {
        anyInAction = false;
        startTime = endTime = 0;
        for (ChannelDataProcessor channel : channelManagers) {
            channel.clearState();
        }
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
                boolean tem = anyInAction();
                if (tem && !anyInAction) {
                    anyInAction = true;
                    startRecord();
                    startTime = System.currentTimeMillis();
                    Log.i(TAG, "processString: Action Start");
                } else if (!tem && anyInAction) {
                    endTime = System.currentTimeMillis();
                    Log.i(TAG, "processString: Action End");
                    long time = endTime - startTime;
                    if (time >= EpicParams.actionTime * 1000) {
                        Log.i(TAG, "processString: Valid Data, time: " + (time / 1000) + "s");
                        ArrayList<Double> maxValues = new ArrayList<>();
                        for (ChannelDataProcessor channel : channelManagers)
                            maxValues.add(channel.getMaxActionValue());
                        Log.i(TAG, "processString: channel max data - " + maxValues.toString());
                        CommandParsing.ActionType type = CommandParsing.getInstance().commandParse(maxValues, time, activated);
                        activated = (type == CommandParsing.ActionType.Crab);
                        CommandParsing.getInstance().act(type);
                    } else {
                        Log.i(TAG, "processString: invalid Data");
                    }
                    clearState();
                }
            }
        }
    }

    private ArrayList<Double> formatString(String str) {
        try {
            ArrayList<Double> result = new ArrayList<>();
            List<String> tem = Arrays.asList(str.split(","));
            List<String> temValue = Arrays.asList(tem.get(2).split(" "));
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
