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
        throw new UnsupportedOperationException("AlternateConstructorInvocation");
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
        throw new UnsupportedOperationException("AnnotationDeclaration");
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
        throw new UnsupportedOperationException("AnnotationElement");
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
        throw new UnsupportedOperationException("AnnotationElementDeclaration");
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
        throw new UnsupportedOperationException("ArrayAccessExpression");
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
        throw new UnsupportedOperationException("ArrayCreationExpression");
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
        throw new UnsupportedOperationException("ArrayInitializer");
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
        throw new UnsupportedOperationException("ArrayType");
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
        throw new UnsupportedOperationException("AssertStatement");
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
        throw new UnsupportedOperationException("AssignmentExpression");
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
        throw new UnsupportedOperationException("BasicType");
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
        throw new UnsupportedOperationException("Block");
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
        throw new UnsupportedOperationException("BlockComment");
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
        throw new UnsupportedOperationException("BreakStatement");
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
        throw new UnsupportedOperationException("CastExpression");
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
        throw new UnsupportedOperationException("CatchClause");
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
        throw new UnsupportedOperationException("ClassBody");
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
        throw new UnsupportedOperationException("ClassDeclaration");
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
        throw new UnsupportedOperationException("ClassInstanceCreationExpression");
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
        throw new UnsupportedOperationException("ClassLiteral");
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
        throw new UnsupportedOperationException("CompilationUnit");
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
        throw new UnsupportedOperationException("ConditionalExpression");
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
        throw new UnsupportedOperationException("ConstructorDeclaration");
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
        throw new UnsupportedOperationException("ContinueStatement");
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
        throw new UnsupportedOperationException("DoStatement");
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
        throw new UnsupportedOperationException("DocBlock");
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
        throw new UnsupportedOperationException("DocField");
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
        throw new UnsupportedOperationException("DocMethod");
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
        throw new UnsupportedOperationException("DocMethodParameter");
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
        throw new UnsupportedOperationException("DocText");
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
        throw new UnsupportedOperationException("EmptyStatement");
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
        throw new UnsupportedOperationException("EnhancedForStatement");
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
        throw new UnsupportedOperationException("EnumConstantDeclaration");
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
        throw new UnsupportedOperationException("EnumDeclaration");
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
        throw new UnsupportedOperationException("ExpressionStatement");
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
        throw new UnsupportedOperationException("FieldAccessExpression");
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
        throw new UnsupportedOperationException("FieldDeclaration");
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
        throw new UnsupportedOperationException("ForStatement");
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
        throw new UnsupportedOperationException("FormalParameterDeclaration");
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
        throw new UnsupportedOperationException("IfStatement");
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
        throw new UnsupportedOperationException("ImportDeclaration");
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
        throw new UnsupportedOperationException("InfixExpression");
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
        throw new UnsupportedOperationException("InitializerDeclaration");
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
        throw new UnsupportedOperationException("InstanceofExpression");
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
        throw new UnsupportedOperationException("InterfaceDeclaration");
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
        throw new UnsupportedOperationException("Javadoc");
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
        throw new UnsupportedOperationException("LabeledStatement");
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
        throw new UnsupportedOperationException("LineComment");
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
        throw new UnsupportedOperationException("Literal");
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
        throw new UnsupportedOperationException("LocalClassDeclaration");
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
        throw new UnsupportedOperationException("LocalVariableDeclaration");
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
        throw new UnsupportedOperationException("MarkerAnnotation");
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
        throw new UnsupportedOperationException("MethodDeclaration");
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
        throw new UnsupportedOperationException("MethodInvocationExpression");
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
        throw new UnsupportedOperationException("Modifier");
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
        throw new UnsupportedOperationException("NamedType");
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
        throw new UnsupportedOperationException("NormalAnnotation");
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
        throw new UnsupportedOperationException("PackageDeclaration");
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
        throw new UnsupportedOperationException("ParameterizedType");
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
        throw new UnsupportedOperationException("ParenthesizedExpression");
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
        throw new UnsupportedOperationException("PostfixExpression");
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
        throw new UnsupportedOperationException("QualifiedName");
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
        throw new UnsupportedOperationException("QualifiedType");
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
        throw new UnsupportedOperationException("ReturnStatement");
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
        throw new UnsupportedOperationException("SimpleName");
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
        throw new UnsupportedOperationException("SingleElementAnnotation");
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
        throw new UnsupportedOperationException("StatementExpressionList");
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
        throw new UnsupportedOperationException("Super");
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
        throw new UnsupportedOperationException("SuperConstructorInvocation");
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
        throw new UnsupportedOperationException("SwitchCaseLabel");
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
        throw new UnsupportedOperationException("SwitchDefaultLabel");
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
        throw new UnsupportedOperationException("SwitchStatement");
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
        throw new UnsupportedOperationException("SynchronizedStatement");
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
        throw new UnsupportedOperationException("This");
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
        throw new UnsupportedOperationException("ThrowStatement");
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
        throw new UnsupportedOperationException("TryStatement");
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
        throw new UnsupportedOperationException("TypeParameterDeclaration");
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
        throw new UnsupportedOperationException("UnaryExpression");
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
        throw new UnsupportedOperationException("VariableDeclarator");
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
        throw new UnsupportedOperationException("WhileStatement");
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
        throw new UnsupportedOperationException("Wildcard");
    }
}
