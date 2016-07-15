package com.royll.graphicsanimation;

import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.os.AsyncTask;
import android.support.v4.util.LruCache;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import java.lang.ref.WeakReference;

public class CachingBitmapActivity extends AppCompatActivity {

    private Bitmap mPlaceHolderBitmap;
    private LruCache<String, Bitmap> mCacheList;

    ImageView mImageView;
    TextView mText;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_caching_bitmap);
        mImageView = (ImageView) findViewById(R.id.imageview3);
        mText = (TextView) findViewById(R.id.text);


        initCacheMemorySize();
//        mPlaceHolderBitmap = BitmapUtil.decodeSampledBitmapFromResource(getResources(), R.mipmap.placeholder, 800, 800);
//        loadBitmap(R.mipmap.test, mImageView);
    }

    @Override
    protected void onResume() {
        super.onResume();
        //写在onResume中测试缓存  按Home键到桌面再回来会读取缓存
        mPlaceHolderBitmap = BitmapUtil.decodeSampledBitmapFromResource(getResources(), R.mipmap.placeholder, 800, 800);
        loadBitmap(R.mipmap.test, mImageView);
    }

    /**
     * 初始化内存缓存大小
     */
    private void initCacheMemorySize() {
        // Get max available VM memory, exceeding this amount will throw an
        // OutOfMemory exception. Stored in kilobytes as LruCache takes an
        // int in its constructor.
        final int maxMemory = (int) (Runtime.getRuntime().maxMemory() / 1024);

        // Use 1/8th of the available memory for this memory cache.
        final int cacheSize = maxMemory / 8;
        mCacheList = new LruCache<String, Bitmap>(cacheSize) {
            @Override
            protected int sizeOf(String key, Bitmap value) {
                // The cache size will be measured in kilobytes rather than
                // number of items.
                return value.getByteCount() / 1024;
            }
        };
    }

    private void loadBitmap(int resId, ImageView imageView) {

        final String imageKey = String.valueOf(resId);
        final Bitmap bitmap = getBitmapFromMemoryCache(imageKey);
        if (bitmap != null) {
            mImageView.setImageBitmap(bitmap);
            mText.setText("此图片取自内存缓存区");
        } else {
            if (canclePotentialWork(resId, imageView)) {
                BitmapWorkTask task = new BitmapWorkTask(mImageView);
                task.execute(resId);
                AsyncDrawable asyncDrawable = new AsyncDrawable(getResources(), mPlaceHolderBitmap, task);
                mImageView.setImageDrawable(asyncDrawable);
            }
        }

    }


    class BitmapWorkTask extends AsyncTask<Integer, Void, Bitmap> {
        private final WeakReference<ImageView> imageViewWeakReference;
        private int resId;

        public BitmapWorkTask(ImageView imageview) {
            imageViewWeakReference = new WeakReference<ImageView>(imageview);
        }

        @Override
        protected Bitmap doInBackground(Integer... params) {
            resId = params[0];
            Bitmap bitmap = BitmapUtil.decodeSampledBitmapFromResource(getResources(), resId, 800, 800);
            addBitmapToMemoryCache(String.valueOf(resId), bitmap);
            return bitmap;
        }

        @Override
        protected void onPostExecute(Bitmap bitmap) {
            if (isCancelled()) {
                bitmap = null;
            }
            if (imageViewWeakReference != null && bitmap != null) {
                final ImageView imageView = imageViewWeakReference.get();
                final BitmapWorkTask bitmapWorkerTask =
                        getBitmapWorkTask(imageView);
                if (this == bitmapWorkerTask && imageView != null) {
                    imageView.setImageBitmap(bitmap);
                    mText.setText("此图片取自资源文件");
                }
            }
        }
    }

    static class AsyncDrawable extends BitmapDrawable {
        private final WeakReference<BitmapWorkTask> bitmapWorkTaskWeakReference;

        public AsyncDrawable(Resources res, Bitmap bitmap, BitmapWorkTask task) {
            super(res, bitmap);
            bitmapWorkTaskWeakReference = new WeakReference<BitmapWorkTask>(task);
        }

        public BitmapWorkTask getBitmapWorkTask() {
            return bitmapWorkTaskWeakReference.get();
        }
    }


    public static boolean canclePotentialWork(int resid, ImageView imageView) {
        final BitmapWorkTask bitmapWorkTask = getBitmapWorkTask(imageView);
        if (bitmapWorkTask != null) {
            final int resId = bitmapWorkTask.resId;
            if (resId == 0 || resId != resid) {
                bitmapWorkTask.cancel(true);
            } else {
                return false;
            }

        }
        return true;
    }

    private static BitmapWorkTask getBitmapWorkTask(ImageView imageView) {
        if (imageView != null) {
            final Drawable drawable = imageView.getDrawable();
            if (drawable instanceof AsyncDrawable) {
                final AsyncDrawable asyncDrawable = (AsyncDrawable) drawable;
                return asyncDrawable.getBitmapWorkTask();
            }
        }
        return null;
    }

    /**
     * 添加bitmap至内存缓存
     *
     * @param key
     * @param bitmap
     */
    public void addBitmapToMemoryCache(String key, Bitmap bitmap) {
        if (getBitmapFromMemoryCache(key) == null) {
            mCacheList.put(key, bitmap);
        }
    }

    /**
     * 从内存缓存中获取bitmap
     *
     * @param key
     * @return
     */
    public Bitmap getBitmapFromMemoryCache(String key) {
        return mCacheList.get(key);
    }


}
