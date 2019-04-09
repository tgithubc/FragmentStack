package com.tgithubc.lib;

import android.os.Bundle;

/**
 * type由业务具体类型决定，用来区分fragment 定制页面
 * Created by tc :)
 */
public interface IFragmentType {

    @FragmentType
    int getFragmentType();

    void onNewIntent(Bundle bundle);
}
