package com.appromobile.Go2joyCast.dialog;

import android.app.Dialog;
import android.content.Context;
import android.view.Gravity;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;

import com.appromobile.Go2joyCast.R;

import java.util.List;

/**
 * Created by Chau Huynh on 1/10/2018.
 */

public class RouterDialog {
    private static RouterDialog Instance = null;

    public static RouterDialog getInstance() {
        if (Instance == null) {
            Instance = new RouterDialog();
        }
        return Instance;
    }
    private Dialog dialog;
    private ArrayAdapter<String> mAdapter;

    public void show(Context context, List<String> list, final DialogCallback dialogCallback) {
        dialog = new Dialog(context, R.style.myDialog);
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        dialog.setContentView(R.layout.item_router_dialog);
        Window window = dialog.getWindow();
        if (window != null) {
            WindowManager.LayoutParams wlp = window.getAttributes();
            if (wlp != null) {
                wlp.gravity = Gravity.CENTER;
                window.setAttributes(wlp);
                dialog.getWindow().setLayout(WindowManager.LayoutParams.MATCH_PARENT, WindowManager.LayoutParams.MATCH_PARENT);
                dialog.show();

                ListView listview = dialog.findViewById(R.id.list_media);
                mAdapter = new ArrayAdapter<>(context, android.R.layout.simple_list_item_1, list);
                listview.setAdapter(mAdapter);

                listview.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                    @Override
                    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                        dialogCallback.onSelect(position);
                        dialog.dismiss();
                    }
                });

                dialog.findViewById(R.id.fragment_router).setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        dialog.dismiss();
                    }
                });

            }
        }
    }

    public void hide(){
        if (dialog != null && dialog.isShowing()){
            dialog.dismiss();
            dialog = null;
        }
    }
}
