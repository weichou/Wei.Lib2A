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

import android.app.Activity;
import android.app.Dialog;
import android.content.Context;
import android.util.SparseArray;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;

import java.lang.reflect.Array;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import hobby.wei.c.L;
import hobby.wei.c.utils.IdGetter;
import hobby.wei.c.utils.ReflectUtils;

/**
 * @author 周伟 Wei Chou(weichou2010@gmail.com)
 */
public class Injector {
    private static final String TAG = "Injector";

    public static void inject(Activity activity, Class<?> stopSearch) {
        inject(activity, activity, stopSearch);
    }

    public static void inject(Object injectee, Activity activity, Class<?> stopSearch) {
        try {
            injectAnnos(injectee, activity, null, stopSearch);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    public static void inject(Dialog dialog, Class<?> stopSearch) {
        inject(dialog, dialog, stopSearch);
    }

    public static void inject(Object injectee, Dialog dialog, Class<?> stopSearch) {
        try {
            injectAnnos(injectee, dialog, null, stopSearch);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    public static void inject(Object injectee, View view, Class<?> stopSearch) {
        try {
            injectAnnos(injectee, view, null, stopSearch);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    public static void inject(ViewSettable injectee, ViewGroup parent, Class<?> stopSearch) {
        try {
            injectAnnos(injectee, null, parent, stopSearch);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    public static void inject(ViewGroup view, boolean createChildren, Class<?> stopSearch) {
        try {
            injectAnnos(view, createChildren ? null : view, view, stopSearch);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    public static int layoutID(Context context, Class<?> clazz) {
        final ViewLayoutId layoutId = clazz.getAnnotation(ViewLayoutId.class);
        return layoutId == null ? 0 : layoutId.value() > 0 ? layoutId.value() :
                getLayoutIdByName(context, layoutId.name());
    }

    public static int listViewID(Context context, Class<?> clazz) {
        final ViewListId listId = clazz.getAnnotation(ViewListId.class);
        return listId == null ? 0 : listId.value() > 0 ? listId.value() :
                getIdByName(context, listId.name());
    }

    public static int viewID(Context context, Field field) {
        return viewID(context, field.getAnnotation(ViewId.class));
    }

    private static int viewID(Context context, ViewId viewId) {
        return viewId == null ? 0 : viewId.value() > 0 ? viewId.value() :
                getIdByName(context, viewId.name());
    }

    public static int[] arrayIDs(Context context, Field field) {
        return arrayIDs(context, field.getAnnotation(Ids.class));
    }

    private static int[] arrayIDs(Context context, Ids ids) {
        if (ids == null) return new int[0];
        int len = ids.value().length;
        final int[] array = new int[len + ids.names().length];
        if (len > 0) System.arraycopy(ids.value(), 0, array, 0, len);
        for (String name : ids.names()) {
            array[len++] = getIdByName(context, name);
        }
        return array;
    }

    private static int getLayoutIdByName(Context context, String name) {
        return name.length() > 0 ? IdGetter.getLayoutId(context, name) : 0;
    }

    private static int getIdByName(Context context, String name) {
        return name.length() > 0 ? IdGetter.getIdId(context, name) : 0;
    }

    /**
     * 执行View的注入操作。
     *
     * @param injectee   被注入的对象。
     * @param container  layoutId可被解析的容器，同时被用于findViewById(); 当injectee为ViewGroup的时候可为null.
     * @param parent     仅用于{@link LayoutInflater#inflate(int, ViewGroup, boolean)}.
     *                   此时injectee必须实现{@link ViewSettable}接口。
     * @param stopSearch 在哪一级父类停止搜索并解析注解。
     * @throws Exception
     */
    private static void injectAnnos(final Object injectee, Object container,
                                    final ViewGroup parent, final Class<?> stopSearch) throws Exception {
        final long begin = System.nanoTime();
        final InjectWorker worker;
        final Context context;
        if (container instanceof Activity) {
            context = (Activity) container;
            // injectAnnos @ViewLayoutId
            final int layoutId = layoutID(context, injectee.getClass());
            // 对于没有layoutId的，仅为了注入viewId
            if (layoutId > 0) ((Activity) container).setContentView(layoutId);
            worker = new InjectWorker4Acty(injectee, (Activity) container, stopSearch);
        } else if (container instanceof Dialog) {
            context = ((Dialog) container).getContext();
            final int layoutId = layoutID(context, injectee.getClass());
            if (layoutId > 0) ((Dialog) container).setContentView(layoutId);
            worker = new InjectWorker4Dialog(injectee, (Dialog) container, stopSearch);
        } else {
            if (container == null) {
                if (injectee instanceof ViewGroup) {
                    // 这个比较特殊：
                    // 如果contaner是View, 则认为整个layout结构是已经创建好的；
                    // 这里只有在injectee是ViewGroup, 且contaner为空的情况下，才创建layout。
                    // 即：存在layoutId, 并不都要创建，AbsViewHolder便是一个例子，在进入本方法之前layout已经创建。
                    final ViewGroup viewGroup = (ViewGroup) injectee;
                    context = viewGroup.getContext();
                    final int layoutId = layoutID(context, injectee.getClass());
                    if (layoutId > 0) View.inflate(context, layoutId, viewGroup);
                    container = viewGroup;
                } else if (injectee instanceof ViewSettable) {
                    if (parent == null) {
                        throw new IllegalArgumentException("当前参数组合下，parent不能为null");
                    }
                    context = parent.getContext();
                    final int layoutId = layoutID(context, injectee.getClass());
                    if (layoutId > 0) {
                        final View view = LayoutInflater.from(context).inflate(layoutId, parent, false);
                        ((ViewSettable) injectee).onInjectView(view);
                        container = view;
                    } else {
                        throw new IllegalArgumentException("当前参数组合下，需要layoutId标注");
                    }
                } else {
                    throw new IllegalArgumentException("当前参数组合，无法生成container");
                }
            } else if (container instanceof View) {
                context = ((View) container).getContext();
                // 即使存在layoutId, 也不创建，如AbsViewHolder, 在进入本方法之前layout已经创建。
            } else {
                throw new IllegalArgumentException("container类型不合法（可以为null）");
            }
            worker = new InjectWorker4View(injectee, (View) container, stopSearch);
        }
        worker.inject(context);
        L.i(TAG, "time duration:%sms. injectee:%s", ((System.nanoTime() - begin) * 1e-6), L.s(injectee.getClass().getSimpleName()));
    }

    private static abstract class InjectWorker<O, C> {
        private static final Method sOnClickMethod = OnClickListener.class.getDeclaredMethods()[0];
        private static final String sOcmName = sOnClickMethod.getName();
        private static final Class<?> sOcmReturnType = sOnClickMethod.getReturnType();
        private static final Class<?>[] sOcmParamTypes = sOnClickMethod.getParameterTypes();

        protected final O mInjectee;
        protected final C mContainer;
        protected final Class<?> mStopSearch;

        private final SparseArray<View> mId2Views = new SparseArray<>();
        private final Map<OnClickListener, Ids> mHighPriorityOcl2Ids = new HashMap<>();
        private boolean mInjecteeOclChecked;

        protected InjectWorker(O injectee, C container, Class<?> stopSearch) {
            mInjectee = injectee;
            mContainer = container;
            mStopSearch = stopSearch;
        }

        protected static View findViewById(Activity container, int id) {
            return container.findViewById(id);
        }

        protected static View findViewById(Dialog container, final int id) {
            return container.findViewById(id);
        }

        protected static View findViewById(View container, final int id) {
            return container.findViewById(id);
        }

        protected abstract View findViewById(int id);

        public void inject(final Context context) throws Exception {
            final List<Field> fields = ReflectUtils.getFields(mInjectee.getClass(), mStopSearch);
            for (Field field : fields) {
                final Class<?> type = field.getType();
                if (View.class.isAssignableFrom(type)) {
                    //inject @ViewId
                    injectFieldViewWithClick(context, field);
                } else if (OnClickListener.class.isAssignableFrom(type)) {
                    //inject @ViewOnClick
                    final ViewOnClick viewOnClick = field.getAnnotation(ViewOnClick.class);
                    if (viewOnClick != null) {
                        field.setAccessible(true);
                        final OnClickListener onClick = (OnClickListener) field.get(mInjectee);
                        field.setAccessible(false);
                        if (onClick != null) {
                            //设计为最高优先级，先存起来，最后处理
                            mHighPriorityOcl2Ids.put(onClick, viewOnClick.value());
                        }
                    }
                } else {
                    injectFieldArrayIds(context, field, type);
                }
            }
            //inject @ViewOnClick
            final List<Method> methods = ReflectUtils.getMethods(mInjectee.getClass(), mStopSearch);
            for (final Method method : methods) {
                injectMethodOnClick(context, method);
            }
            //inject @ViewOnClick 优先级最高，会覆盖掉前面的
            for (Map.Entry<OnClickListener, Ids> entry : mHighPriorityOcl2Ids.entrySet()) {
                for (int id : arrayIDs(context, entry.getValue())) {
                    final View view = findOrGetView(id);
                    if (view != null) {
                        view.setOnClickListener(entry.getKey());
                    }
                }
            }
        }

        private void injectFieldViewWithClick(Context context, Field field) throws Exception {
            final ViewId viewId = field.getAnnotation(ViewId.class);
            if (viewId != null) {
                final View view = findOrGetView(viewID(context, viewId));
                if (view != null) {
                    field.setAccessible(true);
                    field.set(mInjectee, view);
                    field.setAccessible(false);
                    final int visible = viewId.visibility();
                    if (visible == View.VISIBLE || visible == View.INVISIBLE || visible == View.GONE) {
                        //noinspection WrongConstant
                        view.setVisibility(visible);
                    }
                    //inject @ViewOnClick
                    if (field.getAnnotation(ViewOnClick.class) != null) {
                        checkInjecteeOnClickable();
                        view.setOnClickListener((OnClickListener) mInjectee);
                    }
                }
            }
        }

        private void injectMethodOnClick(Context context, final Method method) {
            final ViewOnClick viewOnClick = method.getAnnotation(ViewOnClick.class);
            if (viewOnClick != null) {
                for (int id : arrayIDs(context, viewOnClick.value())) {
                    final View view = findOrGetView(id);
                    if (view != null) {
                        if (mInjectee instanceof OnClickListener
                                && method.getName().equals(sOcmName)
                                && method.getReturnType().equals(sOcmReturnType)
                                && Arrays.equals(method.getParameterTypes(), sOcmParamTypes)) {
                            view.setOnClickListener((OnClickListener) mInjectee);
                        } else if (method.getParameterTypes().length <= 0) {
                            view.setOnClickListener(new OnClickListener() {
                                @Override
                                public void onClick(View v) {
                                    try {
                                        method.setAccessible(true);
                                        method.invoke(mInjectee);
                                        method.setAccessible(false);
                                    } catch (Exception e) {
                                        throw new RuntimeException(e);
                                    }
                                }
                            });
                        } else {
                            view.setOnClickListener(new OnClickListener() {
                                @Override
                                public void onClick(View v) {
                                    try {
                                        method.setAccessible(true);
                                        method.invoke(mInjectee, v);
                                        method.setAccessible(false);
                                    } catch (Exception e) {
                                        throw new RuntimeException(e);
                                    }
                                }
                            });
                        }
                    }
                }
            }
        }

        private void injectFieldArrayIds(Context context, Field field, Class<?> type) throws Exception {
            final boolean intArray = int[].class.isAssignableFrom(type);
            final boolean viewArray = View[].class.isAssignableFrom(type);
            if (intArray || viewArray) {
                final int[] ids = arrayIDs(context, field);
                if (intArray) { //不过滤0值
                    field.setAccessible(true);
                    field.set(mInjectee, ids);
                    field.setAccessible(false);
                } else {
                    final LinkedList<View> views = new LinkedList<>();
                    for (int id : ids) {
                        final View view = findOrGetView(id);
                        //与int[]变量不同，这里对Object过滤null.
                        if (view != null) views.add(view);
                    }
                    //生成符合定义类型的数组
                    final View[] array = (View[]) Array.newInstance(type.getComponentType(), views.size());
                    field.setAccessible(true);
                    field.set(mInjectee, views.toArray(array));
                    field.setAccessible(false);
                }
            }
        }

        private View findOrGetView(int id) {
            View view = null;
            if (id > 0) {
                view = mId2Views.get(id);
                if (view == null) {
                    view = findViewById(id);
                    mId2Views.put(id, view);    //后面还有地方会使用这个map
                }
            }
            return view;
        }

        private void checkInjecteeOnClickable() {
            if (mInjecteeOclChecked) return;
            if (mInjectee instanceof OnClickListener) {
                mInjecteeOclChecked = true;
            } else {
                throw new IllegalStateException("参数injectee的类型应该实现OnClickListener接口，"
                        + "否则不要给有@ViewId标注的变量增加@ViewOnClick标注。");
            }
        }
    }

    private static class InjectWorker4Acty extends InjectWorker<Object, Activity> {
        protected InjectWorker4Acty(Object injectee, Activity container, Class<?> stopSearch) {
            super(injectee, container, stopSearch);
        }

        @Override
        protected View findViewById(int id) {
            return findViewById(mContainer, id);
        }
    }

    private static class InjectWorker4Dialog extends InjectWorker<Object, Dialog> {
        protected InjectWorker4Dialog(Object injectee, Dialog container, Class<?> stopSearch) {
            super(injectee, container, stopSearch);
        }

        @Override
        protected View findViewById(int id) {
            return findViewById(mContainer, id);
        }
    }

    private static class InjectWorker4View extends InjectWorker<Object, View> {
        protected InjectWorker4View(Object injectee, View container, Class<?> stopSearch) {
            super(injectee, container, stopSearch);
        }

        @Override
        protected View findViewById(int id) {
            return findViewById(mContainer, id);
        }
    }

    public interface ViewSettable {
        void onInjectView(View view);
    }
}
