/**
 * Copyright 2011-2012 Asakusa Framework Team.
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

import com.asakusafw.utils.java.model.syntax.*;
import com.asakusafw.utils.java.model.syntax.ImportKind.Range;
import com.asakusafw.utils.java.model.syntax.ImportKind.Target;
import com.asakusafw.utils.java.model.util.CommentEmitTrait;
import com.asakusafw.utils.java.model.util.NoThrow;

/**
 * {@link Model}を出力する。
 */
public class ModelEmitter {

    private static final EmitEngine ENGINE = new EmitEngine();

    private PrintWriter writer;

    /**
     * インスタンスを生成する。
     * @param writer 出力先
     * @throws IllegalArgumentException 引数に{@code null}が含まれる場合
     */
    public ModelEmitter(PrintWriter writer) {
        if (writer == null) {
            throw new IllegalArgumentException("writer must not be null"); //$NON-NLS-1$
        }
        this.writer = writer;
    }

    /**
     * 指定の要素をこのインスタンスに関連した出力先に出力する。
     * @param element 出力する要素
     * @throws IllegalArgumentException 引数に{@code null}が含まれる場合
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
     * 指定の要素をこのインスタンスに関連した出力先に出力する。
     * @param element 出力する要素
     * @param context 利用するコンテキストオブジェクト
     * @throws IllegalArgumentException 引数に{@code null}が含まれる場合
     */
    public static void emit(Model element, EmitContext context) {
        if (context == null) {
            throw new IllegalArgumentException("context must not be null"); //$NON-NLS-1$
        }
        element.accept(ENGINE, context);
    }
}

/**
 * {@link ModelEmitter}のエンジン部分。
 */
class EmitEngine extends StrictVisitor<Void, EmitContext, NoThrow> {

    @Override
    public Void visitAlternateConstructorInvocation(
            AlternateConstructorInvocation elem, EmitContext context) {
        begin(elem, context);
        processBlockComment(elem, context);
        context.statement(EmitDirection.BEGIN);
        processTypeParameters(elem.getTypeArguments(), context);
        context.keyword("this");
        processParameters(elem.getArguments(), context);
        context.separator(";");
        context.statement(EmitDirection.END);
        return null;
    }

    @Override
    public Void visitAnnotationDeclaration(AnnotationDeclaration elem,
            EmitContext context) {
        begin(elem, context);
        context.declaration(EmitDirection.BEGIN);
        process(elem.getJavadoc(), context);
        processBlockComment(elem, context);
        process(elem.getModifiers(), context);
        context.symbol("@");
        context.keyword("interface");
        process(elem.getName(), context);
        context.classBlock(EmitDirection.BEGIN);
        process(elem.getBodyDeclarations(), context);
        context.classBlock(EmitDirection.END);
        context.declaration(EmitDirection.END);
        return null;
    }

    @Override
    public Void visitAnnotationElement(AnnotationElement elem,
            EmitContext context) {
        begin(elem, context);
        processInlineComment(elem, context);
        process(elem.getName(), context);
        context.operator("=");
        process(elem.getExpression(), context);
        return null;
    }

    @Override
    public Void visitAnnotationElementDeclaration(
            AnnotationElementDeclaration elem, EmitContext context) {
        begin(elem, context);
        context.declaration(EmitDirection.END);
        process(elem.getJavadoc(), context);
        processBlockComment(elem, context);
        process(elem.getModifiers(), context);
        process(elem.getType(), context);
        process(elem.getName(), context);
        processParameters(Collections.<Model>emptyList(), context);
        if (appears(elem.getDefaultExpression())) {
            context.keyword("default");
            process(elem.getDefaultExpression(), context);
        }
        context.separator(";");
        context.declaration(EmitDirection.END);
        return null;
    }

    @Override
    public Void visitArrayAccessExpression(ArrayAccessExpression elem,
            EmitContext context) {
        begin(elem, context);
        processInlineComment(elem, context);
        process(elem.getArray(), context);
        context.symbol("[");
        process(elem.getIndex(), context);
        context.separator("]");
        return null;
    }

