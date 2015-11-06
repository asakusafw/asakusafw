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

import java.util.List;

import javax.annotation.Generated;

/**
 * A factory for providing {@link Model} objects.
 */
@Generated("com.asakusafw.utils.java.model.syntax.ModelFactory")
public interface ModelFactory {

    /**
     * Returns a new {@link AlternateConstructorInvocation} object.
     * @param arguments the arguments
     * @return the created object
     * @throws IllegalArgumentException if {@code arguments} was {@code null}
     */
    AlternateConstructorInvocation newAlternateConstructorInvocation(
            Expression... arguments
    );

    /**
     * Returns a new {@link AlternateConstructorInvocation} object.
     * @param arguments the arguments
     * @return the created object
     * @throws IllegalArgumentException if {@code arguments} was {@code null}
     */
    AlternateConstructorInvocation newAlternateConstructorInvocation(
            List<? extends Expression> arguments
    );

    /**
     * Returns a new {@link AlternateConstructorInvocation} object.
     * @param typeArguments the type arguments
     * @param arguments the arguments
     * @return the created object
     * @throws IllegalArgumentException if {@code typeArguments} was {@code null}
     * @throws IllegalArgumentException if {@code arguments} was {@code null}
     */
    AlternateConstructorInvocation newAlternateConstructorInvocation(
            List<? extends Type> typeArguments,
            List<? extends Expression> arguments
    );

    /**
     * Returns a new {@link AnnotationDeclaration} object.
     * @param javadoc the documentation comments, or {@code null} if there is no documentation comments
     * @param modifiers the modifiers and annotations
     * @param name the simple type name
     * @param bodyDeclarations the member declarations
     * @return the created object
     * @throws IllegalArgumentException if {@code modifiers} was {@code null}
     * @throws IllegalArgumentException if {@code name} was {@code null}
     * @throws IllegalArgumentException if {@code bodyDeclarations} was {@code null}
     */
    AnnotationDeclaration newAnnotationDeclaration(
            Javadoc javadoc,
            List<? extends Attribute> modifiers,
            SimpleName name,
            List<? extends TypeBodyDeclaration> bodyDeclarations
    );

    /**
     * Returns a new {@link AnnotationElement} object.
     * @param name annotation element name
     * @param expression the expression value
     * @return the created object
     * @throws IllegalArgumentException if {@code name} was {@code null}
     * @throws IllegalArgumentException if {@code expression} was {@code null}
     */
    AnnotationElement newAnnotationElement(
            SimpleName name,
            Expression expression
    );

    /**
     * Returns a new {@link AnnotationElementDeclaration} object.
     * @param javadoc the documentation comments, or {@code null} if there is no documentation comments
     * @param modifiers the modifiers and annotations
     * @param type annotation element type
     * @param name annotation element name
     * @param defaultExpression the default expression value, or {@code null} if there is no default value
     * @return the created object
     * @throws IllegalArgumentException if {@code modifiers} was {@code null}
     * @throws IllegalArgumentException if {@code type} was {@code null}
     * @throws IllegalArgumentException if {@code name} was {@code null}
     */
    AnnotationElementDeclaration newAnnotationElementDeclaration(
            Javadoc javadoc,
            List<? extends Attribute> modifiers,
            Type type,
            SimpleName name,
            Expression defaultExpression
    );

    /**
     * Returns a new {@link ArrayAccessExpression} object.
     * @param array the array expression
     * @param index the index expression
     * @return the created object
     * @throws IllegalArgumentException if {@code array} was {@code null}
     * @throws IllegalArgumentException if {@code index} was {@code null}
     */
    ArrayAccessExpression newArrayAccessExpression(
            Expression array,
            Expression index
    );

    /**
     * Returns a new {@link ArrayCreationExpression} object.
     * @param type the array type
     * @param arrayInitializer the array initializer, or {@code null} if there is no array initializer
     * @return the created object
     * @throws IllegalArgumentException if {@code type} was {@code null}
     */
    ArrayCreationExpression newArrayCreationExpression(
            ArrayType type,
            ArrayInitializer arrayInitializer
    );

    /**
     * Returns a new {@link ArrayCreationExpression} object.
     * @param type the array type
     * @param dimensionExpressions the dimension expressions
     * @param arrayInitializer the array initializer, or {@code null} if there is no array initializer
     * @return the created object
     * @throws IllegalArgumentException if {@code type} was {@code null}
     * @throws IllegalArgumentException if {@code dimensionExpressions} was {@code null}
     */
    ArrayCreationExpression newArrayCreationExpression(
            ArrayType type,
            List<? extends Expression> dimensionExpressions,
            ArrayInitializer arrayInitializer
    );

    /**
     * Returns a new {@link ArrayInitializer} object.
     * @param elements the element expressions
     * @return the created object
     * @throws IllegalArgumentException if {@code elements} was {@code null}
     */
    ArrayInitializer newArrayInitializer(
            Expression... elements
    );

    /**
     * Returns a new {@link ArrayInitializer} object.
     * @param elements the element expressions
     * @return the created object
     * @throws IllegalArgumentException if {@code elements} was {@code null}
     */
    ArrayInitializer newArrayInitializer(
            List<? extends Expression> elements
    );

    /**
     * Returns a new {@link ArrayType} object.
     * @param componentType the element type
     * @return the created object
     * @throws IllegalArgumentException if {@code componentType} was {@code null}
     */
    ArrayType newArrayType(
            Type componentType
    );

    /**
     * Returns a new {@link AssertStatement} object.
     * @param expression the assertion expression
     * @return the created object
     * @throws IllegalArgumentException if {@code expression} was {@code null}
     */
    AssertStatement newAssertStatement(
            Expression expression
    );

    /**
     * Returns a new {@link AssertStatement} object.
     * @param expression the assertion expression
     * @param message the message expression, or {@code null} if there is no message expression
     * @return the created object
     * @throws IllegalArgumentException if {@code expression} was {@code null}
     */
    AssertStatement newAssertStatement(
            Expression expression,
            Expression message
    );

    /**
     * Returns a new {@link AssignmentExpression} object.
     * @param leftHandSide the left hand side expression
     * @param rightHandSide the right hand side expression
     * @return the created object
     * @throws IllegalArgumentException if {@code leftHandSide} was {@code null}
     * @throws IllegalArgumentException if {@code rightHandSide} was {@code null}
     */
    AssignmentExpression newAssignmentExpression(
            Expression leftHandSide,
            Expression rightHandSide
    );

