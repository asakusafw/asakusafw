/**
 * Copyright 2011-2014 Asakusa Framework Team.
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
package com.asakusafw.compiler.flow;

/**
 * コンパイル可能な要素。
 * @param <T> コンパイルした結果の情報
 */
public interface Compilable<T> {

    /**
     * この要素がコンパイル済みである場合のみ{@code true}を返す。
     * @return コンパイル済みである場合のみ{@code true}
     */
    boolean isCompiled();

    /**
     * この要素のコンパイル結果を返す。
     * @return コンパイル結果
     * @throws IllegalStateException コンパイル済みでない場合
     * @see #isCompiled()
     */
    T getCompiled();

    /**
     * この要素のコンパイル結果を設定する。
     * @param object コンパイル結果
     * @throws IllegalStateException すでにコンパイル済みであった場合
     * @throws IllegalArgumentException 引数に{@code null}が指定された場合
     * {@link #isCompiled()}
     */
    void setCompiled(T object);

    /**
     * {@link Compilable}の機能を提供するクラス。
     * @param <T> コンパイルした結果の情報
     */
    class Trait<T> implements Compilable<T> {

        private T compiled;

        @Override
        public boolean isCompiled() {
            return compiled != null;
        }

        @Override
        public T getCompiled() {
            if (isCompiled() == false) {
                throw new IllegalStateException();
            }
            return compiled;
        }

        @Override
        public void setCompiled(T object) {
            if (isCompiled()) {
                throw new IllegalStateException();
            }
            this.compiled = object;
        }
    }
}
