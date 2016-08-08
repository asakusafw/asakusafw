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
package com.asakusafw.utils.java.model.syntax;

/**
 * Visitor for {@link Model}.
 * Each method in this implementation does nothing and always returns {@code null}.
 * @param <C> type of visitor context
 * @param <R> type of visitor result
 * @param <E> type of visitor exception
 * @since 0.1.0
 * @version 0.9.0
 */
public abstract class Visitor<R, C, E extends Throwable> {

    /**
     * Processes {@link AlternateConstructorInvocation} using this visitor.
     * @param elem the target element
     * @param context the current context (nullable)
     * @return the processing result
     * @throws E if error was occurred while processing the target element
     */
    public R visitAlternateConstructorInvocation(
            AlternateConstructorInvocation elem,
            C context) throws E {
        return null;
    }

    /**
     * Processes {@link AnnotationDeclaration} using this visitor.
     * @param elem the target element
     * @param context the current context (nullable)
     * @return the processing result
     * @throws E if error was occurred while processing the target element
     */
    public R visitAnnotationDeclaration(
            AnnotationDeclaration elem,
            C context) throws E {
        return null;
    }

    /**
     * Processes {@link AnnotationElement} using this visitor.
     * @param elem the target element
     * @param context the current context (nullable)
     * @return the processing result
     * @throws E if error was occurred while processing the target element
     */
    public R visitAnnotationElement(
            AnnotationElement elem,
            C context) throws E {
        return null;
    }

    /**
     * Processes {@link AnnotationElementDeclaration} using this visitor.
     * @param elem the target element
     * @param context the current context (nullable)
     * @return the processing result
     * @throws E if error was occurred while processing the target element
     */
    public R visitAnnotationElementDeclaration(
            AnnotationElementDeclaration elem,
            C context) throws E {
        return null;
    }

    /**
     * Processes {@link ArrayAccessExpression} using this visitor.
     * @param elem the target element
     * @param context the current context (nullable)
     * @return the processing result
     * @throws E if error was occurred while processing the target element
     */
    public R visitArrayAccessExpression(
            ArrayAccessExpression elem,
            C context) throws E {
        return null;
    }

    /**
     * Processes {@link ArrayCreationExpression} using this visitor.
     * @param elem the target element
     * @param context the current context (nullable)
     * @return the processing result
     * @throws E if error was occurred while processing the target element
     */
    public R visitArrayCreationExpression(
            ArrayCreationExpression elem,
            C context) throws E {
        return null;
    }

    /**
     * Processes {@link ArrayInitializer} using this visitor.
     * @param elem the target element
     * @param context the current context (nullable)
     * @return the processing result
     * @throws E if error was occurred while processing the target element
     */
    public R visitArrayInitializer(
            ArrayInitializer elem,
            C context) throws E {
        return null;
    }

    /**
     * Processes {@link ArrayType} using this visitor.
     * @param elem the target element
     * @param context the current context (nullable)
     * @return the processing result
     * @throws E if error was occurred while processing the target element
     */
    public R visitArrayType(
            ArrayType elem,
            C context) throws E {
        return null;
    }

    /**
     * Processes {@link AssertStatement} using this visitor.
     * @param elem the target element
     * @param context the current context (nullable)
     * @return the processing result
     * @throws E if error was occurred while processing the target element
     */
    public R visitAssertStatement(
            AssertStatement elem,
            C context) throws E {
        return null;
    }

    /**
     * Processes {@link AssignmentExpression} using this visitor.
     * @param elem the target element
     * @param context the current context (nullable)
     * @return the processing result
     * @throws E if error was occurred while processing the target element
     */
    public R visitAssignmentExpression(
            AssignmentExpression elem,
            C context) throws E {
        return null;
    }

    /**
     * Processes {@link BasicType} using this visitor.
     * @param elem the target element
     * @param context the current context (nullable)
     * @return the processing result
     * @throws E if error was occurred while processing the target element
     */
    public R visitBasicType(
            BasicType elem,
            C context) throws E {
        return null;
    }

    /**
     * Processes {@link Block} using this visitor.
     * @param elem the target element
     * @param context the current context (nullable)
     * @return the processing result
     * @throws E if error was occurred while processing the target element
     */
    public R visitBlock(
            Block elem,
            C context) throws E {
        return null;
    }

    /**
     * Processes {@link BlockComment} using this visitor.
     * @param elem the target element
     * @param context the current context (nullable)
     * @return the processing result
     * @throws E if error was occurred while processing the target element
     */
    public R visitBlockComment(
            BlockComment elem,
            C context) throws E {
        return null;
    }

