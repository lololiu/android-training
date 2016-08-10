package com.royll.graphicsanimation;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;

public class MainActivity extends AppCompatActivity implements View.OnClickListener {

    private Button mLoadBigBitMapBtn;
    private Button mProcessBitmapWithAsyncTaskBtn;
    private Button mCachingBitmap;
    private Button mCreateSceneApplyTranstion;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        mLoadBigBitMapBtn = (Button) findViewById(R.id.load_large_bitmap_btn);
        mProcessBitmapWithAsyncTaskBtn = (Button) findViewById(R.id.process_bitmap_with_asynctask);
        mCachingBitmap= (Button) findViewById(R.id.caching_bitmap);
        mCreateSceneApplyTranstion= (Button) findViewById(R.id.create_scene_and_apply_transition);
        mLoadBigBitMapBtn.setOnClickListener(this);
        mProcessBitmapWithAsyncTaskBtn.setOnClickListener(this);
        mCachingBitmap.setOnClickListener(this);
        mCreateSceneApplyTranstion.setOnClickListener(this);
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
        }
    }

    private void startActivityHelp(Class className) {
        Intent it = new Intent(MainActivity.this, className);
        startActivity(it);
    }
}
