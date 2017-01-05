/**
 * Copyright 2011-2017 Asakusa Framework Team.
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

import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

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
import com.asakusafw.utils.java.model.syntax.Attribute;
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
import com.asakusafw.utils.java.model.syntax.ContinueStatement;
import com.asakusafw.utils.java.model.syntax.DoStatement;
import com.asakusafw.utils.java.model.syntax.DocBlock;
import com.asakusafw.utils.java.model.syntax.DocElement;
import com.asakusafw.utils.java.model.syntax.DocField;
import com.asakusafw.utils.java.model.syntax.DocMethod;
import com.asakusafw.utils.java.model.syntax.DocMethodParameter;
import com.asakusafw.utils.java.model.syntax.DocText;
import com.asakusafw.utils.java.model.syntax.EmptyStatement;
import com.asakusafw.utils.java.model.syntax.EnhancedForStatement;
import com.asakusafw.utils.java.model.syntax.EnumConstantDeclaration;
import com.asakusafw.utils.java.model.syntax.EnumDeclaration;
import com.asakusafw.utils.java.model.syntax.Expression;
import com.asakusafw.utils.java.model.syntax.ExpressionStatement;
import com.asakusafw.utils.java.model.syntax.FieldAccessExpression;
import com.asakusafw.utils.java.model.syntax.FieldDeclaration;
import com.asakusafw.utils.java.model.syntax.ForStatement;
import com.asakusafw.utils.java.model.syntax.FormalParameterDeclaration;
import com.asakusafw.utils.java.model.syntax.IfStatement;
import com.asakusafw.utils.java.model.syntax.ImportDeclaration;
import com.asakusafw.utils.java.model.syntax.ImportKind.Range;
import com.asakusafw.utils.java.model.syntax.ImportKind.Target;
import com.asakusafw.utils.java.model.syntax.InfixExpression;
import com.asakusafw.utils.java.model.syntax.InitializerDeclaration;
import com.asakusafw.utils.java.model.syntax.InstanceofExpression;
import com.asakusafw.utils.java.model.syntax.InterfaceDeclaration;
import com.asakusafw.utils.java.model.syntax.Javadoc;
import com.asakusafw.utils.java.model.syntax.LabeledStatement;
import com.asakusafw.utils.java.model.syntax.LambdaExpression;
import com.asakusafw.utils.java.model.syntax.LambdaParameter;
import com.asakusafw.utils.java.model.syntax.LineComment;
import com.asakusafw.utils.java.model.syntax.Literal;
import com.asakusafw.utils.java.model.syntax.LocalClassDeclaration;
import com.asakusafw.utils.java.model.syntax.LocalVariableDeclaration;
import com.asakusafw.utils.java.model.syntax.MarkerAnnotation;
import com.asakusafw.utils.java.model.syntax.MethodDeclaration;
import com.asakusafw.utils.java.model.syntax.MethodInvocationExpression;
import com.asakusafw.utils.java.model.syntax.Model;
import com.asakusafw.utils.java.model.syntax.ModelKind;
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
import com.asakusafw.utils.java.model.syntax.Statement;
import com.asakusafw.utils.java.model.syntax.StatementExpressionList;
import com.asakusafw.utils.java.model.syntax.StrictVisitor;
import com.asakusafw.utils.java.model.syntax.Super;
import com.asakusafw.utils.java.model.syntax.SuperConstructorInvocation;
import com.asakusafw.utils.java.model.syntax.SwitchCaseLabel;
import com.asakusafw.utils.java.model.syntax.SwitchDefaultLabel;
import com.asakusafw.utils.java.model.syntax.SwitchLabel;
import com.asakusafw.utils.java.model.syntax.SwitchStatement;
import com.asakusafw.utils.java.model.syntax.SynchronizedStatement;
import com.asakusafw.utils.java.model.syntax.This;
import com.asakusafw.utils.java.model.syntax.ThrowStatement;
import com.asakusafw.utils.java.model.syntax.TryResource;
import com.asakusafw.utils.java.model.syntax.TryStatement;
import com.asakusafw.utils.java.model.syntax.Type;
import com.asakusafw.utils.java.model.syntax.TypeParameterDeclaration;
import com.asakusafw.utils.java.model.syntax.UnaryExpression;
import com.asakusafw.utils.java.model.syntax.UnionType;
import com.asakusafw.utils.java.model.syntax.VariableDeclarator;
import com.asakusafw.utils.java.model.syntax.WhileStatement;
import com.asakusafw.utils.java.model.syntax.Wildcard;
import com.asakusafw.utils.java.model.syntax.WildcardBoundKind;
import com.asakusafw.utils.java.model.util.CommentEmitTrait;
import com.asakusafw.utils.java.model.util.NoThrow;

/**
 * Emits Java DOM objects.
 */
public class ModelEmitter {

    private static final EmitEngine ENGINE = new EmitEngine();

    private final PrintWriter writer;

    /**
     * Creates a new instance.
     * @param writer the target writer
     * @throws IllegalArgumentException if the parameter is {@code null}
     */
    public ModelEmitter(PrintWriter writer) {
        if (writer == null) {
            throw new IllegalArgumentException("writer must not be null"); //$NON-NLS-1$
        }
        this.writer = writer;
    }

    /**
     * Emits a Java DOM object into the target writer.
     * @param element the target object
     * @throws IllegalArgumentException if the parameter is {@code null}
     */
    public void emit(Model element) {
        if (element == null) {
            throw new IllegalArgumentException("element must not be null"); //$NON-NLS-1$
        }
        PrintEmitContext context = new PrintEmitContext(writer);
        emit(element, context);
        context.flushComments();
    }

    /**
     * Emits a Java DOM object into the target context.
     * @param element the target object
     * @param context the current context
     * @throws IllegalArgumentException if the parameters are {@code null}
     */
    public static void emit(Model element, EmitContext context) {
        if (element == null) {
            throw new IllegalArgumentException("element must not be null"); //$NON-NLS-1$
        }
        if (context == null) {
            throw new IllegalArgumentException("context must not be null"); //$NON-NLS-1$
        }
        element.accept(ENGINE, context);
    }
}

/**
 * An engine for {@link ModelEmitter}.
 */
class EmitEngine extends StrictVisitor<Void, EmitContext, NoThrow> {

