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
package com.asakusafw.compiler.flow.visualizer;

import java.util.UUID;

import com.asakusafw.compiler.common.Precondition;

/**
 * 可視化用のラベル。
 */
public class VisualLabel implements VisualNode {

    private final UUID id = UUID.randomUUID();

    private String label;

    /**
     * インスタンスを生成する。
     * @param label 対象のラベル、省略する場合は{@code null}
     * @throws IllegalArgumentException 引数に{@code null}が指定された場合
     */
    public VisualLabel(String label) {
        this.label = label;
    }

    @Override
    public Kind getKind() {
        return Kind.LABEL;
    }

    @Override
    public UUID getId() {
        return id;
    }

    /**
     * このラベルの文字列を返す。
     * @return このラベルの文字列、省略する場合は{@code null}
     */
    public String getLabel() {
        return label;
    }

    @Override
    public <R, C, E extends Throwable> R accept(VisualNodeVisitor<R, C, E> visitor, C context) throws E {
        Precondition.checkMustNotBeNull(visitor, "visitor"); //$NON-NLS-1$
        R result = visitor.visitLabel(context, this);
        return result;
    }
}
