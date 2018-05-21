package com.dragger;

import android.app.Activity;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.VideoView;

import com.dragger.groupx.SwipeDragger;

/**
 * Created by mohit.soni on 23-04-2018.
 */

public class SwipeActivity extends Activity {
    public static Uri uri;
    VideoView videoView;
    MediaPlayer maMediaPlayer;
    ListView lv, lvi;

    SwipeDragger dragLayout;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.swipe_main_layout);

        lvi = (ListView) findViewById(R.id.lvi);
        lv = (ListView) findViewById(R.id.lv);

        dragLayout = (SwipeDragger) findViewById(R.id.dragLayout1);
        dragLayout.setID(R.id.rl, R.id.lv, R.layout.swipe_main_layout);

        videoView = (VideoView) findViewById(R.id.video_view);
        uri = Uri.parse("android.resource://" + getPackageName() + "/" + R.raw.sample);
        maMediaPlayer = new MediaPlayer();
        videoView.setVideoURI(uri);
        videoView.start();


        BaseAdapter baseAdapter = new BaseAdapter() {
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
                    view = getLayoutInflater().inflate(android.R.layout.simple_list_item_1, viewGroup, false);
                }
                ((TextView) view.findViewById(android.R.id.text1)).setText(getItem(i));
                return view;
            }
        };

        lvi.setAdapter(baseAdapter);
        lv.setAdapter(baseAdapter);


        lv.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                dragLayout.maximize();
            }
        });
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        videoView.stopPlayback();
    }

    public void log(String tag, String msg) {
        Log.d(tag, msg);
    }
}
