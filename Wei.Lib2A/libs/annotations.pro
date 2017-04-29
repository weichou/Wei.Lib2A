# Copyright (C) 2016-present, Wei Chou (weichou2010@gmail.com)
#
# Licensed under the Apache License, Version 2.0 (the "License");
# you may not use this file except in compliance with the License.
# You may obtain a copy of the License at
#
#      http://www.apache.org/licenses/LICENSE-2.0
#
# Unless required by applicable law or agreed to in writing, software
# distributed under the License is distributed on an "AS IS" BASIS,
# WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
# See the License for the specific language governing permissions and
# limitations under the License.
#

##__________________________________________________
##本实现基于ProGuard5.2.1测试
# @author Wei Chou(weichou2010@gmail.com)
# @version 1.0, 19/02/2016
##``````````````````````````````````````````````````

#####################注意：注释不可以中文结束，应该像本行这样#####################


#####>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>##
#####名词解释：
#[强]保留：既不被重命名，又不被删除；
#[弱]保留：只确保不被重命名，不保证不被删除；
#[反]保留：不保证不被重命名，但确保不被删除；
#[不]保留：排除到以上保留名单之外。
#####优先级：
#由高到低依次为强、反、弱、不。被延续到子类（接口）的，子类享有相同优先级。
#任何位置，若被不同优先级的注解同时标注，则会应用较高优先级。
#####约定：
#含有[$$]字符的为[反保留]；
#含有[$]字符（除反保留外）的为[强保留]；
#不含以上标识字符的为[弱保留]；
#没有用于[不保留]的标签，即：不加任何标注则为不保留；
#另：以[e]结尾的表示将其前面名称表达的功能延续到子类。
#需要注意的是：只有父类（接口）存在的情况下，才能延续到子类。
#即：首先需要将父类强保留或反保留。
#####其它：
#对于[**.anno.inject]包中的任何注解，只要该注解没有被优化掉，就会对其直接作用的内容进行反保留。
##更多内容请见@**.anno.proguard.Keep注解的文档。
##<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<#####


#####>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>##
#####客户代码可使用如下方法引用本配置：
#-include libs/annotations.pro
##<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<#####


##__________________________________________________
##这些都不要：
#-dontshrink
#-keepattributes *Annotation*
##``````````````````````````````````````````````````

##__________________________________________________
##保持泛型签名，用于Gson反序列化，以及其他反射用途#
-keepattributes Signature
#
##__________________________________________________
##保持行号，以便跟踪错误记录
#但是这些信息会占用不小的空间（只不过不是在原始字节码上增加的）
#通过命令可恢复原文：java -jar retrace.jar mapping.txt stacktrace.txt
#见http://proguard.sourceforge.net/manual/examples.html#stacktrace##
##``````````````````````````````````````````````````
#-printmapping out.map  #默认会输出到 mapping.txt，这里不用配置#
#参数x会出现在at com.xxx.ClassName.method(x:40)括号中，
#默认跟混淆前的类名一致，所以必须替换#
-renamesourcefileattribute x
##``````````````````````````````````````````````````
-keepattributes SourceFile, LineNumberTable
##``````````````````````````````````````````````````

##__________________________________________________
#虽然class限定词对interface也起作用，保险起见，还是都写上
##``````````````````````````````````````````````````


#####>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>##
#####Android提供的Keep注解，不推荐使用#
#####目前Gradle(据说)也并未对其实现#
-keep, allowoptimization, allowobfuscation @interface android.support.annotation.Keep
-keep @android.support.annotation.Keep @interface *
-keep @android.support.annotation.Keep interface *
-keep @android.support.annotation.Keep class *
-keepclassmembers @interface * {
    @android.support.annotation.Keep *;
}
-keepclassmembers interface * {
    @android.support.annotation.Keep *;
}
#这种用法不对，会把任何类名都保留#
#-keep class * {
#    @android.support.annotation.Keep *;
#}
-keepclassmembers class * {
    @android.support.annotation.Keep *;
}
##<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<#####



