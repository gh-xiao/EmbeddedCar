package com.xiao.embeddedcar.FragmentUI

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.viewbinding.ViewBinding
import com.xiao.embeddedcar.ViewModel.MainViewModel

/**
 * <VB : ViewBinding>表示这个类可以接受任何继承自ViewBinding的类型作为参数
 * <T>(...)中的(...)表示构造器接收这些参数并初始化
 * 该类继承自Fragment
 */
abstract class AbstractFragment<VB : ViewBinding, VM : ViewModel>(
    private val inflater: (LayoutInflater, ViewGroup?, Boolean) -> VB,
    private val viewModelClass: Class<VM>?,
    private val mainViewModelTag: Boolean = false
) : Fragment() {

    /**
     * 注册当前Fragment的viewModel
     */
    private val viewModel by lazy {
        val viewModelProvider = ViewModelProvider(this)
        viewModelClass?.let {
            viewModelProvider[it]
        }
    }

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
     * 初始化Fragment
     */
    abstract fun initFragment(
        binding: VB,
        viewModel: VM?,
        savedInstanceState: Bundle?
    )

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
     * 控件动作初始化
     */
    abstract fun init()

    /**
     * 观察者数据状态更新活动
     */
    abstract fun observerDataStateUpdateAction()
}