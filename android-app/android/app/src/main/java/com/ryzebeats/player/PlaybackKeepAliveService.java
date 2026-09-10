package com.ryzebeats.player;

import android.app.*;
import android.content.*;
import android.media.*;
import android.net.Uri;
import android.os.*;
import androidx.core.app.NotificationCompat;
import java.util.ArrayList;

public class PlaybackKeepAliveService extends Service {
    public static final String ACTION_SET_QUEUE = "com.ryzebeats.player.SET_QUEUE";
    public static final String ACTION_PLAY = "com.ryzebeats.player.PLAY";
    public static final String ACTION_PAUSE = "com.ryzebeats.player.PAUSE";
    public static final String ACTION_NEXT = "com.ryzebeats.player.NEXT";
    public static final String ACTION_PREVIOUS = "com.ryzebeats.player.PREVIOUS";
    public static final String EXTRA_URIS = "uris";
    public static final String EXTRA_INDEX = "index";
    public static final String EXTRA_PLAYING = "playing";
    private static final String CHANNEL_ID = "ryzebeats-playback";
    private static final int NOTIFICATION_ID = 8421;
    private final ArrayList<String> queue = new ArrayList<>();
    private MediaPlayer player;
    private boolean prepared;
    private boolean playWhenPrepared;
    private int index;
    private PowerManager.WakeLock wakeLock;
    private android.content.SharedPreferences state() { return getSharedPreferences("ryzebeats-native-playback", MODE_PRIVATE); }
    private void saveState(boolean playing) { String uri = queue.isEmpty() || index >= queue.size() ? "" : queue.get(index); state().edit().putString("uri", uri).putInt("index", index).putBoolean("playing", playing).apply(); }

    @Override public void onCreate() { super.onCreate(); if (Build.VERSION.SDK_INT >= 26) { NotificationChannel c = new NotificationChannel(CHANNEL_ID, "RyzeBeats playback", NotificationManager.IMPORTANCE_LOW); c.setShowBadge(false); ((NotificationManager)getSystemService(NOTIFICATION_SERVICE)).createNotificationChannel(c); } PowerManager pm=(PowerManager)getSystemService(POWER_SERVICE); wakeLock=pm.newWakeLock(PowerManager.PARTIAL_WAKE_LOCK,"RyzeBeats:Playback"); wakeLock.setReferenceCounted(false); }
    @Override public int onStartCommand(Intent intent,int flags,int startId) { if (intent != null) { String a=intent.getAction(); if (ACTION_SET_QUEUE.equals(a)) { ArrayList<String> u=intent.getStringArrayListExtra(EXTRA_URIS); if(u!=null){queue.clear();queue.addAll(u);} index=Math.max(0,Math.min(intent.getIntExtra(EXTRA_INDEX,0),Math.max(0,queue.size()-1))); if(intent.getBooleanExtra(EXTRA_PLAYING,false)) startCurrent(); else pause(); } else if(ACTION_PLAY.equals(a)) { if(player==null) startCurrent(); else if(prepared) { if(!player.isPlaying()) player.start(); saveState(true); refreshNotification(); acquire(); } else { playWhenPrepared=true; saveState(true); refreshNotification(); } } else if(ACTION_PAUSE.equals(a)) pause(); else if(ACTION_NEXT.equals(a)) next(); else if(ACTION_PREVIOUS.equals(a)) previous(); } if(player!=null || !queue.isEmpty()) refreshNotification(); return START_STICKY; }
    private void startCurrent(){ if(queue.isEmpty())return; playWhenPrepared=true; saveState(true); releasePlayer(); try { player=new MediaPlayer(); prepared=false; player.setAudioAttributes(new AudioAttributes.Builder().setUsage(AudioAttributes.USAGE_MEDIA).setContentType(AudioAttributes.CONTENT_TYPE_MUSIC).build()); player.setDataSource(this, Uri.parse(queue.get(index))); player.setOnPreparedListener(p->{prepared=true; if(playWhenPrepared){p.start();saveState(true);acquire();} else {saveState(false);} refreshNotification();}); player.setOnCompletionListener(p->{if(index+1<queue.size()){index++;startCurrent();}else{pause();}}); player.setOnErrorListener((p,w,e)->{if(index+1<queue.size()){index++;startCurrent();}return true;}); player.prepareAsync(); }catch(Exception e){if(index+1<queue.size()){index++;startCurrent();}} }
    private void next(){if(queue.isEmpty())return;index=(index+1)%queue.size();startCurrent();} private void previous(){if(queue.isEmpty())return;index=(index-1+queue.size())%queue.size();startCurrent();}
    private void pause(){playWhenPrepared=false; if(player!=null&&player.isPlaying())player.pause(); saveState(false); releaseWake(); refreshNotification();} private void acquire(){if(wakeLock!=null&&!wakeLock.isHeld())wakeLock.acquire();} private void releaseWake(){if(wakeLock!=null&&wakeLock.isHeld())wakeLock.release();}
    private PendingIntent action(String a){return PendingIntent.getService(this,a.hashCode(),new Intent(this,PlaybackKeepAliveService.class).setAction(a),PendingIntent.FLAG_UPDATE_CURRENT|PendingIntent.FLAG_IMMUTABLE);}
    private void refreshNotification(){boolean playing=player!=null&&player.isPlaying(); Notification n=new NotificationCompat.Builder(this,CHANNEL_ID).setSmallIcon(com.ryzebeats.player.R.mipmap.ic_launcher).setContentTitle("RyzeBeats").setContentText("Local music").setContentIntent(PendingIntent.getActivity(this, 0, new Intent(this, MainActivity.class).addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP | Intent.FLAG_ACTIVITY_CLEAR_TOP), PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE)).setOngoing(playing).setSilent(true).setShowWhen(false).addAction(new NotificationCompat.Action(android.R.drawable.ic_media_previous,"Previous",action(ACTION_PREVIOUS))).addAction(new NotificationCompat.Action(playing?android.R.drawable.ic_media_pause:android.R.drawable.ic_media_play,playing?"Pause":"Play",action(playing?ACTION_PAUSE:ACTION_PLAY))).addAction(new NotificationCompat.Action(android.R.drawable.ic_media_next,"Next",action(ACTION_NEXT))).build(); if(Build.VERSION.SDK_INT>=29)startForeground(NOTIFICATION_ID,n,android.content.pm.ServiceInfo.FOREGROUND_SERVICE_TYPE_MEDIA_PLAYBACK);else startForeground(NOTIFICATION_ID,n);}
    private void releasePlayer(){if(player!=null){try{player.reset();player.release();}catch(Exception ignored){}player=null;} prepared=false;}
    @Override public void onDestroy(){releasePlayer();releaseWake();super.onDestroy();} @Override public IBinder onBind(Intent i){return null;}
}
