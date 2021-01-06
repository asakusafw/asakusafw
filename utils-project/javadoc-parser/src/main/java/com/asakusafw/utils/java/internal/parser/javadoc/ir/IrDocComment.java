/**
 * Copyright 2011-2021 Asakusa Framework Team.
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
 * Represents Java documentation comments.
 */
public class IrDocComment extends AbstractIrDocElement {

    private static final long serialVersionUID = 1L;

    private List<? extends IrDocBlock> blocks;

    /**
     * Creates a new instance.
     */
    public IrDocComment() {
        this.blocks = Collections.emptyList();
    }

    @Override
    public IrDocElementKind getKind() {
        return IrDocElementKind.COMMENT;
    }

    /**
     * Returns the element blocks.
     * @return the element blocks
     */
    public List<? extends IrDocBlock> getBlocks() {
        return this.blocks;
    }

    /**
     * Sets the element blocks.
     * @param blocks the element blocks
     * @throws IllegalArgumentException if the parameter is {@code null}
     */
    public void setBlocks(List<? extends IrDocBlock> blocks) {
        if (blocks == null) {
            throw new IllegalArgumentException("blocks"); //$NON-NLS-1$
        }
        this.blocks = Collections.unmodifiableList(new ArrayList<>(blocks));
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
        IrDocComment other = (IrDocComment) obj;
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
