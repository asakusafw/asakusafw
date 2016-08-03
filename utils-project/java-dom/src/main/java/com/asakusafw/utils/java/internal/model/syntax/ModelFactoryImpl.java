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
package com.asakusafw.utils.java.internal.model.syntax;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import javax.annotation.Generated;

import com.asakusafw.utils.java.internal.model.util.ExpressionPriority;
import com.asakusafw.utils.java.model.syntax.*;

//CHECKSTYLE:OFF
/**
 * An implementation of {@link ModelFactory}.
 */
@Generated("com.asakusafw.utils.java.model.syntax.ModelFactory")
public class ModelFactoryImpl implements ModelFactory {

    @Override
    public AlternateConstructorInvocation newAlternateConstructorInvocation(
            Expression...arguments) {
        Util.notNull(arguments, "arguments"); //$NON-NLS-1$
        return this.newAlternateConstructorInvocation0(
            Collections.emptyList(),
            Arrays.asList(arguments)
        );
    }

    @Override
    public AlternateConstructorInvocation newAlternateConstructorInvocation(
            List<? extends Expression> arguments) {
        return this.newAlternateConstructorInvocation0(
            Collections.emptyList(),
            arguments
        );
    }

    @Override
    public AlternateConstructorInvocation newAlternateConstructorInvocation(
            List<? extends Type> typeArguments,
            List<? extends Expression> arguments) {
        return this.newAlternateConstructorInvocation0(
            typeArguments,
            arguments
        );
    }

    private AlternateConstructorInvocationImpl newAlternateConstructorInvocation0(
            List<? extends Type> typeArguments,
            List<? extends Expression> arguments) {
        Util.notNull(typeArguments, "typeArguments"); //$NON-NLS-1$
        Util.notContainNull(typeArguments, "typeArguments"); //$NON-NLS-1$
        Util.notNull(arguments, "arguments"); //$NON-NLS-1$
        Util.notContainNull(arguments, "arguments"); //$NON-NLS-1$
        AlternateConstructorInvocationImpl result = new AlternateConstructorInvocationImpl();
        result.setTypeArguments(typeArguments);
        result.setArguments(arguments);
        return result;
    }

    @Override
    public AnnotationDeclaration newAnnotationDeclaration(
            Javadoc javadoc,
            List<? extends Attribute> modifiers,
            SimpleName name,
            List<? extends TypeBodyDeclaration> bodyDeclarations) {
        return this.newAnnotationDeclaration0(
            javadoc,
            modifiers,
            name,
            bodyDeclarations
        );
    }

    private AnnotationDeclarationImpl newAnnotationDeclaration0(
            Javadoc javadoc,
            List<? extends Attribute> modifiers,
            SimpleName name,
            List<? extends TypeBodyDeclaration> bodyDeclarations) {
        Util.notNull(modifiers, "modifiers"); //$NON-NLS-1$
        Util.notContainNull(modifiers, "modifiers"); //$NON-NLS-1$
        Util.notNull(name, "name"); //$NON-NLS-1$
        Util.notNull(bodyDeclarations, "bodyDeclarations"); //$NON-NLS-1$
        Util.notContainNull(bodyDeclarations, "bodyDeclarations"); //$NON-NLS-1$
        AnnotationDeclarationImpl result = new AnnotationDeclarationImpl();
        result.setJavadoc(javadoc);
        result.setModifiers(modifiers);
        result.setName(name);
        result.setBodyDeclarations(bodyDeclarations);
        return result;
    }

    @Override
    public AnnotationElement newAnnotationElement(
            SimpleName name,
            Expression expression) {
        return this.newAnnotationElement0(
            name,
            expression
        );
    }

    private AnnotationElementImpl newAnnotationElement0(
            SimpleName name,
            Expression expression) {
        Util.notNull(name, "name"); //$NON-NLS-1$
        Util.notNull(expression, "expression"); //$NON-NLS-1$
        AnnotationElementImpl result = new AnnotationElementImpl();
        result.setName(name);
        result.setExpression(expression);
        return result;
    }

    @Override
    public AnnotationElementDeclaration newAnnotationElementDeclaration(
            Javadoc javadoc,
            List<? extends Attribute> modifiers,
            Type type,
            SimpleName name,
            Expression defaultExpression) {
        return this.newAnnotationElementDeclaration0(
            javadoc,
            modifiers,
            type,
            name,
            defaultExpression
        );
    }

    private AnnotationElementDeclarationImpl newAnnotationElementDeclaration0(
            Javadoc javadoc,
            List<? extends Attribute> modifiers,
            Type type,
            SimpleName name,
            Expression defaultExpression) {
        Util.notNull(modifiers, "modifiers"); //$NON-NLS-1$
        Util.notContainNull(modifiers, "modifiers"); //$NON-NLS-1$
        Util.notNull(type, "type"); //$NON-NLS-1$
        Util.notNull(name, "name"); //$NON-NLS-1$
        AnnotationElementDeclarationImpl result = new AnnotationElementDeclarationImpl();
        result.setJavadoc(javadoc);
        result.setModifiers(modifiers);
        result.setType(type);
        result.setName(name);
        result.setDefaultExpression(defaultExpression);
        return result;
    }

    @Override
    public ArrayAccessExpression newArrayAccessExpression(
            Expression array,
            Expression index) {
        return this.newArrayAccessExpression0(
            array,
            index
        );
    }

    private ArrayAccessExpressionImpl newArrayAccessExpression0(
            Expression array,
            Expression index) {
        Util.notNull(array, "array"); //$NON-NLS-1$
        Util.notNull(index, "index"); //$NON-NLS-1$
        ArrayAccessExpressionImpl result = new ArrayAccessExpressionImpl();
        result.setArray(parenthesize(array, ExpressionPriority.PRIMARY));
        result.setIndex(index);
        return result;
    }

    @Override
    public ArrayCreationExpression newArrayCreationExpression(
            ArrayType type,
            ArrayInitializer arrayInitializer) {
        return this.newArrayCreationExpression0(
            type,
            Collections.emptyList(),
            arrayInitializer
        );
    }

    @Override
    public ArrayCreationExpression newArrayCreationExpression(
            ArrayType type,
            List<? extends Expression> dimensionExpressions,
            ArrayInitializer arrayInitializer) {
        return this.newArrayCreationExpression0(
            type,
            dimensionExpressions,
            arrayInitializer
        );
    }

    private ArrayCreationExpressionImpl newArrayCreationExpression0(
            ArrayType type,
            List<? extends Expression> dimensionExpressions,
            ArrayInitializer arrayInitializer) {
        Util.notNull(type, "type"); //$NON-NLS-1$
        Util.notNull(dimensionExpressions, "dimensionExpressions"); //$NON-NLS-1$
        Util.notContainNull(dimensionExpressions, "dimensionExpressions"); //$NON-NLS-1$
        ArrayCreationExpressionImpl result = new ArrayCreationExpressionImpl();
        result.setType(type);
        result.setDimensionExpressions(dimensionExpressions);
        result.setArrayInitializer(arrayInitializer);
        return result;
    }

    @Override
    public ArrayInitializer newArrayInitializer(Expression...elements) {
        Util.notNull(elements, "elements"); //$NON-NLS-1$
        return this.newArrayInitializer0(
            Arrays.asList(elements)
        );
    }

    @Override
    public ArrayInitializer newArrayInitializer(
            List<? extends Expression> elements) {
        return this.newArrayInitializer0(
            elements
        );
    }

    private ArrayInitializerImpl newArrayInitializer0(
            List<? extends Expression> elements) {
        Util.notNull(elements, "elements"); //$NON-NLS-1$
        Util.notContainNull(elements, "elements"); //$NON-NLS-1$
        ArrayInitializerImpl result = new ArrayInitializerImpl();
        result.setElements(elements);
        return result;
    }

    @Override
    public ArrayType newArrayType(
            Type componentType) {
        return this.newArrayType0(
            componentType
        );
    }

    private ArrayTypeImpl newArrayType0(
            Type componentType) {
        Util.notNull(componentType, "componentType"); //$NON-NLS-1$
        ArrayTypeImpl result = new ArrayTypeImpl();
        result.setComponentType(componentType);
        return result;
    }

    @Override
    public AssertStatement newAssertStatement(
            Expression expression) {
        return this.newAssertStatement0(
            expression,
            null
        );
    }

    @Override
    public AssertStatement newAssertStatement(
            Expression expression,
            Expression message) {
        return this.newAssertStatement0(
            expression,
            message
        );
    }

    private AssertStatementImpl newAssertStatement0(
            Expression expression,
            Expression message) {
        Util.notNull(expression, "expression"); //$NON-NLS-1$
        AssertStatementImpl result = new AssertStatementImpl();
        result.setExpression(expression);
        result.setMessage(message);
        return result;
    }

