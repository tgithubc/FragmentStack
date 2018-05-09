package com.tgithubc.lib;

import android.os.Bundle;
import android.view.View;

import java.util.List;
import java.util.Map;

/**
 * 启动需要的参数，无关启动的不要乱加
 * <p>
 * fragment必须得传
 * type封到方法里
 * 因为要支持无限打开,tag不再建议暴露
 * 剩余下面的都是可选参数随意组合
 * <p>
 * Created by tc :)
 */

class StartParameter {

    // tag一般不建议自己来设定，工具内部会自己维护
    // 如果想精确定位你打开的fragment,tag最好设置成绝对路径类名，也就是class.getName()
    String tag;
    // 启动模式
    int startMode;
    // 进入动画
    int enterAnimation;
    // 退出动画
    int exitAnimation;
    // 是否隐藏下一层，有时候透明的fragment不需要隐藏
    boolean isHideBottomLayer = true;
    // 打开的时候是否同时关掉当前 open with pop
    boolean isPopCurrent = false;
    // 打开的时候同时关闭其下fragment，直到某个tag的fragment
    String popEndTag;
    // 打开的时候同时关闭其下fragment，直到某个tag的fragment并且可选是否包含这个目标fragment
    boolean isIncludePopEnd;
    // 共享元素动画需要的
    List<Map.Entry<View, String>> shareViews;
    // 以singleTop，singleTask启动的时候可能回需要携带些新数据，放这里就行
    Bundle bundle;

    private StartParameter(Builder builder) {
        this.tag = builder.tag;
        this.enterAnimation = builder.enterAnimation;
        this.exitAnimation = builder.exitAnimation;
        this.isHideBottomLayer = builder.isHideBottomLayer;
        this.isPopCurrent = builder.isPopCurrent;
        this.popEndTag = builder.popEndTag;
        this.isIncludePopEnd = builder.isIncludePopEnd;
        this.shareViews = builder.shareViews;
        this.startMode = builder.startMode;
        this.bundle = builder.bundle;
    }

    static final class Builder {

        private String tag;
        private int enterAnimation;
        private int exitAnimation;
        private boolean isHideBottomLayer = true;
        private boolean isIncludePopEnd;
        private boolean isPopCurrent;
        private String popEndTag;
        private List<Map.Entry<View, String>> shareViews;
        private int startMode;
        private Bundle bundle;

        public Builder withTag(String tag) {
            this.tag = tag;
            return this;
        }

        public Builder withEnterAnimation(int animation) {
            this.enterAnimation = animation;
            return this;
        }

        public Builder withExitAnimation(int animation) {
            this.exitAnimation = animation;
            return this;
        }

        public Builder withHideBottomLayer(boolean isHideBottomLayer) {
            this.isHideBottomLayer = isHideBottomLayer;
            return this;
        }

        public Builder withPopCurrent(boolean isPopCurrent) {
            this.isPopCurrent = isPopCurrent;
            return this;
        }

        public Builder withPopEndTag(String tag, boolean include) {
            this.popEndTag = tag;
            this.isIncludePopEnd = include;
            return this;
        }

        public Builder withNewBundle(Bundle bundle) {
            this.bundle = bundle;
            return this;
        }

        public Builder withShareViews(List<Map.Entry<View, String>> shareViews) {
            this.shareViews = shareViews;
            return this;
        }

        public Builder withStartMode(@StartMode int startMode) {
            this.startMode = startMode;
            return this;
        }

        public StartParameter build() {
            return new StartParameter(this);
        }
    }

    @Override
    public String toString() {
        return "StartParameter{" +
                "tag='" + tag + '\'' +
                ", startMode=" + startMode +
                ", enterAnimation=" + enterAnimation +
                ", exitAnimation=" + exitAnimation +
                ", isHideBottomLayer=" + isHideBottomLayer +
                ", isPopCurrent=" + isPopCurrent +
                ", popEndTag='" + popEndTag + '\'' +
                ", isIncludePopEnd=" + isIncludePopEnd +
                ", shareViews=" + shareViews +
                ", bundle=" + bundle +
                '}';
    }
}
