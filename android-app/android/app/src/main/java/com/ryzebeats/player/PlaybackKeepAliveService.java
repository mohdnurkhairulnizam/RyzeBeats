package com.ryzebeats.player;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Intent;
import android.media.AudioAttributes;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Build;
import android.os.IBinder;
import android.os.PowerManager;
import androidx.core.app.NotificationCompat;
import java.util.ArrayList;

public class PlaybackKeepAliveService extends Service {
    public static final String ACTION_SET_QUEUE = "com.ryzebeats.player.SET_QUEUE";
    public static final String ACTION_NEXT = "com.ryzebeats.player.NEXT";
    public static final String ACTION_PREVIOUS = "com.ryzebeats.player.PREVIOUS";
    public static final String ACTION_TOGGLE = "com.ryzebeats.player.TOGGLE";
    public static final String EXTRA_URIS = "uris";
    public static final String EXTRA_INDEX = "index";
    public static final String EXTRA_PLAYING = "playing";
    public static final String EXTRA_TITLE = "title";
    private static final String CHANNEL_ID = "ryzebeats-playback";
    private static final int NOTIFICATION_ID = 8421;
    private final ArrayList<String> queue = new ArrayList<>();
    private int index = 0;
    private boolean repeatAll = false;
    private MediaPlayer player;
    private PowerManager.WakeLock wakeLock;
    private String title = "RyzeBeats";

    @Override public void onCreate() { super.onCreate(); createChannel(); PowerManager power = (PowerManager) getSystemService(POWER_SERVICE); wakeLock = power.newWakeLock(PowerManager.PARTIAL_WAKE_LOCK, "RyzeBeats:NativePlayback"); wakeLock.setReferenceCounted(false); }
    private void createChannel() { if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) { NotificationChannel channel = new NotificationChannel(CHANNEL_ID, "RyzeBeats playback", NotificationManager.IMPORTANCE_LOW); channel.setDescription("Background local music playback"); channel.setShowBadge(false); ((NotificationManager) getSystemService(NOTIFICATION_SERVICE)).createNotificationChannel(channel); } }
    @Override public int onStartCommand(Intent intent, int flags, int startId) { if (intent != null) { String action = intent.getAction(); if (ACTION_SET_QUEUE.equals(action)) { ArrayList<String> incoming = intent.getStringArrayListExtra(EXTRA_URIS); if (incoming != null) { queue.clear(); queue.addAll(incoming); } index = Math.max(0, Math.min(intent.getIntExtra(EXTRA_INDEX, 0), Math.max(0, queue.size() - 1))); title = intent.getStringExtra(EXTRA_TITLE) == null ? "RyzeBeats" : intent.getStringExtra(EXTRA_TITLE); if (intent.getBooleanExtra(EXTRA_PLAYING, false)) startCurrent(); else pause(); } else if (ACTION_NEXT.equals(action)) next(); else if (ACTION_PREVIOUS.equals(action)) previous(); else if (ACTION_TOGGLE.equals(action)) toggle(); } if (player != null || !queue.isEmpty()) startForegroundNow(); return START_STICKY; }
    private void startForegroundNow() { Notification notification = new NotificationCompat.Builder(this, CHANNEL_ID).setSmallIcon(com.ryzebeats.player.R.mipmap.ic_launcher).setContentTitle(title).setContentText("RyzeBeats · local music").setContentIntent(PendingIntent.getActivity(this, 0, new Intent(this, MainActivity.class).addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP), PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE)).setOngoing(player != null && player.isPlaying()).setSilent(true).setShowWhen(false).addAction(new NotificationCompat.Action(android.R.drawable.ic_media_previous, "Previous", action(ACTION_PREVIOUS))).addAction(new NotificationCompat.Action(player != null && player.isPlaying() ? android.R.drawable.ic_media_pause : android.R.drawable.ic_media_play, player != null && player.isPlaying() ? "Pause" : "Play", action(ACTION_TOGGLE))).addAction(new NotificationCompat.Action(android.R.drawable.ic_media_next, "Next", action(ACTION_NEXT))).build(); if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) startForeground(NOTIFICATION_ID, notification, android.content.pm.ServiceInfo.FOREGROUND_SERVICE_TYPE_MEDIA_PLAYBACK); else startForeground(NOTIFICATION_ID, notification); }
    private PendingIntent action(String action) { return PendingIntent.getService(this, action.hashCode(), new Intent(this, PlaybackKeepAliveService.class).setAction(action), PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE); }
    private void startCurrent() { if (queue.isEmpty()) return; releasePlayer(); try { player = new MediaPlayer(); player.setAudioAttributes(new AudioAttributes.Builder().setUsage(AudioAttributes.USAGE_MEDIA).setContentType(AudioAttributes.CONTENT_TYPE_MUSIC).build()); player.setDataSource(this, Uri.parse(queue.get(index))); player.setOnPreparedListener(mp -> { if (wakeLock != null && !wakeLock.isHeld()) wakeLock.acquire(); mp.start(); startForegroundNow(); }); player.setOnCompletionListener(mp -> { if (index + 1 < queue.size()) { index++; startCurrent(); } else if (repeatAll && !queue.isEmpty()) { index = 0; startCurrent(); } else { pause(); } }); player.setOnErrorListener((mp, what, extra) -> { if (index + 1 < queue.size()) { index++; startCurrent(); } return true; }); player.prepareAsync(); } catch (Exception error) { if (index + 1 < queue.size()) { index++; startCurrent(); } } }
    private void next() { if (queue.isEmpty()) return; index = (index + 1) % queue.size(); startCurrent(); }
    private void previous() { if (queue.isEmpty()) return; index = (index - 1 + queue.size()) % queue.size(); startCurrent(); }
    private void toggle() { if (player == null) startCurrent(); else if (player.isPlaying()) pause(); else { player.start(); startForegroundNow(); } }
    private void pause() { if (player != null && player.isPlaying()) player.pause(); if (wakeLock != null && wakeLock.isHeld()) wakeLock.release(); if (player != null) startForegroundNow(); }
    private void releasePlayer() { if (player != null) { try { player.reset(); player.release(); } catch (Exception ignored) {} player = null; } }
    @Override public void onDestroy() { releasePlayer(); if (wakeLock != null && wakeLock.isHeld()) wakeLock.release(); super.onDestroy(); }
    @Override public IBinder onBind(Intent intent) { return null; }
}
