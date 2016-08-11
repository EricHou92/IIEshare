package com.hou.iieshare.ui.main;

import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;

import com.hou.iieshare.R;
import com.hou.iieshare.sdk.cache.Cache;
import com.hou.iieshare.ui.setting.AboutActivity;
import com.hou.iieshare.ui.setting.FileBrowseActivity;
import com.hou.iieshare.ui.transfer.RadarScanActivity;
import com.hou.iieshare.ui.transfer.ReceiveActivity;
import com.hou.iieshare.utils.PreferenceUtils;

public class MainActivity extends AppCompatActivity implements View.OnClickListener {

    private EditText nameEdit;

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Toolbar toolbar = (Toolbar) findViewById(R.id.activity_main_toolbar);
        setSupportActionBar(toolbar);

        toolbar.setOnMenuItemClickListener(onMenuItemClick);

        Button send = (Button) findViewById(R.id.activity_main_i_send);
        send.setOnClickListener(this);
        Button receive = (Button) findViewById(R.id.activity_main_i_receive);
        receive.setOnClickListener(this);

        nameEdit = (EditText) findViewById(R.id.activity_main_name_edit);
        nameEdit.setText((String) PreferenceUtils.getParam(MainActivity.this, "String",
                Build.DEVICE));
    }

    @Override
    public void onPause()
    {
        super.onPause();
        //记住用户修改的名字
        PreferenceUtils.setParam(MainActivity.this, "String", nameEdit.getText()
                .toString());
    }

    @Override
    public void onClick(View view)
    {
        switch (view.getId())
        {
            case R.id.activity_main_i_send :
                Cache.selectedList.clear();
                startActivity(new Intent(MainActivity.this, RadarScanActivity.class)
                        .putExtra("name", nameEdit.getText().toString()));
                break;

            case R.id.activity_main_i_receive :
                Cache.selectedList.clear();
                startActivity(new Intent(MainActivity.this, ReceiveActivity.class)
                        .putExtra("name", nameEdit.getText().toString()));
                break;
        }
    }

    private Toolbar.OnMenuItemClickListener onMenuItemClick = new Toolbar.OnMenuItemClickListener()
    {
        @Override
        public boolean onMenuItemClick(MenuItem menuItem)
        {
            switch (menuItem.getItemId())
            {
                case R.id.menu_item_receive_directory :
                    startActivity(new Intent(MainActivity.this, FileBrowseActivity.class));
                    break;
                case R.id.menu_item_about :
                    startActivity(new Intent(MainActivity.this, AboutActivity.class));
                    break;
            }
            return true;
        }
    };

    @Override
    public boolean onCreateOptionsMenu(Menu menu)
    {
        // 為了讓 Toolbar 的 Menu 有作用，這邊的程式不可以拿掉
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }
}
