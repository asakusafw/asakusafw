/**
 * Copyright 2011-2016 Asakusa Framework Team.
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
package com.asakusafw.dmdl.model;

import com.asakusafw.dmdl.Region;


/**
 * A top level interface of each AST model.
 * @since 0.2.0
 */
public interface AstNode {

    /**
     * Returns the region of this node.
     * @return the region, or {@code null} if unknown
     */
    Region getRegion();

    /**
     * Accepts and calls back the visitor.
     * @param <C> type of visitor context
     * @param <R> type of visitor result
     * @param context the visitor context
     * @param visitor the visitor to call back
     * @return call back result
     * @throws IllegalArgumentException if {@code visitor} was {@code null}
     */
    <C, R> R accept(C context, Visitor<C, R> visitor);

    /**
     * Visitor of {@link AstNode}.
     * @param <C> type of visitor context
     * @param <R> type of visitor result
     */
    public interface Visitor<C, R> {

        /**
         * Called back from {@link AstAttribute}.
         * @param context the context of this visitor
         * @param node the node invoked {@link AstNode#accept(Object, Visitor)}
         * @return the result
         */
        R visitAttribute(C context, AstAttribute node);

        /**
         * Called back from {@link AstAttributeElement}.
         * @param context the context of this visitor
         * @param node the node invoked {@link AstNode#accept(Object, Visitor)}
         * @return the result
         */
        R visitAttributeElement(C context, AstAttributeElement node);

        /**
         * Called back from {@link AstAttributeValueArray}.
         * @param context the context of this visitor
         * @param node the node invoked {@link AstNode#accept(Object, Visitor)}
         * @return the result
         */
        R visitAttributeValueArray(C context, AstAttributeValueArray node);

        /**
         * Called back from {@link AstBasicType}.
         * @param context the context of this visitor
         * @param node the node invoked {@link AstNode#accept(Object, Visitor)}
         * @return the result
         */
        R visitBasicType(C context, AstBasicType node);

        /**
         * Called back from {@link AstDescription}.
         * @param context the context of this visitor
         * @param node the node invoked {@link AstNode#accept(Object, Visitor)}
         * @return the result
         */
        R visitDescription(C context, AstDescription node);

        /**
         * Called back from {@link AstGrouping}.
         * @param context the context of this visitor
         * @param node the node invoked {@link AstNode#accept(Object, Visitor)}
         * @return the result
         */
        R visitGrouping(C context, AstGrouping node);

        /**
         * Called back from {@link AstJoin}.
         * @param context the context of this visitor
         * @param node the node invoked {@link AstNode#accept(Object, Visitor)}
         * @return the result
         */
        R visitJoin(C context, AstJoin node);

        /**
         * Called back from {@link AstLiteral}.
         * @param context the context of this visitor
         * @param node the node invoked {@link AstNode#accept(Object, Visitor)}
         * @return the result
         */
        R visitLiteral(C context, AstLiteral node);

        /**
         * Called back from {@link AstModelDefinition}.
         * @param <T> Type of target definition type
         * @param context the context of this visitor
         * @param node the node invoked {@link AstNode#accept(Object, Visitor)}
         * @return the result
         */
        <T extends AstTerm<T>> R visitModelDefinition(C context, AstModelDefinition<T> node);

        /**
         * Called back from {@link AstModelFolding}.
         * @param context the context of this visitor
         * @param node the node invoked {@link AstNode#accept(Object, Visitor)}
         * @return the result
         */
        R visitModelFolding(C context, AstModelFolding node);

        /**
         * Called back from {@link AstModelMapping}.
         * @param context the context of this visitor
         * @param node the node invoked {@link AstNode#accept(Object, Visitor)}
         * @return the result
         */
        R visitModelMapping(C context, AstModelMapping node);

        /**
         * Called back from {@link AstModelReference}.
         * @param context the context of this visitor
         * @param node the node invoked {@link AstNode#accept(Object, Visitor)}
         * @return the result
         */
        R visitModelReference(C context, AstModelReference node);

        /**
         * Called back from {@link AstPropertyDefinition}.
         * @param context the context of this visitor
         * @param node the node invoked {@link AstNode#accept(Object, Visitor)}
         * @return the result
         */
        R visitPropertyDefinition(C context, AstPropertyDefinition node);

        /**
         * Called back from {@link AstPropertyFolding}.
         * @param context the context of this visitor
         * @param node the node invoked {@link AstNode#accept(Object, Visitor)}
         * @return the result
         */
        R visitPropertyFolding(C context, AstPropertyFolding node);

        /**
         * Called back from {@link AstPropertyMapping}.
         * @param context the context of this visitor
         * @param node the node invoked {@link AstNode#accept(Object, Visitor)}
         * @return the result
         */
        R visitPropertyMapping(C context, AstPropertyMapping node);