    @Override
    public Void visitAlternateConstructorInvocation(AlternateConstructorInvocation elem, EmitContext context) {
        begin(elem, context);
        processBlockComment(elem, context);
        context.statement(EmitDirection.BEGIN);
        processTypeParameters(elem.getTypeArguments(), context);
        context.keyword("this"); //$NON-NLS-1$
        processParameters(elem.getArguments(), context);
        context.separator(";"); //$NON-NLS-1$
        context.statement(EmitDirection.END);
        return null;
    }

    @Override
    public Void visitAnnotationDeclaration(AnnotationDeclaration elem, EmitContext context) {
        begin(elem, context);
        context.declaration(EmitDirection.BEGIN);
        process(elem.getJavadoc(), context);
        processBlockComment(elem, context);
        processAttributes(elem.getModifiers(), context);
        context.symbol("@"); //$NON-NLS-1$
        context.keyword("interface"); //$NON-NLS-1$
        process(elem.getName(), context);
        context.classBlock(EmitDirection.BEGIN);
        process(elem.getBodyDeclarations(), context);
        context.classBlock(EmitDirection.END);
        context.declaration(EmitDirection.END);
        return null;
    }

    @Override
    public Void visitAnnotationElement(AnnotationElement elem, EmitContext context) {
        begin(elem, context);
        processInlineComment(elem, context);
        process(elem.getName(), context);
        context.operator("="); //$NON-NLS-1$
        process(elem.getExpression(), context);
        return null;
    }

    @Override
    public Void visitAnnotationElementDeclaration(AnnotationElementDeclaration elem, EmitContext context) {
        begin(elem, context);
        context.declaration(EmitDirection.END);
        process(elem.getJavadoc(), context);
        processBlockComment(elem, context);
        processAttributes(elem.getModifiers(), context);
        process(elem.getType(), context);
        process(elem.getName(), context);
        processParameters(Collections.emptyList(), context);
        if (appears(elem.getDefaultExpression())) {
            context.keyword("default"); //$NON-NLS-1$
            process(elem.getDefaultExpression(), context);
        }
        context.separator(";"); //$NON-NLS-1$
        context.declaration(EmitDirection.END);
        return null;
    }

    @Override
    public Void visitArrayAccessExpression(ArrayAccessExpression elem, EmitContext context) {
        begin(elem, context);
        processInlineComment(elem, context);
        process(elem.getArray(), context);
        context.symbol("["); //$NON-NLS-1$
        process(elem.getIndex(), context);
        context.separator("]"); //$NON-NLS-1$
        return null;
    }

    @Override
    public Void visitArrayCreationExpression(ArrayCreationExpression elem, EmitContext context) {
        begin(elem, context);
        processInlineComment(elem, context);
        Type scalar;
        int dim = 0;
        {
            Type current = elem.getType();
            while (current instanceof ArrayType) {
                dim++;
                current = ((ArrayType) current).getComponentType();
            }
            scalar = current;
        }
        context.keyword("new"); //$NON-NLS-1$
        process(scalar, context);
        for (Expression expr : elem.getDimensionExpressions()) {
            context.symbol("["); //$NON-NLS-1$
            process(expr, context);
            context.separator("]"); //$NON-NLS-1$
            dim--;
        }
        // can be dim < 0 (invalid array type)
        for (int i = 0; i < dim; i++) {
            context.symbol("["); //$NON-NLS-1$
            context.separator("]"); //$NON-NLS-1$
        }
        process(elem.getArrayInitializer(), context);
        return null;
    }

    @Override
    public Void visitArrayInitializer(ArrayInitializer elem, EmitContext context) {
        begin(elem, context);
        processInlineComment(elem, context);
        context.arrayInitializerBlock(EmitDirection.BEGIN);
        processJoinWithComma(elem.getElements(), context);
        context.arrayInitializerBlock(EmitDirection.END);
        return null;
    }

    @Override
    public Void visitArrayType(ArrayType elem, EmitContext context) {
        begin(elem, context);
        processInlineComment(elem, context);
        process(elem.getComponentType(), context);
        context.symbol("["); //$NON-NLS-1$
        context.separator("]"); //$NON-NLS-1$
        return null;
    }

    @Override
    public Void visitAssertStatement(AssertStatement elem, EmitContext context) {
        begin(elem, context);
        processBlockComment(elem, context);
        context.statement(EmitDirection.BEGIN);
        context.keyword("assert"); //$NON-NLS-1$
        process(elem.getExpression(), context);
        if (appears(elem.getMessage())) {
            context.separator(":"); //$NON-NLS-1$
            process(elem.getMessage(), context);
        }
        context.separator(";"); //$NON-NLS-1$
        context.statement(EmitDirection.END);
        return null;
    }

    @Override
    public Void visitAssignmentExpression(AssignmentExpression elem, EmitContext context) {
        begin(elem, context);
        processInlineComment(elem, context);
        process(elem.getLeftHandSide(), context);
        context.operator(elem.getOperator().getAssignmentSymbol());
        process(elem.getRightHandSide(), context);
        return null;
    }

    @Override
    public Void visitBasicType(BasicType elem, EmitContext context) {
        begin(elem, context);
        processInlineComment(elem, context);
        context.keyword(elem.getTypeKind().getKeyword());
        return null;
    }

    @Override
    public Void visitBlock(Block elem, EmitContext context) {
        begin(elem, context);
        processBlockComment(elem, context);
        context.statementBlock(EmitDirection.BEGIN);
        process(elem.getStatements(), context);
        context.statementBlock(EmitDirection.END);
        return null;
    }

    private static final Pattern HEAD_ASTER = Pattern.compile("^ ?\\* ?"); //$NON-NLS-1$

    @Override
    public Void visitBlockComment(BlockComment elem, EmitContext context) {
        String content = elem.getString();
        if (content.startsWith("/*")) { //$NON-NLS-1$
            content = content.substring(2);
        }
        if (content.endsWith("*/")) { //$NON-NLS-1$
            content = content.substring(0, content.length() - 2);
        }
        List<String> results = new ArrayList<>();
        String[] lines = content.split("\\n|\\r|\\r\\n"); //$NON-NLS-1$
        for (String line : lines) {
            if (line.startsWith(" * ")) { //$NON-NLS-1$
                Matcher m = HEAD_ASTER.matcher(line);
                if (m.find()) {
                    results.add(line.substring(m.end()));
                } else {
                    results.add(line);
                }
            }
        }
        context.putBlockComment(results);
        return null;
    }