    /**
     * Processes {@link BreakStatement} using this visitor.
     * @param elem the target element
     * @param context the current context (nullable)
     * @return the processing result
     * @throws E if error was occurred while processing the target element
     */
    public R visitBreakStatement(
            BreakStatement elem,
            C context) throws E {
        return null;
    }

    /**
     * Processes {@link CastExpression} using this visitor.
     * @param elem the target element
     * @param context the current context (nullable)
     * @return the processing result
     * @throws E if error was occurred while processing the target element
     */
    public R visitCastExpression(
            CastExpression elem,
            C context) throws E {
        return null;
    }

    /**
     * Processes {@link CatchClause} using this visitor.
     * @param elem the target element
     * @param context the current context (nullable)
     * @return the processing result
     * @throws E if error was occurred while processing the target element
     */
    public R visitCatchClause(
            CatchClause elem,
            C context) throws E {
        return null;
    }

    /**
     * Processes {@link ClassBody} using this visitor.
     * @param elem the target element
     * @param context the current context (nullable)
     * @return the processing result
     * @throws E if error was occurred while processing the target element
     */
    public R visitClassBody(
            ClassBody elem,
            C context) throws E {
        return null;
    }

    /**
     * Processes {@link ClassDeclaration} using this visitor.
     * @param elem the target element
     * @param context the current context (nullable)
     * @return the processing result
     * @throws E if error was occurred while processing the target element
     */
    public R visitClassDeclaration(
            ClassDeclaration elem,
            C context) throws E {
        return null;
    }

    /**
     * Processes {@link ClassInstanceCreationExpression} using this visitor.
     * @param elem the target element
     * @param context the current context (nullable)
     * @return the processing result
     * @throws E if error was occurred while processing the target element
     */
    public R visitClassInstanceCreationExpression(
            ClassInstanceCreationExpression elem,
            C context) throws E {
        return null;
    }

    /**
     * Processes {@link ClassLiteral} using this visitor.
     * @param elem the target element
     * @param context the current context (nullable)
     * @return the processing result
     * @throws E if error was occurred while processing the target element
     */
    public R visitClassLiteral(
            ClassLiteral elem,
            C context) throws E {
        return null;
    }

    /**
     * Processes {@link CompilationUnit} using this visitor.
     * @param elem the target element
     * @param context the current context (nullable)
     * @return the processing result
     * @throws E if error was occurred while processing the target element
     */
    public R visitCompilationUnit(
            CompilationUnit elem,
            C context) throws E {
        return null;
    }

    /**
     * Processes {@link ConditionalExpression} using this visitor.
     * @param elem the target element
     * @param context the current context (nullable)
     * @return the processing result
     * @throws E if error was occurred while processing the target element
     */
    public R visitConditionalExpression(
            ConditionalExpression elem,
            C context) throws E {
        return null;
    }

    /**
     * Processes {@link ConstructorDeclaration} using this visitor.
     * @param elem the target element
     * @param context the current context (nullable)
     * @return the processing result
     * @throws E if error was occurred while processing the target element
     */
    public R visitConstructorDeclaration(
            ConstructorDeclaration elem,
            C context) throws E {
        return null;
    }

    /**
     * Processes {@link ContinueStatement} using this visitor.
     * @param elem the target element
     * @param context the current context (nullable)
     * @return the processing result
     * @throws E if error was occurred while processing the target element
     */
    public R visitContinueStatement(
            ContinueStatement elem,
            C context) throws E {
        return null;
    }

    /**
     * Processes {@link DoStatement} using this visitor.
     * @param elem the target element
     * @param context the current context (nullable)
     * @return the processing result
     * @throws E if error was occurred while processing the target element
     */
    public R visitDoStatement(
            DoStatement elem,
            C context) throws E {
        return null;
    }

    /**
     * Processes {@link DocBlock} using this visitor.
     * @param elem the target element
     * @param context the current context (nullable)
     * @return the processing result
     * @throws E if error was occurred while processing the target element
     */
    public R visitDocBlock(
            DocBlock elem,
            C context) throws E {
        return null;
    }

    /**
     * Processes {@link DocField} using this visitor.
     * @param elem the target element
     * @param context the current context (nullable)
     * @return the processing result
     * @throws E if error was occurred while processing the target element
     */
    public R visitDocField(
            DocField elem,
            C context) throws E {
        return null;
    }

