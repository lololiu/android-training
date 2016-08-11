package com.royll.graphicsanimation;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;

import com.royll.graphicsanimation.add_animations.CrossFadingTwoViewsActicvity;
import com.royll.graphicsanimation.add_animations.ViewPagerScreenSlideActivity;

public class MainActivity extends AppCompatActivity implements View.OnClickListener {

    private Button mLoadBigBitMapBtn;
    private Button mProcessBitmapWithAsyncTaskBtn;
    private Button mCachingBitmap;
    private Button mCreateSceneApplyTranstion;
    private Button mCrossFadingTwoView;
    private Button mViewpager4ScreenSlide;
    private Button mCardFlip;
    private Button mZoomView;
    private Button mLayoutChange;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        mLoadBigBitMapBtn = (Button) findViewById(R.id.load_large_bitmap_btn);
        mProcessBitmapWithAsyncTaskBtn = (Button) findViewById(R.id.process_bitmap_with_asynctask);
        mCachingBitmap= (Button) findViewById(R.id.caching_bitmap);
        mCreateSceneApplyTranstion= (Button) findViewById(R.id.create_scene_and_apply_transition);
        mCrossFadingTwoView= (Button) findViewById(R.id.crossfading_two_views_btn);
        mViewpager4ScreenSlide= (Button) findViewById(R.id.using_viewpager_for_screen_slides_btn);
        mCardFlip= (Button) findViewById(R.id.card_flip_btn);
        mZoomView= (Button) findViewById(R.id.zoom_view_btn);
        mLayoutChange= (Button) findViewById(R.id.layout_change_btn);

        mLoadBigBitMapBtn.setOnClickListener(this);
        mProcessBitmapWithAsyncTaskBtn.setOnClickListener(this);
        mCachingBitmap.setOnClickListener(this);
        mCreateSceneApplyTranstion.setOnClickListener(this);
        mCrossFadingTwoView.setOnClickListener(this);
        mViewpager4ScreenSlide.setOnClickListener(this);
        mCardFlip.setOnClickListener(this);
        mZoomView.setOnClickListener(this);
        mLayoutChange.setOnClickListener(this);
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.load_large_bitmap_btn:
                startActivityHelp(LoadLargeBitmapActivty.class);
                break;
            case R.id.process_bitmap_with_asynctask:
                startActivityHelp(ProcessBitmapWithAsyncTask.class);
                break;
            case R.id.caching_bitmap:
                startActivityHelp(CachingBitmapActivity.class);
                break;
            case R.id.create_scene_and_apply_transition:
                startActivityHelp(CreateSceneTransitionsActivity.class);
                break;
            case R.id.crossfading_two_views_btn:
                startActivityHelp(CrossFadingTwoViewsActicvity.class);
                break;
            case R.id.using_viewpager_for_screen_slides_btn:
                startActivityHelp(ViewPagerScreenSlideActivity.class);
                break;
            default:
                break;
        }
    }

    private void startActivityHelp(Class className) {
        Intent it = new Intent(MainActivity.this, className);
        startActivity(it);
    }
}
