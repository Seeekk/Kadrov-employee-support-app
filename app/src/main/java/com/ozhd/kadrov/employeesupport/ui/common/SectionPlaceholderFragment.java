package com.ozhd.kadrov.employeesupport.ui.common;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.ozhd.kadrov.employeesupport.databinding.FragmentPlaceholderBinding;

/**
 * Заглушка раздела с заголовком — для вкладок, где пока нет отдельной бизнес-логики.
 */
public class SectionPlaceholderFragment extends Fragment {

    private static final String ARG_TITLE = "title";

    public static SectionPlaceholderFragment newInstance(@NonNull String title) {
        SectionPlaceholderFragment f = new SectionPlaceholderFragment();
        Bundle b = new Bundle();
        b.putString(ARG_TITLE, title);
        f.setArguments(b);
        return f;
    }

    private FragmentPlaceholderBinding binding;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        binding = FragmentPlaceholderBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        Bundle args = getArguments();
        String title = args != null ? args.getString(ARG_TITLE) : "";
        TextView tv = binding.textSection;
        tv.setText(title != null ? title : "");
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}
