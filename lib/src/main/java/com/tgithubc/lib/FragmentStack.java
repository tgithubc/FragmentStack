package com.tgithubc.lib;

import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.os.Build;
import android.support.annotation.NonNull;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.text.TextUtils;
import android.util.Log;
import android.util.Pair;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.ImageView;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.ListIterator;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * Created by tc :)
 */
public class FragmentStack implements IFragmentStack{

    private static final String TAG = "FragmentStack";
    private static final String SEPARATOR = "###";
    private LinkedList<Pair<String, Fragment>> mStack;
    private StartParameter mDefaultParameter;
    private FragmentManager mFragmentManager;
    private FragmentActivity mActivity;
    private int mContainerId;
    private AtomicInteger mTagAtomic = new AtomicInteger();
    private OnFragmentStackChangeListener mListener;

    private FragmentStack() {
    }

    private static class SingletonHolder {
        private static final FragmentStack INSTANCE = new FragmentStack();
    }

    public static FragmentStack getInstance() {
        return SingletonHolder.INSTANCE;
    }

    @Override
    public void bind(int containerId, FragmentActivity activity) {
        mContainerId = containerId;
        mActivity = activity;
        mFragmentManager = activity.getSupportFragmentManager();
        mStack = new LinkedList<>();
        mDefaultParameter = getDefaultParameter();
        if (activity instanceof OnFragmentStackChangeListener) {
            mListener = (OnFragmentStackChangeListener) activity;
        }
    }

    @NonNull
    private StartParameter getDefaultParameter() {
        return new StartParameter.Builder()
                .withEnterAnimation(0)
                .withPopCurrent(false)
                .withHideBottomLayer(true)
                .withStartMode(StartMode.STANDARD)
                .withShareViews(null)
                .build();
    }

    @Override
    public void unBind() {
        mStack.clear();
        mFragmentManager = null;
        mListener = null;
        mActivity = null;
    }

    @Override
    public void showFragment(Fragment fragment) {
        showFragment(fragment, mDefaultParameter);
    }

    @Override
    public void navigateToHome() {
        if (mStack.isEmpty()) {
            return;
        }
        FragmentTransaction transaction = mFragmentManager.beginTransaction();
        for (Pair<String, Fragment> pair : mStack) {
            transaction.remove(pair.second);
        }
        transaction.commitAllowingStateLoss();
        mStack.clear();
        mTagAtomic.set(0);
        if (mListener != null) {
            mListener.onShowMainLayer();
        }
    }


    @Override
    public void navigateToFragment(String tag, boolean includeTarget) {
        if (TextUtils.isEmpty(tag) || mStack.isEmpty()) {
            return;
        }

        List<Pair<String, Fragment>> findArrary = findTargetFragmentAndUpList(tag);
        if (findArrary == null || findArrary.isEmpty()) {
            return;
        }
        FragmentTransaction transaction = mFragmentManager.beginTransaction();
        Pair<String, Fragment> target = findArrary.get(findArrary.size() - 1);
        Fragment showFragment;
        for (Pair<String, Fragment> pair : findArrary) {
            if (pair == target) {
                continue;
            }
            transaction.remove(pair.second);
            mStack.remove(pair);
        }
        if (includeTarget) {
            int targetIndex = mStack.indexOf(target);
            if (targetIndex - 1 < 0) {
                transaction.remove(target.second).commitAllowingStateLoss();
                mStack.remove(target);
                if (mListener != null) {
                    mListener.onShowMainLayer();
                }
                return;
            } else {
                showFragment = mStack.get(targetIndex - 1).second;
                transaction.show(showFragment)
                        .remove(target.second)
                        .commitAllowingStateLoss();
                executeTransactionNow();
                mStack.remove(target);
                safeShowFragmentView(showFragment);
                showFragment.onResume();
            }
        } else {
            showFragment = target.second;
            transaction.show(showFragment).commitAllowingStateLoss();
            executeTransactionNow();
            safeShowFragmentView(showFragment);
            showFragment.onResume();
        }
        // 移除了n个之后（n>=1）
        if (mListener != null) {
            mListener.onPopFragment(getTopFragment());
        }
    }

