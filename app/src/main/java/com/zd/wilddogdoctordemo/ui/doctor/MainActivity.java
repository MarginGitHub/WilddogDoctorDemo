package com.zd.wilddogdoctordemo.ui.doctor;

import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.design.widget.BottomNavigationView;
import android.view.MenuItem;

import com.zd.wilddogdoctordemo.R;
import com.zd.wilddogdoctordemo.adapter.VideoConversationFragmentPagerAdapter;
import com.zd.wilddogdoctordemo.ui.BaseActivity;
import com.zd.wilddogdoctordemo.ui.View.NoSlidingViewPaper;

import butterknife.BindView;
import butterknife.ButterKnife;

public class MainActivity extends BaseActivity {


    @BindView(R.id.content)
    NoSlidingViewPaper mContent;
    @BindView(R.id.navigation)
    BottomNavigationView mNavigation;
    private VideoConversationFragmentPagerAdapter mPagerAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        ButterKnife.bind(this);
        initViews();
    }

    @Override
    protected void initViews() {
        mPagerAdapter = new VideoConversationFragmentPagerAdapter(getSupportFragmentManager());
        mContent.setAdapter(mPagerAdapter);
        mNavigation.setOnNavigationItemSelectedListener(new BottomNavigationView.OnNavigationItemSelectedListener() {
            @Override
            public boolean onNavigationItemSelected(@NonNull MenuItem item) {
                switch (item.getItemId()) {
                    case R.id.online_doctors:
                        mContent.setCurrentItem(0);
                        break;
                    case R.id.video_history:
                        mContent.setCurrentItem(1);
                        break;
                    case R.id.about_me:
                        mContent.setCurrentItem(2);
                        break;

                }
                return true;
            }
        });
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
    }
}
