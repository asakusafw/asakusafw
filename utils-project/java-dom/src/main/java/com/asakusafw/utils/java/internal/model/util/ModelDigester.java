/**
 * Copyright 2011-2019 Asakusa Framework Team.
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

import com.asakusafw.utils.java.model.syntax.AlternateConstructorInvocation;
import com.asakusafw.utils.java.model.syntax.AnnotationDeclaration;
import com.asakusafw.utils.java.model.syntax.AnnotationElement;
import com.asakusafw.utils.java.model.syntax.AnnotationElementDeclaration;
import com.asakusafw.utils.java.model.syntax.ArrayAccessExpression;
import com.asakusafw.utils.java.model.syntax.ArrayCreationExpression;
import com.asakusafw.utils.java.model.syntax.ArrayInitializer;
import com.asakusafw.utils.java.model.syntax.ArrayType;
import com.asakusafw.utils.java.model.syntax.AssertStatement;
import com.asakusafw.utils.java.model.syntax.AssignmentExpression;
import com.asakusafw.utils.java.model.syntax.BasicType;
import com.asakusafw.utils.java.model.syntax.Block;
import com.asakusafw.utils.java.model.syntax.BlockComment;
import com.asakusafw.utils.java.model.syntax.BreakStatement;
import com.asakusafw.utils.java.model.syntax.CastExpression;
import com.asakusafw.utils.java.model.syntax.CatchClause;
import com.asakusafw.utils.java.model.syntax.ClassBody;
import com.asakusafw.utils.java.model.syntax.ClassDeclaration;
import com.asakusafw.utils.java.model.syntax.ClassInstanceCreationExpression;
import com.asakusafw.utils.java.model.syntax.ClassLiteral;
import com.asakusafw.utils.java.model.syntax.CompilationUnit;
import com.asakusafw.utils.java.model.syntax.ConditionalExpression;
import com.asakusafw.utils.java.model.syntax.ConstructorDeclaration;
import com.asakusafw.utils.java.model.syntax.ConstructorReferenceExpression;
import com.asakusafw.utils.java.model.syntax.ContinueStatement;
import com.asakusafw.utils.java.model.syntax.DoStatement;
import com.asakusafw.utils.java.model.syntax.DocBlock;
import com.asakusafw.utils.java.model.syntax.DocField;
import com.asakusafw.utils.java.model.syntax.DocMethod;
import com.asakusafw.utils.java.model.syntax.DocMethodParameter;
import com.asakusafw.utils.java.model.syntax.DocText;
import com.asakusafw.utils.java.model.syntax.EmptyStatement;
import com.asakusafw.utils.java.model.syntax.EnhancedForStatement;
import com.asakusafw.utils.java.model.syntax.EnumConstantDeclaration;
import com.asakusafw.utils.java.model.syntax.EnumDeclaration;
import com.asakusafw.utils.java.model.syntax.ExpressionStatement;
import com.asakusafw.utils.java.model.syntax.FieldAccessExpression;
import com.asakusafw.utils.java.model.syntax.FieldDeclaration;
import com.asakusafw.utils.java.model.syntax.ForStatement;
import com.asakusafw.utils.java.model.syntax.FormalParameterDeclaration;
import com.asakusafw.utils.java.model.syntax.IfStatement;
import com.asakusafw.utils.java.model.syntax.ImportDeclaration;
import com.asakusafw.utils.java.model.syntax.InfixExpression;
import com.asakusafw.utils.java.model.syntax.InitializerDeclaration;
import com.asakusafw.utils.java.model.syntax.InstanceofExpression;
import com.asakusafw.utils.java.model.syntax.InterfaceDeclaration;
import com.asakusafw.utils.java.model.syntax.Javadoc;
import com.asakusafw.utils.java.model.syntax.LabeledStatement;
import com.asakusafw.utils.java.model.syntax.LambdaExpression;
import com.asakusafw.utils.java.model.syntax.LineComment;
import com.asakusafw.utils.java.model.syntax.Literal;
import com.asakusafw.utils.java.model.syntax.LocalClassDeclaration;
import com.asakusafw.utils.java.model.syntax.LocalVariableDeclaration;
import com.asakusafw.utils.java.model.syntax.MarkerAnnotation;
import com.asakusafw.utils.java.model.syntax.MethodDeclaration;
import com.asakusafw.utils.java.model.syntax.MethodInvocationExpression;
import com.asakusafw.utils.java.model.syntax.MethodReferenceExpression;
import com.asakusafw.utils.java.model.syntax.Model;
import com.asakusafw.utils.java.model.syntax.Modifier;
import com.asakusafw.utils.java.model.syntax.NamedType;
import com.asakusafw.utils.java.model.syntax.NormalAnnotation;
import com.asakusafw.utils.java.model.syntax.PackageDeclaration;
import com.asakusafw.utils.java.model.syntax.ParameterizedType;
import com.asakusafw.utils.java.model.syntax.ParenthesizedExpression;
import com.asakusafw.utils.java.model.syntax.PostfixExpression;
import com.asakusafw.utils.java.model.syntax.QualifiedName;
import com.asakusafw.utils.java.model.syntax.QualifiedType;
import com.asakusafw.utils.java.model.syntax.ReturnStatement;
import com.asakusafw.utils.java.model.syntax.SimpleName;
import com.asakusafw.utils.java.model.syntax.SingleElementAnnotation;
import com.asakusafw.utils.java.model.syntax.StatementExpressionList;
import com.asakusafw.utils.java.model.syntax.StrictVisitor;
import com.asakusafw.utils.java.model.syntax.Super;
import com.asakusafw.utils.java.model.syntax.SuperConstructorInvocation;
import com.asakusafw.utils.java.model.syntax.SwitchCaseLabel;
import com.asakusafw.utils.java.model.syntax.SwitchDefaultLabel;
import com.asakusafw.utils.java.model.syntax.SwitchStatement;
import com.asakusafw.utils.java.model.syntax.SynchronizedStatement;
import com.asakusafw.utils.java.model.syntax.This;
import com.asakusafw.utils.java.model.syntax.ThrowStatement;
import com.asakusafw.utils.java.model.syntax.TryResource;
import com.asakusafw.utils.java.model.syntax.TryStatement;
import com.asakusafw.utils.java.model.syntax.TypeParameterDeclaration;
import com.asakusafw.utils.java.model.syntax.UnaryExpression;
import com.asakusafw.utils.java.model.syntax.UnionType;
import com.asakusafw.utils.java.model.syntax.VariableDeclarator;
import com.asakusafw.utils.java.model.syntax.WhileStatement;
import com.asakusafw.utils.java.model.syntax.Wildcard;
import com.asakusafw.utils.java.model.util.NoThrow;

/**
 * Computes digest value for {@link Model} objects.
 */
