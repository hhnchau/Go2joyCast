package com.appromobile.Go2joyCast.cast;

import android.os.Bundle;
import android.view.Menu;
import android.widget.ImageView;

import com.appromobile.Go2joyCast.R;
import com.google.android.gms.cast.framework.CastButtonFactory;
import com.google.android.gms.cast.framework.media.widget.ExpandedControllerActivity;

/**
 * Created by appro on 01/09/2017.
 */
public class ExpandedControlsActivity extends ExpandedControllerActivity {
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        super.onCreateOptionsMenu(menu);
        getMenuInflater().inflate(R.menu.menu_main, menu);
        CastButtonFactory.setUpMediaRouteButton(this, menu, R.id.media_route_menu_item);
        return true;
    }
}
