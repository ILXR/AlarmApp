package com.example.alarm.util;

import android.app.Application;
import android.widget.Toast;

public class MyApplication extends Application {
    private static final String        TAG = "MyApplication";
    private static       MyApplication _INSTANCE;
    private              Toast         toast;

    public MyApplication() {
        _INSTANCE = this;
    }

    public static MyApplication getInstance() {
        return _INSTANCE;
    }

    public void toast(String str) {
        if (toast != null)
            toast.cancel();
        toast = Toast.makeText(this, str, Toast.LENGTH_SHORT);
        toast.show();
    }
}