public final class ModelDigester extends StrictVisitor<Void, DigestContext, NoThrow> {

    /**
     * The singleton instance.
     */
    public static final ModelDigester INSTANCE = new ModelDigester();

    private ModelDigester() {
        // only for the singleton instance
    }

    /**
     * Returns the digest value of the target object.
     * @param model the target object
     * @return the corresponded digest value
     * @throws IllegalArgumentException if the parameter {@code null}
     */
    public static int compute(Model model) {
        if (model == null) {
            throw new IllegalArgumentException("model must not be null"); //$NON-NLS-1$
        }
        DigestContext digest = new DigestContext();
        model.accept(INSTANCE, digest);
        return digest.total;
    }

    @Override
    public Void visitAlternateConstructorInvocation(
            AlternateConstructorInvocation elem,
            DigestContext context) {
        digest(elem.getModelKind(), context);
        digest(elem.getTypeArguments(), context);
        digest(elem.getArguments(), context);
        return null;
    }

    @Override
    public Void visitAnnotationDeclaration(
            AnnotationDeclaration elem,
            DigestContext context) {
        digest(elem.getModelKind(), context);
        digest(elem.getJavadoc(), context);
        digest(elem.getModifiers(), context);
        digest(elem.getName(), context);
        digest(elem.getBodyDeclarations(), context);
        return null;
    }

