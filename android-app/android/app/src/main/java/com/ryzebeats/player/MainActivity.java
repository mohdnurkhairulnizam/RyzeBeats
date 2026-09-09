package com.ryzebeats.player;

import com.getcapacitor.BridgeActivity;

public class MainActivity extends BridgeActivity {
    @Override
    public void onCreate(android.os.Bundle savedInstanceState) {
        registerPlugin(MusicNotificationPlugin.class);
        registerPlugin(MediaLibraryPlugin.class);
        super.onCreate(savedInstanceState);
        if (android.os.Build.VERSION.SDK_INT >= 30) {
            getWindow().setDecorFitsSystemWindows(true);
        }
        getWindow().setStatusBarColor(android.graphics.Color.rgb(8, 22, 32));
        getWindow().setNavigationBarColor(android.graphics.Color.rgb(8, 22, 32));
        getWindow().getDecorView().setSystemUiVisibility(0);
        if (android.os.Build.VERSION.SDK_INT >= 33) requestPermissions(new String[]{"android.permission.POST_NOTIFICATIONS"}, 8422);
    }
}
