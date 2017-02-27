package com.bemusementpark.rxscuttle;

import android.app.Activity;
import android.app.Application;
import android.os.Build;
import android.os.Bundle;
import android.os.Looper;
import android.support.annotation.NonNull;

import java.util.Map;
import java.util.WeakHashMap;

import io.reactivex.Observable;
import io.reactivex.ObservableSource;
import io.reactivex.functions.Function;
import io.reactivex.functions.Predicate;
import io.reactivex.subjects.PublishSubject;

import static com.bemusementpark.rxscuttle.RxScuttle.Event.CREATE;
import static com.bemusementpark.rxscuttle.RxScuttle.Event.DESTROY;
import static com.bemusementpark.rxscuttle.RxScuttle.Event.PAUSE;
import static com.bemusementpark.rxscuttle.RxScuttle.Event.RESUME;
import static com.bemusementpark.rxscuttle.RxScuttle.Event.START;
import static com.bemusementpark.rxscuttle.RxScuttle.Event.STOP;
import static io.reactivex.Observable.just;

public class RxScuttle {

    public enum Event {
        CREATE, START, RESUME, PAUSE, STOP, DESTROY
    }

    @NonNull
    private static final Map<Activity, RxScuttle> map = new WeakHashMap<>();

    @NonNull
    private final Application application;

    @NonNull
    private PublishSubject<Event> subject = PublishSubject.create();

    private RxScuttle(@NonNull Activity activity) {
        application = activity.getApplication();
        // Only register for callbacks if Activity is not destroyed.
        // We can only check on API 17+
        if (!(Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR1
                && activity.isDestroyed())) {
            application.registerActivityLifecycleCallbacks(new Callbacks());
        }
    }

    @NonNull
    public static RxScuttle with(@NonNull Activity activity) {
        if (Looper.myLooper() != Looper.getMainLooper()) {
            throw new IllegalStateException("Expected to be called on the main thread but was "
                    + Thread.currentThread().getName());
        }
        RxScuttle scuttle = map.get(activity);
        if (scuttle == null) {
            scuttle = new RxScuttle(activity);
            map.put(activity, scuttle);
        }
        return scuttle;
    }

    @NonNull
    public Observable<?> events(@NonNull final Event event) {
        return subject.filter(new Predicate<Event>() {
            @Override
            public boolean test(Event lastEvent) throws Exception {
                return lastEvent == event;
            }
        });
    }

    @NonNull
    public Observable<?> lifecycle() {
        return subject.take(1)
                .switchMap(new Function<Event, ObservableSource<?>>() {
                    @Override
                    public ObservableSource<?> apply(Event event) throws Exception {
                        // As lifecycle() could be called on a freshly created RxScuttle wrapping
                        // an Activity that is part-way through its lifecycle, we don't know what
                        // state of the lifecycle we are in until the next lifecycle event.
                        // Hence the following switch may not be exactly what you are expecting.
                        switch (event) {
                            case START: // was created
                                return events(DESTROY);
                            case RESUME: // was started
                                return events(STOP);
                            case PAUSE: // was resumed, now it's already paused
                                return just(PAUSE);
                            case STOP: // was started, now it's already stopped
                                return just(STOP);
                            case CREATE: // shouldn't happen
                            case DESTROY: // was created, now it's already destroyed
                            default:
                                return just(DESTROY);
                        }
                    }
                });
    }

    @NonNull
    public static Observable<?> pauses(Activity activity) {
        return RxScuttle.with(activity).events(PAUSE);
    }

    @NonNull
    public static Observable<?> stops(Activity activity) {
        return RxScuttle.with(activity).events(STOP);
    }

    @NonNull
    public static Observable<?> destroys(Activity activity) {
        return RxScuttle.with(activity).events(DESTROY);
    }

    @NonNull
    public static Observable<?> lifecycle(Activity activity) {
        return RxScuttle.with(activity).lifecycle();
    }

    private class Callbacks implements Application.ActivityLifecycleCallbacks {
        @Override
        public void onActivityCreated(@NonNull Activity activity, @NonNull Bundle bundle) {
            subject.onNext(CREATE);
        }

        @Override
        public void onActivityStarted(@NonNull Activity activity) {
            subject.onNext(START);
        }

        @Override
        public void onActivityResumed(@NonNull Activity activity) {
            subject.onNext(RESUME);
        }

        @Override
        public void onActivityPaused(@NonNull Activity activity) {
            subject.onNext(PAUSE);
        }

        @Override
        public void onActivityStopped(@NonNull Activity activity) {
            subject.onNext(STOP);
        }

        @Override
        public void onActivitySaveInstanceState(@NonNull Activity activity, @NonNull Bundle bundle) {

        }

        @Override
        public void onActivityDestroyed(@NonNull Activity activity) {
            subject.onNext(DESTROY);
            application.unregisterActivityLifecycleCallbacks(this);
        }
    }
}