    @Override
    public Void visitAnnotationElement(
            AnnotationElement elem,
            DigestContext context) {
        digest(elem.getModelKind(), context);
        digest(elem.getName(), context);
        digest(elem.getExpression(), context);
        return null;
    }

    @Override
    public Void visitAnnotationElementDeclaration(
            AnnotationElementDeclaration elem,
            DigestContext context) {
        digest(elem.getModelKind(), context);
        digest(elem.getJavadoc(), context);
        digest(elem.getModifiers(), context);
        digest(elem.getType(), context);
        digest(elem.getName(), context);
        digest(elem.getDefaultExpression(), context);
        return null;
    }

    @Override
    public Void visitArrayAccessExpression(
            ArrayAccessExpression elem,
            DigestContext context) {
        digest(elem.getModelKind(), context);
        digest(elem.getArray(), context);
        digest(elem.getIndex(), context);
        return null;
    }

    @Override
    public Void visitArrayCreationExpression(
            ArrayCreationExpression elem,
            DigestContext context) {
        digest(elem.getModelKind(), context);
        digest(elem.getType(), context);
        digest(elem.getDimensionExpressions(), context);
        digest(elem.getArrayInitializer(), context);
        return null;
    }

    @Override
    public Void visitArrayInitializer(
            ArrayInitializer elem,
            DigestContext context) {
        digest(elem.getModelKind(), context);
        digest(elem.getElements(), context);
        return null;
    }

    @Override
    public Void visitArrayType(
            ArrayType elem,
            DigestContext context) {
        digest(elem.getModelKind(), context);
        digest(elem.getComponentType(), context);
        return null;
    }

    @Override
    public Void visitAssertStatement(
            AssertStatement elem,
            DigestContext context) {
        digest(elem.getModelKind(), context);
        digest(elem.getExpression(), context);
        digest(elem.getMessage(), context);
        return null;
    }

    @Override
    public Void visitAssignmentExpression(
            AssignmentExpression elem,
            DigestContext context) {
        digest(elem.getModelKind(), context);
        digest(elem.getLeftHandSide(), context);
        digest(elem.getOperator(), context);
        digest(elem.getRightHandSide(), context);
        return null;
    }

    @Override
    public Void visitBasicType(
            BasicType elem,
            DigestContext context) {
        digest(elem.getModelKind(), context);
        digest(elem.getTypeKind(), context);
        return null;
    }

    @Override
    public Void visitBlock(
            Block elem,
            DigestContext context) {
        digest(elem.getModelKind(), context);
        digest(elem.getStatements(), context);
        return null;
    }

    @Override
    public Void visitBlockComment(
            BlockComment elem,
            DigestContext context) {
        digest(elem.getModelKind(), context);
        digest(elem.getString(), context);
        return null;
    }

    @Override
    public Void visitBreakStatement(
            BreakStatement elem,
            DigestContext context) {
        digest(elem.getModelKind(), context);
        digest(elem.getTarget(), context);
        return null;
    }

    @Override
    public Void visitCastExpression(
            CastExpression elem,
            DigestContext context) {
        digest(elem.getModelKind(), context);
        digest(elem.getType(), context);
        digest(elem.getExpression(), context);
        return null;
    }

    @Override
    public Void visitCatchClause(
            CatchClause elem,
            DigestContext context) {
        digest(elem.getModelKind(), context);
        digest(elem.getParameter(), context);
        digest(elem.getBody(), context);
        return null;
    }

    @Override
    public Void visitClassBody(
            ClassBody elem,
            DigestContext context) {
        digest(elem.getModelKind(), context);
        digest(elem.getBodyDeclarations(), context);
        return null;
    }

