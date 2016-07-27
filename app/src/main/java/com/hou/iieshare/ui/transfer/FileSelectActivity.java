package com.hou.iieshare.ui.transfer;

import android.os.Build;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.design.widget.TabLayout;
import android.support.v4.app.Fragment;
import android.support.v4.view.ViewPager;
import android.support.v7.widget.Toolbar;
import android.text.TextUtils;
import android.view.View;

import com.hou.iieshare.R;
import com.hou.iieshare.ui.common.BaseActivity;
import com.hou.iieshare.ui.common.FragmentAdapter;
import com.hou.iieshare.ui.transfer.fragment.AppFragment;
import com.hou.iieshare.ui.transfer.fragment.OnSelectItemClickListener;
import com.hou.iieshare.utils.ToastUtils;

import java.util.ArrayList;
import java.util.List;

public class FileSelectActivity extends BaseActivity implements OnSelectItemClickListener{

    private String title;
    private String userName = Build.DEVICE;
    private TabLayout tabLayout;
    private ViewPager viewPager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_file_select);
        Toolbar toolbar = (Toolbar) findViewById(R.id.activity_file_select_toolbar);
        setSupportActionBar(toolbar);

        title = toolbar.getTitle().toString();
        if (TextUtils.isEmpty(title))
            title = getString(R.string.title_activity_file_select);

        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.activity_file_select_fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                //if(Cache.selectedList.size()>0)
                //    startActivity(new Intent(FileSelectActivity.this,
                //            RadarScanActivity.class).putExtra("name",userName));
                //else
                    ToastUtils.showTextToast(getApplicationContext(),"请选择文件");

                Snackbar.make(view, "Replace with your own action", Snackbar.LENGTH_LONG)
                        .setAction("Action", null).show();
            }
        });

        List<String> titles = new ArrayList<>();
        titles.add(getString(R.string.app));
        titles.add(getString(R.string.picture));

        tabLayout = (TabLayout) findViewById(R.id.activity_file_select_tabLayout);
        tabLayout.addTab(tabLayout.newTab().setText(titles.get(0)));
        tabLayout.addTab(tabLayout.newTab().setText(titles.get(1)));

        viewPager = (ViewPager) findViewById(R.id.activity_file_select_viewpager);
        List <Fragment> fragments = new ArrayList<>();
        fragments.add(new AppFragment());
        //fragments.add(new PictureFragment());

        FragmentAdapter adapter = new FragmentAdapter(getSupportFragmentManager(),fragments,titles);
        viewPager.setAdapter(adapter);

        tabLayout.setupWithViewPager(viewPager);
        tabLayout.setTabsFromPagerAdapter(adapter);
    }

    @Override
    public void onItemClicked(int type) {

    }
}
