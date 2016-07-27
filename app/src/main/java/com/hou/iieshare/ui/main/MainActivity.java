package com.hou.iieshare.ui.main;

import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;

import com.hou.iieshare.R;
import com.hou.iieshare.sdk.cache.Cache;
import com.hou.iieshare.ui.transfer.FileSelectActivity;
import com.hou.iieshare.utils.PreferenceUtils;

public class MainActivity extends AppCompatActivity implements View.OnClickListener {

    private EditText nameEdit;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        Button send =(Button)findViewById(R.id.activity_main_i_send);
        send.setOnClickListener( this);
        Button receive = (Button)findViewById(R.id.activity_main_i_receive);
        receive.setOnClickListener(this);
        Button sendtoPC = (Button)findViewById(R.id.main_file_to_PC);
        sendtoPC.setOnClickListener(this);

        nameEdit = (EditText) findViewById(R.id.activity_main_name_edit);
        nameEdit.setText((String) PreferenceUtils.getParam(MainActivity.this, "String",
                Build.DEVICE));


    }

    @Override
    public void onClick(View view) {

        switch (view.getId()){
            case R.id.activity_main_i_send :
                Cache.selectedList.clear();
                startActivity(new Intent(MainActivity.this, FileSelectActivity.class)
                        .putExtra("name",nameEdit.getText().toString()));
                break;


        }
    }
}
