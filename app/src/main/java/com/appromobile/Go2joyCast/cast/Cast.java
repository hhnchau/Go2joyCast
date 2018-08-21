package com.appromobile.Go2joyCast.cast;

import android.content.Intent;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.animation.AccelerateInterpolator;
import android.view.animation.DecelerateInterpolator;
import android.widget.MediaController;
import android.widget.VideoView;

import com.appromobile.Go2joyCast.R;
import com.google.android.gms.cast.MediaInfo;
import com.google.android.gms.cast.MediaMetadata;
import com.google.android.gms.cast.framework.AppVisibilityListener;
import com.google.android.gms.cast.framework.CastButtonFactory;
import com.google.android.gms.cast.framework.CastContext;
import com.google.android.gms.cast.framework.CastState;
import com.google.android.gms.cast.framework.CastStateListener;
import com.google.android.gms.cast.framework.IntroductoryOverlay;
import com.google.android.gms.cast.framework.Session;
import com.google.android.gms.cast.framework.SessionManagerListener;
import com.google.android.gms.cast.framework.media.RemoteMediaClient;
import com.google.android.gms.common.images.WebImage;

public class Cast extends AppCompatActivity implements SessionManagerListener,
        AppVisibilityListener,
        CastStateListener {

    private IntroductoryOverlay mIntroductoryOverlay;
    private MenuItem mMediaRouterButton;

    private int position = 0;
    private VideoView videoView;
    private MediaController mediaControls;
    String url = "";


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.cast);

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        //toolbar.setVisibility(View.GONE);

        //setSupportActionBar((Toolbar) findViewById(R.id.toolbar));
        if (mediaControls == null) {
            mediaControls = new MediaController(this);
        }
        videoView = (VideoView) findViewById(R.id.video);

        CastContext.getSharedInstance(this).addCastStateListener(this);
        CastContext.getSharedInstance(this).addAppVisibilityListener(this);
        CastContext.getSharedInstance(this).getSessionManager().addSessionManagerListener(this);


        //Get Intent
        if (getIntent().getExtras() != null) {
            url = getIntent().getExtras().getString("link");
            if (url != null) {
                playChromeCast(url);
            }
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        super.onCreateOptionsMenu(menu);
        getMenuInflater().inflate(R.menu.menu_main, menu);
        mMediaRouterButton = CastButtonFactory.setUpMediaRouteButton(getApplicationContext(),
                menu,
                R.id.media_route_menu_item);

        showIntroductoryOverlay();

        return true;
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        CastContext.getSharedInstance(this).removeAppVisibilityListener(this);
        CastContext.getSharedInstance(this).removeCastStateListener(this);
        CastContext.getSharedInstance(this).getSessionManager().removeSessionManagerListener(this);
    }

    //Play Chrome Cast
    private void playChromeCast(String url) {

        if (CastContext.getSharedInstance(this).getSessionManager().getCurrentCastSession() != null
                && CastContext.getSharedInstance(this).getSessionManager().getCurrentCastSession().getRemoteMediaClient() != null) {

            RemoteMediaClient remoteMediaClient = CastContext.getSharedInstance(this).getSessionManager().getCurrentCastSession().getRemoteMediaClient();


            //if (remoteMediaClient.getMediaInfo() != null && remoteMediaClient.getMediaInfo().getMetadata() != null) {
                //Show Button Cast
            //    startActivity(new Intent(this, ExpandedControlsActivity.class));

            //} else {
                MediaMetadata metadata = new MediaMetadata(MediaMetadata.MEDIA_TYPE_MOVIE);

                metadata.putString(MediaMetadata.KEY_TITLE, getString(R.string.title_cast));
                metadata.putString(MediaMetadata.KEY_SUBTITLE, getString(R.string.title_cast));
                metadata.addImage(new WebImage(Uri.parse(getString(R.string.img_cast))));

                MediaInfo mediaInfo = new MediaInfo.Builder(url)
                        .setStreamType(MediaInfo.STREAM_TYPE_BUFFERED)
                        .setContentType("video/*")
                        .setMetadata(metadata)
                        .build();

                remoteMediaClient.load(mediaInfo, true, 0);

            Intent intent = new Intent(this, ExpandedControlsActivity.class);
            startActivity(intent);
            //finish();

            //}
        } else {

            playLocalVideo(url);

        }
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
                            Cast.this, mMediaRouterButton)
                            .setTitleText(getString(R.string.app_name))
                            .setOverlayColor(R.color.colorPrimary)
                            .setSingleTime()
                            .setOnOverlayDismissedListener(
                                    new IntroductoryOverlay.OnOverlayDismissedListener() {
                                        @Override
                                        public void onOverlayDismissed() {
                                            mIntroductoryOverlay = null;
                                        }
                                    })
                            .build();
                    mIntroductoryOverlay.show();
                }
            });
        }
    }

    @Override
    public void onSessionStarting(Session session) {
        Log.e("Tuts+", "onSessionsStarting");
    }

    @Override
    public void onSessionStarted(Session session, String s) {
        Log.e("Tuts+", "onSessionStarted");
        invalidateOptionsMenu();
    }

    @Override
    public void onSessionStartFailed(Session session, int i) {
        Log.e("Tuts+", "onSessionStartFailed");
    }

    @Override
    public void onSessionEnding(Session session) {
        Log.e("Tuts+", "onSessionEnding");
    }

    @Override
    public void onSessionEnded(Session session, int i) {
        Log.e("Tuts+", "onSessionEnded");
    }

    @Override
    public void onSessionResuming(Session session, String s) {
        Log.e("Tuts+", "onSessionResuming");
    }

    @Override
    public void onSessionResumed(Session session, boolean b) {
        Log.e("Tuts+", "onSessionResumed");
        invalidateOptionsMenu();
    }

    @Override
    public void onSessionResumeFailed(Session session, int i) {
        Log.e("Tuts+", "onSessionResumeFailed");
    }

    @Override
    public void onSessionSuspended(Session session, int i) {
        Log.e("Tuts+", "onSessionSuspended");
    }

    @Override
    public void onAppEnteredForeground() {
        Log.e("Tuts+", "onAppEnteredForeground");
    }

    @Override
    public void onAppEnteredBackground() {
        Log.e("Tuts+", "onAppEnteredBackground");
    }

    @Override
    public void onCastStateChanged(int newState) {
        Log.e("Tuts+", "onCastStateChanged");

        switch (newState) {
            case CastState.CONNECTED: {
                //Toast.makeText(Cast.this, "Connected", Toast.LENGTH_SHORT).show();
                videoView.stopPlayback();
                if (url != null) {
                    playChromeCast(url);
                }
                break;
            }
            case CastState.CONNECTING: {
                //Toast.makeText(Cast.this, "Connecting", Toast.LENGTH_SHORT).show();
                break;
            }
            case CastState.NOT_CONNECTED: {
                //Toast.makeText(Cast.this, "Not Connect", Toast.LENGTH_SHORT).show();
                break;
            }
            case CastState.NO_DEVICES_AVAILABLE: {
                //Toast.makeText(Cast.this, "Nodevice", Toast.LENGTH_SHORT).show();
                break;
            }
        }

        if (newState != CastState.NO_DEVICES_AVAILABLE) {
            showIntroductoryOverlay();
        }
    }

    //Player Local
    private void playLocalVideo(String url) {
        try {
            videoView.setMediaController(mediaControls);
            videoView.setVideoURI(Uri.parse(url));
        } catch (Exception e) {
            e.printStackTrace();
        }
        videoView.requestFocus();

        videoView.setOnPreparedListener(new MediaPlayer.OnPreparedListener() {
            public void onPrepared(MediaPlayer mediaPlayer) {
                videoView.seekTo(position);
                if (position == 0) {
                    videoView.start();
                } else {
                    videoView.pause();
                }
            }
        });
    }

    @Override
    public void onSaveInstanceState(Bundle savedInstanceState) {
        super.onSaveInstanceState(savedInstanceState);
        savedInstanceState.putInt("Position", videoView.getCurrentPosition());
        videoView.pause();
    }

    @Override
    public void onRestoreInstanceState(Bundle savedInstanceState) {
        super.onRestoreInstanceState(savedInstanceState);
        position = savedInstanceState.getInt("Position");
        videoView.seekTo(position);
    }

}