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

public class StatusFragment extends Fragment{

    private static final String STATE_RUNNING = "STATE_RUNNING";
    private static final String STATE_CON_STATUS = "STATE_CON_STATUS";
    private TextView statusText;
    private ToggleButton activateToggle;

    public static StatusFragment newInstance() {
        Bundle args = new Bundle();
        StatusFragment fragment = new StatusFragment();
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.fragment_status, container, false);
        statusText = (TextView) v.findViewById(R.id.status_text);
        activateToggle = (ToggleButton) v.findViewById(R.id.activate_toggle);

        if(savedInstanceState != null && savedInstanceState.getBoolean(STATE_RUNNING)){
            activateToggle.setChecked(true);
        }else {
            activateToggle.setChecked(PusherService.isRunning(getActivity()));
        }

        if(savedInstanceState != null)
            statusText.setText(savedInstanceState.getCharSequence(STATE_CON_STATUS));

        activateToggle.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton compoundButton, boolean b) {
                if(b){
                    PusherService.start(getActivity());
                }else{
                    PusherService.stop(getActivity());
                }
            }
        });
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

    public void onEventMainThread(ConnectionState lastStatus){
        if(lastStatus != null)
            statusText.setText(lastStatus.toString());
    }
}
