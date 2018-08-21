package com.appromobile.Go2joyCast.fragment;

import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.os.Handler;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.MediaRouteDialogFactory;
import android.support.v7.media.MediaControlIntent;
import android.support.v7.media.MediaRouteSelector;
import android.support.v7.media.MediaRouter;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.Toast;

import com.appromobile.Go2joyCast.R;
import com.appromobile.Go2joyCast.base.Main;
import com.google.android.gms.cast.CastDevice;
import com.google.android.gms.cast.CastMediaControlIntent;

import java.util.ArrayList;

/**
 * Created by appro on 10/11/2017.
 */

public class FragmentRouterDevice extends Fragment {
    private View rootView;

    private MediaRouter mMediaRouter;
    private MediaRouteSelector mMediaRouteSelector;
    private MediaRouter.Callback mMediaRouterCallback;
    private final ArrayList<MediaRouter.RouteInfo> mRouteInfos = new ArrayList<MediaRouter.RouteInfo>();
    private CastDevice mSelectedDevice;

    private ArrayList<String> mRouteNames = new ArrayList<String>();
    private ArrayAdapter<String> mAdapter;


    private boolean isDevice = false;
    private ProgressDialog loading;

    private OnRouterDeviceCallBack onRouterDeviceCallBack;

    public void setOnRouterDeviceCallBack(OnRouterDeviceCallBack onRouterDeviceCallBack) {
        this.onRouterDeviceCallBack = onRouterDeviceCallBack;
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        rootView = inflater.inflate(R.layout.frg_router_media, container, false);

        initListView();

        loading = new ProgressDialog(getActivity());
        loading.setMessage("Scanning..");
        //loading.setTitle("Get Data");
        loading.setIndeterminate(false);
        loading.setCancelable(true);
        loading.show();

        Handler handler = new Handler();
        handler.postDelayed(new Runnable() {
            @Override
            public void run() {
                    Alert();

                if (loading != null && loading.isShowing()) {
                    loading.dismiss();
                }

                if (mRouteInfos.size() == 0){
                    //CallBack
                    onRouterDeviceCallBack.selected();
                }


            }
        }, 3000);


        return rootView;
    }

    private void initListView() {
        final ListView listview = rootView.findViewById(R.id.list_media);
        mRouteNames = new ArrayList<>();
        if (getActivity() != null) {
            mAdapter = new ArrayAdapter<>(getActivity(), android.R.layout.simple_list_item_1, mRouteNames);
            listview.setAdapter(mAdapter);

            listview.setOnItemClickListener(new AdapterView.OnItemClickListener() {

                @Override
                public void onItemClick(AdapterView<?> parent, final View view,
                                        int position, long id) {

                    MediaRouter.RouteInfo info = mRouteInfos.get(position);
                    mMediaRouter.selectRoute(info);
                }

            });


            mMediaRouteSelector = new MediaRouteSelector.Builder()
                    .addControlCategory(CastMediaControlIntent.categoryForCast(getResources().getString(R.string.cast_app_id)))
                    .build();
            mMediaRouter = MediaRouter.getInstance(getActivity());
            mMediaRouterCallback = new MyMediaRouterCallback();
        }
    }

    @Override
    public void onResume() {
        super.onResume();

        if (mAdapter != null) {
            mAdapter.clear();
        }
        mMediaRouter.addCallback(mMediaRouteSelector, mMediaRouterCallback, MediaRouter.CALLBACK_FLAG_REQUEST_DISCOVERY);

    }

    @Override
    public void onPause() {
        mMediaRouter.removeCallback(mMediaRouterCallback);
        super.onPause();
    }

    private class MyMediaRouterCallback extends MediaRouter.Callback {

        @Override
        public void onRouteAdded(MediaRouter router, MediaRouter.RouteInfo info) {


            isDevice = true;
            if (loading != null && loading.isShowing()) {
                loading.dismiss();
            }

            mRouteInfos.add(info);
            mRouteNames.add(info.getName() + " (" + info.getDescription() + ")");
            mAdapter.notifyDataSetChanged();

        }

        @Override
        public void onRouteRemoved(MediaRouter router, MediaRouter.RouteInfo info) {

            for (int i = 0; i < mRouteInfos.size(); i++) {
                MediaRouter.RouteInfo routeInfo = mRouteInfos.get(i);
                if (routeInfo.equals(info)) {
                    mRouteInfos.remove(i);
                    mRouteNames.remove(i);
                    mAdapter.notifyDataSetChanged();
                    return;
                }
            }

        }

        @Override
        public void onRouteSelected(MediaRouter router, MediaRouter.RouteInfo info) {

            mSelectedDevice = CastDevice.getFromBundle(info.getExtras());

            //CallBack
            onRouterDeviceCallBack.selected();

        }

        @Override
        public void onRouteUnselected(MediaRouter router, MediaRouter.RouteInfo info) {
            mSelectedDevice = null;
        }

    }


    public void Alert() {
        if (getActivity() != null) {
            AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
            builder.setMessage(getString(R.string.txt_connection_message))
                    .setCancelable(false)
                    .setPositiveButton(getString(R.string.ok), new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            //CallBack
                            onRouterDeviceCallBack.selected();
                            dialog.dismiss();
                        }
                    });

            AlertDialog alert = builder.create();
            alert.setTitle(getString(R.string.txt_connection_title));
            alert.show();
        }
    }


    public interface OnRouterDeviceCallBack {
        void selected();
    }

}
