package com.wearfleet.glass;

import android.content.Context;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.widget.FrameLayout;
import android.widget.TextView;

import com.pusher.client.connection.ConnectionState;

import de.greenrobot.event.EventBus;

public class StatusView extends FrameLayout {
    private final TextView mStatusView;
    private ChangeListener mChangeListener;

    public interface ChangeListener {
        public void onChange();
    }

    public StatusView(Context context) {
        this(context, null, 0);
    }

    public StatusView(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public StatusView(Context context, AttributeSet attrs, int style) {
        super(context, attrs, style);

        LayoutInflater.from(context).inflate(R.layout.card_status, this);

        mStatusView = (TextView) findViewById(R.id.status_text);
        EventBus.getDefault().registerSticky(this);
    }

    public void setListener(ChangeListener listener) {
        mChangeListener = listener;
    }

    public void onEventMainThread(ConnectionState lastStatus){
        if(lastStatus != null)
            mStatusView.setText(lastStatus.toString());
        if (mChangeListener != null) {
            mChangeListener.onChange();
        }
    }

}