    @Override
    public Void visitBreakStatement(BreakStatement elem, EmitContext context) {
        begin(elem, context);
        processBlockComment(elem, context);
        context.statement(EmitDirection.BEGIN);
        context.keyword("break"); //$NON-NLS-1$
        process(elem.getTarget(), context);
        context.separator(";"); //$NON-NLS-1$
        context.statement(EmitDirection.END);
        return null;
    }

    @Override
    public Void visitCastExpression(CastExpression elem, EmitContext context) {
        begin(elem, context);
        processInlineComment(elem, context);
        context.symbol("("); //$NON-NLS-1$
        process(elem.getType(), context);
        context.separator(")"); //$NON-NLS-1$
        process(elem.getExpression(), context);
        return null;
    }

    @Override
    public Void visitCatchClause(CatchClause elem, EmitContext context) {
        begin(elem, context);
        processBlockComment(elem, context);
        context.keyword("catch"); //$NON-NLS-1$
        context.symbol("("); //$NON-NLS-1$
        process(elem.getParameter(), context);
        context.separator(")"); //$NON-NLS-1$
        process(elem.getBody(), context);
        return null;
    }

    @Override
    public Void visitClassBody(ClassBody elem, EmitContext context) {
        begin(elem, context);
        processInlineComment(elem, context);
        context.classBlock(EmitDirection.BEGIN);
        process(elem.getBodyDeclarations(), context);
        context.classBlock(EmitDirection.END);
        return null;
    }

    @Override
    public Void visitClassDeclaration(ClassDeclaration elem, EmitContext context) {
        begin(elem, context);
        context.declaration(EmitDirection.BEGIN);
        process(elem.getJavadoc(), context);
        processBlockComment(elem, context);
        processAttributes(elem.getModifiers(), context);
        context.keyword("class"); //$NON-NLS-1$
        process(elem.getName(), context);
        processTypeParameters(elem.getTypeParameters(), context);
        if (appears(elem.getSuperClass())) {
            context.keyword("extends"); //$NON-NLS-1$
            process(elem.getSuperClass(), context);
        }
        if (appears(elem.getSuperInterfaceTypes())) {
            context.keyword("implements"); //$NON-NLS-1$
            processJoinWithComma(elem.getSuperInterfaceTypes(), context);
        }
        context.classBlock(EmitDirection.BEGIN);
        process(elem.getBodyDeclarations(), context);
        context.classBlock(EmitDirection.END);
        context.declaration(EmitDirection.END);
        return null;
    }

    @Override
    public Void visitClassInstanceCreationExpression(ClassInstanceCreationExpression elem, EmitContext context) {
        begin(elem, context);
        processInlineComment(elem, context);
        if (process(elem.getQualifier(), context)) {
            context.symbol("."); //$NON-NLS-1$
        }
        context.keyword("new"); //$NON-NLS-1$
        processTypeParameters(elem.getTypeArguments(), context);
        process(elem.getType(), context);
        processParameters(elem.getArguments(), context);
        process(elem.getBody(), context);
        return null;
    }

    @Override
    public Void visitClassLiteral(ClassLiteral elem, EmitContext context) {
        begin(elem, context);
        processInlineComment(elem, context);
        process(elem.getType(), context);
        context.symbol("."); //$NON-NLS-1$
        context.keyword("class"); //$NON-NLS-1$
        return null;
    }

    @Override
    public Void visitCompilationUnit(CompilationUnit elem, EmitContext context) {
        begin(elem, context);
        processCompilationUnitComment(elem, context);
        process(elem.getPackageDeclaration(), context);
        process(elem.getImportDeclarations(), context);
        process(elem.getTypeDeclarations(), context);
        return null;
    }

    @Override
    public Void visitConditionalExpression(ConditionalExpression elem, EmitContext context) {
        begin(elem, context);
        processInlineComment(elem, context);
        process(elem.getCondition(), context);
        context.operator("?"); //$NON-NLS-1$
        process(elem.getThenExpression(), context);
        context.operator(":"); //$NON-NLS-1$
        process(elem.getElseExpression(), context);
        return null;
    }

    @Override
    public Void visitConstructorDeclaration(ConstructorDeclaration elem, EmitContext context) {
        begin(elem, context);
        context.declaration(EmitDirection.BEGIN);
        process(elem.getJavadoc(), context);
        processBlockComment(elem, context);
        processAttributes(elem.getModifiers(), context);
        processTypeParameters(elem.getTypeParameters(), context);
        process(elem.getName(), context);
        processParameters(elem.getFormalParameters(), context);
        if (appears(elem.getExceptionTypes())) {
            context.keyword("throws"); //$NON-NLS-1$
            processJoinWithComma(elem.getExceptionTypes(), context);
        }
        process(elem.getBody(), context);
        context.declaration(EmitDirection.END);
        return null;
    }

    @Override
    public Void visitContinueStatement(ContinueStatement elem, EmitContext context) {
        begin(elem, context);
        processBlockComment(elem, context);
        context.statement(EmitDirection.BEGIN);
        context.keyword("continue"); //$NON-NLS-1$
        process(elem.getTarget(), context);
        context.separator(";"); //$NON-NLS-1$
        context.statement(EmitDirection.END);
        return null;
    }

    @Override
    public Void visitDoStatement(DoStatement elem, EmitContext context) {
        begin(elem, context);
        processBlockComment(elem, context);
        context.statement(EmitDirection.BEGIN);
        context.keyword("do"); //$NON-NLS-1$
        process(elem.getBody(), context);
        context.keyword("while"); //$NON-NLS-1$
        context.symbol("("); //$NON-NLS-1$
        process(elem.getCondition(), context);
        context.separator(")"); //$NON-NLS-1$
        context.separator(";"); //$NON-NLS-1$
        context.statement(EmitDirection.END);
        return null;
    }

    @Override
    public Void visitEmptyStatement(EmptyStatement elem, EmitContext context) {
        begin(elem, context);
        processBlockComment(elem, context);
        context.statement(EmitDirection.BEGIN);
        context.separator(";"); //$NON-NLS-1$
        context.statement(EmitDirection.END);
        return null;
    }