    @Override
    public Void visitClassDeclaration(
            ClassDeclaration elem,
            DigestContext context) {
        digest(elem.getModelKind(), context);
        digest(elem.getJavadoc(), context);
        digest(elem.getModifiers(), context);
        digest(elem.getName(), context);
        digest(elem.getTypeParameters(), context);
        digest(elem.getSuperClass(), context);
        digest(elem.getSuperInterfaceTypes(), context);
        digest(elem.getBodyDeclarations(), context);
        return null;
    }

    @Override
    public Void visitClassInstanceCreationExpression(
            ClassInstanceCreationExpression elem,
            DigestContext context) {
        digest(elem.getModelKind(), context);
        digest(elem.getQualifier(), context);
        digest(elem.getTypeArguments(), context);
        digest(elem.getType(), context);
        digest(elem.getArguments(), context);
        digest(elem.getBody(), context);
        return null;
    }

    @Override
    public Void visitClassLiteral(
            ClassLiteral elem,
            DigestContext context) {
        digest(elem.getModelKind(), context);
        digest(elem.getType(), context);
        return null;
    }

    @Override
    public Void visitCompilationUnit(
            CompilationUnit elem,
            DigestContext context) {
        digest(elem.getModelKind(), context);
        digest(elem.getPackageDeclaration(), context);
        digest(elem.getImportDeclarations(), context);
        digest(elem.getTypeDeclarations(), context);
        digest(elem.getComments(), context);
        return null;
    }

    @Override
    public Void visitConditionalExpression(
            ConditionalExpression elem,
            DigestContext context) {
        digest(elem.getModelKind(), context);
        digest(elem.getCondition(), context);
        digest(elem.getThenExpression(), context);
        digest(elem.getElseExpression(), context);
        return null;
    }

    @Override
    public Void visitConstructorDeclaration(
            ConstructorDeclaration elem,
            DigestContext context) {
        digest(elem.getModelKind(), context);
        digest(elem.getJavadoc(), context);
        digest(elem.getModifiers(), context);
        digest(elem.getTypeParameters(), context);
        digest(elem.getName(), context);
        digest(elem.getFormalParameters(), context);
        digest(elem.getExceptionTypes(), context);
        digest(elem.getBody(), context);
        return null;
    }

    @Override
    public Void visitContinueStatement(
            ContinueStatement elem,
            DigestContext context) {
        digest(elem.getModelKind(), context);
        digest(elem.getTarget(), context);
        return null;
    }

    @Override
    public Void visitDoStatement(
            DoStatement elem,
            DigestContext context) {
        digest(elem.getModelKind(), context);
        digest(elem.getBody(), context);
        digest(elem.getCondition(), context);
        return null;
    }

    @Override
    public Void visitDocBlock(
            DocBlock elem,
            DigestContext context) {
        digest(elem.getModelKind(), context);
        digest(elem.getTag(), context);
        digest(elem.getElements(), context);
        return null;
    }

    @Override
    public Void visitDocField(
            DocField elem,
            DigestContext context) {
        digest(elem.getModelKind(), context);
        digest(elem.getType(), context);
        digest(elem.getName(), context);
        return null;
    }

    @Override
    public Void visitDocMethod(
            DocMethod elem,
            DigestContext context) {
        digest(elem.getModelKind(), context);
        digest(elem.getType(), context);
        digest(elem.getName(), context);
        digest(elem.getFormalParameters(), context);
        return null;
    }

    @Override
    public Void visitDocMethodParameter(
            DocMethodParameter elem,
            DigestContext context) {
        digest(elem.getModelKind(), context);
        digest(elem.getType(), context);
        digest(elem.getName(), context);
        digest(elem.isVariableArity(), context);
        return null;
    }

    @Override
    public Void visitDocText(
            DocText elem,
            DigestContext context) {
        digest(elem.getModelKind(), context);
        digest(elem.getString(), context);
        return null;
    }

    @Override
    public Void visitEmptyStatement(
            EmptyStatement elem,
            DigestContext context) {
        digest(elem.getModelKind(), context);
        return null;
    }

