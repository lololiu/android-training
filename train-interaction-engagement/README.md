# Best Practices for Interaction and Engagement（交互与约定）

### Implementing Effective Navigation 实现有效的导航栏

* [Creating Swipe Views with Tabs](https://developer.android.com/training/implementing-navigation/lateral.html)

    使用[ViewPager](https://developer.android.com/reference/android/support/v4/view/ViewPager.html)来进行多界面滑动切换
    两种适配器：
    **[FragmentPagerAdapter](https://developer.android.com/reference/android/support/v4/app/FragmentPagerAdapter.html)**

    > This is best when navigating between sibling screens representing a fixed, small number of pages.

    **[FragmentStatePagerAdapter](https://developer.android.com/reference/android/support/v4/app/FragmentStatePagerAdapter.html)**

    > This is best for paging across a collection of objects for which the number of pages is undetermined. It destroys fragments as the user navigates to other pages, minimizing memory usage.

* [Creating a Navigation Drawer 导航抽屉](https://developer.android.com/training/implementing-navigation/nav-drawer.html)
    ```xml
    <android.support.v4.widget.DrawerLayout
        xmlns:android="http://schemas.android.com/apk/res/android"
        android:id="@+id/drawer_layout"
        android:layout_width="match_parent"
        android:layout_height="match_parent">
        <!-- The main content view -->
        <FrameLayout
            android:id="@+id/content_frame"
            android:layout_width="match_parent"
            android:layout_height="match_parent" />
        <!-- The navigation drawer -->
        <ListView android:id="@+id/left_drawer"
            android:layout_width="240dp"
            android:layout_height="match_parent"
            android:layout_gravity="start"
            android:choiceMode="singleChoice"
            android:divider="@android:color/transparent"
            android:dividerHeight="0dp"
            android:background="#111"/>
    </android.support.v4.widget.DrawerLayout>

    ```
    注意：
    > * The main content view (the FrameLayout above) must be the first child in the DrawerLayout because the XML order implies z-ordering and the drawer must be on top of the content.
    > * The main content view is set to match the parent view's width and height, because it represents the entire UI when the navigation drawer is hidden.
    > * The drawer view (the ListView) must specify its horizontal gravity with the android:layout_gravity attribute. To support right-to-left (RTL) languages, specify the value with "start" instead of "left" (so the drawer appears on the right when the layout is RTL).
    > * The drawer view specifies its width in dp units and the height matches the parent view. The drawer width should be no more than 320dp so the user can always see a portion of the main content.

* [Providing Up Navigation 向上导航按钮](https://developer.android.com/training/implementing-navigation/ancestral.html)

    设置父Activity
    ```xml
    <application>
        <!-- ... -->
        <!-- The main/home activity (it has no parent activity) -->
        <activity
            android:name="com.example.myfirstapp.MainActivity" >

        </activity>
        <!-- A child of the main activity -->
        <!--parentActivityName 指定父类Activity -->
        <activity
            android:name="com.example.myfirstapp.DisplayMessageActivity"
            android:label="@string/title_activity_display_message"
            android:parentActivityName="com.example.myfirstapp.MainActivity" >
            <!-- Parent activity meta-data to support 4.0 and lower 向下兼容-->
            <meta-data
                android:name="android.support.PARENT_ACTIVITY"
                android:value="com.example.myfirstapp.MainActivity" />
        </activity>
    </application>
    ```

    Add Up Action（添加向上按钮）
    ```java
    @Override
    public void onCreate(Bundle savedInstanceState) {
        ...
        getActionBar().setDisplayHomeAsUpEnabled(true);
    }
    ```

    Navigate Up to Parent Activity

    > To navigate up when the user presses the app icon, you can use the NavUtils class's static method, navigateUpFromSameTask(). When you call this method, it finishes the current activity and starts (or resumes) the appropriate parent activity. If the target parent activity is in the task's back stack, it is brought forward. The way it is brought forward depends on whether the parent activity is able to handle an onNewIntent() call:
    > * If the parent activity has launch mode <singleTop>, or the up intent contains FLAG_ACTIVITY_CLEAR_TOP, the parent activity is brought to the top of the stack, and receives the intent through its onNewIntent() method.
    > * If the parent activity has launch mode <standard>, and the up intent does not contain FLAG_ACTIVITY_CLEAR_TOP, the parent activity is popped off the stack, and a new instance of that activity is created on top of the stack to receive the intent.

    ```java
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
        // Respond to the action bar's Up/Home button
        case android.R.id.home:
            NavUtils.navigateUpFromSameTask(this);
            return true;
        }
        return super.onOptionsItemSelected(item);
    }
    ```

    注意： navigateUpFromSameTask()方法只是用于你的app是当前栈的owner，如果该页面是从其他app通过intent filters进来的，你需要创建一个属于你自己app的返回栈。解决办法如下。

    Navigate up with a new back stack（使用新的返回栈向上导航）

    > If your activity provides any intent filters that allow other apps to start the activity, you should implement the onOptionsItemSelected() callback such that if the user presses the Up button after entering your activity from another app's task, your app starts a new task with the appropriate back stack before navigating up.
    > You can do so by first calling shouldUpRecreateTask() to check whether the current activity instance exists in a different app's task. If it returns true, then build a new task with TaskStackBuilder. Otherwise, you can use the navigateUpFromSameTask() method as shown above.

    ```java
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
        // Respond to the action bar's Up/Home button
        case android.R.id.home:
            Intent upIntent = NavUtils.getParentActivityIntent(this);
            if (NavUtils.shouldUpRecreateTask(this, upIntent)) {
                // This activity is NOT part of this app's task, so create a new task
                // when navigating up, with a synthesized back stack.
                TaskStackBuilder.create(this)
                        // Add all of this activity's parents to the back stack
                        .addNextIntentWithParentStack(upIntent)
                        // Navigate up to the closest parent
                        .startActivities();
            } else {
                // This activity is part of this app's task, so simply
                // navigate up to the logical parent activity.
                NavUtils.navigateUpTo(this, upIntent);
            }
            return true;
        }
        return super.onOptionsItemSelected(item);
    }
    ```

    > Note:In order for the addNextIntentWithParentStack() method to work, you must declare the logical parent of each activity in your manifest file, using the android:parentActivityName attribute (and corresponding <meta-data> element) as described above.

* [Providing Proper Back Navigation 系统自带的返回导航](https://developer.android.com/training/implementing-navigation/temporal.html)

    用户点击通知(notification)时可以新建回退栈
    ```java
    // Intent for the activity to open when user selects the notification
    Intent detailsIntent = new Intent(this, DetailsActivity.class);

    // Use TaskStackBuilder to build the back stack and get the PendingIntent
    PendingIntent pendingIntent =
            TaskStackBuilder.create(this)
                            // add all of DetailsActivity's parents to the stack,
                            // followed by DetailsActivity itself
                            .addNextIntentWithParentStack(upIntent)
                            .getPendingIntent(0, PendingIntent.FLAG_UPDATE_CURRENT);

    NotificationCompat.Builder builder = new NotificationCompat.Builder(this);
    builder.setContentIntent(pendingIntent);
    ```

    Implement Back Navigation for Fragments
    ```java
    // Works with either the framework FragmentManager or the
    // support package FragmentManager (getSupportFragmentManager).
    getSupportFragmentManager().beginTransaction()
                               .add(detailFragment, "detail")
                               // Add this transaction to the back stack
                               .addToBackStack()
                               .commit();
    ```

    > Note: You should not add transactions to the back stack when the transaction is for horizontal navigation (such as when switching tabs) or when modifying the content appearance (such as when adjusting filters). For more information, about when Back navigation is appropriate, see the Navigation design guide.

    设置监听：
    ```java
    getSupportFragmentManager().addOnBackStackChangedListener(
            new FragmentManager.OnBackStackChangedListener() {
                public void onBackStackChanged() {
                    // Update your UI here.
                }
            });
    ```

    Implement Back Navigation for WebViews
    ```java
    @Override
    public void onBackPressed() {
        if (mWebView.canGoBack()) {
            mWebView.goBack();
            return;
        }

        // Otherwise defer to system default behavior.
        super.onBackPressed();
    }
    ```

### Notifying the User

* [Building a Notification](https://developer.android.com/training/notify-user/build-notification.html)
    ```java
    //创建Notification Builder
    NotificationCompat.Builder mBuilder =
        new NotificationCompat.Builder(this)
        .setSmallIcon(R.drawable.notification_icon)
        .setContentTitle("My notification")
        .setContentText("Hello World!");

    //定义Notification的Action（行为）
    Intent resultIntent = new Intent(this, ResultActivity.class);
    // Because clicking the notification opens a new ("special") activity, there's
    // no need to create an artificial back stack.
    PendingIntent resultPendingIntent =
        PendingIntent.getActivity(
        this,
        0,
        resultIntent,
        PendingIntent.FLAG_UPDATE_CURRENT
    );

    //设置通知内容点击行为
    mBuilder.setContentIntent(resultPendingIntent);

    //发布通知
    // Sets an ID for the notification 设置一个通知ID方便更新or取消
    int mNotificationId = 001;
    // Gets an instance of the NotificationManager service
    NotificationManager mNotifyMgr =
            (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
    // Builds the notification and issues it.
    mNotifyMgr.notify(mNotificationId, mBuilder.build());
    ```

* [Preserving Navigation when Starting an Activity（启动Activity时保留导航）](https://developer.android.com/training/notify-user/navigation.html)

    **设置一个常规的Activity PendingIntent**

    在manifest中定义你application的Activity层次，最终的manifest文件应该像这个
    ```xml
    <activity
        android:name=".MainActivity"
        android:label="@string/app_name" >
        <intent-filter>
            <action android:name="android.intent.action.MAIN" />
            <category android:name="android.intent.category.LAUNCHER" />
        </intent-filter>
    </activity>
    <activity
        android:name=".ResultActivity"
        android:parentActivityName=".MainActivity">
        <meta-data
            android:name="android.support.PARENT_ACTIVITY"
            android:value=".MainActivity"/>
    </activity>
    ```

    在基于启动Activity的Intent中创建一个返回栈，比如：
    ```java
    int id = 1;
    ...
    Intent resultIntent = new Intent(this, ResultActivity.class);
    TaskStackBuilder stackBuilder = TaskStackBuilder.create(this);
    // Adds the back stack
    stackBuilder.addParentStack(ResultActivity.class);
    // Adds the Intent to the top of the stack
    stackBuilder.addNextIntent(resultIntent);
    // Gets a PendingIntent containing the entire back stack
    PendingIntent resultPendingIntent =
            stackBuilder.getPendingIntent(0, PendingIntent.FLAG_UPDATE_CURRENT);
    ...
    NotificationCompat.Builder builder = new NotificationCompat.Builder(this);
    builder.setContentIntent(resultPendingIntent);
    NotificationManager mNotificationManager =
        (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
    mNotificationManager.notify(id, builder.build());
    ```

    **设置一个特定的Activity PendingIntent**

    ```java
    // Instantiate a Builder object.
    NotificationCompat.Builder builder = new NotificationCompat.Builder(this);
    // Creates an Intent for the Activity
    Intent notifyIntent =
            new Intent(new ComponentName(this, ResultActivity.class));
    // Sets the Activity to start in a new, empty task
    notifyIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK |
            Intent.FLAG_ACTIVITY_CLEAR_TASK);
    // Creates the PendingIntent
    PendingIntent notifyIntent =
            PendingIntent.getActivity(
            this,
            0,
            notifyIntent,
            PendingIntent.FLAG_UPDATE_CURRENT
    );

    // Puts the PendingIntent into the notification builder
    builder.setContentIntent(notifyIntent);
    // Notifications are issued by sending them to the
    // NotificationManager system service.
    NotificationManager mNotificationManager =
        (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
    // Builds an anonymous Notification object from the builder, and
    // passes it to the NotificationManager
    mNotificationManager.notify(id, builder.build());
    ```

* [Updating Notifications](https://developer.android.com/training/notify-user/managing.html)

    **Modify a Notification**
    想要设置一个可以被更新的Notification，需要在发布它的时候调用NotificationManager.notify(ID, notification)方法为它指定一个notification ID。更新一个已经发布的Notification，需要更新或者创建一个NotificationCompat.Builder对象，并从这个对象创建一个Notification对象，然后用与先前一样的ID去发布这个Notification。

    下面的代码片段演示了更新一个notification来反映事件发生的次数，它把notification堆积起来，显示一个总数。
    ```java
    mNotificationManager =
            (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
    // Sets an ID for the notification, so it can be updated
    int notifyID = 1;
    mNotifyBuilder = new NotificationCompat.Builder(this)
        .setContentTitle("New Message")
        .setContentText("You've received new messages.")
        .setSmallIcon(R.drawable.ic_notify_status)
    numMessages = 0;
    // Start of a loop that processes data and then notifies the user
    ...
        mNotifyBuilder.setContentText(currentText)
            .setNumber(++numMessages);
        // Because the ID remains unchanged, the existing notification is
        // updated.
        mNotificationManager.notify(
                notifyID,
                mNotifyBuilder.build());
    ```

    **Remove Notifications**
    Notifications remain visible until one of the following happens:
    > * The user dismisses the notification either individually or by using "Clear All" (if the notification can be cleared).
    > * The user touches the notification, and you called setAutoCancel() when you created the notification.
    > * You call cancel() for a specific notification ID. This method also deletes ongoing notifications.
    > * You call cancelAll(), which removes all of the notifications you previously issued.

* [Using Big View Styles](https://developer.android.com/training/notify-user/expanded.html)

    **Construct the Big View**

    ```java
    // Sets up the Snooze and Dismiss action buttons that will appear in the
    // big view of the notification.
    Intent dismissIntent = new Intent(this, PingService.class);
    dismissIntent.setAction(CommonConstants.ACTION_DISMISS);
    PendingIntent piDismiss = PendingIntent.getService(this, 0, dismissIntent, 0);

    Intent snoozeIntent = new Intent(this, PingService.class);
    snoozeIntent.setAction(CommonConstants.ACTION_SNOOZE);
    PendingIntent piSnooze = PendingIntent.getService(this, 0, snoozeIntent, 0);

    // Constructs the Builder object.
    NotificationCompat.Builder builder =
            new NotificationCompat.Builder(this)
            .setSmallIcon(R.drawable.ic_stat_notification)
            .setContentTitle(getString(R.string.notification))
            .setContentText(getString(R.string.ping))
            .setDefaults(Notification.DEFAULT_ALL) // requires VIBRATE permission
            /*
             * Sets the big view "big text" style and supplies the
             * text (the user's reminder message) that will be displayed
             * in the detail area of the expanded notification.
             * These calls are ignored by the support library for
             * pre-4.1 devices.
             */
            .setStyle(new NotificationCompat.BigTextStyle()
                    .bigText(msg))
            .addAction (R.drawable.ic_stat_dismiss,
                    getString(R.string.dismiss), piDismiss)
            .addAction (R.drawable.ic_stat_snooze,
                    getString(R.string.snooze), piSnooze);

    ```

* [Displaying Progress in a Notification(显示进度条)](https://developer.android.com/training/notify-user/display-progress.html)

    **Display a Fixed-duration Progress Indicator(展示固定长度的进度指示器)**
    ```java
    int id = 1;
    ...
    mNotifyManager =
            (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
    mBuilder = new NotificationCompat.Builder(this);
    mBuilder.setContentTitle("Picture Download")
        .setContentText("Download in progress")
        .setSmallIcon(R.drawable.ic_notification);
    // Start a lengthy operation in a background thread
    new Thread(
        new Runnable() {
            @Override
            public void run() {
                int incr;
                // Do the "lengthy" operation 20 times
                for (incr = 0; incr <= 100; incr+=5) {
                        // Sets the progress indicator to a max value, the
                        // current completion percentage, and "determinate"
                        // state
                        //第三个参数是个boolean类型，决定进度条是 indeterminate (true) 还是 determinate (false)
                        mBuilder.setProgress(100, incr, false);
                        // Displays the progress bar for the first time.
                        mNotifyManager.notify(id, mBuilder.build());
                            // Sleeps the thread, simulating an operation
                            // that takes time
                            try {
                                // Sleep for 5 seconds
                                Thread.sleep(5*1000);
                            } catch (InterruptedException e) {
                                Log.d(TAG, "sleep failure");
                            }
                }
                // When the loop is finished, updates the notification
                mBuilder.setContentText("Download complete")
                // Removes the progress bar
                        .setProgress(0,0,false);
                mNotifyManager.notify(id, mBuilder.build());
            }
        }
    // Starts the thread by calling the run() method in its Runnable
    ).start();
    ```

    **Display a Continuing Activity Indicator（展示持续的活动的指示器）**
    > 为了展示一个持续的(indeterminate)活动的指示器,用setProgress(0, 0, true)方法把指示器添加进notification，然后发布这个notification 。前两个参数忽略，第三个参数决定indicator 还是 indeterminate。结果是指示器与进度条有同样的样式，除了它的动画正在进行。

    > 在操作开始的时候发布notification，动画将会一直进行直到你更新notification。当操作完成时，调用 setProgress(0, 0, false) 方法，然后更新notification来移除这个动画指示器。一定要这么做，否责即使你操作完成了，动画还是会在那运行。同时也要记得更新notification的文字来显示操作完成。

    ```java
    // Sets an activity indicator for an operation of indeterminate length
    mBuilder.setProgress(0, 0, true);
    // Issues the notification
    mNotifyManager.notify(id, mBuilder.build());
    ```

### Adding Search Functionality(添加搜索功能)

* [Setting Up the Search Interface](https://developer.android.com/training/search/setup.html)

    **添加Search View到action bar中**

    > res/menu/options_menu.xml
    > collapseActionView属性允许你的SearchView占据整个action bar，在不使用的时候折叠成普通的action bar item。由于在手持设备中action bar的空间有限，建议使用collapsibleActionView属性来提供更好的用户体验。

    ```xml
    <?xml version="1.0" encoding="utf-8"?>
    <menu xmlns:android="http://schemas.android.com/apk/res/android">
        <item android:id="@+id/search"
              android:title="@string/search_title"
              android:icon="@drawable/ic_search"
              android:showAsAction="collapseActionView|ifRoom"
              android:actionViewClass="android.widget.SearchView" />
    </menu>
    ```
    ```java
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.options_menu, menu);

        return true;
    }
    ```

    **创建一个检索配置**

    检索配置(searchable configuration)在 res/xml/searchable.xml文件中定义了SearchView如何运行。检索配置中至少要包含一个android:label属性，与Android manifest中的<application>或<activity> android:label属性值相同。但我们还是建议添加android:hint属性来告诉用户应该在搜索框中输入什么内容:

    ```xml
    <?xml version="1.0" encoding="utf-8"?>

    <searchable xmlns:android="http://schemas.android.com/apk/res/android"
            android:label="@string/app_name"
            android:hint="@string/search_hint" />
    ```

    在你的应用的manifest文件中，声明一个指向res/xml/searchable.xml文件的<meta-data>元素，来告诉你的应用在哪里能找到检索配置。在你想要显示SearchView的<activity>中声明<meta-data>元素:

    ```xml
    <activity>
        ...
        <meta-data android:name="android.app.searchable"
                android:resource="@xml/searchable" />

    </activity>
    ```

    在你之前创建的onCreateOptionsMenu()方法中，调用setSearchableInfo(SearchableInfo)把SearchView和检索配置关联在一起:
    ```java
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.options_menu, menu);

        // 关联检索配置和SearchView
        SearchManager searchManager =
               (SearchManager) getSystemService(Context.SEARCH_SERVICE);
        SearchView searchView =
                (SearchView) menu.findItem(R.id.search).getActionView();
        searchView.setSearchableInfo(
                searchManager.getSearchableInfo(getComponentName()));

        return true;
    }
    ```

    调用getSearchableInfo()返回一个SearchableInfo由检索配置XML文件创建的对象。检索配置与SearchView正确关联后，当用户提交一个搜索请求时，SearchView会以ACTION_SEARCH intent启动一个activity。所以你现在需要一个能过滤这个intent和处理搜索请求的activity。

    **创建一个检索activity**

    当用户提交一个搜索请求时，SearchView会尝试以ACTION_SEARCH启动一个activity。检索activity会过滤ACTION_SEARCH intent并在某种数据集中根据请求进行搜索。要创建一个检索activity，在你选择的activity中声明对ACTION_SEARCH intent过滤:

    ```xml
    <activity android:name=".SearchResultsActivity">
        ...
        <intent-filter>
            <action android:name="android.intent.action.SEARCH" />
        </intent-filter>
        ...
    </activity>
    ```
    在你的检索activity中，通过在onCreate()方法中检查ACTION_SEARCH intent来处理它。

    > Note:如果你的检索activity在single top mode下启动(android:launchMode="singleTop")，也要在onNewIntent()方法中处理ACTION_SEARCH intent。在single top mode下你的activity只有一个会被创建，而随后启动的activity将不会在栈中创建新的activity。这种启动模式很有用，因为用户可以在当前activity中进行搜索，而不用在每次搜索时都创建一个activity实例。

    ```java
    public class SearchResultsActivity extends Activity {

        @Override
        public void onCreate(Bundle savedInstanceState) {
            ...
            handleIntent(getIntent());
        }

        @Override
        protected void onNewIntent(Intent intent) {
            ...
            handleIntent(intent);
        }

        private void handleIntent(Intent intent) {

            if (Intent.ACTION_SEARCH.equals(intent.getAction())) {
                String query = intent.getStringExtra(SearchManager.QUERY);
                //通过某种方法，根据请求检索你的数据
            }
        }
        ...
    }
    ```
    如果你现在运行你的app，SearchView就能接收用户的搜索请求，以ACTION_SEARCH intent启动你的检索activity。现在就由你来解决如何依据请求来储存和搜索数据。


