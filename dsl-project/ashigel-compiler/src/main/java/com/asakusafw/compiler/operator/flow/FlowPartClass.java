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
package com.asakusafw.compiler.operator.flow;

import java.util.List;

import javax.lang.model.element.TypeElement;

import com.asakusafw.compiler.common.Precondition;
import com.asakusafw.compiler.operator.OperatorPortDeclaration;
import com.asakusafw.utils.collections.Lists;
import com.asakusafw.utils.java.model.syntax.DocElement;

/**
 * フロー部品クラスの情報。
 */
public class FlowPartClass {

    private TypeElement element;

    private List<DocElement> documentation;

    private List<OperatorPortDeclaration> inputPorts;

    private List<OperatorPortDeclaration> outputPorts;

    private List<OperatorPortDeclaration> parameters;

    /**
     * インスタンスを生成する。
     * @param element このフロー部品クラスの宣言
     * @param documentation フロー部品全体のドキュメンテーション
     * @param inputPorts 入力ポート宣言の一覧
     * @param outputPorts 出力ポート宣言の一覧
     * @param parameters パラメーター宣言の一覧
     * @throws IllegalArgumentException 引数に{@code null}が指定された場合
     */
    public FlowPartClass(
            TypeElement element,
            List<? extends DocElement> documentation,
            List<OperatorPortDeclaration> inputPorts,
            List<OperatorPortDeclaration> outputPorts,
            List<OperatorPortDeclaration> parameters) {
        Precondition.checkMustNotBeNull(element, "element"); //$NON-NLS-1$
        Precondition.checkMustNotBeNull(documentation, "documentation"); //$NON-NLS-1$
        Precondition.checkMustNotBeNull(inputPorts, "inputPorts"); //$NON-NLS-1$
        Precondition.checkMustNotBeNull(outputPorts, "outputPorts"); //$NON-NLS-1$
        Precondition.checkMustNotBeNull(parameters, "parameters"); //$NON-NLS-1$
        this.element = element;
        this.documentation = Lists.from(documentation);
        this.inputPorts = Lists.from(inputPorts);
        this.outputPorts = Lists.from(outputPorts);
        this.parameters = Lists.from(parameters);
    }

    /**
     * このフロー部品クラスを表現するクラスを返す。
     * @return フロー部品クラスを表現するクラス
     */
    public TypeElement getElement() {
        return element;
    }

    /**
     * このフロー部品のドキュメンテーションを返す。
     * @return このフロー部品のドキュメンテーション
     */
    public List<DocElement> getDocumentation() {
        return documentation;
    }

    /**
     * このフロー部品の入力ポート宣言を返す。
     * @return ポート宣言の一覧
     */
    public List<OperatorPortDeclaration> getInputPorts() {
        return inputPorts;
    }

    /**
     * このフロー部品の出力ポート宣言を返す。
     * @return ポート宣言の一覧
     */
    public List<OperatorPortDeclaration> getOutputPorts() {
        return outputPorts;
    }

    /**
     * このフロー部品のパラメーター宣言を返す。
     * @return パラメーター宣言
     */
    public List<OperatorPortDeclaration> getParameters() {
        return parameters;
    }
}
