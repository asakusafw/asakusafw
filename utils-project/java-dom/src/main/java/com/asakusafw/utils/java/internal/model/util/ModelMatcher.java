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
package com.asakusafw.utils.java.internal.model.util;

import java.util.List;

import com.asakusafw.utils.java.model.syntax.*;
import com.asakusafw.utils.java.model.util.NoThrow;

/**
 * {@link Model}どうしを比較するビジタ。
 */
public final class ModelMatcher extends StrictVisitor<Boolean, Model, NoThrow> {

    /**
     * このクラスのインスタンス。
     */
    public static final ModelMatcher INSTANCE = new ModelMatcher();

    private ModelMatcher() {
        // only for the singleton instance
    }

    @Override
    public Boolean visitAlternateConstructorInvocation(
            AlternateConstructorInvocation elem,
            Model context) {
        if (elem.getModelKind() != context.getModelKind()) {
            return Boolean.FALSE;
        }
        AlternateConstructorInvocation that = (AlternateConstructorInvocation) context;
        if (Boolean.FALSE.equals(match(elem.getTypeArguments(), that.getTypeArguments()))) {
            return Boolean.FALSE;
        }
        if (Boolean.FALSE.equals(match(elem.getArguments(), that.getArguments()))) {
            return Boolean.FALSE;
        }
        return Boolean.TRUE;
    }

    @Override
    public Boolean visitAnnotationDeclaration(
            AnnotationDeclaration elem,
            Model context) {
        if (elem.getModelKind() != context.getModelKind()) {
            return Boolean.FALSE;
        }
        AnnotationDeclaration that = (AnnotationDeclaration) context;
        if (Boolean.FALSE.equals(match(elem.getJavadoc(), that.getJavadoc()))) {
            return Boolean.FALSE;
        }
        if (Boolean.FALSE.equals(match(elem.getModifiers(), that.getModifiers()))) {
            return Boolean.FALSE;
        }
        if (Boolean.FALSE.equals(match(elem.getName(), that.getName()))) {
            return Boolean.FALSE;
        }
        if (Boolean.FALSE.equals(match(elem.getBodyDeclarations(), that.getBodyDeclarations()))) {
            return Boolean.FALSE;
        }
        return Boolean.TRUE;
    }

    @Override
    public Boolean visitAnnotationElement(
            AnnotationElement elem,
            Model context) {
        if (elem.getModelKind() != context.getModelKind()) {
            return Boolean.FALSE;
        }
        AnnotationElement that = (AnnotationElement) context;
        if (Boolean.FALSE.equals(match(elem.getName(), that.getName()))) {
            return Boolean.FALSE;
        }
        if (Boolean.FALSE.equals(match(elem.getExpression(), that.getExpression()))) {
            return Boolean.FALSE;
        }
        return Boolean.TRUE;
    }

    @Override
    public Boolean visitAnnotationElementDeclaration(
            AnnotationElementDeclaration elem,
            Model context) {
        if (elem.getModelKind() != context.getModelKind()) {
            return Boolean.FALSE;
        }
        AnnotationElementDeclaration that = (AnnotationElementDeclaration) context;
        if (Boolean.FALSE.equals(match(elem.getJavadoc(), that.getJavadoc()))) {
            return Boolean.FALSE;
        }
        if (Boolean.FALSE.equals(match(elem.getModifiers(), that.getModifiers()))) {
            return Boolean.FALSE;
        }
        if (Boolean.FALSE.equals(match(elem.getType(), that.getType()))) {
            return Boolean.FALSE;
        }
        if (Boolean.FALSE.equals(match(elem.getName(), that.getName()))) {
            return Boolean.FALSE;
        }
        if (Boolean.FALSE.equals(match(elem.getDefaultExpression(), that.getDefaultExpression()))) {
            return Boolean.FALSE;
        }
        return Boolean.TRUE;
    }

    @Override
    public Boolean visitArrayAccessExpression(
            ArrayAccessExpression elem,
            Model context) {
        if (elem.getModelKind() != context.getModelKind()) {
            return Boolean.FALSE;
        }
        ArrayAccessExpression that = (ArrayAccessExpression) context;
        if (Boolean.FALSE.equals(match(elem.getArray(), that.getArray()))) {
            return Boolean.FALSE;
        }
        if (Boolean.FALSE.equals(match(elem.getIndex(), that.getIndex()))) {
            return Boolean.FALSE;
        }
        return Boolean.TRUE;
    }

    @Override
    public Boolean visitArrayCreationExpression(
            ArrayCreationExpression elem,
            Model context) {
        if (elem.getModelKind() != context.getModelKind()) {
            return Boolean.FALSE;
        }
        ArrayCreationExpression that = (ArrayCreationExpression) context;
        if (Boolean.FALSE.equals(match(elem.getType(), that.getType()))) {
            return Boolean.FALSE;
        }
        if (Boolean.FALSE.equals(match(elem.getDimensionExpressions(), that.getDimensionExpressions()))) {
            return Boolean.FALSE;
        }
        if (Boolean.FALSE.equals(match(elem.getArrayInitializer(), that.getArrayInitializer()))) {
            return Boolean.FALSE;
        }
        return Boolean.TRUE;
    }

    @Override
    public Boolean visitArrayInitializer(
            ArrayInitializer elem,
            Model context) {
        if (elem.getModelKind() != context.getModelKind()) {
            return Boolean.FALSE;
        }
        ArrayInitializer that = (ArrayInitializer) context;
        if (Boolean.FALSE.equals(match(elem.getElements(), that.getElements()))) {
            return Boolean.FALSE;
        }
        return Boolean.TRUE;
    }

    @Override
    public Boolean visitArrayType(
            ArrayType elem,
            Model context) {
        if (elem.getModelKind() != context.getModelKind()) {
            return Boolean.FALSE;
        }
        ArrayType that = (ArrayType) context;
        if (Boolean.FALSE.equals(match(elem.getComponentType(), that.getComponentType()))) {
            return Boolean.FALSE;
        }
        return Boolean.TRUE;
    }

    @Override
    public Boolean visitAssertStatement(
            AssertStatement elem,
            Model context) {
        if (elem.getModelKind() != context.getModelKind()) {
            return Boolean.FALSE;
        }
        AssertStatement that = (AssertStatement) context;
        if (Boolean.FALSE.equals(match(elem.getExpression(), that.getExpression()))) {
            return Boolean.FALSE;
        }
        if (Boolean.FALSE.equals(match(elem.getMessage(), that.getMessage()))) {
            return Boolean.FALSE;
        }
        return Boolean.TRUE;
    }