    @Override
    public AssignmentExpression newAssignmentExpression(
            Expression leftHandSide,
            Expression rightHandSide) {
        return this.newAssignmentExpression0(
            leftHandSide,
            InfixOperator.ASSIGN,
            rightHandSide
        );
    }

    @Override
    public AssignmentExpression newAssignmentExpression(
            Expression leftHandSide,
            InfixOperator operator,
            Expression rightHandSide) {
        return this.newAssignmentExpression0(
            leftHandSide,
            operator,
            rightHandSide
        );
    }

    private AssignmentExpressionImpl newAssignmentExpression0(
            Expression leftHandSide,
            InfixOperator operator,
            Expression rightHandSide) {
        Util.notNull(leftHandSide, "leftHandSide"); //$NON-NLS-1$
        Util.notNull(operator, "operator"); //$NON-NLS-1$
        Util.notNull(rightHandSide, "rightHandSide"); //$NON-NLS-1$
        AssignmentExpressionImpl result = new AssignmentExpressionImpl();
        result.setLeftHandSide(parenthesize(leftHandSide, ExpressionPriority.ASSIGNMENT));
        result.setOperator(operator);
        result.setRightHandSide(parenthesizeRight(rightHandSide, ExpressionPriority.ASSIGNMENT));
        return result;
    }

    @Override
    public BasicType newBasicType(
            BasicTypeKind typeKind) {
        return this.newBasicType0(
            typeKind
        );
    }

    private BasicTypeImpl newBasicType0(
            BasicTypeKind typeKind) {
        Util.notNull(typeKind, "typeKind"); //$NON-NLS-1$
        BasicTypeImpl result = new BasicTypeImpl();
        result.setTypeKind(typeKind);
        return result;
    }

    @Override
    public Block newBlock(
            Statement... statements) {
        Util.notNull(statements, "statements"); //$NON-NLS-1$
        return this.newBlock0(
            Arrays.asList(statements)
        );
    }

    @Override
    public Block newBlock(
            List<? extends Statement> statements) {
        return this.newBlock0(
            statements
        );
    }

    private BlockImpl newBlock0(
            List<? extends Statement> statements) {
        Util.notNull(statements, "statements"); //$NON-NLS-1$
        Util.notContainNull(statements, "statements"); //$NON-NLS-1$
        BlockImpl result = new BlockImpl();
        result.setStatements(statements);
        return result;
    }

    @Override
    public BlockComment newBlockComment(
            String string) {
        return this.newBlockComment0(
            string
        );
    }

    private BlockCommentImpl newBlockComment0(
            String string) {
        Util.notNull(string, "string"); //$NON-NLS-1$
        BlockCommentImpl result = new BlockCommentImpl();
        result.setString(string);
        return result;
    }

    @Override
    public BreakStatement newBreakStatement() {
        return this.newBreakStatement0(
            null
        );
    }

    @Override
    public BreakStatement newBreakStatement(
            SimpleName target) {
        return this.newBreakStatement0(
            target
        );
    }

    private BreakStatementImpl newBreakStatement0(
            SimpleName target) {
        BreakStatementImpl result = new BreakStatementImpl();
        result.setTarget(target);
        return result;
    }

    @Override
    public CastExpression newCastExpression(
            Type type,
            Expression expression) {
        return this.newCastExpression0(
            type,
            expression
        );
    }

    private CastExpressionImpl newCastExpression0(
            Type type,
            Expression expression) {
        Util.notNull(type, "type"); //$NON-NLS-1$
        Util.notNull(expression, "expression"); //$NON-NLS-1$
        CastExpressionImpl result = new CastExpressionImpl();
        result.setType(type);
        result.setExpression(parenthesize(expression, ExpressionPriority.CAST));
        return result;
    }

    @Override
    public CatchClause newCatchClause(
            FormalParameterDeclaration parameter,
            Block body) {
        return this.newCatchClause0(
            parameter,
            body
        );
    }

    private CatchClauseImpl newCatchClause0(
            FormalParameterDeclaration parameter,
            Block body) {
        Util.notNull(parameter, "parameter"); //$NON-NLS-1$
        Util.notNull(body, "body"); //$NON-NLS-1$
        CatchClauseImpl result = new CatchClauseImpl();
        result.setParameter(parameter);
        result.setBody(body);
        return result;
    }

    @Override
    public ClassBody newClassBody(
            List<? extends TypeBodyDeclaration> bodyDeclarations) {
        return this.newClassBody0(
            bodyDeclarations
        );
    }

    private ClassBodyImpl newClassBody0(
            List<? extends TypeBodyDeclaration> bodyDeclarations) {
        Util.notNull(bodyDeclarations, "bodyDeclarations"); //$NON-NLS-1$
        Util.notContainNull(bodyDeclarations, "bodyDeclarations"); //$NON-NLS-1$
        ClassBodyImpl result = new ClassBodyImpl();
        result.setBodyDeclarations(bodyDeclarations);
        return result;
    }

    @Override
    public ClassDeclaration newClassDeclaration(
            Javadoc javadoc,
            List<? extends Attribute> modifiers,
            SimpleName name,
            Type superClass,
            List<? extends Type> superInterfaceTypes,
            List<? extends TypeBodyDeclaration> bodyDeclarations) {
        return this.newClassDeclaration0(
            javadoc,
            modifiers,
            name,
            Collections.emptyList(),
            superClass,
            superInterfaceTypes,
            bodyDeclarations
        );
    }

    @Override
    public ClassDeclaration newClassDeclaration(
            Javadoc javadoc,
            List<? extends Attribute> modifiers,
            SimpleName name,
            List<? extends TypeParameterDeclaration> typeParameters,
            Type superClass,
            List<? extends Type> superInterfaceTypes,
            List<? extends TypeBodyDeclaration> bodyDeclarations) {
        return this.newClassDeclaration0(
            javadoc,
            modifiers,
            name,
            typeParameters,
            superClass,
            superInterfaceTypes,
            bodyDeclarations
        );
    }

    private ClassDeclarationImpl newClassDeclaration0(
            Javadoc javadoc,
            List<? extends Attribute> modifiers,
            SimpleName name,
            List<? extends TypeParameterDeclaration> typeParameters,
            Type superClass,
            List<? extends Type> superInterfaceTypes,
            List<? extends TypeBodyDeclaration> bodyDeclarations) {
        Util.notNull(modifiers, "modifiers"); //$NON-NLS-1$
        Util.notContainNull(modifiers, "modifiers"); //$NON-NLS-1$
        Util.notNull(name, "name"); //$NON-NLS-1$
        Util.notNull(typeParameters, "typeParameters"); //$NON-NLS-1$
        Util.notContainNull(typeParameters, "typeParameters"); //$NON-NLS-1$
        Util.notNull(superInterfaceTypes, "superInterfaceTypes"); //$NON-NLS-1$
        Util.notContainNull(superInterfaceTypes, "superInterfaceTypes"); //$NON-NLS-1$
        Util.notNull(bodyDeclarations, "bodyDeclarations"); //$NON-NLS-1$
        Util.notContainNull(bodyDeclarations, "bodyDeclarations"); //$NON-NLS-1$
        ClassDeclarationImpl result = new ClassDeclarationImpl();
        result.setJavadoc(javadoc);
        result.setModifiers(modifiers);
        result.setName(name);
        result.setTypeParameters(typeParameters);
        result.setSuperClass(superClass);
        result.setSuperInterfaceTypes(superInterfaceTypes);
        result.setBodyDeclarations(bodyDeclarations);
        return result;
    }

    @Override
    public ClassInstanceCreationExpression newClassInstanceCreationExpression(
            Type type,
            Expression...arguments) {
        Util.notNull(arguments, "arguments"); //$NON-NLS-1$
        return this.newClassInstanceCreationExpression0(
            null,
            Collections.emptyList(),
            type,
            Arrays.asList(arguments),
            null
        );
    }

    @Override
    public ClassInstanceCreationExpression newClassInstanceCreationExpression(
            Type type,
            List<? extends Expression> arguments) {
        return this.newClassInstanceCreationExpression0(
            null,
            Collections.emptyList(),
            type,
            arguments,
            null
        );
    }

    @Override
    public ClassInstanceCreationExpression newClassInstanceCreationExpression(
            Expression qualifier,
            List<? extends Type> typeArguments,
            Type type,
            List<? extends Expression> arguments,
            ClassBody body) {
        return this.newClassInstanceCreationExpression0(
            qualifier,
            typeArguments,
            type,
            arguments,
            body
        );
    }

    private ClassInstanceCreationExpressionImpl newClassInstanceCreationExpression0(
            Expression qualifier,
            List<? extends Type> typeArguments,
            Type type,
            List<? extends Expression> arguments,
            ClassBody body) {
        Util.notNull(typeArguments, "typeArguments"); //$NON-NLS-1$
        Util.notContainNull(typeArguments, "typeArguments"); //$NON-NLS-1$
        Util.notNull(type, "type"); //$NON-NLS-1$
        Util.notNull(arguments, "arguments"); //$NON-NLS-1$
        Util.notContainNull(arguments, "arguments"); //$NON-NLS-1$
        ClassInstanceCreationExpressionImpl result = new ClassInstanceCreationExpressionImpl();
        result.setQualifier(parenthesize(qualifier, ExpressionPriority.PRIMARY));
        result.setTypeArguments(typeArguments);
        result.setType(type);
        result.setArguments(arguments);
        result.setBody(body);
        return result;
    }