    /**
     * Returns a new {@link AssignmentExpression} object.
     * @param leftHandSide the left hand side expression
     * @param operator the simple assignment operator, or an infix operator for compound assignment
     * @param rightHandSide the right hand side expression
     * @return the created object
     * @throws IllegalArgumentException if {@code leftHandSide} was {@code null}
     * @throws IllegalArgumentException if {@code operator} was {@code null}
     * @throws IllegalArgumentException if {@code rightHandSide} was {@code null}
     */
    AssignmentExpression newAssignmentExpression(
            Expression leftHandSide,
            InfixOperator operator,
            Expression rightHandSide
    );

    /**
     * Returns a new {@link BasicType} object.
     * @param typeKind the type kind
     * @return the created object
     * @throws IllegalArgumentException if {@code typeKind} was {@code null}
     */
    BasicType newBasicType(
            BasicTypeKind typeKind
    );

    /**
     * Returns a new {@link Block} object.
     * @param statements the statements
     * @return the created object
     * @throws IllegalArgumentException if {@code statements} was {@code null}
     */
    Block newBlock(
            Statement... statements
    );

    /**
     * Returns a new {@link Block} object.
     * @param statements the statements
     * @return the created object
     * @throws IllegalArgumentException if {@code statements} was {@code null}
     */
    Block newBlock(
            List<? extends Statement> statements
    );

    /**
     * Returns a new {@link BlockComment} object.
     * @param string the comment text
     * @return the created object
     * @throws IllegalArgumentException if {@code string} was {@code null}
     * @throws IllegalArgumentException if {@code string} was empty
     */
    BlockComment newBlockComment(
            String string
    );

    /**
     * Returns a new {@link BreakStatement} object.
     * @return the created object
     */
    BreakStatement newBreakStatement();

    /**
     * Returns a new {@link BreakStatement} object.
     * @param target the target label, or {@code null} if there is no target labels
     * @return the created object
     */
    BreakStatement newBreakStatement(
            SimpleName target
    );

    /**
     * Returns a new {@link CastExpression} object.
     * @param type the target type
     * @param expression the term
     * @return the created object
     * @throws IllegalArgumentException if {@code type} was {@code null}
     * @throws IllegalArgumentException if {@code expression} was {@code null}
     */
    CastExpression newCastExpression(
            Type type,
            Expression expression
    );

    /**
     * Returns a new {@link CatchClause} object.
     * @param parameter the expression parameter
     * @param body the {@code catch} block
     * @return the created object
     * @throws IllegalArgumentException if {@code parameter} was {@code null}
     * @throws IllegalArgumentException if {@code body} was {@code null}
     */
    CatchClause newCatchClause(
            FormalParameterDeclaration parameter,
            Block body
    );

    /**
     * Returns a new {@link ClassBody} object.
     * @param bodyDeclarations the member declarations
     * @return the created object
     * @throws IllegalArgumentException if {@code bodyDeclarations} was {@code null}
     */
    ClassBody newClassBody(
            List<? extends TypeBodyDeclaration> bodyDeclarations
    );

    /**
     * Returns a new {@link ClassDeclaration} object.
     * @param javadoc the documentation comments, or {@code null} if there is no documentation comments
     * @param modifiers the modifiers and annotations
     * @param name the simple type name
     * @param superClass the super class, or {@code null} if there is no explicit super class
     * @param superInterfaceTypes the super interface types
     * @param bodyDeclarations the member declarations
     * @return the created object
     * @throws IllegalArgumentException if {@code modifiers} was {@code null}
     * @throws IllegalArgumentException if {@code name} was {@code null}
     * @throws IllegalArgumentException if {@code superInterfaceTypes} was {@code null}
     * @throws IllegalArgumentException if {@code bodyDeclarations} was {@code null}
     */
    ClassDeclaration newClassDeclaration(
            Javadoc javadoc,
            List<? extends Attribute> modifiers,
            SimpleName name,
            Type superClass,
            List<? extends Type> superInterfaceTypes,
            List<? extends TypeBodyDeclaration> bodyDeclarations
    );

    /**
     * Returns a new {@link ClassDeclaration} object.
     * @param javadoc the documentation comments, or {@code null} if there is no documentation comments
     * @param modifiers the modifiers and annotations
     * @param name the simple type name
     * @param typeParameters the type parameters
     * @param superClass the super class, or {@code null} if there is no explicit super class
     * @param superInterfaceTypes the super interface types
     * @param bodyDeclarations the member declarations
     * @return the created object
     * @throws IllegalArgumentException if {@code modifiers} was {@code null}
     * @throws IllegalArgumentException if {@code name} was {@code null}
     * @throws IllegalArgumentException if {@code typeParameters} was {@code null}
     * @throws IllegalArgumentException if {@code superInterfaceTypes} was {@code null}
     * @throws IllegalArgumentException if {@code bodyDeclarations} was {@code null}
     */
    ClassDeclaration newClassDeclaration(
            Javadoc javadoc,
            List<? extends Attribute> modifiers,
            SimpleName name,
            List<? extends TypeParameterDeclaration> typeParameters,
            Type superClass,
            List<? extends Type> superInterfaceTypes,
            List<? extends TypeBodyDeclaration> bodyDeclarations
    );

    /**
     * Returns a new {@link ClassInstanceCreationExpression} object.
     * @param type the target type
     * @param arguments the arguments
     * @return the created object
     * @throws IllegalArgumentException if {@code type} was {@code null}
     * @throws IllegalArgumentException if {@code arguments} was {@code null}
     */
    ClassInstanceCreationExpression newClassInstanceCreationExpression(
            Type type,
            Expression... arguments
    );

    /**
     * Returns a new {@link ClassInstanceCreationExpression} object.
     * @param type the target type
     * @param arguments the arguments
     * @return the created object
     * @throws IllegalArgumentException if {@code type} was {@code null}
     * @throws IllegalArgumentException if {@code arguments} was {@code null}
     */
    ClassInstanceCreationExpression newClassInstanceCreationExpression(
            Type type,
            List<? extends Expression> arguments
    );

    /**
     * Returns a new {@link ClassInstanceCreationExpression} object.
     * @param qualifier the qualifier expression, or {@code null} if there is no qualifiers
     * @param typeArguments the type arguments
     * @param type the target type
     * @param arguments the arguments
     * @param body the anonymous class body, or {@code null} if the target is not an anonymous class
     * @return the created object
     * @throws IllegalArgumentException if {@code typeArguments} was {@code null}
     * @throws IllegalArgumentException if {@code type} was {@code null}
     * @throws IllegalArgumentException if {@code arguments} was {@code null}
     */
    ClassInstanceCreationExpression newClassInstanceCreationExpression(
            Expression qualifier,
            List<? extends Type> typeArguments,
            Type type,
            List<? extends Expression> arguments,
            ClassBody body
    );

