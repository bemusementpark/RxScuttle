package com.bemusementpark.rxscuttle.sample;

import android.util.Log;

import io.reactivex.Observer;
import io.reactivex.disposables.Disposable;

class OnCompleteLog<T> implements Observer<T> {

    private String string;
    private String tag;

    OnCompleteLog(String tag, String string) {
        this.tag = tag;
        this.string = string;
    }

    @Override
    public void onSubscribe(Disposable d) {

    }

    @Override
    public void onNext(Object value) {

    }

    @Override
    public void onError(Throwable e) {

    }

    @Override
    public void onComplete() {
        Log.d(tag, string);
    }
}