    @Override
    public ClassLiteral newClassLiteral(
            Type type) {
        return this.newClassLiteral0(
            type
        );
    }

    private ClassLiteralImpl newClassLiteral0(
            Type type) {
        Util.notNull(type, "type"); //$NON-NLS-1$
        ClassLiteralImpl result = new ClassLiteralImpl();
        result.setType(type);
        return result;
    }

    @Override
    public CompilationUnit newCompilationUnit(
            PackageDeclaration packageDeclaration,
            List<? extends ImportDeclaration> importDeclarations,
            List<? extends TypeDeclaration> typeDeclarations,
            List<? extends Comment> comments) {
        return this.newCompilationUnit0(
            packageDeclaration,
            importDeclarations,
            typeDeclarations,
            comments
        );
    }

    private CompilationUnitImpl newCompilationUnit0(
            PackageDeclaration packageDeclaration,
            List<? extends ImportDeclaration> importDeclarations,
            List<? extends TypeDeclaration> typeDeclarations,
            List<? extends Comment> comments) {
        Util.notNull(importDeclarations, "importDeclarations"); //$NON-NLS-1$
        Util.notContainNull(importDeclarations, "importDeclarations"); //$NON-NLS-1$
        Util.notNull(typeDeclarations, "typeDeclarations"); //$NON-NLS-1$
        Util.notContainNull(typeDeclarations, "typeDeclarations"); //$NON-NLS-1$
        Util.notNull(comments, "comments"); //$NON-NLS-1$
        Util.notContainNull(comments, "comments"); //$NON-NLS-1$
        CompilationUnitImpl result = new CompilationUnitImpl();
        result.setPackageDeclaration(packageDeclaration);
        result.setImportDeclarations(importDeclarations);
        result.setTypeDeclarations(typeDeclarations);
        result.setComments(comments);
        return result;
    }

    @Override
    public ConditionalExpression newConditionalExpression(
            Expression condition,
            Expression thenExpression,
            Expression elseExpression) {
        return this.newConditionalExpression0(
            condition,
            thenExpression,
            elseExpression
        );
    }

    private ConditionalExpressionImpl newConditionalExpression0(
            Expression condition,
            Expression thenExpression,
            Expression elseExpression) {
        Util.notNull(condition, "condition"); //$NON-NLS-1$
        Util.notNull(thenExpression, "thenExpression"); //$NON-NLS-1$
        Util.notNull(elseExpression, "elseExpression"); //$NON-NLS-1$
        ConditionalExpressionImpl result = new ConditionalExpressionImpl();
        result.setCondition(parenthesize(condition, ExpressionPriority.CONDITIONAL));
        result.setThenExpression(parenthesize(thenExpression, ExpressionPriority.CONDITIONAL));
        result.setElseExpression(parenthesize(elseExpression, ExpressionPriority.CONDITIONAL));
        return result;
    }

    @Override
    public ConstructorDeclaration newConstructorDeclaration(
            Javadoc javadoc,
            List<? extends Attribute> modifiers,
            SimpleName name,
            List<? extends FormalParameterDeclaration> formalParameters,
            List<? extends Statement> statements) {
        Util.notNull(statements, "statements"); //$NON-NLS-1$
        return this.newConstructorDeclaration0(
            javadoc,
            modifiers,
            Collections.emptyList(),
            name,
            formalParameters,
            Collections.emptyList(),
            newBlock(statements)
        );
    }

    @Override
    public ConstructorDeclaration newConstructorDeclaration(
            Javadoc javadoc,
            List<? extends Attribute> modifiers,
            List<? extends TypeParameterDeclaration> typeParameters,
            SimpleName name,
            List<? extends FormalParameterDeclaration> formalParameters,
            List<? extends Type> exceptionTypes,
            Block body) {
        return this.newConstructorDeclaration0(
            javadoc,
            modifiers,
            typeParameters,
            name,
            formalParameters,
            exceptionTypes,
            body
        );
    }

    private ConstructorDeclarationImpl newConstructorDeclaration0(
            Javadoc javadoc,
            List<? extends Attribute> modifiers,
            List<? extends TypeParameterDeclaration> typeParameters,
            SimpleName name,
            List<? extends FormalParameterDeclaration> formalParameters,
            List<? extends Type> exceptionTypes,
            Block body) {
        Util.notNull(modifiers, "modifiers"); //$NON-NLS-1$
        Util.notContainNull(modifiers, "modifiers"); //$NON-NLS-1$
        Util.notNull(typeParameters, "typeParameters"); //$NON-NLS-1$
        Util.notContainNull(typeParameters, "typeParameters"); //$NON-NLS-1$
        Util.notNull(name, "name"); //$NON-NLS-1$
        Util.notNull(formalParameters, "formalParameters"); //$NON-NLS-1$
        Util.notContainNull(formalParameters, "formalParameters"); //$NON-NLS-1$
        Util.notNull(exceptionTypes, "exceptionTypes"); //$NON-NLS-1$
        Util.notContainNull(exceptionTypes, "exceptionTypes"); //$NON-NLS-1$
        Util.notNull(body, "body"); //$NON-NLS-1$
        ConstructorDeclarationImpl result = new ConstructorDeclarationImpl();
        result.setJavadoc(javadoc);
        result.setModifiers(modifiers);
        result.setTypeParameters(typeParameters);
        result.setName(name);
        result.setFormalParameters(formalParameters);
        result.setExceptionTypes(exceptionTypes);
        result.setBody(body);
        return result;
    }

    @Override
    public ContinueStatement newContinueStatement() {
        return this.newContinueStatement0(
            null
        );
    }

    @Override
    public ContinueStatement newContinueStatement(
            SimpleName target) {
        return this.newContinueStatement0(
            target
        );
    }

    private ContinueStatementImpl newContinueStatement0(
            SimpleName target) {
        ContinueStatementImpl result = new ContinueStatementImpl();
        result.setTarget(target);
        return result;
    }

    @Override
    public DoStatement newDoStatement(
            Statement body,
            Expression condition) {
        return this.newDoStatement0(
            body,
            condition
        );
    }

    private DoStatementImpl newDoStatement0(
            Statement body,
            Expression condition) {
        Util.notNull(body, "body"); //$NON-NLS-1$
        Util.notNull(condition, "condition"); //$NON-NLS-1$
        DoStatementImpl result = new DoStatementImpl();
        result.setBody(body);
        result.setCondition(condition);
        return result;
    }

    @Override
    public DocBlock newDocBlock(
            String tag,
            List<? extends DocElement> elements) {
        return this.newDocBlock0(
            tag,
            elements
        );
    }

    private DocBlockImpl newDocBlock0(
            String tag,
            List<? extends DocElement> elements) {
        Util.notNull(tag, "tag"); //$NON-NLS-1$
        Util.notNull(elements, "elements"); //$NON-NLS-1$
        Util.notContainNull(elements, "elements"); //$NON-NLS-1$
        DocBlockImpl result = new DocBlockImpl();
        result.setTag(tag);
        result.setElements(elements);
        return result;
    }

    @Override
    public DocField newDocField(
            Type type,
            SimpleName name) {
        return this.newDocField0(
            type,
            name
        );
    }

    private DocFieldImpl newDocField0(
            Type type,
            SimpleName name) {
        Util.notNull(name, "name"); //$NON-NLS-1$
        DocFieldImpl result = new DocFieldImpl();
        result.setType(type);
        result.setName(name);
        return result;
    }

    @Override
    public DocMethod newDocMethod(
            Type type,
            SimpleName name,
            List<? extends DocMethodParameter> formalParameters) {
        return this.newDocMethod0(
            type,
            name,
            formalParameters
        );
    }

    private DocMethodImpl newDocMethod0(
            Type type,
            SimpleName name,
            List<? extends DocMethodParameter> formalParameters) {
        Util.notNull(name, "name"); //$NON-NLS-1$
        Util.notNull(formalParameters, "formalParameters"); //$NON-NLS-1$
        Util.notContainNull(formalParameters, "formalParameters"); //$NON-NLS-1$
        DocMethodImpl result = new DocMethodImpl();
        result.setType(type);
        result.setName(name);
        result.setFormalParameters(formalParameters);
        return result;
    }

    @Override
    public DocMethodParameter newDocMethodParameter(
            Type type,
            SimpleName name,
            boolean variableArity) {
        return this.newDocMethodParameter0(
            type,
            name,
            variableArity
        );
    }

