package com.tgithubc.lib;

import android.support.annotation.IntDef;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

/**
 * Created by tc :)
 */
@Retention(RetentionPolicy.SOURCE)
@IntDef({
        StartMode.STANDARD,
        StartMode.SINGLE_TOP,
        StartMode.SINGLE_INSTANCE,
        StartMode.SINGLE_TASK
})
@interface StartMode {
    int STANDARD = 0;
    int SINGLE_TOP = 1;
    int SINGLE_INSTANCE = 2;
    int SINGLE_TASK = 3;
}
