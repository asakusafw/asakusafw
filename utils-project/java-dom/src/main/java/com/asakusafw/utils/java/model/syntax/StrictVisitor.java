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
package com.asakusafw.utils.java.model.syntax;

/**
 * An implementation of {@link Visitor} which raises {@link UnsupportedOperationException} in all methods.
 * @param <C> type of visitor context
 * @param <R> type of visitor result
 * @param <E> type of visitor exception
 */
public abstract class StrictVisitor<R, C, E extends Throwable>
        extends Visitor<R, C, E> {

    @Override
    public R visitAlternateConstructorInvocation(
            AlternateConstructorInvocation elem,
            C context) throws E {
        throw new UnsupportedOperationException("AlternateConstructorInvocation"); //$NON-NLS-1$
    }

    @Override
    public R visitAnnotationDeclaration(
            AnnotationDeclaration elem,
            C context) throws E {
        throw new UnsupportedOperationException("AnnotationDeclaration"); //$NON-NLS-1$
    }

    @Override
    public R visitAnnotationElement(
            AnnotationElement elem,
            C context) throws E {
        throw new UnsupportedOperationException("AnnotationElement"); //$NON-NLS-1$
    }

    @Override
    public R visitAnnotationElementDeclaration(
            AnnotationElementDeclaration elem,
            C context) throws E {
        throw new UnsupportedOperationException("AnnotationElementDeclaration"); //$NON-NLS-1$
    }

    @Override
    public R visitArrayAccessExpression(
            ArrayAccessExpression elem,
            C context) throws E {
        throw new UnsupportedOperationException("ArrayAccessExpression"); //$NON-NLS-1$
    }

    @Override
    public R visitArrayCreationExpression(
            ArrayCreationExpression elem,
            C context) throws E {
        throw new UnsupportedOperationException("ArrayCreationExpression"); //$NON-NLS-1$
    }

    @Override
    public R visitArrayInitializer(
            ArrayInitializer elem,
            C context) throws E {
        throw new UnsupportedOperationException("ArrayInitializer"); //$NON-NLS-1$
    }

    @Override
    public R visitArrayType(
            ArrayType elem,
            C context) throws E {
        throw new UnsupportedOperationException("ArrayType"); //$NON-NLS-1$
    }

    @Override
    public R visitAssertStatement(
            AssertStatement elem,
            C context) throws E {
        throw new UnsupportedOperationException("AssertStatement"); //$NON-NLS-1$
    }

    @Override
    public R visitAssignmentExpression(
            AssignmentExpression elem,
            C context) throws E {
        throw new UnsupportedOperationException("AssignmentExpression"); //$NON-NLS-1$
    }

    @Override
    public R visitBasicType(
            BasicType elem,
            C context) throws E {
        throw new UnsupportedOperationException("BasicType"); //$NON-NLS-1$
    }

    @Override
    public R visitBlock(
            Block elem,
            C context) throws E {
        throw new UnsupportedOperationException("Block"); //$NON-NLS-1$
    }

    @Override
    public R visitBlockComment(
            BlockComment elem,
            C context) throws E {
        throw new UnsupportedOperationException("BlockComment"); //$NON-NLS-1$
    }

    @Override
    public R visitBreakStatement(
            BreakStatement elem,
            C context) throws E {
        throw new UnsupportedOperationException("BreakStatement"); //$NON-NLS-1$
    }

    @Override
    public R visitCastExpression(
            CastExpression elem,
            C context) throws E {
        throw new UnsupportedOperationException("CastExpression"); //$NON-NLS-1$
    }

    @Override
    public R visitCatchClause(
            CatchClause elem,
            C context) throws E {
        throw new UnsupportedOperationException("CatchClause"); //$NON-NLS-1$
    }

    @Override
    public R visitClassBody(
            ClassBody elem,
            C context) throws E {
        throw new UnsupportedOperationException("ClassBody"); //$NON-NLS-1$
    }

    @Override
    public R visitClassDeclaration(
            ClassDeclaration elem,
            C context) throws E {
        throw new UnsupportedOperationException("ClassDeclaration"); //$NON-NLS-1$
    }

    @Override
    public R visitClassInstanceCreationExpression(
            ClassInstanceCreationExpression elem,
            C context) throws E {
        throw new UnsupportedOperationException("ClassInstanceCreationExpression"); //$NON-NLS-1$
    }

    @Override
    public R visitClassLiteral(
            ClassLiteral elem,
            C context) throws E {
        throw new UnsupportedOperationException("ClassLiteral"); //$NON-NLS-1$
    }

    @Override
    public R visitCompilationUnit(
            CompilationUnit elem,
            C context) throws E {
        throw new UnsupportedOperationException("CompilationUnit"); //$NON-NLS-1$
    }

    @Override
    public R visitConditionalExpression(
            ConditionalExpression elem,
            C context) throws E {
        throw new UnsupportedOperationException("ConditionalExpression"); //$NON-NLS-1$
    }

    @Override
    public R visitConstructorDeclaration(
            ConstructorDeclaration elem,
            C context) throws E {
        throw new UnsupportedOperationException("ConstructorDeclaration"); //$NON-NLS-1$
    }

    @Override
    public R visitContinueStatement(
            ContinueStatement elem,
            C context) throws E {
        throw new UnsupportedOperationException("ContinueStatement"); //$NON-NLS-1$
    }

    @Override
    public R visitDoStatement(
            DoStatement elem,
            C context) throws E {
        throw new UnsupportedOperationException("DoStatement"); //$NON-NLS-1$
    }

    @Override
    public R visitDocBlock(
            DocBlock elem,
            C context) throws E {
        throw new UnsupportedOperationException("DocBlock"); //$NON-NLS-1$
    }

    @Override
    public R visitDocField(
            DocField elem,
            C context) throws E {
        throw new UnsupportedOperationException("DocField"); //$NON-NLS-1$
    }

    @Override
    public R visitDocMethod(
            DocMethod elem,
            C context) throws E {
        throw new UnsupportedOperationException("DocMethod"); //$NON-NLS-1$
    }

    @Override
    public R visitDocMethodParameter(
            DocMethodParameter elem,
            C context) throws E {
        throw new UnsupportedOperationException("DocMethodParameter"); //$NON-NLS-1$
    }

    @Override
    public R visitDocText(
            DocText elem,
            C context) throws E {
        throw new UnsupportedOperationException("DocText"); //$NON-NLS-1$
    }

    @Override
    public R visitEmptyStatement(
            EmptyStatement elem,
            C context) throws E {
        throw new UnsupportedOperationException("EmptyStatement"); //$NON-NLS-1$
    }

    @Override
    public R visitEnhancedForStatement(
            EnhancedForStatement elem,
            C context) throws E {
        throw new UnsupportedOperationException("EnhancedForStatement"); //$NON-NLS-1$
    }

    @Override
    public R visitEnumConstantDeclaration(
            EnumConstantDeclaration elem,
            C context) throws E {
        throw new UnsupportedOperationException("EnumConstantDeclaration"); //$NON-NLS-1$
    }

    @Override
    public R visitEnumDeclaration(
            EnumDeclaration elem,
            C context) throws E {
        throw new UnsupportedOperationException("EnumDeclaration"); //$NON-NLS-1$
    }

    @Override
    public R visitExpressionStatement(
            ExpressionStatement elem,
            C context) throws E {
        throw new UnsupportedOperationException("ExpressionStatement"); //$NON-NLS-1$
    }

    @Override
    public R visitFieldAccessExpression(
            FieldAccessExpression elem,
            C context) throws E {
        throw new UnsupportedOperationException("FieldAccessExpression"); //$NON-NLS-1$
    }

    @Override
    public R visitFieldDeclaration(
            FieldDeclaration elem,
            C context) throws E {
        throw new UnsupportedOperationException("FieldDeclaration"); //$NON-NLS-1$
    }

    @Override
    public R visitForStatement(
            ForStatement elem,
            C context) throws E {
        throw new UnsupportedOperationException("ForStatement"); //$NON-NLS-1$
    }

    @Override
    public R visitFormalParameterDeclaration(
            FormalParameterDeclaration elem,
            C context) throws E {
        throw new UnsupportedOperationException("FormalParameterDeclaration"); //$NON-NLS-1$
    }

    @Override
    public R visitIfStatement(
            IfStatement elem,
            C context) throws E {
        throw new UnsupportedOperationException("IfStatement"); //$NON-NLS-1$
    }

    @Override
    public R visitImportDeclaration(
            ImportDeclaration elem,
            C context) throws E {
        throw new UnsupportedOperationException("ImportDeclaration"); //$NON-NLS-1$
    }

    @Override
    public R visitInfixExpression(
            InfixExpression elem,
            C context) throws E {
        throw new UnsupportedOperationException("InfixExpression"); //$NON-NLS-1$
    }

    @Override
    public R visitInitializerDeclaration(
            InitializerDeclaration elem,
            C context) throws E {
        throw new UnsupportedOperationException("InitializerDeclaration"); //$NON-NLS-1$
    }

    @Override
    public R visitInstanceofExpression(
            InstanceofExpression elem,
            C context) throws E {
        throw new UnsupportedOperationException("InstanceofExpression"); //$NON-NLS-1$
    }

    @Override
    public R visitInterfaceDeclaration(
            InterfaceDeclaration elem,
            C context) throws E {
        throw new UnsupportedOperationException("InterfaceDeclaration"); //$NON-NLS-1$
    }

    @Override
    public R visitJavadoc(
            Javadoc elem,
            C context) throws E {
        throw new UnsupportedOperationException("Javadoc"); //$NON-NLS-1$
    }

    @Override
    public R visitLabeledStatement(
            LabeledStatement elem,
            C context) throws E {
        throw new UnsupportedOperationException("LabeledStatement"); //$NON-NLS-1$
    }

    @Override
    public R visitLambdaExpression(
            LambdaExpression elem,
            C context) throws E {
        throw new UnsupportedOperationException("LambdaExpression"); //$NON-NLS-1$
    }

    @Override
    public R visitLineComment(
            LineComment elem,
            C context) throws E {
        throw new UnsupportedOperationException("LineComment"); //$NON-NLS-1$
    }

    @Override
    public R visitLiteral(
            Literal elem,
            C context) throws E {
        throw new UnsupportedOperationException("Literal"); //$NON-NLS-1$
    }

    @Override
    public R visitLocalClassDeclaration(
            LocalClassDeclaration elem,
            C context) throws E {
        throw new UnsupportedOperationException("LocalClassDeclaration"); //$NON-NLS-1$
    }

    @Override
    public R visitLocalVariableDeclaration(
            LocalVariableDeclaration elem,
            C context) throws E {
        throw new UnsupportedOperationException("LocalVariableDeclaration"); //$NON-NLS-1$
    }

    @Override
    public R visitMarkerAnnotation(
            MarkerAnnotation elem,
            C context) throws E {
        throw new UnsupportedOperationException("MarkerAnnotation"); //$NON-NLS-1$
    }

    @Override
    public R visitMethodDeclaration(
            MethodDeclaration elem,
            C context) throws E {
        throw new UnsupportedOperationException("MethodDeclaration"); //$NON-NLS-1$
    }

    @Override
    public R visitMethodInvocationExpression(
            MethodInvocationExpression elem,
            C context) throws E {
        throw new UnsupportedOperationException("MethodInvocationExpression"); //$NON-NLS-1$
    }

    @Override
    public R visitConstructorReferenceExpression(
            ConstructorReferenceExpression elem,
            C context) throws E {
        throw new UnsupportedOperationException("ConstructorReferenceExpression"); //$NON-NLS-1$
    }

    @Override
    public R visitMethodReferenceExpression(
            MethodReferenceExpression elem,
            C context) throws E {
        throw new UnsupportedOperationException("MethodReferenceExpression"); //$NON-NLS-1$
    }

    @Override
    public R visitModifier(
            Modifier elem,
            C context) throws E {
        throw new UnsupportedOperationException("Modifier"); //$NON-NLS-1$
    }

    @Override
    public R visitNamedType(
            NamedType elem,
            C context) throws E {
        throw new UnsupportedOperationException("NamedType"); //$NON-NLS-1$
    }

    @Override
    public R visitNormalAnnotation(
            NormalAnnotation elem,
            C context) throws E {
        throw new UnsupportedOperationException("NormalAnnotation"); //$NON-NLS-1$
    }

    @Override
    public R visitPackageDeclaration(
            PackageDeclaration elem,
            C context) throws E {
        throw new UnsupportedOperationException("PackageDeclaration"); //$NON-NLS-1$
    }

    @Override
    public R visitParameterizedType(
            ParameterizedType elem,
            C context) throws E {
        throw new UnsupportedOperationException("ParameterizedType"); //$NON-NLS-1$
    }

    @Override
    public R visitParenthesizedExpression(
            ParenthesizedExpression elem,
            C context) throws E {
        throw new UnsupportedOperationException("ParenthesizedExpression"); //$NON-NLS-1$
    }

    @Override
    public R visitPostfixExpression(
            PostfixExpression elem,
            C context) throws E {
        throw new UnsupportedOperationException("PostfixExpression"); //$NON-NLS-1$
    }

    @Override
    public R visitQualifiedName(
            QualifiedName elem,
            C context) throws E {
        throw new UnsupportedOperationException("QualifiedName"); //$NON-NLS-1$
    }

    @Override
    public R visitQualifiedType(
            QualifiedType elem,
            C context) throws E {
        throw new UnsupportedOperationException("QualifiedType"); //$NON-NLS-1$
    }

    @Override
    public R visitReturnStatement(
            ReturnStatement elem,
            C context) throws E {
        throw new UnsupportedOperationException("ReturnStatement"); //$NON-NLS-1$
    }

    @Override
    public R visitSimpleName(
            SimpleName elem,
            C context) throws E {
        throw new UnsupportedOperationException("SimpleName"); //$NON-NLS-1$
    }

    @Override
    public R visitSingleElementAnnotation(
            SingleElementAnnotation elem,
            C context) throws E {
        throw new UnsupportedOperationException("SingleElementAnnotation"); //$NON-NLS-1$
    }

    @Override
    public R visitStatementExpressionList(
            StatementExpressionList elem,
            C context) throws E {
        throw new UnsupportedOperationException("StatementExpressionList"); //$NON-NLS-1$
    }

    @Override
    public R visitSuper(
            Super elem,
            C context) throws E {
        throw new UnsupportedOperationException("Super"); //$NON-NLS-1$
    }

    @Override
    public R visitSuperConstructorInvocation(
            SuperConstructorInvocation elem,
            C context) throws E {
        throw new UnsupportedOperationException("SuperConstructorInvocation"); //$NON-NLS-1$
    }

    @Override
    public R visitSwitchCaseLabel(
            SwitchCaseLabel elem,
            C context) throws E {
        throw new UnsupportedOperationException("SwitchCaseLabel"); //$NON-NLS-1$
    }

    @Override
    public R visitSwitchDefaultLabel(
            SwitchDefaultLabel elem,
            C context) throws E {
        throw new UnsupportedOperationException("SwitchDefaultLabel"); //$NON-NLS-1$
    }

    @Override
    public R visitSwitchStatement(
            SwitchStatement elem,
            C context) throws E {
        throw new UnsupportedOperationException("SwitchStatement"); //$NON-NLS-1$
    }

    @Override
    public R visitSynchronizedStatement(
            SynchronizedStatement elem,
            C context) throws E {
        throw new UnsupportedOperationException("SynchronizedStatement"); //$NON-NLS-1$
    }

    @Override
    public R visitThis(
            This elem,
            C context) throws E {
        throw new UnsupportedOperationException("This"); //$NON-NLS-1$
    }

    @Override
    public R visitThrowStatement(
            ThrowStatement elem,
            C context) throws E {
        throw new UnsupportedOperationException("ThrowStatement"); //$NON-NLS-1$
    }

    @Override
    public R visitTryResource(
            TryResource elem,
            C context) throws E {
        throw new UnsupportedOperationException("TryResource"); //$NON-NLS-1$
    }

    @Override
    public R visitTryStatement(
            TryStatement elem,
            C context) throws E {
        throw new UnsupportedOperationException("TryStatement"); //$NON-NLS-1$
    }

    @Override
    public R visitTypeParameterDeclaration(
            TypeParameterDeclaration elem,
            C context) throws E {
        throw new UnsupportedOperationException("TypeParameterDeclaration"); //$NON-NLS-1$
    }

    @Override
    public R visitUnaryExpression(
            UnaryExpression elem,
            C context) throws E {
        throw new UnsupportedOperationException("UnaryExpression"); //$NON-NLS-1$
    }

    @Override
    public R visitUnionType(
            UnionType elem,
            C context) throws E {
        throw new UnsupportedOperationException("UnionType"); //$NON-NLS-1$
    }

    @Override
    public R visitVariableDeclarator(
            VariableDeclarator elem,
            C context) throws E {
        throw new UnsupportedOperationException("VariableDeclarator"); //$NON-NLS-1$
    }

    @Override
    public R visitWhileStatement(
            WhileStatement elem,
            C context) throws E {
        throw new UnsupportedOperationException("WhileStatement"); //$NON-NLS-1$
    }

    @Override
    public R visitWildcard(
            Wildcard elem,
            C context) throws E {
        throw new UnsupportedOperationException("Wildcard"); //$NON-NLS-1$
    }
}
