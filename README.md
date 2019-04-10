# FragmentStack
轻量，低侵入，优化了过渡绘制，模拟Activity四种启动模式的单Activity多Fragment管理
# 使用
step1:  
 
```groovy
add maven { url "https://jitpack.io" } to

allprojects {
    repositories {
    }
}   

add 
compile 'com.tgithubc:fragment_stack:1.0'
```
    
step2:    

你的BaseFragment实现INewIntent，实现onNewIntent(Bundle bundle);
    

step3:  
在合适的位置绑定Activity  
```java
FragmentStack.getInstance().bind(int containerId, FragmentActivity activity);
```      

简单使用:  
```java
FragmentStack.getInstance().showFragment(Fragment fragment);
FragmentStack.getInstance().showFragment(Fragment fragment, StartParameter parameter);

其他方法参见IFragmentStack接口

```  
自定义配置:
```java
// 自定义配置，参见：StartParameter类  
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
```
关于启动模式：  
模拟Activity四种启动模式
参见StartMode类
```java
StartMode.STANDARD,
StartMode.SINGLE_TOP,
StartMode.SINGLE_INSTANCE,
StartMode.SINGLE_TASK
```
