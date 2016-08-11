package com.royll.graphicsanimation.add_animations;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.ScrollView;

import com.royll.graphicsanimation.R;

/**
 * Created by liulou on 2016/8/11.
 * desc:
 */
public class CrossFadingTwoViewsActicvity extends AppCompatActivity {

    ScrollView mScrollView;
    ProgressBar mProgressBar;

    private int mShortAnimationDuration;//系统默认“短”的动画时间

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.acticity_crossfading_two_views);
        mScrollView= (ScrollView) findViewById(R.id.content);
        mProgressBar= (ProgressBar) findViewById(R.id.loading);
        mScrollView.setVisibility(View.VISIBLE);
        mScrollView.setAlpha(0f);

        mShortAnimationDuration=getResources().getInteger(android.R.integer.config_shortAnimTime);

        mScrollView.animate()
                .alpha(1f)
                .setDuration(mShortAnimationDuration)
                .setListener(null);
        mProgressBar.animate()
                .alpha(0f)
                .setDuration(mShortAnimationDuration)
                .setListener(new AnimatorListenerAdapter() {
                    @Override
                    public void onAnimationEnd(Animator animation) {
                        mProgressBar.setVisibility(View.GONE);
                    }
                });
    }
}
