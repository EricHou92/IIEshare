package com.hou.iieshare.utils;


import android.app.Notification;
import android.app.NotificationManager;
import android.content.Context;
import android.net.Uri;
import android.os.Handler;
import android.os.Looper;
import android.os.Vibrator;
import android.view.Gravity;
import android.widget.Toast;

import com.hou.iieshare.MyApplication;
import com.hou.iieshare.R;


/**
 * Created by ciciya on 2015/8/5.
 */
public class ToastUtils
{

    private static Toast toast = null;
    private static Handler handler = new Handler(Looper.getMainLooper());

    public static void showTextToast(Context context, String msg)
    {
        showMessage(context, msg, Toast.LENGTH_SHORT);
    }

    public static void showMessage(final Context act, final String msg, final int len)
    {
        new Thread(new Runnable()
        {
            public void run()
            {
                handler.post(new Runnable()
                {
                    @Override
                    public void run()
                    {
                        if (toast != null)
                        {
                            toast.setText(msg);
                        }
                        else
                        {
                            toast = Toast.makeText(act, msg, len);
                            toast.setGravity(Gravity.BOTTOM, 0, 100);
                        }
                        toast.show();
                    }
                });
            }
        }).start();
    }

    public static void showVoice(){
        //增加声音震动提醒
        NotificationManager manger = (NotificationManager) MyApplication.getInstance().getSystemService(Context.NOTIFICATION_SERVICE);
        Notification notification = new Notification();
        //notification.defaults= Notification.DEFAULT_SOUND;
        notification.sound = Uri.parse("android.resource://" + MyApplication.getInstance().getPackageName() + "/" + R.raw.bell);
        manger.notify(1, notification);
        Vibrator vibrator = (Vibrator)MyApplication.getInstance().getSystemService(Context.VIBRATOR_SERVICE);
        vibrator.vibrate(new long[]{300,1000},-1);
    }

}