    @Override
    public boolean pop() {
        if (!mStack.isEmpty()) {
            FragmentTransaction transaction = mFragmentManager.beginTransaction();
            if (mStack.size() == 1) {
                transaction.remove(getTopFragment()).commitAllowingStateLoss();
                mStack.removeLast();
                if (mListener != null) {
                    mListener.onShowMainLayer();
                }
                return true;
            } else {
                Fragment showFragment = mStack.get(mStack.size() - 2).second;
                Log.d(TAG, "close Fragment 【"
                        + getTopFragment().getClass().getName()
                        + "】，and show pre Fragment 【:"
                        + showFragment.getClass().getName()
                        +"】");
                transaction.show(showFragment)
                        .remove(getTopFragment())
                        .commitAllowingStateLoss();
                executeTransactionNow();
                mStack.removeLast();
                safeShowFragmentView(showFragment);
                showFragment.onResume();
                if (mListener != null) {
                    mListener.onPopFragment(getTopFragment());
                }
                return true;
            }
        } else {
            return false;
        }
    }

    @Override
    public Fragment getTopFragment() {
        if (mStack != null && !mStack.isEmpty()) {
            return mStack.getLast().second;
        } else {
            return null;
        }
    }

    @Override
    public Fragment getPreFragment() {
        if (mStack == null || mStack.isEmpty() || mStack.size() == 1) {
            // viewpager
            return null;
        } else {
            return mStack.get(mStack.size() - 2).second;
        }
    }

    public boolean isMainLayerShow() {
        return mStack.size() == 0;
    }

    @Override
    public Fragment findFragmentByTag(String tag) {
        for (int i = 0, size = mStack.size(); i < size; i++) {
            Pair<String, Fragment> pair = mStack.get(i);
            if (pair.first.equals(tag)) {
                return pair.second;
            }
        }
        return null;
    }

    @Override
    public int getFragmentCountByClazz(Class clazz) {
        int count = 0;
        for (int i = 0, size = mStack.size(); i < size; i++) {
            Pair<String, Fragment> pair = mStack.get(i);
            if (pair.second.getClass() == clazz) {
                count++;
            }
        }
        return count;
    }

    @Override
    public void showFragment(Fragment fragment, StartParameter parameter) {
        if (!(fragment instanceof INewIntent)) {
            throw new RuntimeException("fragment" + fragment + "should be implements IFragmentType");
        }
        if (parameter == null) {
            parameter = mDefaultParameter;
        }
        String tag;
        if (!TextUtils.isEmpty(parameter.tag)) {
            tag = parameter.tag;
        } else {
            tag = fragment.getClass().getName()
                    + SEPARATOR
                    + mTagAtomic.incrementAndGet();
        }
        if (mStack.isEmpty()) {
            openStandard(fragment, tag, parameter);
        } else {
            handlerStartMode(fragment, tag, parameter);
        }
        if (mListener != null) {
            mListener.onPushFragment(fragment);
        }
        Log.d(TAG, "show Fragment 【"
                + fragment.getClass().getName()
                + "】,StartParameter :"
                + parameter);
    }

    private void handlerStartMode(Fragment fragment, String tag, StartParameter parameter) {
        @StartMode
        int mode = parameter.startMode;
        switch (mode) {
            case StartMode.STANDARD:
                openStandard(fragment, tag, parameter);
                break;
            case StartMode.SINGLE_INSTANCE:
                openSingleInstance(fragment, tag, parameter);
                break;
            case StartMode.SINGLE_TOP:
                openSingleTop(fragment, tag, parameter);
                break;
            case StartMode.SINGLE_TASK:
                openSingleTask(fragment, tag, parameter);
                break;
        }
    }