    @Override
    public Void visitEnhancedForStatement(EnhancedForStatement elem, EmitContext context) {
        begin(elem, context);
        processBlockComment(elem, context);
        context.statement(EmitDirection.BEGIN);
        context.keyword("for"); //$NON-NLS-1$
        context.symbol("("); //$NON-NLS-1$
        process(elem.getParameter(), context);
        context.separator(":"); //$NON-NLS-1$
        process(elem.getExpression(), context);
        context.separator(")"); //$NON-NLS-1$
        process(elem.getBody(), context);
        context.statement(EmitDirection.END);
        return null;
    }

    @Override
    public Void visitEnumConstantDeclaration(EnumConstantDeclaration elem, EmitContext context) {
        begin(elem, context);
        context.declaration(EmitDirection.BEGIN);
        process(elem.getJavadoc(), context);
        processBlockComment(elem, context);
        processAttributes(elem.getModifiers(), context);
        process(elem.getName(), context);
        if (appears(elem.getArguments())) {
            processParameters(elem.getArguments(), context);
        }
        process(elem.getBody(), context);
        context.separator(","); //$NON-NLS-1$
        context.declaration(EmitDirection.END);
        return null;
    }

    @Override
    public Void visitEnumDeclaration(EnumDeclaration elem, EmitContext context) {
        begin(elem, context);
        context.declaration(EmitDirection.BEGIN);
        process(elem.getJavadoc(), context);
        processBlockComment(elem, context);
        processAttributes(elem.getModifiers(), context);
        context.keyword("enum"); //$NON-NLS-1$
        process(elem.getName(), context);
        if (appears(elem.getSuperInterfaceTypes())) {
            context.keyword("implements"); //$NON-NLS-1$
            processJoinWithComma(elem.getSuperInterfaceTypes(), context);
        }
        context.classBlock(EmitDirection.BEGIN);
        process(elem.getConstantDeclarations(), context);
        if (appears(elem.getBodyDeclarations())) {
            context.declaration(EmitDirection.BEGIN);
            context.separator(";"); //$NON-NLS-1$
            context.declaration(EmitDirection.END);
            process(elem.getBodyDeclarations(), context);
        }
        context.classBlock(EmitDirection.END);
        context.declaration(EmitDirection.END);
        return null;
    }

    @Override
    public Void visitExpressionStatement(ExpressionStatement elem, EmitContext context) {
        begin(elem, context);
        processBlockComment(elem, context);
        context.statement(EmitDirection.BEGIN);
        process(elem.getExpression(), context);
        context.separator(";"); //$NON-NLS-1$
        context.statement(EmitDirection.END);
        return null;
    }

    @Override
    public Void visitFieldAccessExpression(FieldAccessExpression elem, EmitContext context) {
        begin(elem, context);
        processInlineComment(elem, context);
        process(elem.getQualifier(), context);
        context.symbol("."); //$NON-NLS-1$
        process(elem.getName(), context);
        return null;
    }

    @Override
    public Void visitFieldDeclaration(FieldDeclaration elem, EmitContext context) {
        begin(elem, context);
        context.declaration(EmitDirection.BEGIN);
        process(elem.getJavadoc(), context);
        processBlockComment(elem, context);
        processAttributes(elem.getModifiers(), context);
        process(elem.getType(), context);
        processJoinWithComma(elem.getVariableDeclarators(), context);
        context.separator(";"); //$NON-NLS-1$
        context.declaration(EmitDirection.END);
        return null;
    }

    @Override
    public Void visitFormalParameterDeclaration(FormalParameterDeclaration elem, EmitContext context) {
        begin(elem, context);
        processInlineComment(elem, context);
        process(elem.getModifiers(), context); // one liner instead of processAttributes(...)
        process(elem.getType(), context);
        if (elem.isVariableArity()) {
            context.separator("..."); //$NON-NLS-1$
        }
        process(elem.getName(), context);
        for (int i = 0, n = elem.getExtraDimensions(); i < n; i++) {
            context.symbol("["); //$NON-NLS-1$
            context.separator("]"); //$NON-NLS-1$
        }
        return null;
    }

    @Override
    public Void visitForStatement(ForStatement elem, EmitContext context) {
        begin(elem, context);
        processBlockComment(elem, context);
        context.statement(EmitDirection.BEGIN);
        context.keyword("for"); //$NON-NLS-1$
        context.symbol("("); //$NON-NLS-1$
        if (elem.getInitialization() instanceof LocalVariableDeclaration) {
            LocalVariableDeclaration decl = (LocalVariableDeclaration) elem.getInitialization();
            processLocalVaribale(decl, context);
        } else {
            process(elem.getInitialization(), context);
        }
        context.separator(";"); //$NON-NLS-1$
        process(elem.getCondition(), context);
        context.separator(";"); //$NON-NLS-1$
        process(elem.getUpdate(), context);
        context.separator(")"); //$NON-NLS-1$
        process(elem.getBody(), context);
        context.statement(EmitDirection.END);
        return null;
    }

    @Override
    public Void visitIfStatement(IfStatement elem, EmitContext context) {
        begin(elem, context);
        processBlockComment(elem, context);
        context.statement(EmitDirection.BEGIN);
        context.keyword("if"); //$NON-NLS-1$
        context.symbol("("); //$NON-NLS-1$
        process(elem.getCondition(), context);
        context.separator(")"); //$NON-NLS-1$
        process(elem.getThenStatement(), context);
        if (appears(elem.getElseStatement())) {
            context.keyword("else"); //$NON-NLS-1$
            process(elem.getElseStatement(), context);
        }
        context.statement(EmitDirection.END);
        return null;
    }

    @Override
    public Void visitImportDeclaration(ImportDeclaration elem, EmitContext context) {
        begin(elem, context);
        processBlockComment(elem, context);
        context.declaration(EmitDirection.BEGIN);
        context.keyword("import"); //$NON-NLS-1$
        if (elem.getImportKind().getTarget() == Target.MEMBER) {
            context.keyword("static"); //$NON-NLS-1$
        }
        process(elem.getName(), context);
        if (elem.getImportKind().getRange() == Range.ON_DEMAND) {
            context.symbol("."); //$NON-NLS-1$
            context.symbol("*"); //$NON-NLS-1$
        }
        context.separator(";"); //$NON-NLS-1$
        context.declaration(EmitDirection.END);
        return null;
    }

