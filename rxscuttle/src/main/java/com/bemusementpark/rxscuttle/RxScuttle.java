package com.bemusementpark.rxscuttle;

import android.app.Activity;
import android.app.Application;
import android.os.Build;
import android.os.Bundle;
import android.os.Looper;
import android.support.annotation.NonNull;
import android.util.Log;

import java.util.HashMap;
import java.util.Map;

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
    private static final String TAG = "RxScuttle";

    public enum Event {
        CREATE, START, RESUME, PAUSE, STOP, DESTROY
    }

    private static final Callbacks callbacks = new Callbacks();

    /**
     * @param activity The Activity to observe
     * @param filter   The event type to filter on
     * @return an Observable that emits {@link Event}s of the specified type when that lifecycle
     * event occurs.
     */
    @NonNull
    public static Observable<Event> events(@NonNull final Activity activity, @NonNull final Event filter) {
        return callbacks.events(activity)
                .filter(new Predicate<Event>() {
                    @Override
                    public boolean test(Event event) throws Exception {
                        return event == filter;
                    }
                });
    }

    /**
     * @return an Observable that emits an {@link Event#PAUSE} when the supplied Activity is
     * paused.
     */
    @NonNull
    public static Observable<Event> pauses(@NonNull Activity activity) {
        return events(activity, PAUSE);
    }

    /**
     * @return an Observable that emits an {@link Event#STOP} when the supplied Activity is
     * stopped.
     */
    @NonNull
    public static Observable<Event> stops(@NonNull Activity activity) {
        return events(activity, STOP);
    }

    /**
     * @return an Observable that emits an {@link Event#DESTROY} when the supplied Activity is
     * destroyed.
     */
    @NonNull
    public static Observable<Event> destroys(@NonNull Activity activity) {
        return events(activity, DESTROY);
    }

    /**
     * @return an Observable that emits the {@link Event} that caused the Activity lifecycle scope
     * at time of subscription to close, and then completes.
     */
    @NonNull
    public static Observable<Event> lifecycle(@NonNull final Activity activity) {
        return callbacks.events(activity)
                .concatMap(new Function<Event, ObservableSource<Event>>() {
                    @Override
                    public ObservableSource<Event> apply(Event event) throws Exception {
                        // As lifecycle() could be called on a freshly created RxScuttle wrapping
                        // an Activity that is part-way through its lifecycle, we don't know what
                        // state of the lifecycle we are in until the next lifecycle events.
                        switch (event) {
                            case CREATE: // hadn't been created yet
                            case START: // was created
                                return destroys(activity);
                            case RESUME: // was started
                                return stops(activity);
                            case DESTROY:
                                return just(DESTROY);
                            default:
                                // otherwise the current events closes the current lifecycle scope
                                return events(activity, event).startWith(event);
                        }
                    }
                });
    }

    private static class Callbacks implements Application.ActivityLifecycleCallbacks {
        @NonNull
        final Map<Activity, PublishSubject<Event>> subjects = new HashMap<>();

        PublishSubject<Event> events(@NonNull Activity activity) {
            checkMainThread();

            // Only register for callbacks if Activity is not destroyed.
            // We can only check on API 17+
            if (!(Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR1
                    && activity.isDestroyed())) {
                Log.e(TAG, "RxScuttle used on Activity that has already been destroyed");
            }

            if (subjects.isEmpty()) {
                activity.getApplication().registerActivityLifecycleCallbacks(this);
            }
            if (!subjects.containsKey(activity)) {
                PublishSubject<Event> subject = PublishSubject.create();
                subjects.put(activity, subject);
                return subject;
            }
            return subjects.get(activity);
        }

        @Override
        public void onActivityCreated(@NonNull Activity activity, @NonNull Bundle bundle) {
            PublishSubject<Event> subject = subjects.get(activity);
            if (subject != null) subject.onNext(CREATE);
        }

        @Override
        public void onActivityStarted(@NonNull Activity activity) {
            PublishSubject<Event> subject = subjects.get(activity);
            if (subject != null) subject.onNext(START);
        }

        @Override
        public void onActivityResumed(@NonNull Activity activity) {
            PublishSubject<Event> subject = subjects.get(activity);
            if (subject != null) subject.onNext(RESUME);
        }

        @Override
        public void onActivityPaused(@NonNull Activity activity) {
            PublishSubject<Event> subject = subjects.get(activity);
            if (subject != null) subject.onNext(PAUSE);
        }

        @Override
        public void onActivityStopped(@NonNull Activity activity) {
            PublishSubject<Event> subject = subjects.get(activity);
            if (subject != null) subject.onNext(STOP);
        }

        @Override
        public void onActivityDestroyed(@NonNull Activity activity) {
            PublishSubject<Event> subject = subjects.get(activity);
            if (subject != null) {
                subject.onNext(DESTROY);
                subject.onComplete();
                subjects.remove(activity);
                if (subjects.isEmpty()) {
                    activity.getApplication().unregisterActivityLifecycleCallbacks(this);
                }
            }
        }

        @Override
        public void onActivitySaveInstanceState(@NonNull Activity activity, @NonNull Bundle bundle) {
        }

        private static void checkMainThread() {
            if (Looper.myLooper() != Looper.getMainLooper()) {
                throw new IllegalStateException("Expected to be called on the main thread but was "
                        + Thread.currentThread().getName());
            }
        }
    }
}
