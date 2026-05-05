package com.ozhd.kadrov.employeesupport.ui.requests;

import android.graphics.drawable.GradientDrawable;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.ozhd.kadrov.employeesupport.R;
import com.ozhd.kadrov.employeesupport.data.local.pojo.RequestWithAuthor;
import com.ozhd.kadrov.employeesupport.data.model.RequestStatus;
import com.ozhd.kadrov.employeesupport.databinding.FragmentRequestsBinding;
import com.ozhd.kadrov.employeesupport.databinding.ItemRequestBinding;
import com.ozhd.kadrov.employeesupport.ui.viewmodel.RequestsViewModel;

import java.util.ArrayList;
import java.util.List;

public class RequestsFragment extends Fragment {

    public static RequestsFragment newInstance() {
        return new RequestsFragment();
    }

    private FragmentRequestsBinding binding;
    private RequestsViewModel viewModel;
    private RequestAdapter adapter;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        binding = FragmentRequestsBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        viewModel = new ViewModelProvider(this).get(RequestsViewModel.class);
        adapter = new RequestAdapter(viewModel.isHr());
        binding.recyclerRequests.setLayoutManager(new LinearLayoutManager(requireContext()));
        binding.recyclerRequests.setAdapter(adapter);
        viewModel.getRequests().observe(getViewLifecycleOwner(), requests -> {
            adapter.setData(requests);
            boolean empty = requests == null || requests.isEmpty();
            binding.textEmpty.setVisibility(empty ? View.VISIBLE : View.GONE);
        });
    }

    private void showRejectDialog(@NonNull RequestWithAuthor item) {
        EditText input = new EditText(requireContext());
        input.setHint(R.string.request_reject_reason_hint);
        AlertDialog dialog = new MaterialAlertDialogBuilder(requireContext())
                .setTitle(R.string.request_reject_reason_title)
                .setView(input)
                .setPositiveButton(android.R.string.ok, null)
                .setNegativeButton(android.R.string.cancel, null)
                .show();
        dialog.getButton(AlertDialog.BUTTON_POSITIVE).setOnClickListener(v -> {
                    String reason = input.getText() != null ? input.getText().toString().trim() : "";
                    if (TextUtils.isEmpty(reason)) {
                        input.setError(getString(R.string.request_reject_reason_required));
                        return;
                    }
                    viewModel.reject(item.request.id, reason);
                    dialog.dismiss();
                });
    }

    private void showRejectReason(@NonNull String reason) {
        new MaterialAlertDialogBuilder(requireContext())
                .setTitle(R.string.request_reject_reason_title)
                .setMessage(reason)
                .setPositiveButton(android.R.string.ok, null)
                .show();
    }

    private static final class StatusUi {
        final String text;
        final int backgroundColorRes;
        final int textColorRes;

        StatusUi(String text, int backgroundColorRes, int textColorRes) {
            this.text = text;
            this.backgroundColorRes = backgroundColorRes;
            this.textColorRes = textColorRes;
        }
    }

    private StatusUi statusUi(@NonNull RequestStatus status) {
        if (status == RequestStatus.APPROVED) {
            return new StatusUi(
                    getString(R.string.request_status_approved),
                    R.color.status_approved_bg,
                    R.color.status_approved_text
            );
        }
        if (status == RequestStatus.REJECTED) {
            return new StatusUi(
                    getString(R.string.request_status_rejected),
                    R.color.status_rejected_bg,
                    R.color.status_rejected_text
            );
        }
        return new StatusUi(
                getString(R.string.request_status_pending),
                R.color.status_pending_bg,
                R.color.status_pending_text
        );
    }

    private class RequestAdapter extends RecyclerView.Adapter<RequestVH> {
        private final List<RequestWithAuthor> items = new ArrayList<>();
        private final boolean hr;

        RequestAdapter(boolean hr) {
            this.hr = hr;
        }

        void setData(@Nullable List<RequestWithAuthor> requests) {
            items.clear();
            if (requests != null) {
                items.addAll(requests);
            }
            notifyDataSetChanged();
        }

        @NonNull
        @Override
        public RequestVH onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            ItemRequestBinding itemBinding = ItemRequestBinding.inflate(
                    LayoutInflater.from(parent.getContext()), parent, false);
            return new RequestVH(itemBinding);
        }

        @Override
        public void onBindViewHolder(@NonNull RequestVH holder, int position) {
            RequestWithAuthor item = items.get(position);
            holder.binding.textTitle.setText(item.request.title);
            String author = item.authorFullName != null ? item.authorFullName : getString(R.string.request_author_unknown);
            holder.binding.textAuthor.setText(getString(R.string.request_author_format, author));

            StatusUi statusUi = statusUi(item.request.status);
            holder.binding.textStatus.setText(statusUi.text);
            holder.binding.textStatus.setTextColor(
                    ContextCompat.getColor(requireContext(), statusUi.textColorRes));
            GradientDrawable bg = new GradientDrawable();
            bg.setShape(GradientDrawable.RECTANGLE);
            bg.setCornerRadius(100f);
            bg.setColor(ContextCompat.getColor(requireContext(), statusUi.backgroundColorRes));
            holder.binding.textStatus.setBackground(bg);

            boolean rejected = item.request.status == RequestStatus.REJECTED
                    && !TextUtils.isEmpty(item.request.rejectionReason);
            holder.binding.textStatus.setOnClickListener(rejected
                    ? v -> showRejectReason(item.request.rejectionReason)
                    : null);

            boolean showActions = hr && item.request.status == RequestStatus.PENDING;
            holder.binding.layoutActions.setVisibility(showActions ? View.VISIBLE : View.GONE);
            holder.binding.buttonApprove.setOnClickListener(v -> viewModel.approve(item.request.id));
            holder.binding.buttonReject.setOnClickListener(v -> showRejectDialog(item));
        }

        @Override
        public int getItemCount() {
            return items.size();
        }
    }

    private static class RequestVH extends RecyclerView.ViewHolder {
        final ItemRequestBinding binding;

        RequestVH(ItemRequestBinding binding) {
            super(binding.getRoot());
            this.binding = binding;
        }
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}
