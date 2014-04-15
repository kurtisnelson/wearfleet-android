package com.wearfleet.client;

import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.Fragment;

import com.wearfleet.core.SingleFragmentActivity;

public class ChatActivity extends SingleFragmentActivity {
    @Override
    protected void onResume() {
        super.onResume();
        sendBroadcast(new Intent(FleetService.CLEAR_CHATS_FILTER));
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(getLayoutResId());
    }

    @Override
    protected Fragment createFragment() {
        return ChatFragment.newInstance();
    }
}
