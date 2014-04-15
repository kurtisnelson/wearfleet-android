package com.wearfleet.client;

import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.support.v4.app.NotificationCompat;
import android.support.v4.app.TaskStackBuilder;
import android.text.Html;
import android.widget.Toast;

import com.pusher.client.connection.ConnectionState;
import com.wearfleet.core.events.AbortEvent;
import com.wearfleet.core.events.ChatEvent;

import de.greenrobot.event.EventBus;

public class FleetService extends com.wearfleet.core.FleetService {
    private static final String TAG = "FleetService";
    public static final String CLEAR_CHATS_FILTER = "CLEAR_CHATS_FILTER";
    private final int NOTIFICATION_ID = 0;
    private NotificationManager mNotifyMgr;
    private NotificationCompat.Builder mBuilder;
    private int numMessages = 0;
    private NotificationCompat.InboxStyle mInboxStyle;

    @Override
    public void onCreate() {
        super.onCreate();
        EventBus.getDefault().register(this);
        mNotifyMgr =
                (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        setupNotification();
        registerReceiver(clearChatsReceiver, new IntentFilter(CLEAR_CHATS_FILTER));
        return super.onStartCommand(intent, flags, startId);
    }

    @Override
    public void onDestroy() {
        EventBus.getDefault().unregister(this);
        stopNotification();
        unregisterReceiver(clearChatsReceiver);
        super.onDestroy();
    }

    private void setupNotification() {
        mBuilder =
                new NotificationCompat.Builder(this)
                        .setSmallIcon(R.drawable.ic_launcher)
                        .setContentTitle(getString(R.string.status_unknown))
                        .setContentText(getString(R.string.no_messages));

        mInboxStyle =
                new NotificationCompat.InboxStyle();
        mInboxStyle.setBigContentTitle(getString(R.string.status_unknown));

        mBuilder.setStyle(mInboxStyle);

        Intent chatIntent = new Intent(this, ChatActivity.class);
        TaskStackBuilder stackBuilder = TaskStackBuilder.create(this);
        stackBuilder.addParentStack(ChatActivity.class);
        stackBuilder.addNextIntent(chatIntent);
        PendingIntent chatPendingIntent =
                stackBuilder.getPendingIntent(0, PendingIntent.FLAG_UPDATE_CURRENT);
        mBuilder.addAction(android.R.drawable.sym_action_chat, "Chat", chatPendingIntent);

        Intent statusIntent = new Intent(this, StatusActivity.class);
        TaskStackBuilder statusStackBuilder = TaskStackBuilder.create(this);
        statusStackBuilder.addParentStack(StatusActivity.class);
        statusStackBuilder.addNextIntent(statusIntent);
        PendingIntent statusPendingIntent =
                statusStackBuilder.getPendingIntent(0, PendingIntent.FLAG_UPDATE_CURRENT);

        mBuilder.setContentIntent(statusPendingIntent);

        mBuilder.setOngoing(true);

        PendingIntent stopIntent = PendingIntent.getBroadcast(this, 0, new Intent(super.STOP_SERVICE_FILTER), 0);
        mBuilder.addAction(android.R.drawable.ic_menu_close_clear_cancel, "Stop", stopIntent);

        mNotifyMgr.notify(
                NOTIFICATION_ID,
                mBuilder.build());
    }

    public void onEventMainThread(ConnectionState lastStatus) {
        if (lastStatus != null) {
            mBuilder.setContentTitle(lastStatus.toString());
            mInboxStyle.setBigContentTitle(lastStatus.toString());
            mNotifyMgr.notify(
                    NOTIFICATION_ID,
                    mBuilder.build());
        }
    }

    public void onEventMainThread(ChatEvent e) {
        mBuilder.setNumber(++numMessages);
        mBuilder.setContentText(getString(R.string.new_messages));
        mInboxStyle.addLine(Html.fromHtml("<strong>"+e.getName()+":</strong> " + e.getMessage()));
        mNotifyMgr.notify(
                NOTIFICATION_ID,
                mBuilder.build());
    }

    public void onEventMainThread(AbortEvent e){
        Toast.makeText(this, "Error: " + e.getMessage(), Toast.LENGTH_LONG).show();
    }
    private void stopNotification() {
        mNotifyMgr.cancel(NOTIFICATION_ID);
    }

    public static void start(Context context) {
        Intent i = new Intent(context, FleetService.class);
        context.startService(i);
    }

    public static void stop(Context context) {
        Intent i = new Intent(context, FleetService.class);
        context.stopService(i);
    }

    public static boolean isRunning(Context c) {
        return com.wearfleet.core.FleetService.isRunning(c, FleetService.class);
    }

    protected BroadcastReceiver clearChatsReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            numMessages = 0;
            mBuilder.setNumber(0);
            mBuilder.setContentText(getString(R.string.no_messages));
            mInboxStyle =
                    new NotificationCompat.InboxStyle();
            mBuilder.setStyle(mInboxStyle);
            mNotifyMgr.notify(
                    NOTIFICATION_ID,
                    mBuilder.build());
            onEventMainThread(EventBus.getDefault().getStickyEvent(ConnectionState.class));
        }
    };
}
