package com.ozhd.kadrov.employeesupport.ui.requests;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;

import com.google.android.material.tabs.TabLayoutMediator;
import com.ozhd.kadrov.employeesupport.data.local.entity.RequestKindEntity;
import com.ozhd.kadrov.employeesupport.databinding.FragmentCreateRequestBinding;
import com.ozhd.kadrov.employeesupport.ui.viewmodel.CreateRequestViewModel;

import java.util.List;

/**
 * Подвкладки создаются динамически из активных {@link RequestKindEntity} в БД.
 */
public class CreateRequestFragment extends Fragment {

    public static CreateRequestFragment newInstance() {
        return new CreateRequestFragment();
    }

    private FragmentCreateRequestBinding binding;
    private TabLayoutMediator mediator;
    private CreateRequestViewModel viewModel;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        binding = FragmentCreateRequestBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        viewModel = new ViewModelProvider(this).get(CreateRequestViewModel.class);

        viewModel.getKinds().observe(getViewLifecycleOwner(), this::onKindsLoaded);
    }

    private void onKindsLoaded(@Nullable List<RequestKindEntity> kinds) {
        if (binding == null) {
            return;
        }
        if (kinds == null || kinds.isEmpty()) {
            binding.textKindsEmpty.setVisibility(View.VISIBLE);
            binding.tabLayout.setVisibility(View.GONE);
            binding.viewPager.setVisibility(View.GONE);
            if (mediator != null) {
                mediator.detach();
                mediator = null;
            }
            return;
        }
        binding.textKindsEmpty.setVisibility(View.GONE);
        binding.tabLayout.setVisibility(View.VISIBLE);
        binding.viewPager.setVisibility(View.VISIBLE);

        if (mediator != null) {
            mediator.detach();
            mediator = null;
        }
        DynamicCreatePagerAdapter adapter = new DynamicCreatePagerAdapter(this, kinds);
        binding.viewPager.setAdapter(adapter);
        int limit = Math.min(3, Math.max(1, kinds.size()));
        binding.viewPager.setOffscreenPageLimit(limit);
        mediator = new TabLayoutMediator(binding.tabLayout, binding.viewPager,
                (tab, position) -> tab.setText(kinds.get(position).name));
        mediator.attach();
    }

    @Override
    public void onDestroyView() {
        if (mediator != null) {
            mediator.detach();
            mediator = null;
        }
        binding = null;
        super.onDestroyView();
    }
}
