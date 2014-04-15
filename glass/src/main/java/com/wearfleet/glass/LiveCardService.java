package com.wearfleet.glass;

import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.media.AudioManager;
import android.os.IBinder;
import android.util.Log;

import com.google.android.glass.app.Card;
import com.google.android.glass.media.Sounds;
import com.google.android.glass.timeline.LiveCard;
import com.google.android.glass.timeline.TimelineManager;
import com.wearfleet.core.events.ChatEvent;

import de.greenrobot.event.EventBus;

public class LiveCardService extends Service {
    private static final String TAG = "LiveCardService";
    private LiveCard mLiveCard;
    private static final String LIVE_CARD_TAG = "service_card";
    private StatusDrawer mCallback;

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
            TimelineManager tm = TimelineManager.from(this);
            mLiveCard = tm.createLiveCard(LIVE_CARD_TAG);

            mCallback = new StatusDrawer(this);
            mLiveCard.setDirectRenderingEnabled(true).getSurfaceHolder().addCallback(mCallback);

            Intent intent = new Intent(this, MenuActivity.class);
            mLiveCard.setAction(PendingIntent.getActivity(this, 0,
                    intent, 0));
            mLiveCard.publish(LiveCard.PublishMode.REVEAL);
        } else {
            return;
        }
    }


    private void unpublishCard() {
        if (mLiveCard != null) {
            if(mCallback != null){
                mLiveCard.getSurfaceHolder().removeCallback(mCallback);
            }
            mLiveCard.unpublish();
            mLiveCard = null;
        }
    }

    public void onEventMainThread(ChatEvent e){
        Log.d(TAG, "ChatEvent");
        Card c = new Card(this);
        c.setText(e.getMessage());
        c.setFootnote(e.getName());
        TimelineManager tm = TimelineManager.from(this);
        tm.insert(c);
        AudioManager audio = (AudioManager) getSystemService(Context.AUDIO_SERVICE);
        audio.playSoundEffect(Sounds.SUCCESS);
    }
}
