/**
 * Copyright 2011-2013 Asakusa Framework Team.
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
package com.asakusafw.compiler.operator;

import javax.lang.model.element.AnnotationMirror;
import javax.lang.model.element.ExecutableElement;

import com.asakusafw.compiler.common.Precondition;

/**
 * 演算子メソッドの構造。
 */
public class OperatorMethod {

    private ExecutableElement element;

    private OperatorProcessor processor;

    /**
     * インスタンスを生成する。
     * @param element 演算子メソッドのメソッド宣言
     * @param processor 対応する演算子プロセッサ
     * @throws IllegalArgumentException 引数に{@code null}が含まれる場合
     */
    public OperatorMethod(ExecutableElement element, OperatorProcessor processor) {
        Precondition.checkMustNotBeNull(element, "element"); //$NON-NLS-1$
        Precondition.checkMustNotBeNull(processor, "processor"); //$NON-NLS-1$
        this.element = element;
        this.processor = processor;
    }

    /**
     * 演算子メソッドに付与された演算子注釈を返す。
     * @return 付与された演算子注釈
     */
    public AnnotationMirror getAnnotation() {
        return processor.getOperatorAnnotation(element);
    }

    /**
     * 演算子メソッドのメソッド宣言を返す。
     * @return 演算子メソッドのメソッド宣言
     */
    public ExecutableElement getElement() {
        return this.element;
    }

    /**
     * 対応する演算子プロセッサを返す。
     * @return 対応する演算子プロセッサ
     */
    public OperatorProcessor getProcessor() {
        return this.processor;
    }
}
