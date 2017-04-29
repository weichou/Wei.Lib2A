/*
 * Copyright (C) 2014-present, Wei Chou (weichou2010@gmail.com)
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package hobby.wei.c.anno.inject;

import java.lang.annotation.ElementType;
import java.lang.annotation.Inherited;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * 主题参数配置。命名规则：这里填写的参数ID的名字是最基本的名字（见{@link com.wei.c.framework.theme.Theme#DEFAULT}），如R.id.abcd。
 * 当应用某主题时，若主题为内置主题（见{@link com.wei.c.framework.theme.Theme#inner}），则该主题的suffix将被添加到ID名字的后面
 * （如suffix为night，则应用该主题后的resID为R.id.abcd_night）；若主题为外置主题，则将主题的suffix插入到文件名后面扩展名之前
 * （如：图片path/abcd.png，应用主题之后的路径为path/abcd_night.png），同时还有加载不同dpi资源的规则，这里不用了解，打包资源时保持跟res下一致即可。
 * 
 * @author 周伟 Wei Chou(weichou2010@gmail.com)
 */
@Target(ElementType.FIELD)
@Retention(RetentionPolicy.RUNTIME)
@Inherited	//若不加这个，则子类将无法获得父类的注解，使用后可获得，并且如果子类使用了和父类相同的注解，则父类的注解将被擦除，非常好
public @interface ViewThemeParams {
    public static final String FONT_NORMAL = null;
    public static final String FONT_SANS = "sans-serif";
    public static final String FONT_SERIF = "serif";
    public static final String FONT_MONOSPACE = "monospace";
    
	int background() default 0;
	int backgroundColor() default 0;
	
	int textSize() default 0;
	/**如果是系统支持的字体，则这里应该填写字体的名字（但不推荐写在这里），如：sans-serif、serif和monospace等；
	 * 如果是字体文件：
	 * 若是内置主题，则应放在assets目录下，这里填写相对assets目录的路径dir/fontname.xxx；
	 * 若为外置主题，则要填写相对于外置主题根目录的路径。
	 * 但是注意：由于这里只能填写一个路径，却有可能要处理内外置主题的切换，因此打包外置主题文件时，路径应该与内置保持一致。**/
	String textTypeface() default "";
	/**{@link android.graphics.Typeface#BOLD} 等**/
	int textTypefaceStyle() default 0;
	
	int textColor() default 0;
	int textColorHint() default 0;
	int textColorLink() default 0;
	int textColorHighlight() default 0;
	
	int textView_drawableLeft() default 0;
	int textView_drawableTop() default 0;
	int textView_drawableRight() default 0;
	int textView_drawableBottom() default 0;
	
	int textView_shadowColor() default 0;
	int textView_shadowDx() default 0;
	int textView_shadowDy() default 0;
	int textView_shadowRadius() default 0;
}
