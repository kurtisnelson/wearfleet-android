package com.wearfleet.glass;

import android.content.Intent;
import android.os.Bundle;
import android.speech.RecognizerIntent;
import android.support.v4.app.FragmentActivity;

import com.wearfleet.core.events.ChatEvent;

import java.util.List;

import de.greenrobot.event.EventBus;

public class ChatActivity extends FragmentActivity{
    private static final int SPEECH_MESSAGE = 0;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Intent intent = new Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH);
        startActivityForResult(intent, SPEECH_MESSAGE);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if(requestCode == SPEECH_MESSAGE && resultCode == RESULT_OK){
            List<String> results = data.getStringArrayListExtra(
                    RecognizerIntent.EXTRA_RESULTS);
            String spokenText = results.get(0);
            EventBus.getDefault().post(new ChatEvent(spokenText));
            finish();
        }
        super.onActivityResult(requestCode, resultCode, data);
    }
}
