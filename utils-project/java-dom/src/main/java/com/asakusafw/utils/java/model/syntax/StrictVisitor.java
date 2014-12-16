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
package com.asakusafw.utils.java.model.syntax;

/**
 * {@link Model}を渡り歩くビジタ。
 * <p>
 * この実装では、すべてのメソッド{@link UnsupportedOperationException}をスローする。
 * </p>
 * @param <R> 戻り値の型
 * @param <C> コンテキストオブジェクトの型
 * @param <E> 例外の型
 */
public abstract class StrictVisitor<R, C, E extends Throwable>
        extends Visitor<R, C, E> {

    /**
     * {@link AlternateConstructorInvocation#accept(Visitor,Object)}
     * が呼び出された際にコールバックされる。
     * @param elem
     *     {@link AlternateConstructorInvocation#accept(Visitor,Object)}
     *     が呼び出されたオブジェクト。
     * @param context コンテキストオブジェクト(省略可)
     * @return このビジタの実行結果
     * @throws E この処理中に例外が発生した場合
     */
    @Override
    public R visitAlternateConstructorInvocation(
            AlternateConstructorInvocation elem,
            C context) throws E {
        throw new UnsupportedOperationException("AlternateConstructorInvocation"); //$NON-NLS-1$
    }

    /**
     * {@link AnnotationDeclaration#accept(Visitor,Object)}
     * が呼び出された際にコールバックされる。
     * @param elem
     *     {@link AnnotationDeclaration#accept(Visitor,Object)}
     *     が呼び出されたオブジェクト。
     * @param context コンテキストオブジェクト(省略可)
     * @return このビジタの実行結果
     * @throws E この処理中に例外が発生した場合
     */
    @Override
    public R visitAnnotationDeclaration(
            AnnotationDeclaration elem,
            C context) throws E {
        throw new UnsupportedOperationException("AnnotationDeclaration"); //$NON-NLS-1$
    }

    /**
     * {@link AnnotationElement#accept(Visitor,Object)}
     * が呼び出された際にコールバックされる。
     * @param elem
     *     {@link AnnotationElement#accept(Visitor,Object)}
     *     が呼び出されたオブジェクト。
     * @param context コンテキストオブジェクト(省略可)
     * @return このビジタの実行結果
     * @throws E この処理中に例外が発生した場合
     */
    @Override
    public R visitAnnotationElement(
            AnnotationElement elem,
            C context) throws E {
        throw new UnsupportedOperationException("AnnotationElement"); //$NON-NLS-1$
    }

    /**
     * {@link AnnotationElementDeclaration#accept(Visitor,Object)}
     * が呼び出された際にコールバックされる。
     * @param elem
     *     {@link AnnotationElementDeclaration#accept(Visitor,Object)}
     *     が呼び出されたオブジェクト。
     * @param context コンテキストオブジェクト(省略可)
     * @return このビジタの実行結果
     * @throws E この処理中に例外が発生した場合
     */
    @Override
    public R visitAnnotationElementDeclaration(
            AnnotationElementDeclaration elem,
            C context) throws E {
        throw new UnsupportedOperationException("AnnotationElementDeclaration"); //$NON-NLS-1$
    }

    /**
     * {@link ArrayAccessExpression#accept(Visitor,Object)}
     * が呼び出された際にコールバックされる。
     * @param elem
     *     {@link ArrayAccessExpression#accept(Visitor,Object)}
     *     が呼び出されたオブジェクト。
     * @param context コンテキストオブジェクト(省略可)
     * @return このビジタの実行結果
     * @throws E この処理中に例外が発生した場合
     */
    @Override
    public R visitArrayAccessExpression(
            ArrayAccessExpression elem,
            C context) throws E {
        throw new UnsupportedOperationException("ArrayAccessExpression"); //$NON-NLS-1$
    }

    /**
     * {@link ArrayCreationExpression#accept(Visitor,Object)}
     * が呼び出された際にコールバックされる。
     * @param elem
     *     {@link ArrayCreationExpression#accept(Visitor,Object)}
     *     が呼び出されたオブジェクト。
     * @param context コンテキストオブジェクト(省略可)
     * @return このビジタの実行結果
     * @throws E この処理中に例外が発生した場合
     */
    @Override
    public R visitArrayCreationExpression(
            ArrayCreationExpression elem,
            C context) throws E {
        throw new UnsupportedOperationException("ArrayCreationExpression"); //$NON-NLS-1$
    }

    /**
     * {@link ArrayInitializer#accept(Visitor,Object)}
     * が呼び出された際にコールバックされる。
     * @param elem
     *     {@link ArrayInitializer#accept(Visitor,Object)}
     *     が呼び出されたオブジェクト。
     * @param context コンテキストオブジェクト(省略可)
     * @return このビジタの実行結果
     * @throws E この処理中に例外が発生した場合
     */
    @Override
    public R visitArrayInitializer(
            ArrayInitializer elem,
            C context) throws E {
        throw new UnsupportedOperationException("ArrayInitializer"); //$NON-NLS-1$
    }

    /**
     * {@link ArrayType#accept(Visitor,Object)}
     * が呼び出された際にコールバックされる。
     * @param elem
     *     {@link ArrayType#accept(Visitor,Object)}
     *     が呼び出されたオブジェクト。
     * @param context コンテキストオブジェクト(省略可)
     * @return このビジタの実行結果
     * @throws E この処理中に例外が発生した場合
     */
    @Override
    public R visitArrayType(
            ArrayType elem,
            C context) throws E {
        throw new UnsupportedOperationException("ArrayType"); //$NON-NLS-1$
    }

    /**
     * {@link AssertStatement#accept(Visitor,Object)}
     * が呼び出された際にコールバックされる。
     * @param elem
     *     {@link AssertStatement#accept(Visitor,Object)}
     *     が呼び出されたオブジェクト。
     * @param context コンテキストオブジェクト(省略可)
     * @return このビジタの実行結果
     * @throws E この処理中に例外が発生した場合
     */
    @Override
    public R visitAssertStatement(
            AssertStatement elem,
            C context) throws E {
        throw new UnsupportedOperationException("AssertStatement"); //$NON-NLS-1$
    }

    /**
     * {@link AssignmentExpression#accept(Visitor,Object)}
     * が呼び出された際にコールバックされる。
     * @param elem
     *     {@link AssignmentExpression#accept(Visitor,Object)}
     *     が呼び出されたオブジェクト。
     * @param context コンテキストオブジェクト(省略可)
     * @return このビジタの実行結果
     * @throws E この処理中に例外が発生した場合
     */
    @Override
    public R visitAssignmentExpression(
            AssignmentExpression elem,
            C context) throws E {
        throw new UnsupportedOperationException("AssignmentExpression"); //$NON-NLS-1$
    }

    /**
     * {@link BasicType#accept(Visitor,Object)}
     * が呼び出された際にコールバックされる。
     * @param elem
     *     {@link BasicType#accept(Visitor,Object)}
     *     が呼び出されたオブジェクト。
     * @param context コンテキストオブジェクト(省略可)
     * @return このビジタの実行結果
     * @throws E この処理中に例外が発生した場合
     */
    @Override
    public R visitBasicType(
            BasicType elem,
            C context) throws E {
        throw new UnsupportedOperationException("BasicType"); //$NON-NLS-1$
    }

    /**
     * {@link Block#accept(Visitor,Object)}
     * が呼び出された際にコールバックされる。
     * @param elem
     *     {@link Block#accept(Visitor,Object)}
     *     が呼び出されたオブジェクト。
     * @param context コンテキストオブジェクト(省略可)
     * @return このビジタの実行結果
     * @throws E この処理中に例外が発生した場合
     */
    @Override
    public R visitBlock(
            Block elem,
            C context) throws E {
        throw new UnsupportedOperationException("Block"); //$NON-NLS-1$
    }

    /**
     * {@link BlockComment#accept(Visitor,Object)}
     * が呼び出された際にコールバックされる。
     * @param elem
     *     {@link BlockComment#accept(Visitor,Object)}
     *     が呼び出されたオブジェクト。
     * @param context コンテキストオブジェクト(省略可)
     * @return このビジタの実行結果
     * @throws E この処理中に例外が発生した場合
     */
    @Override
    public R visitBlockComment(
            BlockComment elem,
            C context) throws E {
        throw new UnsupportedOperationException("BlockComment"); //$NON-NLS-1$
    }

    /**
     * {@link BreakStatement#accept(Visitor,Object)}
     * が呼び出された際にコールバックされる。
     * @param elem
     *     {@link BreakStatement#accept(Visitor,Object)}
     *     が呼び出されたオブジェクト。
     * @param context コンテキストオブジェクト(省略可)
     * @return このビジタの実行結果
     * @throws E この処理中に例外が発生した場合
     */
    @Override
    public R visitBreakStatement(
            BreakStatement elem,
            C context) throws E {
        throw new UnsupportedOperationException("BreakStatement"); //$NON-NLS-1$
    }

    /**
     * {@link CastExpression#accept(Visitor,Object)}
     * が呼び出された際にコールバックされる。
     * @param elem
     *     {@link CastExpression#accept(Visitor,Object)}
     *     が呼び出されたオブジェクト。
     * @param context コンテキストオブジェクト(省略可)
     * @return このビジタの実行結果
     * @throws E この処理中に例外が発生した場合
     */
    @Override
    public R visitCastExpression(
            CastExpression elem,
            C context) throws E {
        throw new UnsupportedOperationException("CastExpression"); //$NON-NLS-1$
    }

    /**
     * {@link CatchClause#accept(Visitor,Object)}
     * が呼び出された際にコールバックされる。
     * @param elem
     *     {@link CatchClause#accept(Visitor,Object)}
     *     が呼び出されたオブジェクト。
     * @param context コンテキストオブジェクト(省略可)
     * @return このビジタの実行結果
     * @throws E この処理中に例外が発生した場合
     */
    @Override
    public R visitCatchClause(
            CatchClause elem,
            C context) throws E {
        throw new UnsupportedOperationException("CatchClause"); //$NON-NLS-1$
    }

    /**
     * {@link ClassBody#accept(Visitor,Object)}
     * が呼び出された際にコールバックされる。
     * @param elem
     *     {@link ClassBody#accept(Visitor,Object)}
     *     が呼び出されたオブジェクト。
     * @param context コンテキストオブジェクト(省略可)
     * @return このビジタの実行結果
     * @throws E この処理中に例外が発生した場合
     */
    @Override
    public R visitClassBody(
            ClassBody elem,
            C context) throws E {
        throw new UnsupportedOperationException("ClassBody"); //$NON-NLS-1$
    }

    /**
     * {@link ClassDeclaration#accept(Visitor,Object)}
     * が呼び出された際にコールバックされる。
     * @param elem
     *     {@link ClassDeclaration#accept(Visitor,Object)}
     *     が呼び出されたオブジェクト。
     * @param context コンテキストオブジェクト(省略可)
     * @return このビジタの実行結果
     * @throws E この処理中に例外が発生した場合
     */
    @Override
    public R visitClassDeclaration(
            ClassDeclaration elem,
            C context) throws E {
        throw new UnsupportedOperationException("ClassDeclaration"); //$NON-NLS-1$
    }

    /**
     * {@link ClassInstanceCreationExpression#accept(Visitor,Object)}
     * が呼び出された際にコールバックされる。
     * @param elem
     *     {@link ClassInstanceCreationExpression#accept(Visitor,Object)}
     *     が呼び出されたオブジェクト。
     * @param context コンテキストオブジェクト(省略可)
     * @return このビジタの実行結果
     * @throws E この処理中に例外が発生した場合
     */
    @Override
    public R visitClassInstanceCreationExpression(
            ClassInstanceCreationExpression elem,
            C context) throws E {
        throw new UnsupportedOperationException("ClassInstanceCreationExpression"); //$NON-NLS-1$
    }

    /**
     * {@link ClassLiteral#accept(Visitor,Object)}
     * が呼び出された際にコールバックされる。
     * @param elem
     *     {@link ClassLiteral#accept(Visitor,Object)}
     *     が呼び出されたオブジェクト。
     * @param context コンテキストオブジェクト(省略可)
     * @return このビジタの実行結果
     * @throws E この処理中に例外が発生した場合
     */
    @Override
    public R visitClassLiteral(
            ClassLiteral elem,
            C context) throws E {
        throw new UnsupportedOperationException("ClassLiteral"); //$NON-NLS-1$
    }

    /**
     * {@link CompilationUnit#accept(Visitor,Object)}
     * が呼び出された際にコールバックされる。
     * @param elem
     *     {@link CompilationUnit#accept(Visitor,Object)}
     *     が呼び出されたオブジェクト。
     * @param context コンテキストオブジェクト(省略可)
     * @return このビジタの実行結果
     * @throws E この処理中に例外が発生した場合
     */
    @Override
    public R visitCompilationUnit(
            CompilationUnit elem,
            C context) throws E {
        throw new UnsupportedOperationException("CompilationUnit"); //$NON-NLS-1$
    }

    /**
     * {@link ConditionalExpression#accept(Visitor,Object)}
     * が呼び出された際にコールバックされる。
     * @param elem
     *     {@link ConditionalExpression#accept(Visitor,Object)}
     *     が呼び出されたオブジェクト。
     * @param context コンテキストオブジェクト(省略可)
     * @return このビジタの実行結果
     * @throws E この処理中に例外が発生した場合
     */
    @Override
    public R visitConditionalExpression(
            ConditionalExpression elem,
            C context) throws E {
        throw new UnsupportedOperationException("ConditionalExpression"); //$NON-NLS-1$
    }

    /**
     * {@link ConstructorDeclaration#accept(Visitor,Object)}
     * が呼び出された際にコールバックされる。
     * @param elem
     *     {@link ConstructorDeclaration#accept(Visitor,Object)}
     *     が呼び出されたオブジェクト。
     * @param context コンテキストオブジェクト(省略可)
     * @return このビジタの実行結果
     * @throws E この処理中に例外が発生した場合
     */
    @Override
    public R visitConstructorDeclaration(
            ConstructorDeclaration elem,
            C context) throws E {
        throw new UnsupportedOperationException("ConstructorDeclaration"); //$NON-NLS-1$
    }

    /**
     * {@link ContinueStatement#accept(Visitor,Object)}
     * が呼び出された際にコールバックされる。
     * @param elem
     *     {@link ContinueStatement#accept(Visitor,Object)}
     *     が呼び出されたオブジェクト。
     * @param context コンテキストオブジェクト(省略可)
     * @return このビジタの実行結果
     * @throws E この処理中に例外が発生した場合
     */
    @Override
    public R visitContinueStatement(
            ContinueStatement elem,
            C context) throws E {
        throw new UnsupportedOperationException("ContinueStatement"); //$NON-NLS-1$
    }

    /**
     * {@link DoStatement#accept(Visitor,Object)}
     * が呼び出された際にコールバックされる。
     * @param elem
     *     {@link DoStatement#accept(Visitor,Object)}
     *     が呼び出されたオブジェクト。
     * @param context コンテキストオブジェクト(省略可)
     * @return このビジタの実行結果
     * @throws E この処理中に例外が発生した場合
     */
    @Override
    public R visitDoStatement(
            DoStatement elem,
            C context) throws E {
        throw new UnsupportedOperationException("DoStatement"); //$NON-NLS-1$
    }

    /**
     * {@link DocBlock#accept(Visitor,Object)}
     * が呼び出された際にコールバックされる。
     * @param elem
     *     {@link DocBlock#accept(Visitor,Object)}
     *     が呼び出されたオブジェクト。
     * @param context コンテキストオブジェクト(省略可)
     * @return このビジタの実行結果
     * @throws E この処理中に例外が発生した場合
     */
    @Override
    public R visitDocBlock(
            DocBlock elem,
            C context) throws E {
        throw new UnsupportedOperationException("DocBlock"); //$NON-NLS-1$
    }

    /**
     * {@link DocField#accept(Visitor,Object)}
     * が呼び出された際にコールバックされる。
     * @param elem
     *     {@link DocField#accept(Visitor,Object)}
     *     が呼び出されたオブジェクト。
     * @param context コンテキストオブジェクト(省略可)
     * @return このビジタの実行結果
     * @throws E この処理中に例外が発生した場合
     */
    @Override
    public R visitDocField(
            DocField elem,
            C context) throws E {
        throw new UnsupportedOperationException("DocField"); //$NON-NLS-1$
    }

    /**
     * {@link DocMethod#accept(Visitor,Object)}
     * が呼び出された際にコールバックされる。
     * @param elem
     *     {@link DocMethod#accept(Visitor,Object)}
     *     が呼び出されたオブジェクト。
     * @param context コンテキストオブジェクト(省略可)
     * @return このビジタの実行結果
     * @throws E この処理中に例外が発生した場合
     */
    @Override
    public R visitDocMethod(
            DocMethod elem,
            C context) throws E {
        throw new UnsupportedOperationException("DocMethod"); //$NON-NLS-1$
    }

    /**
     * {@link DocMethodParameter#accept(Visitor,Object)}
     * が呼び出された際にコールバックされる。
     * @param elem
     *     {@link DocMethodParameter#accept(Visitor,Object)}
     *     が呼び出されたオブジェクト。
     * @param context コンテキストオブジェクト(省略可)
     * @return このビジタの実行結果
     * @throws E この処理中に例外が発生した場合
     */
    @Override
    public R visitDocMethodParameter(
            DocMethodParameter elem,
            C context) throws E {
        throw new UnsupportedOperationException("DocMethodParameter"); //$NON-NLS-1$
    }

    /**
     * {@link DocText#accept(Visitor,Object)}
     * が呼び出された際にコールバックされる。
     * @param elem
     *     {@link DocText#accept(Visitor,Object)}
     *     が呼び出されたオブジェクト。
     * @param context コンテキストオブジェクト(省略可)
     * @return このビジタの実行結果
     * @throws E この処理中に例外が発生した場合
     */
    @Override
    public R visitDocText(
            DocText elem,
            C context) throws E {
        throw new UnsupportedOperationException("DocText"); //$NON-NLS-1$
    }

    /**
     * {@link EmptyStatement#accept(Visitor,Object)}
     * が呼び出された際にコールバックされる。
     * @param elem
     *     {@link EmptyStatement#accept(Visitor,Object)}
     *     が呼び出されたオブジェクト。
     * @param context コンテキストオブジェクト(省略可)
     * @return このビジタの実行結果
     * @throws E この処理中に例外が発生した場合
     */
    @Override
    public R visitEmptyStatement(
            EmptyStatement elem,
            C context) throws E {
        throw new UnsupportedOperationException("EmptyStatement"); //$NON-NLS-1$
    }

    /**
     * {@link EnhancedForStatement#accept(Visitor,Object)}
     * が呼び出された際にコールバックされる。
     * @param elem
     *     {@link EnhancedForStatement#accept(Visitor,Object)}
     *     が呼び出されたオブジェクト。
     * @param context コンテキストオブジェクト(省略可)
     * @return このビジタの実行結果
     * @throws E この処理中に例外が発生した場合
     */
    @Override
    public R visitEnhancedForStatement(
            EnhancedForStatement elem,
            C context) throws E {
        throw new UnsupportedOperationException("EnhancedForStatement"); //$NON-NLS-1$
    }

    /**
     * {@link EnumConstantDeclaration#accept(Visitor,Object)}
     * が呼び出された際にコールバックされる。
     * @param elem
     *     {@link EnumConstantDeclaration#accept(Visitor,Object)}
     *     が呼び出されたオブジェクト。
     * @param context コンテキストオブジェクト(省略可)
     * @return このビジタの実行結果
     * @throws E この処理中に例外が発生した場合
     */
    @Override
    public R visitEnumConstantDeclaration(
            EnumConstantDeclaration elem,
            C context) throws E {
        throw new UnsupportedOperationException("EnumConstantDeclaration"); //$NON-NLS-1$
    }

    /**
     * {@link EnumDeclaration#accept(Visitor,Object)}
     * が呼び出された際にコールバックされる。
     * @param elem
     *     {@link EnumDeclaration#accept(Visitor,Object)}
     *     が呼び出されたオブジェクト。
     * @param context コンテキストオブジェクト(省略可)
     * @return このビジタの実行結果
     * @throws E この処理中に例外が発生した場合
     */
    @Override
    public R visitEnumDeclaration(
            EnumDeclaration elem,
            C context) throws E {
        throw new UnsupportedOperationException("EnumDeclaration"); //$NON-NLS-1$
    }

    /**
     * {@link ExpressionStatement#accept(Visitor,Object)}
     * が呼び出された際にコールバックされる。
     * @param elem
     *     {@link ExpressionStatement#accept(Visitor,Object)}
     *     が呼び出されたオブジェクト。
     * @param context コンテキストオブジェクト(省略可)
     * @return このビジタの実行結果
     * @throws E この処理中に例外が発生した場合
     */
    @Override
    public R visitExpressionStatement(
            ExpressionStatement elem,
            C context) throws E {
        throw new UnsupportedOperationException("ExpressionStatement"); //$NON-NLS-1$
    }

    /**
     * {@link FieldAccessExpression#accept(Visitor,Object)}
     * が呼び出された際にコールバックされる。
     * @param elem
     *     {@link FieldAccessExpression#accept(Visitor,Object)}
     *     が呼び出されたオブジェクト。
     * @param context コンテキストオブジェクト(省略可)
     * @return このビジタの実行結果
     * @throws E この処理中に例外が発生した場合
     */
    @Override
    public R visitFieldAccessExpression(
            FieldAccessExpression elem,
            C context) throws E {
        throw new UnsupportedOperationException("FieldAccessExpression"); //$NON-NLS-1$
    }

    /**
     * {@link FieldDeclaration#accept(Visitor,Object)}
     * が呼び出された際にコールバックされる。
     * @param elem
     *     {@link FieldDeclaration#accept(Visitor,Object)}
     *     が呼び出されたオブジェクト。
     * @param context コンテキストオブジェクト(省略可)
     * @return このビジタの実行結果
     * @throws E この処理中に例外が発生した場合
     */
    @Override
    public R visitFieldDeclaration(
            FieldDeclaration elem,
            C context) throws E {
        throw new UnsupportedOperationException("FieldDeclaration"); //$NON-NLS-1$
    }

    /**
     * {@link ForStatement#accept(Visitor,Object)}
     * が呼び出された際にコールバックされる。
     * @param elem
     *     {@link ForStatement#accept(Visitor,Object)}
     *     が呼び出されたオブジェクト。
     * @param context コンテキストオブジェクト(省略可)
     * @return このビジタの実行結果
     * @throws E この処理中に例外が発生した場合
     */
    @Override
    public R visitForStatement(
            ForStatement elem,
            C context) throws E {
        throw new UnsupportedOperationException("ForStatement"); //$NON-NLS-1$
    }

    /**
     * {@link FormalParameterDeclaration#accept(Visitor,Object)}
     * が呼び出された際にコールバックされる。
     * @param elem
     *     {@link FormalParameterDeclaration#accept(Visitor,Object)}
     *     が呼び出されたオブジェクト。
     * @param context コンテキストオブジェクト(省略可)
     * @return このビジタの実行結果
     * @throws E この処理中に例外が発生した場合
     */
    @Override
    public R visitFormalParameterDeclaration(
            FormalParameterDeclaration elem,
            C context) throws E {
        throw new UnsupportedOperationException("FormalParameterDeclaration"); //$NON-NLS-1$
    }

    /**
     * {@link IfStatement#accept(Visitor,Object)}
     * が呼び出された際にコールバックされる。
     * @param elem
     *     {@link IfStatement#accept(Visitor,Object)}
     *     が呼び出されたオブジェクト。
     * @param context コンテキストオブジェクト(省略可)
     * @return このビジタの実行結果
     * @throws E この処理中に例外が発生した場合
     */
    @Override
    public R visitIfStatement(
            IfStatement elem,
            C context) throws E {
        throw new UnsupportedOperationException("IfStatement"); //$NON-NLS-1$
    }

    /**
     * {@link ImportDeclaration#accept(Visitor,Object)}
     * が呼び出された際にコールバックされる。
     * @param elem
     *     {@link ImportDeclaration#accept(Visitor,Object)}
     *     が呼び出されたオブジェクト。
     * @param context コンテキストオブジェクト(省略可)
     * @return このビジタの実行結果
     * @throws E この処理中に例外が発生した場合
     */
    @Override
    public R visitImportDeclaration(
            ImportDeclaration elem,
            C context) throws E {
        throw new UnsupportedOperationException("ImportDeclaration"); //$NON-NLS-1$
    }

    /**
     * {@link InfixExpression#accept(Visitor,Object)}
     * が呼び出された際にコールバックされる。
     * @param elem
     *     {@link InfixExpression#accept(Visitor,Object)}
     *     が呼び出されたオブジェクト。
     * @param context コンテキストオブジェクト(省略可)
     * @return このビジタの実行結果
     * @throws E この処理中に例外が発生した場合
     */
    @Override
    public R visitInfixExpression(
            InfixExpression elem,
            C context) throws E {
        throw new UnsupportedOperationException("InfixExpression"); //$NON-NLS-1$
    }

    /**
     * {@link InitializerDeclaration#accept(Visitor,Object)}
     * が呼び出された際にコールバックされる。
     * @param elem
     *     {@link InitializerDeclaration#accept(Visitor,Object)}
     *     が呼び出されたオブジェクト。
     * @param context コンテキストオブジェクト(省略可)
     * @return このビジタの実行結果
     * @throws E この処理中に例外が発生した場合
     */
    @Override
    public R visitInitializerDeclaration(
            InitializerDeclaration elem,
            C context) throws E {
        throw new UnsupportedOperationException("InitializerDeclaration"); //$NON-NLS-1$
    }

    /**
     * {@link InstanceofExpression#accept(Visitor,Object)}
     * が呼び出された際にコールバックされる。
     * @param elem
     *     {@link InstanceofExpression#accept(Visitor,Object)}
     *     が呼び出されたオブジェクト。
     * @param context コンテキストオブジェクト(省略可)
     * @return このビジタの実行結果
     * @throws E この処理中に例外が発生した場合
     */
    @Override
    public R visitInstanceofExpression(
            InstanceofExpression elem,
            C context) throws E {
        throw new UnsupportedOperationException("InstanceofExpression"); //$NON-NLS-1$
    }

    /**
     * {@link InterfaceDeclaration#accept(Visitor,Object)}
     * が呼び出された際にコールバックされる。
     * @param elem
     *     {@link InterfaceDeclaration#accept(Visitor,Object)}
     *     が呼び出されたオブジェクト。
     * @param context コンテキストオブジェクト(省略可)
     * @return このビジタの実行結果
     * @throws E この処理中に例外が発生した場合
     */
    @Override
    public R visitInterfaceDeclaration(
            InterfaceDeclaration elem,
            C context) throws E {
        throw new UnsupportedOperationException("InterfaceDeclaration"); //$NON-NLS-1$
    }

    /**
     * {@link Javadoc#accept(Visitor,Object)}
     * が呼び出された際にコールバックされる。
     * @param elem
     *     {@link Javadoc#accept(Visitor,Object)}
     *     が呼び出されたオブジェクト。
     * @param context コンテキストオブジェクト(省略可)
     * @return このビジタの実行結果
     * @throws E この処理中に例外が発生した場合
     */
    @Override
    public R visitJavadoc(
            Javadoc elem,
            C context) throws E {
        throw new UnsupportedOperationException("Javadoc"); //$NON-NLS-1$
    }

    /**
     * {@link LabeledStatement#accept(Visitor,Object)}
     * が呼び出された際にコールバックされる。
     * @param elem
     *     {@link LabeledStatement#accept(Visitor,Object)}
     *     が呼び出されたオブジェクト。
     * @param context コンテキストオブジェクト(省略可)
     * @return このビジタの実行結果
     * @throws E この処理中に例外が発生した場合
     */
    @Override
    public R visitLabeledStatement(
            LabeledStatement elem,
            C context) throws E {
        throw new UnsupportedOperationException("LabeledStatement"); //$NON-NLS-1$
    }

    /**
     * {@link LineComment#accept(Visitor,Object)}
     * が呼び出された際にコールバックされる。
     * @param elem
     *     {@link LineComment#accept(Visitor,Object)}
     *     が呼び出されたオブジェクト。
     * @param context コンテキストオブジェクト(省略可)
     * @return このビジタの実行結果
     * @throws E この処理中に例外が発生した場合
     */
    @Override
    public R visitLineComment(
            LineComment elem,
            C context) throws E {
        throw new UnsupportedOperationException("LineComment"); //$NON-NLS-1$
    }

    /**
     * {@link Literal#accept(Visitor,Object)}
     * が呼び出された際にコールバックされる。
     * @param elem
     *     {@link Literal#accept(Visitor,Object)}
     *     が呼び出されたオブジェクト。
     * @param context コンテキストオブジェクト(省略可)
     * @return このビジタの実行結果
     * @throws E この処理中に例外が発生した場合
     */
    @Override
    public R visitLiteral(
            Literal elem,
            C context) throws E {
        throw new UnsupportedOperationException("Literal"); //$NON-NLS-1$
    }

    /**
     * {@link LocalClassDeclaration#accept(Visitor,Object)}
     * が呼び出された際にコールバックされる。
     * @param elem
     *     {@link LocalClassDeclaration#accept(Visitor,Object)}
     *     が呼び出されたオブジェクト。
     * @param context コンテキストオブジェクト(省略可)
     * @return このビジタの実行結果
     * @throws E この処理中に例外が発生した場合
     */
    @Override
    public R visitLocalClassDeclaration(
            LocalClassDeclaration elem,
            C context) throws E {
        throw new UnsupportedOperationException("LocalClassDeclaration"); //$NON-NLS-1$
    }

    /**
     * {@link LocalVariableDeclaration#accept(Visitor,Object)}
     * が呼び出された際にコールバックされる。
     * @param elem
     *     {@link LocalVariableDeclaration#accept(Visitor,Object)}
     *     が呼び出されたオブジェクト。
     * @param context コンテキストオブジェクト(省略可)
     * @return このビジタの実行結果
     * @throws E この処理中に例外が発生した場合
     */
    @Override
    public R visitLocalVariableDeclaration(
            LocalVariableDeclaration elem,
            C context) throws E {
        throw new UnsupportedOperationException("LocalVariableDeclaration"); //$NON-NLS-1$
    }

    /**
     * {@link MarkerAnnotation#accept(Visitor,Object)}
     * が呼び出された際にコールバックされる。
     * @param elem
     *     {@link MarkerAnnotation#accept(Visitor,Object)}
     *     が呼び出されたオブジェクト。
     * @param context コンテキストオブジェクト(省略可)
     * @return このビジタの実行結果
     * @throws E この処理中に例外が発生した場合
     */
    @Override
    public R visitMarkerAnnotation(
            MarkerAnnotation elem,
            C context) throws E {
        throw new UnsupportedOperationException("MarkerAnnotation"); //$NON-NLS-1$
    }

    /**
     * {@link MethodDeclaration#accept(Visitor,Object)}
     * が呼び出された際にコールバックされる。
     * @param elem
     *     {@link MethodDeclaration#accept(Visitor,Object)}
     *     が呼び出されたオブジェクト。
     * @param context コンテキストオブジェクト(省略可)
     * @return このビジタの実行結果
     * @throws E この処理中に例外が発生した場合
     */
    @Override
    public R visitMethodDeclaration(
            MethodDeclaration elem,
            C context) throws E {
        throw new UnsupportedOperationException("MethodDeclaration"); //$NON-NLS-1$
    }

    /**
     * {@link MethodInvocationExpression#accept(Visitor,Object)}
     * が呼び出された際にコールバックされる。
     * @param elem
     *     {@link MethodInvocationExpression#accept(Visitor,Object)}
     *     が呼び出されたオブジェクト。
     * @param context コンテキストオブジェクト(省略可)
     * @return このビジタの実行結果
     * @throws E この処理中に例外が発生した場合
     */
    @Override
    public R visitMethodInvocationExpression(
            MethodInvocationExpression elem,
            C context) throws E {
        throw new UnsupportedOperationException("MethodInvocationExpression"); //$NON-NLS-1$
    }

    /**
     * {@link Modifier#accept(Visitor,Object)}
     * が呼び出された際にコールバックされる。
     * @param elem
     *     {@link Modifier#accept(Visitor,Object)}
     *     が呼び出されたオブジェクト。
     * @param context コンテキストオブジェクト(省略可)
     * @return このビジタの実行結果
     * @throws E この処理中に例外が発生した場合
     */
    @Override
    public R visitModifier(
            Modifier elem,
            C context) throws E {
        throw new UnsupportedOperationException("Modifier"); //$NON-NLS-1$
    }

    /**
     * {@link NamedType#accept(Visitor,Object)}
     * が呼び出された際にコールバックされる。
     * @param elem
     *     {@link NamedType#accept(Visitor,Object)}
     *     が呼び出されたオブジェクト。
     * @param context コンテキストオブジェクト(省略可)
     * @return このビジタの実行結果
     * @throws E この処理中に例外が発生した場合
     */
    @Override
    public R visitNamedType(
            NamedType elem,
            C context) throws E {
        throw new UnsupportedOperationException("NamedType"); //$NON-NLS-1$
    }

    /**
     * {@link NormalAnnotation#accept(Visitor,Object)}
     * が呼び出された際にコールバックされる。
     * @param elem
     *     {@link NormalAnnotation#accept(Visitor,Object)}
     *     が呼び出されたオブジェクト。
     * @param context コンテキストオブジェクト(省略可)
     * @return このビジタの実行結果
     * @throws E この処理中に例外が発生した場合
     */
    @Override
    public R visitNormalAnnotation(
            NormalAnnotation elem,
            C context) throws E {
        throw new UnsupportedOperationException("NormalAnnotation"); //$NON-NLS-1$
    }

    /**
     * {@link PackageDeclaration#accept(Visitor,Object)}
     * が呼び出された際にコールバックされる。
     * @param elem
     *     {@link PackageDeclaration#accept(Visitor,Object)}
     *     が呼び出されたオブジェクト。
     * @param context コンテキストオブジェクト(省略可)
     * @return このビジタの実行結果
     * @throws E この処理中に例外が発生した場合
     */
    @Override
    public R visitPackageDeclaration(
            PackageDeclaration elem,
            C context) throws E {
        throw new UnsupportedOperationException("PackageDeclaration"); //$NON-NLS-1$
    }

    /**
     * {@link ParameterizedType#accept(Visitor,Object)}
     * が呼び出された際にコールバックされる。
     * @param elem
     *     {@link ParameterizedType#accept(Visitor,Object)}
     *     が呼び出されたオブジェクト。
     * @param context コンテキストオブジェクト(省略可)
     * @return このビジタの実行結果
     * @throws E この処理中に例外が発生した場合
     */
    @Override
    public R visitParameterizedType(
            ParameterizedType elem,
            C context) throws E {
        throw new UnsupportedOperationException("ParameterizedType"); //$NON-NLS-1$
    }

    /**
     * {@link ParenthesizedExpression#accept(Visitor,Object)}
     * が呼び出された際にコールバックされる。
     * @param elem
     *     {@link ParenthesizedExpression#accept(Visitor,Object)}
     *     が呼び出されたオブジェクト。
     * @param context コンテキストオブジェクト(省略可)
     * @return このビジタの実行結果
     * @throws E この処理中に例外が発生した場合
     */
    @Override
    public R visitParenthesizedExpression(
            ParenthesizedExpression elem,
            C context) throws E {
        throw new UnsupportedOperationException("ParenthesizedExpression"); //$NON-NLS-1$
    }

    /**
     * {@link PostfixExpression#accept(Visitor,Object)}
     * が呼び出された際にコールバックされる。
     * @param elem
     *     {@link PostfixExpression#accept(Visitor,Object)}
     *     が呼び出されたオブジェクト。
     * @param context コンテキストオブジェクト(省略可)
     * @return このビジタの実行結果
     * @throws E この処理中に例外が発生した場合
     */
    @Override
    public R visitPostfixExpression(
            PostfixExpression elem,
            C context) throws E {
        throw new UnsupportedOperationException("PostfixExpression"); //$NON-NLS-1$
    }

    /**
     * {@link QualifiedName#accept(Visitor,Object)}
     * が呼び出された際にコールバックされる。
     * @param elem
     *     {@link QualifiedName#accept(Visitor,Object)}
     *     が呼び出されたオブジェクト。
     * @param context コンテキストオブジェクト(省略可)
     * @return このビジタの実行結果
     * @throws E この処理中に例外が発生した場合
     */
    @Override
    public R visitQualifiedName(
            QualifiedName elem,
            C context) throws E {
        throw new UnsupportedOperationException("QualifiedName"); //$NON-NLS-1$
    }

    /**
     * {@link QualifiedType#accept(Visitor,Object)}
     * が呼び出された際にコールバックされる。
     * @param elem
     *     {@link QualifiedType#accept(Visitor,Object)}
     *     が呼び出されたオブジェクト。
     * @param context コンテキストオブジェクト(省略可)
     * @return このビジタの実行結果
     * @throws E この処理中に例外が発生した場合
     */
    @Override
    public R visitQualifiedType(
            QualifiedType elem,
            C context) throws E {
        throw new UnsupportedOperationException("QualifiedType"); //$NON-NLS-1$
    }

    /**
     * {@link ReturnStatement#accept(Visitor,Object)}
     * が呼び出された際にコールバックされる。
     * @param elem
     *     {@link ReturnStatement#accept(Visitor,Object)}
     *     が呼び出されたオブジェクト。
     * @param context コンテキストオブジェクト(省略可)
     * @return このビジタの実行結果
     * @throws E この処理中に例外が発生した場合
     */
    @Override
    public R visitReturnStatement(
            ReturnStatement elem,
            C context) throws E {
        throw new UnsupportedOperationException("ReturnStatement"); //$NON-NLS-1$
    }

    /**
     * {@link SimpleName#accept(Visitor,Object)}
     * が呼び出された際にコールバックされる。
     * @param elem
     *     {@link SimpleName#accept(Visitor,Object)}
     *     が呼び出されたオブジェクト。
     * @param context コンテキストオブジェクト(省略可)
     * @return このビジタの実行結果
     * @throws E この処理中に例外が発生した場合
     */
    @Override
    public R visitSimpleName(
            SimpleName elem,
            C context) throws E {
        throw new UnsupportedOperationException("SimpleName"); //$NON-NLS-1$
    }

    /**
     * {@link SingleElementAnnotation#accept(Visitor,Object)}
     * が呼び出された際にコールバックされる。
     * @param elem
     *     {@link SingleElementAnnotation#accept(Visitor,Object)}
     *     が呼び出されたオブジェクト。
     * @param context コンテキストオブジェクト(省略可)
     * @return このビジタの実行結果
     * @throws E この処理中に例外が発生した場合
     */
    @Override
    public R visitSingleElementAnnotation(
            SingleElementAnnotation elem,
            C context) throws E {
        throw new UnsupportedOperationException("SingleElementAnnotation"); //$NON-NLS-1$
    }

    /**
     * {@link StatementExpressionList#accept(Visitor,Object)}
     * が呼び出された際にコールバックされる。
     * @param elem
     *     {@link StatementExpressionList#accept(Visitor,Object)}
     *     が呼び出されたオブジェクト。
     * @param context コンテキストオブジェクト(省略可)
     * @return このビジタの実行結果
     * @throws E この処理中に例外が発生した場合
     */
    @Override
    public R visitStatementExpressionList(
            StatementExpressionList elem,
            C context) throws E {
        throw new UnsupportedOperationException("StatementExpressionList"); //$NON-NLS-1$
    }

    /**
     * {@link Super#accept(Visitor,Object)}
     * が呼び出された際にコールバックされる。
     * @param elem
     *     {@link Super#accept(Visitor,Object)}
     *     が呼び出されたオブジェクト。
     * @param context コンテキストオブジェクト(省略可)
     * @return このビジタの実行結果
     * @throws E この処理中に例外が発生した場合
     */
    @Override
    public R visitSuper(
            Super elem,
            C context) throws E {
        throw new UnsupportedOperationException("Super"); //$NON-NLS-1$
    }

    /**
     * {@link SuperConstructorInvocation#accept(Visitor,Object)}
     * が呼び出された際にコールバックされる。
     * @param elem
     *     {@link SuperConstructorInvocation#accept(Visitor,Object)}
     *     が呼び出されたオブジェクト。
     * @param context コンテキストオブジェクト(省略可)
     * @return このビジタの実行結果
     * @throws E この処理中に例外が発生した場合
     */
    @Override
    public R visitSuperConstructorInvocation(
            SuperConstructorInvocation elem,
            C context) throws E {
        throw new UnsupportedOperationException("SuperConstructorInvocation"); //$NON-NLS-1$
    }

    /**
     * {@link SwitchCaseLabel#accept(Visitor,Object)}
     * が呼び出された際にコールバックされる。
     * @param elem
     *     {@link SwitchCaseLabel#accept(Visitor,Object)}
     *     が呼び出されたオブジェクト。
     * @param context コンテキストオブジェクト(省略可)
     * @return このビジタの実行結果
     * @throws E この処理中に例外が発生した場合
     */
    @Override
    public R visitSwitchCaseLabel(
            SwitchCaseLabel elem,
            C context) throws E {
        throw new UnsupportedOperationException("SwitchCaseLabel"); //$NON-NLS-1$
    }

    /**
     * {@link SwitchDefaultLabel#accept(Visitor,Object)}
     * が呼び出された際にコールバックされる。
     * @param elem
     *     {@link SwitchDefaultLabel#accept(Visitor,Object)}
     *     が呼び出されたオブジェクト。
     * @param context コンテキストオブジェクト(省略可)
     * @return このビジタの実行結果
     * @throws E この処理中に例外が発生した場合
     */
    @Override
    public R visitSwitchDefaultLabel(
            SwitchDefaultLabel elem,
            C context) throws E {
        throw new UnsupportedOperationException("SwitchDefaultLabel"); //$NON-NLS-1$
    }

    /**
     * {@link SwitchStatement#accept(Visitor,Object)}
     * が呼び出された際にコールバックされる。
     * @param elem
     *     {@link SwitchStatement#accept(Visitor,Object)}
     *     が呼び出されたオブジェクト。
     * @param context コンテキストオブジェクト(省略可)
     * @return このビジタの実行結果
     * @throws E この処理中に例外が発生した場合
     */
    @Override
    public R visitSwitchStatement(
            SwitchStatement elem,
            C context) throws E {
        throw new UnsupportedOperationException("SwitchStatement"); //$NON-NLS-1$
    }

    /**
     * {@link SynchronizedStatement#accept(Visitor,Object)}
     * が呼び出された際にコールバックされる。
     * @param elem
     *     {@link SynchronizedStatement#accept(Visitor,Object)}
     *     が呼び出されたオブジェクト。
     * @param context コンテキストオブジェクト(省略可)
     * @return このビジタの実行結果
     * @throws E この処理中に例外が発生した場合
     */
    @Override
    public R visitSynchronizedStatement(
            SynchronizedStatement elem,
            C context) throws E {
        throw new UnsupportedOperationException("SynchronizedStatement"); //$NON-NLS-1$
    }

    /**
     * {@link This#accept(Visitor,Object)}
     * が呼び出された際にコールバックされる。
     * @param elem
     *     {@link This#accept(Visitor,Object)}
     *     が呼び出されたオブジェクト。
     * @param context コンテキストオブジェクト(省略可)
     * @return このビジタの実行結果
     * @throws E この処理中に例外が発生した場合
     */
    @Override
    public R visitThis(
            This elem,
            C context) throws E {
        throw new UnsupportedOperationException("This"); //$NON-NLS-1$
    }

    /**
     * {@link ThrowStatement#accept(Visitor,Object)}
     * が呼び出された際にコールバックされる。
     * @param elem
     *     {@link ThrowStatement#accept(Visitor,Object)}
     *     が呼び出されたオブジェクト。
     * @param context コンテキストオブジェクト(省略可)
     * @return このビジタの実行結果
     * @throws E この処理中に例外が発生した場合
     */
    @Override
    public R visitThrowStatement(
            ThrowStatement elem,
            C context) throws E {
        throw new UnsupportedOperationException("ThrowStatement"); //$NON-NLS-1$
    }

    /**
     * {@link TryStatement#accept(Visitor,Object)}
     * が呼び出された際にコールバックされる。
     * @param elem
     *     {@link TryStatement#accept(Visitor,Object)}
     *     が呼び出されたオブジェクト。
     * @param context コンテキストオブジェクト(省略可)
     * @return このビジタの実行結果
     * @throws E この処理中に例外が発生した場合
     */
    @Override
    public R visitTryStatement(
            TryStatement elem,
            C context) throws E {
        throw new UnsupportedOperationException("TryStatement"); //$NON-NLS-1$
    }

    /**
     * {@link TypeParameterDeclaration#accept(Visitor,Object)}
     * が呼び出された際にコールバックされる。
     * @param elem
     *     {@link TypeParameterDeclaration#accept(Visitor,Object)}
     *     が呼び出されたオブジェクト。
     * @param context コンテキストオブジェクト(省略可)
     * @return このビジタの実行結果
     * @throws E この処理中に例外が発生した場合
     */
    @Override
    public R visitTypeParameterDeclaration(
            TypeParameterDeclaration elem,
            C context) throws E {
        throw new UnsupportedOperationException("TypeParameterDeclaration"); //$NON-NLS-1$
    }

    /**
     * {@link UnaryExpression#accept(Visitor,Object)}
     * が呼び出された際にコールバックされる。
     * @param elem
     *     {@link UnaryExpression#accept(Visitor,Object)}
     *     が呼び出されたオブジェクト。
     * @param context コンテキストオブジェクト(省略可)
     * @return このビジタの実行結果
     * @throws E この処理中に例外が発生した場合
     */
    @Override
    public R visitUnaryExpression(
            UnaryExpression elem,
            C context) throws E {
        throw new UnsupportedOperationException("UnaryExpression"); //$NON-NLS-1$
    }

    /**
     * {@link VariableDeclarator#accept(Visitor,Object)}
     * が呼び出された際にコールバックされる。
     * @param elem
     *     {@link VariableDeclarator#accept(Visitor,Object)}
     *     が呼び出されたオブジェクト。
     * @param context コンテキストオブジェクト(省略可)
     * @return このビジタの実行結果
     * @throws E この処理中に例外が発生した場合
     */
    @Override
    public R visitVariableDeclarator(
            VariableDeclarator elem,
            C context) throws E {
        throw new UnsupportedOperationException("VariableDeclarator"); //$NON-NLS-1$
    }

    /**
     * {@link WhileStatement#accept(Visitor,Object)}
     * が呼び出された際にコールバックされる。
     * @param elem
     *     {@link WhileStatement#accept(Visitor,Object)}
     *     が呼び出されたオブジェクト。
     * @param context コンテキストオブジェクト(省略可)
     * @return このビジタの実行結果
     * @throws E この処理中に例外が発生した場合
     */
    @Override
    public R visitWhileStatement(
            WhileStatement elem,
            C context) throws E {
        throw new UnsupportedOperationException("WhileStatement"); //$NON-NLS-1$
    }

    /**
     * {@link Wildcard#accept(Visitor,Object)}
     * が呼び出された際にコールバックされる。
     * @param elem
     *     {@link Wildcard#accept(Visitor,Object)}
     *     が呼び出されたオブジェクト。
     * @param context コンテキストオブジェクト(省略可)
     * @return このビジタの実行結果
     * @throws E この処理中に例外が発生した場合
     */
    @Override
    public R visitWildcard(
            Wildcard elem,
            C context) throws E {
        throw new UnsupportedOperationException("Wildcard"); //$NON-NLS-1$
    }
}
