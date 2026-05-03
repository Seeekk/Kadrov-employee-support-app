package com.ozhd.kadrov.employeesupport.ui.chat;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.DiffUtil;
import androidx.recyclerview.widget.ListAdapter;
import androidx.recyclerview.widget.RecyclerView;

import com.ozhd.kadrov.employeesupport.R;
import com.ozhd.kadrov.employeesupport.data.local.pojo.MessageWithSender;

import java.util.Objects;

/**
 * Чат: два ViewType — «мои» сообщения (другой фон/выравнивание) и чужие; имя через JOIN users.
 */
public class ChatMessageAdapter extends ListAdapter<MessageWithSender, RecyclerView.ViewHolder> {

    private static final int TYPE_SELF = 1;
    private static final int TYPE_OTHER = 2;

    private final String currentUserId;

    private static final DiffUtil.ItemCallback<MessageWithSender> DIFF =
            new DiffUtil.ItemCallback<MessageWithSender>() {
                @Override
                public boolean areItemsTheSame(@NonNull MessageWithSender oldItem,
                                               @NonNull MessageWithSender newItem) {
                    return oldItem.message.id.equals(newItem.message.id);
                }

                @Override
                public boolean areContentsTheSame(@NonNull MessageWithSender oldItem,
                                                    @NonNull MessageWithSender newItem) {
                    return oldItem.message.text.equals(newItem.message.text)
                            && oldItem.message.timestamp == newItem.message.timestamp
                            && Objects.equals(oldItem.senderFullName, newItem.senderFullName)
                            && oldItem.message.senderId.equals(newItem.message.senderId);
                }
            };

    public ChatMessageAdapter(@NonNull String currentUserId) {
        super(DIFF);
        this.currentUserId = currentUserId;
    }

    @Override
    public int getItemViewType(int position) {
        MessageWithSender row = getItem(position);
        boolean self = currentUserId != null && currentUserId.equals(row.message.senderId);
        return self ? TYPE_SELF : TYPE_OTHER;
    }

    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        LayoutInflater inflater = LayoutInflater.from(parent.getContext());
        if (viewType == TYPE_SELF) {
            View v = inflater.inflate(R.layout.item_chat_self, parent, false);
            return new SelfVH(v);
        }
        View v = inflater.inflate(R.layout.item_chat_other, parent, false);
        return new OtherVH(v);
    }

    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position) {
        MessageWithSender row = getItem(position);
        if (holder instanceof SelfVH) {
            ((SelfVH) holder).bind(row);
        } else if (holder instanceof OtherVH) {
            ((OtherVH) holder).bind(row);
        }
    }

    static class SelfVH extends RecyclerView.ViewHolder {
        private final TextView sender;
        private final TextView body;

        SelfVH(@NonNull View itemView) {
            super(itemView);
            sender = itemView.findViewById(R.id.sender_label);
            body = itemView.findViewById(R.id.message_body);
        }

        void bind(MessageWithSender row) {
            String name = row.senderFullName != null && !row.senderFullName.isEmpty()
                    ? row.senderFullName
                    : itemView.getContext().getString(R.string.chat_you);
            sender.setText(name);
            body.setText(row.message.text);
        }
    }

    static class OtherVH extends RecyclerView.ViewHolder {
        private final TextView sender;
        private final TextView body;

        OtherVH(@NonNull View itemView) {
            super(itemView);
            sender = itemView.findViewById(R.id.sender_label);
            body = itemView.findViewById(R.id.message_body);
        }

        void bind(MessageWithSender row) {
            String name = row.senderFullName != null && !row.senderFullName.isEmpty()
                    ? row.senderFullName
                    : itemView.getContext().getString(R.string.chat_unknown_sender);
            sender.setText(name);
            body.setText(row.message.text);
        }
    }
}
