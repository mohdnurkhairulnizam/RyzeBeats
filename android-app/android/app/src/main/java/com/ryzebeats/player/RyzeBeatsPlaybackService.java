package com.ryzebeats.player;

import android.content.Intent;
import android.os.Bundle;
import androidx.annotation.Nullable;
import androidx.media3.common.AudioAttributes;
import androidx.media3.common.C;
import androidx.media3.common.MediaItem;
import androidx.media3.common.Player;
import androidx.media3.exoplayer.ExoPlayer;
import androidx.media3.session.MediaSession;
import androidx.media3.session.MediaSessionService;
import java.util.ArrayList;

public class RyzeBeatsPlaybackService extends MediaSessionService {
    public static final String ACTION_SET_QUEUE = "com.ryzebeats.player.MEDIA3_SET_QUEUE";
    public static final String ACTION_PLAY = "com.ryzebeats.player.MEDIA3_PLAY";
    public static final String ACTION_PAUSE = "com.ryzebeats.player.MEDIA3_PAUSE";
    public static final String ACTION_NEXT = "com.ryzebeats.player.MEDIA3_NEXT";
    public static final String ACTION_PREVIOUS = "com.ryzebeats.player.MEDIA3_PREVIOUS";
    public static final String EXTRA_URIS = "uris";
    public static final String EXTRA_INDEX = "index";
    public static final String EXTRA_PLAYING = "playing";
    private ExoPlayer player;
    private MediaSession session;

    @Override public void onCreate() {
        super.onCreate();
        AudioAttributes attributes = new AudioAttributes.Builder().setUsage(C.USAGE_MEDIA).setContentType(C.AUDIO_CONTENT_TYPE_MUSIC).build();
        player = new ExoPlayer.Builder(this).setAudioAttributes(attributes, true).setHandleAudioBecomingNoisy(true).build();
        player.setRepeatMode(Player.REPEAT_MODE_OFF);
        session = new MediaSession.Builder(this, player).setCallback(new MediaSession.Callback() {}).build();
        player.addListener(new Player.Listener() { @Override public void onPlaybackStateChanged(int state) { if (state == Player.STATE_ENDED) { if (player.hasNextMediaItem()) player.seekToNextMediaItem(); else stopSelf(); } } });
    }

    @Override public int onStartCommand(Intent intent, int flags, int startId) {
        if (intent != null) {
            String action = intent.getAction();
            if (ACTION_SET_QUEUE.equals(action)) setQueue(intent);
            else if (ACTION_PLAY.equals(action)) player.play();
            else if (ACTION_PAUSE.equals(action)) player.pause();
            else if (ACTION_NEXT.equals(action)) player.seekToNextMediaItem();
            else if (ACTION_PREVIOUS.equals(action)) player.seekToPreviousMediaItem();
        }
        return START_STICKY;
    }

    private void setQueue(Intent intent) {
        ArrayList<String> uris = intent.getStringArrayListExtra(EXTRA_URIS);
        if (uris == null || uris.isEmpty()) return;
        ArrayList<MediaItem> items = new ArrayList<>();
        for (String uri : uris) items.add(new MediaItem.Builder().setUri(uri).build());
        int index = Math.max(0, Math.min(intent.getIntExtra(EXTRA_INDEX, 0), items.size() - 1));
        player.setMediaItems(items, index, 0L);
        player.prepare();
        if (intent.getBooleanExtra(EXTRA_PLAYING, false)) player.play();
    }

    @Nullable @Override public MediaSession onGetSession(MediaSession.ControllerInfo controllerInfo) { return session; }
    @Override public void onTaskRemoved(Intent rootIntent) { if (player != null && player.isPlaying()) return; stopSelf(); }
    @Override public void onDestroy() { if (session != null) session.release(); if (player != null) player.release(); super.onDestroy(); }
}
