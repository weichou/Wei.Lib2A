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

package hobby.wei.c.data.abs;

import hobby.wei.c.anno.proguard.KeepC$$e;

/**
 * 替代{@link com.google.gson.reflect.TypeToken}，
 * 所有使用到{@link com.google.gson.reflect.TypeToken}的地方，
 * 用本类替代即可解决混淆问题。<br/>
 * （不过这次测试ProGuard v4.7-5.2.1都没有复现该问题。保险起见，先留着）
 *
 * @author Wei Chou(weichou2010@gmail.com)
 * @version 1.0, 11/12/2015
 */
@KeepC$$e
public abstract class TypeToken<T> extends com.google.gson.reflect.TypeToken<T> {
}