#####>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>##
#-----------------接下来的内容顺序非常重要-----------------#
##<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<#####
#
##__________________________________________________
#allowshrinking 压缩，删除没有用到的方法字段等#
#allowoptimization 优化，文件合并之类，比较复杂。注意：需要通过反射来调用的都不要启用。#
#allowobfuscation 重命名，常说的混淆其实是这一步#
#-keep, allowshrinking, allowoptimization, allowobfuscation @interface **.anno.proguard.P$$
#
##本实现的基础#
-keep, allowoptimization, allowobfuscation @interface **.anno.proguard.P$$
-keep, allowoptimization, allowobfuscation @**.anno.proguard.P$$ @interface *
##``````````````````````````````````````````````````
#
#####>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>##
#####以下为字段：
#-keep后面带names的只保证不重命名，不保证不被删除。即[弱]保留#
##__________________________________________________
##弱保留全部非static字段#
-keepclassmembernames @**.anno.proguard.KeepV @interface * {
    !static <fields>;
}
-keepclassmembernames @**.anno.proguard.KeepV interface * {
    !static <fields>;
}
-keepclassmembernames @**.anno.proguard.KeepV class * {
    !static <fields>;
}
##``````````````````````````````````````````````````
##强保留全部public非static字段#
-keepclassmembers @**.anno.proguard.KeepVp$ @interface * {
    public !static <fields>;
}
-keepclassmembers @**.anno.proguard.KeepVp$ interface * {
    public !static <fields>;
}
-keepclassmembers @**.anno.proguard.KeepVp$ class * {
    public !static <fields>;
}
##``````````````````````````````````````````````````
##同KeepVp$，并延续到子类#
-keepclassmembers @**.anno.proguard.KeepVp$e @interface * {
    public !static <fields>;
}
-keepclassmembers @**.anno.proguard.KeepVp$e interface * {
    public !static <fields>;
}
-keepclassmembers @**.anno.proguard.KeepVp$e class * {
    public !static <fields>;
}
##``````````````````````````````````````````````````
##延续到子类#
-keepclassmembers interface * extends @**.anno.proguard.KeepVp$e * {
    public !static <fields>;
}
-keepclassmembers class * implements @**.anno.proguard.KeepVp$e * {
    public !static <fields>;
}
-keepclassmembers class * extends @**.anno.proguard.KeepVp$e * {
    public !static <fields>;
}
##``````````````````````````````````````````````````
##弱保留全部static字段#
-keepclassmembernames @**.anno.proguard.KeepVs @interface * {
    static <fields>;
}
-keepclassmembernames @**.anno.proguard.KeepVs interface * {
    static <fields>;
}
-keepclassmembernames @**.anno.proguard.KeepVs class * {
    static <fields>;
}
##``````````````````````````````````````````````````
##强保留全部public static字段#
-keepclassmembers @**.anno.proguard.KeepVps$ @interface * {
    public static <fields>;
}
-keepclassmembers @**.anno.proguard.KeepVps$ interface * {
    public static <fields>;
}
-keepclassmembers @**.anno.proguard.KeepVps$ class * {
    public static <fields>;
}
##<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<#####
#
#
#####>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>##
#####以下为方法：
##__________________________________________________
##弱保留全部非static方法#
-keepclassmembernames @**.anno.proguard.KeepM @interface * {
    !static <methods>;
}
-keepclassmembernames @**.anno.proguard.KeepM interface * {
    !static <methods>;
}
-keepclassmembernames @**.anno.proguard.KeepM class * {
    !static <methods>;
}
##``````````````````````````````````````````````````
##强保留全部public非static方法#
-keepclassmembers @**.anno.proguard.KeepMp$ @interface * {
    public !static <methods>;
}
-keepclassmembers @**.anno.proguard.KeepMp$ interface * {
    public !static <methods>;
}
-keepclassmembers @**.anno.proguard.KeepMp$ class * {
    public !static <methods>;
}
##``````````````````````````````````````````````````
##同KeepMp$，并延续到子类#
-keepclassmembers @**.anno.proguard.KeepMp$e @interface * {
    public !static <methods>;
}
-keepclassmembers @**.anno.proguard.KeepMp$e interface * {
    public !static <methods>;
}
-keepclassmembers @**.anno.proguard.KeepMp$e class * {
    public !static <methods>;
}
##``````````````````````````````````````````````````
##延续到子类#
-keepclassmembers interface * extends @**.anno.proguard.KeepMp$e * {
    public !static <methods>;
}
-keepclassmembers class * implements @**.anno.proguard.KeepMp$e * {
    public !static <methods>;
}
-keepclassmembers class * extends @**.anno.proguard.KeepMp$e * {
    public !static <methods>;
}
##``````````````````````````````````````````````````
##弱保留全部static方法#
-keepclassmembernames @**.anno.proguard.KeepMs @interface * {
    static <methods>;
}
-keepclassmembernames @**.anno.proguard.KeepMs interface * {
    static <methods>;
}
-keepclassmembernames @**.anno.proguard.KeepMs class * {
    static <methods>;
}
##``````````````````````````````````````````````````
##强保留全部public static方法#
-keepclassmembers @**.anno.proguard.KeepMps$ @interface * {
    public static <methods>;
}
-keepclassmembers @**.anno.proguard.KeepMps$ interface * {
    public static <methods>;
}
-keepclassmembers @**.anno.proguard.KeepMps$ class * {
    public static <methods>;
}
##<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<#####
#
#
#####>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>##
##__________________________________________________
##弱保留#
-keepnames @**.anno.proguard.Keep @interface *
-keepnames @**.anno.proguard.Keep interface *
-keepnames @**.anno.proguard.Keep class *
-keepclassmembernames @interface * {
    @**.anno.proguard.Keep *;
}
-keepclassmembernames interface * {
    @**.anno.proguard.Keep *;
}
#这种用法不对，会把任何类名都保留#
#-keepnames class * {
#    @**.anno.proguard.Keep *;
#}
-keepclassmembernames class * {
    @**.anno.proguard.Keep *;
}
##``````````````````````````````````````````````````
#
##__________________________________________________
##任何被**.anno.inject包中的注解标注的，只要注解还在，就反保留#
##（如果注解已经不在了，那这句是不起作用的）#
-keepclassmembers, allowoptimization, allowobfuscation class * {
    @**.anno.inject.** *;
}
##``````````````````````````````````````````````````
##反保留构造方法#
##但allowoptimization会导致反射找不到构造方法#
-keepclassmembers, allowobfuscation @**.anno.proguard.KeepC$$e class * {
    <init>(...);
}
#延续到子类#
-keepclassmembers, allowobfuscation class * implements @**.anno.proguard.KeepC$$e * {
    <init>(...);
}
-keepclassmembers, allowobfuscation class * extends @**.anno.proguard.KeepC$$e * {
    <init>(...);
}
##``````````````````````````````````````````````````
#
##__________________________________________________
##反保留#
-keep, allowoptimization, allowobfuscation @**.anno.proguard.Keep$$ @interface *
-keep, allowoptimization, allowobfuscation @**.anno.proguard.Keep$$ interface *
-keep, allowoptimization, allowobfuscation @**.anno.proguard.Keep$$ class *
-keepclassmembers, allowoptimization, allowobfuscation @interface * {
    @**.anno.proguard.Keep$$ *;
}
-keepclassmembers, allowoptimization, allowobfuscation interface * {
    @**.anno.proguard.Keep$$ *;
}
-keepclassmembers, allowoptimization, allowobfuscation class * {
    @**.anno.proguard.Keep$$ *;
}
##``````````````````````````````````````````````````
#
##__________________________________________________
##强保留#
-keep @**.anno.proguard.Keep$ @interface *
-keep @**.anno.proguard.Keep$ interface *
-keep @**.anno.proguard.Keep$ class *
-keepclassmembers @interface * {
    @**.anno.proguard.Keep$ *;
}
-keepclassmembers interface * {
    @**.anno.proguard.Keep$ *;
}
-keepclassmembers class * {
    @**.anno.proguard.Keep$ *;
}
##``````````````````````````````````````````````````
##<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<#####