    @Override
    public Void visitInfixExpression(InfixExpression elem, EmitContext context) {
        begin(elem, context);
        processInlineComment(elem, context);
        process(elem.getLeftOperand(), context);
        context.operator(elem.getOperator().getSymbol());
        process(elem.getRightOperand(), context);
        return null;
    }

    @Override
    public Void visitInitializerDeclaration(InitializerDeclaration elem, EmitContext context) {
        begin(elem, context);
        context.declaration(EmitDirection.BEGIN);
        processBlockComment(elem, context);
        processAttributes(elem.getModifiers(), context);
        process(elem.getBody(), context);
        context.declaration(EmitDirection.END);
        return null;
    }

    @Override
    public Void visitInstanceofExpression(InstanceofExpression elem, EmitContext context) {
        begin(elem, context);
        processInlineComment(elem, context);
        process(elem.getExpression(), context);
        context.keyword("instanceof"); //$NON-NLS-1$
        process(elem.getType(), context);
        return null;
    }

    @Override
    public Void visitInterfaceDeclaration(InterfaceDeclaration elem, EmitContext context) {
        begin(elem, context);
        context.declaration(EmitDirection.BEGIN);
        process(elem.getJavadoc(), context);
        processBlockComment(elem, context);
        processAttributes(elem.getModifiers(), context);
        context.keyword("interface"); //$NON-NLS-1$
        process(elem.getName(), context);
        processTypeParameters(elem.getTypeParameters(), context);
        if (appears(elem.getSuperInterfaceTypes())) {
            context.keyword("extends"); //$NON-NLS-1$
            processJoinWithComma(elem.getSuperInterfaceTypes(), context);
        }
        context.classBlock(EmitDirection.BEGIN);
        process(elem.getBodyDeclarations(), context);
        context.classBlock(EmitDirection.END);
        context.declaration(EmitDirection.END);
        return null;
    }

    @Override
    public Void visitLabeledStatement(LabeledStatement elem, EmitContext context) {
        begin(elem, context);
        processBlockComment(elem, context);
        context.statement(EmitDirection.BEGIN);
        process(elem.getLabel(), context);
        context.separator(":"); //$NON-NLS-1$
        process(elem.getBody(), context);
        context.statement(EmitDirection.END);
        return null;
    }

    @Override
    public Void visitLambdaExpression(LambdaExpression elem, EmitContext context) {
        List<? extends LambdaParameter> parameters = elem.getParameters();
        if (parameters.size() == 1 && parameters.get(0).getModelKind() == ModelKind.SIMPLE_NAME) {
            process(parameters.get(0), context);
        } else {
            context.symbol("("); //$NON-NLS-1$
            processJoinWithComma(parameters, context);
            context.separator(")"); //$NON-NLS-1$
        }
        context.operator("->"); //$NON-NLS-1$
        process(elem.getBody(), context);
        return null;
    }

    @Override
    public Void visitLineComment(LineComment elem, EmitContext context) {
        String body = elem.getString();
        if (body.startsWith("//")) { //$NON-NLS-1$
            body = body.substring(2);
        }
        if (body.startsWith(" ")) { //$NON-NLS-1$
            body = body.substring(1);
        }
        context.putLineComment(body);
        return null;
    }

    @Override
    public Void visitLiteral(Literal elem, EmitContext context) {
        begin(elem, context);
        processInlineComment(elem, context);
        context.immediate(elem.getToken());
        return null;
    }

    @Override
    public Void visitLocalClassDeclaration(LocalClassDeclaration elem, EmitContext context) {
        begin(elem, context);
        processBlockComment(elem, context);
        context.statement(EmitDirection.BEGIN);
        process(elem.getDeclaration(), context);
        context.statement(EmitDirection.END);
        return null;
    }

    @Override
    public Void visitLocalVariableDeclaration(LocalVariableDeclaration elem, EmitContext context) {
        begin(elem, context);
        processBlockComment(elem, context);
        context.statement(EmitDirection.BEGIN);
        processLocalVaribale(elem, context);
        context.separator(";"); //$NON-NLS-1$
        context.statement(EmitDirection.END);
        return null;
    }

    private void processLocalVaribale(LocalVariableDeclaration elem, EmitContext context) {
        begin(elem, context);
        processAttributes(elem.getModifiers(), context);
        process(elem.getType(), context);
        processJoinWithComma(elem.getVariableDeclarators(), context);
    }

    @Override
    public Void visitMarkerAnnotation(MarkerAnnotation elem, EmitContext context) {
        begin(elem, context);
        processInlineComment(elem, context);
        context.symbol("@"); //$NON-NLS-1$
        process(elem.getType(), context);
        return null;
    }

    @Override
    public Void visitMethodDeclaration(MethodDeclaration elem, EmitContext context) {
        begin(elem, context);
        context.declaration(EmitDirection.BEGIN);
        process(elem.getJavadoc(), context);
        processBlockComment(elem, context);
        processAttributes(elem.getModifiers(), context);
        processTypeParameters(elem.getTypeParameters(), context);
        process(elem.getReturnType(), context);
        process(elem.getName(), context);
        processParameters(elem.getFormalParameters(), context);
        for (int i = 0, n = elem.getExtraDimensions(); i < n; i++) {
            context.symbol("["); //$NON-NLS-1$
            context.separator("]"); //$NON-NLS-1$
        }
        if (appears(elem.getExceptionTypes())) {
            context.keyword("throws"); //$NON-NLS-1$
            processJoinWithComma(elem.getExceptionTypes(), context);
        }
        if (appears(elem.getBody())) {
            process(elem.getBody(), context);
        } else {
            context.separator(";"); //$NON-NLS-1$
        }
        context.declaration(EmitDirection.END);
        return null;
    }

    @Override
    public Void visitMethodInvocationExpression(MethodInvocationExpression elem, EmitContext context) {
        begin(elem, context);
        processInlineComment(elem, context);
        if (process(elem.getQualifier(), context)) {
            context.symbol("."); //$NON-NLS-1$
        }
        processTypeParameters(elem.getTypeArguments(), context);
        process(elem.getName(), context);
        processParameters(elem.getArguments(), context);
        return null;
    }

