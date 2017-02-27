package com.bemusementpark.rxscuttle;

import android.app.Activity;

import io.reactivex.ObservableSource;

public class RxActivity extends Activity {
    public ObservableSource<?> lifecycle() {
        return RxScuttle.lifecycle(this);
    }

    public ObservableSource<?> pauses() {
        return RxScuttle.pauses(this);
    }

    public ObservableSource<?> stops() {
        return RxScuttle.stops(this);
    }

    public ObservableSource<?> destroys() {
        return RxScuttle.destroys(this);
    }
}
