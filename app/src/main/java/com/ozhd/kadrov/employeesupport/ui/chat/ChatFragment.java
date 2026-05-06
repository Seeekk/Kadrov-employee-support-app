package com.ozhd.kadrov.employeesupport.ui.chat;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.google.firebase.firestore.ListenerRegistration;
import com.ozhd.kadrov.employeesupport.core.AppConstants;
import com.ozhd.kadrov.employeesupport.core.SessionManager;
import com.ozhd.kadrov.employeesupport.data.local.pojo.MessageWithSender;
import com.ozhd.kadrov.employeesupport.data.repository.ChatRepository;
import com.ozhd.kadrov.employeesupport.databinding.FragmentChatBinding;

import java.util.Collections;
import java.util.List;

/**
 * Чат работает в гибридном режиме: Room + Firestore.
 */
public class ChatFragment extends Fragment {

    public static ChatFragment newInstance() {
        return new ChatFragment();
    }

    private FragmentChatBinding binding;
    private ChatRepository chatRepository;
    private ChatMessageAdapter adapter;
    private ListenerRegistration firestoreRegistration;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        binding = FragmentChatBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        chatRepository = new ChatRepository(requireContext());
        String selfId = new SessionManager(requireContext()).getUserIdOrEmpty();
        adapter = new ChatMessageAdapter(selfId);
        binding.recyclerMessages.setLayoutManager(new LinearLayoutManager(requireContext()));
        binding.recyclerMessages.setAdapter(adapter);

        String chatId = AppConstants.CHAT_GLOBAL_ID;
        chatRepository.ensureLocalChat(chatId, "Общий чат", true);

        chatRepository.observeMessagesWithSender(chatId).observe(getViewLifecycleOwner(), rows -> {
            FragmentChatBinding b = binding;
            if (b == null) {
                return;
            }
            List<MessageWithSender> list = rows != null ? rows : Collections.emptyList();
            adapter.submitList(list);
            if (!list.isEmpty()) {
                b.recyclerMessages.scrollToPosition(list.size() - 1);
            }
        });

        binding.buttonSend.setOnClickListener(v -> send());
    }

    @Override
    public void onStart() {
        super.onStart();
        firestoreRegistration = chatRepository.attachRealtimeSync(AppConstants.CHAT_GLOBAL_ID);
    }

    @Override
    public void onStop() {
        super.onStop();
        if (firestoreRegistration != null) {
            firestoreRegistration.remove();
            firestoreRegistration = null;
        }
    }

    private void send() {
        FragmentChatBinding b = binding;
        if (b == null) {
            return;
        }
        CharSequence text = b.inputMessage.getText();
        if (text == null || text.toString().trim().isEmpty()) {
            return;
        }
        SessionManager sm = new SessionManager(requireContext());
        String userId = sm.getUserIdOrEmpty();
        if (userId.isEmpty()) {
            return;
        }
        chatRepository.sendMessage(AppConstants.CHAT_GLOBAL_ID, userId, text.toString().trim());
        b.inputMessage.setText("");
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}