    @Override
    public Boolean visitAssignmentExpression(
            AssignmentExpression elem,
            Model context) {
        if (elem.getModelKind() != context.getModelKind()) {
            return Boolean.FALSE;
        }
        AssignmentExpression that = (AssignmentExpression) context;
        if (Boolean.FALSE.equals(match(elem.getLeftHandSide(), that.getLeftHandSide()))) {
            return Boolean.FALSE;
        }
        if (Boolean.FALSE.equals(match(elem.getOperator(), that.getOperator()))) {
            return Boolean.FALSE;
        }
        if (Boolean.FALSE.equals(match(elem.getRightHandSide(), that.getRightHandSide()))) {
            return Boolean.FALSE;
        }
        return Boolean.TRUE;
    }

    @Override
    public Boolean visitBasicType(
            BasicType elem,
            Model context) {
        if (elem.getModelKind() != context.getModelKind()) {
            return Boolean.FALSE;
        }
        BasicType that = (BasicType) context;
        if (Boolean.FALSE.equals(match(elem.getTypeKind(), that.getTypeKind()))) {
            return Boolean.FALSE;
        }
        return Boolean.TRUE;
    }

    @Override
    public Boolean visitBlock(
            Block elem,
            Model context) {
        if (elem.getModelKind() != context.getModelKind()) {
            return Boolean.FALSE;
        }
        Block that = (Block) context;
        if (Boolean.FALSE.equals(match(elem.getStatements(), that.getStatements()))) {
            return Boolean.FALSE;
        }
        return Boolean.TRUE;
    }

    @Override
    public Boolean visitBlockComment(
            BlockComment elem,
            Model context) {
        if (elem.getModelKind() != context.getModelKind()) {
            return Boolean.FALSE;
        }
        BlockComment that = (BlockComment) context;
        if (Boolean.FALSE.equals(match(elem.getString(), that.getString()))) {
            return Boolean.FALSE;
        }
        return Boolean.TRUE;
    }

    @Override
    public Boolean visitBreakStatement(
            BreakStatement elem,
            Model context) {
        if (elem.getModelKind() != context.getModelKind()) {
            return Boolean.FALSE;
        }
        BreakStatement that = (BreakStatement) context;
        if (Boolean.FALSE.equals(match(elem.getTarget(), that.getTarget()))) {
            return Boolean.FALSE;
        }
        return Boolean.TRUE;
    }

    @Override
    public Boolean visitCastExpression(
            CastExpression elem,
            Model context) {
        if (elem.getModelKind() != context.getModelKind()) {
            return Boolean.FALSE;
        }
        CastExpression that = (CastExpression) context;
        if (Boolean.FALSE.equals(match(elem.getType(), that.getType()))) {
            return Boolean.FALSE;
        }
        if (Boolean.FALSE.equals(match(elem.getExpression(), that.getExpression()))) {
            return Boolean.FALSE;
        }
        return Boolean.TRUE;
    }

    @Override
    public Boolean visitCatchClause(
            CatchClause elem,
            Model context) {
        if (elem.getModelKind() != context.getModelKind()) {
            return Boolean.FALSE;
        }
        CatchClause that = (CatchClause) context;
        if (Boolean.FALSE.equals(match(elem.getParameter(), that.getParameter()))) {
            return Boolean.FALSE;
        }
        if (Boolean.FALSE.equals(match(elem.getBody(), that.getBody()))) {
            return Boolean.FALSE;
        }
        return Boolean.TRUE;
    }

    @Override
    public Boolean visitClassBody(
            ClassBody elem,
            Model context) {
        if (elem.getModelKind() != context.getModelKind()) {
            return Boolean.FALSE;
        }
        ClassBody that = (ClassBody) context;
        if (Boolean.FALSE.equals(match(elem.getBodyDeclarations(), that.getBodyDeclarations()))) {
            return Boolean.FALSE;
        }
        return Boolean.TRUE;
    }

    @Override
    public Boolean visitClassDeclaration(
            ClassDeclaration elem,
            Model context) {
        if (elem.getModelKind() != context.getModelKind()) {
            return Boolean.FALSE;
        }
        ClassDeclaration that = (ClassDeclaration) context;
        if (Boolean.FALSE.equals(match(elem.getJavadoc(), that.getJavadoc()))) {
            return Boolean.FALSE;
        }
        if (Boolean.FALSE.equals(match(elem.getModifiers(), that.getModifiers()))) {
            return Boolean.FALSE;
        }
        if (Boolean.FALSE.equals(match(elem.getName(), that.getName()))) {
            return Boolean.FALSE;
        }
        if (Boolean.FALSE.equals(match(elem.getTypeParameters(), that.getTypeParameters()))) {
            return Boolean.FALSE;
        }
        if (Boolean.FALSE.equals(match(elem.getSuperClass(), that.getSuperClass()))) {
            return Boolean.FALSE;
        }
        if (Boolean.FALSE.equals(match(elem.getSuperInterfaceTypes(), that.getSuperInterfaceTypes()))) {
            return Boolean.FALSE;
        }
        if (Boolean.FALSE.equals(match(elem.getBodyDeclarations(), that.getBodyDeclarations()))) {
            return Boolean.FALSE;
        }
        return Boolean.TRUE;
    }

    @Override
    public Boolean visitClassInstanceCreationExpression(
            ClassInstanceCreationExpression elem,
            Model context) {
        if (elem.getModelKind() != context.getModelKind()) {
            return Boolean.FALSE;
        }
        ClassInstanceCreationExpression that = (ClassInstanceCreationExpression) context;
        if (Boolean.FALSE.equals(match(elem.getQualifier(), that.getQualifier()))) {
            return Boolean.FALSE;
        }
        if (Boolean.FALSE.equals(match(elem.getTypeArguments(), that.getTypeArguments()))) {
            return Boolean.FALSE;
        }
        if (Boolean.FALSE.equals(match(elem.getType(), that.getType()))) {
            return Boolean.FALSE;
        }
        if (Boolean.FALSE.equals(match(elem.getArguments(), that.getArguments()))) {
            return Boolean.FALSE;
        }
        if (Boolean.FALSE.equals(match(elem.getBody(), that.getBody()))) {
            return Boolean.FALSE;
        }
        return Boolean.TRUE;
    }

    @Override
    public Boolean visitClassLiteral(
            ClassLiteral elem,
            Model context) {
        if (elem.getModelKind() != context.getModelKind()) {
            return Boolean.FALSE;
        }
        ClassLiteral that = (ClassLiteral) context;
        if (Boolean.FALSE.equals(match(elem.getType(), that.getType()))) {
            return Boolean.FALSE;
        }
        return Boolean.TRUE;
    }

