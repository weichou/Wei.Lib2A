/*
 * Copyright (C) 2017-present, Wei.Chou(weichou2010@gmail.com)
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

package hobby.wei.c.tools;

/**
 * @author Wei.Chou
 * @version 1.0, 23/03/2017
 */
public interface ICache<K, V> {
    V get(K key);

    V getOnly(K key);

    V refresh(K key);

    void dirty(K key);

    boolean update(K key, V value);

    void clear();

    interface Delegate<K, V> {
        V load(K key);

        boolean update(K key, V value);
    }

    class Impl<K, V> implements ICache<K, V> {
        protected final LruCache<K, V> mLruCache;
        protected final Delegate<K, V> mDelegate;

        /**
         * @param cacheSize 单位：条（一个key-value对为一条）。
         */
        public Impl(int cacheSize, Delegate<K, V> delegate) {
            mLruCache = new LruCache<K, V>(cacheSize) {
                @Override
                protected int sizeOf(K key, V value) {
            /*
             * 由于通过反射递归遍历父类属性来计算对象实际内存占用是不现实的：
             * 一是性能问题；
             * 二是涉及到共享对象、循环引用、32/64位处理器对象头、引用压缩、4/8bytes对齐等因素的不确定性，
             * 要做到准确性，代价很大，没必要。
             * 因此这里按条数，一条数据的占用就是1。
             */
                    return 1;
                }
            };
            mDelegate = delegate;
        }

        @Override
        public V get(K key) {
            V value = mLruCache.get(key);
            if (value == null) {
                value = refresh(key);
            }
            return value;
        }

        @Override
        public V getOnly(K key) {
            return mLruCache.get(key);
        }

        @Override
        public V refresh(K key) {
            final V value = mDelegate.load(key);
            if (value != null) mLruCache.put(key, value);
            return value;
        }

        @Override
        public void dirty(K key) {
            mLruCache.remove(key);
        }

        @Override
        public boolean update(K key, V value) {
            dirty(key);
            if (mDelegate.update(key, value)) {
                mLruCache.put(key, value);
                return true;
            }
            return false;
        }

        @Override
        public void clear() {
            mLruCache.evictAll();
        }

        /**
         * 仅在第一次{@link #get(K)}的时候，使用线程同步。等同于单例实现。
         */
        public static class SyncGet<K, V> extends Impl<K, V> {
            public SyncGet(int cacheSize, Delegate<K, V> delegate) {
                super(cacheSize, delegate);
            }

            @Override
            public V get(K key) {
                V value = mLruCache.get(key);
                if (value == null) {
                    synchronized (this) {
                        value = super.get(key);
                    }
                }
                return value;
            }
        }

        public static class Sync<K, V> extends Impl<K, V> {
            public Sync(int cacheSize, Delegate<K, V> delegate) {
                super(cacheSize, delegate);
            }

            @Override
            public synchronized V get(K key) {
                return super.get(key);
            }

            @Override
            public synchronized V getOnly(K key) {
                return super.getOnly(key);
            }

            @Override
            public synchronized V refresh(K key) {
                return super.refresh(key);
            }

            @Override
            public synchronized void dirty(K key) {
                super.dirty(key);
            }

            @Override
            public synchronized boolean update(K key, V value) {
                return super.update(key, value);
            }

            @Override
            public synchronized void clear() {
                super.clear();
            }
        }
    }
}
