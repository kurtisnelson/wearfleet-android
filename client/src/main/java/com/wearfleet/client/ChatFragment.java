package com.wearfleet.client;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;

import com.wearfleet.core.events.ChatEvent;

import de.greenrobot.event.EventBus;

public class ChatFragment extends Fragment {
    private EditText messageField;
    private Button messageButton;
    private ListView messageList;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.fragment_chat, container, false);
        messageField = (EditText) v.findViewById(R.id.messageText);
        messageButton = (Button) v.findViewById(R.id.messageSend);
        messageList = (ListView) v.findViewById(R.id.messageList);

        messageButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String message = messageField.getText().toString();
                messageField.setText("");
                EventBus.getDefault().post(new ChatEvent(message));
            }
        });
        return v;
    }

    public static ChatFragment newInstance() {
        Bundle args = new Bundle();
        ChatFragment fragment = new ChatFragment();
        fragment.setArguments(args);
        return fragment;
    }
}