    @Override
    public Void visitEnhancedForStatement(
            EnhancedForStatement elem,
            DigestContext context) {
        digest(elem.getModelKind(), context);
        digest(elem.getParameter(), context);
        digest(elem.getExpression(), context);
        digest(elem.getBody(), context);
        return null;
    }

    @Override
    public Void visitEnumConstantDeclaration(
            EnumConstantDeclaration elem,
            DigestContext context) {
        digest(elem.getModelKind(), context);
        digest(elem.getJavadoc(), context);
        digest(elem.getModifiers(), context);
        digest(elem.getName(), context);
        digest(elem.getArguments(), context);
        digest(elem.getBody(), context);
        return null;
    }

    @Override
    public Void visitEnumDeclaration(
            EnumDeclaration elem,
            DigestContext context) {
        digest(elem.getModelKind(), context);
        digest(elem.getJavadoc(), context);
        digest(elem.getModifiers(), context);
        digest(elem.getName(), context);
        digest(elem.getSuperInterfaceTypes(), context);
        digest(elem.getConstantDeclarations(), context);
        digest(elem.getBodyDeclarations(), context);
        return null;
    }

    @Override
    public Void visitExpressionStatement(
            ExpressionStatement elem,
            DigestContext context) {
        digest(elem.getModelKind(), context);
        digest(elem.getExpression(), context);
        return null;
    }

    @Override
    public Void visitFieldAccessExpression(
            FieldAccessExpression elem,
            DigestContext context) {
        digest(elem.getModelKind(), context);
        digest(elem.getQualifier(), context);
        digest(elem.getName(), context);
        return null;
    }

    @Override
    public Void visitFieldDeclaration(
            FieldDeclaration elem,
            DigestContext context) {
        digest(elem.getModelKind(), context);
        digest(elem.getJavadoc(), context);
        digest(elem.getModifiers(), context);
        digest(elem.getType(), context);
        digest(elem.getVariableDeclarators(), context);
        return null;
    }

    @Override
    public Void visitForStatement(
            ForStatement elem,
            DigestContext context) {
        digest(elem.getModelKind(), context);
        digest(elem.getInitialization(), context);
        digest(elem.getCondition(), context);
        digest(elem.getUpdate(), context);
        digest(elem.getBody(), context);
        return null;
    }

    @Override
    public Void visitFormalParameterDeclaration(
            FormalParameterDeclaration elem,
            DigestContext context) {
        digest(elem.getModelKind(), context);
        digest(elem.getModifiers(), context);
        digest(elem.getType(), context);
        digest(elem.isVariableArity(), context);
        digest(elem.getName(), context);
        digest(elem.getExtraDimensions(), context);
        return null;
    }

    @Override
    public Void visitIfStatement(
            IfStatement elem,
            DigestContext context) {
        digest(elem.getModelKind(), context);
        digest(elem.getCondition(), context);
        digest(elem.getThenStatement(), context);
        digest(elem.getElseStatement(), context);
        return null;
    }

    @Override
    public Void visitImportDeclaration(
            ImportDeclaration elem,
            DigestContext context) {
        digest(elem.getModelKind(), context);
        digest(elem.getImportKind(), context);
        digest(elem.getName(), context);
        return null;
    }

    @Override
    public Void visitInfixExpression(
            InfixExpression elem,
            DigestContext context) {
        digest(elem.getModelKind(), context);
        digest(elem.getLeftOperand(), context);
        digest(elem.getOperator(), context);
        digest(elem.getRightOperand(), context);
        return null;
    }

    @Override
    public Void visitInitializerDeclaration(
            InitializerDeclaration elem,
            DigestContext context) {
        digest(elem.getModelKind(), context);
        digest(elem.getJavadoc(), context);
        digest(elem.getModifiers(), context);
        digest(elem.getBody(), context);
        return null;
    }

