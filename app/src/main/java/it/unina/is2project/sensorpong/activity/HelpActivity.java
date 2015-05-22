package it.unina.is2project.sensorpong.activity;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentStatePagerAdapter;
import android.support.v4.view.PagerAdapter;
import android.support.v4.view.ViewPager;
import android.widget.RadioGroup;

import java.util.ArrayList;
import java.util.List;

import it.unina.is2project.sensorpong.R;

public class HelpActivity extends FragmentActivity {
    private static final int NUM_PAGES = 6;
    private ViewPager mPager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_screen_slide);

        PagerAdapter mPagerAdapter = new ScreenSlidePagerAdapter(getSupportFragmentManager());
        mPager = (ViewPager) findViewById(R.id.mainPager);
        mPager.setAdapter(mPagerAdapter);

        final RadioGroup radioGroup = (RadioGroup) findViewById(R.id.mainRadio);

        mPager.setOnPageChangeListener(new ViewPager.OnPageChangeListener() {
            @Override
            public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {
            }

            @Override
            public void onPageSelected(int position) {
                switch (position) {
                    case 0:
                        radioGroup.check(R.id.radioButton1);
                        break;
                    case 1:
                        radioGroup.check(R.id.radioButton2);
                        break;
                    case 2:
                        radioGroup.check(R.id.radioButton3);
                        break;
                    case 3:
                        radioGroup.check(R.id.radioButton4);
                        break;
                    case 4:
                        radioGroup.check(R.id.radioButton5);
                        break;
                    case 5:
                        radioGroup.check(R.id.radioButton6);
                        break;
                    default:
                        break;
                }
            }

            @Override
            public void onPageScrollStateChanged(int state) {
            }
        });
    }

    @Override
    public void onBackPressed() {
        if (mPager.getCurrentItem() == 0) {
            super.onBackPressed();
        } else {
            mPager.setCurrentItem(mPager.getCurrentItem() - 1);
        }
    }

    private class ScreenSlidePagerAdapter extends FragmentStatePagerAdapter {

        private List<Fragment> fragments = new ArrayList<>();

        public ScreenSlidePagerAdapter(FragmentManager fm) {
            super(fm);

            for (int i = 0; i < NUM_PAGES; i++) {
                fragments.add(new ScreenSlidePageFragment());
                Bundle args = new Bundle();
                args.putInt("id", i + 1);
                fragments.get(i).setArguments(args);
            }
        }

        @Override
        public Fragment getItem(int position) {
            return fragments.get(position);
        }

        @Override
        public int getCount() {
            return NUM_PAGES;
        }
    }
}
