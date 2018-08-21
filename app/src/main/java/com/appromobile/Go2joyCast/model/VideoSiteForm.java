package com.appromobile.Go2joyCast.model;

import android.text.TextUtils;

/**
 * Created by appro on 08/11/2017.
 */

public class VideoSiteForm {
    private String iconUrl;
    private String lastUpdate;
    private String siteName;
    private String siteUrl;
    private int sn;
    private int type;

    public VideoSiteForm() {

    }

    public void setIconUrl(String iconUrl) {
        this.iconUrl = iconUrl;
    }

    public void setLastUpdate(String lastUpdate) {
        this.lastUpdate = lastUpdate;
    }

    public void setSiteName(String siteName) {
        this.siteName = siteName;
    }

    public void setSiteUrl(String siteUrl) {
        this.siteUrl = siteUrl;
    }

    public void setSn(int sn) {
        this.sn = sn;
    }

    public void setType(int type) {
        this.type = type;
    }

    public String getIconUrl() {
        if (TextUtils.isEmpty(iconUrl)){
            return "";
        }
        return iconUrl;
    }

    public String getLastUpdate() {
        if (TextUtils.isEmpty(lastUpdate)){
            return "";
        }
        return lastUpdate;
    }

    public String getSiteName() {
        if (TextUtils.isEmpty(siteName)){
            return "";
        }
        return siteName;
    }

    public String getSiteUrl() {
        if (TextUtils.isEmpty(siteUrl)){
            return "";
        }
        return siteUrl;
    }

    public int getSn() {
        return sn;
    }

    public int getType() {
        return type;
    }
}