    /**
     * Processes {@link DocMethod} using this visitor.
     * @param elem the target element
     * @param context the current context (nullable)
     * @return the processing result
     * @throws E if error was occurred while processing the target element
     */
    public R visitDocMethod(
            DocMethod elem,
            C context) throws E {
        return null;
    }

    /**
     * Processes {@link DocMethodParameter} using this visitor.
     * @param elem the target element
     * @param context the current context (nullable)
     * @return the processing result
     * @throws E if error was occurred while processing the target element
     */
    public R visitDocMethodParameter(
            DocMethodParameter elem,
            C context) throws E {
        return null;
    }

    /**
     * Processes {@link DocText} using this visitor.
     * @param elem the target element
     * @param context the current context (nullable)
     * @return the processing result
     * @throws E if error was occurred while processing the target element
     */
    public R visitDocText(
            DocText elem,
            C context) throws E {
        return null;
    }

    /**
     * Processes {@link EmptyStatement} using this visitor.
     * @param elem the target element
     * @param context the current context (nullable)
     * @return the processing result
     * @throws E if error was occurred while processing the target element
     */
    public R visitEmptyStatement(
            EmptyStatement elem,
            C context) throws E {
        return null;
    }

    /**
     * Processes {@link EnhancedForStatement} using this visitor.
     * @param elem the target element
     * @param context the current context (nullable)
     * @return the processing result
     * @throws E if error was occurred while processing the target element
     */
    public R visitEnhancedForStatement(
            EnhancedForStatement elem,
            C context) throws E {
        return null;
    }

    /**
     * Processes {@link EnumConstantDeclaration} using this visitor.
     * @param elem the target element
     * @param context the current context (nullable)
     * @return the processing result
     * @throws E if error was occurred while processing the target element
     */
    public R visitEnumConstantDeclaration(
            EnumConstantDeclaration elem,
            C context) throws E {
        return null;
    }

    /**
     * Processes {@link EnumDeclaration} using this visitor.
     * @param elem the target element
     * @param context the current context (nullable)
     * @return the processing result
     * @throws E if error was occurred while processing the target element
     */
    public R visitEnumDeclaration(
            EnumDeclaration elem,
            C context) throws E {
        return null;
    }

    /**
     * Processes {@link ExpressionStatement} using this visitor.
     * @param elem the target element
     * @param context the current context (nullable)
     * @return the processing result
     * @throws E if error was occurred while processing the target element
     */
    public R visitExpressionStatement(
            ExpressionStatement elem,
            C context) throws E {
        return null;
    }

    /**
     * Processes {@link FieldAccessExpression} using this visitor.
     * @param elem the target element
     * @param context the current context (nullable)
     * @return the processing result
     * @throws E if error was occurred while processing the target element
     */
    public R visitFieldAccessExpression(
            FieldAccessExpression elem,
            C context) throws E {
        return null;
    }

    /**
     * Processes {@link FieldDeclaration} using this visitor.
     * @param elem the target element
     * @param context the current context (nullable)
     * @return the processing result
     * @throws E if error was occurred while processing the target element
     */
    public R visitFieldDeclaration(
            FieldDeclaration elem,
            C context) throws E {
        return null;
    }

    /**
     * Processes {@link ForStatement} using this visitor.
     * @param elem the target element
     * @param context the current context (nullable)
     * @return the processing result
     * @throws E if error was occurred while processing the target element
     */
    public R visitForStatement(
            ForStatement elem,
            C context) throws E {
        return null;
    }

    /**
     * Processes {@link FormalParameterDeclaration} using this visitor.
     * @param elem the target element
     * @param context the current context (nullable)
     * @return the processing result
     * @throws E if error was occurred while processing the target element
     */
    public R visitFormalParameterDeclaration(
            FormalParameterDeclaration elem,
            C context) throws E {
        return null;
    }

    /**
     * Processes {@link IfStatement} using this visitor.
     * @param elem the target element
     * @param context the current context (nullable)
     * @return the processing result
     * @throws E if error was occurred while processing the target element
     */
    public R visitIfStatement(
            IfStatement elem,
            C context) throws E {
        return null;
    }

    /**
     * Processes {@link ImportDeclaration} using this visitor.
     * @param elem the target element
     * @param context the current context (nullable)
     * @return the processing result
     * @throws E if error was occurred while processing the target element
     */
    public R visitImportDeclaration(
            ImportDeclaration elem,
            C context) throws E {
        return null;
    }

