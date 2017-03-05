# RxScuttle
## Scuttle RxJava Sub(scription)s on Activity lifecycle callbacks
### *No Activity subclassing required!*

Although you don't have to, the simplest way to use RxScuttle is to extend `RxActivity`:

```
public class MainActivity extends RxActivity {
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        // and use takeUntil to end complete the Observable on the desired lifecycle method or
        // use lifecycle() to infer the corresponding closing lifecycle method to the current scope.
        Observable.interval(5, TimeUnit.SECONDS)
                .takeUntil(lifecycle())
                .subscribe();
```

Calls to `lifecycle()` will infer the `Activity`s lifecycle (at the time of subscription) and will emit when that scope is ended.

For fine-grained control use `takeUntil(pauses()`, `stops()` or `destroys())`

If you can't extend `RxActivity`, or if you just don't think it's cool, then you can use `RxScuttle` via some handy helper methods:

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

## Other Events

If you want to listen to other events (create, start and resume) then grab an `RxScuttle` yourself via:

```
RxScuttle scuttle = RxScuttle.with(activity);
scuttle.events(Event.CREATE);
```

### Inspiration

Inspired by [RxLifecycle](https://github.com/trello/RxLifecycle), the high method count, and [this issue](https://github.com/trello/RxLifecycle/issues/93).
