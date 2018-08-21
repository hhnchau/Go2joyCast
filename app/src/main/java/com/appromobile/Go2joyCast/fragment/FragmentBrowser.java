package com.appromobile.Go2joyCast.fragment;

import android.annotation.SuppressLint;
import android.content.Context;
import android.graphics.Bitmap;
import android.net.Uri;
import android.net.http.SslError;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.util.SparseArray;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.webkit.JavascriptInterface;
import android.webkit.SslErrorHandler;
import android.webkit.ValueCallback;
import android.webkit.WebChromeClient;
import android.webkit.WebView;
import android.webkit.WebViewClient;

import com.appromobile.Go2joyCast.R;
import com.appromobile.Go2joyCast.sql.SqlPlayer;
import com.appromobile.Go2joyCast.utils.Utils;

import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.util.ArrayList;
import java.util.List;

import at.huber.youtubeExtractor.VideoMeta;
import at.huber.youtubeExtractor.YouTubeExtractor;
import at.huber.youtubeExtractor.YtFile;

/**
 * Created by appro on 08/11/2017.
 */

public class FragmentBrowser extends Fragment {

    private static FragmentBrowser Instance = null;
    private static List<String> listPlayer;
    private String cache = "";

    public static FragmentBrowser getInstance(String url) {
        if (Instance == null) {
            Instance = new FragmentBrowser();
        }
        Bundle bundle = new Bundle();
        bundle.putString("url", url);
        Instance.setArguments(bundle);
        return Instance;
    }

    private View rootView;
    private OnBrowserCallBack onBrowserCallBack;

    private WebView webView;
    private List<String> listSub;
    private String tempUrl = "";
    private String thumbnailUrl = "";
    private String title = "";

    public void setOnBrowserCallBack(OnBrowserCallBack onBrowserCallBack) {
        this.onBrowserCallBack = onBrowserCallBack;
    }


    private static final int QUALITY_DEFAULT_FLV = 5;
    private static final int QUALITY_360_FLV = 34;
    private static final int QUALITY_480_FLV = 35;

    private static final int QUALITY_360_MP4 = 18;
    private static final int QUALITY_720_MP4 = 22;
    private static final int QUALITY_1080_MP4 = 37;

    private static final int QUALITY_360_WEBM = 43;
    private static final int QUALITY_480_WEBM = 44;
    private static final int QUALITY_720_WEBM = 45;
    private static final int QUALITY_1080_WEBM = 46;

    private static final int QUALITY_360_STREAM_MP4 = 134;
    private static final int QUALITY_480_STREAM_MP4 = 135;
    private static final int QUALITY_720_STREAM_MP4 = 136;
    private static final int QUALITY_1080_STREAM_MP4 = 137;

    private static final int QUALITY_AUDIO_48_MP4 = 139;
    private static final int QUALITY_AUDIO_128_MP4 = 140;
    private static final int QUALITY_AUDIO_256_MP4 = 141;

    private static final int QUALITY_360_STREAM_WEBM = 167;
    private static final int QUALITY_480_STREAM_WEBM = 168;
    private static final int QUALITY_720_STREAM_WEBM = 169;
    private static final int QUALITY_1080_STREAM_WEBM = 170;

    private static final int QUALITY_AUDIO_128_WEBM = 171;
    private static final int QUALITY_AUDIO_256_WEBM = 172;


