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
package com.asakusafw.compiler.flow.visualizer;

/**
 * {@link VisualNode}を渡り歩くビジタ。
 * <p>
 * この実装では、すべてのメソッドが何も行わずに{@code null}を返す。
 * </p>
 * @param <R> 戻り値の型
 * @param <C> コンテキストオブジェクトの型
 * @param <E> 例外の型
 */
public abstract class VisualNodeVisitor<R, C, E extends Throwable> {

    /**
     * {@link VisualGraph#accept(VisualNodeVisitor, Object)}が呼び出された際にコールバックされる。
     * @param node コールバック元のが呼び出されたオブジェクト。
     * @param context コンテキストオブジェクト(省略可)
     * @return このビジタの実行結果
     * @throws E この処理中に例外が発生した場合
     */
    protected R visitGraph(C context, VisualGraph node) throws E {
        return null;
    }

    /**
     * {@link VisualBlock#accept(VisualNodeVisitor, Object)}が呼び出された際にコールバックされる。
     * @param node コールバック元のが呼び出されたオブジェクト。
     * @param context コンテキストオブジェクト(省略可)
     * @return このビジタの実行結果
     * @throws E この処理中に例外が発生した場合
     */
    protected R visitBlock(C context, VisualBlock node) throws E {
        return null;
    }

    /**
     * {@link VisualFlowPart#accept(VisualNodeVisitor, Object)}が呼び出された際にコールバックされる。
     * @param node コールバック元のが呼び出されたオブジェクト。
     * @param context コンテキストオブジェクト(省略可)
     * @return このビジタの実行結果
     * @throws E この処理中に例外が発生した場合
     */
    protected R visitFlowPart(C context, VisualFlowPart node) throws E {
        return null;
    }

    /**
     * {@link VisualElement#accept(VisualNodeVisitor, Object)}が呼び出された際にコールバックされる。
     * @param node コールバック元のが呼び出されたオブジェクト。
     * @param context コンテキストオブジェクト(省略可)
     * @return このビジタの実行結果
     * @throws E この処理中に例外が発生した場合
     */
    protected R visitElement(C context, VisualElement node) throws E {
        return null;
    }

    /**
     * {@link VisualLabel#accept(VisualNodeVisitor, Object)}が呼び出された際にコールバックされる。
     * @param node コールバック元のが呼び出されたオブジェクト。
     * @param context コンテキストオブジェクト(省略可)
     * @return このビジタの実行結果
     * @throws E この処理中に例外が発生した場合
     */
    protected R visitLabel(C context, VisualLabel node) throws E {
        return null;
    }
}