    /**
     * Returns a new {@link ClassLiteral} object.
     * @param type the target type
     * @return the created object
     * @throws IllegalArgumentException if {@code type} was {@code null}
     */
    ClassLiteral newClassLiteral(
            Type type
    );

    /**
     * Returns a new {@link CompilationUnit} object.
     * @param packageDeclaration the package declaration,
     *     or {@code null} if this compilation unit is on the default (unnamed) package
     * @param importDeclarations the import declarations
     * @param typeDeclarations the type declarations
     * @param comments the comments
     * @return the created object
     * @throws IllegalArgumentException if {@code importDeclarations} was {@code null}
     * @throws IllegalArgumentException if {@code typeDeclarations} was {@code null}
     * @throws IllegalArgumentException if {@code comments} was {@code null}
     */
    CompilationUnit newCompilationUnit(
            PackageDeclaration packageDeclaration,
            List<? extends ImportDeclaration> importDeclarations,
            List<? extends TypeDeclaration> typeDeclarations,
            List<? extends Comment> comments
    );

    /**
     * Returns a new {@link ConditionalExpression} object.
     * @param condition the condition expression
     * @param thenExpression the truth expression
     * @param elseExpression the false expression
     * @return the created object
     * @throws IllegalArgumentException if {@code condition} was {@code null}
     * @throws IllegalArgumentException if {@code thenExpression} was {@code null}
     * @throws IllegalArgumentException if {@code elseExpression} was {@code null}
     */
    ConditionalExpression newConditionalExpression(
            Expression condition,
            Expression thenExpression,
            Expression elseExpression
    );

    /**
     * Returns a new {@link ConstructorDeclaration} object.
     * @param javadoc the documentation comments, or {@code null} if there is no documentation comments
     * @param modifiers the modifiers and annotations
     * @param name the constructor name (as the simple name of the owner class)
     * @param formalParameters the formal parameters
     * @param statements the constructor body statements
     * @return the created object
     * @throws IllegalArgumentException if {@code modifiers} was {@code null}
     * @throws IllegalArgumentException if {@code name} was {@code null}
     * @throws IllegalArgumentException if {@code formalParameters} was {@code null}
     * @throws IllegalArgumentException if {@code statements} was {@code null}
     */
    ConstructorDeclaration newConstructorDeclaration(
            Javadoc javadoc,
            List<? extends Attribute> modifiers,
            SimpleName name,
            List<? extends FormalParameterDeclaration> formalParameters,
            List<? extends Statement> statements
    );

    /**
     * Returns a new {@link ConstructorDeclaration} object.
     * @param javadoc the documentation comments, or {@code null} if there is no documentation comments
     * @param modifiers the modifiers and annotations
     * @param typeParameters the type parameters
     * @param name the constructor name (as the simple name of the owner class)
     * @param formalParameters the formal parameters
     * @param exceptionTypes the exception types
     * @param body the constructor body
     * @return the created object
     * @throws IllegalArgumentException if {@code modifiers} was {@code null}
     * @throws IllegalArgumentException if {@code typeParameters} was {@code null}
     * @throws IllegalArgumentException if {@code name} was {@code null}
     * @throws IllegalArgumentException if {@code formalParameters} was {@code null}
     * @throws IllegalArgumentException if {@code exceptionTypes} was {@code null}
     * @throws IllegalArgumentException if {@code body} was {@code null}
     */
    ConstructorDeclaration newConstructorDeclaration(
            Javadoc javadoc,
            List<? extends Attribute> modifiers,
            List<? extends TypeParameterDeclaration> typeParameters,
            SimpleName name,
            List<? extends FormalParameterDeclaration> formalParameters,
            List<? extends Type> exceptionTypes,
            Block body
    );

    /**
     * Returns a new {@link ContinueStatement} object.
     * @return the created object
     */
    ContinueStatement newContinueStatement();

    /**
     * Returns a new {@link ContinueStatement} object.
     * @param target the target label, or {@code null} if there is no target labels
     * @return the created object
     */
    ContinueStatement newContinueStatement(
            SimpleName target
    );

    /**
     * Returns a new {@link DoStatement} object.
     * @param body the loop body
     * @param condition the condition expression
     * @return the created object
     * @throws IllegalArgumentException if {@code body} was {@code null}
     * @throws IllegalArgumentException if {@code condition} was {@code null}
     */
    DoStatement newDoStatement(
            Statement body,
            Expression condition
    );

    /**
     * Returns a new {@link DocBlock} object.
     * @param tag the block tag
     * @param elements the block elements
     * @return the created object
     * @throws IllegalArgumentException if {@code tag} was {@code null}
     * @throws IllegalArgumentException if {@code elements} was {@code null}
     */
    DocBlock newDocBlock(
            String tag,
            List<? extends DocElement> elements
    );

    /**
     * Returns a new {@link DocField} object.
     * @param type the field owner type, or {@code null} if it is not specified
     * @param name the field name
     * @return the created object
     * @throws IllegalArgumentException if {@code name} was {@code null}
     */
    DocField newDocField(
            Type type,
            SimpleName name
    );

    /**
     * Returns a new {@link DocMethod} object.
     * @param type the member owner type, or {@code null} if it is not specified
     * @param name the target method (or constructor) name
     * @param formalParameters the target method (or constructor) parameters
     * @return the created object
     * @throws IllegalArgumentException if {@code name} was {@code null}
     * @throws IllegalArgumentException if {@code formalParameters} was {@code null}
     */
    DocMethod newDocMethod(
            Type type,
            SimpleName name,
            List<? extends DocMethodParameter> formalParameters
    );

    /**
     * Returns a new {@link DocMethodParameter} object.
     * @param type the parameter type
     * @param name the parameter name, or {@code null} if it is not specified
     * @param variableArity {@code true} if this parameter is variable length, otherwise {@code false}
     * @return the created object
     * @throws IllegalArgumentException if {@code type} was {@code null}
     */
    DocMethodParameter newDocMethodParameter(
            Type type,
            SimpleName name,
            boolean variableArity
    );

    /**
     * Returns a new {@link DocText} object.
     * @param string the contents string
     * @return the created object
     * @throws IllegalArgumentException if {@code string} was {@code null}
     */
    DocText newDocText(
            String string
    );

