package com.example.alarm.bluetooth;

import android.util.Log;

import java.util.ArrayDeque;
import java.util.Deque;

public class ChannelDataProcessor {
    private static final String TAG = "ChannelDataProcessor";
    // algorithm params
    // TODO 需要修改参数
    private static final Double  startActionThreshold = 0.05d;
    private static final Double  maxVolumeValue       = 0.40d;
    /**
     * 一次动作的最小数据量
     */
    private static final Integer minActionSize        = 15;
    /**
     * 一段时间内数据的极差小于该阈值，就可以作为baseline
     */
    private static final double  stableThreshold      = 0.03d;
    private static final int     rollingMeanSize      = 5;
    private static final int     initSize             = 80;
    private final        int    channelId;
    private final Deque<Double> cacheQueue;
    private final Deque<Double> baselineQue;
    private       Double        meanValue;
    private       boolean       hasInit;
    private       boolean       inAction;
    private       int           actionSize;
    private       Double        maxActionValue;
    private       Double        baseLine;

    public ChannelDataProcessor(int id) {
        meanValue = 0d;
        actionSize = 0;
        maxActionValue = 0d;
        cacheQueue = new ArrayDeque<>();
        baselineQue = new ArrayDeque<>();
        inAction = false;
        hasInit = false;
        channelId = id;
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
            if (max - min <= stableThreshold) {
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
        if (inAction) {
            actionSize++;
            maxActionValue = Math.max(maxActionValue, meanValue);
            if (meanValue < (maxActionValue - startActionThreshold) * 2 / 3 && actionSize >= minActionSize) {
                Log.w(TAG, "addDataToList: 检测到" + this.channelId);
                double volume = Math.min(0.5d, (maxActionValue - startActionThreshold) / (maxVolumeValue - startActionThreshold) / 2);
                float vol = 0.5f + Float.parseFloat(Double.toString(volume));
                // TODO 检测到按压动作
                //GuitarActivity.getInstance().playGuitar(this.channelId, vol);
            }
            if (meanValue <= startActionThreshold) { // action end
                clearState();
            }
        } else if (meanValue > startActionThreshold) // action start
            inAction = true;
    }

    public void clearState() {
        actionSize = 0;
        maxActionValue = 0d;
        inAction = false;
    }
}
