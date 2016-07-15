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

### Managing Bitmap Memory