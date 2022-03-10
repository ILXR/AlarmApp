package com.example.alarm.util;

import android.app.Application;
import android.content.Intent;
import android.net.Uri;
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

    public void sendMsg(String number, String msg) {
        Intent sendIntent = new Intent(Intent.ACTION_SENDTO);
        sendIntent.setData(Uri.parse("smsto:" + number));
        sendIntent.putExtra("sms_body", msg);
        startActivity(sendIntent);
    }
}
