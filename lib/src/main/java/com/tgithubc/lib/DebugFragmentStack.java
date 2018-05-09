package com.tgithubc.lib;

import java.util.List;

/**
 * Created by tc :)
 */
class DebugFragmentStack {

    String mFragmentTag;
    List<DebugFragmentStack> mChildFragmentStack;

    DebugFragmentStack(String tag, List<DebugFragmentStack> childFragmentStack) {
        this.mFragmentTag = tag;
        this.mChildFragmentStack = childFragmentStack;
    }
}
