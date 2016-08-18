# Displaying Bitmaps Efficiently

### Loading Large Bitmaps Efficiently
有效地加载大图片
```java
//获取图片的信息 设置inJustDecodeBounds为true时，会使BitmapFactory.decodeResource返回的bitmap对象为null
//即该方法不会怎么返回一个Bitmap对象，但是会返回图片的宽高、类型信息
BitmapFactory.Options options = new BitmapFactory.Options();
options.inJustDecodeBounds = true;
BitmapFactory.decodeResource(getResources(), R.id.myimage, options);
int imageHeight = options.outHeight;
int imageWidth = options.outWidth;
String imageType = options.outMimeType;
```
上图代码相当于“测量”一张图片的大小，当宽高太大时，则要把它适当地缩小，不然加载的Bitmap太大容易造成OOM

```java
//根据提供大小计算缩放比例
public static int calculateInSampleSize(
            BitmapFactory.Options options, int reqWidth, int reqHeight) {
    // Raw height and width of image
    final int height = options.outHeight;
    final int width = options.outWidth;
    int inSampleSize = 1;

    if (height > reqHeight || width > reqWidth) {

        final int halfHeight = height / 2;
        final int halfWidth = width / 2;

        // Calculate the largest inSampleSize value that is a power of 2 and keeps both
        // height and width larger than the requested height and width.
        while ((halfHeight / inSampleSize) >= reqHeight
                && (halfWidth / inSampleSize) >= reqWidth) {
            inSampleSize *= 2;
        }
    }

    return inSampleSize;
}
```
**Note**:测量图片大小时，inJustDecodeBounds为true，当缩小一定比例时获取Bitmap放入ImageView时，需要将inJustDecodeBounds改回false。
```java
public static Bitmap decodeSampledBitmapFromResource(Resources res, int resId,
        int reqWidth, int reqHeight) {

    // First decode with inJustDecodeBounds=true to check dimensions
    final BitmapFactory.Options options = new BitmapFactory.Options();
    options.inJustDecodeBounds = true;
    BitmapFactory.decodeResource(res, resId, options);

    // Calculate inSampleSize
    options.inSampleSize = calculateInSampleSize(options, reqWidth, reqHeight);

    // Decode bitmap with inSampleSize set
    options.inJustDecodeBounds = false;
    return BitmapFactory.decodeResource(res, resId, options);
}

mImageView.setImageBitmap(
    decodeSampledBitmapFromResource(getResources(), R.id.myimage, 100, 100));

```

### Processing Bitmaps Off the UI Thread
主要解决两个问题：
1. 将资源图片转成Bitmap，或者从网络中获取图片资源的IO相关操作不宜在主线程中进行用AsyncTask转换图片后再set到ImageView中
2. 有时候从网络上读取图片资源会耗费很长时间，而此时界面上的ImageView就会一直是空白用户体验不佳，解决方法是先用一张占位符图片，等真正要显示的图片Bitmap获取到后再set进去


### Caching Bitmaps
内存缓存：
```java
private LruCache<String, Bitmap> mMemoryCache;

@Override
protected void onCreate(Bundle savedInstanceState) {
    ...
    // Get max available VM memory, exceeding this amount will throw an
    // OutOfMemory exception. Stored in kilobytes as LruCache takes an
    // int in its constructor.
    final int maxMemory = (int) (Runtime.getRuntime().maxMemory() / 1024);

    // Use 1/8th of the available memory for this memory cache.
    final int cacheSize = maxMemory / 8;

    mMemoryCache = new LruCache<String, Bitmap>(cacheSize) {
        @Override
        protected int sizeOf(String key, Bitmap bitmap) {
            // The cache size will be measured in kilobytes rather than
            // number of items.
            return bitmap.getByteCount() / 1024;
        }
    };
    ...
}

public void addBitmapToMemoryCache(String key, Bitmap bitmap) {
    if (getBitmapFromMemCache(key) == null) {
        mMemoryCache.put(key, bitmap);
    }
}

public Bitmap getBitmapFromMemCache(String key) {
    return mMemoryCache.get(key);
}
```

