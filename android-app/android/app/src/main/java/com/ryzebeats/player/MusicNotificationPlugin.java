package com.ryzebeats.player;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import androidx.core.content.ContextCompat;
import androidx.core.app.NotificationCompat;
import com.getcapacitor.JSArray;
import com.getcapacitor.JSObject;
import com.getcapacitor.Plugin;
import com.getcapacitor.PluginCall;
import com.getcapacitor.PluginMethod;
import com.getcapacitor.annotation.CapacitorPlugin;

@CapacitorPlugin(name = "MusicNotification")
public class MusicNotificationPlugin extends Plugin {
    private static final String CHANNEL_ID = "ryzebeats-playback";
    private static final int NOTIFICATION_ID = 8421;
    private static final String PREFS = "ryzebeats-notification";
    private static final String PENDING = "pending-actions";

    @Override public void load() { createChannel(); }
    private void createChannel() { if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) { NotificationChannel channel = new NotificationChannel(CHANNEL_ID, "RyzeBeats playback", NotificationManager.IMPORTANCE_LOW); channel.setDescription("Playback controls for local music"); channel.setShowBadge(false); ((NotificationManager) getContext().getSystemService(Context.NOTIFICATION_SERVICE)).createNotificationChannel(channel); } }
    private PendingIntent actionIntent(String action) { Intent intent = new Intent(getContext(), MediaActionReceiver.class); intent.putExtra("action", action); return PendingIntent.getBroadcast(getContext(), action.hashCode(), intent, PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE); }
    @PluginMethod public void update(PluginCall call) { String title = call.getString("title", "RyzeBeats"); boolean playing = call.getBoolean("playing", false); Notification notification = new NotificationCompat.Builder(getContext(), CHANNEL_ID).setSmallIcon(com.ryzebeats.player.R.mipmap.ic_launcher).setContentTitle(title).setContentText("Local MP3 · RyzeBeats").setContentIntent(PendingIntent.getActivity(getContext(), 0, new Intent(getContext(), MainActivity.class).addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP), PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE)).setOngoing(playing).setOnlyAlertOnce(true).setSilent(true).setShowWhen(false).addAction(new NotificationCompat.Action(android.R.drawable.ic_media_previous, "Previous", actionIntent("previous"))).addAction(new NotificationCompat.Action(playing ? android.R.drawable.ic_media_pause : android.R.drawable.ic_media_play, playing ? "Pause" : "Play", actionIntent("toggle"))).addAction(new NotificationCompat.Action(android.R.drawable.ic_media_next, "Next", actionIntent("next"))).build(); ((NotificationManager) getContext().getSystemService(Context.NOTIFICATION_SERVICE)).notify(NOTIFICATION_ID, notification); Intent service = new Intent(getContext(), PlaybackKeepAliveService.class); if (playing) { if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) ContextCompat.startForegroundService(getContext(), service); else getContext().startService(service); } else { getContext().stopService(service); } call.resolve(); }
    @PluginMethod public void getPendingActions(PluginCall call) { JSArray actions = new JSArray(); synchronized (this) { String pending = getContext().getSharedPreferences(PREFS, Context.MODE_PRIVATE).getString(PENDING, ""); if (!pending.isEmpty()) { for (String action : pending.split(",")) actions.put(action); getContext().getSharedPreferences(PREFS, Context.MODE_PRIVATE).edit().remove(PENDING).apply(); } } JSObject result = new JSObject(); result.put("actions", actions); call.resolve(result); }
    @PluginMethod public void hide(PluginCall call) { ((NotificationManager) getContext().getSystemService(Context.NOTIFICATION_SERVICE)).cancel(NOTIFICATION_ID); call.resolve(); }
}