        /**
         * Called back from {@link AstReferenceType}.
         * @param context the context of this visitor
         * @param node the node invoked {@link AstNode#accept(Object, Visitor)}
         * @return the result
         */
        R visitReferenceType(C context, AstReferenceType node);

        /**
         * Called back from {@link AstRecordDefinition}.
         * @param context the context of this visitor
         * @param node the node invoked {@link AstNode#accept(Object, Visitor)}
         * @return the result
         */
        R visitRecordDefinition(C context, AstRecordDefinition node);

        /**
         * Called back from {@link AstSequenceType}.
         * @param context the context of this visitor
         * @param node the node invoked {@link AstNode#accept(Object, Visitor)}
         * @return the result
         */
        R visitSequenceType(C context, AstSequenceType node);

        /**
         * Called back from {@link AstScript}.
         * @param context the context of this visitor
         * @param node the node invoked {@link AstNode#accept(Object, Visitor)}
         * @return the result
         */
        R visitScript(C context, AstScript node);

        /**
         * Called back from {@link AstSummarize}.
         * @param context the context of this visitor
         * @param node the node invoked {@link AstNode#accept(Object, Visitor)}
         * @return the result
         */
        R visitSummarize(C context, AstSummarize node);

        /**
         * Called back from {@link AstUnionExpression}.
         * @param <T> the target definition type
         * @param context the context of this visitor
         * @param node the node invoked {@link AstNode#accept(Object, Visitor)}
         * @return the result
         */
        <T extends AstTerm<T>> R visitUnionExpression(C context, AstUnionExpression<T> node);

        /**
         * Called back from {@link AstSimpleName}.
         * @param context the context of this visitor
         * @param node the node invoked {@link AstNode#accept(Object, Visitor)}
         * @return the result
         */
        R visitSimpleName(C context, AstSimpleName node);

        /**
         * Called back from {@link AstQualifiedName}.
         * @param context the context of this visitor
         * @param node the node invoked {@link AstNode#accept(Object, Visitor)}
         * @return the result
         */
        R visitQualifiedName(C context, AstQualifiedName node);
    }

    /**
     * Default implementation of {@link Visitor},
     * which all declared methods returns just {@code null}.
     * @param <C> type of visitor context
     * @param <R> type of visitor result
     */
    public abstract class AbstractVisitor<C, R> implements Visitor<C, R> {

        @Override
        public R visitAttribute(C context, AstAttribute node) {
            return null;
        }

        @Override
        public R visitAttributeElement(C context, AstAttributeElement node) {
            return null;
        }

        @Override
        public R visitAttributeValueArray(C context, AstAttributeValueArray node) {
            return null;
        }

        @Override
        public R visitBasicType(C context, AstBasicType node) {
            return null;
        }

        @Override
        public R visitDescription(C context, AstDescription node) {
            return null;
        }

        @Override
        public R visitGrouping(C context, AstGrouping node) {
            return null;
        }

        @Override
        public R visitJoin(C context, AstJoin node) {
            return null;
        }

        @Override
        public R visitLiteral(C context, AstLiteral node) {
            return null;
        }

        @Override
        public <T extends AstTerm<T>> R visitModelDefinition(C context, AstModelDefinition<T> node) {
            return null;
        }

        @Override
        public R visitModelFolding(C context, AstModelFolding node) {
            return null;
        }

        @Override
        public R visitModelMapping(C context, AstModelMapping node) {
            return null;
        }

        @Override
        public R visitModelReference(C context, AstModelReference node) {
            return null;
        }

        @Override
        public R visitPropertyDefinition(C context, AstPropertyDefinition node) {
            return null;
        }

        @Override
        public R visitPropertyFolding(C context, AstPropertyFolding node) {
            return null;
        }

        @Override
        public R visitPropertyMapping(C context, AstPropertyMapping node) {
            return null;
        }

        @Override
        public R visitRecordDefinition(C context, AstRecordDefinition node) {
            return null;
        }

        @Override
        public R visitReferenceType(C context, AstReferenceType node) {
            return null;
        }

        @Override
        public R visitSequenceType(C context, AstSequenceType node) {
            return null;
        }

        @Override
        public R visitScript(C context, AstScript node) {
            return null;
        }

        @Override
        public R visitSummarize(C context, AstSummarize node) {
            return null;
        }

        @Override
        public <T extends AstTerm<T>> R visitUnionExpression(C context, AstUnionExpression<T> node) {
            return null;
        }

        @Override
        public R visitSimpleName(C context, AstSimpleName node) {
            return null;
        }

        @Override
        public R visitQualifiedName(C context, AstQualifiedName node) {
            return null;
        }
    }
}
