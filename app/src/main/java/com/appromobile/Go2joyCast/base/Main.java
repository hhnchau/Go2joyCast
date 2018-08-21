package com.appromobile.Go2joyCast.base;

import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.media.MediaRouteSelector;
import android.support.v7.media.MediaRouter;
import android.support.v7.widget.Toolbar;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.Toast;

import com.appromobile.Go2joyCast.R;
import com.appromobile.Go2joyCast.api.ApiListCallBack;
import com.appromobile.Go2joyCast.api.HandleApi;
import com.appromobile.Go2joyCast.cast.ExpandedControlsActivity;
import com.appromobile.Go2joyCast.dialog.DialogCallback;
import com.appromobile.Go2joyCast.dialog.RouterDialog;
import com.appromobile.Go2joyCast.fragment.FragmentBrowser;
import com.appromobile.Go2joyCast.fragment.FragmentRecent;
import com.appromobile.Go2joyCast.sql.SqlHandler;

import com.appromobile.Go2joyCast.sql.SqlPlayer;
import com.appromobile.Go2joyCast.utils.Utils;
import com.google.android.gms.cast.CastMediaControlIntent;
import com.google.android.gms.cast.MediaInfo;
import com.google.android.gms.cast.MediaMetadata;
import com.google.android.gms.cast.MediaTrack;
import com.google.android.gms.cast.framework.CastButtonFactory;
import com.google.android.gms.cast.framework.CastContext;
import com.google.android.gms.cast.framework.CastState;
import com.google.android.gms.cast.framework.CastStateListener;
import com.google.android.gms.cast.framework.IntroductoryOverlay;
import com.google.android.gms.cast.framework.Session;
import com.google.android.gms.cast.framework.SessionManagerListener;
import com.google.android.gms.cast.framework.media.RemoteMediaClient;
import com.google.android.gms.common.images.WebImage;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by appro on 05/09/2017.
 */
public class Main extends AppCompatActivity implements View.OnClickListener, CastStateListener, SessionManagerListener {
    private static boolean isRecent = false;
    private static int exit = 0;

    private FragmentBrowser fragmentBrowser;
    private FragmentRecent fragmentRecent;

    private EditText input;
    private ProgressBar progressBar;
    private ImageView home, back, next, search;
    private boolean isConnected = false;
    private static String urlCast = "";

    private IntroductoryOverlay mIntroductoryOverlay;
    private MenuItem mMediaRouterButton;

