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
package com.asakusafw.utils.java.internal.parser.javadoc.ir;

/**
 * A visitor for {@link IrDocElement}.
 * @param <R> type of visitor result
 * @param <P> type of visitor context
 */
public abstract class IrDocElementVisitor<R, P> {

    /**
     * Processes {@link IrDocComment}.
     * @param elem the target element
     * @param context the current context
     * @return the processing result
     */
    public R visitComment(IrDocComment elem, P context) {
        return null;
    }

    /**
     * Processes {@link IrDocBlock}.
     * @param elem the target element
     * @param context the current context
     * @return the processing result
     */
    public R visitBlock(IrDocBlock elem, P context) {
        return null;
    }

    /**
     * Processes {@link IrDocSimpleName}.
     * @param elem the target element
     * @param context the current context
     * @return the processing result
     */
    public R visitSimpleName(IrDocSimpleName elem, P context) {
        return null;
    }

    /**
     * Processes {@link IrDocQualifiedName}.
     * @param elem the target element
     * @param context the current context
     * @return the processing result
     */
    public R visitQualifiedName(IrDocQualifiedName elem, P context) {
        return null;
    }

    /**
     * Processes {@link IrDocField}.
     * @param elem the target element
     * @param context the current context
     * @return the processing result
     */
    public R visitField(IrDocField elem, P context) {
        return null;
    }

    /**
     * Processes {@link IrDocMethod}.
     * @param elem the target element
     * @param context the current context
     * @return the processing result
     */
    public R visitMethod(IrDocMethod elem, P context) {
        return null;
    }

    /**
     * Processes {@link IrDocText}.
     * @param elem the target element
     * @param context the current context
     * @return the processing result
     */
    public R visitText(IrDocText elem, P context) {
        return null;
    }

    /**
     * Processes {@link IrDocMethodParameter}.
     * @param elem the target element
     * @param context the current context
     * @return the processing result
     */
    public R visitMethodParameter(IrDocMethodParameter elem, P context) {
        return null;
    }

    /**
     * Processes {@link IrDocBasicType}.
     * @param elem the target element
     * @param context the current context
     * @return the processing result
     */
    public R visitBasicType(IrDocBasicType elem, P context) {
        return null;
    }

    /**
     * Processes {@link IrDocNamedType}.
     * @param elem the target element
     * @param context the current context
     * @return the processing result
     */
    public R visitNamedType(IrDocNamedType elem, P context) {
        return null;
    }

    /**
     * Processes {@link IrDocArrayType}.
     * @param elem the target element
     * @param context the current context
     * @return the processing result
     */
    public R visitArrayType(IrDocArrayType elem, P context) {
        return null;
    }
}
