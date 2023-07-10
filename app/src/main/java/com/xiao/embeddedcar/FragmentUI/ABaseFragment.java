package com.xiao.embeddedcar.FragmentUI;

import androidx.fragment.app.Fragment;

@Deprecated
public abstract class ABaseFragment extends Fragment {
    /**
     * 控件动作初始化
     */
    abstract void init();

    /**
     * 观察者数据状态更新活动
     */
    abstract void observerDataStateUpdateAction();
}
