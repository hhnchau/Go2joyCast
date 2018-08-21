package com.appromobile.Go2joyCast.base;

import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.Typeface;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.support.annotation.Nullable;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.text.InputType;
import android.util.Log;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.Window;
import android.view.WindowManager;
import android.view.inputmethod.EditorInfo;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.appromobile.Go2joyCast.R;
import com.appromobile.Go2joyCast.api.ApiListCallBack;
import com.appromobile.Go2joyCast.api.HandleApi;
import com.appromobile.Go2joyCast.communicator.Communicator;
import com.appromobile.Go2joyCast.sql.SqlPlayer;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by appro on 17/11/2017.
 */

public class Splash extends AppCompatActivity {

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_splash);

        Window window = getWindow();
        if (window != null) {
            window.clearFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS);
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS);
                window.setStatusBarColor(ContextCompat.getColor(this, R.color.org));
            }
        }

        List<String> list = new ArrayList<>();
        list.add("document.getElementsByTagName('video')[0].outerHTML;");
        list.add("document.getElementsByClassName('video')[0].outerHTML;");
        list.add("document.getElementsByClassName('tumblr_video_container')[0].outerHTML;");
        list.add("document.getElementsByClassName('jw-media jw-reset')[0].outerHTML;");
        list.add("document.getElementsByClassName('jwvideo')[0]childNodes[0].outerHTML;");
        list.add("document.getElementsByClassName('jw-video jw-reset')[0].outerHTML;");

        SqlPlayer.getInstance(this).insert(list);


    }

    @Override
    protected void onResume() {
        super.onResume();
        //Check WiFi On Off
        if (Communicator.getInstance().isWifi(this)) {

            Handler handler = new Handler();
            handler.postDelayed(new Runnable() {
                @Override
                public void run() {
                    Intent i = new Intent(Splash.this, Main.class);
                    startActivity(i);
                    finish();
                }
            }, 1000);


        } else {
            //Show AlertDisconnect
            Communicator.getInstance().Alert(Splash.this, getString(R.string.app_name), getString(R.string.app_need_to_connect_internet_please_turn_on_the_internet), getString(R.string.txt_1_2_setting), "");

        }
    }


}