    private DocMethodParameterImpl newDocMethodParameter0(
            Type type,
            SimpleName name,
            boolean variableArity) {
        Util.notNull(type, "type"); //$NON-NLS-1$
        DocMethodParameterImpl result = new DocMethodParameterImpl();
        result.setType(type);
        result.setName(name);
        result.setVariableArity(variableArity);
        return result;
    }

    @Override
    public DocText newDocText(
            String string) {
        return this.newDocText0(
            string
        );
    }

    private DocTextImpl newDocText0(
            String string) {
        Util.notNull(string, "string"); //$NON-NLS-1$
        DocTextImpl result = new DocTextImpl();
        result.setString(string);
        return result;
    }

    @Override
    public EmptyStatement newEmptyStatement(
            ) {
        return this.newEmptyStatement0(

        );
    }

    private EmptyStatementImpl newEmptyStatement0(
            ) {
        EmptyStatementImpl result = new EmptyStatementImpl();
        return result;
    }

    @Override
    public EnhancedForStatement newEnhancedForStatement(
            FormalParameterDeclaration parameter,
            Expression expression,
            Statement body) {
        return this.newEnhancedForStatement0(
            parameter,
            expression,
            body
        );
    }

    private EnhancedForStatementImpl newEnhancedForStatement0(
            FormalParameterDeclaration parameter,
            Expression expression,
            Statement body) {
        Util.notNull(parameter, "parameter"); //$NON-NLS-1$
        Util.notNull(expression, "expression"); //$NON-NLS-1$
        Util.notNull(body, "body"); //$NON-NLS-1$
        EnhancedForStatementImpl result = new EnhancedForStatementImpl();
        result.setParameter(parameter);
        result.setExpression(expression);
        result.setBody(body);
        return result;
    }

    @Override
    public EnumConstantDeclaration newEnumConstantDeclaration(
            Javadoc javadoc,
            SimpleName name,
            Expression...arguments) {
        Util.notNull(arguments, "arguments"); //$NON-NLS-1$
        return this.newEnumConstantDeclaration0(
            javadoc,
            Collections.emptyList(),
            name,
            Arrays.asList(arguments),
            null
        );
    }

    @Override
    public EnumConstantDeclaration newEnumConstantDeclaration(
            Javadoc javadoc,
            List<? extends Attribute> modifiers,
            SimpleName name,
            List<? extends Expression> arguments,
            ClassBody body) {
        return this.newEnumConstantDeclaration0(
            javadoc,
            modifiers,
            name,
            arguments,
            body
        );
    }

    private EnumConstantDeclarationImpl newEnumConstantDeclaration0(
            Javadoc javadoc,
            List<? extends Attribute> modifiers,
            SimpleName name,
            List<? extends Expression> arguments,
            ClassBody body) {
        Util.notNull(modifiers, "modifiers"); //$NON-NLS-1$
        Util.notContainNull(modifiers, "modifiers"); //$NON-NLS-1$
        Util.notNull(name, "name"); //$NON-NLS-1$
        Util.notNull(arguments, "arguments"); //$NON-NLS-1$
        Util.notContainNull(arguments, "arguments"); //$NON-NLS-1$
        EnumConstantDeclarationImpl result = new EnumConstantDeclarationImpl();
        result.setJavadoc(javadoc);
        result.setModifiers(modifiers);
        result.setName(name);
        result.setArguments(arguments);
        result.setBody(body);
        return result;
    }

    @Override
    public EnumDeclaration newEnumDeclaration(
            Javadoc javadoc,
            List<? extends Attribute> modifiers,
            SimpleName name,
            List<? extends EnumConstantDeclaration> constantDeclarations,
            TypeBodyDeclaration...bodyDeclarations) {
        Util.notNull(bodyDeclarations, "bodyDeclarations"); //$NON-NLS-1$
        return this.newEnumDeclaration0(
            javadoc,
            modifiers,
            name,
            Collections.emptyList(),
            constantDeclarations,
            Arrays.asList(bodyDeclarations)
        );
    }

    @Override
    public EnumDeclaration newEnumDeclaration(
            Javadoc javadoc,
            List<? extends Attribute> modifiers,
            SimpleName name,
            List<? extends Type> superInterfaceTypes,
            List<? extends EnumConstantDeclaration> constantDeclarations,
            List<? extends TypeBodyDeclaration> bodyDeclarations) {
        return this.newEnumDeclaration0(
            javadoc,
            modifiers,
            name,
            superInterfaceTypes,
            constantDeclarations,
            bodyDeclarations
        );
    }

    private EnumDeclarationImpl newEnumDeclaration0(
            Javadoc javadoc,
            List<? extends Attribute> modifiers,
            SimpleName name,
            List<? extends Type> superInterfaceTypes,
            List<? extends EnumConstantDeclaration> constantDeclarations,
            List<? extends TypeBodyDeclaration> bodyDeclarations) {
        Util.notNull(modifiers, "modifiers"); //$NON-NLS-1$
        Util.notContainNull(modifiers, "modifiers"); //$NON-NLS-1$
        Util.notNull(name, "name"); //$NON-NLS-1$
        Util.notNull(superInterfaceTypes, "superInterfaceTypes"); //$NON-NLS-1$
        Util.notContainNull(superInterfaceTypes, "superInterfaceTypes"); //$NON-NLS-1$
        Util.notNull(constantDeclarations, "constantDeclarations"); //$NON-NLS-1$
        Util.notContainNull(constantDeclarations, "constantDeclarations"); //$NON-NLS-1$
        Util.notNull(bodyDeclarations, "bodyDeclarations"); //$NON-NLS-1$
        Util.notContainNull(bodyDeclarations, "bodyDeclarations"); //$NON-NLS-1$
        EnumDeclarationImpl result = new EnumDeclarationImpl();
        result.setJavadoc(javadoc);
        result.setModifiers(modifiers);
        result.setName(name);
        result.setSuperInterfaceTypes(superInterfaceTypes);
        result.setConstantDeclarations(constantDeclarations);
        result.setBodyDeclarations(bodyDeclarations);
        return result;
    }

    @Override
    public ExpressionStatement newExpressionStatement(
            Expression expression) {
        return this.newExpressionStatement0(
            expression
        );
    }

    private ExpressionStatementImpl newExpressionStatement0(
            Expression expression) {
        Util.notNull(expression, "expression"); //$NON-NLS-1$
        ExpressionStatementImpl result = new ExpressionStatementImpl();
        result.setExpression(expression);
        return result;
    }

    @Override
    public FieldAccessExpression newFieldAccessExpression(
            Expression qualifier,
            SimpleName name) {
        return this.newFieldAccessExpression0(
            qualifier,
            name
        );
    }

    private FieldAccessExpressionImpl newFieldAccessExpression0(
            Expression qualifier,
            SimpleName name) {
        Util.notNull(qualifier, "qualifier"); //$NON-NLS-1$
        Util.notNull(name, "name"); //$NON-NLS-1$
        FieldAccessExpressionImpl result = new FieldAccessExpressionImpl();
        result.setQualifier(parenthesize(qualifier, ExpressionPriority.PRIMARY));
        result.setName(name);
        return result;
    }

    @Override
    public FieldDeclaration newFieldDeclaration(
            Javadoc javadoc,
            List<? extends Attribute> modifiers,
            Type type,
            SimpleName name,
            Expression initializer) {
        return this.newFieldDeclaration0(
            javadoc,
            modifiers,
            type,
            Collections.singletonList(newVariableDeclarator(
                name,
                0,
                initializer))
        );
    }

    @Override
    public FieldDeclaration newFieldDeclaration(
            Javadoc javadoc,
            List<? extends Attribute> modifiers,
            Type type,
            List<? extends VariableDeclarator> variableDeclarators) {
        return this.newFieldDeclaration0(
            javadoc,
            modifiers,
            type,
            variableDeclarators
        );
    }

    private FieldDeclarationImpl newFieldDeclaration0(
            Javadoc javadoc,
            List<? extends Attribute> modifiers,
            Type type,
            List<? extends VariableDeclarator> variableDeclarators) {
        Util.notNull(modifiers, "modifiers"); //$NON-NLS-1$
        Util.notContainNull(modifiers, "modifiers"); //$NON-NLS-1$
        Util.notNull(type, "type"); //$NON-NLS-1$
        Util.notNull(variableDeclarators, "variableDeclarators"); //$NON-NLS-1$
        Util.notContainNull(variableDeclarators, "variableDeclarators"); //$NON-NLS-1$
        Util.notEmpty(variableDeclarators, "variableDeclarators"); //$NON-NLS-1$
        FieldDeclarationImpl result = new FieldDeclarationImpl();
        result.setJavadoc(javadoc);
        result.setModifiers(modifiers);
        result.setType(type);
        result.setVariableDeclarators(variableDeclarators);
        return result;
    }

    @Override
    public ForStatement newForStatement(
            ForInitializer initialization,
            Expression condition,
            StatementExpressionList update,
            Statement body) {
        return this.newForStatement0(
            initialization,
            condition,
            update,
            body
        );
    }