    @Override
    public Void visitArrayCreationExpression(ArrayCreationExpression elem,
            EmitContext context) {
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
        context.keyword("new");
        process(scalar, context);
        for (Expression expr : elem.getDimensionExpressions()) {
            context.symbol("[");
            process(expr, context);
            context.separator("]");
            dim--;
        }
        // can be dim < 0 (invalid array type)
        for (int i = 0; i < dim; i++) {
            context.symbol("[");
            context.separator("]");
        }
        process(elem.getArrayInitializer(), context);
        return null;
    }

    @Override
    public Void visitArrayInitializer(ArrayInitializer elem,
            EmitContext context) {
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
        context.symbol("[");
        context.separator("]");
        return null;
    }

    @Override
    public Void visitAssertStatement(AssertStatement elem,
            EmitContext context) {
        begin(elem, context);
        processBlockComment(elem, context);
        context.statement(EmitDirection.BEGIN);
        context.keyword("assert");
        process(elem.getExpression(), context);
        if (appears(elem.getMessage())) {
            context.separator(":");
            process(elem.getMessage(), context);
        }
        context.separator(";");
        context.statement(EmitDirection.END);
        return null;
    }

    @Override
    public Void visitAssignmentExpression(AssignmentExpression elem,
            EmitContext context) {
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

    private static final Pattern HEAD_ASTER = Pattern.compile("^ ?\\* ?");
    @Override
    public Void visitBlockComment(BlockComment elem, EmitContext context) {
        String content = elem.getString();
        if (content.startsWith("/*")) {
            content = content.substring(2);
        }
        if (content.endsWith("*/")) {
            content = content.substring(0, content.length() - 2);
        }
        List<String> results = new ArrayList<String>();
        String[] lines = content.split("\\n|\\r|\\r\\n");
        for (String line : lines) {
            if (line.startsWith(" * ")) {
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
    public Void visitBreakStatement(BreakStatement elem,
            EmitContext context) {
        begin(elem, context);
        processBlockComment(elem, context);
        context.statement(EmitDirection.BEGIN);
        context.keyword("break");
        process(elem.getTarget(), context);
        context.separator(";");
        context.statement(EmitDirection.END);
        return null;
    }

    @Override
    public Void visitCastExpression(CastExpression elem,
            EmitContext context) {
        begin(elem, context);
        processInlineComment(elem, context);
        context.symbol("(");
        process(elem.getType(), context);
        context.separator(")");
        process(elem.getExpression(), context);
        return null;
    }

    @Override
    public Void visitCatchClause(CatchClause elem, EmitContext context) {
        begin(elem, context);
        processBlockComment(elem, context);
        context.keyword("catch");
        context.symbol("(");
        process(elem.getParameter(), context);
        context.separator(")");
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
    public Void visitClassDeclaration(ClassDeclaration elem,
            EmitContext context) {
        begin(elem, context);
        context.declaration(EmitDirection.BEGIN);
        process(elem.getJavadoc(), context);
        processBlockComment(elem, context);
        process(elem.getModifiers(), context);
        context.keyword("class");
        process(elem.getName(), context);
        processTypeParameters(elem.getTypeParameters(), context);
        if (appears(elem.getSuperClass())) {
            context.keyword("extends");
            process(elem.getSuperClass(), context);
        }
        if (appears(elem.getSuperInterfaceTypes())) {
            context.keyword("implements");
            processJoinWithComma(elem.getSuperInterfaceTypes(), context);
        }
        context.classBlock(EmitDirection.BEGIN);
        process(elem.getBodyDeclarations(), context);
        context.classBlock(EmitDirection.END);
        context.declaration(EmitDirection.END);
        return null;
    }

    @Override
    public Void visitClassInstanceCreationExpression(
            ClassInstanceCreationExpression elem, EmitContext context) {
        begin(elem, context);
        processInlineComment(elem, context);
        if (process(elem.getQualifier(), context)) {
            context.symbol(".");
        }
        context.keyword("new");
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
        context.symbol(".");
        context.keyword("class");
        return null;
    }

    @Override
    public Void visitCompilationUnit(CompilationUnit elem,
            EmitContext context) {
        begin(elem, context);
        processCompilationUnitComment(elem, context);
        process(elem.getPackageDeclaration(), context);
        process(elem.getImportDeclarations(), context);
        process(elem.getTypeDeclarations(), context);
        return null;
    }

    @Override
    public Void visitConditionalExpression(ConditionalExpression elem,
            EmitContext context) {
        begin(elem, context);
        processInlineComment(elem, context);
        process(elem.getCondition(), context);
        context.operator("?");
        process(elem.getThenExpression(), context);
        context.operator(":");
        process(elem.getElseExpression(), context);
        return null;
    }

    @Override
    public Void visitConstructorDeclaration(ConstructorDeclaration elem,
            EmitContext context) {
        begin(elem, context);
        context.declaration(EmitDirection.BEGIN);
        process(elem.getJavadoc(), context);
        processBlockComment(elem, context);
        process(elem.getModifiers(), context);
        processTypeParameters(elem.getTypeParameters(), context);
        process(elem.getName(), context);
        processParameters(elem.getFormalParameters(), context);
        if (appears(elem.getExceptionTypes())) {
            context.keyword("throws");
            processJoinWithComma(elem.getExceptionTypes(), context);
        }
        process(elem.getBody(), context);
        context.declaration(EmitDirection.END);
        return null;
    }

    @Override
    public Void visitContinueStatement(ContinueStatement elem,
            EmitContext context) {
        begin(elem, context);
        processBlockComment(elem, context);
        context.statement(EmitDirection.BEGIN);
        context.keyword("continue");
        process(elem.getTarget(), context);
        context.separator(";");
        context.statement(EmitDirection.END);
        return null;
    }

    @Override
    public Void visitDoStatement(DoStatement elem, EmitContext context) {
        begin(elem, context);
        processBlockComment(elem, context);
        context.statement(EmitDirection.BEGIN);
        context.keyword("do");
        process(elem.getBody(), context);
        context.keyword("while");
        context.symbol("(");
        process(elem.getCondition(), context);
        context.separator(")");
        context.separator(";");
        context.statement(EmitDirection.END);
        return null;
    }

    @Override
    public Void visitEmptyStatement(EmptyStatement elem,
            EmitContext context) {
        begin(elem, context);
        processBlockComment(elem, context);
        context.statement(EmitDirection.BEGIN);
        context.separator(";");
        context.statement(EmitDirection.END);
        return null;
    }

    @Override
    public Void visitEnhancedForStatement(EnhancedForStatement elem,
            EmitContext context) {
        begin(elem, context);
        processBlockComment(elem, context);
        context.statement(EmitDirection.BEGIN);
        context.keyword("for");
        context.symbol("(");
        process(elem.getParameter(), context);
        context.separator(":");
        process(elem.getExpression(), context);
        context.separator(")");
        process(elem.getBody(), context);
        context.statement(EmitDirection.END);
        return null;
    }

    @Override
    public Void visitEnumConstantDeclaration(EnumConstantDeclaration elem,
            EmitContext context) {
        begin(elem, context);
        context.declaration(EmitDirection.BEGIN);
        process(elem.getJavadoc(), context);
        processBlockComment(elem, context);
        process(elem.getModifiers(), context);
        process(elem.getName(), context);
        if (appears(elem.getArguments())) {
            processParameters(elem.getArguments(), context);
        }
        process(elem.getBody(), context);
        context.separator(",");
        context.declaration(EmitDirection.END);
        return null;
    }

    @Override
    public Void visitEnumDeclaration(EnumDeclaration elem,
            EmitContext context) {
        begin(elem, context);
        context.declaration(EmitDirection.BEGIN);
        process(elem.getJavadoc(), context);
        processBlockComment(elem, context);
        process(elem.getModifiers(), context);
        context.keyword("enum");
        process(elem.getName(), context);
        if (appears(elem.getSuperInterfaceTypes())) {
            context.keyword("implements");
            processJoinWithComma(elem.getSuperInterfaceTypes(), context);
        }
        context.classBlock(EmitDirection.BEGIN);
        process(elem.getConstantDeclarations(), context);
        if (appears(elem.getBodyDeclarations())) {
            context.declaration(EmitDirection.BEGIN);
            context.separator(";");
            context.declaration(EmitDirection.END);
            process(elem.getBodyDeclarations(), context);
        }
        context.classBlock(EmitDirection.END);
        context.declaration(EmitDirection.END);
        return null;
    }

    @Override
    public Void visitExpressionStatement(ExpressionStatement elem,
            EmitContext context) {
        begin(elem, context);
        processBlockComment(elem, context);
        context.statement(EmitDirection.BEGIN);
        process(elem.getExpression(), context);
        context.separator(";");
        context.statement(EmitDirection.END);
        return null;
    }

    @Override
    public Void visitFieldAccessExpression(FieldAccessExpression elem,
            EmitContext context) {
        begin(elem, context);
        processInlineComment(elem, context);
        process(elem.getQualifier(), context);
        context.symbol(".");
        process(elem.getName(), context);
        return null;
    }

    @Override
    public Void visitFieldDeclaration(FieldDeclaration elem,
            EmitContext context) {
        begin(elem, context);
        context.declaration(EmitDirection.BEGIN);
        process(elem.getJavadoc(), context);
        processBlockComment(elem, context);
        process(elem.getModifiers(), context);
        process(elem.getType(), context);
        processJoinWithComma(elem.getVariableDeclarators(), context);
        context.separator(";");
        context.declaration(EmitDirection.END);
        return null;
    }

    @Override
    public Void visitFormalParameterDeclaration(
            FormalParameterDeclaration elem, EmitContext context) {
        begin(elem, context);
        processInlineComment(elem, context);
        process(elem.getModifiers(), context);
        process(elem.getType(), context);
        if (elem.isVariableArity()) {
            context.separator("...");
        }
        process(elem.getName(), context);
        for (int i = 0, n = elem.getExtraDimensions(); i < n; i++) {
            context.symbol("[");
            context.separator("]");
        }
        return null;
    }

    @Override
    public Void visitForStatement(ForStatement elem, EmitContext context) {
        begin(elem, context);
        processBlockComment(elem, context);
        context.statement(EmitDirection.BEGIN);
        context.keyword("for");
        context.symbol("(");
        if (elem.getInitialization() instanceof LocalVariableDeclaration) {
            LocalVariableDeclaration decl =
                (LocalVariableDeclaration) elem.getInitialization();
            processLocalVaribale(decl, context);
        } else {
            process(elem.getInitialization(), context);
        }
        context.separator(";");
        process(elem.getCondition(), context);
        context.separator(";");
        process(elem.getUpdate(), context);
        context.separator(")");
        process(elem.getBody(), context);
        context.statement(EmitDirection.END);
        return null;
    }

    @Override
    public Void visitIfStatement(IfStatement elem, EmitContext context) {
        begin(elem, context);
        processBlockComment(elem, context);
        context.statement(EmitDirection.BEGIN);
        context.keyword("if");
        context.symbol("(");
        process(elem.getCondition(), context);
        context.separator(")");
        process(elem.getThenStatement(), context);
        if (appears(elem.getElseStatement())) {
            context.keyword("else");
            process(elem.getElseStatement(), context);
        }
        context.statement(EmitDirection.END);
        return null;
    }

    @Override
    public Void visitImportDeclaration(ImportDeclaration elem,
            EmitContext context) {
        begin(elem, context);
        processBlockComment(elem, context);
        context.declaration(EmitDirection.BEGIN);
        context.keyword("import");
        if (elem.getImportKind().getTarget() == Target.MEMBER) {
            context.keyword("static");
        }
        process(elem.getName(), context);
        if (elem.getImportKind().getRange() == Range.ON_DEMAND) {
            context.symbol(".");
            context.symbol("*");
        }
        context.separator(";");
        context.declaration(EmitDirection.END);
        return null;
    }

    @Override
    public Void visitInfixExpression(InfixExpression elem,
            EmitContext context) {
        begin(elem, context);
        processInlineComment(elem, context);
        process(elem.getLeftOperand(), context);
        context.operator(elem.getOperator().getSymbol());
        process(elem.getRightOperand(), context);
        return null;
    }

    @Override
    public Void visitInitializerDeclaration(InitializerDeclaration elem,
            EmitContext context) {
        begin(elem, context);
        context.declaration(EmitDirection.BEGIN);
        processBlockComment(elem, context);
        process(elem.getModifiers(), context);
        process(elem.getBody(), context);
        context.declaration(EmitDirection.END);
        return null;
    }

    @Override
    public Void visitInstanceofExpression(InstanceofExpression elem,
            EmitContext context) {
        begin(elem, context);
        processInlineComment(elem, context);
        process(elem.getExpression(), context);
        context.keyword("instanceof");
        process(elem.getType(), context);
        return null;
    }

    @Override
    public Void visitInterfaceDeclaration(InterfaceDeclaration elem,
            EmitContext context) {
        begin(elem, context);
        context.declaration(EmitDirection.BEGIN);
        process(elem.getJavadoc(), context);
        processBlockComment(elem, context);
        process(elem.getModifiers(), context);
        context.keyword("interface");
        process(elem.getName(), context);
        processTypeParameters(elem.getTypeParameters(), context);
        if (appears(elem.getSuperInterfaceTypes())) {
            context.keyword("extends");
            processJoinWithComma(elem.getSuperInterfaceTypes(), context);
        }
        context.classBlock(EmitDirection.BEGIN);
        process(elem.getBodyDeclarations(), context);
        context.classBlock(EmitDirection.END);
        context.declaration(EmitDirection.END);
        return null;
    }

    @Override
    public Void visitLabeledStatement(LabeledStatement elem,
            EmitContext context) {
        begin(elem, context);
        processBlockComment(elem, context);
        context.statement(EmitDirection.BEGIN);
        process(elem.getLabel(), context);
        context.separator(":");
        process(elem.getBody(), context);
        context.statement(EmitDirection.END);
        return null;
    }

    @Override
    public Void visitLineComment(LineComment elem, EmitContext context) {
        String body = elem.getString();
        if (body.startsWith("//")) {
            body = body.substring(2);
        }
        if (body.startsWith(" ")) {
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
    public Void visitLocalClassDeclaration(LocalClassDeclaration elem,
            EmitContext context) {
        begin(elem, context);
        processBlockComment(elem, context);
        context.statement(EmitDirection.BEGIN);
        process(elem.getDeclaration(), context);
        context.statement(EmitDirection.END);
        return null;
    }

    @Override
    public Void visitLocalVariableDeclaration(LocalVariableDeclaration elem,
            EmitContext context) {
        begin(elem, context);
        processBlockComment(elem, context);
        context.statement(EmitDirection.BEGIN);
        processLocalVaribale(elem, context);
        context.separator(";");
        context.statement(EmitDirection.END);
        return null;
    }

    private void processLocalVaribale(LocalVariableDeclaration elem,
            EmitContext context) {
        begin(elem, context);
        process(elem.getModifiers(), context);
        process(elem.getType(), context);
        processJoinWithComma(elem.getVariableDeclarators(), context);
    }

    @Override
    public Void visitMarkerAnnotation(MarkerAnnotation elem,
            EmitContext context) {
        begin(elem, context);
        processInlineComment(elem, context);
        context.symbol("@");
        process(elem.getType(), context);
        return null;
    }

    @Override
    public Void visitMethodDeclaration(MethodDeclaration elem,
            EmitContext context) {
        begin(elem, context);
        context.declaration(EmitDirection.BEGIN);
        process(elem.getJavadoc(), context);
        processBlockComment(elem, context);
        process(elem.getModifiers(), context);
        processTypeParameters(elem.getTypeParameters(), context);
        process(elem.getReturnType(), context);
        process(elem.getName(), context);
        processParameters(elem.getFormalParameters(), context);
        for (int i = 0, n = elem.getExtraDimensions(); i < n; i++) {
            context.symbol("[");
            context.separator("]");
        }
        if (appears(elem.getExceptionTypes())) {
            context.keyword("throws");
            // TODO indentation
            processJoinWithComma(elem.getExceptionTypes(), context);
        }
        if (appears(elem.getBody())) {
            process(elem.getBody(), context);
        } else {
            context.separator(";");
        }
        context.declaration(EmitDirection.END);
        return null;
    }

    @Override
    public Void visitMethodInvocationExpression(
            MethodInvocationExpression elem, EmitContext context) {
        begin(elem, context);
        processInlineComment(elem, context);
        if (process(elem.getQualifier(), context)) {
            context.symbol(".");
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
    public Void visitNormalAnnotation(NormalAnnotation elem,
            EmitContext context) {
        begin(elem, context);
        processInlineComment(elem, context);
        context.symbol("@");
        process(elem.getType(), context);
        processParameters(elem.getElements(), context);
        return null;
    }

    @Override
    public Void visitPackageDeclaration(PackageDeclaration elem,
            EmitContext context) {
        begin(elem, context);
        context.declaration(EmitDirection.BEGIN);
        process(elem.getJavadoc(), context);
        processBlockComment(elem, context);
        context.keyword("package");
        process(elem.getName(), context);
        context.separator(";");
        context.declaration(EmitDirection.END);
        return null;
    }

    @Override
    public Void visitParameterizedType(ParameterizedType elem,
            EmitContext context) {
        begin(elem, context);
        processInlineComment(elem, context);
        process(elem.getType(), context);
        processTypeParameters(elem.getTypeArguments(), context);
        return null;
    }

    @Override
    public Void visitParenthesizedExpression(ParenthesizedExpression elem,
            EmitContext context) {
        begin(elem, context);
        processInlineComment(elem, context);
        context.symbol("(");
        process(elem.getExpression(), context);
        context.separator(")");
        return null;
    }

    @Override
    public Void visitPostfixExpression(PostfixExpression elem,
            EmitContext context) {
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
        context.symbol(".");
        process(elem.getSimpleName(), context);
        return null;
    }

    @Override
    public Void visitQualifiedType(QualifiedType elem, EmitContext context) {
        begin(elem, context);
        processInlineComment(elem, context);
        process(elem.getQualifier(), context);
        context.symbol(".");
        process(elem.getSimpleName(), context);
        return null;
    }

    @Override
    public Void visitReturnStatement(ReturnStatement elem,
            EmitContext context) {
        begin(elem, context);
        processBlockComment(elem, context);
        context.statement(EmitDirection.BEGIN);
        context.keyword("return");
        process(elem.getExpression(), context);
        context.separator(";");
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
    public Void visitSingleElementAnnotation(SingleElementAnnotation elem,
            EmitContext context) {
        begin(elem, context);
        processInlineComment(elem, context);
        context.symbol("@");
        process(elem.getType(), context);
        context.symbol("(");
        process(elem.getExpression(), context);
        context.separator(")");
        return null;
    }

    @Override
    public Void visitStatementExpressionList(StatementExpressionList elem,
            EmitContext context) {
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
            context.symbol(".");
        }
        context.keyword("super");
        return null;
    }

    @Override
    public Void visitSuperConstructorInvocation(
            SuperConstructorInvocation elem, EmitContext context) {
        begin(elem, context);
        processBlockComment(elem, context);
        context.statement(EmitDirection.BEGIN);
        if (process(elem.getQualifier(), context)) {
            context.symbol(".");
        }
        processTypeParameters(elem.getTypeArguments(), context);
        context.keyword("super");
        processParameters(elem.getArguments(), context);
        context.separator(";");
        context.statement(EmitDirection.END);
        return null;
    }

    @Override
    public Void visitSwitchCaseLabel(SwitchCaseLabel elem,
            EmitContext context) {
        begin(elem, context);
        processBlockComment(elem, context);
        context.statement(EmitDirection.BEGIN);
        context.keyword("case");
        process(elem.getExpression(), context);
        context.symbol(":");
        context.statement(EmitDirection.END);
        return null;
    }

    @Override
    public Void visitSwitchDefaultLabel(SwitchDefaultLabel elem,
            EmitContext context) {
        begin(elem, context);
        processBlockComment(elem, context);
        context.statement(EmitDirection.BEGIN);
        context.keyword("default");
        context.symbol(":");
        context.statement(EmitDirection.END);
        return null;
    }

    @Override
    public Void visitSwitchStatement(SwitchStatement elem,
            EmitContext context) {
        begin(elem, context);
        processBlockComment(elem, context);
        context.statement(EmitDirection.BEGIN);
        context.keyword("switch");
        context.symbol("(");
        process(elem.getExpression(), context);
        context.separator(")");
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
    public Void visitSynchronizedStatement(SynchronizedStatement elem,
            EmitContext context) {
        begin(elem, context);
        processBlockComment(elem, context);
        context.statement(EmitDirection.BEGIN);
        context.keyword("synchronized");
        context.symbol("(");
        process(elem.getExpression(), context);
        context.separator(")");
        process(elem.getBody(), context);
        context.statement(EmitDirection.END);
        return null;
    }

    @Override
    public Void visitThis(This elem, EmitContext context) {
        begin(elem, context);
        processInlineComment(elem, context);
        if (process(elem.getQualifier(), context)) {
            context.symbol(".");
        }
        context.keyword("this");
        return null;
    }

    @Override
    public Void visitThrowStatement(ThrowStatement elem,
            EmitContext context) {
        begin(elem, context);
        processBlockComment(elem, context);
        context.statement(EmitDirection.BEGIN);
        context.keyword("throw");
        process(elem.getExpression(), context);
        context.separator(";");
        context.statement(EmitDirection.END);
        return null;
    }

    @Override
    public Void visitTryStatement(TryStatement elem, EmitContext context) {
        begin(elem, context);
        processBlockComment(elem, context);
        context.statement(EmitDirection.BEGIN);
        context.keyword("try");
        process(elem.getTryBlock(), context);
        process(elem.getCatchClauses(), context);
        if (appears(elem.getFinallyBlock())) {
            context.keyword("finally");
            process(elem.getFinallyBlock(), context);
        }
        context.statement(EmitDirection.END);
        return null;
    }

    @Override
    public Void visitTypeParameterDeclaration(TypeParameterDeclaration elem,
            EmitContext context) {
        begin(elem, context);
        processInlineComment(elem, context);
        process(elem.getName(), context);
        Iterator<? extends Type> iter = elem.getTypeBounds().iterator();
        if (iter.hasNext()) {
            context.keyword("extends");
            process(iter.next(), context);
            while (iter.hasNext()) {
                context.separator("&");
                process(iter.next(), context);
            }
        }
        return null;
    }

    @Override
    public Void visitUnaryExpression(UnaryExpression elem,
            EmitContext context) {
        begin(elem, context);
        processInlineComment(elem, context);
        context.operator(elem.getOperator().getSymbol());
        process(elem.getOperand(), context);
        return null;
    }

    @Override
    public Void visitVariableDeclarator(VariableDeclarator elem,
            EmitContext context) {
        begin(elem, context);
        processInlineComment(elem, context);
        process(elem.getName(), context);
        for (int i = 0, n = elem.getExtraDimensions(); i < n; i++) {
            context.symbol("[");
            context.separator("]");
        }
        if (appears(elem.getInitializer())) {
            context.operator("=");
            process(elem.getInitializer(), context);
        }
        return null;
    }

    @Override
    public Void visitWhileStatement(WhileStatement elem,
            EmitContext context) {
        begin(elem, context);
        processBlockComment(elem, context);
        context.statement(EmitDirection.BEGIN);
        context.keyword("while");
        context.symbol("(");
        process(elem.getCondition(), context);
        context.separator(")");
        process(elem.getBody(), context);
        context.statement(EmitDirection.END);
        return null;
    }

    @Override
    public Void visitWildcard(Wildcard elem, EmitContext context) {
        begin(elem, context);
        processInlineComment(elem, context);
        context.keyword("?");
        if (elem.getBoundKind() == WildcardBoundKind.UPPER_BOUNDED) {
            context.keyword("extends");
            process(elem.getTypeBound(), context);
        } else if (elem.getBoundKind() == WildcardBoundKind.LOWER_BOUNDED) {
            context.keyword("super");
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
        if (tag.equals("@param") && isDocTypeParameter(elements)) {
            // @param <T>
            context.symbol("<");
            context.symbol(((SimpleName) elements.get(1)).getToken());
            context.symbol(">");
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
        if (((DocText) elements.get(0)).getString().equals("<") == false) {
            return false;
        }
        if (((DocText) elements.get(2)).getString().equals(">") == false) {
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
        context.symbol("#");
        process(elem.getName(), context);
        return null;
    }

    @Override
    public Void visitDocMethod(DocMethod elem, EmitContext context) {
        begin(elem, context);
        process(elem.getType(), context);
        context.symbol("#");
        process(elem.getName(), context);
        context.symbol("(");
        processJoinWithComma(elem.getFormalParameters(), context);
        context.separator(")");
        return null;
    }

    @Override
    public Void visitDocMethodParameter(DocMethodParameter elem,
            EmitContext context) {
        begin(elem, context);
        process(elem.getType(), context);
        if (elem.isVariableArity()) {
            context.separator("...");
        }
        process(elem.getName(), context);
        return null;
    }

    @Override
    public Void visitDocText(DocText elem, EmitContext context) {
        begin(elem, context);
        if (elem.getString().startsWith(" ")) {
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

    private boolean process(List<? extends Model> elements,
            EmitContext context) {
        for (Model element : elements) {
            element.accept(this, context);
        }
        return true;
    }

    private void processJoinWithComma(List<? extends Model> elements,
            EmitContext context) {
        Iterator<? extends Model> iter = elements.iterator();
        if (iter.hasNext()) {
            process(iter.next(), context);
            while (iter.hasNext()) {
                context.separator(",");
                process(iter.next(), context);
            }
        }
    }

    private void processParameters(List<? extends Model> elements,
            EmitContext context) {
        // TODO indentation
        context.symbol("(");
        processJoinWithComma(elements, context);
        context.separator(")");
    }

    private void processTypeParameters(List<? extends Model> elements,
            EmitContext context) {
        if (appears(elements)) {
            context.symbol("<");
            processJoinWithComma(elements, context);
            context.separator(">");
        }
    }

    private void processCompilationUnitComment(
            CompilationUnit elem,
            EmitContext context) {
        CommentEmitTrait comment =
            elem.findModelTrait(CommentEmitTrait.class);
        if (comment == null) {
            return;
        }
        context.putBlockComment(comment.getContents());
    }

    private void processBlockComment(Model elem, EmitContext context) {
        CommentEmitTrait comment =
            elem.findModelTrait(CommentEmitTrait.class);
        if (comment == null) {
            return;
        }
        for (String line : comment.getContents()) {
            context.putLineComment(line);
        }
    }

    private void processInlineComment(Model elem, EmitContext context) {
        CommentEmitTrait comment =
            elem.findModelTrait(CommentEmitTrait.class);
        if (comment == null) {
            return;
        }
        for (String line : comment.getContents()) {
            context.putInlineComment(line);
        }
    }
}