    @Override
    public Boolean visitCompilationUnit(
            CompilationUnit elem,
            Model context) {
        if (elem.getModelKind() != context.getModelKind()) {
            return Boolean.FALSE;
        }
        CompilationUnit that = (CompilationUnit) context;
        if (Boolean.FALSE.equals(match(elem.getPackageDeclaration(), that.getPackageDeclaration()))) {
            return Boolean.FALSE;
        }
        if (Boolean.FALSE.equals(match(elem.getImportDeclarations(), that.getImportDeclarations()))) {
            return Boolean.FALSE;
        }
        if (Boolean.FALSE.equals(match(elem.getTypeDeclarations(), that.getTypeDeclarations()))) {
            return Boolean.FALSE;
        }
        if (Boolean.FALSE.equals(match(elem.getComments(), that.getComments()))) {
            return Boolean.FALSE;
        }
        return Boolean.TRUE;
    }

    @Override
    public Boolean visitConditionalExpression(
            ConditionalExpression elem,
            Model context) {
        if (elem.getModelKind() != context.getModelKind()) {
            return Boolean.FALSE;
        }
        ConditionalExpression that = (ConditionalExpression) context;
        if (Boolean.FALSE.equals(match(elem.getCondition(), that.getCondition()))) {
            return Boolean.FALSE;
        }
        if (Boolean.FALSE.equals(match(elem.getThenExpression(), that.getThenExpression()))) {
            return Boolean.FALSE;
        }
        if (Boolean.FALSE.equals(match(elem.getElseExpression(), that.getElseExpression()))) {
            return Boolean.FALSE;
        }
        return Boolean.TRUE;
    }

    @Override
    public Boolean visitConstructorDeclaration(
            ConstructorDeclaration elem,
            Model context) {
        if (elem.getModelKind() != context.getModelKind()) {
            return Boolean.FALSE;
        }
        ConstructorDeclaration that = (ConstructorDeclaration) context;
        if (Boolean.FALSE.equals(match(elem.getJavadoc(), that.getJavadoc()))) {
            return Boolean.FALSE;
        }
        if (Boolean.FALSE.equals(match(elem.getModifiers(), that.getModifiers()))) {
            return Boolean.FALSE;
        }
        if (Boolean.FALSE.equals(match(elem.getTypeParameters(), that.getTypeParameters()))) {
            return Boolean.FALSE;
        }
        if (Boolean.FALSE.equals(match(elem.getName(), that.getName()))) {
            return Boolean.FALSE;
        }
        if (Boolean.FALSE.equals(match(elem.getFormalParameters(), that.getFormalParameters()))) {
            return Boolean.FALSE;
        }
        if (Boolean.FALSE.equals(match(elem.getExceptionTypes(), that.getExceptionTypes()))) {
            return Boolean.FALSE;
        }
        if (Boolean.FALSE.equals(match(elem.getBody(), that.getBody()))) {
            return Boolean.FALSE;
        }
        return Boolean.TRUE;
    }

    @Override
    public Boolean visitContinueStatement(
            ContinueStatement elem,
            Model context) {
        if (elem.getModelKind() != context.getModelKind()) {
            return Boolean.FALSE;
        }
        ContinueStatement that = (ContinueStatement) context;
        if (Boolean.FALSE.equals(match(elem.getTarget(), that.getTarget()))) {
            return Boolean.FALSE;
        }
        return Boolean.TRUE;
    }

    @Override
    public Boolean visitDoStatement(
            DoStatement elem,
            Model context) {
        if (elem.getModelKind() != context.getModelKind()) {
            return Boolean.FALSE;
        }
        DoStatement that = (DoStatement) context;
        if (Boolean.FALSE.equals(match(elem.getBody(), that.getBody()))) {
            return Boolean.FALSE;
        }
        if (Boolean.FALSE.equals(match(elem.getCondition(), that.getCondition()))) {
            return Boolean.FALSE;
        }
        return Boolean.TRUE;
    }

    @Override
    public Boolean visitDocBlock(
            DocBlock elem,
            Model context) {
        if (elem.getModelKind() != context.getModelKind()) {
            return Boolean.FALSE;
        }
        DocBlock that = (DocBlock) context;
        if (Boolean.FALSE.equals(match(elem.getTag(), that.getTag()))) {
            return Boolean.FALSE;
        }
        if (Boolean.FALSE.equals(match(elem.getElements(), that.getElements()))) {
            return Boolean.FALSE;
        }
        return Boolean.TRUE;
    }

    @Override
    public Boolean visitDocField(
            DocField elem,
            Model context) {
        if (elem.getModelKind() != context.getModelKind()) {
            return Boolean.FALSE;
        }
        DocField that = (DocField) context;
        if (Boolean.FALSE.equals(match(elem.getType(), that.getType()))) {
            return Boolean.FALSE;
        }
        if (Boolean.FALSE.equals(match(elem.getName(), that.getName()))) {
            return Boolean.FALSE;
        }
        return Boolean.TRUE;
    }

    @Override
    public Boolean visitDocMethod(
            DocMethod elem,
            Model context) {
        if (elem.getModelKind() != context.getModelKind()) {
            return Boolean.FALSE;
        }
        DocMethod that = (DocMethod) context;
        if (Boolean.FALSE.equals(match(elem.getType(), that.getType()))) {
            return Boolean.FALSE;
        }
        if (Boolean.FALSE.equals(match(elem.getName(), that.getName()))) {
            return Boolean.FALSE;
        }
        if (Boolean.FALSE.equals(match(elem.getFormalParameters(), that.getFormalParameters()))) {
            return Boolean.FALSE;
        }
        return Boolean.TRUE;
    }

    @Override
    public Boolean visitDocMethodParameter(
            DocMethodParameter elem,
            Model context) {
        if (elem.getModelKind() != context.getModelKind()) {
            return Boolean.FALSE;
        }
        DocMethodParameter that = (DocMethodParameter) context;
        if (Boolean.FALSE.equals(match(elem.getType(), that.getType()))) {
            return Boolean.FALSE;
        }
        if (Boolean.FALSE.equals(match(elem.getName(), that.getName()))) {
            return Boolean.FALSE;
        }
        if (Boolean.FALSE.equals(match(elem.isVariableArity(), that.isVariableArity()))) {
            return Boolean.FALSE;
        }
        return Boolean.TRUE;
    }