    private ForStatementImpl newForStatement0(
            ForInitializer initialization,
            Expression condition,
            StatementExpressionList update,
            Statement body) {
        Util.notNull(body, "body"); //$NON-NLS-1$
        ForStatementImpl result = new ForStatementImpl();
        result.setInitialization(initialization);
        result.setCondition(condition);
        result.setUpdate(update);
        result.setBody(body);
        return result;
    }

    @Override
    public FormalParameterDeclaration newFormalParameterDeclaration(
            Type type,
            SimpleName name) {
        return this.newFormalParameterDeclaration0(
            Collections.emptyList(),
            type,
            false,
            name,
            0
        );
    }

    @Override
    public FormalParameterDeclaration newFormalParameterDeclaration(
            List<? extends Attribute> modifiers,
            Type type,
            boolean variableArity,
            SimpleName name,
            int extraDimensions) {
        return this.newFormalParameterDeclaration0(
            modifiers,
            type,
            variableArity,
            name,
            extraDimensions
        );
    }

    private FormalParameterDeclarationImpl newFormalParameterDeclaration0(
            List<? extends Attribute> modifiers,
            Type type,
            boolean variableArity,
            SimpleName name,
            int extraDimensions) {
        Util.notNull(modifiers, "modifiers"); //$NON-NLS-1$
        Util.notContainNull(modifiers, "modifiers"); //$NON-NLS-1$
        Util.notNull(type, "type"); //$NON-NLS-1$
        Util.notNull(name, "name"); //$NON-NLS-1$
        FormalParameterDeclarationImpl result = new FormalParameterDeclarationImpl();
        result.setModifiers(modifiers);
        result.setType(type);
        result.setVariableArity(variableArity);
        result.setName(name);
        result.setExtraDimensions(extraDimensions);
        return result;
    }

    @Override
    public IfStatement newIfStatement(
            Expression condition,
            Statement thenStatement) {
        return this.newIfStatement0(
            condition,
            thenStatement,
            null
        );
    }

    @Override
    public IfStatement newIfStatement(
            Expression condition,
            Statement thenStatement,
            Statement elseStatement) {
        return this.newIfStatement0(
            condition,
            thenStatement,
            elseStatement
        );
    }

    private IfStatementImpl newIfStatement0(
            Expression condition,
            Statement thenStatement,
            Statement elseStatement) {
        Util.notNull(condition, "condition"); //$NON-NLS-1$
        Util.notNull(thenStatement, "thenStatement"); //$NON-NLS-1$
        IfStatementImpl result = new IfStatementImpl();
        result.setCondition(condition);
        result.setThenStatement(thenStatement);
        result.setElseStatement(elseStatement);
        return result;
    }

    @Override
    public ImportDeclaration newImportDeclaration(
            ImportKind importKind,
            Name name) {
        return this.newImportDeclaration0(
            importKind,
            name
        );
    }

    private ImportDeclarationImpl newImportDeclaration0(
            ImportKind importKind,
            Name name) {
        Util.notNull(importKind, "importKind"); //$NON-NLS-1$
        Util.notNull(name, "name"); //$NON-NLS-1$
        ImportDeclarationImpl result = new ImportDeclarationImpl();
        result.setImportKind(importKind);
        result.setName(name);
        return result;
    }

    @Override
    public InfixExpression newInfixExpression(
            Expression leftOperand,
            InfixOperator operator,
            Expression rightOperand) {
        return this.newInfixExpression0(
            leftOperand,
            operator,
            rightOperand
        );
    }

    private InfixExpressionImpl newInfixExpression0(
            Expression leftOperand,
            InfixOperator operator,
            Expression rightOperand) {
        Util.notNull(leftOperand, "leftOperand"); //$NON-NLS-1$
        Util.notNull(operator, "operator"); //$NON-NLS-1$
        Util.notNull(rightOperand, "rightOperand"); //$NON-NLS-1$
        InfixExpressionImpl result = new InfixExpressionImpl();
        result.setLeftOperand(parenthesize(leftOperand, ExpressionPriority.valueOf(operator)));
        result.setOperator(operator);
        result.setRightOperand(parenthesizeRight(rightOperand, ExpressionPriority.valueOf(operator)));
        return result;
    }

    @Override
    public InitializerDeclaration newInitializerDeclaration(
            List<? extends Statement> statements) {
        Util.notNull(statements, "statements"); //$NON-NLS-1$
        return this.newInitializerDeclaration0(
            null,
            Collections.emptyList(),
            newBlock(statements)
        );
    }

    @Override
    public InitializerDeclaration newInitializerDeclaration(
            Javadoc javadoc,
            List<? extends Attribute> modifiers,
            Block body) {
        return this.newInitializerDeclaration0(
            javadoc,
            modifiers,
            body
        );
    }

    private InitializerDeclarationImpl newInitializerDeclaration0(
            Javadoc javadoc,
            List<? extends Attribute> modifiers,
            Block body) {
        Util.notNull(modifiers, "modifiers"); //$NON-NLS-1$
        Util.notContainNull(modifiers, "modifiers"); //$NON-NLS-1$
        Util.notNull(body, "body"); //$NON-NLS-1$
        InitializerDeclarationImpl result = new InitializerDeclarationImpl();
        result.setJavadoc(javadoc);
        result.setModifiers(modifiers);
        result.setBody(body);
        return result;
    }

    @Override
    public InstanceofExpression newInstanceofExpression(
            Expression expression,
            Type type) {
        return this.newInstanceofExpression0(
            expression,
            type
        );
    }

    private InstanceofExpressionImpl newInstanceofExpression0(
            Expression expression,
            Type type) {
        Util.notNull(expression, "expression"); //$NON-NLS-1$
        Util.notNull(type, "type"); //$NON-NLS-1$
        InstanceofExpressionImpl result = new InstanceofExpressionImpl();
        result.setExpression(parenthesize(expression, ExpressionPriority.RELATIONAL));
        result.setType(type);
        return result;
    }

    @Override
    public InterfaceDeclaration newInterfaceDeclaration(
            Javadoc javadoc,
            List<? extends Attribute> modifiers,
            SimpleName name,
            List<? extends Type> superInterfaceTypes,
            List<? extends TypeBodyDeclaration> bodyDeclarations) {
        return this.newInterfaceDeclaration0(
            javadoc,
            modifiers,
            name,
            Collections.emptyList(),
            superInterfaceTypes,
            bodyDeclarations
        );
    }

    @Override
    public InterfaceDeclaration newInterfaceDeclaration(
            Javadoc javadoc,
            List<? extends Attribute> modifiers,
            SimpleName name,
            List<? extends TypeParameterDeclaration> typeParameters,
            List<? extends Type> superInterfaceTypes,
            List<? extends TypeBodyDeclaration> bodyDeclarations) {
        return this.newInterfaceDeclaration0(
            javadoc,
            modifiers,
            name,
            typeParameters,
            superInterfaceTypes,
            bodyDeclarations
        );
    }

    private InterfaceDeclarationImpl newInterfaceDeclaration0(
            Javadoc javadoc,
            List<? extends Attribute> modifiers,
            SimpleName name,
            List<? extends TypeParameterDeclaration> typeParameters,
            List<? extends Type> superInterfaceTypes,
            List<? extends TypeBodyDeclaration> bodyDeclarations) {
        Util.notNull(modifiers, "modifiers"); //$NON-NLS-1$
        Util.notContainNull(modifiers, "modifiers"); //$NON-NLS-1$
        Util.notNull(name, "name"); //$NON-NLS-1$
        Util.notNull(typeParameters, "typeParameters"); //$NON-NLS-1$
        Util.notContainNull(typeParameters, "typeParameters"); //$NON-NLS-1$
        Util.notNull(superInterfaceTypes, "superInterfaceTypes"); //$NON-NLS-1$
        Util.notContainNull(superInterfaceTypes, "superInterfaceTypes"); //$NON-NLS-1$
        Util.notNull(bodyDeclarations, "bodyDeclarations"); //$NON-NLS-1$
        Util.notContainNull(bodyDeclarations, "bodyDeclarations"); //$NON-NLS-1$
        InterfaceDeclarationImpl result = new InterfaceDeclarationImpl();
        result.setJavadoc(javadoc);
        result.setModifiers(modifiers);
        result.setName(name);
        result.setTypeParameters(typeParameters);
        result.setSuperInterfaceTypes(superInterfaceTypes);
        result.setBodyDeclarations(bodyDeclarations);
        return result;
    }

    @Override
    public Javadoc newJavadoc(
            List<? extends DocBlock> blocks) {
        return this.newJavadoc0(
            blocks
        );
    }

    private JavadocImpl newJavadoc0(
            List<? extends DocBlock> blocks) {
        Util.notNull(blocks, "blocks"); //$NON-NLS-1$
        Util.notContainNull(blocks, "blocks"); //$NON-NLS-1$
        JavadocImpl result = new JavadocImpl();
        result.setBlocks(blocks);
        return result;
    }

