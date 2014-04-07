package com.wearfleet.client;

import android.os.Bundle;
import android.support.v4.app.Fragment;

import com.wearfleet.core.SingleFragmentActivity;
import com.wearfleet.core.StatusFragment;

public class StatusActivity extends SingleFragmentActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(getLayoutResId());
    }

    @Override
    protected Fragment createFragment() {
        return StatusFragment.newInstance();
    }
}