    /**
     * Returns a new {@link EmptyStatement} object.
     * @return the created object
     */
    EmptyStatement newEmptyStatement(

    );

    /**
     * Returns a new {@link EnhancedForStatement} object.
     * @param parameter the loop parameter
     * @param expression the loop target
     * @param body the loop body
     * @return the created object
     * @throws IllegalArgumentException if {@code parameter} was {@code null}
     * @throws IllegalArgumentException if {@code expression} was {@code null}
     * @throws IllegalArgumentException if {@code body} was {@code null}
     */
    EnhancedForStatement newEnhancedForStatement(
            FormalParameterDeclaration parameter,
            Expression expression,
            Statement body
    );

    /**
     * Returns a new {@link EnumConstantDeclaration} object.
     * @param javadoc the documentation comments, or {@code null} if there is no documentation comments
     * @param name the enum constant name
     * @param arguments the constructor arguments
     * @return the created object
     * @throws IllegalArgumentException if {@code name} was {@code null}
     * @throws IllegalArgumentException if {@code arguments} was {@code null}
     */
    EnumConstantDeclaration newEnumConstantDeclaration(
            Javadoc javadoc,
            SimpleName name,
            Expression... arguments
    );

    /**
     * Returns a new {@link EnumConstantDeclaration} object.
     * @param javadoc the documentation comments, or {@code null} if there is no documentation comments
     * @param modifiers the modifiers and annotations
     * @param name the enum constant name
     * @param arguments the constructor arguments
     * @param body the class body, or {@code null} if there is no class body
     * @return the created object
     * @throws IllegalArgumentException if {@code modifiers} was {@code null}
     * @throws IllegalArgumentException if {@code name} was {@code null}
     * @throws IllegalArgumentException if {@code arguments} was {@code null}
     */
    EnumConstantDeclaration newEnumConstantDeclaration(
            Javadoc javadoc,
            List<? extends Attribute> modifiers,
            SimpleName name,
            List<? extends Expression> arguments,
            ClassBody body
    );

    /**
     * Returns a new {@link EnumDeclaration} object.
     * @param javadoc the documentation comments, or {@code null} if there is no documentation comments
     * @param modifiers the modifiers and annotations
     * @param name the simple type name
     * @param constantDeclarations the enum constant declarations
     * @param bodyDeclarations the member declarations
     * @return the created object
     * @throws IllegalArgumentException if {@code modifiers} was {@code null}
     * @throws IllegalArgumentException if {@code name} was {@code null}
     * @throws IllegalArgumentException if {@code constantDeclarations} was {@code null}
     * @throws IllegalArgumentException if {@code bodyDeclarations} was {@code null}
     */
    EnumDeclaration newEnumDeclaration(
            Javadoc javadoc,
            List<? extends Attribute> modifiers,
            SimpleName name,
            List<? extends EnumConstantDeclaration> constantDeclarations,
            TypeBodyDeclaration... bodyDeclarations
    );

    /**
     * Returns a new {@link EnumDeclaration} object.
     * @param javadoc the documentation comments, or {@code null} if there is no documentation comments
     * @param modifiers the modifiers and annotations
     * @param name the simple type name
     * @param superInterfaceTypes the super interface types
     * @param constantDeclarations the enum constant declarations
     * @param bodyDeclarations the member declarations
     * @return the created object
     * @throws IllegalArgumentException if {@code modifiers} was {@code null}
     * @throws IllegalArgumentException if {@code name} was {@code null}
     * @throws IllegalArgumentException if {@code superInterfaceTypes} was {@code null}
     * @throws IllegalArgumentException if {@code constantDeclarations} was {@code null}
     * @throws IllegalArgumentException if {@code bodyDeclarations} was {@code null}
     */
    EnumDeclaration newEnumDeclaration(
            Javadoc javadoc,
            List<? extends Attribute> modifiers,
            SimpleName name,
            List<? extends Type> superInterfaceTypes,
            List<? extends EnumConstantDeclaration> constantDeclarations,
            List<? extends TypeBodyDeclaration> bodyDeclarations
    );

    /**
     * Returns a new {@link ExpressionStatement} object.
     * @param expression the internal expression
     * @return the created object
     * @throws IllegalArgumentException if {@code expression} was {@code null}
     */
    ExpressionStatement newExpressionStatement(
            Expression expression
    );

    /**
     * Returns a new {@link FieldAccessExpression} object.
     * @param qualifier the qualifier expression
     * @param name the target field name
     * @return the created object
     * @throws IllegalArgumentException if {@code qualifier} was {@code null}
     * @throws IllegalArgumentException if {@code name} was {@code null}
     */
    FieldAccessExpression newFieldAccessExpression(
            Expression qualifier,
            SimpleName name
    );

    /**
     * Returns a new {@link FieldDeclaration} object.
     * @param javadoc the documentation comments, or {@code null} if there is no documentation comments
     * @param modifiers the modifiers and annotations
     * @param type the field type
     * @param name the field name
     * @param initializer the initializer expression, or {@code null} if it is not specified
     * @return the created object
     * @throws IllegalArgumentException if {@code modifiers} was {@code null}
     * @throws IllegalArgumentException if {@code type} was {@code null}
     * @throws IllegalArgumentException if {@code name} was {@code null}
     */
    FieldDeclaration newFieldDeclaration(
            Javadoc javadoc,
            List<? extends Attribute> modifiers,
            Type type,
            SimpleName name,
            Expression initializer
    );

    /**
     * Returns a new {@link FieldDeclaration} object.
     * @param javadoc the documentation comments, or {@code null} if there is no documentation comments
     * @param modifiers the modifiers and annotations
     * @param type the field type
     * @param variableDeclarators the variable declarators
     * @return the created object
     * @throws IllegalArgumentException if {@code modifiers} was {@code null}
     * @throws IllegalArgumentException if {@code type} was {@code null}
     * @throws IllegalArgumentException if {@code variableDeclarators} was {@code null}
     * @throws IllegalArgumentException if {@code variableDeclarators} was empty
     */
    FieldDeclaration newFieldDeclaration(
            Javadoc javadoc,
            List<? extends Attribute> modifiers,
            Type type,
            List<? extends VariableDeclarator> variableDeclarators
    );

    /**
     * Returns a new {@link ForStatement} object.
     * @param initialization the loop initialization part, or {@code null} if it is not specified
     * @param condition the loop condition, or {@code null} if it is not specified
     * @param update the loop update part, or {@code null} if it is not specified
     * @param body the loop body
     * @return the created object
     * @throws IllegalArgumentException if {@code body} was {@code null}
     */
    ForStatement newForStatement(
            ForInitializer initialization,
            Expression condition,
            StatementExpressionList update,
            Statement body
    );

