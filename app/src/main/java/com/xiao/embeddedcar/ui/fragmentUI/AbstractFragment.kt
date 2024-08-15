package com.xiao.embeddedcar.ui.fragmentUI

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.viewbinding.ViewBinding
import com.xiao.embeddedcar.data.ViewModel.MainViewModel

/**
 * <VB : ViewBinding>表示这个类可以接受任何继承自ViewBinding的类型作为参数
 * <T>(...)中的(...)表示构造器接收这些参数并初始化
 * 该类继承自Fragment
 */
abstract class AbstractFragment<VB : ViewBinding, VM : ViewModel>(
    // ViewBinding
    private val inflater: (LayoutInflater, ViewGroup?, Boolean) -> VB,
    // 可空参数
    private val viewModelClass: Class<VM>?,
    // 是否需要MainViewModel
    private val mainViewModelTag: Boolean = false
) : Fragment() {

    /**
     * 注册当前Fragment的viewModel
     */
    private val viewModel by lazy { viewModelClass?.let { ViewModelProvider(this)[it] } }

    /**
     * 注册公共ViewModel
     * MainViewModel?表示该项可为空
     */
    val mainViewModel: MainViewModel? by lazy {
        if (mainViewModelTag)
            ViewModelProvider(requireActivity())[MainViewModel::class.java]
        else null
    }

    /**
     * Fragment视图创建
     */
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val binding = inflater(inflater, container, false)
        initFragment(binding, viewModel, savedInstanceState)
        init()
        observerDataStateUpdateAction()
        return binding.root
    }

    /**
     * Fragment属性获取
     */
    abstract fun initFragment(
        binding: VB,
        viewModel: VM?,
        savedInstanceState: Bundle?
    )

    /**
     * 控件动作初始化
     */
    abstract fun init()

    /**
     * 观察者数据状态更新活动
     */
    abstract fun observerDataStateUpdateAction()
}