# RxScuttle
Scuttle RxJava Subs(criptions) on Activity lifecycle callbacks

The simplest way to use RxScuttle is to extend RxActivity:

```
public class MainActivity extends RxActivity {
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Log.d(TAG, "onCreate");
        Observable.interval(5, TimeUnit.SECONDS)
                .takeUntil(lifecycle())
                .subscribe();
```

Calls to `lifecycle()` will "intuit" the `Activity`s lifecycle and will emit when that scope is ended.

For more control you can just `takeUntil(pauses()`, `stops()` or `destroys())`

If you can't extend `RxActivity` then you can use `RxScuttle` via some handy helper methods:

```
public class MainActivity extends RxActivity {
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        
        Observable.interval(5, TimeUnit.SECONDS)
            .takeUntil(RxScuttle.pauses(this))
            .subscribe();
        Observable.interval(5, TimeUnit.SECONDS)
            .takeUntil(RxScuttle.stops(this))
            .subscribe();
        Observable.interval(5, TimeUnit.SECONDS)
            .takeUntil(RxScuttle.destroys(this))
            .subscribe();
        Observable.interval(5, TimeUnit.SECONDS)
            .takeUntil(RxScuttle.lifecycle(this))
            .subscribe();
```

...and it doesn't look so bad with a static import:

```
.takeUntil(pauses(this))
```

...and if you want to listen to other events (create, start and resume) then grab an `RxScuttle` yourself via:

```
RxScuttle scuttle = RxScuttle.with(activity);
scuttle.events(Event.CREATE);
```
