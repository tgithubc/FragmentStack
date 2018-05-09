package com.tgithubc.lib;

import android.os.Bundle;

/**
 * Created by tc :)
 */
public interface IFragmentType {

    void setFragmentType(@FragmentType int type);

    @FragmentType
    int getFragmentType();

    void onNewIntent(Bundle bundle);
}