    @Override
    public Boolean visitDocText(
            DocText elem,
            Model context) {
        if (elem.getModelKind() != context.getModelKind()) {
            return Boolean.FALSE;
        }
        DocText that = (DocText) context;
        if (Boolean.FALSE.equals(match(elem.getString(), that.getString()))) {
            return Boolean.FALSE;
        }
        return Boolean.TRUE;
    }

    @Override
    public Boolean visitEmptyStatement(
            EmptyStatement elem,
            Model context) {
        if (elem.getModelKind() != context.getModelKind()) {
            return Boolean.FALSE;
        }
        return Boolean.TRUE;
    }

    @Override
    public Boolean visitEnhancedForStatement(
            EnhancedForStatement elem,
            Model context) {
        if (elem.getModelKind() != context.getModelKind()) {
            return Boolean.FALSE;
        }
        EnhancedForStatement that = (EnhancedForStatement) context;
        if (Boolean.FALSE.equals(match(elem.getParameter(), that.getParameter()))) {
            return Boolean.FALSE;
        }
        if (Boolean.FALSE.equals(match(elem.getExpression(), that.getExpression()))) {
            return Boolean.FALSE;
        }
        if (Boolean.FALSE.equals(match(elem.getBody(), that.getBody()))) {
            return Boolean.FALSE;
        }
        return Boolean.TRUE;
    }

    @Override
    public Boolean visitEnumConstantDeclaration(
            EnumConstantDeclaration elem,
            Model context) {
        if (elem.getModelKind() != context.getModelKind()) {
            return Boolean.FALSE;
        }
        EnumConstantDeclaration that = (EnumConstantDeclaration) context;
        if (Boolean.FALSE.equals(match(elem.getJavadoc(), that.getJavadoc()))) {
            return Boolean.FALSE;
        }
        if (Boolean.FALSE.equals(match(elem.getModifiers(), that.getModifiers()))) {
            return Boolean.FALSE;
        }
        if (Boolean.FALSE.equals(match(elem.getName(), that.getName()))) {
            return Boolean.FALSE;
        }
        if (Boolean.FALSE.equals(match(elem.getArguments(), that.getArguments()))) {
            return Boolean.FALSE;
        }
        if (Boolean.FALSE.equals(match(elem.getBody(), that.getBody()))) {
            return Boolean.FALSE;
        }
        return Boolean.TRUE;
    }

    @Override
    public Boolean visitEnumDeclaration(
            EnumDeclaration elem,
            Model context) {
        if (elem.getModelKind() != context.getModelKind()) {
            return Boolean.FALSE;
        }
        EnumDeclaration that = (EnumDeclaration) context;
        if (Boolean.FALSE.equals(match(elem.getJavadoc(), that.getJavadoc()))) {
            return Boolean.FALSE;
        }
        if (Boolean.FALSE.equals(match(elem.getModifiers(), that.getModifiers()))) {
            return Boolean.FALSE;
        }
        if (Boolean.FALSE.equals(match(elem.getName(), that.getName()))) {
            return Boolean.FALSE;
        }
        if (Boolean.FALSE.equals(match(elem.getSuperInterfaceTypes(), that.getSuperInterfaceTypes()))) {
            return Boolean.FALSE;
        }
        if (Boolean.FALSE.equals(match(elem.getConstantDeclarations(), that.getConstantDeclarations()))) {
            return Boolean.FALSE;
        }
        if (Boolean.FALSE.equals(match(elem.getBodyDeclarations(), that.getBodyDeclarations()))) {
            return Boolean.FALSE;
        }
        return Boolean.TRUE;
    }

    @Override
    public Boolean visitExpressionStatement(
            ExpressionStatement elem,
            Model context) {
        if (elem.getModelKind() != context.getModelKind()) {
            return Boolean.FALSE;
        }
        ExpressionStatement that = (ExpressionStatement) context;
        if (Boolean.FALSE.equals(match(elem.getExpression(), that.getExpression()))) {
            return Boolean.FALSE;
        }
        return Boolean.TRUE;
    }

    @Override
    public Boolean visitFieldAccessExpression(
            FieldAccessExpression elem,
            Model context) {
        if (elem.getModelKind() != context.getModelKind()) {
            return Boolean.FALSE;
        }
        FieldAccessExpression that = (FieldAccessExpression) context;
        if (Boolean.FALSE.equals(match(elem.getQualifier(), that.getQualifier()))) {
            return Boolean.FALSE;
        }
        if (Boolean.FALSE.equals(match(elem.getName(), that.getName()))) {
            return Boolean.FALSE;
        }
        return Boolean.TRUE;
    }

    @Override
    public Boolean visitFieldDeclaration(
            FieldDeclaration elem,
            Model context) {
        if (elem.getModelKind() != context.getModelKind()) {
            return Boolean.FALSE;
        }
        FieldDeclaration that = (FieldDeclaration) context;
        if (Boolean.FALSE.equals(match(elem.getJavadoc(), that.getJavadoc()))) {
            return Boolean.FALSE;
        }
        if (Boolean.FALSE.equals(match(elem.getModifiers(), that.getModifiers()))) {
            return Boolean.FALSE;
        }
        if (Boolean.FALSE.equals(match(elem.getType(), that.getType()))) {
            return Boolean.FALSE;
        }
        if (Boolean.FALSE.equals(match(elem.getVariableDeclarators(), that.getVariableDeclarators()))) {
            return Boolean.FALSE;
        }
        return Boolean.TRUE;
    }

    @Override
    public Boolean visitForStatement(
            ForStatement elem,
            Model context) {
        if (elem.getModelKind() != context.getModelKind()) {
            return Boolean.FALSE;
        }
        ForStatement that = (ForStatement) context;
        if (Boolean.FALSE.equals(match(elem.getInitialization(), that.getInitialization()))) {
            return Boolean.FALSE;
        }
        if (Boolean.FALSE.equals(match(elem.getCondition(), that.getCondition()))) {
            return Boolean.FALSE;
        }
        if (Boolean.FALSE.equals(match(elem.getUpdate(), that.getUpdate()))) {
            return Boolean.FALSE;
        }
        if (Boolean.FALSE.equals(match(elem.getBody(), that.getBody()))) {
            return Boolean.FALSE;
        }
        return Boolean.TRUE;
    }