    @Override
    public Void visitInstanceofExpression(
            InstanceofExpression elem,
            DigestContext context) {
        digest(elem.getModelKind(), context);
        digest(elem.getExpression(), context);
        digest(elem.getType(), context);
        return null;
    }

    @Override
    public Void visitInterfaceDeclaration(
            InterfaceDeclaration elem,
            DigestContext context) {
        digest(elem.getModelKind(), context);
        digest(elem.getJavadoc(), context);
        digest(elem.getModifiers(), context);
        digest(elem.getName(), context);
        digest(elem.getTypeParameters(), context);
        digest(elem.getSuperInterfaceTypes(), context);
        digest(elem.getBodyDeclarations(), context);
        return null;
    }

    @Override
    public Void visitJavadoc(
            Javadoc elem,
            DigestContext context) {
        digest(elem.getModelKind(), context);
        digest(elem.getBlocks(), context);
        return null;
    }

    @Override
    public Void visitLabeledStatement(
            LabeledStatement elem,
            DigestContext context) {
        digest(elem.getModelKind(), context);
        digest(elem.getLabel(), context);
        digest(elem.getBody(), context);
        return null;
    }

    @Override
    public Void visitLambdaExpression(
            LambdaExpression elem,
            DigestContext context) {
        digest(elem.getParameters(), context);
        digest(elem.getBody(), context);
        return null;
    }

    @Override
    public Void visitLineComment(
            LineComment elem,
            DigestContext context) {
        digest(elem.getModelKind(), context);
        digest(elem.getString(), context);
        return null;
    }

    @Override
    public Void visitLiteral(
            Literal elem,
            DigestContext context) {
        digest(elem.getModelKind(), context);
        digest(elem.getToken(), context);
        return null;
    }

    @Override
    public Void visitLocalClassDeclaration(
            LocalClassDeclaration elem,
            DigestContext context) {
        digest(elem.getModelKind(), context);
        digest(elem.getDeclaration(), context);
        return null;
    }

    @Override
    public Void visitLocalVariableDeclaration(
            LocalVariableDeclaration elem,
            DigestContext context) {
        digest(elem.getModelKind(), context);
        digest(elem.getModifiers(), context);
        digest(elem.getType(), context);
        digest(elem.getVariableDeclarators(), context);
        return null;
    }

    @Override
    public Void visitMarkerAnnotation(
            MarkerAnnotation elem,
            DigestContext context) {
        digest(elem.getModelKind(), context);
        digest(elem.getType(), context);
        return null;
    }

    @Override
    public Void visitMethodDeclaration(
            MethodDeclaration elem,
            DigestContext context) {
        digest(elem.getModelKind(), context);
        digest(elem.getJavadoc(), context);
        digest(elem.getModifiers(), context);
        digest(elem.getTypeParameters(), context);
        digest(elem.getReturnType(), context);
        digest(elem.getName(), context);
        digest(elem.getFormalParameters(), context);
        digest(elem.getExtraDimensions(), context);
        digest(elem.getExceptionTypes(), context);
        digest(elem.getBody(), context);
        return null;
    }

    @Override
    public Void visitMethodInvocationExpression(
            MethodInvocationExpression elem,
            DigestContext context) {
        digest(elem.getModelKind(), context);
        digest(elem.getQualifier(), context);
        digest(elem.getTypeArguments(), context);
        digest(elem.getName(), context);
        digest(elem.getArguments(), context);
        return null;
    }

    @Override
    public Void visitConstructorReferenceExpression(
            ConstructorReferenceExpression elem,
            DigestContext context) {
        digest(elem.getModelKind(), context);
        digest(elem.getQualifier(), context);
        digest(elem.getTypeArguments(), context);
        return null;
    }

    @Override
    public Void visitMethodReferenceExpression(
            MethodReferenceExpression elem,
            DigestContext context) {
        digest(elem.getModelKind(), context);
        digest(elem.getQualifier(), context);
        digest(elem.getTypeArguments(), context);
        digest(elem.getName(), context);
        return null;
    }

