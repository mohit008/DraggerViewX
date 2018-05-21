package com.dragger.service;

import android.app.Service;
import android.content.Intent;
import android.graphics.PixelFormat;
import android.media.Image;
import android.net.Uri;
import android.os.IBinder;
import android.support.annotation.Nullable;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.VideoView;

import com.dragger.R;
import com.dragger.groupx.AlwaysOnTopDragger;
import com.dragger.util.WindowStat;

/**
 * Created by mohit.soni on 24-04-2018.
 */

public class OnTopService extends Service implements WindowStat {
    View view;
    WindowManager windowManager;
    WindowManager.LayoutParams params;

    int _y = 0, y_ = 0, _x = 0, x_ = 0;

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
                WindowManager.LayoutParams.MATCH_PARENT,
                WindowManager.LayoutParams.MATCH_PARENT,
                WindowManager.LayoutParams.TYPE_SYSTEM_ALERT,
                WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE,
                PixelFormat.TRANSLUCENT);

        params.x = 0;
        params.y = 0;

        final LayoutInflater layoutInflater = (LayoutInflater) getBaseContext().getSystemService(LAYOUT_INFLATER_SERVICE);
        view = layoutInflater.inflate(R.layout.ontop_layout, null);

        final AlwaysOnTopDragger dragLayout = (AlwaysOnTopDragger) view.findViewById(R.id.dragLayout1);
        dragLayout.setID(R.id.rl, R.id.lvOnTop, R.layout.ontop_layout);
        dragLayout.setWindow(this);

        ListView lvOnTop = (ListView) view.findViewById(R.id.lvOnTop);

        lvOnTop.setAdapter(new BaseAdapter() {
            @Override
            public int getCount() {
                return 50;
            }

            @Override
            public String getItem(int i) {
                return "Push" + i;
            }

            @Override
            public long getItemId(int i) {
                return i;
            }

            @Override
            public View getView(int i, View rView, ViewGroup viewGroup) {
                View view = rView;
                if (view == null) {
                    view = layoutInflater.inflate(android.R.layout.simple_list_item_1, viewGroup, false);
                }
                ((TextView) view.findViewById(android.R.id.text1)).setText(getItem(i));
                return view;
            }
        });

//        VideoView videoView = (VideoView) view.findViewById(R.id.video1);
//        Uri uri = Uri.parse("android.resource://" + getPackageName() + "/" + R.raw.sample);
//        videoView.setVideoURI(uri);
//        videoView.start();

        ImageView imageView = (ImageView) view.findViewById(R.id.img);

        windowManager.addView(view, params);
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
        Log.d("OnTopService" + "_" + tag, msg);
    }

    @Override
    public void setDimen(int x, int y, int width, int height, int direction) {
        switch (direction) {
            case MotionEvent.ACTION_MOVE: {
                params.x = _x + (x - x_);
                params.y = _y + (y - y_);

                windowManager.updateViewLayout(view, params);
                /*log("ACTION_MOVE", params.x + ":" + params.y);*/
                break;
            }
            case MotionEvent.ACTION_DOWN: {
                _y = params.y;
                y_ = y;
                /*log("ACTION_DOWN _ Y", _y + ":" + y_);*/

                _x = params.x;
                x_ = x;
                /*log("ACTION_DOWN _ X", _x + ":" + x_);*/
                break;
            }

            case MotionEvent.ACTION_MASK: {
                params.x = x;
                params.y = y;

                params.width = width;
                params.height = height;

                windowManager.updateViewLayout(view, params);
                break;
            }
        }
    }
}