    /**
     * Processes {@link InfixExpression} using this visitor.
     * @param elem the target element
     * @param context the current context (nullable)
     * @return the processing result
     * @throws E if error was occurred while processing the target element
     */
    public R visitInfixExpression(
            InfixExpression elem,
            C context) throws E {
        return null;
    }

    /**
     * Processes {@link InitializerDeclaration} using this visitor.
     * @param elem the target element
     * @param context the current context (nullable)
     * @return the processing result
     * @throws E if error was occurred while processing the target element
     */
    public R visitInitializerDeclaration(
            InitializerDeclaration elem,
            C context) throws E {
        return null;
    }

    /**
     * Processes {@link InstanceofExpression} using this visitor.
     * @param elem the target element
     * @param context the current context (nullable)
     * @return the processing result
     * @throws E if error was occurred while processing the target element
     */
    public R visitInstanceofExpression(
            InstanceofExpression elem,
            C context) throws E {
        return null;
    }

    /**
     * Processes {@link InterfaceDeclaration} using this visitor.
     * @param elem the target element
     * @param context the current context (nullable)
     * @return the processing result
     * @throws E if error was occurred while processing the target element
     */
    public R visitInterfaceDeclaration(
            InterfaceDeclaration elem,
            C context) throws E {
        return null;
    }

    /**
     * Processes {@link Javadoc} using this visitor.
     * @param elem the target element
     * @param context the current context (nullable)
     * @return the processing result
     * @throws E if error was occurred while processing the target element
     */
    public R visitJavadoc(
            Javadoc elem,
            C context) throws E {
        return null;
    }

    /**
     * Processes {@link LabeledStatement} using this visitor.
     * @param elem the target element
     * @param context the current context (nullable)
     * @return the processing result
     * @throws E if error was occurred while processing the target element
     */
    public R visitLabeledStatement(
            LabeledStatement elem,
            C context) throws E {
        return null;
    }

    /**
     * Processes {@link LambdaExpression} using this visitor.
     * @param elem the target element
     * @param context the current context (nullable)
     * @return the processing result
     * @throws E if error was occurred while processing the target element
     * @since 0.9.0
     */
    public R visitLambdaExpression(
            LambdaExpression elem,
            C context) throws E {
        return null;
    }

    /**
     * Processes {@link LineComment} using this visitor.
     * @param elem the target element
     * @param context the current context (nullable)
     * @return the processing result
     * @throws E if error was occurred while processing the target element
     */
    public R visitLineComment(
            LineComment elem,
            C context) throws E {
        return null;
    }

    /**
     * Processes {@link Literal} using this visitor.
     * @param elem the target element
     * @param context the current context (nullable)
     * @return the processing result
     * @throws E if error was occurred while processing the target element
     */
    public R visitLiteral(
            Literal elem,
            C context) throws E {
        return null;
    }

    /**
     * Processes {@link LocalClassDeclaration} using this visitor.
     * @param elem the target element
     * @param context the current context (nullable)
     * @return the processing result
     * @throws E if error was occurred while processing the target element
     */
    public R visitLocalClassDeclaration(
            LocalClassDeclaration elem,
            C context) throws E {
        return null;
    }

    /**
     * Processes {@link LocalVariableDeclaration} using this visitor.
     * @param elem the target element
     * @param context the current context (nullable)
     * @return the processing result
     * @throws E if error was occurred while processing the target element
     */
    public R visitLocalVariableDeclaration(
            LocalVariableDeclaration elem,
            C context) throws E {
        return null;
    }

    /**
     * Processes {@link MarkerAnnotation} using this visitor.
     * @param elem the target element
     * @param context the current context (nullable)
     * @return the processing result
     * @throws E if error was occurred while processing the target element
     */
    public R visitMarkerAnnotation(
            MarkerAnnotation elem,
            C context) throws E {
        return null;
    }

    /**
     * Processes {@link MethodDeclaration} using this visitor.
     * @param elem the target element
     * @param context the current context (nullable)
     * @return the processing result
     * @throws E if error was occurred while processing the target element
     */
    public R visitMethodDeclaration(
            MethodDeclaration elem,
            C context) throws E {
        return null;
    }

    /**
     * Processes {@link MethodInvocationExpression} using this visitor.
     * @param elem the target element
     * @param context the current context (nullable)
     * @return the processing result
     * @throws E if error was occurred while processing the target element
     */
    public R visitMethodInvocationExpression(
            MethodInvocationExpression elem,
            C context) throws E {
        return null;
    }

