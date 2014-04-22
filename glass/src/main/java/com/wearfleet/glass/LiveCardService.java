package com.wearfleet.glass;

import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.media.AudioManager;
import android.os.IBinder;
import android.util.Log;

import com.google.android.glass.media.Sounds;
import com.google.android.glass.timeline.LiveCard;
import com.wearfleet.core.events.ChatEvent;

import de.greenrobot.event.EventBus;

public class LiveCardService extends Service {
    private static final String TAG = "LiveCardService";
    private LiveCard mLiveCard;
    private static final String LIVE_CARD_TAG = "service_card";
    private StatusViewRenderer mRenderer;

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        FleetService.start(this);
        publishCard();
        return START_STICKY;
    }

    @Override
    public void onCreate() {
        super.onCreate();
        EventBus.getDefault().register(this);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        FleetService.stop(this);
        unpublishCard();
        EventBus.getDefault().unregister(this);
    }

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    private void publishCard() {
        if (mLiveCard == null) {
            mLiveCard = new LiveCard(this, LIVE_CARD_TAG);
            mLiveCard.attach(this);
            mRenderer = new StatusViewRenderer(this);
            mLiveCard.setDirectRenderingEnabled(true);
            mLiveCard.getSurfaceHolder().addCallback(mRenderer);

            Intent intent = new Intent(this, MenuActivity.class);
            mLiveCard.setAction(PendingIntent.getActivity(this, 0,
                    intent, 0));
            mLiveCard.publish(LiveCard.PublishMode.REVEAL);
        } else {
            mLiveCard.navigate();
        }
    }


    private void unpublishCard() {
        if (mLiveCard != null) {
            if(mRenderer != null){
                mLiveCard.getSurfaceHolder().removeCallback(mRenderer);
                mRenderer = null;
            }
            mLiveCard.unpublish();
            mLiveCard = null;
        }
    }

    public void onEventMainThread(ChatEvent e){
        Log.d(TAG, "ChatEvent");
        AudioManager audio = (AudioManager) getSystemService(Context.AUDIO_SERVICE);
        audio.playSoundEffect(Sounds.SUCCESS);
        if(mLiveCard != null)
            mLiveCard.navigate();
    }
}