    @Override
    public LabeledStatement newLabeledStatement(
            SimpleName label,
            Statement body) {
        return this.newLabeledStatement0(
            label,
            body
        );
    }

    private LabeledStatementImpl newLabeledStatement0(
            SimpleName label,
            Statement body) {
        Util.notNull(label, "label"); //$NON-NLS-1$
        Util.notNull(body, "body"); //$NON-NLS-1$
        LabeledStatementImpl result = new LabeledStatementImpl();
        result.setLabel(label);
        result.setBody(body);
        return result;
    }

    @Override
    public LineComment newLineComment(
            String string) {
        return this.newLineComment0(
            string
        );
    }

    private LineCommentImpl newLineComment0(
            String string) {
        Util.notNull(string, "string"); //$NON-NLS-1$
        LineCommentImpl result = new LineCommentImpl();
        result.setString(string);
        return result;
    }

    @Override
    public Literal newLiteral(
            String token) {
        return this.newLiteral0(
            token
        );
    }

    private LiteralImpl newLiteral0(
            String token) {
        Util.notNull(token, "token"); //$NON-NLS-1$
        LiteralImpl result = new LiteralImpl();
        result.setToken(token);
        return result;
    }

    @Override
    public LocalClassDeclaration newLocalClassDeclaration(
            ClassDeclaration declaration) {
        return this.newLocalClassDeclaration0(
            declaration
        );
    }

    private LocalClassDeclarationImpl newLocalClassDeclaration0(
            ClassDeclaration declaration) {
        Util.notNull(declaration, "declaration"); //$NON-NLS-1$
        LocalClassDeclarationImpl result = new LocalClassDeclarationImpl();
        result.setDeclaration(declaration);
        return result;
    }

    @Override
    public LocalVariableDeclaration newLocalVariableDeclaration(
            Type type,
            SimpleName name,
            Expression initializer) {
        return this.newLocalVariableDeclaration0(
            Collections.emptyList(),
            type,
            Collections.singletonList(newVariableDeclarator(
                name,
                0,
                initializer))
        );
    }

    @Override
    public LocalVariableDeclaration newLocalVariableDeclaration(
            List<? extends Attribute> modifiers,
            Type type,
            List<? extends VariableDeclarator> variableDeclarators) {
        return this.newLocalVariableDeclaration0(
            modifiers,
            type,
            variableDeclarators
        );
    }

    private LocalVariableDeclarationImpl newLocalVariableDeclaration0(
            List<? extends Attribute> modifiers,
            Type type,
            List<? extends VariableDeclarator> variableDeclarators) {
        Util.notNull(modifiers, "modifiers"); //$NON-NLS-1$
        Util.notContainNull(modifiers, "modifiers"); //$NON-NLS-1$
        Util.notNull(type, "type"); //$NON-NLS-1$
        Util.notNull(variableDeclarators, "variableDeclarators"); //$NON-NLS-1$
        Util.notContainNull(variableDeclarators, "variableDeclarators"); //$NON-NLS-1$
        Util.notEmpty(variableDeclarators, "variableDeclarators"); //$NON-NLS-1$
        LocalVariableDeclarationImpl result = new LocalVariableDeclarationImpl();
        result.setModifiers(modifiers);
        result.setType(type);
        result.setVariableDeclarators(variableDeclarators);
        return result;
    }

    @Override
    public MarkerAnnotation newMarkerAnnotation(
            NamedType type) {
        return this.newMarkerAnnotation0(
            type
        );
    }

    private MarkerAnnotationImpl newMarkerAnnotation0(
            NamedType type) {
        Util.notNull(type, "type"); //$NON-NLS-1$
        MarkerAnnotationImpl result = new MarkerAnnotationImpl();
        result.setType(type);
        return result;
    }

    @Override
    public MethodDeclaration newMethodDeclaration(
            Javadoc javadoc,
            List<? extends Attribute> modifiers,
            Type returnType,
            SimpleName name,
            List<? extends FormalParameterDeclaration> formalParameters,
            List<? extends Statement> statements) {
        Util.notNull(statements, "statements"); //$NON-NLS-1$
        return this.newMethodDeclaration0(
            javadoc,
            modifiers,
            Collections.emptyList(),
            returnType,
            name,
            formalParameters,
            0,
            Collections.emptyList(),
            newBlock(statements)
        );
    }

    @Override
    public MethodDeclaration newMethodDeclaration(
            Javadoc javadoc,
            List<? extends Attribute> modifiers,
            List<? extends TypeParameterDeclaration> typeParameters,
            Type returnType,
            SimpleName name,
            List<? extends FormalParameterDeclaration> formalParameters,
            int extraDimensions,
            List<? extends Type> exceptionTypes,
            Block body) {
        return this.newMethodDeclaration0(
            javadoc,
            modifiers,
            typeParameters,
            returnType,
            name,
            formalParameters,
            extraDimensions,
            exceptionTypes,
            body
        );
    }

    private MethodDeclarationImpl newMethodDeclaration0(
            Javadoc javadoc,
            List<? extends Attribute> modifiers,
            List<? extends TypeParameterDeclaration> typeParameters,
            Type returnType,
            SimpleName name,
            List<? extends FormalParameterDeclaration> formalParameters,
            int extraDimensions,
            List<? extends Type> exceptionTypes,
            Block body) {
        Util.notNull(modifiers, "modifiers"); //$NON-NLS-1$
        Util.notContainNull(modifiers, "modifiers"); //$NON-NLS-1$
        Util.notNull(typeParameters, "typeParameters"); //$NON-NLS-1$
        Util.notContainNull(typeParameters, "typeParameters"); //$NON-NLS-1$
        Util.notNull(returnType, "returnType"); //$NON-NLS-1$
        Util.notNull(name, "name"); //$NON-NLS-1$
        Util.notNull(formalParameters, "formalParameters"); //$NON-NLS-1$
        Util.notContainNull(formalParameters, "formalParameters"); //$NON-NLS-1$
        Util.notNull(exceptionTypes, "exceptionTypes"); //$NON-NLS-1$
        Util.notContainNull(exceptionTypes, "exceptionTypes"); //$NON-NLS-1$
        MethodDeclarationImpl result = new MethodDeclarationImpl();
        result.setJavadoc(javadoc);
        result.setModifiers(modifiers);
        result.setTypeParameters(typeParameters);
        result.setReturnType(returnType);
        result.setName(name);
        result.setFormalParameters(formalParameters);
        result.setExtraDimensions(extraDimensions);
        result.setExceptionTypes(exceptionTypes);
        result.setBody(body);
        return result;
    }

    @Override
    public MethodInvocationExpression newMethodInvocationExpression(
            Expression qualifier,
            SimpleName name,
            Expression...arguments) {
        Util.notNull(arguments, "arguments"); //$NON-NLS-1$
        return this.newMethodInvocationExpression0(
            qualifier,
            Collections.emptyList(),
            name,
            Arrays.asList(arguments)
        );
    }

    @Override
    public MethodInvocationExpression newMethodInvocationExpression(
            Expression qualifier,
            SimpleName name,
            List<? extends Expression> arguments) {
        return this.newMethodInvocationExpression0(
            qualifier,
            Collections.emptyList(),
            name,
            arguments
        );
    }

    @Override
    public MethodInvocationExpression newMethodInvocationExpression(
            Expression qualifier,
            List<? extends Type> typeArguments,
            SimpleName name,
            List<? extends Expression> arguments) {
        return this.newMethodInvocationExpression0(
            qualifier,
            typeArguments,
            name,
            arguments
        );
    }

    private MethodInvocationExpressionImpl newMethodInvocationExpression0(
            Expression qualifier,
            List<? extends Type> typeArguments,
            SimpleName name,
            List<? extends Expression> arguments) {
        Util.notNull(typeArguments, "typeArguments"); //$NON-NLS-1$
        Util.notContainNull(typeArguments, "typeArguments"); //$NON-NLS-1$
        Util.notNull(name, "name"); //$NON-NLS-1$
        Util.notNull(arguments, "arguments"); //$NON-NLS-1$
        Util.notContainNull(arguments, "arguments"); //$NON-NLS-1$
        MethodInvocationExpressionImpl result = new MethodInvocationExpressionImpl();
        result.setQualifier(parenthesize(qualifier, ExpressionPriority.PRIMARY));
        result.setTypeArguments(typeArguments);
        result.setName(name);
        result.setArguments(arguments);
        return result;
    }

    @Override
    public Modifier newModifier(
            ModifierKind modifierKind) {
        return this.newModifier0(
            modifierKind
        );
    }

    private ModifierImpl newModifier0(
            ModifierKind modifierKind) {
        Util.notNull(modifierKind, "modifierKind"); //$NON-NLS-1$
        ModifierImpl result = new ModifierImpl();
        result.setModifierKind(modifierKind);
        return result;
    }

