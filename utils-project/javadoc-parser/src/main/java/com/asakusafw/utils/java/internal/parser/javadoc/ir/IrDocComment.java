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
package com.asakusafw.utils.java.internal.parser.javadoc.ir;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Javadoc全体。
 */
public class IrDocComment extends AbstractIrDocElement {

    private static final long serialVersionUID = 1L;

    private List<? extends IrDocBlock> blocks;

    /**
     * インスタンスを生成する。
     * @throws IllegalArgumentException 引数に{@code null}が含まれていた場合
     */
    public IrDocComment() {
        super();
        this.blocks = Collections.emptyList();
    }

    @Override
    public IrDocElementKind getKind() {
        return IrDocElementKind.COMMENT;
    }

    /**
     * このコメントに含まれるブロックの一覧を返す。
     * @return このコメントに含まれるブロックの一覧
     */
    public List<? extends IrDocBlock> getBlocks() {
        return this.blocks;
    }

    /**
     * このコメントに含まれるブロックの一覧を設定する。
     * @param blocks このコメントに含まれるブロックの一覧
     * @throws IllegalArgumentException 引数に{@code null}が含まれていた場合
     */
    public void setBlocks(List<? extends IrDocBlock> blocks) {
        if (blocks == null) {
            throw new IllegalArgumentException("blocks"); //$NON-NLS-1$
        }
        this.blocks = Collections.unmodifiableList(
            new ArrayList<IrDocBlock>(blocks));
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((blocks == null) ? 0 : blocks.hashCode());
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
        final IrDocComment other = (IrDocComment) obj;
        if (blocks == null) {
            if (other.blocks != null) {
                return false;
            }
        } else if (!blocks.equals(other.blocks)) {
            return false;
        }
        return true;
    }

    @Override
    public String toString() {
        StringWriter sw = new StringWriter();
        PrintWriter w = new PrintWriter(sw);
        w.println("/**"); //$NON-NLS-1$
        for (IrDocBlock b: getBlocks()) {
            w.print(" * "); //$NON-NLS-1$
            w.print(b);
            w.println();
        }
        w.println(" */"); //$NON-NLS-1$
        w.flush();
        return sw.toString();
    }

    @Override
    public <R, P> R accept(IrDocElementVisitor<R, P> visitor, P context) {
        if (visitor == null) {
            throw new IllegalArgumentException("visitor"); //$NON-NLS-1$
        }
        return visitor.visitComment(this, context);
    }
}
