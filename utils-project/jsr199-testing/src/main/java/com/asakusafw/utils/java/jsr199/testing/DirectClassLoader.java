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
package com.asakusafw.utils.java.jsr199.testing;

import java.util.HashMap;
import java.util.Map;

/**
 * クラスファイルの内容を直接取り扱うローダー。
 */
public class DirectClassLoader extends ClassLoader {

    private Map<String, byte[]> classes;

    /**
     * インスタンスを生成する。
     * @param parent 親のクラスローダー
     */
    public DirectClassLoader(ClassLoader parent) {
        super(parent);
        this.classes = new HashMap<String, byte[]>();
    }

    /**
     * このローダーがロード可能なクラスの情報を追加する。
     * @param name 追加するクラスのバイナリ名
     * @param content 追加するクラスの内容
     * @throws IllegalArgumentException 引数に{@code null}が含まれる場合
     */
    public synchronized void add(String name, byte[] content) {
        if (name == null) {
            throw new IllegalArgumentException("name must not be null"); //$NON-NLS-1$
        }
        if (content == null) {
            throw new IllegalArgumentException("content must not be null"); //$NON-NLS-1$
        }
        classes.put(name, content);
    }

    @Override
    protected synchronized Class<?> findClass(String name) throws ClassNotFoundException {
        byte[] bytes = classes.remove(name);
        if (bytes == null) {
            return super.findClass(name);
        }
        return defineClass(name, bytes, 0, bytes.length, null);
    }
}
