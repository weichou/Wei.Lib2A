#gradle配置中最好启用优化。即引用'proguard-android-optimize.txt'
#proguardFiles getDefaultProguardFile('proguard-android-optimize.txt'), 'proguard-rules.pro'

-include libs/annotations.pro

#如果是以jar包的方式导入的而不是作为库项目，那么需要下面两行：#
-dontwarn hobby.wei.c.**
-libraryjars libs/wei.lib2a.jar

-keep class com.google.gson.stream.** { *; }
