package com.xiao.embeddedcar.ui.fragmentUI;

import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.xiao.embeddedcar.data.ViewModel.ConnectViewModel;
import com.xiao.embeddedcar.databinding.FragmentTestBinding;

public class testFragment extends AbstractFragment<FragmentTestBinding, ConnectViewModel> {

    public testFragment() {
        super(FragmentTestBinding::inflate, ConnectViewModel.class, false);
    }

    @Override
    public void initFragment(@NonNull FragmentTestBinding binding, @Nullable ConnectViewModel viewModel, @Nullable Bundle savedInstanceState) {
    }

    @Override
    public void init() {
    }

    @Override
    public void observerDataStateUpdateAction() {
    }

}