    /**
     * SingleTop
     * 如果栈顶就是目标Fragment的实例触发其onNewInstance，可以处理预先携带的bundle数据
     * 如果栈顶不是目标Fragment，重新打开一个实例
     *
     * @param fragment  目标Fragment
     * @param tag       目标Fragment tag
     * @param parameter 跳转参数
     */
    private void openSingleTop(Fragment fragment, String tag, StartParameter parameter) {
        if (!tag.startsWith(getRealTag(mStack.getLast().first))) {
            openStandard(fragment, tag, parameter);
        } else {
            // 触发onNewIntent，自己带着刷新参数过去
            Fragment target = mStack.getLast().second;
            ((INewIntent) target).onNewIntent(parameter.bundle);
        }
    }

    /**
     * SingleTask
     * 倒序寻找栈中有没有目标Fragment的实例，如果有，将其上面全部弹出，触发其onNewInstance，
     * 可以处理预先携带的bundle数据
     *
     * @param fragment  目标Fragment
     * @param tag       目标Fragment tag
     * @param parameter 跳转参数
     */
    private void openSingleTask(Fragment fragment, String tag, StartParameter parameter) {
        ListIterator<Pair<String, Fragment>> it = mStack.listIterator(mStack.size());
        List<Pair<String, Fragment>> upList = new ArrayList<>();
        Fragment target = null;
        while (it.hasPrevious()) {
            Pair<String, Fragment> element = it.previous();
            if (tag.startsWith(getRealTag(element.first))) {
                target = element.second;
                break;
            } else {
                upList.add(element);
            }
        }

        if (target != null) {
            FragmentTransaction transaction = mFragmentManager.beginTransaction();
            for (Pair<String, Fragment> pair : upList) {
                transaction.remove(pair.second);
                mStack.remove(pair);
            }
            transaction.show(target).commitAllowingStateLoss();
            executeTransactionNow();
            ((INewIntent) target).onNewIntent(parameter.bundle);
            safeShowFragmentView(target);
            target.onResume();
            return;
        }

        openStandard(fragment, tag, parameter);
    }

    /**
     * SingleInstance
     * 模拟单例实例，比方播放页
     *
     * @param fragment  目标Fragment
     * @param tag       目标Fragment tag
     * @param parameter 跳转参数
     */
    private void openSingleInstance(Fragment fragment, String tag, StartParameter parameter) {
        FragmentTransaction transaction = mFragmentManager.beginTransaction();
        handlerAnimation(parameter, transaction);
        transaction.setTransition(FragmentTransaction.TRANSIT_NONE)
                .add(mContainerId, fragment, tag);
        Fragment preFragment = mStack.getLast().second;
        if (!TextUtils.isEmpty(parameter.popEndTag)) {
            List<Pair<String, Fragment>> findArrary
                    = findTargetFragmentAndUpList(parameter.popEndTag);
            if (findArrary == null || findArrary.isEmpty()) {
                return;
            }
            Pair<String, Fragment> target = findArrary.get(findArrary.size() - 1);
            for (Pair<String, Fragment> pair : findArrary) {
                if (!parameter.isIncludePopEnd && pair == target) {
                    continue;
                }
                transaction.remove(pair.second);
                mStack.remove(pair);
            }
        } else if (parameter.isPopCurrent) {
            transaction.remove(preFragment);
            mStack.remove(mStack.getLast());
        } else {
            // isHideBottomLayer && 没入场动画 才能隐藏下层
            if (parameter.isHideBottomLayer && parameter.enterAnimation == 0) {
                transaction.hide(preFragment);
            }
            // 只要前面不pop掉pre就得触发其onPause
            preFragment.onPause();
        }

        // 把存在的实例都干掉,如果有的话
        ListIterator<Pair<String, Fragment>> it = mStack.listIterator();
        while (it.hasNext()) {
            Pair<String, Fragment> element = it.next();
            if (tag.startsWith(getRealTag(element.first))) {
                transaction.remove(element.second);
                it.remove();
            }
        }

        transaction.commitAllowingStateLoss();
        mStack.add(new Pair<>(tag, fragment));
    }