    /**
     * Returns a new {@link FormalParameterDeclaration} object.
     * @param type the parameter type
     * @param name the parameter name
     * @return the created object
     * @throws IllegalArgumentException if {@code type} was {@code null}
     * @throws IllegalArgumentException if {@code name} was {@code null}
     */
    FormalParameterDeclaration newFormalParameterDeclaration(
            Type type,
            SimpleName name
    );

    /**
     * Returns a new {@link FormalParameterDeclaration} object.
     * @param modifiers the modifiers and annotations
     * @param type the parameter type
     * @param variableArity {@code true} if this parameter is variable arity, otherwise {@code false}
     * @param name the parameter name
     * @param extraDimensions the extra dimensions
     * @return the created object
     * @throws IllegalArgumentException if {@code modifiers} was {@code null}
     * @throws IllegalArgumentException if {@code type} was {@code null}
     * @throws IllegalArgumentException if {@code name} was {@code null}
     * @throws IllegalArgumentException if {@code extraDimensions} was negative value
     */
    FormalParameterDeclaration newFormalParameterDeclaration(
            List<? extends Attribute> modifiers,
            Type type,
            boolean variableArity,
            SimpleName name,
            int extraDimensions
    );

    /**
     * Returns a new {@link IfStatement} object.
     * @param condition the condition expression
     * @param thenStatement the truth statement
     * @return the created object
     * @throws IllegalArgumentException if {@code condition} was {@code null}
     * @throws IllegalArgumentException if {@code thenStatement} was {@code null}
     */
    IfStatement newIfStatement(
            Expression condition,
            Statement thenStatement
    );

    /**
     * Returns a new {@link IfStatement} object.
     * @param condition the condition expression
     * @param thenStatement the truth statement
     * @param elseStatement the false statement, or {@code null} if it is not specified
     * @return the created object
     * @throws IllegalArgumentException if {@code condition} was {@code null}
     * @throws IllegalArgumentException if {@code thenStatement} was {@code null}
     */
    IfStatement newIfStatement(
            Expression condition,
            Statement thenStatement,
            Statement elseStatement
    );

    /**
     * Returns a new {@link ImportDeclaration} object.
     * @param importKind the import kind
     * @param name the import target name
     * @return the created object
     * @throws IllegalArgumentException if {@code importKind} was {@code null}
     * @throws IllegalArgumentException if {@code name} was {@code null}
     */
    ImportDeclaration newImportDeclaration(
            ImportKind importKind,
            Name name
    );

    /**
     * Returns a new {@link InfixExpression} object.
     * @param leftOperand the left term
     * @param operator the infix operator
     * @param rightOperand the right term
     * @return the created object
     * @throws IllegalArgumentException if {@code leftOperand} was {@code null}
     * @throws IllegalArgumentException if {@code operator} was {@code null}
     * @throws IllegalArgumentException if {@code rightOperand} was {@code null}
     */
    InfixExpression newInfixExpression(
            Expression leftOperand,
            InfixOperator operator,
            Expression rightOperand
    );

    /**
     * Returns a new {@link InitializerDeclaration} object.
     * @param body the initializer body
     * @return the created object
     * @throws IllegalArgumentException if {@code body} was {@code null}
     */
    InitializerDeclaration newInitializerDeclaration(
            List<? extends Statement> body
    );

    /**
     * Returns a new {@link InitializerDeclaration} object.
     * @param javadoc the documentation comments, or {@code null} if there is no documentation comments
     * @param modifiers the modifiers and annotations
     * @param body the initializer body
     * @return the created object
     * @throws IllegalArgumentException if {@code modifiers} was {@code null}
     * @throws IllegalArgumentException if {@code body} was {@code null}
     */
    InitializerDeclaration newInitializerDeclaration(
            Javadoc javadoc,
            List<? extends Attribute> modifiers,
            Block body
    );

    /**
     * Returns a new {@link InstanceofExpression} object.
     * @param expression the left term
     * @param type the target type
     * @return the created object
     * @throws IllegalArgumentException if {@code expression} was {@code null}
     * @throws IllegalArgumentException if {@code type} was {@code null}
     */
    InstanceofExpression newInstanceofExpression(
            Expression expression,
            Type type
    );

    /**
     * Returns a new {@link InterfaceDeclaration} object.
     * @param javadoc the documentation comments, or {@code null} if there is no documentation comments
     * @param modifiers the modifiers and annotations
     * @param name the simple type name
     * @param superInterfaceTypes the super interface types
     * @param bodyDeclarations the member declarations
     * @return the created object
     * @throws IllegalArgumentException if {@code modifiers} was {@code null}
     * @throws IllegalArgumentException if {@code name} was {@code null}
     * @throws IllegalArgumentException if {@code superInterfaceTypes} was {@code null}
     * @throws IllegalArgumentException if {@code bodyDeclarations} was {@code null}
     */
    InterfaceDeclaration newInterfaceDeclaration(
            Javadoc javadoc,
            List<? extends Attribute> modifiers,
            SimpleName name,
            List<? extends Type> superInterfaceTypes,
            List<? extends TypeBodyDeclaration> bodyDeclarations
    );

    /**
     * Returns a new {@link InterfaceDeclaration} object.
     * @param javadoc the documentation comments, or {@code null} if there is no documentation comments
     * @param modifiers the modifiers and annotations
     * @param name the simple type name
     * @param typeParameters the type parameters
     * @param superInterfaceTypes the super interface types
     * @param bodyDeclarations the member declarations
     * @return the created object
     * @throws IllegalArgumentException if {@code modifiers} was {@code null}
     * @throws IllegalArgumentException if {@code name} was {@code null}
     * @throws IllegalArgumentException if {@code typeParameters} was {@code null}
     * @throws IllegalArgumentException if {@code superInterfaceTypes} was {@code null}
     * @throws IllegalArgumentException if {@code bodyDeclarations} was {@code null}
     */
    InterfaceDeclaration newInterfaceDeclaration(
            Javadoc javadoc,
            List<? extends Attribute> modifiers,
            SimpleName name,
            List<? extends TypeParameterDeclaration> typeParameters,
            List<? extends Type> superInterfaceTypes,
            List<? extends TypeBodyDeclaration> bodyDeclarations
    );

    /**
     * Returns a new {@link Javadoc} object.
     * @param blocks the doc blocks
     * @return the created object
     * @throws IllegalArgumentException if {@code blocks} was {@code null}
     */
    Javadoc newJavadoc(
            List<? extends DocBlock> blocks
    );