    private WebView webViewExtractor;
    private String idExtractor = "";

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        rootView = inflater.inflate(R.layout.frg_browser, container, false);
        initWebView();
        //initWebViewExtractor();
        loadPlayer();
        if (getArguments() != null) {
            String link = getArguments().getString("url", "http://google.com");

            loadUrl(link);
        }
        return rootView;
    }

    public void loadUrl(String url) {
        cache = Utils.convertSiteName(url);
        webView.loadUrl(url);
    }

    private void loadPlayer() {
        listPlayer = new ArrayList<>();
        listPlayer = SqlPlayer.getInstance(getActivity()).selectAll();
    }

    @SuppressLint({"SetJavaScriptEnabled", "AddJavascriptInterface"})
    public void initWebView() {
        listSub = new ArrayList<>();
        webView = rootView.findViewById(R.id.web_view);
        webView.getSettings().setJavaScriptEnabled(true);
        webView.getSettings().setUserAgentString("Mozilla/5.0 (Linux; Android 4.4.4; One Build/KTU84L.H4) AppleWebKit/537.36 (KHTML, like Gecko) Version/4.0 Chrome/33.0.0.0 Mobile Safari/537.36 [Awesome Kitteh/v2.0]");
        //webView.addJavascriptInterface(new MyJavaScriptInterface(), "HtmlViewer");
        webView.addJavascriptInterface(new VideoDetectScriptInterface(), "VideoTag");
        webView.getSettings().setDomStorageEnabled(true);
        webView.getSettings().setJavaScriptCanOpenWindowsAutomatically(true);
        webView.getSettings().setLoadWithOverviewMode(true);
        webView.getSettings().setUseWideViewPort(true);
        webView.getSettings().setDefaultTextEncodingName("utf-8");
        webView.getSettings().setBuiltInZoomControls(true);

        webView.setWebChromeClient(new WebChromeClient() {
            @Override
            public void onProgressChanged(WebView view, int newProgress) {
                super.onProgressChanged(view, newProgress);
                onBrowserCallBack.progressChange(newProgress);
            }
        });

        webView.setWebViewClient(new WebViewClient() {

            @Override
            public void onPageStarted(WebView view, String url, Bitmap favicon) {
                super.onPageStarted(view, url, favicon);
                onBrowserCallBack.pageStart(url);
            }


            @Override
            public boolean shouldOverrideUrlLoading(WebView view, String url) {
                String host = Uri.parse(url).getHost();
                //Deny ads
                if (host != null && host.contains(cache) || cache.equals("google.com") || cache.equals("www.google.com") || cache.equals("m.google.com") || host != null && host.contains("hdviet.com")) {
                    return false;
                }
                return true;
            }

            @Override
            public void onLoadResource(WebView view, final String url) {
                super.onLoadResource(view, url);

                regexUrl(url);

            }

            @Override
            public void onPageFinished(WebView view, final String url) {
                super.onPageFinished(view, url);

                //JSUrl();
                addJsThumbnail();
                addJsTitle();
                addJsVideoDetect();

            }

            @Override
            public void onReceivedSslError(WebView view, SslErrorHandler handler, SslError error) {
                handler.proceed();
            }
        });
    }

    @SuppressLint({"SetJavaScriptEnabled", "AddJavascriptInterface"})
    private void initWebViewExtractor() {
        webViewExtractor = rootView.findViewById(R.id.web_view_extract);
        webViewExtractor.getSettings().setLoadsImagesAutomatically(false);
        webViewExtractor.getSettings().setJavaScriptEnabled(true);//Mozilla/5.0 (Linux; Android 6.0; Nexus 7 Build/MRA58V) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/47.0.2526.83 Safari/537.36
        //webViewExtractor.getSettings().setUserAgentString("Mozilla/5.0 (Linux; Android 4.4.4; One Build/KTU84L.H4) AppleWebKit/537.36 (KHTML, like Gecko) Version/4.0 Chrome/33.0.0.0 Mobile Safari/537.36 [Awesome Kitteh/v2.0]");
        //webViewExtractor.getSettings().setUserAgentString("Mozilla/5.0 (Linux; Android 7.0; LG-H910 Build/NRD90C) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/51.0.2704.90 Mobile Safari/537.36");
        webViewExtractor.setWebViewClient(new WebViewClient() {
            @Override
            public void onPageFinished(WebView view, String url) {
                super.onPageFinished(view, url);
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
                    webViewExtractor.evaluateJavascript("(function() {return " + "document.getElementsByClassName('link-group')[0].outerHTML;" + "})();", new ValueCallback<String>() {
                        @Override
                        public void onReceiveValue(final String urls) {
                            Handler handler = new Handler();
                            handler.post(new Runnable() {
                                @Override
                                public void run() {
                                    Log.d("LINK-YOU-EXTRACT", urls);
                                    String u = Utils.regexYoutube(urls);
                                    if (!u.equals("")) {
                                        String newUrl = Utils.fromHtml(u).toString();
                                        Log.d("LINK-YOU-EXTRACT", newUrl);
                                        //new AlertDialog.Builder(getActivity()).setTitle("HTML").setMessage(newUrl).setPositiveButton(android.R.string.ok, null).setCancelable(false).create().show();
                                        onBrowserCallBack.getUrl(title, thumbnailUrl, newUrl, listSub);

                                    }
                                }
                            });
                        }
                    });
                }
            }
        });
    }

    //Detect Link
    private void regexUrl(final String url) {

        Handler handler = new Handler();
        handler.post(new Runnable() {
            @Override
            public void run() {
                Log.d("LINKS-ORG", url);

                //Check Youtube
                String id = Utils.getIdYoutube(url);
                if (!id.equals("") && !id.equals(idExtractor)) {
                    idExtractor = id;
                    //webViewExtractor.loadUrl("http://ssyoutube.com/watch?v=" + id);

                    Extractor youtubeExtractor = new Extractor(getActivity(), new Extractor.YtCallback() {
                        @Override
                        public void YtReturn(String title, String thumbnail, String url) {
                            onBrowserCallBack.getUrl(title, thumbnail, url, listSub);
                        }
                    });
                    youtubeExtractor.extract("https://www.youtube.com/watch?v=" + idExtractor, true, true);

                }

//                //Check temp link
//                if (url.contains(".m3u8")) {
//                    if (url.contains("hdviet.com/")) {
//                        tempUrl = url;
//                        onBrowserCallBack.getUrl(title, thumbnailUrl, url, listSub);
//                    }
//                }

                //Check Sub

                handleHdViet(url);

                if (url.endsWith(".vtt") || url.endsWith(".srt")) {
                    boolean exist = false;
                    //Check Exist Sub
                    if (listSub != null && listSub.size() > 0) {
                        for (int i = 0; i < listSub.size(); i++) {
                            if (listSub.get(i).equals(url)) {
                                exist = true;
                            }
                        }
                    }
                    if (!exist) {
                        if (listSub == null) {
                            listSub = new ArrayList<>();
                        }
                        listSub.add(url);
                        Log.d("LINKS-SUB", url);
                    }
                }
            }
        });
    }

    private void handleHdViet(final String url) {
        new Handler().post(new Runnable() {
            @Override
            public void run() {
                if (url.contains("hdviet.com/")) {
                    //Get Title
                    title = Utils.getTitleHdViet(url);

                    if (url.contains("hdviet.com/backdrops/") && url.endsWith(".jpg")) {
                        Log.d("LINKS-HDVIET-THUMBNAIL", url);
                        thumbnailUrl = url;
                    }
                    //Get link Film
                    if (url.endsWith("m3u8")) {
                        tempUrl = url;
                        Log.d("LINKS-HDVIET-FILM", url);
                        onBrowserCallBack.getUrl(title, thumbnailUrl, url, listSub);
                    }
                }
            }
        });
    }

    private void addJsVideoDetect() {
        String js = "javascript:";

        //js += "window.VideoTag.showHTML" + "('<html>'+document.getElementsByTagName('html')[0].innerHTML+'</html>');";

        js += "var video = document.getElementsByTagName('video')[0];";

        js += "video.pause();";

        if (Build.VERSION.SDK_INT <= Build.VERSION_CODES.KITKAT) {

            //js += "window.addEventListener('click', isPlay);";
            js += "setInterval(isPlay, 5000);";

            js += "function isPlay(){";
            {
                js += "if(!video.paused){";
                {
                    js += "window.VideoTag.callbackWebView(" + "video.getAttribute('src')" + ");";
                }
                js += "};";
            }
            js += "};";
        } else {
            js += "video.onplay = (function(){";
            {
                js += "return function(){";
                {
                    js += "isPlay(video);";
                }
                js += "};";
            }
            js += "})(video);";

            js += "function isPlay(video){";
            {
                //js += "if(!video.paused && !stop){";
                //{
                //CallBack Is Playing
                js += "window.VideoTag.callbackWebView(" + "video.getAttribute('src')" + ");";
                //js += "stop = true;";
                //}
                //js += "};";
            }
            js += "};";
        }
        webView.loadUrl(js);
    }

    class VideoDetectScriptInterface {
        @JavascriptInterface
        public void callbackWebView(final String s) {
            if (getActivity() != null) {
                getActivity().runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        if (s != null) {
                            Log.v("LINKS-JS-ORG:--->", s);
                            if (!s.equals("null") && s.startsWith("http")) {
                                if (!s.contains("https://m.youtube.com/")) {
                                    Log.v("LINKS-JS:--->", s);
                                    onBrowserCallBack.getUrl(title, thumbnailUrl, s, listSub);
                                }
                            } else {
                                if (!tempUrl.equals("")) {
                                    Log.v("LINKS-JS:--->", tempUrl);
                                    onBrowserCallBack.getUrl(title, thumbnailUrl, tempUrl, listSub);
                                }
                            }
                        }
                    }
                });
            }
        }