    @Override
    public Boolean visitFormalParameterDeclaration(
            FormalParameterDeclaration elem,
            Model context) {
        if (elem.getModelKind() != context.getModelKind()) {
            return Boolean.FALSE;
        }
        FormalParameterDeclaration that = (FormalParameterDeclaration) context;
        if (Boolean.FALSE.equals(match(elem.getModifiers(), that.getModifiers()))) {
            return Boolean.FALSE;
        }
        if (Boolean.FALSE.equals(match(elem.getType(), that.getType()))) {
            return Boolean.FALSE;
        }
        if (Boolean.FALSE.equals(match(elem.isVariableArity(), that.isVariableArity()))) {
            return Boolean.FALSE;
        }
        if (Boolean.FALSE.equals(match(elem.getName(), that.getName()))) {
            return Boolean.FALSE;
        }
        if (Boolean.FALSE.equals(match(elem.getExtraDimensions(), that.getExtraDimensions()))) {
            return Boolean.FALSE;
        }
        return Boolean.TRUE;
    }

    @Override
    public Boolean visitIfStatement(
            IfStatement elem,
            Model context) {
        if (elem.getModelKind() != context.getModelKind()) {
            return Boolean.FALSE;
        }
        IfStatement that = (IfStatement) context;
        if (Boolean.FALSE.equals(match(elem.getCondition(), that.getCondition()))) {
            return Boolean.FALSE;
        }
        if (Boolean.FALSE.equals(match(elem.getThenStatement(), that.getThenStatement()))) {
            return Boolean.FALSE;
        }
        if (Boolean.FALSE.equals(match(elem.getElseStatement(), that.getElseStatement()))) {
            return Boolean.FALSE;
        }
        return Boolean.TRUE;
    }

    @Override
    public Boolean visitImportDeclaration(
            ImportDeclaration elem,
            Model context) {
        if (elem.getModelKind() != context.getModelKind()) {
            return Boolean.FALSE;
        }
        ImportDeclaration that = (ImportDeclaration) context;
        if (Boolean.FALSE.equals(match(elem.getImportKind(), that.getImportKind()))) {
            return Boolean.FALSE;
        }
        if (Boolean.FALSE.equals(match(elem.getName(), that.getName()))) {
            return Boolean.FALSE;
        }
        return Boolean.TRUE;
    }

    @Override
    public Boolean visitInfixExpression(
            InfixExpression elem,
            Model context) {
        if (elem.getModelKind() != context.getModelKind()) {
            return Boolean.FALSE;
        }
        InfixExpression that = (InfixExpression) context;
        if (Boolean.FALSE.equals(match(elem.getLeftOperand(), that.getLeftOperand()))) {
            return Boolean.FALSE;
        }
        if (Boolean.FALSE.equals(match(elem.getOperator(), that.getOperator()))) {
            return Boolean.FALSE;
        }
        if (Boolean.FALSE.equals(match(elem.getRightOperand(), that.getRightOperand()))) {
            return Boolean.FALSE;
        }
        return Boolean.TRUE;
    }

    @Override
    public Boolean visitInitializerDeclaration(
            InitializerDeclaration elem,
            Model context) {
        if (elem.getModelKind() != context.getModelKind()) {
            return Boolean.FALSE;
        }
        InitializerDeclaration that = (InitializerDeclaration) context;
        if (Boolean.FALSE.equals(match(elem.getJavadoc(), that.getJavadoc()))) {
            return Boolean.FALSE;
        }
        if (Boolean.FALSE.equals(match(elem.getModifiers(), that.getModifiers()))) {
            return Boolean.FALSE;
        }
        if (Boolean.FALSE.equals(match(elem.getBody(), that.getBody()))) {
            return Boolean.FALSE;
        }
        return Boolean.TRUE;
    }

    @Override
    public Boolean visitInstanceofExpression(
            InstanceofExpression elem,
            Model context) {
        if (elem.getModelKind() != context.getModelKind()) {
            return Boolean.FALSE;
        }
        InstanceofExpression that = (InstanceofExpression) context;
        if (Boolean.FALSE.equals(match(elem.getExpression(), that.getExpression()))) {
            return Boolean.FALSE;
        }
        if (Boolean.FALSE.equals(match(elem.getType(), that.getType()))) {
            return Boolean.FALSE;
        }
        return Boolean.TRUE;
    }

    @Override
    public Boolean visitInterfaceDeclaration(
            InterfaceDeclaration elem,
            Model context) {
        if (elem.getModelKind() != context.getModelKind()) {
            return Boolean.FALSE;
        }
        InterfaceDeclaration that = (InterfaceDeclaration) context;
        if (Boolean.FALSE.equals(match(elem.getJavadoc(), that.getJavadoc()))) {
            return Boolean.FALSE;
        }
        if (Boolean.FALSE.equals(match(elem.getModifiers(), that.getModifiers()))) {
            return Boolean.FALSE;
        }
        if (Boolean.FALSE.equals(match(elem.getName(), that.getName()))) {
            return Boolean.FALSE;
        }
        if (Boolean.FALSE.equals(match(elem.getTypeParameters(), that.getTypeParameters()))) {
            return Boolean.FALSE;
        }
        if (Boolean.FALSE.equals(match(elem.getSuperInterfaceTypes(), that.getSuperInterfaceTypes()))) {
            return Boolean.FALSE;
        }
        if (Boolean.FALSE.equals(match(elem.getBodyDeclarations(), that.getBodyDeclarations()))) {
            return Boolean.FALSE;
        }
        return Boolean.TRUE;
    }

    @Override
    public Boolean visitJavadoc(
            Javadoc elem,
            Model context) {
        if (elem.getModelKind() != context.getModelKind()) {
            return Boolean.FALSE;
        }
        Javadoc that = (Javadoc) context;
        if (Boolean.FALSE.equals(match(elem.getBlocks(), that.getBlocks()))) {
            return Boolean.FALSE;
        }
        return Boolean.TRUE;
    }

    @Override
    public Boolean visitLabeledStatement(
            LabeledStatement elem,
            Model context) {
        if (elem.getModelKind() != context.getModelKind()) {
            return Boolean.FALSE;
        }
        LabeledStatement that = (LabeledStatement) context;
        if (Boolean.FALSE.equals(match(elem.getLabel(), that.getLabel()))) {
            return Boolean.FALSE;
        }
        if (Boolean.FALSE.equals(match(elem.getBody(), that.getBody()))) {
            return Boolean.FALSE;
        }
        return Boolean.TRUE;
    }

    @Override
    public Boolean visitLineComment(
            LineComment elem,
            Model context) {
        if (elem.getModelKind() != context.getModelKind()) {
            return Boolean.FALSE;
        }
        LineComment that = (LineComment) context;
        if (Boolean.FALSE.equals(match(elem.getString(), that.getString()))) {
            return Boolean.FALSE;
        }
        return Boolean.TRUE;
    }

