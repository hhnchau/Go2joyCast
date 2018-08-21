package com.appromobile.Go2joyCast.fragment;

import android.app.Activity;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentManager;
import android.support.v7.app.MediaRouteControllerDialogFragment;
import android.support.v7.app.MediaRouteDialogFactory;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.appromobile.Go2joyCast.R;
import com.appromobile.Go2joyCast.api.ApiListCallBack;
import com.appromobile.Go2joyCast.api.HandleApi;
import com.appromobile.Go2joyCast.model.VideoSiteForm;
import com.appromobile.Go2joyCast.sql.SqlHandler;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Created by appro on 08/11/2017.
 */

public class FragmentRecent extends Fragment {
    private View rootView;
    private OnRecentCallBack onRecentCallBack;

    private RecyclerView recyclerViewRecent;
    private RecyclerView recyclerViewRecommended;
    private AdapterRecent adapterRecent;
    private AdapterRecent adapterRecommended;
    private List<VideoSiteForm> listRecent;
    private List<VideoSiteForm> listRecommended;
    private TextView txtRecent, txtRecommend;


    public void setOnRecentCallBack(OnRecentCallBack onRecentCallBack) {
        this.onRecentCallBack = onRecentCallBack;
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        rootView = inflater.inflate(R.layout.frg_recent, container, false);

        initRecyclerViewRecent();
        initRecyclerViewRecommended();
        findLimitVideoSiteList();

        return rootView;
    }

    private void initRecyclerViewRecommended() {
        recyclerViewRecommended = rootView.findViewById(R.id.rcv_Recommended);
        recyclerViewRecommended.setHasFixedSize(true);
        //recyclerViewRecommended.setLayoutManager(new LinearLayoutManager(getActivity()));  // Type: ListView
        recyclerViewRecommended.setLayoutManager(new GridLayoutManager(getActivity(), 4));  // Type: GridView
        recyclerViewRecommended.setItemAnimator(new DefaultItemAnimator());
        listRecommended = new ArrayList<>();
        adapterRecommended = new AdapterRecent(getActivity(), listRecommended);
        adapterRecommended.setOnItemClick(new AdapterRecent.OnItemClick() {
            @Override
            public void onClick(int p) {
                onRecentCallBack.click(listRecommended.get(p).getSiteUrl());
            }
        });

        recyclerViewRecommended.setAdapter(adapterRecommended);
    }

    private void initRecyclerViewRecent() {
        recyclerViewRecent = rootView.findViewById(R.id.rcv_recent);
        recyclerViewRecent.setHasFixedSize(true);
        //recyclerViewRecent.setLayoutManager(new LinearLayoutManager(getActivity()));  // Type: ListView
        recyclerViewRecent.setLayoutManager(new GridLayoutManager(getActivity(), 4));  // Type: GridView
        recyclerViewRecent.setItemAnimator(new DefaultItemAnimator());
        listRecent = new ArrayList<>();
        adapterRecent = new AdapterRecent(getActivity(), listRecent);
        adapterRecent.setOnItemClick(new AdapterRecent.OnItemClick() {
            @Override
            public void onClick(int p) {
                onRecentCallBack.click(listRecent.get(p).getSiteUrl());
            }
        });
        recyclerViewRecent.setAdapter(adapterRecent);

    }

    private void findLimitVideoSiteList() {
        HandleApi.getInstance().findLimitVideoSiteList(new ApiListCallBack() {
            @Override
            @SuppressWarnings("unchecked")
            public void resultApiList(List<Object> list) {
                if (list == null) {
                    findLimitVideoSQL(listRecommended);
                } else {
                    listRecommended.addAll((List<VideoSiteForm>) (Object) list);
                    adapterRecommended.notifyDataSetChanged();

                    findLimitVideoSQL(listRecommended);
                }

            }
        });
    }

    private void findLimitVideoSQL(List<VideoSiteForm> listRecommended) {
        List<String> list = SqlHandler.getInstance(getActivity()).selectAll();

        for (int i = 0; i < list.size(); i++) {
            String s = convertSiteName(list.get(i));
            int count = 0;
            for (int j = 0; j < listRecommended.size(); j++) {
                if (listRecommended.get(j).getSiteUrl().contains(s)) {
                    listRecent.add(listRecommended.get(j));
                    count++;
                    break;
                }
            }
            if (count == 0) {
                VideoSiteForm videoSiteForm = new VideoSiteForm();
                videoSiteForm.setSiteUrl(list.get(i));
                listRecent.add(videoSiteForm);
            }
        }

        if (listRecent.size() > 0) {
            adapterRecent.notifyDataSetChanged();
        } else {
            rootView.findViewById(R.id.recent).setVisibility(View.GONE);
        }
    }

    private String convertSiteName(String url) {
        String s = "";
        Matcher siteNameMatcher = Pattern.compile("//(.*?)/").matcher(url);
        while (siteNameMatcher.find()) {
            s = siteNameMatcher.group(1);
        }
        return s;
    }

    public interface OnRecentCallBack {
        void click(String url);
    }
}