    /**
     * Processes {@link Modifier} using this visitor.
     * @param elem the target element
     * @param context the current context (nullable)
     * @return the processing result
     * @throws E if error was occurred while processing the target element
     */
    public R visitModifier(
            Modifier elem,
            C context) throws E {
        return null;
    }

    /**
     * Processes {@link NamedType} using this visitor.
     * @param elem the target element
     * @param context the current context (nullable)
     * @return the processing result
     * @throws E if error was occurred while processing the target element
     */
    public R visitNamedType(
            NamedType elem,
            C context) throws E {
        return null;
    }

    /**
     * Processes {@link NormalAnnotation} using this visitor.
     * @param elem the target element
     * @param context the current context (nullable)
     * @return the processing result
     * @throws E if error was occurred while processing the target element
     */
    public R visitNormalAnnotation(
            NormalAnnotation elem,
            C context) throws E {
        return null;
    }

    /**
     * Processes {@link PackageDeclaration} using this visitor.
     * @param elem the target element
     * @param context the current context (nullable)
     * @return the processing result
     * @throws E if error was occurred while processing the target element
     */
    public R visitPackageDeclaration(
            PackageDeclaration elem,
            C context) throws E {
        return null;
    }

    /**
     * Processes {@link ParameterizedType} using this visitor.
     * @param elem the target element
     * @param context the current context (nullable)
     * @return the processing result
     * @throws E if error was occurred while processing the target element
     */
    public R visitParameterizedType(
            ParameterizedType elem,
            C context) throws E {
        return null;
    }

    /**
     * Processes {@link ParenthesizedExpression} using this visitor.
     * @param elem the target element
     * @param context the current context (nullable)
     * @return the processing result
     * @throws E if error was occurred while processing the target element
     */
    public R visitParenthesizedExpression(
            ParenthesizedExpression elem,
            C context) throws E {
        return null;
    }

    /**
     * Processes {@link PostfixExpression} using this visitor.
     * @param elem the target element
     * @param context the current context (nullable)
     * @return the processing result
     * @throws E if error was occurred while processing the target element
     */
    public R visitPostfixExpression(
            PostfixExpression elem,
            C context) throws E {
        return null;
    }

    /**
     * Processes {@link QualifiedName} using this visitor.
     * @param elem the target element
     * @param context the current context (nullable)
     * @return the processing result
     * @throws E if error was occurred while processing the target element
     */
    public R visitQualifiedName(
            QualifiedName elem,
            C context) throws E {
        return null;
    }

    /**
     * Processes {@link QualifiedType} using this visitor.
     * @param elem the target element
     * @param context the current context (nullable)
     * @return the processing result
     * @throws E if error was occurred while processing the target element
     */
    public R visitQualifiedType(
            QualifiedType elem,
            C context) throws E {
        return null;
    }

    /**
     * Processes {@link ReturnStatement} using this visitor.
     * @param elem the target element
     * @param context the current context (nullable)
     * @return the processing result
     * @throws E if error was occurred while processing the target element
     */
    public R visitReturnStatement(
            ReturnStatement elem,
            C context) throws E {
        return null;
    }

    /**
     * Processes {@link SimpleName} using this visitor.
     * @param elem the target element
     * @param context the current context (nullable)
     * @return the processing result
     * @throws E if error was occurred while processing the target element
     */
    public R visitSimpleName(
            SimpleName elem,
            C context) throws E {
        return null;
    }

    /**
     * Processes {@link SingleElementAnnotation} using this visitor.
     * @param elem the target element
     * @param context the current context (nullable)
     * @return the processing result
     * @throws E if error was occurred while processing the target element
     */
    public R visitSingleElementAnnotation(
            SingleElementAnnotation elem,
            C context) throws E {
        return null;
    }

    /**
     * Processes {@link StatementExpressionList} using this visitor.
     * @param elem the target element
     * @param context the current context (nullable)
     * @return the processing result
     * @throws E if error was occurred while processing the target element
     */
    public R visitStatementExpressionList(
            StatementExpressionList elem,
            C context) throws E {
        return null;
    }

    /**
     * Processes {@link Super} using this visitor.
     * @param elem the target element
     * @param context the current context (nullable)
     * @return the processing result
     * @throws E if error was occurred while processing the target element
     */
    public R visitSuper(
            Super elem,
            C context) throws E {
        return null;
    }

