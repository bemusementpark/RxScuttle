package com.bemusementpark.rxscuttle.sample;

import android.os.Bundle;
import android.util.Log;

import com.bemusementpark.rxscuttle.RxActivity;

import io.reactivex.Observable;

public class MainActivity extends RxActivity {

    private static String TAG = MainActivity.class.getSimpleName();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Log.d(TAG, "onCreate");
        Observable.never()
                .takeUntil(lifecycle())
                .subscribe(new OnCompleteLog<>(TAG, "subscription created in onCreate completed, hopefully just before onDestroy()"));
    }

    @Override
    protected void onStart() {
        super.onStart();
        Log.d(TAG, "onStart");
        Observable.never()
                .takeUntil(lifecycle())
                .subscribe(new OnCompleteLog<>(TAG, "subscription created in onStart completed, hopefully just before onStop()"));
    }

    @Override
    protected void onResume() {
        super.onResume();
        Log.d(TAG, "onResume");
        Observable.never()
                .takeUntil(lifecycle())
                .subscribe(new OnCompleteLog<>(TAG, "subscription created in onResume completed, hopefully just before onPause()"));
    }

    @Override
    protected void onPause() {
        super.onPause();
        Log.d(TAG, "onPause");
    }

    @Override
    protected void onStop() {
        super.onStop();
        Log.d(TAG, "onStop");
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        Log.d(TAG, "onDestroy");
    }
}