    /**
     * Returns a new {@link LabeledStatement} object.
     * @param label the label name
     * @param body the body statement
     * @return the created object
     * @throws IllegalArgumentException if {@code label} was {@code null}
     * @throws IllegalArgumentException if {@code body} was {@code null}
     */
    LabeledStatement newLabeledStatement(
            SimpleName label,
            Statement body
    );

    /**
     * Returns a new {@link LineComment} object.
     * @param string the comment text
     * @return the created object
     * @throws IllegalArgumentException if {@code string} was {@code null}
     * @throws IllegalArgumentException if {@code string} was empty
     */
    LineComment newLineComment(
            String string
    );

    /**
     * Returns a new {@link Literal} object.
     * @param token the literal token
     * @return the created object
     * @throws IllegalArgumentException if {@code token} was {@code null}
     * @throws IllegalArgumentException if {@code token} was empty
     */
    Literal newLiteral(
            String token
    );

    /**
     * Returns a new {@link LocalClassDeclaration} object.
     * @param declaration the target class declaration
     * @return the created object
     * @throws IllegalArgumentException if {@code declaration} was {@code null}
     */
    LocalClassDeclaration newLocalClassDeclaration(
            ClassDeclaration declaration
    );

    /**
     * Returns a new {@link LocalVariableDeclaration} object.
     * @param type the variable type
     * @param name the variable name
     * @param initializer the initializer expression, or {@code null} if it is not specified
     * @return the created object
     * @throws IllegalArgumentException if {@code type} was {@code null}
     * @throws IllegalArgumentException if {@code name} was {@code null}
     */
    LocalVariableDeclaration newLocalVariableDeclaration(
            Type type,
            SimpleName name,
            Expression initializer
    );

    /**
     * Returns a new {@link LocalVariableDeclaration} object.
     * @param modifiers the modifiers and annotations
     * @param type the variable type
     * @param variableDeclarators the variable declarators
     * @return the created object
     * @throws IllegalArgumentException if {@code modifiers} was {@code null}
     * @throws IllegalArgumentException if {@code type} was {@code null}
     * @throws IllegalArgumentException if {@code variableDeclarators} was {@code null}
     * @throws IllegalArgumentException if {@code variableDeclarators} was empty
     */
    LocalVariableDeclaration newLocalVariableDeclaration(
            List<? extends Attribute> modifiers,
            Type type,
            List<? extends VariableDeclarator> variableDeclarators
    );

    /**
     * Returns a new {@link MarkerAnnotation} object.
     * @param type the annotation type
     * @return the created object
     * @throws IllegalArgumentException if {@code type} was {@code null}
     */
    MarkerAnnotation newMarkerAnnotation(
            NamedType type
    );

    /**
     * Returns a new {@link MethodDeclaration} object.
     * @param javadoc the documentation comments, or {@code null} if there is no documentation comments
     * @param modifiers the modifiers and annotations
     * @param returnType the return type
     * @param name the method name
     * @param formalParameters the formal parameters
     * @param statements the method body, or {@code null} if the method does not have any method body
     * @return the created object
     * @throws IllegalArgumentException if {@code modifiers} was {@code null}
     * @throws IllegalArgumentException if {@code returnType} was {@code null}
     * @throws IllegalArgumentException if {@code name} was {@code null}
     * @throws IllegalArgumentException if {@code formalParameters} was {@code null}
     */
    MethodDeclaration newMethodDeclaration(
            Javadoc javadoc,
            List<? extends Attribute> modifiers,
            Type returnType,
            SimpleName name,
            List<? extends FormalParameterDeclaration> formalParameters,
            List<? extends Statement> statements
    );

    /**
     * Returns a new {@link MethodDeclaration} object.
     * @param javadoc the documentation comments, or {@code null} if there is no documentation comments
     * @param modifiers the modifiers and annotations
     * @param typeParameters the type parameters
     * @param returnType the return type
     * @param name the method name
     * @param formalParameters the formal parameters
     * @param extraDimensions the extra dimensions
     * @param exceptionTypes the exception types
     * @param body the method body, or {@code null} if the method does not have any method body
     * @return the created object
     * @throws IllegalArgumentException if {@code modifiers} was {@code null}
     * @throws IllegalArgumentException if {@code typeParameters} was {@code null}
     * @throws IllegalArgumentException if {@code returnType} was {@code null}
     * @throws IllegalArgumentException if {@code name} was {@code null}
     * @throws IllegalArgumentException if {@code formalParameters} was {@code null}
     * @throws IllegalArgumentException if {@code extraDimensions} was negative
     * @throws IllegalArgumentException if {@code exceptionTypes} was {@code null}
     */
    MethodDeclaration newMethodDeclaration(
            Javadoc javadoc,
            List<? extends Attribute> modifiers,
            List<? extends TypeParameterDeclaration> typeParameters,
            Type returnType,
            SimpleName name,
            List<? extends FormalParameterDeclaration> formalParameters,
            int extraDimensions,
            List<? extends Type> exceptionTypes,
            Block body
    );

    /**
     * Returns a new {@link MethodInvocationExpression} object.
     * @param qualifier the qualifier expression or type qualifier,
     *     or {@code null} if there is no qualifier expression (indicates a simple method invocation)
     * @param name the method name
     * @param arguments the arguments
     * @return the created object
     * @throws IllegalArgumentException if {@code name} was {@code null}
     * @throws IllegalArgumentException if {@code arguments} was {@code null}
     */
    MethodInvocationExpression newMethodInvocationExpression(
            Expression qualifier,
            SimpleName name,
            Expression... arguments
    );

    /**
     * Returns a new {@link MethodInvocationExpression} object.
     * @param qualifier the qualifier expression or type qualifier,
     *     or {@code null} if there is no qualifier expression (indicates a simple method invocation)
     * @param name the method name
     * @param arguments the arguments
     * @return the created object
     * @throws IllegalArgumentException if {@code name} was {@code null}
     * @throws IllegalArgumentException if {@code arguments} was {@code null}
     */
    MethodInvocationExpression newMethodInvocationExpression(
            Expression qualifier,
            SimpleName name,
            List<? extends Expression> arguments
    );

