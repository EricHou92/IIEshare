package com.hou.iieshare.ui;

import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import com.hou.iieshare.R;
import com.hou.iieshare.utils.Cache;

public class MainActivity extends AppCompatActivity implements View.OnClickListener {

    private TextView nameEdit;

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        Button send = (Button) findViewById(R.id.activity_main_i_send);
        if (send != null) {
            send.setOnClickListener(this);
        }
        Button receive = (Button) findViewById(R.id.activity_main_i_receive);
        if (receive != null) {
            receive.setOnClickListener(this);
        }

        nameEdit = (TextView) findViewById(R.id.activity_main_name_edit);
        if (nameEdit != null) {
            nameEdit.setText(Build.DEVICE);
        }
    }

    @Override
    public void onClick(View view)
    {
        switch (view.getId())
        {
            case R.id.activity_main_i_send :
                Cache.selectedList.clear();
                startActivity(new Intent(MainActivity.this, SendActivity.class)
                        .putExtra("name", nameEdit.getText().toString()));
                break;

            case R.id.activity_main_i_receive :
                Cache.selectedList.clear();
                startActivity(new Intent(MainActivity.this, ReceiveActivity.class)
                        .putExtra("name", nameEdit.getText().toString()));
                break;
        }
    }

}
