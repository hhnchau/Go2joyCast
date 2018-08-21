package com.appromobile.Go2joyCast.fragment;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.appromobile.Go2joyCast.R;
import com.appromobile.Go2joyCast.api.UrlParams;
import com.appromobile.Go2joyCast.model.VideoSiteForm;
import com.bumptech.glide.Glide;

import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;


/**
 * Created by appro on 08/11/2017.
 */

public class AdapterRecent extends RecyclerView.Adapter<AdapterRecent.ViewHolder> {
    private Context context;
    private List<VideoSiteForm> listVideoSiteForm;
    private OnItemClick onItemClick;

    public AdapterRecent(Context context, List<VideoSiteForm> listVideoSiteForm) {
        this.context = context;
        this.listVideoSiteForm = listVideoSiteForm;
    }

    public void setOnItemClick(OnItemClick onItemClick) {
        this.onItemClick = onItemClick;
    }

    @Override
    public AdapterRecent.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View rootView = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_recent, parent, false);
        return new ViewHolder(rootView);
    }

    @Override
    public void onBindViewHolder(AdapterRecent.ViewHolder holder, final int position) {
        VideoSiteForm videoSiteForm = listVideoSiteForm.get(position);
        if (videoSiteForm != null) {
            if (videoSiteForm.getIconUrl().equals("")) {
                if (videoSiteForm.getSiteName().equals("")) {
                    holder.txt.setText(convertSiteName(videoSiteForm.getSiteUrl()));
                } else {
                    holder.txt.setText(videoSiteForm.getSiteName());
                }
                holder.img.setImageResource(0);
            } else {
                holder.txt.setText("");

                String url = videoSiteForm.getIconUrl();
                Glide
                        .with(context)
                        .load(url)
                        .into(holder.img);
            }
        }


        holder.relativeLayout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onItemClick.onClick(position);
            }
        });

    }

    @Override
    public int getItemCount() {
        return listVideoSiteForm.size();
    }

    public class ViewHolder extends RecyclerView.ViewHolder {
        private ImageView img;
        private TextView txt;
        private RelativeLayout relativeLayout;


        public ViewHolder(View itemView) {
            super(itemView);

            img = itemView.findViewById(R.id.item_img);
            txt = itemView.findViewById(R.id.item_name);
            relativeLayout =  itemView.findViewById(R.id.item_recent);

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

    public interface OnItemClick {
        void onClick(int p);
    }
}
