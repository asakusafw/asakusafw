/**
 * Copyright 2011-2016 Asakusa Framework Team.
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
package com.asakusafw.testtools;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.List;

import org.apache.hadoop.io.Writable;

import com.asakusafw.runtime.value.ValueOption;

/**
 * Modelオブジェクトの比較用のComparator。
 * @param <T> モデルオブジェクトの型
 */
public class ModelComparator<T extends Writable> implements Comparator<T>, Serializable {

    private static final long serialVersionUID = 2L;

    /**
     * モデルオブジェクトからキー項目を取得するgetterのリスト。
     */
    private transient List<Method> getters;

    /**
     * コンストラクタ。
     * @param columnInfos モデルクラスのカラム(フィールド)情報
     * @param modelClass ソート対象のモデルクラス
     */
    public ModelComparator(List<ColumnInfo> columnInfos, Class<?> modelClass) {
        getters = new ArrayList<Method>();
        for (ColumnInfo info : columnInfos) {
            if (info.isKey()) {
                try {
                    Method method = modelClass.getMethod(info.getGetterName());
                    getters.add(method);
                } catch (SecurityException e) {
                    throw new RuntimeException(e);
                } catch (NoSuchMethodException e) {
                    throw new RuntimeException(e);
                }
            }
        }
    }

    @Override
    public int compare(T o1, T o2) {
        for (Method getter : getters) {
            @SuppressWarnings("rawtypes")
            Comparable vo1;
            @SuppressWarnings("rawtypes")
            Comparable vo2;
            try {
                vo1  = (ValueOption<?>) getter.invoke(o1);
                vo2  = (ValueOption<?>) getter.invoke(o2);
            } catch (IllegalArgumentException e) {
                throw new RuntimeException(e);
            } catch (IllegalAccessException e) {
                throw new RuntimeException(e);
            } catch (InvocationTargetException e) {
                throw new RuntimeException(e);
            }
            @SuppressWarnings("unchecked")
            int ret = vo1.compareTo(vo2);
            if (ret != 0) {
                return ret;
            }
        }
        return 0;
    }

    private void readObject(ObjectInputStream stream) throws IOException, ClassNotFoundException {
        stream.defaultReadObject();
        Method[] methods = new Method[stream.readInt()];
        for (int i = 0; i < methods.length; i++) {
            Class<?> aClass = (Class<?>) stream.readObject();
            String methodName = stream.readUTF();
            try {
                methods[i] = aClass.getDeclaredMethod(methodName);
            } catch (SecurityException e) {
                throw new IOException(e);
            } catch (NoSuchMethodException e) {
                throw new IOException(e);
            }
        }
        this.getters = Arrays.asList(methods);
    }

    private void writeObject(ObjectOutputStream stream) throws IOException {
        stream.defaultWriteObject();
        stream.writeInt(getters.size());
        for (Method method : getters) {
            stream.writeObject(method.getDeclaringClass());
            stream.writeUTF(method.getName());
        }
    }
}
