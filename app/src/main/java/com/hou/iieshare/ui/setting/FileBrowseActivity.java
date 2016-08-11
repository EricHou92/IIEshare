package com.hou.iieshare.ui.setting;

import android.os.Bundle;
import android.support.design.widget.TabLayout;
import android.support.v4.view.ViewPager;
import android.support.v7.app.ActionBar;
import android.support.v7.widget.Toolbar;

import com.hou.iieshare.R;
import com.hou.iieshare.ui.common.BaseActivity;
import com.hou.iieshare.ui.common.FragmentAdapter;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by ciciya on 2016/8/11.
 */
public class FileBrowseActivity extends BaseActivity {
    private ViewPager mViewPager;
    private TabLayout mTabLayout;

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_file_browse);

        Toolbar toolbar = (Toolbar) findViewById(R.id.activity_receive_browse_toolbar);
        setSupportActionBar(toolbar);
        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null)
            actionBar.setDisplayHomeAsUpEnabled(true);

        initWidget();
    }

    private void initWidget()
    {
        List<String> titles = new ArrayList<>();
        titles.add(getString(R.string.app));
        titles.add(getString(R.string.picture));

        mTabLayout = (TabLayout) findViewById(R.id.activity_receive_browse_tabLayout);
        mTabLayout.addTab(mTabLayout.newTab().setText(titles.get(0)));
        mTabLayout.addTab(mTabLayout.newTab().setText(titles.get(1)));

        mViewPager = (ViewPager) findViewById(R.id.activity_receive_browse_viewpager);

        List<android.support.v4.app.Fragment> fragments = new ArrayList<>();

        FragmentAdapter adapter = new FragmentAdapter(getSupportFragmentManager(),
                fragments, titles);
        mViewPager.setAdapter(adapter);

        mTabLayout.setupWithViewPager(mViewPager);
        mTabLayout.setTabsFromPagerAdapter(adapter);
    }
}
