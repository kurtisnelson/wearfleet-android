package com.wearfleet.client;

import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;

import com.wearfleet.core.events.ChatEvent;

import de.greenrobot.event.EventBus;

public class ChatFragment extends Fragment {
    private EditText messageField;
    private Button messageButton;
    private ListView messageList;
    private ArrayAdapter<ChatEvent> messageAdapter;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public void onResume() {
        super.onResume();
        getActivity().sendBroadcast(new Intent(FleetService.CLEAR_CHATS_FILTER));
        EventBus.getDefault().register(this);
    }

    @Override
    public void onPause() {
        super.onPause();
        EventBus.getDefault().unregister(this);
    }

    public void onEventMainThread(ChatEvent e){
        messageAdapter.add(e);
        getActivity().sendBroadcast(new Intent(FleetService.CLEAR_CHATS_FILTER));
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.fragment_chat, container, false);
        messageField = (EditText) v.findViewById(R.id.messageText);
        messageButton = (Button) v.findViewById(R.id.messageSend);
        messageList = (ListView) v.findViewById(R.id.messageList);

        messageAdapter = new ArrayAdapter<ChatEvent>(getActivity(), android.R.layout.simple_list_item_1);
        messageAdapter.setNotifyOnChange(true);
        messageList.setAdapter(messageAdapter);

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
