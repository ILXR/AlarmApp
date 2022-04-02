package com.example.alarm.bluetooth;

import com.example.alarm.util.EpicParams;

import java.util.ArrayDeque;
import java.util.Deque;

public class ChannelDataProcessor {
    private static final String TAG = "ChannelDataProcessor";

    // algorithm params
    private static final Double startActionThreshold = EpicParams.startActionThreshold;
    private static final int    rollingMeanSize      = 5;
    private static final int    initSize             = 100;
    private static final double stableThreshold      = 0.05d;

    private final Deque<Double> cacheQueue;
    private final Deque<Double> baselineQue;
    private       Double        meanValue;
    private       boolean       startRecordValue;
    private       boolean       hasInit;
    private       boolean       inAction;
    private       Double        maxActionValue;
    private       Double        baseLine;

    public ChannelDataProcessor() {
        meanValue = 0d;
        maxActionValue = 0d;
        cacheQueue = new ArrayDeque<>();
        baselineQue = new ArrayDeque<>();
        inAction = false;
        hasInit = false;
        startRecordValue = false;
    }

    public void addData(Double data) {
        baselineQue.offerLast(data);
        if (baselineQue.size() > initSize) {
            baselineQue.pollFirst();
            double sum = 0d, max = Double.MIN_VALUE, min = Double.MAX_VALUE;
            for (Double item : baselineQue) {
                max = Math.max(max, item);
                min = Math.min(min, item);
                sum += item;
            }
            if (!inAction && max - min <= stableThreshold) {
                // 当正在采集动作数据时，不应该修改baseline，否则会将峰值作为baseline
                hasInit = true;
                baseLine = sum / initSize;
            }
        }
        if (hasInit) {
            data -= baseLine;
            addDataToQueue(data);
            addDataToList();
        }
    }

    public boolean isInAction() {
        return inAction;
    }


    private void addDataToQueue(Double data) {
        int size = cacheQueue.size();
        if (size == rollingMeanSize) {
            Double front = cacheQueue.pollFirst();
            meanValue = meanValue - (front - data) / size;
        } else if (size == 0) {
            meanValue = data;
        } else {
            meanValue = (meanValue * size + data) / (size + 1);
        }
        cacheQueue.addLast(data);
    }

    private void addDataToList() {
        if (startRecordValue) {
            maxActionValue = Math.max(maxActionValue, meanValue);
        }
        if (meanValue > startActionThreshold && !inAction)
            inAction = true;
        if (meanValue <= startActionThreshold && inAction) {
            inAction = false;
        }
    }

    public void startRecord() {
        startRecordValue = true;
    }

    public Double getMaxActionValue() {
        return maxActionValue;
    }

    public void clearState() {
        maxActionValue = 0d;
        inAction = false;
        startRecordValue = false;
    }
}
