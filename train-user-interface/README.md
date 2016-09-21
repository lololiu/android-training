# Best Practices for User Interface

### Creating Custom Views 创建自定义View

* [Creating a View Class](https://developer.android.com/training/custom-views/create-view.html)

    为了成为一个设计良好的类，自定义的view应该:

    1. 遵守Android标准规则
    2. 提供自定义的风格属性值并能够被Android XML Layout所识别
    3. 发出可访问的事件
    4. 能够兼容Android的不同平台

    为了让Android Developer Tools能够识别你的view，你必须至少提供一个constructor，它包含一个Contenx与一个AttributeSet对象作为参数。这个constructor允许layout editor创建并编辑你的view的实例。
    ```java
    class PieChart extends View {
        public PieChart(Context context, AttributeSet attrs) {
            super(context, attrs);
        }
    }
    ```

    ##### 定义自定义属性(res/values/attrs.xml)
    ```xml
    <resources>
       <declare-styleable name="PieChart">
           <attr name="showText" format="boolean" />
           <attr name="labelPosition" format="enum">
               <enum name="left" value="0"/>
               <enum name="right" value="1"/>
           </attr>
       </declare-styleable>
    </resources>
    ```

    引用属性，注意自定义的命名空间
    ```xml
    <?xml version="1.0" encoding="utf-8"?>
    <LinearLayout xmlns:android="http://schemas.android.com/apk/res/android"
       xmlns:custom="http://schemas.android.com/apk/res/com.example.customviews">
     <com.example.customviews.charting.PieChart
         custom:showText="true"
         custom:labelPosition="left" />
    </LinearLayout>
    ```

    ##### 应用自定义属性
    > 当view从XML layout被创建的时候，在xml标签下的属性值都是从resource下读取出来并传递到view的constructor作为一个AttributeSet参数。尽管可以从AttributeSet中直接读取数值，可是这样做有些弊端。
    > * 拥有属性的资源并没有经过解析
    > * Styles并没有运用上

    Note:通过 attrs 的方法是可以直接获取到属性值的，但是不能确定值类型，如:
    ```java
    String title = attrs.getAttributeValue(null, "title");
    int resId = attrs.getAttributeResourceValue(null, "title", 0);
    title = context.getText(resId));
    ```
    都能获取到 "title" 属性，但你不知道值是字符串还是resId，处理起来就容易出问题，下面的方法则能在编译时就发现问题：
    通过obtainStyledAttributes()来获取属性值。这个方法会传递一个TypedArray对象，它是间接referenced并且styled的。
    ```java
    public PieChart(Context context, AttributeSet attrs) {
       super(context, attrs);
       TypedArray a = context.getTheme().obtainStyledAttributes(
            attrs,
            R.styleable.PieChart,
            0, 0);

       try {
           mShowText = a.getBoolean(R.styleable.PieChart_showText, false);
           mTextPos = a.getInteger(R.styleable.PieChart_labelPosition, 0);
       } finally {
           a.recycle();
       }
    }
    ```
    **Note:TypedArray对象是一个共享资源，必须被在使用后进行回收。**

    ##### 添加属性和事件
    ```java
    public boolean isShowText() {
       return mShowText;
    }

    public void setShowText(boolean showText) {
       mShowText = showText;
       invalidate();
       requestLayout();
    }
    ```
    请注意，在setShowText方法里面有调用invalidate() and requestLayout(). 这两个调用是确保稳定运行的关键。当view的某些内容发生变化的时候，需要调用invalidate来通知系统对这个view进行redraw，当某些元素变化会引起组件大小变化时，需要调用requestLayout方法。调用时若忘了这两个方法，将会导致hard-to-find bugs。

* [Custom Drawing](https://developer.android.com/training/custom-views/custom-drawing.html)

    ##### Override onDraw()

    ##### Create Drawing Objects
    android.graphics framework把绘制定义为下面两类:
    * 绘制什么，由Canvas处理
    * 如何绘制，由Paint处理
    ```java
    private void init() {
       mTextPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
       mTextPaint.setColor(mTextColor);
       if (mTextHeight == 0) {
           mTextHeight = mTextPaint.getTextSize();
       } else {
           mTextPaint.setTextSize(mTextHeight);
       }

       mPiePaint = new Paint(Paint.ANTI_ALIAS_FLAG);
       mPiePaint.setStyle(Paint.Style.FILL);
       mPiePaint.setTextSize(mTextHeight);

       mShadowPaint = new Paint(0);
       mShadowPaint.setColor(0xff101010);
       mShadowPaint.setMaskFilter(new BlurMaskFilter(8, BlurMaskFilter.Blur.NORMAL));
     }
    ```
    刚开始就创建对象是一个重要的优化技巧。Views会被频繁的重新绘制，初始化许多绘制对象需要花费昂贵的代价。在onDraw方法里面创建绘制对象会严重影响到性能并使得你的UI显得卡顿。

    ##### 处理布局事件
    为了正确的绘制你的view，你需要知道view的大小。复杂的自定义view通常需要根据在屏幕上的大小与形状执行多次layout计算。而不是假设这个view在屏幕上的显示大小。即使只有一个程序会使用你的view，仍然是需要处理屏幕大小不同，密度不同，方向不同所带来的影响。

    尽管view有许多方法是用来计算大小的，但是大多数是不需要重写的。如果你的view不需要特别的控制它的大小，唯一需要重写的方法是onSizeChanged().

    onSizeChanged()，当你的view第一次被赋予一个大小时，或者你的view大小被更改时会被执行。在onSizeChanged方法里面计算位置，间距等其他与你的view大小值。

    当你的view被设置大小时，layout manager(布局管理器)假定这个大小包括所有的view的内边距(padding)。当你计算你的view大小时，你必须处理内边距的值。这段PieChart.onSizeChanged()中的代码演示该怎么做:
    ```java
      // Account for padding
           float xpad = (float)(getPaddingLeft() + getPaddingRight());
           float ypad = (float)(getPaddingTop() + getPaddingBottom());

           // Account for the label
           if (mShowText) xpad += mTextWidth;

           float ww = (float)w - xpad;
           float hh = (float)h - ypad;

           // Figure out how big we can make the pie.
           float diameter = Math.min(ww, hh);
    ```

    如果你想更加精确的控制你的view的大小，需要重写onMeasure()方法。这个方法的参数是View.MeasureSpec，它会告诉你的view的父控件的大小。那些值被包装成int类型，你可以使用静态方法来获取其中的信息。

    这里是一个实现onMeasure()的例子。在这个例子中PieChart试着使它的区域足够大，使pie可以像它的label一样大:
    ```java
    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
       // Try for a width based on our minimum
       int minw = getPaddingLeft() + getPaddingRight() + getSuggestedMinimumWidth();
       int w = resolveSizeAndState(minw, widthMeasureSpec, 1);

       // Whatever the width ends up being, ask for a height that would let the pie
       // get as big as it can
       int minh = MeasureSpec.getSize(w) - (int)mTextWidth + getPaddingBottom() + getPaddingTop();
       int h = resolveSizeAndState(MeasureSpec.getSize(w) - (int)mTextWidth, heightMeasureSpec, 0);

       setMeasuredDimension(w, h);
    }
    ```
    上面的代码有三个重要的事情需要注意:
    * 计算的过程有把view的padding考虑进去。这个在后面会提到，这部分是view所控制的。
    * 帮助方法resolveSizeAndState()是用来创建最终的宽高值的。这个方法比较 view 的期望值与传递给 onMeasure 方法的 spec 值，然后返回一个合适的View.MeasureSpec值。
    * onMeasure()没有返回值。它通过调用setMeasuredDimension()来获取结果。调用这个方法是强制执行的，如果你遗漏了这个方法，会出现运行时异常。

    ##### Draw!
    每个view的onDraw都是不同的，但是有下面一些常见的操作
    * 绘制文字使用drawText()。指定字体通过调用setTypeface(), 通过setColor()来设置文字颜色.
    * 绘制基本图形使用drawRect(), drawOval(), drawArc(). 通过setStyle()来指定形状是否需要filled, outlined.
    * 绘制一些复杂的图形，使用Path类. 通过给Path对象添加直线与曲线, 然后使用drawPath()来绘制图形. 和基本图形一样，paths也可以通过setStyle来设置是outlined, filled, both.
    * 通过创建LinearGradient对象来定义渐变。调用setShader()来使用LinearGradient。
    * 通过使用drawBitmap来绘制图片.

* [Making the View Interactive 使得View可交互](https://developer.android.com/training/custom-views/making-interactive.html)

    ##### 处理输入的手势
    ```java
    class mListener extends GestureDetector.SimpleOnGestureListener {
       @Override
       public boolean onDown(MotionEvent e) {
           return true;
       }
    }
    mDetector = new GestureDetector(PieChart.this.getContext(), new mListener());
    ```
    不管你是否使用GestureDetector.SimpleOnGestureListener, 你必须总是实现onDown()方法，并返回true。这一步是必须的，因为所有的gestures都是从onDown()开始的。如果你在onDown()里面返回false，系统会认为你想要忽略后续的gesture,那么GestureDetector.OnGestureListener的其他回调方法就不会被执行到了。一旦你实现了GestureDetector.OnGestureListener并且创建了GestureDetector的实例, 你可以使用你的GestureDetector来中止你在onTouchEvent里面收到的touch事件。
    ```java
    @Override
    public boolean onTouchEvent(MotionEvent event) {
       boolean result = mDetector.onTouchEvent(event);
       if (!result) {
           if (event.getAction() == MotionEvent.ACTION_UP) {
               stopScrolling();
               result = true;
           }
       }
       return result;
    }
    ```
    当你传递一个touch事件到onTouchEvent()时，若这个事件没有被辨认出是何种gesture，它会返回false。你可以执行自定义的gesture-decection代码。

    ##### 创建基本合理的物理运动
    [Scroller](https://developer.android.com/reference/android/widget/Scroller.html)

    ##### 使过渡平滑
    [property animation framework](https://developer.android.com/guide/topics/graphics/prop-animation.html)

* [Optimizing the View](https://developer.android.com/training/custom-views/optimizing-view.html#less)

    为了避免UI显得卡顿，你必须确保动画能够保持在60fps。

    为了加速你的view，对于频繁调用的方法，需要尽量减少不必要的代码。先从onDraw开始，需要特别注意不应该在这里做内存分配的事情，因为它会导致GC，从而导致卡顿。在初始化或者动画间隙期间做分配内存的动作。不要在动画正在执行的时候做内存分配的事情。

    你还需要尽可能的减少onDraw被调用的次数，大多数时候导致onDraw都是因为调用了invalidate().因此请尽量减少调用invaildate()的次数。如果可能的话，尽量调用含有4个参数的invalidate()方法而不是没有参数的invalidate()。没有参数的invalidate会强制重绘整个view。

    另外一个非常耗时的操作是请求layout。任何时候执行requestLayout()，会使得Android UI系统去遍历整个View的层级来计算出每一个view的大小。如果找到有冲突的值，它会需要重新计算好几次。另外需要尽量保持View的层级是扁平化的，这样对提高效率很有帮助。


### Managing the System UI

* [Dimming the System Bars 淡化系统bar](https://developer.android.com/training/system-ui/dim.html)

    ##### 淡化状态栏和系统栏
    如果要淡化状态和通知栏，在版本为4.0以上的Android系统上，你可以像如下使用SYSTEM_UI_FLAG_LOW_PROFILE这个标签
    ```java
    // This example uses decor view, but you can use any visible view.
    View decorView = getActivity().getWindow().getDecorView();
    int uiOptions = View.SYSTEM_UI_FLAG_LOW_PROFILE;
    decorView.setSystemUiVisibility(uiOptions);
    ```
    一旦用户触摸到了状态栏或者是系统栏，这个标签就会被清除，使系统栏重新显现（无透明度）。在标签被清除的情况下，如果你想重新淡化系统栏就必须重新设定这个标签。

    ##### 显示状态栏与导航栏
    如果你想动态的清除显示标签，你可以使用setSystemUiVisibility()方法：
    ```java
    View decorView = getActivity().getWindow().getDecorView();
    // Calling setSystemUiVisibility() with a value of 0 clears
    // all flags.
    decorView.setSystemUiVisibility(0);
    ```

* [Hiding the Status Bar 隐藏状态栏](https://developer.android.com/training/system-ui/status.html)

    ##### 在4.0及以下版本中隐藏状态栏
    改写manifest设定主题
    ```xml
    <application
        ...
        android:theme="@android:style/Theme.Holo.NoActionBar.Fullscreen" >
        ...
    </application>
    ```
    使用WindowManager来动态隐藏状态栏
    ```java
    public class MainActivity extends Activity {

        @Override
        protected void onCreate(Bundle savedInstanceState) {
            super.onCreate(savedInstanceState);
            // If the Android version is lower than Jellybean, use this call to hide
            // the status bar.
            if (Build.VERSION.SDK_INT < 16) {
                getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
                        WindowManager.LayoutParams.FLAG_FULLSCREEN);
            }
            setContentView(R.layout.activity_main);
        }
        ...
    }
    ```

    ##### 在4.1及以上版本中隐藏状态栏
    在Android 4.1(API level 16)以及更高的版本中，你可以使用setSystemUiVisibility()来进行动态隐藏。setSystemUiVisibility()在View层面设置了UI的标签，然后这些设置被整合到了Window层面。setSystemUiVisibility()给了你一个比设置WindowManager标签更加粒度化的操作。下面这段代码隐藏了状态栏：
    ```java
    View decorView = getWindow().getDecorView();
    // Hide the status bar.
    int uiOptions = View.SYSTEM_UI_FLAG_FULLSCREEN;
    decorView.setSystemUiVisibility(uiOptions);
    // Remember that you should never show the action bar if the
    // status bar is hidden, so hide that too if necessary.
    ActionBar actionBar = getActionBar();
    actionBar.hide();
    ```
    注意以下几点：
    * 一旦UI标签被清除(比如跳转到另一个Activity),如果你还想隐藏状态栏你就必须再次设定它。详细可以看第五节如何监听并响应UI可见性的变化。
    * 在不同的地方设置UI标签是有所区别的。如果你在Activity的onCreate()方法中隐藏系统栏，当用户按下home键系统栏就会重新显示。当用户再重新打开Activity的时候，onCreate()不会被调用，所以系统栏还会保持可见。如果你想让在不同
    * setSystemUiVisibility()仅仅在被调用的View显示的时候才会生效。
    * 当从View导航到别的地方时，用setSystemUiVisibility()设置的标签会被清除。

    ##### 让内容显示在状态栏之后
    在Android 4.1及以上版本，你可以将应用的内容显示在状态栏之后，这样当状态栏显示与隐藏的时候，内容区域的大小就不会发生变化。要做到这个效果，我们需要用到SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN这个标志。同时，你也有可能需要SYSTEM_UI_FLAG_LAYOUT_STABLE这个标志来帮助你的应用维持一个稳定的布局。

    当使用这种方法的时候，你就需要来确保应用中特定区域不会被系统栏掩盖（比如地图应用中一些自带的操作区域）。如果被覆盖了，应用可能就会无法使用。在大多数的情况下，你可以在布局文件中添加android:fitsSystemWindows标签，设置它为true。它会调整父ViewGroup使它留出特定区域给系统栏，对于大多数应用这种方法就足够了。

    在一些情况下，你可能需要修改默认的padding大小来获取合适的布局。为了控制内容区域的布局相对系统栏（它占据了一个叫做“内容嵌入”content insets的区域）的位置，你可以重写fitSystemWindows(Rect insets)方法。当窗口的内容嵌入区域发生变化时，fitSystemWindows()方法会被view的hierarchy调用，让View做出相应的调整适应。重写这个方法你就可以按你的意愿处理嵌入区域与应用的布局。

    同步状态栏与Action Bar的变化
    在Android 4.1及以上的版本，为了防止在Action Bar隐藏和显示的时候布局发生变化，你可以使用Action Bar的overlay模式。在Overlay模式中，Activity的布局占据了所有可能的空间，好像Action Bar不存在一样，系统会在布局的上方绘制Aciton Bar。虽然这会遮盖住上方的一些布局，但是当Action Bar显示或者隐藏的时候，系统就不需要重新改变布局区域的大小，使之无缝的变化。

    要启用Action Bar的overlay模式，你需要创建一个继承自Action Bar主题的自定义主题，将android:windowActionBarOverlay属性设置为true。要了解详细信息，请参考添加Action Bar课程中的Action Bar的覆盖层叠。

    设置SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN来让你的activity使用的屏幕区域与设置SYSTEM_UI_FLAG_FULLSCREEN时的区域相同。当你需要隐藏系统UI时，使用SYSTEM_UI_FLAG_FULLSCREEN。这个操作也同时隐藏了Action Bar（因为windowActionBarOverlay="true"），当同时显示与隐藏ActionBar与状态栏的时候，使用一个动画来让他们相互协调。

* [Hiding the Navigation Bar 隐藏导航栏](https://developer.android.com/training/system-ui/navigation.html)

    ##### 在4.0及以上版本中隐藏导航栏
    你可以在Android 4.0以及以上版本，使用SYSTEM_UI_FLAG_HIDE_NAVIGATION标志来隐藏导航栏。这段代码同时隐藏了导航栏和系统栏：
    ```java
    View decorView = getWindow().getDecorView();
    // Hide both the navigation bar and the status bar.
    // SYSTEM_UI_FLAG_FULLSCREEN is only available on Android 4.1 and higher, but as
    // a general rule, you should design your app to hide the status bar whenever you
    // hide the navigation bar.
    int uiOptions = View.SYSTEM_UI_FLAG_HIDE_NAVIGATION
                  | View.SYSTEM_UI_FLAG_FULLSCREEN;
    decorView.setSystemUiVisibility(uiOptions);
    ```
    注意以下几点:
    * 使用这个方法时，触摸屏幕的任何一个区域都会使导航栏（与状态栏）重新显示。用户的交互会使这个标签SYSTEM_UI_FLAG_HIDE_NAVIGATION被清除。
    * 一旦这个标签被清除了，如果你想再次隐藏导航栏，你就需要重新对这个标签进行设定。在下一节响应UI可见性的变化中，将详细讲解应用监听系统UI变化来做出相应的调整操作。
    * 在不同的地方设置UI标签是有所区别的。如果你在Activity的onCreate()方法中隐藏系统栏，当用户按下home键系统栏就会重新显示。当用户再重新打开activity的时候，onCreate()不会被调用，所以系统栏还会保持可见。如果你想让在不同Activity之间切换时，系统UI保持不变，你需要在onReasume()与onWindowFocusChaned()里设定UI标签。
    * setSystemUiVisibility()仅仅在被调用的View显示的时候才会生效。
    * 当从View导航到别的地方时，用setSystemUiVisibility()设置的标签会被清除

    ##### 让内容显示在导航栏之后
    在Android 4.1与更高的版本中，你可以让应用的内容显示在导航栏的后面，这样当导航栏展示或隐藏的时候内容区域就不会发生布局大小的变化。可以使用SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION标签来做到这个效果。同时，你也有可能需要SYSTEM_UI_FLAG_LAYOUT_STABLE这个标签来帮助你的应用维持一个稳定的布局。

    当你使用这种方法的时候，就需要你来确保应用中特定区域不会被系统栏掩盖。更详细的信息可以浏览隐藏状态栏一节。

* [响应UI可见性的变化](http://developer.android.com/training/system-ui/visibility.html)

    为了获取系统UI可见性变化的通知，我们需要对View注册View.OnSystemUiVisibilityChangeListener监听器。通常上来说，这个View是用来控制导航的可见性的。

    例如你可以添加如下代码在onCreate中

    ```java
    View decorView = getWindow().getDecorView();
    decorView.setOnSystemUiVisibilityChangeListener
            (new View.OnSystemUiVisibilityChangeListener() {
        @Override
        public void onSystemUiVisibilityChange(int visibility) {
            // Note that system bars will only be "visible" if none of the
            // LOW_PROFILE, HIDE_NAVIGATION, or FULLSCREEN flags are set.
            if ((visibility & View.SYSTEM_UI_FLAG_FULLSCREEN) == 0) {
                // TODO: The system bars are visible. Make any desired
                // adjustments to your UI, such as showing the action bar or
                // other navigational controls.
            } else {
                // TODO: The system bars are NOT visible. Make any desired
                // adjustments to your UI, such as hiding the action bar or
                // other navigational controls.
            }
        }
    });
    ```