    private MediaRouter mMediaRouter;
    private MediaRouteSelector mMediaRouteSelector;
    private MediaRouter.Callback mMediaRouterCallback;
    private final ArrayList<MediaRouter.RouteInfo> mRouteInfos = new ArrayList<>();
    private final ArrayList<String> mRouteNames = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main);

        Window window = getWindow();
        if (window != null) {
            window.clearFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS);
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS);
                window.setStatusBarColor(ContextCompat.getColor(this, R.color.org));
            }
        }

        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        //MediaRouteButton mediaRouteButton = findViewById(R.id.cast);
        //CastButtonFactory.setUpMediaRouteButton(this, mediaRouteButton);

    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        super.onCreateOptionsMenu(menu);
        getMenuInflater().inflate(R.menu.menu_main, menu);
        mMediaRouterButton = CastButtonFactory.setUpMediaRouteButton(getApplicationContext(),
                menu,
                R.id.media_route_menu_item);


        return true;
    }


    private void initFragment() {
        fragmentRecent = new FragmentRecent();
        listenerFragmentRecent();
        fragmentBrowser = new FragmentBrowser();
        listenerFragmentBrowser();

        getSupportFragmentManager().beginTransaction()
                .add(R.id.main_frame, fragmentRecent, FragmentRecent.class.getName())
                .add(R.id.main_frame, fragmentBrowser, FragmentBrowser.class.getName())
                .commit();
    }

    //Recent
    private void showFragmentRecent() {
        if (fragmentRecent == null) {
            fragmentRecent = new FragmentRecent();
        }

        if (!fragmentRecent.isVisible()) {
            getSupportFragmentManager().beginTransaction()
                    .replace(R.id.main_frame, fragmentRecent, fragmentRecent.getClass().getName())
                    //.addToBackStack(null)
                    .commit();
        }
        listenerFragmentRecent();

    }

    //Browser
    private void showFragmentBrowser(String url) {
        fragmentBrowser = (FragmentBrowser) getSupportFragmentManager().findFragmentByTag(FragmentBrowser.class.getName());
        if (fragmentBrowser == null) {
            fragmentBrowser = FragmentBrowser.getInstance(url);
        }

        if (!fragmentBrowser.isVisible()) {
            getSupportFragmentManager().beginTransaction()
                    .replace(R.id.main_frame, fragmentBrowser, fragmentBrowser.getClass().getName())
                    //.addToBackStack(null)
                    .commit();
        }
        listenerFragmentBrowser();

    }

    //Callback Recent
    private void listenerFragmentRecent() {
        fragmentRecent.setOnRecentCallBack(new FragmentRecent.OnRecentCallBack() {
            @Override
            public void click(String url) {
                setLink(url);
            }
        });
    }

    //Callback Browser
    private void listenerFragmentBrowser() {
        fragmentBrowser.setOnBrowserCallBack(new FragmentBrowser.OnBrowserCallBack() {
            @Override
            public void click() {

            }

            @Override
            public void pageStart(String url) {
                //Store DB
                if (isRecent) {
                    isRecent = false;
                    SqlHandler.getInstance(Main.this).insert(url);
                }

                input.setText(url);
                search.setVisibility(View.GONE);
                Utils.hideKeyboard(Main.this);
            }

            @Override
            public void progressChange(final int progress) {
                Handler handler = new Handler();
                handler.post(new Runnable() {
                    @Override
                    public void run() {
                        progressBar.setProgress(progress);
                        if (progress == 100) {
                            progressBar.setVisibility(View.GONE);
                        } else {
                            progressBar.setVisibility(View.VISIBLE);
                        }
                    }
                });
            }

            @Override
            public void getUrl(String title, String thumbnailUrl, String url, List<String> listSub) {
                if (!urlCast.equals(url)) {
                    urlCast = url;
                    //AlertDisconnect();
                    gotoCast(title, thumbnailUrl, url, listSub);
                }
            }
        });
    }

    //Show Dialog Router
    private void showDialogRouter(List<String> listRouter) {
        RouterDialog.getInstance().hide();
        RouterDialog.getInstance().show(this, listRouter, new DialogCallback() {
            @Override
            public void onSelect(int position) {
                MediaRouter.RouteInfo info = mRouteInfos.get(position);
                mMediaRouter.selectRoute(info);
            }
        });
    }

    //Init
    private void init() {
        mMediaRouteSelector = new MediaRouteSelector.Builder()
                .addControlCategory(CastMediaControlIntent.categoryForCast(getResources().getString(R.string.cast_app_id)))
                .build();
        mMediaRouter = MediaRouter.getInstance(this);
        mMediaRouterCallback = new MyMediaRouterCallback();

        input = findViewById(R.id.input);
        input.setOnKeyListener(new View.OnKeyListener() {
            public boolean onKey(View v, int keyCode, KeyEvent event) {
                if ((event.getAction() == KeyEvent.ACTION_DOWN) && (keyCode == KeyEvent.KEYCODE_ENTER)) {
                    setLink(input.getText().toString());
                    return true;
                }
                return false;
            }
        });
        progressBar = findViewById(R.id.progress);
        home = findViewById(R.id.home);
        home.setOnClickListener(this);
        back = findViewById(R.id.back);
        back.setOnClickListener(this);
        next = findViewById(R.id.next);
        next.setOnClickListener(this);
        search = findViewById(R.id.icon_search);
        search.setOnClickListener(this);

        Utils.hideKeyboard(Main.this);
    }

    private void setLink(String input) {
        if (input != null) {
            urlCast = "";
            isRecent = true;

            loadUrl(Utils.isUrl(input));

        }
    }

    //Start Browser
    private void loadUrl(String url) {
        clearTitle();
        showFragmentBrowser(url);
        fragmentBrowser = (FragmentBrowser) getSupportFragmentManager().findFragmentByTag(FragmentBrowser.class.getName());
        if (fragmentBrowser != null) {
            fragmentBrowser.loadUrl(url);
        }
    }

    private void pauseVideo() {
        fragmentBrowser = (FragmentBrowser) getSupportFragmentManager().findFragmentByTag(FragmentBrowser.class.getName());
        if (fragmentBrowser != null) {
            fragmentBrowser.pause();
        }
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.home:
                urlCast = "";
                input.setText("");
                search.setVisibility(View.VISIBLE);
                showFragmentRecent();
                progressBar.setVisibility(View.GONE);
//                fragmentBrowser = (FragmentBrowser) getSupportFragmentManager().findFragmentByTag(FragmentBrowser.class.getName());
//                if (fragmentBrowser != null) {
//                    fragmentBrowser.pause();
//                    fragmentBrowser.clearWebView("");
//                }
                break;
            case R.id.back:
                urlCast = "";
                progressBar.setVisibility(View.GONE);
                backPress();
                break;
            case R.id.next:
                urlCast = "";
                progressBar.setVisibility(View.GONE);
                if (fragmentBrowser != null && fragmentBrowser.isVisible()) {
                    if (fragmentBrowser.canGoForward()) {
                        fragmentBrowser.goForward();
                    }
                }
                break;
//            case R.id.icon_search:
//                urlCast = "";
//                progressBar.setVisibility(View.GONE);
//                setLink(input.getText().toString());
//                break;
        }
    }

    @Override
    public void onBackPressed() {
        backPress();
    }

    private void backPress() {

        fragmentBrowser = (FragmentBrowser) getSupportFragmentManager().findFragmentByTag(FragmentBrowser.class.getName());
        if (fragmentBrowser != null && fragmentBrowser.isVisible()) {
            if (fragmentBrowser.canGoBack()) {
                fragmentBrowser.goBack();
            } else {
                fragmentRecent = (FragmentRecent) getSupportFragmentManager().findFragmentByTag(FragmentRecent.class.getName());
                if (fragmentRecent != null && fragmentRecent.isVisible()) {
                    onBackPressed();
                } else {
                    input.setText("");
                    search.setVisibility(View.VISIBLE);
                    showFragmentRecent();
                }
            }
        } else {
            exit++;
            if (exit == 2) {
                finish();
            } else {
                Toast.makeText(Main.this, getString(R.string.exit), Toast.LENGTH_SHORT).show();
            }
            Handler handler = new Handler();
            handler.postDelayed(new Runnable() {
                @Override
                public void run() {
                    exit = 0;
                }
            }, 3000);
        }

    }


    private void gotoCast(String title, String thumbnailUrl, String url, List<String> listSub) {
        if (isConnected) {

            playChromeCast(title, thumbnailUrl, url, listSub);

        } else {

            AlertNoDeviceCast();

        }
    }

    @Override
    protected void onPause() {
        super.onPause();

        mMediaRouter.removeCallback(mMediaRouterCallback);
        mRouteNames.clear();
        mRouteInfos.clear();

        //pauseVideo();

    }


    @Override
    protected void onResume() {
        super.onResume();
//        if (fragmentBrowser != null) {
//            fragmentBrowser.resume();
//        }
        init();

        CastContext.getSharedInstance(this).addCastStateListener(this);
        CastContext.getSharedInstance(this).getSessionManager().addSessionManagerListener(this);
        mMediaRouter.addCallback(mMediaRouteSelector, mMediaRouterCallback, MediaRouter.CALLBACK_FLAG_REQUEST_DISCOVERY);

        //initFragment();
        showFragmentRecent();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        CastContext.getSharedInstance(this).removeCastStateListener(this);
        CastContext.getSharedInstance(this).getSessionManager().removeSessionManagerListener(this);
//        fragmentBrowser = (FragmentBrowser) getSupportFragmentManager().findFragmentByTag(FragmentBrowser.class.getName());
//        if (fragmentBrowser != null) {
//            fragmentBrowser.clearWebView(currentUrl);
//        }
    }

    //Show button Chrome Cast
    private void showIntroductoryOverlay() {
        if (mIntroductoryOverlay != null) {
            mIntroductoryOverlay.remove();
        }
        if ((mMediaRouterButton != null) && mMediaRouterButton.isVisible()) {
            new Handler().post(new Runnable() {
                @Override
                public void run() {
                    mIntroductoryOverlay = new IntroductoryOverlay.Builder(
                            Main.this, mMediaRouterButton)
                            .setTitleText(getString(R.string.app_name))
                            .setOverlayColor(R.color.colorPrimary)
                            //.setSingleTime()
                            .setOnOverlayDismissedListener(
                                    new IntroductoryOverlay.OnOverlayDismissedListener() {
                                        @Override
                                        public void onOverlayDismissed() {
                                            mIntroductoryOverlay.remove();
                                            mIntroductoryOverlay = null;
                                        }
                                    })
                            .build();
                    mIntroductoryOverlay.show();
                }
            });
        }
    }

    private void playChromeCast(String title, String thumbnailUrl, String url, List<String> listSub) {

        if (CastContext.getSharedInstance(this).getSessionManager().getCurrentCastSession() != null
                && CastContext.getSharedInstance(this).getSessionManager().getCurrentCastSession().getRemoteMediaClient() != null) {

            //Crate Metadata
            MediaMetadata metadata = new MediaMetadata(MediaMetadata.MEDIA_TYPE_MOVIE);
            metadata.putString(MediaMetadata.KEY_TITLE, getString(R.string.title_cast));
            metadata.putString(MediaMetadata.KEY_SUBTITLE, title);
            metadata.addImage(new WebImage(Uri.parse(thumbnailUrl)));

            //Start Cast
            final RemoteMediaClient remoteMediaClient = CastContext.getSharedInstance(this).getSessionManager().getCurrentCastSession().getRemoteMediaClient();
            if (remoteMediaClient == null) {
                return;
            }

            remoteMediaClient.addListener(new RemoteMediaClient.Listener() {
                @Override
                public void onStatusUpdated() {
                    Intent intent = new Intent(Main.this, ExpandedControlsActivity.class);
                    startActivity(intent);
                    remoteMediaClient.removeListener(this);
                }

                @Override
                public void onMetadataUpdated() {
                }

                @Override
                public void onQueueStatusUpdated() {
                }

                @Override
                public void onPreloadStatusUpdated() {
                }

                @Override
                public void onSendingRemoteMediaRequest() {
                }

                @Override
                public void onAdBreakStatusUpdated() {
                }
            });
            remoteMediaClient.load(buildVideo(url, metadata, listSub), true, 0);

        }
    }

    private void clearTitle() {
        //Delete Sub
        fragmentBrowser = (FragmentBrowser) getSupportFragmentManager().findFragmentByTag(FragmentBrowser.class.getName());
        if (fragmentBrowser != null) {
            fragmentBrowser.clearSub();
            fragmentBrowser.clearTempUrl();
            fragmentBrowser.clearTitle();
        }
    }

    private static MediaInfo buildVideo(String url, MediaMetadata metadata, List<String> listSub) {
        MediaInfo mediaInfo;

        if (listSub != null && listSub.size() > 0) {
            List<MediaTrack> mediaTrackList = new ArrayList<>();
            for (int i = 0; i < listSub.size(); i++) {
                String name = "VN Subtitle";
                String language = "vni";
                if (listSub.get(i).contains("EN")) {
                    name = "EN Subtitle";
                    language = "en-US";
                }
                mediaTrackList.add(buildTrack(i, "text", "caption", listSub.get(i).replace(".srt", ".vtt"), name, language));
            }

            mediaInfo = new MediaInfo.Builder(url)
                    .setStreamType(MediaInfo.STREAM_TYPE_BUFFERED)
                    .setContentType("video/*")
                    .setMetadata(metadata)
                    .setMediaTracks(mediaTrackList)
                    .build();
        } else {
            mediaInfo = new MediaInfo.Builder(url)
                    .setStreamType(MediaInfo.STREAM_TYPE_BUFFERED)
                    .setContentType("video/*")
                    .setMetadata(metadata)
                    .build();
        }

        return mediaInfo;
    }

    //Add MediaTrack
    private static MediaTrack buildTrack(long id, String type, String subType, String contentId, String name, String language) {
        int trackType = MediaTrack.TYPE_UNKNOWN;
        if ("text".equals(type)) {
            trackType = MediaTrack.TYPE_TEXT;
        } else if ("video".equals(type)) {
            trackType = MediaTrack.TYPE_VIDEO;
        } else if ("audio".equals(type)) {
            trackType = MediaTrack.TYPE_AUDIO;
        }

        int trackSubType = MediaTrack.SUBTYPE_NONE;
        if (subType != null) {
            if ("captions".equals(type)) {
                trackSubType = MediaTrack.SUBTYPE_CAPTIONS;
            } else if ("subtitle".equals(type)) {
                trackSubType = MediaTrack.SUBTYPE_SUBTITLES;
            }
        }
        return new MediaTrack.Builder(id, trackType)
                .setName(name)
                .setSubtype(trackSubType)
                .setContentId(contentId)
                .setLanguage(language).build();
    }

    //AlertDisconnect
    public void AlertDisconnect() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setMessage(getString(R.string.txt_disconnect_message))
                .setCancelable(false)
                .setPositiveButton(getString(R.string.ok), new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        //CallBack
                        dialog.dismiss();
                    }
                });
        AlertDialog alert = builder.create();
        alert.setTitle(getString(R.string.txt_disconnect_title));
        alert.show();
    }

    //AlertDisconnect
    public void AlertNoDeviceCast() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setMessage(getString(R.string.txt_not_connected_message))
                .setCancelable(false)
                .setPositiveButton(getString(R.string.ok), new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        //CallBack
                        dialog.dismiss();
                    }
                });
        AlertDialog alert = builder.create();
        alert.setTitle(getString(R.string.txt_not_connected_title));
        alert.show();
    }

    @SuppressWarnings("unchecked")
    private void addPlayerFromServer() {
        HandleApi.getInstance().findLimitCastPlayerList(new ApiListCallBack() {
            @Override
            public void resultApiList(List<Object> list) {
                List<String> l = new ArrayList<>();
                l.addAll((List<String>) (Object) list);
                SqlPlayer.getInstance(Main.this).insert(l);
            }
        });
    }

    @Override
    public void onCastStateChanged(int i) {

        switch (i) {
            case CastState.CONNECTED:
                Toast.makeText(this, "Connected", Toast.LENGTH_SHORT).show();
                isConnected = true;

                if (mIntroductoryOverlay != null) {
                    mIntroductoryOverlay.remove();
                }

                break;
            case CastState.CONNECTING:
                Toast.makeText(this, "Connecting", Toast.LENGTH_SHORT).show();
                isConnected = false;
                break;

            case CastState.NOT_CONNECTED:
                Toast.makeText(this, "Not Connected", Toast.LENGTH_SHORT).show();
                isConnected = false;
                //showIntroductoryOverlay();
                break;

            case CastState.NO_DEVICES_AVAILABLE:
                Toast.makeText(this, "Nodevice", Toast.LENGTH_SHORT).show();
                isConnected = false;
                break;
        }
    }

    @Override
    public void onSessionStarting(Session session) {
        //Toast.makeText(this, "onSessionStarting", Toast.LENGTH_SHORT).show();
    }

    @Override
    public void onSessionStarted(Session session, String s) {
        //Toast.makeText(this, "onSessionStarted", Toast.LENGTH_SHORT).show();
    }

    @Override
    public void onSessionStartFailed(Session session, int i) {
        //Toast.makeText(this, "onSessionStartFailed", Toast.LENGTH_SHORT).show();
    }

    @Override
    public void onSessionEnding(Session session) {
        //Toast.makeText(this, "onSessionEnding", Toast.LENGTH_SHORT).show();
        //AlertDisconnect();
    }

    @Override
    public void onSessionEnded(Session session, int i) {
        // Toast.makeText(this, "onSessionEnded", Toast.LENGTH_SHORT).show();
        if (i > 0) {
            AlertDisconnect();
        }
        isConnected = false;
    }

    @Override
    public void onSessionResuming(Session session, String s) {
        //Toast.makeText(this, "onSessionResuming", Toast.LENGTH_SHORT).show();
    }

    @Override
    public void onSessionResumed(Session session, boolean b) {
        //Toast.makeText(this, "onSessionResumed", Toast.LENGTH_SHORT).show();
    }

    @Override
    public void onSessionResumeFailed(Session session, int i) {
        //Toast.makeText(this, "onSessionResumeFailed", Toast.LENGTH_SHORT).show();
    }

    @Override
    public void onSessionSuspended(Session session, int i) {
        //Toast.makeText(this, "onSessionSuspended", Toast.LENGTH_SHORT).show();
    }

    private class MyMediaRouterCallback extends MediaRouter.Callback {

        @Override
        public void onRouteAdded(MediaRouter router, MediaRouter.RouteInfo info) {

            boolean exist = false;
            for (int i = 0; i < mRouteNames.size(); i++) {
                if (mRouteNames.get(i).equals(info.getName())) {
                    exist = true;
                }
            }

            if (!exist) {
                mRouteInfos.add(info);
                mRouteNames.add(info.getName());
            }

            if (mRouteNames.size() > 0) {
                showDialogRouter(mRouteNames);
            }

        }

        @Override
        public void onRouteRemoved(MediaRouter router, MediaRouter.RouteInfo info) {

            for (int i = 0; i < mRouteInfos.size(); i++) {
                MediaRouter.RouteInfo routeInfo = mRouteInfos.get(i);
                if (routeInfo.equals(info)) {
                    mRouteInfos.remove(i);
                    mRouteNames.remove(i);

                    if (mRouteNames.size() > 0) {
                        showDialogRouter(mRouteNames);
                    }

                    return;
                }
            }

        }

    }
}