    @Override
    public Void visitModifier(Modifier elem, EmitContext context) {
        begin(elem, context);
        processInlineComment(elem, context);
        context.keyword(elem.getModifierKind().getKeyword());
        return null;
    }

    @Override
    public Void visitNamedType(NamedType elem, EmitContext context) {
        begin(elem, context);
        processInlineComment(elem, context);
        process(elem.getName(), context);
        return null;
    }

    @Override
    public Void visitNormalAnnotation(NormalAnnotation elem, EmitContext context) {
        begin(elem, context);
        processInlineComment(elem, context);
        context.symbol("@"); //$NON-NLS-1$
        process(elem.getType(), context);
        processParameters(elem.getElements(), context);
        return null;
    }

    @Override
    public Void visitPackageDeclaration(PackageDeclaration elem, EmitContext context) {
        begin(elem, context);
        context.declaration(EmitDirection.BEGIN);
        process(elem.getJavadoc(), context);
        processBlockComment(elem, context);
        context.keyword("package"); //$NON-NLS-1$
        process(elem.getName(), context);
        context.separator(";"); //$NON-NLS-1$
        context.declaration(EmitDirection.END);
        return null;
    }

    @Override
    public Void visitParameterizedType(ParameterizedType elem, EmitContext context) {
        begin(elem, context);
        processInlineComment(elem, context);
        process(elem.getType(), context);
        // may be diamond operator
        context.symbol("<"); //$NON-NLS-1$
        processJoinWithComma(elem.getTypeArguments(), context);
        context.separator(">"); //$NON-NLS-1$
        return null;
    }

    @Override
    public Void visitParenthesizedExpression(ParenthesizedExpression elem, EmitContext context) {
        begin(elem, context);
        processInlineComment(elem, context);
        context.symbol("("); //$NON-NLS-1$
        process(elem.getExpression(), context);
        context.separator(")"); //$NON-NLS-1$
        return null;
    }

    @Override
    public Void visitPostfixExpression(PostfixExpression elem, EmitContext context) {
        begin(elem, context);
        processInlineComment(elem, context);
        process(elem.getOperand(), context);
        context.operator(elem.getOperator().getSymbol());
        return null;
    }

    @Override
    public Void visitQualifiedName(QualifiedName elem, EmitContext context) {
        begin(elem, context);
        processInlineComment(elem, context);
        process(elem.getQualifier(), context);
        context.symbol("."); //$NON-NLS-1$
        process(elem.getSimpleName(), context);
        return null;
    }

    @Override
    public Void visitQualifiedType(QualifiedType elem, EmitContext context) {
        begin(elem, context);
        processInlineComment(elem, context);
        process(elem.getQualifier(), context);
        context.symbol("."); //$NON-NLS-1$
        process(elem.getSimpleName(), context);
        return null;
    }

    @Override
    public Void visitReturnStatement(ReturnStatement elem, EmitContext context) {
        begin(elem, context);
        processBlockComment(elem, context);
        context.statement(EmitDirection.BEGIN);
        context.keyword("return"); //$NON-NLS-1$
        process(elem.getExpression(), context);
        context.separator(";"); //$NON-NLS-1$
        context.statement(EmitDirection.END);
        return null;
    }

    @Override
    public Void visitSimpleName(SimpleName elem, EmitContext context) {
        begin(elem, context);
        processInlineComment(elem, context);
        context.immediate(elem.getToken());
        return null;
    }

    @Override
    public Void visitSingleElementAnnotation(SingleElementAnnotation elem, EmitContext context) {
        begin(elem, context);
        processInlineComment(elem, context);
        context.symbol("@"); //$NON-NLS-1$
        process(elem.getType(), context);
        context.symbol("("); //$NON-NLS-1$
        process(elem.getExpression(), context);
        context.separator(")"); //$NON-NLS-1$
        return null;
    }

    @Override
    public Void visitStatementExpressionList(StatementExpressionList elem, EmitContext context) {
        begin(elem, context);
        processInlineComment(elem, context);
        processJoinWithComma(elem.getExpressions(), context);
        return null;
    }

    @Override
    public Void visitSuper(Super elem, EmitContext context) {
        begin(elem, context);
        processInlineComment(elem, context);
        if (process(elem.getQualifier(), context)) {
            context.symbol("."); //$NON-NLS-1$
        }
        context.keyword("super"); //$NON-NLS-1$
        return null;
    }

    @Override
    public Void visitSuperConstructorInvocation(SuperConstructorInvocation elem, EmitContext context) {
        begin(elem, context);
        processBlockComment(elem, context);
        context.statement(EmitDirection.BEGIN);
        if (process(elem.getQualifier(), context)) {
            context.symbol("."); //$NON-NLS-1$
        }
        processTypeParameters(elem.getTypeArguments(), context);
        context.keyword("super"); //$NON-NLS-1$
        processParameters(elem.getArguments(), context);
        context.separator(";"); //$NON-NLS-1$
        context.statement(EmitDirection.END);
        return null;
    }

    @Override
    public Void visitSwitchCaseLabel(SwitchCaseLabel elem, EmitContext context) {
        begin(elem, context);
        processBlockComment(elem, context);
        context.statement(EmitDirection.BEGIN);
        context.keyword("case"); //$NON-NLS-1$
        process(elem.getExpression(), context);
        context.symbol(":"); //$NON-NLS-1$
        context.statement(EmitDirection.END);
        return null;
    }

    @Override
    public Void visitSwitchDefaultLabel(SwitchDefaultLabel elem, EmitContext context) {
        begin(elem, context);
        processBlockComment(elem, context);
        context.statement(EmitDirection.BEGIN);
        context.keyword("default"); //$NON-NLS-1$
        context.symbol(":"); //$NON-NLS-1$
        context.statement(EmitDirection.END);
        return null;
    }

