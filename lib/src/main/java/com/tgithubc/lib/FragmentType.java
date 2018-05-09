package com.tgithubc.lib;

import android.support.annotation.IntDef;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

/**
 * Created by tc :)
 */
@Retention(RetentionPolicy.SOURCE)
@IntDef({
        FragmentType.TYPE_SUB,
        FragmentType.TYPE_FULL,
        FragmentType.TYPE_NONE
})
public @interface FragmentType {
    int TYPE_NONE = -1;
    int TYPE_SUB = 0;// 底部控制栏之上的fragment
    int TYPE_FULL = 1;// 全屏fragment
}
