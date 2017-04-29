/*
 * Copyright (C) 2015-present, Wei Chou (weichou2010@gmail.com)
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

import android.view.View.OnClickListener;

/**
 * 如果没有参数，等同于 {@link ViewOnClick#handler() @ViewOnClick(handler=Instance.CLASS)},
 * 表示直接使用当前类所实现的{@link OnClickListener}，仅用于View类型的变量。
 * @author 周伟 Wei Chou(weichou2010@gmail.com)
 */
@Target({ElementType.FIELD, ElementType.METHOD})
@Retention(RetentionPolicy.RUNTIME)
@Inherited
public @interface ViewOnClick {
	Ids value() default @Ids(0);
	/**
	 * 指定当前View点击事件的处理对象类型
	 * @deprecated 不需要指定，会自动适应。
	 */
	@Deprecated
	Instance handler() default Instance.CLASS;

	enum Instance {
		/**当前类的对象**/
		CLASS,
		METHOD,
		/**当前类的对象中的变量或常量指定的对象**/
		FIELD
	}
}
