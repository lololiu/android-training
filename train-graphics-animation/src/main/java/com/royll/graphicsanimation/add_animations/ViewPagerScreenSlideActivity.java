package com.royll.graphicsanimation.add_animations;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentStatePagerAdapter;
import android.support.v4.view.ViewPager;

import com.royll.graphicsanimation.R;

/**
 * Created by liulou on 2016/8/11.
 * desc:
 */
public class ViewPagerScreenSlideActivity extends FragmentActivity {


    ViewPager mViewPager;
    ScreenSlidePagerAdapter mScreenSlideAdapter;
    private static final int NUM_PAGES = 5;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_viewpage_slide);
        mViewPager = (ViewPager) findViewById(R.id.viewpager);
        mScreenSlideAdapter=new ScreenSlidePagerAdapter(getSupportFragmentManager());
        mViewPager.setAdapter(mScreenSlideAdapter);
    }

    private class ScreenSlidePagerAdapter extends FragmentStatePagerAdapter {
        public ScreenSlidePagerAdapter(FragmentManager fm) {
            super(fm);
        }

        @Override
        public int getCount() {
            return NUM_PAGES;
        }

        @Override
        public Fragment getItem(int position) {
            return new ViewPagerSlideFragment();
        }
    }
}