磁盘缓存：
[推荐阅读blog](http://blog.csdn.net/guolin_blog/article/details/28863651)

# Animating Views Using Scenes and Transitions

> Android includes the transitions framework, which enables you to easily animate changes between two view hierarchies. The framework animates the views at runtime by changing some of their property values over time. The framework includes built-in animations for common effects and lets you create custom animations and transition lifecycle callbacks.

### The Transitions Framework

该框架具备以下一些特性：
* 群组级别的动画：可以为整个视图结构中的所有子View添加一个或多个动画效果。
* 基于转换的动画：基于View属性的初始值和结束值来执行动画。
* 内置的动画效果：包含了预设的通用动画效果，例如渐隐或者移动。
* 支持资源文件：可以从资源文件中加载View结构和内置动画。
* 生命周期回调：为整个动画过程和视图结构变化过程提供便于控制的回调方法。
  概览

Transitions框架的局限性：
* 对SurfaceView应用动画可能无法被正确显示。SurfaceView实例是通过一个非UI进程来更新的，所以SurfaceView的更新可能与其他的View不同步。
* 当某些专场类型被应用在TextureView上时可能无法获得预期效果
* AdapterView的子类，比如ListView，它们管理子View的方式导致无法应用转场效果。如果要对AdapterView的子类应用专场可能会知道设备显示被挂起。
* 当试图对TextView应用缩放动画的时候，其中的文本内容可能在TextView对象还没有完全完成缩放之前就跳到一个新的位置上。为了避免这个问题，不要对包含文本内容的View应用缩放动画。

### Creating a Scene
* 通过xml定义Scene
`res/layout/activity_main.xml`
```xml
<LinearLayout xmlns:android="http://schemas.android.com/apk/res/android"
    android:id="@+id/master_layout">
    <TextView
        android:id="@+id/title"
        ...
        android:text="Title"/>
    <FrameLayout
        android:id="@+id/scene_root">
        <include layout="@layout/a_scene" />
    </FrameLayout>
</LinearLayout>
```

`res/layout/a_scene.xml`
```xml
<RelativeLayout xmlns:android="http://schemas.android.com/apk/res/android"
    android:id="@+id/scene_container"
    android:layout_width="match_parent"
    android:layout_height="match_parent" >
    <TextView
        android:id="@+id/text_view1
        android:text="Text Line 1" />
    <TextView
        android:id="@+id/text_view2
        android:text="Text Line 2" />
</RelativeLayout>
```

`res/layout/another_scene.xml`
```xml
<RelativeLayout xmlns:android="http://schemas.android.com/apk/res/android"
    android:id="@+id/scene_container"
    android:layout_width="match_parent"
    android:layout_height="match_parent" >
    <TextView
        android:id="@+id/text_view2
        android:text="Text Line 2" />
    <TextView
        android:id="@+id/text_view1
        android:text="Text Line 1" />
</RelativeLayout>
```

```java
Scene mAScene;
Scene mAnotherScene;

// Create the scene root for the scenes in this app
mSceneRoot = (ViewGroup) findViewById(R.id.scene_root);

// Create the scenes
mAScene = Scene.getSceneForLayout(mSceneRoot, R.layout.a_scene, this);
mAnotherScene =
    Scene.getSceneForLayout(mSceneRoot, R.layout.another_scene, this);
```

注：
* 两个scene中的控件id需一致
* 大多数情况下，不需要显式地创建一个开始场景。如果应用了一个转场，框架会把之前的结束场景用作后续转场的开始场景。如果之前没有应用过转场，框架会搜集当前屏幕中视图结构的状态作为开始场景。

### Applying a Transition
在xml中创建Transition
```xml
<fade xmlns:android="http://schemas.android.com/apk/res/android" />
```

```java
Transition mFadeTransition =
        TransitionInflater.from(this).
        inflateTransition(R.transition.fade_transition);
```
在代码中创建Transition
```java
Transition mFadeTransition = new Fade();
```

提交Transition
```
TransitionManager.go(mEndingScene, mFadeTransition);
```

创建多个Transition效果
```xml
<transitionSet xmlns:android="http://schemas.android.com/apk/res/android"
    android:transitionOrdering="sequential">
    <fade android:fadingMode="fade_out" />
    <changeBounds />
    <fade android:fadingMode="fade_in" />
</transitionSet>
```

不创建Scene使用Transition
```xml
<RelativeLayout xmlns:android="http://schemas.android.com/apk/res/android"
    android:id="@+id/mainLayout"
    android:layout_width="match_parent"
    android:layout_height="match_parent" >
    <EditText
        android:id="@+id/inputText"
        android:layout_alignParentLeft="true"
        android:layout_alignParentTop="true"
        android:layout_width="match_parent"
        android:layout_height="wrap_content" />
    ...
</RelativeLayout>
```

```java
private TextView mLabelText;
private Fade mFade;
private ViewGroup mRootView;
...

// Load the layout
this.setContentView(R.layout.activity_main);
...

// Create a new TextView and set some View properties
mLabelText = new TextView();
mLabelText.setText("Label").setId("1");

// Get the root view and create a transition
mRootView = (ViewGroup) findViewById(R.id.mainLayout);
mFade = new Fade(IN);

// Start recording changes to the view hierarchy
TransitionManager.beginDelayedTransition(mRootView, mFade);

// Add the new TextView to the view hierarchy
mRootView.addView(mLabelText);

// When the system redraws the screen to show this update,
// the framework will animate the addition as a fade in
```

### Creating Custom Transitions
继承Transition
```java
public class CustomTransition extends Transition {

    @Override
    public void captureStartValues(TransitionValues values) {}

    @Override
    public void captureEndValues(TransitionValues values) {}

    @Override
    public Animator createAnimator(ViewGroup sceneRoot,
                                   TransitionValues startValues,
                                   TransitionValues endValues) {}
}
```

自定义TransitionValues键值对名称格式
> package_name:transition_name:property_name

比如:
> com.example.android.customtransition:CustomTransition:background

捕捉开始值和结束值
```java
public class CustomTransition extends Transition {

    // Define a key for storing a property value in
    // TransitionValues.values with the syntax
    // package_name:transition_class:property_name to avoid collisions
    private static final String PROPNAME_BACKGROUND =
            "com.example.android.customtransition:CustomTransition:background";

    @Override
    public void captureStartValues(TransitionValues transitionValues) {
        // Call the convenience method captureValues
        captureValues(transitionValues);
    }


    // For the view in transitionValues.view, get the values you
    // want and put them in transitionValues.values
    private void captureValues(TransitionValues transitionValues) {
        // Get a reference to the view
        View view = transitionValues.view;
        // Store its background property in the values map
        transitionValues.values.put(PROPNAME_BACKGROUND, view.getBackground());
    }
    ...
}
```

createAnimator创建自定义动画
> 为了给开始场景和结束场景中的View添加动画效果，还需要重写createAnimator()方法提供一个animator对象。当框架调用该方法时，将传递两个参数：场景根视图和包含了开始场景和结束场景中捕捉到的熟知的TransitionValues对象。
> 框架调用createAnimator()方法的次数取决于开始场景和结束场景发生变化的次数。例如，考虑一个渐隐/渐显动画是作为一个自定义动画存在的，如果开始场景中有5个View，在结束场景中将移除其中2个，同时添加一个新的View，那么框架将调用createAnimator()方法6 次：

> * 同时存在于开始场景和结束场景内的View调用渐隐j/渐显效果，共3次。
> * 结束场景中被移除的View添加渐隐效果（共2次）
> * 结束场景中新加入的View添加渐显效果（共1次）

> 对于同时存在于开始场景和结束场景中的View来说。框架为初始值和结束值各自提供了一个TransitionValues对象参数。对于只在开始场景或者结束场景中存在的的View，框架提供一个TransitionValues对象，其中只有先关的TransitionValues会被添加合适的参数，另一个将被设置为null。例如对于只在开始场景中存在的View，重写createAnimator时获取到的TransitionValues对象中只有初始值列表被记录了相关数据，而结束值列表为空，因为该View已经不会显示在结束场景中。

> 当创建自定义转场的时候，实现createAnimator(ViewGroup, TransitionValues, TransitionValues) 方法，使用捕捉到的属性值来创建一个Animator对象并返回给狂框架。

兼容库：[Transitions-Everywhere](https://github.com/andkulikov/Transitions-Everywhere)


# Adding Animations

### Crossfading Two Views
属性动画设置View Alpha值
```java
private void crossfade() {

    // Set the content view to 0% opacity but visible, so that it is visible
    // (but fully transparent) during the animation.
    mContentView.setAlpha(0f);
    mContentView.setVisibility(View.VISIBLE);

    // Animate the content view to 100% opacity, and clear any animation
    // listener set on the view.
    mContentView.animate()
            .alpha(1f)
            .setDuration(mShortAnimationDuration)
            .setListener(null);

    // Animate the loading view to 0% opacity. After the animation ends,
    // set its visibility to GONE as an optimization step (it won't
    // participate in layout passes, etc.)
    mLoadingView.animate()
            .alpha(0f)
            .setDuration(mShortAnimationDuration)
            .setListener(new AnimatorListenerAdapter() {
                @Override
                public void onAnimationEnd(Animator animation) {
                    mLoadingView.setVisibility(View.GONE);
                }
            });
}
```

### Using ViewPager for Screen Slides
利用PageTransformer来自定义ViewPager滑动动画
```
public void transformPage(View view, float position) {

}
```
position图解
![图片来自网络](https://github.com/lololiu/android-training/raw/master/images/position图解.png)

代码:
```java
public class RoratePageTransformer implements ViewPager.PageTransformer {
    private static final float MIN_SCALE = 0.75f;

    public void transformPage(View view, float position) {
        int pageWidth = view.getWidth();

        if (position < -1) { // [-Infinity,-1)
            // This page is way off-screen to the left.
            view.setAlpha(0);

        } else if (position <= 0) { // [-1,0]
            // Use the default slide transition when moving to the left page
            view.setAlpha(position+1);
        } else if (position <= 1) { // (0,1]
            // Fade the page out.
            view.setAlpha(1 - position);
            view.setRotation((1-position)*360);
            view.setScaleX(1-position);
            view.setScaleY(1-position);

        } else { // (1,+Infinity]
            // This page is way off-screen to the right.
            view.setAlpha(0);
        }
    }
}
```
