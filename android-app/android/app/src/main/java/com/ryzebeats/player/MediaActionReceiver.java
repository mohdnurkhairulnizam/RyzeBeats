package com.ryzebeats.player;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import androidx.core.content.ContextCompat;

public class MediaActionReceiver extends BroadcastReceiver {
    private static final String PREFS = "ryzebeats-notification";
    private static final String PENDING = "pending-actions";
    @Override public void onReceive(Context context, Intent intent) {
        String action = intent.getStringExtra("action");
        if (action == null) return;
        String nativeAction = "previous".equals(action) ? PlaybackKeepAliveService.ACTION_PREVIOUS : "next".equals(action) ? PlaybackKeepAliveService.ACTION_NEXT : PlaybackKeepAliveService.ACTION_TOGGLE;
        Intent service = new Intent(context, PlaybackKeepAliveService.class).setAction(nativeAction);
        ContextCompat.startForegroundService(context, service);
        String current = context.getSharedPreferences(PREFS, Context.MODE_PRIVATE).getString(PENDING, "");
        String next = current.isEmpty() ? action : current + "," + action;
        context.getSharedPreferences(PREFS, Context.MODE_PRIVATE).edit().putString(PENDING, next).apply();
        Intent launch = new Intent(context, MainActivity.class).addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_SINGLE_TOP);
        context.startActivity(launch);
    }
}