    /**
     * 标准启动
     *
     * @param fragment  目标Fragment
     * @param tag       目标Fragment tag
     * @param parameter 跳转参数
     */
    private void openStandard(Fragment fragment, String tag, StartParameter parameter) {
        FragmentTransaction transaction = mFragmentManager.beginTransaction();
        handlerAnimation(parameter, transaction);
        transaction.setTransition(FragmentTransaction.TRANSIT_NONE)
                .add(mContainerId, fragment, tag);
        if (mStack.isEmpty()) {
            if (mListener != null) {
                mListener.onHideMainLayer(parameter.isHideBottomLayer
                        && parameter.enterAnimation == 0);
            }
            transaction.commitAllowingStateLoss();
            mStack.add(new Pair<>(tag, fragment));
            return;
        }
        Fragment preFragment = mStack.getLast().second;
        if (!TextUtils.isEmpty(parameter.popEndTag)) {
            List<Pair<String, Fragment>> findArrary
                    = findTargetFragmentAndUpList(parameter.popEndTag);
            if (findArrary == null || findArrary.isEmpty()) {
                return;
            }
            Pair<String, Fragment> target = findArrary.get(findArrary.size() - 1);
            for (Pair<String, Fragment> pair : findArrary) {
                if (!parameter.isIncludePopEnd && pair == target) {
                    continue;
                }
                transaction.remove(pair.second);
                mStack.remove(pair);
            }
        } else if (parameter.isPopCurrent) {
            transaction.remove(preFragment);
            mStack.remove(mStack.getLast());
        } else {
            // isHideBottomLayer && 没入场动画 才能隐藏下层
            if (parameter.isHideBottomLayer && parameter.enterAnimation == 0) {
                transaction.hide(preFragment);
            }
            // 只要前面不pop掉pre就得触发其onPause
            preFragment.onPause();
        }

        transaction.commitAllowingStateLoss();
        mStack.add(new Pair<>(tag, fragment));
    }

