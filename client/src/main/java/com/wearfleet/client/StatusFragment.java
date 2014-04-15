package com.wearfleet.client;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CompoundButton;

public class StatusFragment extends com.wearfleet.core.StatusFragment {

    public static StatusFragment newInstance() {
        Bundle args = new Bundle();
        StatusFragment fragment = new StatusFragment();
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View v = super.onCreateView(inflater, container, savedInstanceState);

        if (savedInstanceState != null && savedInstanceState.getBoolean(STATE_RUNNING)) {
            activateToggle.setChecked(true);
        } else {
            activateToggle.setChecked(FleetService.isRunning(getActivity()));
        }

        activateToggle.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton compoundButton, boolean b) {
                if (b) {
                    FleetService.start(getActivity());
                } else {
                    FleetService.stop(getActivity());
                }
            }
        });
        return v;
    }
}