//        @JavascriptInterface
//        public void showHTML(final String html) {
//            new Handler().post(new Runnable() {
//                @Override
//                public void run() {
//                    if (html != null){
//                        Matcher siteNameMatcher = Pattern.compile("document.querySelector(\\\"meta[property='og:image']\\\").getAttribute(\\\"content\\\");").matcher(html);
//                        while (siteNameMatcher.find()) {
//                            thumbnailUrl = siteNameMatcher.group(1);
//                            Log.v("LINKS-THUMBNAIL", thumbnailUrl);
//                        }
//
//                        siteNameMatcher = Pattern.compile("document.querySelector(\\\"meta[property='og:title']\\\").getAttribute(\\\"content\\\");").matcher(html);
//                        while (siteNameMatcher.find()) {
//                            title = siteNameMatcher.group(1);
//                            Log.v("LINKS-TITLE", title);
//                        }
//                    }
//                }
//            });
//        }
    }

    private void addJsThumbnail() {
        Handler handler = new Handler();
        handler.post(new Runnable() {
            @Override
            public void run() {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
                    webView.evaluateJavascript("(function() {var element = document.querySelector('meta[property=\"og:image\"]');" + "return element && element.getAttribute('content');" + "})();", new ValueCallback<String>() {
                        @Override
                        public void onReceiveValue(final String url) {
                            if (url != null && !url.equals("null")) {
                                String newUrl = "";
                                if (url.length() > 2) {
                                    newUrl = url.substring(1);
                                    newUrl = newUrl.substring(0, newUrl.length() - 1);
                                    thumbnailUrl = newUrl;
                                    Log.v("LINKS-THUMBNAIL", thumbnailUrl);
                                }
                            }
                        }
                    });
                }
            }

        });
    }

    private void addJsTitle() {
        Handler handler = new Handler();
        handler.post(new Runnable() {
            @Override
            public void run() {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
                    webView.evaluateJavascript("(function() {var element = document.querySelector('meta[property=\"og:title\"]');" + "return element && element.getAttribute('content');" + "})();", new ValueCallback<String>() {
                        @Override
                        public void onReceiveValue(final String url) {
                            if (url != null && !url.equals("null")) {
                                String newUrl = "";
                                if (url.length() > 2) {
                                    newUrl = url.substring(1);
                                    newUrl = newUrl.substring(0, newUrl.length() - 1);

                                    try {
                                        title = URLDecoder.decode(newUrl, "UTF8");
                                        Log.v("LINKS-TITLE", title);
                                    } catch (UnsupportedEncodingException e) {
                                        e.printStackTrace();
                                    }
                                }
                            }
                        }
                    });
                }
            }
        });
    }

    private void JSUrl() {

        Handler handler = new Handler();
        handler.post(new Runnable() {
            @Override
            public void run() {

                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
                    for (int i = 0; i < listPlayer.size(); i++) {
                        final int finalI = i;
                        webView.evaluateJavascript("(function() {return " + listPlayer.get(i) + "})();", new ValueCallback<String>() {
                            @Override
                            public void onReceiveValue(final String url) {
                                Log.v("LINK-VIDEO: " + listPlayer.get(finalI) + " -->", url);
                                if (!url.equals("null")) {
                                    String u = Utils.cutUrl(url);
                                    if (!u.equals("")) {
                                        String newUrl = Utils.fromHtml(u).toString();
                                        Log.v("LINK-DETECT: " + listPlayer.get(finalI) + " -->", newUrl);
                                        if (newUrl.startsWith("http")) {
                                            if (!url.contains("https://m.youtube.com/")) {
                                                onBrowserCallBack.getUrl(title, thumbnailUrl, newUrl, listSub);
                                            }
                                        } else {
                                            if (!tempUrl.equals("")) {
                                                Log.v("LINK-TEMP: " + listPlayer.get(finalI) + " -->", tempUrl);
                                                onBrowserCallBack.getUrl(title, thumbnailUrl, tempUrl, listSub);
                                            }
                                        }
                                    }
                                }
                            }
                        });
                    }
                }
            }
        });
    }

    public boolean canGoBack() {
        return webView.canGoBack();
    }

    public void goBack() {
        webView.goBack();
    }

    public boolean canGoForward() {
        return webView.canGoForward();
    }

    public void goForward() {
        webView.goForward();
    }

    public void pause() {
        if (webView != null) {
            webView.onPause();
            webView.pauseTimers();
        }
    }

    public void resume() {
        if (webView != null) {
            webView.onResume();
            webView.resumeTimers();
        }
    }

    public void destroy() {
        if (webView != null) {
            webView.destroy();
            webView = null;
        }
    }

    public void clearWebView(String url) {
        if (webView != null) {
            webView.loadUrl(url);
        }
    }

    public void clearSub() {
        if (listSub != null) {
            listSub = null;
            Log.v("LINK-CLEAR-SUB: ", " ");
        }
    }

    public void clearTempUrl() {
        tempUrl = "";
        Log.v("LINK-CLEAR-TEMP-URL: ", " ");
    }

    public void clearTitle() {
        Log.v("LINK-CLEAR-TITLE: ", " ");
        title = "";
        thumbnailUrl = "";
    }

    @Override
    public void onPause() {
        super.onPause();
        clearTitle();
        pause();
    }

    @Override
    public void onResume() {
        super.onResume();
        resume();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        destroy();
    }

    public interface OnBrowserCallBack {
        void click();

        void pageStart(String url);

        void progressChange(int progress);

        void getUrl(String title, String thumbnailUrl, String url, List<String> listSub);
    }

    private static class Extractor extends YouTubeExtractor {
        private YtCallback ytCallback;

        private Extractor(Context con, YtCallback ytCallback) {
            super(con);
            this.ytCallback = ytCallback;
        }

        public interface YtCallback {
            void YtReturn(String title, String thumbnail, String url);
        }

        @Override
        protected void onExtractionComplete(SparseArray<YtFile> map, VideoMeta videoMeta) {
            if (map != null) {
                String url = "";
                String title = "";
                String thumbnail = "";
                if (videoMeta != null) {
                    title = videoMeta.getTitle();
                    Log.d("HUY-TITLE", title);
                    thumbnail = videoMeta.getHqImageUrl();
                    Log.d("HUY-THUMBNAIL", thumbnail);
                }

                YtFile item = map.get(QUALITY_1080_MP4);
                if (item != null) {
                    url = item.getUrl();
                    Log.d("HUY-YOU-URL", url);
                    ytCallback.YtReturn(title, thumbnail, url);
                    return;
                }
                item = map.get(QUALITY_1080_WEBM);
                if (item != null) {
                    url = item.getUrl();
                    Log.d("HUY-YOU-URL", url);
                    ytCallback.YtReturn(title, thumbnail, url);
                    return;
                }

                item = map.get(QUALITY_720_MP4);
                if (item != null) {
                    url = item.getUrl();
                    Log.d("HUY-YOU-URL", url);
                    ytCallback.YtReturn(title, thumbnail, url);
                    return;
                }

                item = map.get(QUALITY_720_WEBM);
                if (item != null) {
                    url = item.getUrl();
                    Log.d("HUY-YOU-URL", url);
                    ytCallback.YtReturn(title, thumbnail, url);
                    return;
                }

                item = map.get(QUALITY_480_WEBM);
                if (item != null) {
                    url = item.getUrl();
                    Log.d("HUY-YOU-URL", url);
                    ytCallback.YtReturn(title, thumbnail, url);
                    return;
                }

                item = map.get(QUALITY_480_FLV);
                if (item != null) {
                    url = item.getUrl();
                    Log.d("HUY-YOU-URL", url);
                    ytCallback.YtReturn(title, thumbnail, url);
                    return;
                }

                item = map.get(QUALITY_360_MP4);
                if (item != null) {
                    url = item.getUrl();
                    Log.d("HUY-YOU-URL", url);
                    ytCallback.YtReturn(title, thumbnail, url);
                    return;
                }

                item = map.get(QUALITY_360_WEBM);
                if (item != null) {
                    url = item.getUrl();
                    Log.d("HUY-YOU-URL", url);
                    ytCallback.YtReturn(title, thumbnail, url);
                    return;
                }

                item = map.get(QUALITY_360_FLV);
                if (item != null) {
                    url = item.getUrl();
                    Log.d("HUY-YOU-URL", url);
                    ytCallback.YtReturn(title, thumbnail, url);
                    return;
                }

                item = map.get(QUALITY_DEFAULT_FLV);
                if (item != null) {
                    url = item.getUrl();
                    Log.d("HUY-YOU-URL", url);
                    ytCallback.YtReturn(title, thumbnail, url);
                    return;
                }

                item = map.get(93);
                if (item != null) {
                    url = item.getUrl();
                    Log.d("HUY-YOU-URL", url);
                    ytCallback.YtReturn(title, thumbnail, url);
                    return;
                }

                item = map.get(94);
                if (item != null) {
                    url = item.getUrl();
                    Log.d("HUY-YOU-URL", url);
                    ytCallback.YtReturn(title, thumbnail, url);
                    return;
                }

                item = map.get(95);
                if (item != null) {
                    url = item.getUrl();
                    Log.d("HUY-YOU-URL", url);
                    ytCallback.YtReturn(title, thumbnail, url);
                    return;
                }

                item = map.get(96);
                if (item != null) {
                    url = item.getUrl();
                    Log.d("HUY-YOU-URL", url);
                    ytCallback.YtReturn(title, thumbnail, url);
                }

            }
        }
    }
}