    @Override
    public Void visitSwitchStatement(SwitchStatement elem, EmitContext context) {
        begin(elem, context);
        processBlockComment(elem, context);
        context.statement(EmitDirection.BEGIN);
        context.keyword("switch"); //$NON-NLS-1$
        context.symbol("("); //$NON-NLS-1$
        process(elem.getExpression(), context);
        context.separator(")"); //$NON-NLS-1$
        context.statementBlock(EmitDirection.BEGIN);
        processSwitchBody(elem, context);
        context.statementBlock(EmitDirection.END);
        context.statement(EmitDirection.END);
        return null;
    }

    private void processSwitchBody(SwitchStatement elem, EmitContext context) {
        if (appears(elem.getStatements()) == false) {
            return;
        }
        boolean inLabel = false;
        for (Statement stmt : elem.getStatements()) {
            if (stmt instanceof SwitchLabel) {
                if (inLabel) {
                    context.switchLabel(EmitDirection.END);
                }
                process(stmt, context);
                context.switchLabel(EmitDirection.BEGIN);
                inLabel = true;
            } else {
                process(stmt, context);
            }
        }
        if (inLabel) {
            context.switchLabel(EmitDirection.END);
        }
    }

    @Override
    public Void visitSynchronizedStatement(SynchronizedStatement elem, EmitContext context) {
        begin(elem, context);
        processBlockComment(elem, context);
        context.statement(EmitDirection.BEGIN);
        context.keyword("synchronized"); //$NON-NLS-1$
        context.symbol("("); //$NON-NLS-1$
        process(elem.getExpression(), context);
        context.separator(")"); //$NON-NLS-1$
        process(elem.getBody(), context);
        context.statement(EmitDirection.END);
        return null;
    }

    @Override
    public Void visitThis(This elem, EmitContext context) {
        begin(elem, context);
        processInlineComment(elem, context);
        if (process(elem.getQualifier(), context)) {
            context.symbol("."); //$NON-NLS-1$
        }
        context.keyword("this"); //$NON-NLS-1$
        return null;
    }

    @Override
    public Void visitThrowStatement(ThrowStatement elem, EmitContext context) {
        begin(elem, context);
        processBlockComment(elem, context);
        context.statement(EmitDirection.BEGIN);
        context.keyword("throw"); //$NON-NLS-1$
        process(elem.getExpression(), context);
        context.separator(";"); //$NON-NLS-1$
        context.statement(EmitDirection.END);
        return null;
    }

    @Override
    public Void visitTryResource(TryResource elem, EmitContext context) {
        begin(elem, context);
        processInlineComment(elem, context);
        process(elem.getParameter(), context);
        context.operator("="); //$NON-NLS-1$
        process(elem.getInitializer(), context);
        return null;
    }

    @Override
    public Void visitTryStatement(TryStatement elem, EmitContext context) {
        begin(elem, context);
        processBlockComment(elem, context);
        context.statement(EmitDirection.BEGIN);
        context.keyword("try"); //$NON-NLS-1$
        if (appears(elem.getResources())) {
            context.symbol("("); //$NON-NLS-1$
            boolean first = true;
            for (TryResource resource : elem.getResources()) {
                if (first) {
                    first = false;
                } else {
                    context.separator(";"); //$NON-NLS-1$
                    context.blockPadding();
                }
                process(resource, context);
            }
            context.separator(")");
        }
        process(elem.getTryBlock(), context);
        process(elem.getCatchClauses(), context);
        if (appears(elem.getFinallyBlock())) {
            context.keyword("finally"); //$NON-NLS-1$
            process(elem.getFinallyBlock(), context);
        }
        context.statement(EmitDirection.END);
        return null;
    }

    @Override
    public Void visitTypeParameterDeclaration(TypeParameterDeclaration elem, EmitContext context) {
        begin(elem, context);
        processInlineComment(elem, context);
        process(elem.getName(), context);
        Iterator<? extends Type> iter = elem.getTypeBounds().iterator();
        if (iter.hasNext()) {
            context.keyword("extends"); //$NON-NLS-1$
            process(iter.next(), context);
            while (iter.hasNext()) {
                context.separator("&"); //$NON-NLS-1$
                process(iter.next(), context);
            }
        }
        return null;
    }

    @Override
    public Void visitUnaryExpression(UnaryExpression elem, EmitContext context) {
        begin(elem, context);
        processInlineComment(elem, context);
        context.operator(elem.getOperator().getSymbol());
        process(elem.getOperand(), context);
        return null;
    }

    @Override
    public Void visitUnionType(UnionType elem, EmitContext context) {
        begin(elem, context);
        processJoin("|", elem.getAlternativeTypes(), context); //$NON-NLS-1$
        return null;
    }

    @Override
    public Void visitVariableDeclarator(VariableDeclarator elem, EmitContext context) {
        begin(elem, context);
        processInlineComment(elem, context);
        process(elem.getName(), context);
        for (int i = 0, n = elem.getExtraDimensions(); i < n; i++) {
            context.symbol("["); //$NON-NLS-1$
            context.separator("]"); //$NON-NLS-1$
        }
        if (appears(elem.getInitializer())) {
            context.operator("="); //$NON-NLS-1$
            process(elem.getInitializer(), context);
        }
        return null;
    }

    @Override
    public Void visitWhileStatement(WhileStatement elem, EmitContext context) {
        begin(elem, context);
        processBlockComment(elem, context);
        context.statement(EmitDirection.BEGIN);
        context.keyword("while"); //$NON-NLS-1$
        context.symbol("("); //$NON-NLS-1$
        process(elem.getCondition(), context);
        context.separator(")"); //$NON-NLS-1$
        process(elem.getBody(), context);
        context.statement(EmitDirection.END);
        return null;
    }

    @Override
    public Void visitWildcard(Wildcard elem, EmitContext context) {
        begin(elem, context);
        processInlineComment(elem, context);
        context.keyword("?"); //$NON-NLS-1$
        if (elem.getBoundKind() == WildcardBoundKind.UPPER_BOUNDED) {
            context.keyword("extends"); //$NON-NLS-1$
            process(elem.getTypeBound(), context);
        } else if (elem.getBoundKind() == WildcardBoundKind.LOWER_BOUNDED) {
            context.keyword("super"); //$NON-NLS-1$
            process(elem.getTypeBound(), context);
        }
        return null;
    }

