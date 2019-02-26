package tech.oom.idealrecorderdemo;

import android.app.Application;

import tech.oom.idealrecorder.IdealRecorder;


public class MyApplication extends Application {
    @Override
    public void onCreate() {
        super.onCreate();
        IdealRecorder.getInstance().init(this);
    }
}
