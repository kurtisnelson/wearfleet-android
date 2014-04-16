package com.wearfleet.glass;

import android.content.Context;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.widget.FrameLayout;
import android.widget.TextView;

import com.pusher.client.connection.ConnectionState;
import com.wearfleet.core.events.ChatEvent;

import de.greenrobot.event.EventBus;

public class StatusView extends FrameLayout {
    private final TextView mStatus;
    private final TextView mFooter;
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

        mStatus = (TextView) findViewById(R.id.status_text);
        mFooter = (TextView) findViewById(R.id.footer);
        EventBus.getDefault().registerSticky(this);
    }

    public void setListener(ChangeListener listener) {
        mChangeListener = listener;
    }

    public void onEventMainThread(ConnectionState lastStatus){
        if(lastStatus != null){
            if(lastStatus.equals(ConnectionState.CONNECTED)){
                mStatus.setText(getContext().getString(R.string.no_messages));
            }else{
                mStatus.setText(getContext().getString(R.string.status_unknown));
            }
        }
        if (mChangeListener != null) {
            mChangeListener.onChange();
        }
    }

    public void onEventMainThread(ChatEvent e){
        mStatus.setText(e.getMessage());
        mFooter.setText(e.getName());
        if (mChangeListener != null) {
            mChangeListener.onChange();
        }
    }

}