    /**
     * Processes {@link SuperConstructorInvocation} using this visitor.
     * @param elem the target element
     * @param context the current context (nullable)
     * @return the processing result
     * @throws E if error was occurred while processing the target element
     */
    public R visitSuperConstructorInvocation(
            SuperConstructorInvocation elem,
            C context) throws E {
        return null;
    }

    /**
     * Processes {@link SwitchCaseLabel} using this visitor.
     * @param elem the target element
     * @param context the current context (nullable)
     * @return the processing result
     * @throws E if error was occurred while processing the target element
     */
    public R visitSwitchCaseLabel(
            SwitchCaseLabel elem,
            C context) throws E {
        return null;
    }

    /**
     * Processes {@link SwitchDefaultLabel} using this visitor.
     * @param elem the target element
     * @param context the current context (nullable)
     * @return the processing result
     * @throws E if error was occurred while processing the target element
     */
    public R visitSwitchDefaultLabel(
            SwitchDefaultLabel elem,
            C context) throws E {
        return null;
    }

    /**
     * Processes {@link SwitchStatement} using this visitor.
     * @param elem the target element
     * @param context the current context (nullable)
     * @return the processing result
     * @throws E if error was occurred while processing the target element
     */
    public R visitSwitchStatement(
            SwitchStatement elem,
            C context) throws E {
        return null;
    }

    /**
     * Processes {@link SynchronizedStatement} using this visitor.
     * @param elem the target element
     * @param context the current context (nullable)
     * @return the processing result
     * @throws E if error was occurred while processing the target element
     */
    public R visitSynchronizedStatement(
            SynchronizedStatement elem,
            C context) throws E {
        return null;
    }

    /**
     * Processes {@link This} using this visitor.
     * @param elem the target element
     * @param context the current context (nullable)
     * @return the processing result
     * @throws E if error was occurred while processing the target element
     */
    public R visitThis(
            This elem,
            C context) throws E {
        return null;
    }

    /**
     * Processes {@link ThrowStatement} using this visitor.
     * @param elem the target element
     * @param context the current context (nullable)
     * @return the processing result
     * @throws E if error was occurred while processing the target element
     */
    public R visitThrowStatement(
            ThrowStatement elem,
            C context) throws E {
        return null;
    }

    /**
     * Processes {@link TryStatement} using this visitor.
     * @param elem the target element
     * @param context the current context (nullable)
     * @return the processing result
     * @throws E if error was occurred while processing the target element
     */
    public R visitTryStatement(
            TryStatement elem,
            C context) throws E {
        return null;
    }

    /**
     * Processes {@link TypeParameterDeclaration} using this visitor.
     * @param elem the target element
     * @param context the current context (nullable)
     * @return the processing result
     * @throws E if error was occurred while processing the target element
     */
    public R visitTypeParameterDeclaration(
            TypeParameterDeclaration elem,
            C context) throws E {
        return null;
    }

    /**
     * Processes {@link UnaryExpression} using this visitor.
     * @param elem the target element
     * @param context the current context (nullable)
     * @return the processing result
     * @throws E if error was occurred while processing the target element
     */
    public R visitUnaryExpression(
            UnaryExpression elem,
            C context) throws E {
        return null;
    }

    /**
     * Processes {@link UnionType} using this visitor.
     * @param elem the target element
     * @param context the current context (nullable)
     * @return the processing result
     * @throws E if error was occurred while processing the target element
     * @since 0.9.0
     */
    public R visitUnionType(
            UnionType elem,
            C context) throws E {
        return null;
    }

    /**
     * Processes {@link VariableDeclarator} using this visitor.
     * @param elem the target element
     * @param context the current context (nullable)
     * @return the processing result
     * @throws E if error was occurred while processing the target element
     */
    public R visitVariableDeclarator(
            VariableDeclarator elem,
            C context) throws E {
        return null;
    }

    /**
     * Processes {@link WhileStatement} using this visitor.
     * @param elem the target element
     * @param context the current context (nullable)
     * @return the processing result
     * @throws E if error was occurred while processing the target element
     */
    public R visitWhileStatement(
            WhileStatement elem,
            C context) throws E {
        return null;
    }

    /**
     * Processes {@link Wildcard} using this visitor.
     * @param elem the target element
     * @param context the current context (nullable)
     * @return the processing result
     * @throws E if error was occurred while processing the target element
     */
    public R visitWildcard(
            Wildcard elem,
            C context) throws E {
        return null;
    }
}
