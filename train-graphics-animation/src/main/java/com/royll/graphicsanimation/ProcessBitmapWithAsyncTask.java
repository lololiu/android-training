package com.royll.graphicsanimation;

import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.os.AsyncTask;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.widget.ImageView;

import java.lang.ref.WeakReference;

/**
 * 主要解决两个问题：
 * 1.将资源图片转成Bitmap，或者从网络中获取图片资源的IO相关操作不宜在主线程中进行
 *   用AsyncTask转换图片后再set到ImageView中
 * 2.有时候从网络上读取图片资源会耗费很长时间，而此时界面上的ImageView就会一直是空白
 *   用户体验不佳，解决方法是先用一张占位符图片，等真正要显示的图片Bitmap获取到后再set进去
 */
public class ProcessBitmapWithAsyncTask extends AppCompatActivity {
    ImageView mImageView;
    private Bitmap mPlaceHolderBitmap;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_process_bitmap_with_async_task);
        mImageView = (ImageView) findViewById(R.id.imageview1);
        mPlaceHolderBitmap = BitmapUtil.decodeSampledBitmapFromResource(getResources(), R.mipmap.placeholder, 800, 800);
        loadBitmap(R.mipmap.test, mImageView);
    }

    private void loadBitmap(int resId, ImageView imageView) {
        if (canclePotentialWork(resId, imageView)) {
            BitmapWorkTask task = new BitmapWorkTask(mImageView);
            task.execute(resId);
            AsyncDrawable asyncDrawable = new AsyncDrawable(getResources(), mPlaceHolderBitmap, task);
            mImageView.setImageDrawable(asyncDrawable);
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
            return BitmapUtil.decodeSampledBitmapFromResource(getResources(), resId, 800, 800);
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
}