    @Override
    public Boolean visitLiteral(
            Literal elem,
            Model context) {
        if (elem.getModelKind() != context.getModelKind()) {
            return Boolean.FALSE;
        }
        Literal that = (Literal) context;
        if (Boolean.FALSE.equals(match(elem.getToken(), that.getToken()))) {
            return Boolean.FALSE;
        }
        return Boolean.TRUE;
    }

    @Override
    public Boolean visitLocalClassDeclaration(
            LocalClassDeclaration elem,
            Model context) {
        if (elem.getModelKind() != context.getModelKind()) {
            return Boolean.FALSE;
        }
        LocalClassDeclaration that = (LocalClassDeclaration) context;
        if (Boolean.FALSE.equals(match(elem.getDeclaration(), that.getDeclaration()))) {
            return Boolean.FALSE;
        }
        return Boolean.TRUE;
    }

    @Override
    public Boolean visitLocalVariableDeclaration(
            LocalVariableDeclaration elem,
            Model context) {
        if (elem.getModelKind() != context.getModelKind()) {
            return Boolean.FALSE;
        }
        LocalVariableDeclaration that = (LocalVariableDeclaration) context;
        if (Boolean.FALSE.equals(match(elem.getModifiers(), that.getModifiers()))) {
            return Boolean.FALSE;
        }
        if (Boolean.FALSE.equals(match(elem.getType(), that.getType()))) {
            return Boolean.FALSE;
        }
        if (Boolean.FALSE.equals(match(elem.getVariableDeclarators(), that.getVariableDeclarators()))) {
            return Boolean.FALSE;
        }
        return Boolean.TRUE;
    }

    @Override
    public Boolean visitMarkerAnnotation(
            MarkerAnnotation elem,
            Model context) {
        if (elem.getModelKind() != context.getModelKind()) {
            return Boolean.FALSE;
        }
        MarkerAnnotation that = (MarkerAnnotation) context;
        if (Boolean.FALSE.equals(match(elem.getType(), that.getType()))) {
            return Boolean.FALSE;
        }
        return Boolean.TRUE;
    }

    @Override
    public Boolean visitMethodDeclaration(
            MethodDeclaration elem,
            Model context) {
        if (elem.getModelKind() != context.getModelKind()) {
            return Boolean.FALSE;
        }
        MethodDeclaration that = (MethodDeclaration) context;
        if (Boolean.FALSE.equals(match(elem.getJavadoc(), that.getJavadoc()))) {
            return Boolean.FALSE;
        }
        if (Boolean.FALSE.equals(match(elem.getModifiers(), that.getModifiers()))) {
            return Boolean.FALSE;
        }
        if (Boolean.FALSE.equals(match(elem.getTypeParameters(), that.getTypeParameters()))) {
            return Boolean.FALSE;
        }
        if (Boolean.FALSE.equals(match(elem.getReturnType(), that.getReturnType()))) {
            return Boolean.FALSE;
        }
        if (Boolean.FALSE.equals(match(elem.getName(), that.getName()))) {
            return Boolean.FALSE;
        }
        if (Boolean.FALSE.equals(match(elem.getFormalParameters(), that.getFormalParameters()))) {
            return Boolean.FALSE;
        }
        if (Boolean.FALSE.equals(match(elem.getExtraDimensions(), that.getExtraDimensions()))) {
            return Boolean.FALSE;
        }
        if (Boolean.FALSE.equals(match(elem.getExceptionTypes(), that.getExceptionTypes()))) {
            return Boolean.FALSE;
        }
        if (Boolean.FALSE.equals(match(elem.getBody(), that.getBody()))) {
            return Boolean.FALSE;
        }
        return Boolean.TRUE;
    }

    @Override
    public Boolean visitMethodInvocationExpression(
            MethodInvocationExpression elem,
            Model context) {
        if (elem.getModelKind() != context.getModelKind()) {
            return Boolean.FALSE;
        }
        MethodInvocationExpression that = (MethodInvocationExpression) context;
        if (Boolean.FALSE.equals(match(elem.getQualifier(), that.getQualifier()))) {
            return Boolean.FALSE;
        }
        if (Boolean.FALSE.equals(match(elem.getTypeArguments(), that.getTypeArguments()))) {
            return Boolean.FALSE;
        }
        if (Boolean.FALSE.equals(match(elem.getName(), that.getName()))) {
            return Boolean.FALSE;
        }
        if (Boolean.FALSE.equals(match(elem.getArguments(), that.getArguments()))) {
            return Boolean.FALSE;
        }
        return Boolean.TRUE;
    }

    @Override
    public Boolean visitModifier(
            Modifier elem,
            Model context) {
        if (elem.getModelKind() != context.getModelKind()) {
            return Boolean.FALSE;
        }
        Modifier that = (Modifier) context;
        if (Boolean.FALSE.equals(match(elem.getModifierKind(), that.getModifierKind()))) {
            return Boolean.FALSE;
        }
        return Boolean.TRUE;
    }

    @Override
    public Boolean visitNamedType(
            NamedType elem,
            Model context) {
        if (elem.getModelKind() != context.getModelKind()) {
            return Boolean.FALSE;
        }
        NamedType that = (NamedType) context;
        if (Boolean.FALSE.equals(match(elem.getName(), that.getName()))) {
            return Boolean.FALSE;
        }
        return Boolean.TRUE;
    }

    @Override
    public Boolean visitNormalAnnotation(
            NormalAnnotation elem,
            Model context) {
        if (elem.getModelKind() != context.getModelKind()) {
            return Boolean.FALSE;
        }
        NormalAnnotation that = (NormalAnnotation) context;
        if (Boolean.FALSE.equals(match(elem.getType(), that.getType()))) {
            return Boolean.FALSE;
        }
        if (Boolean.FALSE.equals(match(elem.getElements(), that.getElements()))) {
            return Boolean.FALSE;
        }
        return Boolean.TRUE;
    }

    @Override
    public Boolean visitPackageDeclaration(
            PackageDeclaration elem,
            Model context) {
        if (elem.getModelKind() != context.getModelKind()) {
            return Boolean.FALSE;
        }
        PackageDeclaration that = (PackageDeclaration) context;
        if (Boolean.FALSE.equals(match(elem.getJavadoc(), that.getJavadoc()))) {
            return Boolean.FALSE;
        }
        if (Boolean.FALSE.equals(match(elem.getAnnotations(), that.getAnnotations()))) {
            return Boolean.FALSE;
        }
        if (Boolean.FALSE.equals(match(elem.getName(), that.getName()))) {
            return Boolean.FALSE;
        }
        return Boolean.TRUE;
    }

