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
import java.util.ArrayList;

@CapacitorPlugin(name = "MusicNotification")
public class MusicNotificationPlugin extends Plugin {
    private static final String CHANNEL_ID = "ryzebeats-playback";
    private static final int NOTIFICATION_ID = 8421;
    private static final String PREFS = "ryzebeats-notification";
    private static final String PENDING = "pending-actions";

    @Override public void load() { createChannel(); }
    private void createChannel() { if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) { NotificationChannel channel = new NotificationChannel(CHANNEL_ID, "RyzeBeats playback", NotificationManager.IMPORTANCE_LOW); channel.setDescription("Playback controls for local music"); channel.setShowBadge(false); ((NotificationManager) getContext().getSystemService(Context.NOTIFICATION_SERVICE)).createNotificationChannel(channel); } }
    private PendingIntent actionIntent(String action) { Intent intent = new Intent(getContext(), MediaActionReceiver.class); intent.putExtra("action", action); return PendingIntent.getBroadcast(getContext(), action.hashCode(), intent, PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE); }
    private void send(String action) { Intent service = new Intent(getContext(), PlaybackKeepAliveService.class).setAction(action); if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) ContextCompat.startForegroundService(getContext(), service); else getContext().startService(service); }
    @PluginMethod public void update(PluginCall call) { send(call.getBoolean("playing", false) ? PlaybackKeepAliveService.ACTION_PLAY : PlaybackKeepAliveService.ACTION_PAUSE); call.resolve(); }
    @PluginMethod public void setQueue(PluginCall call) { JSArray values = call.getArray("uris", new JSArray()); ArrayList<String> uris = new ArrayList<>(); try { for (int i=0;i<values.length();i++) uris.add(values.getString(i)); } catch(Exception ignored) {} Intent service = new Intent(getContext(), PlaybackKeepAliveService.class).setAction(PlaybackKeepAliveService.ACTION_SET_QUEUE); service.putStringArrayListExtra(PlaybackKeepAliveService.EXTRA_URIS, uris); service.putExtra(PlaybackKeepAliveService.EXTRA_INDEX, call.getInt("index",0)); service.putExtra(PlaybackKeepAliveService.EXTRA_PLAYING, call.getBoolean("playing",false)); if(Build.VERSION.SDK_INT>=Build.VERSION_CODES.O) ContextCompat.startForegroundService(getContext(),service); else getContext().startService(service); call.resolve(); }
    @PluginMethod public void getPendingActions(PluginCall call) { JSArray actions = new JSArray(); synchronized (this) { String pending = getContext().getSharedPreferences(PREFS, Context.MODE_PRIVATE).getString(PENDING, ""); if (!pending.isEmpty()) { for (String action : pending.split(",")) actions.put(action); getContext().getSharedPreferences(PREFS, Context.MODE_PRIVATE).edit().remove(PENDING).apply(); } } JSObject result = new JSObject(); result.put("actions", actions); call.resolve(result); }
    @PluginMethod public void hide(PluginCall call) { ((NotificationManager) getContext().getSystemService(Context.NOTIFICATION_SERVICE)).cancel(NOTIFICATION_ID); call.resolve(); }
}
