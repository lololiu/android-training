package com.royll.graphicsanimation;

import android.graphics.BitmapFactory;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.widget.ImageView;

public class LoadLargeBitmapActivty extends AppCompatActivity {

    private static final String TAG = "LoadLargeBitmapActivty";
    private ImageView mImageView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_load_large_bitmap_activty);
        mImageView = (ImageView) findViewById(R.id.imageview);
        mImageView.setImageBitmap(BitmapUtil.decodeSampledBitmapFromResource(getResources(),R.mipmap.test,800,800));
    }


}