    @Override
    public Boolean visitParameterizedType(
            ParameterizedType elem,
            Model context) {
        if (elem.getModelKind() != context.getModelKind()) {
            return Boolean.FALSE;
        }
        ParameterizedType that = (ParameterizedType) context;
        if (Boolean.FALSE.equals(match(elem.getType(), that.getType()))) {
            return Boolean.FALSE;
        }
        if (Boolean.FALSE.equals(match(elem.getTypeArguments(), that.getTypeArguments()))) {
            return Boolean.FALSE;
        }
        return Boolean.TRUE;
    }

    @Override
    public Boolean visitParenthesizedExpression(
            ParenthesizedExpression elem,
            Model context) {
        if (elem.getModelKind() != context.getModelKind()) {
            return Boolean.FALSE;
        }
        ParenthesizedExpression that = (ParenthesizedExpression) context;
        if (Boolean.FALSE.equals(match(elem.getExpression(), that.getExpression()))) {
            return Boolean.FALSE;
        }
        return Boolean.TRUE;
    }

    @Override
    public Boolean visitPostfixExpression(
            PostfixExpression elem,
            Model context) {
        if (elem.getModelKind() != context.getModelKind()) {
            return Boolean.FALSE;
        }
        PostfixExpression that = (PostfixExpression) context;
        if (Boolean.FALSE.equals(match(elem.getOperand(), that.getOperand()))) {
            return Boolean.FALSE;
        }
        if (Boolean.FALSE.equals(match(elem.getOperator(), that.getOperator()))) {
            return Boolean.FALSE;
        }
        return Boolean.TRUE;
    }

    @Override
    public Boolean visitQualifiedName(
            QualifiedName elem,
            Model context) {
        if (elem.getModelKind() != context.getModelKind()) {
            return Boolean.FALSE;
        }
        QualifiedName that = (QualifiedName) context;
        if (Boolean.FALSE.equals(match(elem.getQualifier(), that.getQualifier()))) {
            return Boolean.FALSE;
        }
        if (Boolean.FALSE.equals(match(elem.getSimpleName(), that.getSimpleName()))) {
            return Boolean.FALSE;
        }
        return Boolean.TRUE;
    }

    @Override
    public Boolean visitQualifiedType(
            QualifiedType elem,
            Model context) {
        if (elem.getModelKind() != context.getModelKind()) {
            return Boolean.FALSE;
        }
        QualifiedType that = (QualifiedType) context;
        if (Boolean.FALSE.equals(match(elem.getQualifier(), that.getQualifier()))) {
            return Boolean.FALSE;
        }
        if (Boolean.FALSE.equals(match(elem.getSimpleName(), that.getSimpleName()))) {
            return Boolean.FALSE;
        }
        return Boolean.TRUE;
    }

    @Override
    public Boolean visitReturnStatement(
            ReturnStatement elem,
            Model context) {
        if (elem.getModelKind() != context.getModelKind()) {
            return Boolean.FALSE;
        }
        ReturnStatement that = (ReturnStatement) context;
        if (Boolean.FALSE.equals(match(elem.getExpression(), that.getExpression()))) {
            return Boolean.FALSE;
        }
        return Boolean.TRUE;
    }

    @Override
    public Boolean visitSimpleName(
            SimpleName elem,
            Model context) {
        if (elem.getModelKind() != context.getModelKind()) {
            return Boolean.FALSE;
        }
        SimpleName that = (SimpleName) context;
        if (Boolean.FALSE.equals(match(elem.getToken(), that.getToken()))) {
            return Boolean.FALSE;
        }
        return Boolean.TRUE;
    }

    @Override
    public Boolean visitSingleElementAnnotation(
            SingleElementAnnotation elem,
            Model context) {
        if (elem.getModelKind() != context.getModelKind()) {
            return Boolean.FALSE;
        }
        SingleElementAnnotation that = (SingleElementAnnotation) context;
        if (Boolean.FALSE.equals(match(elem.getType(), that.getType()))) {
            return Boolean.FALSE;
        }
        if (Boolean.FALSE.equals(match(elem.getExpression(), that.getExpression()))) {
            return Boolean.FALSE;
        }
        return Boolean.TRUE;
    }

    @Override
    public Boolean visitStatementExpressionList(
            StatementExpressionList elem,
            Model context) {
        if (elem.getModelKind() != context.getModelKind()) {
            return Boolean.FALSE;
        }
        StatementExpressionList that = (StatementExpressionList) context;
        if (Boolean.FALSE.equals(match(elem.getExpressions(), that.getExpressions()))) {
            return Boolean.FALSE;
        }
        return Boolean.TRUE;
    }

    @Override
    public Boolean visitSuper(
            Super elem,
            Model context) {
        if (elem.getModelKind() != context.getModelKind()) {
            return Boolean.FALSE;
        }
        Super that = (Super) context;
        if (Boolean.FALSE.equals(match(elem.getQualifier(), that.getQualifier()))) {
            return Boolean.FALSE;
        }
        return Boolean.TRUE;
    }

    @Override
    public Boolean visitSuperConstructorInvocation(
            SuperConstructorInvocation elem,
            Model context) {
        if (elem.getModelKind() != context.getModelKind()) {
            return Boolean.FALSE;
        }
        SuperConstructorInvocation that = (SuperConstructorInvocation) context;
        if (Boolean.FALSE.equals(match(elem.getQualifier(), that.getQualifier()))) {
            return Boolean.FALSE;
        }
        if (Boolean.FALSE.equals(match(elem.getTypeArguments(), that.getTypeArguments()))) {
            return Boolean.FALSE;
        }
        if (Boolean.FALSE.equals(match(elem.getArguments(), that.getArguments()))) {
            return Boolean.FALSE;
        }
        return Boolean.TRUE;
    }

    @Override
    public Boolean visitSwitchCaseLabel(
            SwitchCaseLabel elem,
            Model context) {
        if (elem.getModelKind() != context.getModelKind()) {
            return Boolean.FALSE;
        }
        SwitchCaseLabel that = (SwitchCaseLabel) context;
        if (Boolean.FALSE.equals(match(elem.getExpression(), that.getExpression()))) {
            return Boolean.FALSE;
        }
        return Boolean.TRUE;
    }

    @Override
    public Boolean visitSwitchDefaultLabel(
            SwitchDefaultLabel elem,
            Model context) {
        if (elem.getModelKind() != context.getModelKind()) {
            return Boolean.FALSE;
        }
        return Boolean.TRUE;
    }

