package com.royll.graphicsanimation;

import android.annotation.TargetApi;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.transition.Explode;
import android.transition.TransitionManager;
import android.view.View;
import android.widget.Button;
import android.widget.RelativeLayout;

/**
 * Created by liulou on 2016/8/8.
 * desc:
 */
public class CreateSceneTransitionsActivity extends AppCompatActivity {

    RelativeLayout mRootView;
    View mView1, mView2, mView3, mView4;
    Button mBtn;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.layout_create_scene_apply_translation);
        mRootView = (RelativeLayout) findViewById(R.id.root_view);
        mView1 = findViewById(R.id.red_box);
        mView2 = findViewById(R.id.green_box);
        mView3 = findViewById(R.id.blue_box);
        mView4 = findViewById(R.id.black_box);
        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                changeViews();
            }
        },1000);
    }

    @TargetApi(Build.VERSION_CODES.LOLLIPOP)
    private void changeViews() {
        TransitionManager.beginDelayedTransition(mRootView, new Explode());

        mView1.setVisibility(mView1.getVisibility() == View.VISIBLE ? View.INVISIBLE : View.VISIBLE);
        mView2.setVisibility(mView2.getVisibility() == View.VISIBLE ? View.INVISIBLE : View.VISIBLE);
        mView3.setVisibility(mView3.getVisibility() == View.VISIBLE ? View.INVISIBLE : View.VISIBLE);
        mView4.setVisibility(mView4.getVisibility() == View.VISIBLE ? View.INVISIBLE : View.VISIBLE);
    }
}
