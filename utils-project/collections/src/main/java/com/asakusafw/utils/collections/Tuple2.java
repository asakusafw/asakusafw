/**
 * Copyright 2011-2012 Asakusa Framework Team.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.asakusafw.utils.collections;

/**
 * 二要素からなる組。
 * @param <T1> 第一要素の型
 * @param <T2> 第二要素の型
 */
public class Tuple2<T1, T2> {

    /**
     * 第一要素。
     */
    public final T1 _1;

    /**
     * 第二要素。
     */
    public final T2 _2;

    /**
     * インスタンスを生成する。
     *
     * @param _1
     *            第一要素
     * @param _2
     *            第二要素
     */
    public Tuple2(T1 _1, T2 _2) {
        this._1 = _1;
        this._2 = _2;
    }

    /**
     * 第一要素を返す。
     *
     * @return 第一要素
     */
    public T1 getFirst() {
        return _1;
    }

    /**
     * 第二要素を返す。
     *
     * @return 第二要素
     */
    public T2 getSecond() {
        return _2;
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((_1 == null) ? 0 : _1.hashCode());
        result = prime * result + ((_2 == null) ? 0 : _2.hashCode());
        return result;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj == null) {
            return false;
        }
        if (getClass() != obj.getClass()) {
            return false;
        }
        Tuple2<?, ?> other = (Tuple2<?, ?>) obj;
        if (_1 == null) {
            if (other._1 != null) {
                return false;
            }
        } else if (_1.equals(other._1) == false) {
            return false;
        }
        if (_2 == null) {
            if (other._2 != null) {
                return false;
            }
        } else if (_2.equals(other._2) == false) {
            return false;
        }
        return true;
    }

    @Override
    public String toString() {
        return String.format("(%s, %s)", _1, _2); //$NON-NLS-1$
    }
}
