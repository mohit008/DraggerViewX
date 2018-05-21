package com.dragger;

import android.app.Activity;
import android.content.Intent;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.provider.Settings;
import android.view.View;
import android.widget.Button;

import com.dragger.service.OnTopService;


/**
 * Author @ Mohit Soni on 02-05-2018 06:22 PM.
 */

public class MainSwipper extends Activity {

    int PERMISSIONS_REQUEST = 100;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main_swipper);

        ((Button) findViewById(R.id.main_button)).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
//                startActivity(new Intent(MainSwipper.this, SwipeActivity.class));
            }
        });
        callPermission();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == PERMISSIONS_REQUEST) {
            callPermission();
        }
    }

    public void callPermission(){
        if (Build.VERSION.SDK_INT >= 23) {
            if (!Settings.canDrawOverlays(this)) {
                Intent intent = new Intent(Settings.ACTION_MANAGE_OVERLAY_PERMISSION,
                        Uri.parse("package:" + getApplicationContext().getPackageName()));
                startActivityForResult(intent,PERMISSIONS_REQUEST);
            } else {
                startService(new Intent(MainSwipper.this, OnTopService.class));
            }
        }else{
            startService(new Intent(MainSwipper.this, OnTopService.class));
        }
    }
}
