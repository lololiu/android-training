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

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        mLoadBigBitMapBtn = (Button) findViewById(R.id.load_large_bitmap_btn);
        mProcessBitmapWithAsyncTaskBtn = (Button) findViewById(R.id.process_bitmap_with_asynctask);
        mCachingBitmap= (Button) findViewById(R.id.caching_bitmap);
        mLoadBigBitMapBtn.setOnClickListener(this);
        mProcessBitmapWithAsyncTaskBtn.setOnClickListener(this);
        mCachingBitmap.setOnClickListener(this);
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
        }
    }

    private void startActivityHelp(Class className) {
        Intent it = new Intent(MainActivity.this, className);
        startActivity(it);
    }
}
