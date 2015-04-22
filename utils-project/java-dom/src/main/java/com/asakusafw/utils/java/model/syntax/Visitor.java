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
 * この実装では、すべてのメソッドが何も行わずに{@code null}を返す。
 * </p>
 * @param <R> 戻り値の型
 * @param <C> コンテキストオブジェクトの型
 * @param <E> 例外の型
 */
public abstract class Visitor<R, C, E extends Throwable> {

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
    public R visitAlternateConstructorInvocation(
            AlternateConstructorInvocation elem,
            C context) throws E {
        return null;
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
    public R visitAnnotationDeclaration(
            AnnotationDeclaration elem,
            C context) throws E {
        return null;
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
    public R visitAnnotationElement(
            AnnotationElement elem,
            C context) throws E {
        return null;
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
    public R visitAnnotationElementDeclaration(
            AnnotationElementDeclaration elem,
            C context) throws E {
        return null;
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
    public R visitArrayAccessExpression(
            ArrayAccessExpression elem,
            C context) throws E {
        return null;
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
    public R visitArrayCreationExpression(
            ArrayCreationExpression elem,
            C context) throws E {
        return null;
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
    public R visitArrayInitializer(
            ArrayInitializer elem,
            C context) throws E {
        return null;
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
    public R visitArrayType(
            ArrayType elem,
            C context) throws E {
        return null;
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
    public R visitAssertStatement(
            AssertStatement elem,
            C context) throws E {
        return null;
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
    public R visitAssignmentExpression(
            AssignmentExpression elem,
            C context) throws E {
        return null;
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
    public R visitBasicType(
            BasicType elem,
            C context) throws E {
        return null;
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
    public R visitBlock(
            Block elem,
            C context) throws E {
        return null;
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
    public R visitBlockComment(
            BlockComment elem,
            C context) throws E {
        return null;
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
    public R visitBreakStatement(
            BreakStatement elem,
            C context) throws E {
        return null;
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
    public R visitCastExpression(
            CastExpression elem,
            C context) throws E {
        return null;
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
    public R visitCatchClause(
            CatchClause elem,
            C context) throws E {
        return null;
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
    public R visitClassBody(
            ClassBody elem,
            C context) throws E {
        return null;
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
    public R visitClassDeclaration(
            ClassDeclaration elem,
            C context) throws E {
        return null;
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
    public R visitClassInstanceCreationExpression(
            ClassInstanceCreationExpression elem,
            C context) throws E {
        return null;
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
    public R visitClassLiteral(
            ClassLiteral elem,
            C context) throws E {
        return null;
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
    public R visitCompilationUnit(
            CompilationUnit elem,
            C context) throws E {
        return null;
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
    public R visitConditionalExpression(
            ConditionalExpression elem,
            C context) throws E {
        return null;
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
    public R visitConstructorDeclaration(
            ConstructorDeclaration elem,
            C context) throws E {
        return null;
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
    public R visitContinueStatement(
            ContinueStatement elem,
            C context) throws E {
        return null;
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
    public R visitDoStatement(
            DoStatement elem,
            C context) throws E {
        return null;
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
    public R visitDocBlock(
            DocBlock elem,
            C context) throws E {
        return null;
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
    public R visitDocField(
            DocField elem,
            C context) throws E {
        return null;
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
    public R visitDocMethod(
            DocMethod elem,
            C context) throws E {
        return null;
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
    public R visitDocMethodParameter(
            DocMethodParameter elem,
            C context) throws E {
        return null;
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
    public R visitDocText(
            DocText elem,
            C context) throws E {
        return null;
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
    public R visitEmptyStatement(
            EmptyStatement elem,
            C context) throws E {
        return null;
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
    public R visitEnhancedForStatement(
            EnhancedForStatement elem,
            C context) throws E {
        return null;
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
    public R visitEnumConstantDeclaration(
            EnumConstantDeclaration elem,
            C context) throws E {
        return null;
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
    public R visitEnumDeclaration(
            EnumDeclaration elem,
            C context) throws E {
        return null;
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
    public R visitExpressionStatement(
            ExpressionStatement elem,
            C context) throws E {
        return null;
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
    public R visitFieldAccessExpression(
            FieldAccessExpression elem,
            C context) throws E {
        return null;
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
    public R visitFieldDeclaration(
            FieldDeclaration elem,
            C context) throws E {
        return null;
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
    public R visitForStatement(
            ForStatement elem,
            C context) throws E {
        return null;
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
    public R visitFormalParameterDeclaration(
            FormalParameterDeclaration elem,
            C context) throws E {
        return null;
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
    public R visitIfStatement(
            IfStatement elem,
            C context) throws E {
        return null;
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
    public R visitImportDeclaration(
            ImportDeclaration elem,
            C context) throws E {
        return null;
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
    public R visitInfixExpression(
            InfixExpression elem,
            C context) throws E {
        return null;
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
    public R visitInitializerDeclaration(
            InitializerDeclaration elem,
            C context) throws E {
        return null;
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
    public R visitInstanceofExpression(
            InstanceofExpression elem,
            C context) throws E {
        return null;
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
    public R visitInterfaceDeclaration(
            InterfaceDeclaration elem,
            C context) throws E {
        return null;
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
    public R visitJavadoc(
            Javadoc elem,
            C context) throws E {
        return null;
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
    public R visitLabeledStatement(
            LabeledStatement elem,
            C context) throws E {
        return null;
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
    public R visitLineComment(
            LineComment elem,
            C context) throws E {
        return null;
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
    public R visitLiteral(
            Literal elem,
            C context) throws E {
        return null;
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
    public R visitLocalClassDeclaration(
            LocalClassDeclaration elem,
            C context) throws E {
        return null;
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
    public R visitLocalVariableDeclaration(
            LocalVariableDeclaration elem,
            C context) throws E {
        return null;
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
    public R visitMarkerAnnotation(
            MarkerAnnotation elem,
            C context) throws E {
        return null;
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
    public R visitMethodDeclaration(
            MethodDeclaration elem,
            C context) throws E {
        return null;
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
    public R visitMethodInvocationExpression(
            MethodInvocationExpression elem,
            C context) throws E {
        return null;
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
    public R visitModifier(
            Modifier elem,
            C context) throws E {
        return null;
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
    public R visitNamedType(
            NamedType elem,
            C context) throws E {
        return null;
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
    public R visitNormalAnnotation(
            NormalAnnotation elem,
            C context) throws E {
        return null;
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
    public R visitPackageDeclaration(
            PackageDeclaration elem,
            C context) throws E {
        return null;
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
    public R visitParameterizedType(
            ParameterizedType elem,
            C context) throws E {
        return null;
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
    public R visitParenthesizedExpression(
            ParenthesizedExpression elem,
            C context) throws E {
        return null;
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
    public R visitPostfixExpression(
            PostfixExpression elem,
            C context) throws E {
        return null;
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
    public R visitQualifiedName(
            QualifiedName elem,
            C context) throws E {
        return null;
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
    public R visitQualifiedType(
            QualifiedType elem,
            C context) throws E {
        return null;
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
    public R visitReturnStatement(
            ReturnStatement elem,
            C context) throws E {
        return null;
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
    public R visitSimpleName(
            SimpleName elem,
            C context) throws E {
        return null;
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
    public R visitSingleElementAnnotation(
            SingleElementAnnotation elem,
            C context) throws E {
        return null;
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
    public R visitStatementExpressionList(
            StatementExpressionList elem,
            C context) throws E {
        return null;
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
    public R visitSuper(
            Super elem,
            C context) throws E {
        return null;
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
    public R visitSuperConstructorInvocation(
            SuperConstructorInvocation elem,
            C context) throws E {
        return null;
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
    public R visitSwitchCaseLabel(
            SwitchCaseLabel elem,
            C context) throws E {
        return null;
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
    public R visitSwitchDefaultLabel(
            SwitchDefaultLabel elem,
            C context) throws E {
        return null;
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
    public R visitSwitchStatement(
            SwitchStatement elem,
            C context) throws E {
        return null;
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
    public R visitSynchronizedStatement(
            SynchronizedStatement elem,
            C context) throws E {
        return null;
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
    public R visitThis(
            This elem,
            C context) throws E {
        return null;
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
    public R visitThrowStatement(
            ThrowStatement elem,
            C context) throws E {
        return null;
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
    public R visitTryStatement(
            TryStatement elem,
            C context) throws E {
        return null;
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
    public R visitTypeParameterDeclaration(
            TypeParameterDeclaration elem,
            C context) throws E {
        return null;
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
    public R visitUnaryExpression(
            UnaryExpression elem,
            C context) throws E {
        return null;
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
    public R visitVariableDeclarator(
            VariableDeclarator elem,
            C context) throws E {
        return null;
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
    public R visitWhileStatement(
            WhileStatement elem,
            C context) throws E {
        return null;
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
    public R visitWildcard(
            Wildcard elem,
            C context) throws E {
        return null;
    }
}
