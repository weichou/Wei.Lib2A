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
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import android.view.View;

/**
 * @author 周伟 Wei Chou(weichou2010@gmail.com)
 */
@Target(ElementType.FIELD)
@Retention(RetentionPolicy.RUNTIME)
public @interface ViewId {
	/**
	 * 直接写常量R.id.xxx_name_of_id. 若不是常亮则应该使用下面的name属性。
	 */
	int value() default 0;
	/**
	 * 对于库项目，R.id.xxx_name_of_id不是常量，此时可以把常量名"xxx_name_of_id"写在这里，
	 * 而把{@link #value()}的值设为0.
	 */
	String name() default "";
	/**
	 * 值为{@link View#VISIBLE}、{@link View#INVISIBLE}或{@link View#GONE}
	 */
	int visibility() default -1;
}