#####>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>##
#必须得配合proguard-android-optimize.txt使用，即build.gradle里面将proguard-android.txt改为这个#
#或者确保所有配置中没有这句：#
#-dontoptimize
#
#并启用下面这些：#
##__________________________________________________
#-optimizations !code/simplification/arithmetic,!code/simplification/cast,!field/*,!class/merging/*
#-optimizationpasses 5
#-allowaccessmodification
#
#-dontusemixedcaseclassnames
#-dontskipnonpubliclibraryclasses
#-verbose
#-dontpreverify
##``````````````````````````````````````````````````
-assumenosideeffects class * {
    @**.anno.proguard.Burden *** *(...);
}
#相关字符串优化规则请见@**.anno.proguard.Burden源码文档。
##<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<#####


#####>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>##
#####以下是任何时候都必须保留的#
-keepclassmembers enum * {
    public static **[] values();
    public static ** valueOf(java.lang.String);
}
-keepclassmembers class * implements java.io.Serializable {
    static final long serialVersionUID;
    static final java.io.ObjectStreamField[] serialPersistentFields;
    private void writeObject(java.io.ObjectOutputStream);
    private void readObject(java.io.ObjectInputStream);
    java.lang.Object writeReplace();
    java.lang.Object readResolve();
}
##<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<#####