    @Override
    public NamedType newNamedType(
            Name name) {
        return this.newNamedType0(
            name
        );
    }

    private NamedTypeImpl newNamedType0(
            Name name) {
        Util.notNull(name, "name"); //$NON-NLS-1$
        NamedTypeImpl result = new NamedTypeImpl();
        result.setName(name);
        return result;
    }

    @Override
    public NormalAnnotation newNormalAnnotation(
            NamedType type,
            List<? extends AnnotationElement> elements) {
        return this.newNormalAnnotation0(
            type,
            elements
        );
    }

    private NormalAnnotationImpl newNormalAnnotation0(
            NamedType type,
            List<? extends AnnotationElement> elements) {
        Util.notNull(type, "type"); //$NON-NLS-1$
        Util.notNull(elements, "elements"); //$NON-NLS-1$
        Util.notContainNull(elements, "elements"); //$NON-NLS-1$
        NormalAnnotationImpl result = new NormalAnnotationImpl();
        result.setType(type);
        result.setElements(elements);
        return result;
    }

    @Override
    public PackageDeclaration newPackageDeclaration(Name name) {
        return this.newPackageDeclaration0(
            null,
            Collections.emptyList(),
            name
        );
    }

    @Override
    public PackageDeclaration newPackageDeclaration(
            Javadoc javadoc,
            List<? extends Annotation> annotations,
            Name name) {
        return this.newPackageDeclaration0(
            javadoc,
            annotations,
            name
        );
    }

    private PackageDeclarationImpl newPackageDeclaration0(
            Javadoc javadoc,
            List<? extends Annotation> annotations,
            Name name) {
        Util.notNull(annotations, "annotations"); //$NON-NLS-1$
        Util.notContainNull(annotations, "annotations"); //$NON-NLS-1$
        Util.notNull(name, "name"); //$NON-NLS-1$
        PackageDeclarationImpl result = new PackageDeclarationImpl();
        result.setJavadoc(javadoc);
        result.setAnnotations(annotations);
        result.setName(name);
        return result;
    }

    @Override
    public ParameterizedType newParameterizedType(
            Type type,
            Type...typeArguments) {
        Util.notNull(typeArguments, "typeArguments"); //$NON-NLS-1$
        return this.newParameterizedType0(
            type,
            Arrays.asList(typeArguments)
        );
    }

    @Override
    public ParameterizedType newParameterizedType(
            Type type,
            List<? extends Type> typeArguments) {
        return this.newParameterizedType0(
            type,
            typeArguments
        );
    }

    private ParameterizedTypeImpl newParameterizedType0(
            Type type,
            List<? extends Type> typeArguments) {
        Util.notNull(type, "type"); //$NON-NLS-1$
        Util.notNull(typeArguments, "typeArguments"); //$NON-NLS-1$
        Util.notContainNull(typeArguments, "typeArguments"); //$NON-NLS-1$
        ParameterizedTypeImpl result = new ParameterizedTypeImpl();
        result.setType(type);
        result.setTypeArguments(typeArguments);
        return result;
    }

    @Override
    public ParenthesizedExpression newParenthesizedExpression(
            Expression expression) {
        return this.newParenthesizedExpression0(
            expression
        );
    }

    private ParenthesizedExpressionImpl newParenthesizedExpression0(
            Expression expression) {
        Util.notNull(expression, "expression"); //$NON-NLS-1$
        ParenthesizedExpressionImpl result = new ParenthesizedExpressionImpl();
        result.setExpression(expression);
        return result;
    }

    @Override
    public PostfixExpression newPostfixExpression(
            Expression operand,
            PostfixOperator operator) {
        return this.newPostfixExpression0(
            operand,
            operator
        );
    }

    private PostfixExpressionImpl newPostfixExpression0(
            Expression operand,
            PostfixOperator operator) {
        Util.notNull(operand, "operand"); //$NON-NLS-1$
        Util.notNull(operator, "operator"); //$NON-NLS-1$
        PostfixExpressionImpl result = new PostfixExpressionImpl();
        result.setOperand(parenthesize(operand, ExpressionPriority.UNARY));
        result.setOperator(operator);
        return result;
    }

    @Override
    public QualifiedName newQualifiedName(
            Name qualifier,
            SimpleName simpleName) {
        return this.newQualifiedName0(
            qualifier,
            simpleName
        );
    }

    private QualifiedNameImpl newQualifiedName0(
            Name qualifier,
            SimpleName simpleName) {
        Util.notNull(qualifier, "qualifier"); //$NON-NLS-1$
        Util.notNull(simpleName, "simpleName"); //$NON-NLS-1$
        QualifiedNameImpl result = new QualifiedNameImpl();
        result.setQualifier(qualifier);
        result.setSimpleName(simpleName);
        return result;
    }

    @Override
    public QualifiedType newQualifiedType(
            Type qualifier,
            SimpleName simpleName) {
        return this.newQualifiedType0(
            qualifier,
            simpleName
        );
    }

    private QualifiedTypeImpl newQualifiedType0(
            Type qualifier,
            SimpleName simpleName) {
        Util.notNull(qualifier, "qualifier"); //$NON-NLS-1$
        Util.notNull(simpleName, "simpleName"); //$NON-NLS-1$
        QualifiedTypeImpl result = new QualifiedTypeImpl();
        result.setQualifier(qualifier);
        result.setSimpleName(simpleName);
        return result;
    }

    @Override
    public ReturnStatement newReturnStatement() {
        return this.newReturnStatement0(
            null
        );
    }

    @Override
    public ReturnStatement newReturnStatement(
            Expression expression) {
        return this.newReturnStatement0(
            expression
        );
    }

    private ReturnStatementImpl newReturnStatement0(
            Expression expression) {
        ReturnStatementImpl result = new ReturnStatementImpl();
        result.setExpression(expression);
        return result;
    }

    @Override
    public SimpleName newSimpleName(
            String string) {
        return this.newSimpleName0(
            string
        );
    }

    private SimpleNameImpl newSimpleName0(
            String string) {
        Util.notNull(string, "string"); //$NON-NLS-1$
        SimpleNameImpl result = new SimpleNameImpl();
        result.setToken(string);
        return result;
    }

    @Override
    public SingleElementAnnotation newSingleElementAnnotation(
            NamedType type,
            Expression expression) {
        return this.newSingleElementAnnotation0(
            type,
            expression
        );
    }

    private SingleElementAnnotationImpl newSingleElementAnnotation0(
            NamedType type,
            Expression expression) {
        Util.notNull(type, "type"); //$NON-NLS-1$
        Util.notNull(expression, "expression"); //$NON-NLS-1$
        SingleElementAnnotationImpl result = new SingleElementAnnotationImpl();
        result.setType(type);
        result.setExpression(expression);
        return result;
    }

    @Override
    public StatementExpressionList newStatementExpressionList(
            Expression...expressions) {
        Util.notNull(expressions, "expressions"); //$NON-NLS-1$
        return this.newStatementExpressionList0(
            Arrays.asList(expressions)
        );
    }

    @Override
    public StatementExpressionList newStatementExpressionList(
            List<? extends Expression> expressions) {
        return this.newStatementExpressionList0(
            expressions
        );
    }

    private StatementExpressionListImpl newStatementExpressionList0(
            List<? extends Expression> expressions) {
        Util.notNull(expressions, "expressions"); //$NON-NLS-1$
        Util.notContainNull(expressions, "expressions"); //$NON-NLS-1$
        Util.notEmpty(expressions, "expressions"); //$NON-NLS-1$
        StatementExpressionListImpl result = new StatementExpressionListImpl();
        result.setExpressions(expressions);
        return result;
    }

    @Override
    public Super newSuper() {
        return this.newSuper0(
            null
        );
    }

    @Override
    public Super newSuper(
            NamedType qualifier) {
        return this.newSuper0(
            qualifier
        );
    }

    private SuperImpl newSuper0(
            NamedType qualifier) {
        SuperImpl result = new SuperImpl();
        result.setQualifier(qualifier);
        return result;
    }

    @Override
    public SuperConstructorInvocation newSuperConstructorInvocation(
            Expression...arguments) {
        Util.notNull(arguments, "arguments"); //$NON-NLS-1$
        return this.newSuperConstructorInvocation0(
            null,
            Collections.emptyList(),
            Arrays.asList(arguments)
        );
    }

    @Override
    public SuperConstructorInvocation newSuperConstructorInvocation(
            List<? extends Expression> arguments) {
        return this.newSuperConstructorInvocation0(
            null,
            Collections.emptyList(),
            arguments
        );
    }

    @Override
    public SuperConstructorInvocation newSuperConstructorInvocation(
            Expression qualifier,
            List<? extends Type> typeArguments,
            List<? extends Expression> arguments) {
        return this.newSuperConstructorInvocation0(
            qualifier,
            typeArguments,
            arguments
        );
    }

