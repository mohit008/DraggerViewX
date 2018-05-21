package com.dragger.service;

import android.app.Service;
import android.content.Intent;
import android.graphics.PixelFormat;
import android.net.Uri;
import android.os.IBinder;
import android.support.annotation.Nullable;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.view.WindowManager;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.VideoView;

import com.dragger.R;


/**
 * Created by Mohit Soni on 10-05-2018.
 */

public class AddViewByService extends Service {
    View view;
    WindowManager windowManager;
    WindowManager.LayoutParams params;
    RelativeLayout relativeLayout;

    int _y = 0, y_ = 0, _x = 0, x_ = 0;

    int video_position = 0;
    boolean status = false;

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public void onCreate() {
        super.onCreate();
        windowManager = (WindowManager) getSystemService(WINDOW_SERVICE);
        params = new WindowManager.LayoutParams(
                WindowManager.LayoutParams.WRAP_CONTENT,
                WindowManager.LayoutParams.WRAP_CONTENT,
                WindowManager.LayoutParams.TYPE_SYSTEM_ALERT,
                WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE,
                PixelFormat.TRANSLUCENT);

        params.x = 0;
        params.y = 0;

        relativeLayout = new RelativeLayout(this);
        relativeLayout.setLayoutParams(new LinearLayout.LayoutParams(LinearLayout.LayoutParams.WRAP_CONTENT,
                LinearLayout.LayoutParams.WRAP_CONTENT));
        relativeLayout.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                switch (event.getAction()) {
                    case MotionEvent.ACTION_MOVE: {
                        status = true;
                        // update param of window
                        params.x = _x + ((int) event.getRawX() - x_);
                        params.y = _y + ((int) event.getRawY() - y_);

                        windowManager.updateViewLayout(relativeLayout, params);
                        log("ACTION_MOVE", params.x + ":" + params.y);
                        break;
                    }
                    case MotionEvent.ACTION_UP: {
                        break;
                    }
                    case MotionEvent.ACTION_DOWN: {
                        status = true;
                        _y = params.y;
                        y_ = (int) event.getRawY();
                        log("ACTION_DOWN _ Y", _y + ":" + y_);

                        _x = params.x;
                        x_ = (int) event.getRawX();
                        log("ACTION_DOWN _ X", _x + ":" + x_);
                        break;
                    }
                    case MotionEvent.ACTION_CANCEL:
                        status = false;
                        break;
                }
                return status;
            }
        });

        VideoView videoView = new VideoView(this);
        Uri uri = Uri.parse("android.resource://" + getPackageName() + "/" + R.raw.sample);
        videoView.setVideoURI(uri);
        videoView.setLayoutParams(new LinearLayout.LayoutParams(500, 500));
        videoView.start();
        videoView.seekTo(video_position);

        relativeLayout.addView(videoView);

        windowManager.addView(relativeLayout, params);

        /*WindowManager windowManager = (WindowManager) getSystemService(WINDOW_SERVICE);
        LayoutInflater layoutInflater = (LayoutInflater) getBaseContext().getSystemService(LAYOUT_INFLATER_SERVICE);
        view = layoutInflater.inflate(R.layout.activ, null);
        AlwaysOnTop2 dragLayout = (AlwaysOnTop2) view.findViewById(R.id.dragLayout1);
        dragLayout.setID(R.id.rl, R.id.text2);
        VideoView videoView = (VideoView)view.findViewById(R.id.video1);
        Uri uri = Uri.parse("android.resource://" + getPackageName() + "/" + R.raw.sample);
        videoView.setVideoURI(uri);
        videoView.start();

        windowManager.addView(view, p);*/
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        if (view != null) {
            WindowManager wm = (WindowManager) getSystemService(WINDOW_SERVICE);
            wm.removeView(view);
        }
    }

    public void log(String tag, String msg) {
        Log.d("AddViewByService", msg);
    }
}
