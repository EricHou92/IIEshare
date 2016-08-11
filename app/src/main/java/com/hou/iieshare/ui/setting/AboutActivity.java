package com.hou.iieshare.ui.setting;

import android.os.Bundle;
import android.support.design.widget.CollapsingToolbarLayout;
import android.support.v7.app.ActionBar;
import android.support.v7.widget.Toolbar;
import android.widget.TextView;

import com.hou.iieshare.R;
import com.hou.iieshare.ui.common.BaseActivity;
import com.hou.p2pmanager.p2pcore.P2PManager;

/**
 * Created by ciciya on 2016/8/11.
 */
public class AboutActivity extends BaseActivity{
    private CollapsingToolbarLayout collapsingToolbarLayout;

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_about);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null)
            actionBar.setDisplayHomeAsUpEnabled(true);

        collapsingToolbarLayout = (CollapsingToolbarLayout) findViewById(R.id.activity_about_collapsing_toolbar);
        collapsingToolbarLayout.setTitle(getString(R.string.menu_about));

        TextView receivePath = (TextView) findViewById(R.id.about_receive_path_content);
        receivePath.setText(P2PManager.getSaveDir());    }

}