    @Override
    public Void visitModifier(
            Modifier elem,
            DigestContext context) {
        digest(elem.getModelKind(), context);
        digest(elem.getModifierKind(), context);
        return null;
    }

    @Override
    public Void visitNamedType(
            NamedType elem,
            DigestContext context) {
        digest(elem.getModelKind(), context);
        digest(elem.getName(), context);
        return null;
    }

    @Override
    public Void visitNormalAnnotation(
            NormalAnnotation elem,
            DigestContext context) {
        digest(elem.getModelKind(), context);
        digest(elem.getType(), context);
        digest(elem.getElements(), context);
        return null;
    }

    @Override
    public Void visitPackageDeclaration(
            PackageDeclaration elem,
            DigestContext context) {
        digest(elem.getModelKind(), context);
        digest(elem.getJavadoc(), context);
        digest(elem.getAnnotations(), context);
        digest(elem.getName(), context);
        return null;
    }

    @Override
    public Void visitParameterizedType(
            ParameterizedType elem,
            DigestContext context) {
        digest(elem.getModelKind(), context);
        digest(elem.getType(), context);
        digest(elem.getTypeArguments(), context);
        return null;
    }

    @Override
    public Void visitParenthesizedExpression(
            ParenthesizedExpression elem,
            DigestContext context) {
        digest(elem.getModelKind(), context);
        digest(elem.getExpression(), context);
        return null;
    }

    @Override
    public Void visitPostfixExpression(
            PostfixExpression elem,
            DigestContext context) {
        digest(elem.getModelKind(), context);
        digest(elem.getOperand(), context);
        digest(elem.getOperator(), context);
        return null;
    }

    @Override
    public Void visitQualifiedName(
            QualifiedName elem,
            DigestContext context) {
        digest(elem.getModelKind(), context);
        digest(elem.getQualifier(), context);
        digest(elem.getSimpleName(), context);
        return null;
    }

    @Override
    public Void visitQualifiedType(
            QualifiedType elem,
            DigestContext context) {
        digest(elem.getModelKind(), context);
        digest(elem.getQualifier(), context);
        digest(elem.getSimpleName(), context);
        return null;
    }

    @Override
    public Void visitReturnStatement(
            ReturnStatement elem,
            DigestContext context) {
        digest(elem.getModelKind(), context);
        digest(elem.getExpression(), context);
        return null;
    }

    @Override
    public Void visitSimpleName(
            SimpleName elem,
            DigestContext context) {
        digest(elem.getModelKind(), context);
        digest(elem.getToken(), context);
        return null;
    }

    @Override
    public Void visitSingleElementAnnotation(
            SingleElementAnnotation elem,
            DigestContext context) {
        digest(elem.getModelKind(), context);
        digest(elem.getType(), context);
        digest(elem.getExpression(), context);
        return null;
    }

    @Override
    public Void visitStatementExpressionList(
            StatementExpressionList elem,
            DigestContext context) {
        digest(elem.getModelKind(), context);
        digest(elem.getExpressions(), context);
        return null;
    }

    @Override
    public Void visitSuper(
            Super elem,
            DigestContext context) {
        digest(elem.getModelKind(), context);
        digest(elem.getQualifier(), context);
        return null;
    }

    @Override
    public Void visitSuperConstructorInvocation(
            SuperConstructorInvocation elem,
            DigestContext context) {
        digest(elem.getModelKind(), context);
        digest(elem.getQualifier(), context);
        digest(elem.getTypeArguments(), context);
        digest(elem.getArguments(), context);
        return null;
    }

    @Override
    public Void visitSwitchCaseLabel(
            SwitchCaseLabel elem,
            DigestContext context) {
        digest(elem.getModelKind(), context);
        digest(elem.getExpression(), context);
        return null;
    }

    @Override
    public Void visitSwitchDefaultLabel(
            SwitchDefaultLabel elem,
            DigestContext context) {
        digest(elem.getModelKind(), context);
        return null;
    }

