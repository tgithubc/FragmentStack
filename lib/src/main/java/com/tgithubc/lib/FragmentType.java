package com.tgithubc.lib;

import android.support.annotation.IntDef;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

/**
 *  type由业务具体类型决定，用来区分fragment 定制页面
 * Created by tc :)
 */
@Retention(RetentionPolicy.SOURCE)
@IntDef({
        FragmentType.TYPE_NONE
        // sample code
        //FragmentType.TYPE_FULL,
        //FragmentType.TYPE_NONE
})
public @interface FragmentType {
    int TYPE_NONE = -1;
    // sample code
    //int TYPE_SUB = 0;// 底部控制栏之上的fragment
    //int TYPE_FULL = 1;// 全屏fragment
}