    @Override
    public Void visitJavadoc(Javadoc elem, EmitContext context) {
        begin(elem, context);
        processBlockComment(elem, context);
        context.docComment(EmitDirection.BEGIN);
        for (DocBlock block : elem.getBlocks()) {
            context.docBlock(EmitDirection.BEGIN);
            process(block, context);
            context.docBlock(EmitDirection.END);
        }
        context.docComment(EmitDirection.END);
        return null;
    }

    @Override
    public Void visitDocBlock(DocBlock elem, EmitContext context) {
        begin(elem, context);
        String tag = elem.getTag();
        if (tag.length() != 0) {
            context.separator(tag);
            context.padding();
        }
        int offset = 0;
        List<? extends DocElement> elements = elem.getElements();
        if (tag.equals("@param") && isDocTypeParameter(elements)) { //$NON-NLS-1$
            // @param <T>
            context.symbol("<"); //$NON-NLS-1$
            context.symbol(((SimpleName) elements.get(1)).getToken());
            context.symbol(">"); //$NON-NLS-1$
            context.padding();
            offset = 3;
        }

        for (int i = offset, n = elements.size(); i < n; i++) {
            processDocInlineElement(elements.get(i), i == n - 1, context);
        }
        return null;
    }

    private boolean isDocTypeParameter(List<? extends DocElement> elements) {
        if (elements.size() < 3) {
            return false;
        }
        if (elements.get(0).getModelKind() != ModelKind.DOC_TEXT) {
            return false;
        }
        if (elements.get(1).getModelKind() != ModelKind.SIMPLE_NAME) {
            return false;
        }
        if (elements.get(2).getModelKind() != ModelKind.DOC_TEXT) {
            return false;
        }
        if (((DocText) elements.get(0)).getString().equals("<") == false) { //$NON-NLS-1$
            return false;
        }
        if (((DocText) elements.get(2)).getString().equals(">") == false) { //$NON-NLS-1$
            return false;
        }
        return true;
    }

    private void processDocInlineElement(DocElement elem, boolean last, EmitContext context) {
        if (elem.getModelKind() == ModelKind.DOC_BLOCK) {
            context.docInlineBlock(EmitDirection.BEGIN);
            process(elem, context);
            context.docInlineBlock(EmitDirection.END);
        } else if (elem.getModelKind() == ModelKind.DOC_TEXT) {
            process(elem, context);
        } else {
            context.padding();
            process(elem, context);
            if (last == false) {
                context.padding();
            }
        }
    }

    @Override
    public Void visitDocField(DocField elem, EmitContext context) {
        begin(elem, context);
        process(elem.getType(), context);
        context.symbol("#"); //$NON-NLS-1$
        process(elem.getName(), context);
        return null;
    }

    @Override
    public Void visitDocMethod(DocMethod elem, EmitContext context) {
        begin(elem, context);
        process(elem.getType(), context);
        context.symbol("#"); //$NON-NLS-1$
        process(elem.getName(), context);
        context.symbol("("); //$NON-NLS-1$
        processJoinWithComma(elem.getFormalParameters(), context);
        context.separator(")"); //$NON-NLS-1$
        return null;
    }

    @Override
    public Void visitDocMethodParameter(DocMethodParameter elem, EmitContext context) {
        begin(elem, context);
        process(elem.getType(), context);
        if (elem.isVariableArity()) {
            context.separator("..."); //$NON-NLS-1$
        }
        process(elem.getName(), context);
        return null;
    }

    @Override
    public Void visitDocText(DocText elem, EmitContext context) {
        begin(elem, context);
        if (elem.getString().startsWith(" ")) { //$NON-NLS-1$
            context.symbol(elem.getString());
        } else {
            context.immediate(elem.getString());
        }
        return null;
    }

    private void begin(Model elem, EmitContext context) {
        return;
    }

    private boolean appears(Model element) {
        return element != null;
    }

    private boolean appears(List<? extends Model> elements) {
        return elements.isEmpty() == false;
    }

    private boolean process(Model element, EmitContext context) {
        if (element == null) {
            return false;
        }
        element.accept(this, context);
        return true;
    }

    private boolean process(List<? extends Model> elements, EmitContext context) {
        for (Model element : elements) {
            element.accept(this, context);
        }
        return true;
    }

    private void processJoinWithComma(List<? extends Model> elements, EmitContext context) {
        processJoin(",", elements, context); //$NON-NLS-1$
    }

    private void processJoin(String separator, List<? extends Model> elements, EmitContext context) {
        Iterator<? extends Model> iter = elements.iterator();
        if (iter.hasNext()) {
            process(iter.next(), context);
            while (iter.hasNext()) {
                context.separator(separator);
                process(iter.next(), context);
            }
        }
    }

    private void processAttributes(List<? extends Attribute> elements, EmitContext context) {
        for (Attribute element : elements) {
            element.accept(this, context);
            switch (element.getModelKind()) {
            case MARKER_ANNOTATION:
            case SINGLE_ELEMENT_ANNOTATION:
            case NORMAL_ANNOTATION:
                context.blockPadding();
                break;
            default:
                break;
            }
        }
    }

    private void processParameters(List<? extends Model> elements, EmitContext context) {
        context.symbol("("); //$NON-NLS-1$
        processJoinWithComma(elements, context);
        context.separator(")"); //$NON-NLS-1$
    }

    private void processTypeParameters(List<? extends Model> elements, EmitContext context) {
        if (appears(elements)) {
            context.symbol("<"); //$NON-NLS-1$
            processJoinWithComma(elements, context);
            context.separator(">"); //$NON-NLS-1$
        }
    }

    private void processCompilationUnitComment(CompilationUnit elem, EmitContext context) {
        CommentEmitTrait comment = elem.findModelTrait(CommentEmitTrait.class);
        if (comment == null) {
            return;
        }
        context.putBlockComment(comment.getContents());
    }

    private void processBlockComment(Model elem, EmitContext context) {
        CommentEmitTrait comment = elem.findModelTrait(CommentEmitTrait.class);
        if (comment == null) {
            return;
        }
        for (String line : comment.getContents()) {
            context.putLineComment(line);
        }
    }

    private void processInlineComment(Model elem, EmitContext context) {
        CommentEmitTrait comment = elem.findModelTrait(CommentEmitTrait.class);
        if (comment == null) {
            return;
        }
        for (String line : comment.getContents()) {
            context.putInlineComment(line);
        }
    }
}