    private SuperConstructorInvocationImpl newSuperConstructorInvocation0(
            Expression qualifier,
            List<? extends Type> typeArguments,
            List<? extends Expression> arguments) {
        Util.notNull(typeArguments, "typeArguments"); //$NON-NLS-1$
        Util.notContainNull(typeArguments, "typeArguments"); //$NON-NLS-1$
        Util.notNull(arguments, "arguments"); //$NON-NLS-1$
        Util.notContainNull(arguments, "arguments"); //$NON-NLS-1$
        SuperConstructorInvocationImpl result = new SuperConstructorInvocationImpl();
        result.setQualifier(parenthesize(qualifier, ExpressionPriority.PRIMARY));
        result.setTypeArguments(typeArguments);
        result.setArguments(arguments);
        return result;
    }

    @Override
    public SwitchCaseLabel newSwitchCaseLabel(
            Expression expression) {
        return this.newSwitchCaseLabel0(
            expression
        );
    }

    private SwitchCaseLabelImpl newSwitchCaseLabel0(
            Expression expression) {
        Util.notNull(expression, "expression"); //$NON-NLS-1$
        SwitchCaseLabelImpl result = new SwitchCaseLabelImpl();
        result.setExpression(expression);
        return result;
    }

    @Override
    public SwitchDefaultLabel newSwitchDefaultLabel(
            ) {
        return this.newSwitchDefaultLabel0(

        );
    }

    private SwitchDefaultLabelImpl newSwitchDefaultLabel0(
            ) {
        SwitchDefaultLabelImpl result = new SwitchDefaultLabelImpl();
        return result;
    }

    @Override
    public SwitchStatement newSwitchStatement(
            Expression expression,
            List<? extends Statement> statements) {
        return this.newSwitchStatement0(
            expression,
            statements
        );
    }

    private SwitchStatementImpl newSwitchStatement0(
            Expression expression,
            List<? extends Statement> statements) {
        Util.notNull(expression, "expression"); //$NON-NLS-1$
        Util.notNull(statements, "statements"); //$NON-NLS-1$
        Util.notContainNull(statements, "statements"); //$NON-NLS-1$
        SwitchStatementImpl result = new SwitchStatementImpl();
        result.setExpression(expression);
        result.setStatements(statements);
        return result;
    }

    @Override
    public SynchronizedStatement newSynchronizedStatement(
            Expression expression,
            Block body) {
        return this.newSynchronizedStatement0(
            expression,
            body
        );
    }

    private SynchronizedStatementImpl newSynchronizedStatement0(
            Expression expression,
            Block body) {
        Util.notNull(expression, "expression"); //$NON-NLS-1$
        Util.notNull(body, "body"); //$NON-NLS-1$
        SynchronizedStatementImpl result = new SynchronizedStatementImpl();
        result.setExpression(expression);
        result.setBody(body);
        return result;
    }

    @Override
    public This newThis() {
        return this.newThis0(
            null
        );
    }

    @Override
    public This newThis(
            NamedType qualifier) {
        return this.newThis0(
            qualifier
        );
    }

    private ThisImpl newThis0(
            NamedType qualifier) {
        ThisImpl result = new ThisImpl();
        result.setQualifier(qualifier);
        return result;
    }

    @Override
    public ThrowStatement newThrowStatement(
            Expression expression) {
        return this.newThrowStatement0(
            expression
        );
    }

    private ThrowStatementImpl newThrowStatement0(
            Expression expression) {
        Util.notNull(expression, "expression"); //$NON-NLS-1$
        ThrowStatementImpl result = new ThrowStatementImpl();
        result.setExpression(expression);
        return result;
    }

    @Override
    public TryStatement newTryStatement(
            Block tryBlock,
            List<? extends CatchClause> catchClauses,
            Block finallyBlock) {
        return this.newTryStatement0(
            tryBlock,
            catchClauses,
            finallyBlock
        );
    }

    private TryStatementImpl newTryStatement0(
            Block tryBlock,
            List<? extends CatchClause> catchClauses,
            Block finallyBlock) {
        Util.notNull(tryBlock, "tryBlock"); //$NON-NLS-1$
        Util.notNull(catchClauses, "catchClauses"); //$NON-NLS-1$
        Util.notContainNull(catchClauses, "catchClauses"); //$NON-NLS-1$
        TryStatementImpl result = new TryStatementImpl();
        result.setTryBlock(tryBlock);
        result.setCatchClauses(catchClauses);
        result.setFinallyBlock(finallyBlock);
        return result;
    }

    @Override
    public TypeParameterDeclaration newTypeParameterDeclaration(
            SimpleName name,
            Type...typeBounds) {
        Util.notNull(typeBounds, "typeBounds"); //$NON-NLS-1$
        return this.newTypeParameterDeclaration0(
            name,
            Arrays.asList(typeBounds)
        );
    }

    @Override
    public TypeParameterDeclaration newTypeParameterDeclaration(
            SimpleName name,
            List<? extends Type> typeBounds) {
        return this.newTypeParameterDeclaration0(
            name,
            typeBounds
        );
    }

    private TypeParameterDeclarationImpl newTypeParameterDeclaration0(
            SimpleName name,
            List<? extends Type> typeBounds) {
        Util.notNull(name, "name"); //$NON-NLS-1$
        Util.notNull(typeBounds, "typeBounds"); //$NON-NLS-1$
        Util.notContainNull(typeBounds, "typeBounds"); //$NON-NLS-1$
        TypeParameterDeclarationImpl result = new TypeParameterDeclarationImpl();
        result.setName(name);
        result.setTypeBounds(typeBounds);
        return result;
    }

    @Override
    public UnaryExpression newUnaryExpression(
            UnaryOperator operator,
            Expression operand) {
        return this.newUnaryExpression0(
            operator,
            operand
        );
    }

    private UnaryExpressionImpl newUnaryExpression0(
            UnaryOperator operator,
            Expression operand) {
        Util.notNull(operator, "operator"); //$NON-NLS-1$
        Util.notNull(operand, "operand"); //$NON-NLS-1$
        UnaryExpressionImpl result = new UnaryExpressionImpl();
        result.setOperator(operator);
        result.setOperand(parenthesize(operand, ExpressionPriority.UNARY));
        return result;
    }

    @Override
    public VariableDeclarator newVariableDeclarator(
            SimpleName name,
            Expression initializer) {
        return this.newVariableDeclarator0(
            name,
            0,
            initializer
        );
    }

    @Override
    public VariableDeclarator newVariableDeclarator(
            SimpleName name,
            int extraDimensions,
            Expression initializer) {
        return this.newVariableDeclarator0(
            name,
            extraDimensions,
            initializer
        );
    }

    private VariableDeclaratorImpl newVariableDeclarator0(
            SimpleName name,
            int extraDimensions,
            Expression initializer) {
        Util.notNull(name, "name"); //$NON-NLS-1$
        VariableDeclaratorImpl result = new VariableDeclaratorImpl();
        result.setName(name);
        result.setExtraDimensions(extraDimensions);
        result.setInitializer(initializer);
        return result;
    }

    @Override
    public WhileStatement newWhileStatement(
            Expression condition,
            Statement body) {
        return this.newWhileStatement0(
            condition,
            body
        );
    }

    private WhileStatementImpl newWhileStatement0(
            Expression condition,
            Statement body) {
        Util.notNull(condition, "condition"); //$NON-NLS-1$
        Util.notNull(body, "body"); //$NON-NLS-1$
        WhileStatementImpl result = new WhileStatementImpl();
        result.setCondition(condition);
        result.setBody(body);
        return result;
    }

    @Override
    public Wildcard newWildcard() {
        return this.newWildcard0(
            WildcardBoundKind.UNBOUNDED,
            null
        );
    }

    @Override
    public Wildcard newWildcard(
            WildcardBoundKind boundKind,
            Type typeBound) {
        return this.newWildcard0(
            boundKind,
            typeBound
        );
    }

    private WildcardImpl newWildcard0(
            WildcardBoundKind boundKind,
            Type typeBound) {
        Util.notNull(boundKind, "boundKind"); //$NON-NLS-1$
        WildcardImpl result = new WildcardImpl();
        result.setBoundKind(boundKind);
        result.setTypeBound(typeBound);
        return result;
    }

    private Expression parenthesize(Expression expression, ExpressionPriority context) {
        if (expression == null) {
            return null;
        }
        ExpressionPriority priority = ExpressionPriority.valueOf(expression);
        if (ExpressionPriority.isParenthesesRequired(context, false, priority)) {
            return newParenthesizedExpression0(expression);
        } else {
            return expression;
        }
    }

    private Expression parenthesizeRight(Expression expression, ExpressionPriority context) {
        if (expression == null) {
            return null;
        }
        ExpressionPriority priority = ExpressionPriority.valueOf(expression);
        if (ExpressionPriority.isParenthesesRequired(context, true, priority)) {
            return newParenthesizedExpression0(expression);
        } else {
            return expression;
        }
    }
}
//CHECKSTYLE:ON