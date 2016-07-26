package com.hou.iieshare.ui.main;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import com.hou.iieshare.R;
import com.hou.p2pmanager.p2ptimer.OSTimer;
import com.hou.p2pmanager.p2ptimer.Timeout;

public class WelcomeActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_welcome);

        Timeout timeout = new Timeout()
        {
            @Override
            public void onTimeOut()
            {
                startActivity(new Intent(WelcomeActivity.this, MainActivity.class));
                finish();
            }
        };

        new OSTimer(null, timeout, 2 * 1000).start();
    }
}