    /**
     * Returns a new {@link MethodInvocationExpression} object.
     * @param qualifier the qualifier expression or type qualifier,
     *     or {@code null} if there is no qualifier expression (indicates a simple method invocation)
     * @param typeArguments the type arguments
     * @param name the method name
     * @param arguments the arguments
     * @return the created object
     * @throws IllegalArgumentException if {@code typeArguments} was {@code null}
     * @throws IllegalArgumentException if {@code name} was {@code null}
     * @throws IllegalArgumentException if {@code arguments} was {@code null}
     */
    MethodInvocationExpression newMethodInvocationExpression(
            Expression qualifier,
            List<? extends Type> typeArguments,
            SimpleName name,
            List<? extends Expression> arguments
    );

    /**
     * Returns a new {@link Modifier} object.
     * @param modifierKind the modifier kind
     * @return the created object
     * @throws IllegalArgumentException if {@code modifierKind} was {@code null}
     */
    Modifier newModifier(
            ModifierKind modifierKind
    );

    /**
     * Returns a new {@link NamedType} object.
     * @param name the type name
     * @return the created object
     * @throws IllegalArgumentException if {@code name} was {@code null}
     */
    NamedType newNamedType(
            Name name
    );

    /**
     * Returns a new {@link NormalAnnotation} object.
     * @param type the annotation type
     * @param elements the annotation elements
     * @return the created object
     * @throws IllegalArgumentException if {@code type} was {@code null}
     * @throws IllegalArgumentException if {@code elements} was {@code null}
     */
    NormalAnnotation newNormalAnnotation(
            NamedType type,
            List<? extends AnnotationElement> elements
    );

    /**
     * Returns a new {@link PackageDeclaration} object.
     * @param name the package name
     * @return the created object
     * @throws IllegalArgumentException if {@code name} was {@code null}
     */
    PackageDeclaration newPackageDeclaration(
            Name name
    );

    /**
     * Returns a new {@link PackageDeclaration} object.
     * @param javadoc the documentation comments, or {@code null} if there is no documentation comments
     * @param annotations the annotations
     * @param name the package name
     * @return the created object
     * @throws IllegalArgumentException if {@code annotations} was {@code null}
     * @throws IllegalArgumentException if {@code name} was {@code null}
     */
    PackageDeclaration newPackageDeclaration(
            Javadoc javadoc,
            List<? extends Annotation> annotations,
            Name name
    );

    /**
     * Returns a new {@link ParameterizedType} object.
     * @param type the non parameterized type
     * @param typeArguments the type arguments
     * @return the created object
     * @throws IllegalArgumentException if {@code type} was {@code null}
     * @throws IllegalArgumentException if {@code typeArguments} was {@code null}
     * @throws IllegalArgumentException if {@code typeArguments} was empty
     */
    ParameterizedType newParameterizedType(
            Type type,
            Type... typeArguments
    );

    /**
     * Returns a new {@link ParameterizedType} object.
     * @param type the non parameterized type
     * @param typeArguments the type arguments
     * @return the created object
     * @throws IllegalArgumentException if {@code type} was {@code null}
     * @throws IllegalArgumentException if {@code typeArguments} was {@code null}
     * @throws IllegalArgumentException if {@code typeArguments} was empty
     */
    ParameterizedType newParameterizedType(
            Type type,
            List<? extends Type> typeArguments
    );

    /**
     * Returns a new {@link ParenthesizedExpression} object.
     * @param expression the internal expression
     * @return the created object
     * @throws IllegalArgumentException if {@code expression} was {@code null}
     */
    ParenthesizedExpression newParenthesizedExpression(
            Expression expression
    );

    /**
     * Returns a new {@link PostfixExpression} object.
     * @param operand the term expression
     * @param operator the postfix operator
     * @return the created object
     * @throws IllegalArgumentException if {@code operand} was {@code null}
     * @throws IllegalArgumentException if {@code operator} was {@code null}
     */
    PostfixExpression newPostfixExpression(
            Expression operand,
            PostfixOperator operator
    );

    /**
     * Returns a new {@link QualifiedName} object.
     * @param qualifier the qualifier
     * @param simpleName the simple name
     * @return the created object
     * @throws IllegalArgumentException if {@code qualifier} was {@code null}
     * @throws IllegalArgumentException if {@code simpleName} was {@code null}
     */
    QualifiedName newQualifiedName(
            Name qualifier,
            SimpleName simpleName
    );

    /**
     * Returns a new {@link QualifiedType} object.
     * @param qualifier the qualifier type
     * @param simpleName the simple name
     * @return the created object
     * @throws IllegalArgumentException if {@code qualifier} was {@code null}
     * @throws IllegalArgumentException if {@code simpleName} was {@code null}
     */
    QualifiedType newQualifiedType(
            Type qualifier,
            SimpleName simpleName
    );

    /**
     * Returns a new {@link ReturnStatement} object.
     * @return the created object
     */
    ReturnStatement newReturnStatement();

    /**
     * Returns a new {@link ReturnStatement} object.
     * @param expression the expression, or {@code null} if this does not return anything
     * @return the created object
     */
    ReturnStatement newReturnStatement(
            Expression expression
    );

    /**
     * Returns a new {@link SimpleName} object.
     * @param string the identifier
     * @return the created object
     * @throws IllegalArgumentException if {@code string} was {@code null}
     * @throws IllegalArgumentException if {@code string} was empty
     */
    SimpleName newSimpleName(
            String string
    );

    /**
     * Returns a new {@link SingleElementAnnotation} object.
     * @param type the annotation type
     * @param expression the {@code value} element expression
     * @return the created object
     * @throws IllegalArgumentException if {@code type} was {@code null}
     * @throws IllegalArgumentException if {@code expression} was {@code null}
     */
    SingleElementAnnotation newSingleElementAnnotation(
            NamedType type,
            Expression expression
    );

    /**
     * Returns a new {@link StatementExpressionList} object.
     * @param expressions the expression list
     * @return the created object
     * @throws IllegalArgumentException if {@code expressions} was {@code null}
     * @throws IllegalArgumentException if {@code expressions} was empty
     */
    StatementExpressionList newStatementExpressionList(
            Expression... expressions
    );

    /**
     * Returns a new {@link StatementExpressionList} object.
     * @param expressions the expression list
     * @return the created object
     * @throws IllegalArgumentException if {@code expressions} was {@code null}
     * @throws IllegalArgumentException if {@code expressions} was empty
     */
    StatementExpressionList newStatementExpressionList(
            List<? extends Expression> expressions
    );

    /**
     * Returns a new {@link Super} object.
     * @return the created object
     */
    Super newSuper();

    /**
     * Returns a new {@link Super} object.
     * @param qualifier the type qualifier, or {@code null} if there is no type qualifier
     * @return the created object
     */
    Super newSuper(
            NamedType qualifier
    );

