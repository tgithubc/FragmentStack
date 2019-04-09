package com.tgithubc.lib;


import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;

/**
 * Created by tc :)
 */
public interface IFragmentStack {

    void bind(int containerId, FragmentActivity activity);

    void unBind();

    /**
     * show
     *
     * @param fragment
     */
    void showFragment(Fragment fragment);

    /**
     * show
     *
     * @param fragment  目标Fragment
     * @param parameter 跳转参数
     */
    void showFragment(Fragment fragment, StartParameter parameter);

    /**
     * 正常按次序pop出去一个栈顶fragment
     */
    boolean pop();

    /**
     * 跳转回主页面
     */
    void navigateToHome();

    /**
     * 跳转到某目标fragment，关闭其上所有
     *
     * @param tag           指定到tag
     * @param includeTarget 是否包含关闭目标fragment
     *                      true同时关闭目标fragment，false保留目标fragment
     */
    void navigateToFragment(String tag, boolean includeTarget);

    /**
     * get top Fragment
     */
    Fragment getTopFragment();

    /**
     * get pre fragment
     */
    Fragment getPreFragment();

    /**
     * 是否是在主页
     */
    boolean isMainLayerShow();

    /**
     * 精准查找你指定的tag的fragment
     */
    Fragment findFragmentByTag(String tag);

    /**
     * 根据Fragment的Class来判断在栈中已经存在多少实例
     */
    int getFragmentCountByClazz(Class clazz);
}
