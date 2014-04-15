package com.wearfleet.core;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CompoundButton;
import android.widget.TextView;
import android.widget.ToggleButton;

import com.pusher.client.connection.ConnectionState;

import de.greenrobot.event.EventBus;

public abstract class StatusFragment extends Fragment {

    protected static final String STATE_RUNNING = "STATE_RUNNING";
    private static final String STATE_CON_STATUS = "STATE_CON_STATUS";
    private TextView statusText;
    protected ToggleButton activateToggle;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.fragment_status, container, false);
        statusText = (TextView) v.findViewById(R.id.status_text);
        activateToggle = (ToggleButton) v.findViewById(R.id.activate_toggle);

        if (savedInstanceState != null)
            statusText.setText(savedInstanceState.getCharSequence(STATE_CON_STATUS));
        return v;
    }

    @Override
    public void onResume() {
        super.onResume();
        EventBus.getDefault().registerSticky(this);
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putCharSequence(STATE_CON_STATUS, statusText.getText());
        outState.putBoolean(STATE_RUNNING, activateToggle.isChecked());
    }

    @Override
    public void onPause() {
        super.onPause();
        EventBus.getDefault().unregister(this);
    }

    public void onEventMainThread(ConnectionState lastStatus) {
        if (lastStatus != null)
            statusText.setText(lastStatus.toString());
    }
}