    @Override
    public Void visitSwitchStatement(
            SwitchStatement elem,
            DigestContext context) {
        digest(elem.getModelKind(), context);
        digest(elem.getExpression(), context);
        digest(elem.getStatements(), context);
        return null;
    }

    @Override
    public Void visitSynchronizedStatement(
            SynchronizedStatement elem,
            DigestContext context) {
        digest(elem.getModelKind(), context);
        digest(elem.getExpression(), context);
        digest(elem.getBody(), context);
        return null;
    }

    @Override
    public Void visitThis(
            This elem,
            DigestContext context) {
        digest(elem.getModelKind(), context);
        digest(elem.getQualifier(), context);
        return null;
    }

    @Override
    public Void visitThrowStatement(
            ThrowStatement elem,
            DigestContext context) {
        digest(elem.getModelKind(), context);
        digest(elem.getExpression(), context);
        return null;
    }

    @Override
    public Void visitTryResource(
            TryResource elem,
            DigestContext context) {
        digest(elem.getModelKind(), context);
        digest(elem.getParameter(), context);
        digest(elem.getInitializer(), context);
        return null;
    }

    @Override
    public Void visitTryStatement(
            TryStatement elem,
            DigestContext context) {
        digest(elem.getModelKind(), context);
        digest(elem.getResources(), context);
        digest(elem.getTryBlock(), context);
        digest(elem.getCatchClauses(), context);
        digest(elem.getFinallyBlock(), context);
        return null;
    }

    @Override
    public Void visitTypeParameterDeclaration(
            TypeParameterDeclaration elem,
            DigestContext context) {
        digest(elem.getModelKind(), context);
        digest(elem.getName(), context);
        digest(elem.getTypeBounds(), context);
        return null;
    }

    @Override
    public Void visitUnaryExpression(
            UnaryExpression elem,
            DigestContext context) {
        digest(elem.getModelKind(), context);
        digest(elem.getOperator(), context);
        digest(elem.getOperand(), context);
        return null;
    }

    @Override
    public Void visitUnionType(
            UnionType elem,
            DigestContext context) {
        digest(elem.getModelKind(), context);
        digest(elem.getAlternativeTypes(), context);
        return null;
    }

    @Override
    public Void visitVariableDeclarator(
            VariableDeclarator elem,
            DigestContext context) {
        digest(elem.getModelKind(), context);
        digest(elem.getName(), context);
        digest(elem.getExtraDimensions(), context);
        digest(elem.getInitializer(), context);
        return null;
    }

    @Override
    public Void visitWhileStatement(
            WhileStatement elem,
            DigestContext context) {
        digest(elem.getModelKind(), context);
        digest(elem.getCondition(), context);
        digest(elem.getBody(), context);
        return null;
    }

    @Override
    public Void visitWildcard(
            Wildcard elem,
            DigestContext context) {
        digest(elem.getModelKind(), context);
        digest(elem.getBoundKind(), context);
        digest(elem.getTypeBound(), context);
        return null;
    }

    private void digest(Model model, DigestContext context) {
        if (model == null) {
            context.add(0);
        } else {
            model.accept(this, context);
        }
    }

    private void digest(List<? extends Model> models, DigestContext context) {
        context.add(models.size());
        for (int i = 0, n = models.size(); i < n; i++) {
            models.get(i).accept(this, context);
        }
    }

    private void digest(boolean value, DigestContext context) {
        context.add(value ? 0 : 1);
    }

    private void digest(int value, DigestContext context) {
        context.add(value);
    }

    private void digest(String value, DigestContext context) {
        if (value == null) {
            context.add(0);
        } else {
            context.add(value.hashCode());
        }
    }

    private void digest(Enum<?> value, DigestContext context) {
        if (value == null) {
            context.add(0);
        } else {
            context.add(value.hashCode());
        }
    }
}

/**
 * A computing context for {@link ModelDigester}.
 */
class DigestContext {

    int total = 0;

    void add(int digest) {
        total = total * 31 + digest;
    }
}