    /**
     * Returns a new {@link SuperConstructorInvocation} object.
     * @param arguments the arguments
     * @return the created object
     * @throws IllegalArgumentException if {@code arguments} was {@code null}
     */
    SuperConstructorInvocation newSuperConstructorInvocation(
            Expression... arguments
    );

    /**
     * Returns a new {@link SuperConstructorInvocation} object.
     * @param arguments the arguments
     * @return the created object
     * @throws IllegalArgumentException if {@code arguments} was {@code null}
     */
    SuperConstructorInvocation newSuperConstructorInvocation(
            List<? extends Expression> arguments
    );

    /**
     * Returns a new {@link SuperConstructorInvocation} object.
     * @param qualifier the qualifier expression, or {@code null} if there is no qualifier
     * @param typeArguments the type arguments
     * @param arguments the arguments
     * @return the created object
     * @throws IllegalArgumentException if {@code typeArguments} was {@code null}
     * @throws IllegalArgumentException if {@code arguments} was {@code null}
     */
    SuperConstructorInvocation newSuperConstructorInvocation(
            Expression qualifier,
            List<? extends Type> typeArguments,
            List<? extends Expression> arguments
    );

    /**
     * Returns a new {@link SwitchCaseLabel} object.
     * @param expression the {@code case} label value
     * @return the created object
     * @throws IllegalArgumentException if {@code expression} was {@code null}
     */
    SwitchCaseLabel newSwitchCaseLabel(
            Expression expression
    );

    /**
     * Returns a new {@link SwitchDefaultLabel} object.
     * @return the created object
     */
    SwitchDefaultLabel newSwitchDefaultLabel(

    );

    /**
     * Returns a new {@link SwitchStatement} object.
     * @param expression the selector expression
     * @param statements the {@code switch} body
     * @return the created object
     * @throws IllegalArgumentException if {@code expression} was {@code null}
     * @throws IllegalArgumentException if {@code statements} was {@code null}
     */
    SwitchStatement newSwitchStatement(
            Expression expression,
            List<? extends Statement> statements
    );

    /**
     * Returns a new {@link SynchronizedStatement} object.
     * @param expression the lock expression
     * @param body the body block
     * @return the created object
     * @throws IllegalArgumentException if {@code expression} was {@code null}
     * @throws IllegalArgumentException if {@code body} was {@code null}
     */
    SynchronizedStatement newSynchronizedStatement(
            Expression expression,
            Block body
    );

    /**
     * Returns a new {@link This} object.
     * @return the created object
     */
    This newThis();

    /**
     * Returns a new {@link This} object.
     * @param qualifier the type qualifier, or {@code null} if there is no type qualifier
     * @return the created object
     */
    This newThis(
            NamedType qualifier
    );

    /**
     * Returns a new {@link ThrowStatement} object.
     * @param expression the expression to be thrown
     * @return the created object
     * @throws IllegalArgumentException if {@code expression} was {@code null}
     */
    ThrowStatement newThrowStatement(
            Expression expression
    );

    /**
     * Returns a new {@link TryStatement} object.
     * @param tryBlock the {@code try} clause
     * @param catchClauses the {@code catch} clauses
     * @param finallyBlock the {@code finally} clause, or {@code null} if it is not specified
     * @return the created object
     * @throws IllegalArgumentException if {@code tryBlock} was {@code null}
     * @throws IllegalArgumentException if {@code catchClauses} was {@code null}
     */
    TryStatement newTryStatement(
            Block tryBlock,
            List<? extends CatchClause> catchClauses,
            Block finallyBlock
    );

    /**
     * Returns a new {@link TypeParameterDeclaration} object.
     * @param name the type variable name
     * @param typeBounds the bound types
     * @return the created object
     * @throws IllegalArgumentException if {@code name} was {@code null}
     * @throws IllegalArgumentException if {@code typeBounds} was {@code null}
     */
    TypeParameterDeclaration newTypeParameterDeclaration(
            SimpleName name,
            Type... typeBounds
    );

    /**
     * Returns a new {@link TypeParameterDeclaration} object.
     * @param name the type variable name
     * @param typeBounds the bound types
     * @return the created object
     * @throws IllegalArgumentException if {@code name} was {@code null}
     * @throws IllegalArgumentException if {@code typeBounds} was {@code null}
     */
    TypeParameterDeclaration newTypeParameterDeclaration(
            SimpleName name,
            List<? extends Type> typeBounds
    );

    /**
     * Returns a new {@link UnaryExpression} object.
     * @param operator the unary operator
     * @param operand the term expression
     * @return the created object
     * @throws IllegalArgumentException if {@code operator} was {@code null}
     * @throws IllegalArgumentException if {@code operand} was {@code null}
     */
    UnaryExpression newUnaryExpression(
            UnaryOperator operator,
            Expression operand
    );

    /**
     * Returns a new {@link VariableDeclarator} object.
     * @param name the variable name
     * @param initializer the initializer expression, or {@code null} if it is not specified
     * @return the created object
     * @throws IllegalArgumentException if {@code name} was {@code null}
     */
    VariableDeclarator newVariableDeclarator(
            SimpleName name,
            Expression initializer
    );

    /**
     * Returns a new {@link VariableDeclarator} object.
     * @param name the variable name
     * @param extraDimensions the extra dimensions
     * @param initializer the initializer expression, or {@code null} if it is not specified
     * @return the created object
     * @throws IllegalArgumentException if {@code name} was {@code null}
     * @throws IllegalArgumentException if {@code extraDimensions} was negative
     */
    VariableDeclarator newVariableDeclarator(
            SimpleName name,
            int extraDimensions,
            Expression initializer
    );

    /**
     * Returns a new {@link WhileStatement} object.
     * @param condition the condition expression
     * @param body the loop body
     * @return the created object
     * @throws IllegalArgumentException if {@code condition} was {@code null}
     * @throws IllegalArgumentException if {@code body} was {@code null}
     */
    WhileStatement newWhileStatement(
            Expression condition,
            Statement body
    );

    /**
     * Returns a new {@link Wildcard} object.
     * @return the created object
     */
    Wildcard newWildcard();

    /**
     * Returns a new {@link Wildcard} object.
     * @param boundKind the bound type kind
     * @param typeBound the bound type, or {@code null} if there is no bound types
     * @return the created object
     * @throws IllegalArgumentException if {@code boundKind} was {@code null}
     */
    Wildcard newWildcard(
            WildcardBoundKind boundKind,
            Type typeBound
    );
}
