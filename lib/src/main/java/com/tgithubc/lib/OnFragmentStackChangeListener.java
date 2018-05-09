package com.tgithubc.lib;

import android.support.v4.app.Fragment;

/**
 * 监听fragment栈中增删事件
 * Created by tc :)
 */
public interface OnFragmentStackChangeListener {

    /**
     * 栈顶添加了一个Fragment
     *
     * @param top 添加的fragment
     */
    void onPushFragment(Fragment top);

    /**
     * 栈顶移除了一个Fragment
     *
     * @param top 移除之后新露出的fragment
     */
    void onPopFragment(Fragment top);

    /**
     * 移除到底了，露出viewpager了
     */
    void onShowMainLayer();

    /**
     * 添加首个，隐藏viewpager
     * 有动画入场的时候不能隐藏
     */
    void onHideMainLayer(boolean isHide);
}
