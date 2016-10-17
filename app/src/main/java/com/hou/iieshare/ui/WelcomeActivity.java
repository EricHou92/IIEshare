package com.hou.iieshare.ui;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.widget.ImageView;

import com.hou.iieshare.R;
import com.hou.p2pmanager.p2putils.OSTimer;
import com.hou.p2pmanager.p2putils.Timeout;

public class WelcomeActivity extends Activity {

    private ImageView welcome;

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_welcome);
        welcome = (ImageView) findViewById(R.id.activity_welcome_iie);
        Timeout timeout = new Timeout() {
            @Override
            public void onTimeOut() {
                startActivity(new Intent(WelcomeActivity.this, MainActivity.class));
                finish();
            }
        };

        new OSTimer(null, timeout, 2 * 1000).start();
    }

}
