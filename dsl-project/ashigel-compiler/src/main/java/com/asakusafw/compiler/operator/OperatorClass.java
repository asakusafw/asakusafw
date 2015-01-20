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
package com.asakusafw.compiler.operator;

import java.util.List;

import javax.lang.model.element.AnnotationMirror;
import javax.lang.model.element.ExecutableElement;
import javax.lang.model.element.TypeElement;

import com.asakusafw.compiler.common.Precondition;
import com.asakusafw.utils.collections.Lists;

/**
 * 演算子クラスの構造を表現する。
 * @since 0.1.0
 * @version 0.7.0
 */
public class OperatorClass {

    private final TypeElement element;

    private final List<OperatorMethod> methods;

    /**
     * インスタンスを生成する。
     * @param type この演算子クラスに対応するクラスの宣言
     * @throws IllegalArgumentException 引数に{@code null}が含まれる場合
     */
    public OperatorClass(TypeElement type) {
        Precondition.checkMustNotBeNull(type, "type"); //$NON-NLS-1$
        this.element = type;
        this.methods = Lists.create();
    }

    /**
     * この演算子クラスを表現するクラスを返す。
     * @return この演算子クラスを表現するクラス
     */
    public TypeElement getElement() {
        return this.element;
    }

    /**
     * Registers an operator method into this class.
     * @param annotation the operator annotation
     * @param methodElement the operator method
     * @param processor corresponded operator method
     */
    public void add(
            AnnotationMirror annotation,
            ExecutableElement methodElement,
            OperatorProcessor processor) {
        Precondition.checkMustNotBeNull(annotation, "annotation"); //$NON-NLS-1$
        Precondition.checkMustNotBeNull(methodElement, "methodElement"); //$NON-NLS-1$
        Precondition.checkMustNotBeNull(processor, "processor"); //$NON-NLS-1$
        if (element.equals(methodElement.getEnclosingElement()) == false) {
            throw new IllegalArgumentException("methodElement must be a member of this class"); //$NON-NLS-1$
        }
        OperatorMethod method = new OperatorMethod(annotation, methodElement, processor);
        methods.add(method);
    }

    /**
     * この演算子クラスに指定の演算子メソッドの情報を追加する。
     * @param methodElement 演算子メソッドのメソッド宣言
     * @param processor 対応する演算子プロセッサ
     * @throws IllegalArgumentException 引数に{@code null}が含まれる場合
     */
    public void add(
            ExecutableElement methodElement,
            OperatorProcessor processor) {
        Precondition.checkMustNotBeNull(methodElement, "methodElement"); //$NON-NLS-1$
        Precondition.checkMustNotBeNull(processor, "processor"); //$NON-NLS-1$
        if (element.equals(methodElement.getEnclosingElement()) == false) {
            throw new IllegalArgumentException("methodElement must be a member of this class"); //$NON-NLS-1$
        }
        OperatorMethod method = new OperatorMethod(methodElement, processor);
        methods.add(method);
    }

    /**
     * この演算子クラスに宣言された演算子メソッドの一覧を返す。
     * @return 演算子メソッドの一覧
     */
    public List<OperatorMethod> getMethods() {
        return methods;
    }
}