    @Override
    public Boolean visitSwitchStatement(
            SwitchStatement elem,
            Model context) {
        if (elem.getModelKind() != context.getModelKind()) {
            return Boolean.FALSE;
        }
        SwitchStatement that = (SwitchStatement) context;
        if (Boolean.FALSE.equals(match(elem.getExpression(), that.getExpression()))) {
            return Boolean.FALSE;
        }
        if (Boolean.FALSE.equals(match(elem.getStatements(), that.getStatements()))) {
            return Boolean.FALSE;
        }
        return Boolean.TRUE;
    }

    @Override
    public Boolean visitSynchronizedStatement(
            SynchronizedStatement elem,
            Model context) {
        if (elem.getModelKind() != context.getModelKind()) {
            return Boolean.FALSE;
        }
        SynchronizedStatement that = (SynchronizedStatement) context;
        if (Boolean.FALSE.equals(match(elem.getExpression(), that.getExpression()))) {
            return Boolean.FALSE;
        }
        if (Boolean.FALSE.equals(match(elem.getBody(), that.getBody()))) {
            return Boolean.FALSE;
        }
        return Boolean.TRUE;
    }

    @Override
    public Boolean visitThis(
            This elem,
            Model context) {
        if (elem.getModelKind() != context.getModelKind()) {
            return Boolean.FALSE;
        }
        This that = (This) context;
        if (Boolean.FALSE.equals(match(elem.getQualifier(), that.getQualifier()))) {
            return Boolean.FALSE;
        }
        return Boolean.TRUE;
    }

    @Override
    public Boolean visitThrowStatement(
            ThrowStatement elem,
            Model context) {
        if (elem.getModelKind() != context.getModelKind()) {
            return Boolean.FALSE;
        }
        ThrowStatement that = (ThrowStatement) context;
        if (Boolean.FALSE.equals(match(elem.getExpression(), that.getExpression()))) {
            return Boolean.FALSE;
        }
        return Boolean.TRUE;
    }

    @Override
    public Boolean visitTryStatement(
            TryStatement elem,
            Model context) {
        if (elem.getModelKind() != context.getModelKind()) {
            return Boolean.FALSE;
        }
        TryStatement that = (TryStatement) context;
        if (Boolean.FALSE.equals(match(elem.getTryBlock(), that.getTryBlock()))) {
            return Boolean.FALSE;
        }
        if (Boolean.FALSE.equals(match(elem.getCatchClauses(), that.getCatchClauses()))) {
            return Boolean.FALSE;
        }
        if (Boolean.FALSE.equals(match(elem.getFinallyBlock(), that.getFinallyBlock()))) {
            return Boolean.FALSE;
        }
        return Boolean.TRUE;
    }

    @Override
    public Boolean visitTypeParameterDeclaration(
            TypeParameterDeclaration elem,
            Model context) {
        if (elem.getModelKind() != context.getModelKind()) {
            return Boolean.FALSE;
        }
        TypeParameterDeclaration that = (TypeParameterDeclaration) context;
        if (Boolean.FALSE.equals(match(elem.getName(), that.getName()))) {
            return Boolean.FALSE;
        }
        if (Boolean.FALSE.equals(match(elem.getTypeBounds(), that.getTypeBounds()))) {
            return Boolean.FALSE;
        }
        return Boolean.TRUE;
    }

    @Override
    public Boolean visitUnaryExpression(
            UnaryExpression elem,
            Model context) {
        if (elem.getModelKind() != context.getModelKind()) {
            return Boolean.FALSE;
        }
        UnaryExpression that = (UnaryExpression) context;
        if (Boolean.FALSE.equals(match(elem.getOperator(), that.getOperator()))) {
            return Boolean.FALSE;
        }
        if (Boolean.FALSE.equals(match(elem.getOperand(), that.getOperand()))) {
            return Boolean.FALSE;
        }
        return Boolean.TRUE;
    }

    @Override
    public Boolean visitVariableDeclarator(
            VariableDeclarator elem,
            Model context) {
        if (elem.getModelKind() != context.getModelKind()) {
            return Boolean.FALSE;
        }
        VariableDeclarator that = (VariableDeclarator) context;
        if (Boolean.FALSE.equals(match(elem.getName(), that.getName()))) {
            return Boolean.FALSE;
        }
        if (Boolean.FALSE.equals(match(elem.getExtraDimensions(), that.getExtraDimensions()))) {
            return Boolean.FALSE;
        }
        if (Boolean.FALSE.equals(match(elem.getInitializer(), that.getInitializer()))) {
            return Boolean.FALSE;
        }
        return Boolean.TRUE;
    }

    @Override
    public Boolean visitWhileStatement(
            WhileStatement elem,
            Model context) {
        if (elem.getModelKind() != context.getModelKind()) {
            return Boolean.FALSE;
        }
        WhileStatement that = (WhileStatement) context;
        if (Boolean.FALSE.equals(match(elem.getCondition(), that.getCondition()))) {
            return Boolean.FALSE;
        }
        if (Boolean.FALSE.equals(match(elem.getBody(), that.getBody()))) {
            return Boolean.FALSE;
        }
        return Boolean.TRUE;
    }

    @Override
    public Boolean visitWildcard(
            Wildcard elem,
            Model context) {
        if (elem.getModelKind() != context.getModelKind()) {
            return Boolean.FALSE;
        }
        Wildcard that = (Wildcard) context;
        if (Boolean.FALSE.equals(match(elem.getBoundKind(), that.getBoundKind()))) {
            return Boolean.FALSE;
        }
        if (Boolean.FALSE.equals(match(elem.getTypeBound(), that.getTypeBound()))) {
            return Boolean.FALSE;
        }
        return Boolean.TRUE;
    }

    private Boolean match(Model a, Model b) {
        if (a == b) {
            return true;
        }
        if (a == null || b == null) {
            return Boolean.FALSE;
        }
        if (a.getModelKind() != b.getModelKind()) {
            return Boolean.FALSE;
        }
        return a.accept(this, b);
    }

    private Boolean match(
            List<? extends Model> a,
            List<? extends Model> b) {
        if (a.size() != b.size()) {
            return Boolean.FALSE;
        }
        for (int i = 0, n = a.size(); i < n; i++) {
            if (Boolean.FALSE.equals(a.get(i).accept(this, b.get(i)))) {
                return Boolean.FALSE;
            }
        }
        return Boolean.TRUE;
    }

    private Boolean match(boolean a, boolean b) {
        return a == b;
    }

    private Boolean match(int a, int b) {
        return a == b;
    }

    private Boolean match(String a, String b) {
        if (a == null) {
            return b == null;
        }
        return a.equals(b);
    }

    private <T extends Enum<T>> Boolean match(T a, T b) {
        return a == b;
    }
}
