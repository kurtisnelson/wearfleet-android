package com.wearfleet.client;

import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.Fragment;

import com.wearfleet.core.ConfigActivity;
import com.wearfleet.core.SingleFragmentActivity;
import com.wearfleet.core.utils.Config;

public class StatusActivity extends SingleFragmentActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(getLayoutResId());
        if(!new Config(this).isAuthenticated()){
            startActivity(new Intent(this, ConfigActivity.class));
        }
    }

    @Override
    protected Fragment createFragment() {
        return StatusFragment.newInstance();
    }
}