    /**
     * 处理动画和共享元素
     * <p>
     * example:
     * fragment.setSharedElementEnterTransition(new FragmentTransition());
     * fragment.setEnterTransition(new Fade(Fade.IN));
     * fragment.setExitTransition(new Fade(Fade.OUT));
     * fragment.setSharedElementReturnTransition(new FragmentTransition());
     *
     * @param parameter
     * @param transaction
     */
    private void handlerAnimation(StartParameter parameter, FragmentTransaction transaction) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            List<Map.Entry<View, String>> shareView = parameter.shareViews;
            if (shareView != null && !shareView.isEmpty()) {
                for (int i = 0, size = shareView.size(); i < size; i++) {
                    Map.Entry<View, String> item = shareView.get(i);
                    transaction.addSharedElement(item.getKey(), item.getValue());
                }
            }
        }
        transaction.setCustomAnimations(parameter.enterAnimation, parameter.exitAnimation);
    }

    /**
     * Debug 方法：
     */
    public void showStackView() {
        if (mActivity == null) {
            return;
        }
        View root = mActivity.findViewById(android.R.id.content);
        if (root instanceof FrameLayout) {
            FrameLayout content = (FrameLayout) root;
            final ImageView stackView = new ImageView(mActivity);
            stackView.setImageResource(R.mipmap.fragmentation_ic_stack);
            FrameLayout.LayoutParams params = new FrameLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
            params.gravity = Gravity.END;
            final int dp18 = (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 18, mActivity.getResources().getDisplayMetrics());
            params.topMargin = dp18 * 7;
            params.rightMargin = dp18;
            stackView.setLayoutParams(params);
            content.addView(stackView);
            stackView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    DebugHierarchyViewContainer container = new DebugHierarchyViewContainer(mActivity);
                    container.bindFragmentRecords(getStack());
                    container.setLayoutParams(new ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT,
                            ViewGroup.LayoutParams.MATCH_PARENT));
                    new AlertDialog.Builder(mActivity)
                            .setTitle("栈视图")
                            .setView(container)
                            .setPositiveButton("关闭", null)
                            .setCancelable(true)
                            .show();
                }
            });
        }
    }

    /**
     * Debug 方法：
     * 获取fragment栈队列
     */
    public List<DebugFragmentStack> getStack() {
        if (mStack == null || mStack.size() == 0) {
            return null;
        }
        List<DebugFragmentStack> fragmentRecordList = new ArrayList<>();
        for (Pair<String, Fragment> pair : mStack) {
            fragmentRecordList.add(new DebugFragmentStack(pair.first,
                    getChildFragmentRecords(pair.second)));
        }
        return fragmentRecordList;
    }

    /**
     * Debug 方法：
     * 获取子fragment栈队列
     */
    @SuppressLint("RestrictedApi")
    private List<DebugFragmentStack> getChildFragmentRecords(Fragment parentFragment) {
        List<DebugFragmentStack> fragmentRecords = new ArrayList<>();
        List<Fragment> fragmentList = parentFragment.getChildFragmentManager().getFragments();
        if (fragmentList == null || fragmentList.isEmpty()) {
            return null;
        }
        for (int i = fragmentList.size() - 1; i >= 0; i--) {
            Fragment fragment = fragmentList.get(i);
            if (fragment != null) {
                fragmentRecords.add(new DebugFragmentStack(fragment.getClass().getSimpleName(),
                        getChildFragmentRecords(fragment)));
            }
        }
        return fragmentRecords;
    }

    /**
     * 确保无论左滑或者back操作，当前的露出fragment必须得显示出来view
     *
     * @param showFragment
     */
    private void safeShowFragmentView(Fragment showFragment) {
        View view = showFragment.getView();
        if (view != null && view.getVisibility() != View.VISIBLE) {
            view.setVisibility(View.VISIBLE);
        }
    }

    private String getRealTag(String tag) {
        if (!tag.contains(SEPARATOR)) {
            return tag;
        }
        String[] strings = tag.split(SEPARATOR);
        return strings[0];
    }

    /**
     * 兼容sdk24以下，不使用commitNow，用这个立即提交事务
     */
    private void executeTransactionNow() {
        try {
            mFragmentManager.executePendingTransactions();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * 找到目标fragment和其上所有framgent的集合
     *
     * @param tag 目标fragment tag
     */
    private List<Pair<String, Fragment>> findTargetFragmentAndUpList(String tag) {
        ListIterator<Pair<String, Fragment>> accurateIt = mStack.listIterator(mStack.size());
        List<Pair<String, Fragment>> upList = new ArrayList<>();
        Pair<String, Fragment> target = null;
        while (accurateIt.hasPrevious()) {
            Pair<String, Fragment> pair = accurateIt.previous();
            // 精准查找优先
            if (tag.equals(pair.first)) {
                target = pair;
                break;
            } else {
                upList.add(pair);
            }
        }
        if (target == null) {
            upList.clear();
            ListIterator<Pair<String, Fragment>> fuzzyIt = mStack.listIterator(mStack.size());
            while (fuzzyIt.hasPrevious()) {
                Pair<String, Fragment> pair = fuzzyIt.previous();
                // 再寻找前部匹配
                if (pair.first.startsWith(tag)) {
                    target = pair;
                    break;
                } else {
                    upList.add(pair);
                }
            }
        }
        if (target != null) {
            // 把目标加到最后一个
            upList.add(target);
            return upList;
        }
        return null;
    }
}
