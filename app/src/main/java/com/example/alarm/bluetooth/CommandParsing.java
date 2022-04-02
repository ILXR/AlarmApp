package com.example.alarm.bluetooth;

import android.util.Log;

import com.example.alarm.activity.MainActivity;
import com.example.alarm.util.EpicParams;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Random;


public class CommandParsing {
    private static final String         TAG = "CommandParsing";
    private static       CommandParsing INSTANCE;

    public CommandParsing() {
    }

    public static synchronized CommandParsing getInstance() {
        if (INSTANCE == null) {
            INSTANCE = new CommandParsing();
        }
        return INSTANCE;
    }

    public ActionType commandParse(ArrayList<Double> maxValues, long time, boolean activated) {
        if (activated) {
            //TODO 判断动作类型
            ActionType[] list = new ActionType[]{ActionType.Touch1, ActionType.Touch2, ActionType.Touch3};
            return list[new Random().nextInt(3)];
        } else if (time > EpicParams.crabTime * 1000 && Collections.max(maxValues) >= EpicParams.crabThreshold) {
            Log.i(TAG, "commandParse: 类型为Crab, 激活");
            return ActionType.Crab;
        } else
            return ActionType.Else;
    }

    public void act(ActionType type) {
        if (type == ActionType.Crab || type == ActionType.Else)
            return;
        //TODO 根据不同的动作采取行动
        switch (type) {
            case Touch1: {
                Log.i(TAG, "act: touch1");
                MainActivity.getInstance().startAlarm("110");
                break;
            }
            case Touch2: {
                Log.i(TAG, "act: touch2");
                MainActivity.getInstance().startAlarm("119");
                break;
            }
            case Touch3: {
                Log.i(TAG, "act: touch3");
                MainActivity.getInstance().startAlarm("120");
                break;
            }
        }
    }

    public enum ActionType {
        Touch1,
        Touch2,
        Touch3,
        Crab,
        Else
    }
}
