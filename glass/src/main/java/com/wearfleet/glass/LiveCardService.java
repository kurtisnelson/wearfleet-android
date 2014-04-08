package com.wearfleet.glass;

import android.app.PendingIntent;
import android.app.Service;
import android.content.Intent;
import android.os.IBinder;

import com.google.android.glass.timeline.LiveCard;
import com.google.android.glass.timeline.TimelineManager;
import com.wearfleet.core.PusherService;

public class LiveCardService extends Service {
    private LiveCard mLiveCard;
    private static final String LIVE_CARD_TAG = "service_card";
    private StatusDrawer mCallback;

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        PusherService.start(this);
        publishCard();
        return START_STICKY;
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        PusherService.stop(this);
        unpublishCard();
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
            // Card is already published.
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
}
