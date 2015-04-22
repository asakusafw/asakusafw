/**
 * Copyright 2011-2015 Asakusa Framework Team.
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
package com.asakusafw.runtime.flow.join;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * メモリ上にデータを保持する{@link LookUpTable}の実装。
 * @param <T> 要素の種類
 */
public class VolatileLookUpTable<T> implements LookUpTable<T> {

    private final Map<LookUpKey, List<T>> entity;

    /**
     * インスタンスを生成する。
     * @param entity テーブルが保持すべきキーと値のマップ
     * @throws IllegalArgumentException 引数に{@code null}が指定された場合
     */
    public VolatileLookUpTable(Map<LookUpKey, List<T>> entity) {
        if (entity == null) {
            throw new IllegalArgumentException("entity must not be null"); //$NON-NLS-1$
        }
        this.entity = entity;
    }

    @Override
    public List<T> get(LookUpKey key) {
        if (key == null) {
            throw new IllegalArgumentException("key must not be null"); //$NON-NLS-1$
        }
        List<T> list = entity.get(key);
        if (list == null) {
            return Collections.emptyList();
        }
        return list;
    }

    /**
     * {@link VolatileLookUpTable}を構築するビルダー。
     * @param <T> 要素の型
     */
    public static class Builder<T> implements LookUpTable.Builder<T> {

        private final Map<LookUpKey, List<T>> entity = new HashMap<LookUpKey, List<T>>();

        @Override
        public void add(LookUpKey key, T value) throws IOException {
            if (key == null) {
                throw new IllegalArgumentException("key must not be null"); //$NON-NLS-1$
            }
            List<T> list = entity.get(key);
            if (list == null) {
                list = new ArrayList<T>();
                entity.put(key.copy(), list);
            }
            list.add(value);
        }

        @Override
        public LookUpTable<T> build() throws IOException {
            return new VolatileLookUpTable<T>(entity);
        }
    }
}
