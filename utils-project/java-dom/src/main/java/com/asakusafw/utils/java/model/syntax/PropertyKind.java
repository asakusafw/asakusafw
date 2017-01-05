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
package com.asakusafw.utils.java.model.syntax;

/**
 * Represents a kind of properties in {@link Model}.
 * @since 0.1.0
 * @version 0.9.0
 */
public enum PropertyKind {

    /**
     * Represents {@link Annotation#getType()}.
     */
    ANNOTATION_TYPE(
        Annotation.class,
        "type" //$NON-NLS-1$
    ),

    /**
     * Represents {@link AnnotationElement#getName()}.
     */
    ANNOTATION_ELEMENT_NAME(
        AnnotationElement.class,
        "name" //$NON-NLS-1$
    ),

    /**
     * Represents {@link AnnotationElement#getExpression()}.
     */
    ANNOTATION_ELEMENT_EXPRESSION(
        AnnotationElement.class,
        "expression" //$NON-NLS-1$
    ),

    /**
     * Represents {@link AnnotationElementDeclaration#getType()}.
     */
    ANNOTATION_ELEMENT_DECLARATION_TYPE(
        AnnotationElementDeclaration.class,
        "type" //$NON-NLS-1$
    ),

    /**
     * Represents {@link AnnotationElementDeclaration#getName()}.
     */
    ANNOTATION_ELEMENT_DECLARATION_NAME(
        AnnotationElementDeclaration.class,
        "name" //$NON-NLS-1$
    ),

    /**
     * Represents {@link AnnotationElementDeclaration#getDefaultExpression()}.
     */
    ANNOTATION_ELEMENT_DECLARATION_DEFAULT_EXPRESSION(
        AnnotationElementDeclaration.class,
        "defaultExpression" //$NON-NLS-1$
    ),

    /**
     * Represents {@link ArrayAccessExpression#getArray()}.
     */
    ARRAY_ACCESS_EXPRESSION_ARRAY(
        ArrayAccessExpression.class,
        "array" //$NON-NLS-1$
    ),

    /**
     * Represents {@link ArrayAccessExpression#getIndex()}.
     */
    ARRAY_ACCESS_EXPRESSION_INDEX(
        ArrayAccessExpression.class,
        "index" //$NON-NLS-1$
    ),

    /**
     * Represents {@link ArrayCreationExpression#getType()}.
     */
    ARRAY_CREATION_EXPRESSION_TYPE(
        ArrayCreationExpression.class,
        "type" //$NON-NLS-1$
    ),

    /**
     * Represents {@link ArrayCreationExpression#getDimensionExpressions()}.
     */
    ARRAY_CREATION_EXPRESSION_DIMENSION_EXPRESSIONS(
        ArrayCreationExpression.class,
        "dimensionExpressions" //$NON-NLS-1$
    ),

    /**
     * Represents {@link ArrayCreationExpression#getArrayInitializer()}.
     */
    ARRAY_CREATION_EXPRESSION_ARRAY_INITIALIZER(
        ArrayCreationExpression.class,
        "arrayInitializer" //$NON-NLS-1$
    ),

    /**
     * Represents {@link ArrayInitializer#getElements()}.
     */
    ARRAY_INITIALIZER_ELEMENTS(
        ArrayInitializer.class,
        "elements" //$NON-NLS-1$
    ),

    /**
     * Represents {@link ArrayType#getComponentType()}.
     */
    ARRAY_TYPE_COMPONENT_TYPE(
        ArrayType.class,
        "componentType" //$NON-NLS-1$
    ),

    /**
     * Represents {@link AssertStatement#getExpression()}.
     */
    ASSERT_STATEMENT_EXPRESSION(
        AssertStatement.class,
        "expression" //$NON-NLS-1$
    ),

    /**
     * Represents {@link AssertStatement#getMessage()}.
     */
    ASSERT_STATEMENT_MESSAGE(
        AssertStatement.class,
        "message" //$NON-NLS-1$
    ),

    /**
     * Represents {@link AssignmentExpression#getLeftHandSide()}.
     */
    ASSIGNMENT_EXPRESSION_LEFT_HAND_SIDE(
        AssignmentExpression.class,
        "leftHandSide" //$NON-NLS-1$
    ),

    /**
     * Represents {@link AssignmentExpression#getOperator()}.
     */
    ASSIGNMENT_EXPRESSION_OPERATOR(
        AssignmentExpression.class,
        "operator" //$NON-NLS-1$
    ),

    /**
     * Represents {@link AssignmentExpression#getRightHandSide()}.
     */
    ASSIGNMENT_EXPRESSION_RIGHT_HAND_SIDE(
        AssignmentExpression.class,
        "rightHandSide" //$NON-NLS-1$
    ),

    /**
     * Represents {@link BasicType#getTypeKind()}.
     */
    BASIC_TYPE_TYPE_KIND(
        BasicType.class,
        "typeKind" //$NON-NLS-1$
    ),

    /**
     * Represents {@link Block#getStatements()}.
     */
    BLOCK_STATEMENTS(
        Block.class,
        "statements" //$NON-NLS-1$
    ),

    /**
     * Represents {@link BlockComment#getString()}.
     */
    BLOCK_COMMENT_STRING(
        BlockComment.class,
        "string" //$NON-NLS-1$
    ),

    /**
     * Represents {@link BranchStatement#getTarget()}.
     */
    BRANCH_STATEMENT_TARGET(
        BranchStatement.class,
        "target" //$NON-NLS-1$
    ),

    /**
     * Represents {@link CastExpression#getType()}.
     */
    CAST_EXPRESSION_TYPE(
        CastExpression.class,
        "type" //$NON-NLS-1$
    ),

    /**
     * Represents {@link CastExpression#getExpression()}.
     */
    CAST_EXPRESSION_EXPRESSION(
        CastExpression.class,
        "expression" //$NON-NLS-1$
    ),

    /**
     * Represents {@link CatchClause#getParameter()}.
     */
    CATCH_CLAUSE_PARAMETER(
        CatchClause.class,
        "parameter" //$NON-NLS-1$
    ),

    /**
     * Represents {@link CatchClause#getBody()}.
     */
    CATCH_CLAUSE_BODY(
        CatchClause.class,
        "body" //$NON-NLS-1$
    ),

    /**
     * Represents {@link ClassBody#getBodyDeclarations()}.
     */
    CLASS_BODY_BODY_DECLARATIONS(
        ClassBody.class,
        "bodyDeclarations" //$NON-NLS-1$
    ),

    /**
     * Represents {@link ClassDeclaration#getTypeParameters()}.
     */
    CLASS_DECLARATION_TYPE_PARAMETERS(
        ClassDeclaration.class,
        "typeParameters" //$NON-NLS-1$
    ),

    /**
     * Represents {@link ClassDeclaration#getSuperClass()}.
     */
    CLASS_DECLARATION_SUPER_CLASS(
        ClassDeclaration.class,
        "superClass" //$NON-NLS-1$
    ),

    /**
     * Represents {@link ClassDeclaration#getSuperInterfaceTypes()}.
     */
    CLASS_DECLARATION_SUPER_INTERFACE_TYPES(
        ClassDeclaration.class,
        "superInterfaceTypes" //$NON-NLS-1$
    ),

    /**
     * Represents {@link ClassInstanceCreationExpression#getQualifier()}.
     */
    CLASS_INSTANCE_CREATION_EXPRESSION_QUALIFIER(
        ClassInstanceCreationExpression.class,
        "qualifier" //$NON-NLS-1$
    ),

    /**
     * Represents {@link ClassInstanceCreationExpression#getTypeArguments()}.
     */
    CLASS_INSTANCE_CREATION_EXPRESSION_TYPE_ARGUMENTS(
        ClassInstanceCreationExpression.class,
        "typeArguments" //$NON-NLS-1$
    ),

    /**
     * Represents {@link ClassInstanceCreationExpression#getType()}.
     */
    CLASS_INSTANCE_CREATION_EXPRESSION_TYPE(
        ClassInstanceCreationExpression.class,
        "type" //$NON-NLS-1$
    ),

    /**
     * Represents {@link ClassInstanceCreationExpression#getArguments()}.
     */
    CLASS_INSTANCE_CREATION_EXPRESSION_ARGUMENTS(
        ClassInstanceCreationExpression.class,
        "arguments" //$NON-NLS-1$
    ),

    /**
     * Represents {@link ClassInstanceCreationExpression#getBody()}.
     */
    CLASS_INSTANCE_CREATION_EXPRESSION_BODY(
        ClassInstanceCreationExpression.class,
        "body" //$NON-NLS-1$
    ),

    /**
     * Represents {@link ClassLiteral#getType()}.
     */
    CLASS_LITERAL_TYPE(
        ClassLiteral.class,
        "type" //$NON-NLS-1$
    ),

    /**
     * Represents {@link CompilationUnit#getPackageDeclaration()}.
     */
    COMPILATION_UNIT_PACKAGE_DECLARATION(
        CompilationUnit.class,
        "packageDeclaration" //$NON-NLS-1$
    ),

    /**
     * Represents {@link CompilationUnit#getImportDeclarations()}.
     */
    COMPILATION_UNIT_IMPORT_DECLARATIONS(
        CompilationUnit.class,
        "importDeclarations" //$NON-NLS-1$
    ),

    /**
     * Represents {@link CompilationUnit#getTypeDeclarations()}.
     */
    COMPILATION_UNIT_TYPE_DECLARATIONS(
        CompilationUnit.class,
        "typeDeclarations" //$NON-NLS-1$
    ),

    /**
     * Represents {@link CompilationUnit#getComments()}.
     */
    COMPILATION_UNIT_COMMENTS(
        CompilationUnit.class,
        "comments" //$NON-NLS-1$
    ),

    /**
     * Represents {@link ConditionalExpression#getCondition()}.
     */
    CONDITIONAL_EXPRESSION_CONDITION(
        ConditionalExpression.class,
        "condition" //$NON-NLS-1$
    ),

    /**
     * Represents {@link ConditionalExpression#getThenExpression()}.
     */
    CONDITIONAL_EXPRESSION_THEN_EXPRESSION(
        ConditionalExpression.class,
        "thenExpression" //$NON-NLS-1$
    ),

    /**
     * Represents {@link ConditionalExpression#getElseExpression()}.
     */
    CONDITIONAL_EXPRESSION_ELSE_EXPRESSION(
        ConditionalExpression.class,
        "elseExpression" //$NON-NLS-1$
    ),

    /**
     * Represents {@link ConstructorInvocation#getTypeArguments()}.
     */
    CONSTRUCTOR_INVOCATION_TYPE_ARGUMENTS(
        ConstructorInvocation.class,
        "typeArguments" //$NON-NLS-1$
    ),

    /**
     * Represents {@link ConstructorInvocation#getArguments()}.
     */
    CONSTRUCTOR_INVOCATION_ARGUMENTS(
        ConstructorInvocation.class,
        "arguments" //$NON-NLS-1$
    ),

    /**
     * Represents {@link DoStatement#getBody()}.
     */
    DO_STATEMENT_BODY(
        DoStatement.class,
        "body" //$NON-NLS-1$
    ),

    /**
     * Represents {@link DoStatement#getCondition()}.
     */
    DO_STATEMENT_CONDITION(
        DoStatement.class,
        "condition" //$NON-NLS-1$
    ),

    /**
     * Represents {@link DocBlock#getTag()}.
     */
    DOC_BLOCK_TAG(
        DocBlock.class,
        "tag" //$NON-NLS-1$
    ),

    /**
     * Represents {@link DocBlock#getElements()}.
     */
    DOC_BLOCK_ELEMENTS(
        DocBlock.class,
        "elements" //$NON-NLS-1$
    ),

    /**
     * Represents {@link DocField#getType()}.
     */
    DOC_FIELD_TYPE(
        DocField.class,
        "type" //$NON-NLS-1$
    ),

    /**
     * Represents {@link DocField#getName()}.
     */
    DOC_FIELD_NAME(
        DocField.class,
        "name" //$NON-NLS-1$
    ),

    /**
     * Represents {@link DocMethod#getType()}.
     */
    DOC_METHOD_TYPE(
        DocMethod.class,
        "type" //$NON-NLS-1$
    ),

    /**
     * Represents {@link DocMethod#getName()}.
     */
    DOC_METHOD_NAME(
        DocMethod.class,
        "name" //$NON-NLS-1$
    ),

    /**
     * Represents {@link DocMethod#getFormalParameters()}.
     */
    DOC_METHOD_FORMAL_PARAMETERS(
        DocMethod.class,
        "formalParameters" //$NON-NLS-1$
    ),

    /**
     * Represents {@link DocMethodParameter#getType()}.
     */
    DOC_METHOD_PARAMETER_TYPE(
        DocMethodParameter.class,
        "type" //$NON-NLS-1$
    ),

    /**
     * Represents {@link DocMethodParameter#getName()}.
     */
    DOC_METHOD_PARAMETER_NAME(
        DocMethodParameter.class,
        "name" //$NON-NLS-1$
    ),

    /**
     * Represents {@link DocMethodParameter#isVariableArity()}.
     */
    DOC_METHOD_PARAMETER_VARIABLE_ARITY(
        DocMethodParameter.class,
        "variableArity" //$NON-NLS-1$
    ),

    /**
     * Represents {@link DocText#getString()}.
     */
    DOC_TEXT_STRING(
        DocText.class,
        "string" //$NON-NLS-1$
    ),

    /**
     * Represents {@link EnhancedForStatement#getParameter()}.
     */
    ENHANCED_FOR_STATEMENT_PARAMETER(
        EnhancedForStatement.class,
        "parameter" //$NON-NLS-1$
    ),

    /**
     * Represents {@link EnhancedForStatement#getExpression()}.
     */
    ENHANCED_FOR_STATEMENT_EXPRESSION(
        EnhancedForStatement.class,
        "expression" //$NON-NLS-1$
    ),

    /**
     * Represents {@link EnhancedForStatement#getBody()}.
     */
    ENHANCED_FOR_STATEMENT_BODY(
        EnhancedForStatement.class,
        "body" //$NON-NLS-1$
    ),

    /**
     * Represents {@link EnumConstantDeclaration#getName()}.
     */
    ENUM_CONSTANT_DECLARATION_NAME(
        EnumConstantDeclaration.class,
        "name" //$NON-NLS-1$
    ),

    /**
     * Represents {@link EnumConstantDeclaration#getArguments()}.
     */
    ENUM_CONSTANT_DECLARATION_ARGUMENTS(
        EnumConstantDeclaration.class,
        "arguments" //$NON-NLS-1$
    ),

    /**
     * Represents {@link EnumConstantDeclaration#getBody()}.
     */
    ENUM_CONSTANT_DECLARATION_BODY(
        EnumConstantDeclaration.class,
        "body" //$NON-NLS-1$
    ),

    /**
     * Represents {@link EnumDeclaration#getSuperInterfaceTypes()}.
     */
    ENUM_DECLARATION_SUPER_INTERFACE_TYPES(
        EnumDeclaration.class,
        "superInterfaceTypes" //$NON-NLS-1$
    ),

    /**
     * Represents {@link EnumDeclaration#getConstantDeclarations()}.
     */
    ENUM_DECLARATION_CONSTANT_DECLARATIONS(
        EnumDeclaration.class,
        "constantDeclarations" //$NON-NLS-1$
    ),

    /**
     * Represents {@link ExpressionStatement#getExpression()}.
     */
    EXPRESSION_STATEMENT_EXPRESSION(
        ExpressionStatement.class,
        "expression" //$NON-NLS-1$
    ),

    /**
     * Represents {@link FieldAccessExpression#getQualifier()}.
     */
    FIELD_ACCESS_EXPRESSION_QUALIFIER(
        FieldAccessExpression.class,
        "qualifier" //$NON-NLS-1$
    ),

    /**
     * Represents {@link FieldAccessExpression#getName()}.
     */
    FIELD_ACCESS_EXPRESSION_NAME(
        FieldAccessExpression.class,
        "name" //$NON-NLS-1$
    ),

    /**
     * Represents {@link FieldDeclaration#getType()}.
     */
    FIELD_DECLARATION_TYPE(
        FieldDeclaration.class,
        "type" //$NON-NLS-1$
    ),

    /**
     * Represents {@link FieldDeclaration#getVariableDeclarators()}.
     */
    FIELD_DECLARATION_VARIABLE_DECLARATORS(
        FieldDeclaration.class,
        "variableDeclarators" //$NON-NLS-1$
    ),

    /**
     * Represents {@link ForStatement#getInitialization()}.
     */
    FOR_STATEMENT_INITIALIZATION(
        ForStatement.class,
        "initialization" //$NON-NLS-1$
    ),

    /**
     * Represents {@link ForStatement#getCondition()}.
     */
    FOR_STATEMENT_CONDITION(
        ForStatement.class,
        "condition" //$NON-NLS-1$
    ),

    /**
     * Represents {@link ForStatement#getUpdate()}.
     */
    FOR_STATEMENT_UPDATE(
        ForStatement.class,
        "update" //$NON-NLS-1$
    ),

    /**
     * Represents {@link ForStatement#getBody()}.
     */
    FOR_STATEMENT_BODY(
        ForStatement.class,
        "body" //$NON-NLS-1$
    ),

    /**
     * Represents {@link FormalParameterDeclaration#getModifiers()}.
     */
    FORMAL_PARAMETER_DECLARATION_MODIFIERS(
        FormalParameterDeclaration.class,
        "modifiers" //$NON-NLS-1$
    ),

    /**
     * Represents {@link FormalParameterDeclaration#getType()}.
     */
    FORMAL_PARAMETER_DECLARATION_TYPE(
        FormalParameterDeclaration.class,
        "type" //$NON-NLS-1$
    ),

    /**
     * Represents {@link FormalParameterDeclaration#isVariableArity()}.
     */
    FORMAL_PARAMETER_DECLARATION_VARIABLE_ARITY(
        FormalParameterDeclaration.class,
        "variableArity" //$NON-NLS-1$
    ),

    /**
     * Represents {@link FormalParameterDeclaration#getName()}.
     */
    FORMAL_PARAMETER_DECLARATION_NAME(
        FormalParameterDeclaration.class,
        "name" //$NON-NLS-1$
    ),

    /**
     * Represents {@link FormalParameterDeclaration#getExtraDimensions()}.
     */
    FORMAL_PARAMETER_DECLARATION_EXTRA_DIMENSIONS(
        FormalParameterDeclaration.class,
        "extraDimensions" //$NON-NLS-1$
    ),

    /**
     * Represents {@link IfStatement#getCondition()}.
     */
    IF_STATEMENT_CONDITION(
        IfStatement.class,
        "condition" //$NON-NLS-1$
    ),

    /**
     * Represents {@link IfStatement#getThenStatement()}.
     */
    IF_STATEMENT_THEN_STATEMENT(
        IfStatement.class,
        "thenStatement" //$NON-NLS-1$
    ),

    /**
     * Represents {@link IfStatement#getElseStatement()}.
     */
    IF_STATEMENT_ELSE_STATEMENT(
        IfStatement.class,
        "elseStatement" //$NON-NLS-1$
    ),

    /**
     * Represents {@link ImportDeclaration#getImportKind()}.
     */
    IMPORT_DECLARATION_IMPORT_KIND(
        ImportDeclaration.class,
        "importKind" //$NON-NLS-1$
    ),

    /**
     * Represents {@link ImportDeclaration#getName()}.
     */
    IMPORT_DECLARATION_NAME(
        ImportDeclaration.class,
        "name" //$NON-NLS-1$
    ),

    /**
     * Represents {@link InfixExpression#getLeftOperand()}.
     */
    INFIX_EXPRESSION_LEFT_OPERAND(
        InfixExpression.class,
        "leftOperand" //$NON-NLS-1$
    ),

    /**
     * Represents {@link InfixExpression#getOperator()}.
     */
    INFIX_EXPRESSION_OPERATOR(
        InfixExpression.class,
        "operator" //$NON-NLS-1$
    ),

    /**
     * Represents {@link InfixExpression#getRightOperand()}.
     */
    INFIX_EXPRESSION_RIGHT_OPERAND(
        InfixExpression.class,
        "rightOperand" //$NON-NLS-1$
    ),

    /**
     * Represents {@link InitializerDeclaration#getBody()}.
     */
    INITIALIZER_DECLARATION_BODY(
        InitializerDeclaration.class,
        "body" //$NON-NLS-1$
    ),

    /**
     * Represents {@link InstanceofExpression#getExpression()}.
     */
    INSTANCEOF_EXPRESSION_EXPRESSION(
        InstanceofExpression.class,
        "expression" //$NON-NLS-1$
    ),

    /**
     * Represents {@link InstanceofExpression#getType()}.
     */
    INSTANCEOF_EXPRESSION_TYPE(
        InstanceofExpression.class,
        "type" //$NON-NLS-1$
    ),

    /**
     * Represents {@link InterfaceDeclaration#getTypeParameters()}.
     */
    INTERFACE_DECLARATION_TYPE_PARAMETERS(
        InterfaceDeclaration.class,
        "typeParameters" //$NON-NLS-1$
    ),

    /**
     * Represents {@link InterfaceDeclaration#getSuperInterfaceTypes()}.
     */
    INTERFACE_DECLARATION_SUPER_INTERFACE_TYPES(
        InterfaceDeclaration.class,
        "superInterfaceTypes" //$NON-NLS-1$
    ),

    /**
     * Represents {@link Javadoc#getBlocks()}.
     */
    JAVADOC_BLOCKS(
        Javadoc.class,
        "blocks" //$NON-NLS-1$
    ),

    /**
     * Represents {@link Keyword#getQualifier()}.
     */
    KEYWORD_QUALIFIER(
        Keyword.class,
        "qualifier" //$NON-NLS-1$
    ),

    /**
     * Represents {@link LabeledStatement#getLabel()}.
     */
    LABELED_STATEMENT_LABEL(
        LabeledStatement.class,
        "label" //$NON-NLS-1$
    ),

    /**
     * Represents {@link LabeledStatement#getBody()}.
     */
    LABELED_STATEMENT_BODY(
        LabeledStatement.class,
        "body" //$NON-NLS-1$
    ),

    /**
     * Represents {@link LambdaExpression#getParameters()}.
     * @since 0.9.0
     */
    LAMBDA_EXPRESSION_PARAMETERS(
        LambdaParameter.class,
        "parameters" //$NON-NLS-1$
    ),

    /**
     * Represents {@link LambdaExpression#getBody()}.
     * @since 0.9.0
     */
    LAMBDA_EXPRESSION_BODY(
        LambdaBody.class,
        "body" //$NON-NLS-1$
    ),

    /**
     * Represents {@link LineComment#getString()}.
     */
    LINE_COMMENT_STRING(
        LineComment.class,
        "string" //$NON-NLS-1$
    ),

    /**
     * Represents {@link Literal#getToken()}.
     */
    LITERAL_TOKEN(
        Literal.class,
        "token" //$NON-NLS-1$
    ),

    /**
     * Represents {@link LocalClassDeclaration#getDeclaration()}.
     */
    LOCAL_CLASS_DECLARATION_DECLARATION(
        LocalClassDeclaration.class,
        "declaration" //$NON-NLS-1$
    ),

    /**
     * Represents {@link LocalVariableDeclaration#getModifiers()}.
     */
    LOCAL_VARIABLE_DECLARATION_MODIFIERS(
        LocalVariableDeclaration.class,
        "modifiers" //$NON-NLS-1$
    ),

    /**
     * Represents {@link LocalVariableDeclaration#getType()}.
     */
    LOCAL_VARIABLE_DECLARATION_TYPE(
        LocalVariableDeclaration.class,
        "type" //$NON-NLS-1$
    ),

    /**
     * Represents {@link LocalVariableDeclaration#getVariableDeclarators()}.
     */
    LOCAL_VARIABLE_DECLARATION_VARIABLE_DECLARATORS(
        LocalVariableDeclaration.class,
        "variableDeclarators" //$NON-NLS-1$
    ),

    /**
     * Represents {@link MethodDeclaration#getReturnType()}.
     */
    METHOD_DECLARATION_RETURN_TYPE(
        MethodDeclaration.class,
        "returnType" //$NON-NLS-1$
    ),

    /**
     * Represents {@link MethodDeclaration#getExtraDimensions()}.
     */
    METHOD_DECLARATION_EXTRA_DIMENSIONS(
        MethodDeclaration.class,
        "extraDimensions" //$NON-NLS-1$
    ),

    /**
     * Represents {@link MethodInvocationExpression#getQualifier()}.
     */
    METHOD_INVOCATION_EXPRESSION_QUALIFIER(
        MethodInvocationExpression.class,
        "qualifier" //$NON-NLS-1$
    ),

    /**
     * Represents {@link MethodInvocationExpression#getTypeArguments()}.
     */
    METHOD_INVOCATION_EXPRESSION_TYPE_ARGUMENTS(
        MethodInvocationExpression.class,
        "typeArguments" //$NON-NLS-1$
    ),

    /**
     * Represents {@link MethodInvocationExpression#getName()}.
     */
    METHOD_INVOCATION_EXPRESSION_NAME(
        MethodInvocationExpression.class,
        "name" //$NON-NLS-1$
    ),

    /**
     * Represents {@link MethodInvocationExpression#getArguments()}.
     */
    METHOD_INVOCATION_EXPRESSION_ARGUMENTS(
        MethodInvocationExpression.class,
        "arguments" //$NON-NLS-1$
    ),

    /**
     * Represents {@link MethodOrConstructorDeclaration#getTypeParameters()}.
     */
    METHOD_OR_CONSTRUCTOR_DECLARATION_TYPE_PARAMETERS(
        MethodOrConstructorDeclaration.class,
        "typeParameters" //$NON-NLS-1$
    ),

    /**
     * Represents {@link MethodOrConstructorDeclaration#getName()}.
     */
    METHOD_OR_CONSTRUCTOR_DECLARATION_NAME(
        MethodOrConstructorDeclaration.class,
        "name" //$NON-NLS-1$
    ),

    /**
     * Represents {@link MethodOrConstructorDeclaration#getFormalParameters()}.
     */
    METHOD_OR_CONSTRUCTOR_DECLARATION_FORMAL_PARAMETERS(
        MethodOrConstructorDeclaration.class,
        "formalParameters" //$NON-NLS-1$
    ),

    /**
     * Represents {@link MethodOrConstructorDeclaration#getExceptionTypes()}.
     */
    METHOD_OR_CONSTRUCTOR_DECLARATION_EXCEPTION_TYPES(
        MethodOrConstructorDeclaration.class,
        "exceptionTypes" //$NON-NLS-1$
    ),

    /**
     * Represents {@link MethodOrConstructorDeclaration#getBody()}.
     */
    METHOD_OR_CONSTRUCTOR_DECLARATION_BODY(
        MethodOrConstructorDeclaration.class,
        "body" //$NON-NLS-1$
    ),

    /**
     * Represents {@link Modifier#getModifierKind()}.
     */
    MODIFIER_MODIFIER_KIND(
        Modifier.class,
        "modifierKind" //$NON-NLS-1$
    ),

    /**
     * Represents {@link NamedType#getName()}.
     */
    NAMED_TYPE_NAME(
        NamedType.class,
        "name" //$NON-NLS-1$
    ),

    /**
     * Represents {@link NormalAnnotation#getElements()}.
     */
    NORMAL_ANNOTATION_ELEMENTS(
        NormalAnnotation.class,
        "elements" //$NON-NLS-1$
    ),

    /**
     * Represents {@link PackageDeclaration#getJavadoc()}.
     */
    PACKAGE_DECLARATION_JAVADOC(
        PackageDeclaration.class,
        "javadoc" //$NON-NLS-1$
    ),

    /**
     * Represents {@link PackageDeclaration#getAnnotations()}.
     */
    PACKAGE_DECLARATION_ANNOTATIONS(
        PackageDeclaration.class,
        "annotations" //$NON-NLS-1$
    ),

    /**
     * Represents {@link PackageDeclaration#getName()}.
     */
    PACKAGE_DECLARATION_NAME(
        PackageDeclaration.class,
        "name" //$NON-NLS-1$
    ),

    /**
     * Represents {@link ParameterizedType#getType()}.
     */
    PARAMETERIZED_TYPE_TYPE(
        ParameterizedType.class,
        "type" //$NON-NLS-1$
    ),

    /**
     * Represents {@link ParameterizedType#getTypeArguments()}.
     */
    PARAMETERIZED_TYPE_TYPE_ARGUMENTS(
        ParameterizedType.class,
        "typeArguments" //$NON-NLS-1$
    ),

    /**
     * Represents {@link ParenthesizedExpression#getExpression()}.
     */
    PARENTHESIZED_EXPRESSION_EXPRESSION(
        ParenthesizedExpression.class,
        "expression" //$NON-NLS-1$
    ),

    /**
     * Represents {@link PostfixExpression#getOperand()}.
     */
    POSTFIX_EXPRESSION_OPERAND(
        PostfixExpression.class,
        "operand" //$NON-NLS-1$
    ),

    /**
     * Represents {@link PostfixExpression#getOperator()}.
     */
    POSTFIX_EXPRESSION_OPERATOR(
        PostfixExpression.class,
        "operator" //$NON-NLS-1$
    ),

    /**
     * Represents {@link QualifiedName#getQualifier()}.
     */
    QUALIFIED_NAME_QUALIFIER(
        QualifiedName.class,
        "qualifier" //$NON-NLS-1$
    ),

    /**
     * Represents {@link QualifiedName#getSimpleName()}.
     */
    QUALIFIED_NAME_SIMPLE_NAME(
        QualifiedName.class,
        "simpleName" //$NON-NLS-1$
    ),

    /**
     * Represents {@link QualifiedType#getQualifier()}.
     */
    QUALIFIED_TYPE_QUALIFIER(
        QualifiedType.class,
        "qualifier" //$NON-NLS-1$
    ),

    /**
     * Represents {@link QualifiedType#getSimpleName()}.
     */
    QUALIFIED_TYPE_SIMPLE_NAME(
        QualifiedType.class,
        "simpleName" //$NON-NLS-1$
    ),

    /**
     * Represents {@link ReturnStatement#getExpression()}.
     */
    RETURN_STATEMENT_EXPRESSION(
        ReturnStatement.class,
        "expression" //$NON-NLS-1$
    ),

    /**
     * Represents {@link SimpleName#getToken()}.
     */
    SIMPLE_NAME_STRING(
        SimpleName.class,
        "token" //$NON-NLS-1$
    ),

    /**
     * Represents {@link SingleElementAnnotation#getExpression()}.
     */
    SINGLE_ELEMENT_ANNOTATION_EXPRESSION(
        SingleElementAnnotation.class,
        "expression" //$NON-NLS-1$
    ),

    /**
     * Represents {@link StatementExpressionList#getExpressions()}.
     */
    STATEMENT_EXPRESSION_LIST_EXPRESSIONS(
        StatementExpressionList.class,
        "expressions" //$NON-NLS-1$
    ),

    /**
     * Represents {@link SuperConstructorInvocation#getQualifier()}.
     */
    SUPER_CONSTRUCTOR_INVOCATION_QUALIFIER(
        SuperConstructorInvocation.class,
        "qualifier" //$NON-NLS-1$
    ),

    /**
     * Represents {@link SwitchCaseLabel#getExpression()}.
     */
    SWITCH_CASE_LABEL_EXPRESSION(
        SwitchCaseLabel.class,
        "expression" //$NON-NLS-1$
    ),

    /**
     * Represents {@link SwitchStatement#getExpression()}.
     */
    SWITCH_STATEMENT_EXPRESSION(
        SwitchStatement.class,
        "expression" //$NON-NLS-1$
    ),

    /**
     * Represents {@link SwitchStatement#getStatements()}.
     */
    SWITCH_STATEMENT_STATEMENTS(
        SwitchStatement.class,
        "statements" //$NON-NLS-1$
    ),

    /**
     * Represents {@link SynchronizedStatement#getExpression()}.
     */
    SYNCHRONIZED_STATEMENT_EXPRESSION(
        SynchronizedStatement.class,
        "expression" //$NON-NLS-1$
    ),

    /**
     * Represents {@link SynchronizedStatement#getBody()}.
     */
    SYNCHRONIZED_STATEMENT_BODY(
        SynchronizedStatement.class,
        "body" //$NON-NLS-1$
    ),

    /**
     * Represents {@link ThrowStatement#getExpression()}.
     */
    THROW_STATEMENT_EXPRESSION(
        ThrowStatement.class,
        "expression" //$NON-NLS-1$
    ),

    /**
     * Represents {@link TryResource#getParameter()}.
     */
    TRY_RESOURCE_PARAMETER(
        TryResource.class,
        "parameter" //$NON-NLS-1$
    ),

    /**
     * Represents {@link TryResource#getInitializer()}.
     */
    TRY_RESOURCE_INITIALIZER(
        TryResource.class,
        "initializer" //$NON-NLS-1$
    ),

    /**
     * Represents {@link TryStatement#getResources()}.
     */
    TRY_STATEMENT_RESOURCES(
        TryStatement.class,
        "resources" //$NON-NLS-1$
    ),

    /**
     * Represents {@link TryStatement#getTryBlock()}.
     */
    TRY_STATEMENT_TRY_BLOCK(
        TryStatement.class,
        "tryBlock" //$NON-NLS-1$
    ),

    /**
     * Represents {@link TryStatement#getCatchClauses()}.
     */
    TRY_STATEMENT_CATCH_CLAUSES(
        TryStatement.class,
        "catchClauses" //$NON-NLS-1$
    ),

    /**
     * Represents {@link TryStatement#getFinallyBlock()}.
     */
    TRY_STATEMENT_FINALLY_BLOCK(
        TryStatement.class,
        "finallyBlock" //$NON-NLS-1$
    ),

    /**
     * Represents {@link TypeBodyDeclaration#getJavadoc()}.
     */
    TYPE_BODY_DECLARATION_JAVADOC(
        TypeBodyDeclaration.class,
        "javadoc" //$NON-NLS-1$
    ),

    /**
     * Represents {@link TypeBodyDeclaration#getModifiers()}.
     */
    TYPE_BODY_DECLARATION_MODIFIERS(
        TypeBodyDeclaration.class,
        "modifiers" //$NON-NLS-1$
    ),

    /**
     * Represents {@link TypeDeclaration#getName()}.
     */
    TYPE_DECLARATION_NAME(
        TypeDeclaration.class,
        "name" //$NON-NLS-1$
    ),

    /**
     * Represents {@link TypeDeclaration#getBodyDeclarations()}.
     */
    TYPE_DECLARATION_BODY_DECLARATIONS(
        TypeDeclaration.class,
        "bodyDeclarations" //$NON-NLS-1$
    ),

    /**
     * Represents {@link TypeParameterDeclaration#getName()}.
     */
    TYPE_PARAMETER_DECLARATION_NAME(
        TypeParameterDeclaration.class,
        "name" //$NON-NLS-1$
    ),

    /**
     * Represents {@link TypeParameterDeclaration#getTypeBounds()}.
     */
    TYPE_PARAMETER_DECLARATION_TYPE_BOUNDS(
        TypeParameterDeclaration.class,
        "typeBounds" //$NON-NLS-1$
    ),

    /**
     * Represents {@link UnaryExpression#getOperator()}.
     */
    UNARY_EXPRESSION_OPERATOR(
        UnaryExpression.class,
        "operator" //$NON-NLS-1$
    ),

    /**
     * Represents {@link UnaryExpression#getOperand()}.
     */
    UNARY_EXPRESSION_OPERAND(
        UnaryExpression.class,
        "operand" //$NON-NLS-1$
    ),

    /**
     * Represents {@link UnionType#getAlternativeTypes()}.
     */
    UNION_TYPE_ALTERNATIVE_TYPES(
        UnionType.class,
        "alternativeTypes" //$NON-NLS-1$
    ),

    /**
     * Represents {@link VariableDeclarator#getName()}.
     */
    VARIABLE_DECLARATOR_NAME(
        VariableDeclarator.class,
        "name" //$NON-NLS-1$
    ),

    /**
     * Represents {@link VariableDeclarator#getExtraDimensions()}.
     */
    VARIABLE_DECLARATOR_EXTRA_DIMENSIONS(
        VariableDeclarator.class,
        "extraDimensions" //$NON-NLS-1$
    ),

    /**
     * Represents {@link VariableDeclarator#getInitializer()}.
     */
    VARIABLE_DECLARATOR_INITIALIZER(
        VariableDeclarator.class,
        "initializer" //$NON-NLS-1$
    ),

    /**
     * Represents {@link WhileStatement#getCondition()}.
     */
    WHILE_STATEMENT_CONDITION(
        WhileStatement.class,
        "condition" //$NON-NLS-1$
    ),

    /**
     * Represents {@link WhileStatement#getBody()}.
     */
    WHILE_STATEMENT_BODY(
        WhileStatement.class,
        "body" //$NON-NLS-1$
    ),

    /**
     * Represents {@link Wildcard#getBoundKind()}.
     */
    WILDCARD_BOUND_KIND(
        Wildcard.class,
        "boundKind" //$NON-NLS-1$
    ),

    /**
     * Represents {@link Wildcard#getTypeBound()}.
     */
    WILDCARD_TYPE_BOUND(
        Wildcard.class,
        "typeBound" //$NON-NLS-1$
    ),
    ;

    private Class<? extends Model> ownerType;

    private String name;

    PropertyKind(Class<? extends Model> ownerType, String name) {
        assert ownerType != null;
        assert name != null;
        this.ownerType = ownerType;
        this.name = name;
    }

    /**
     * Returns the interface type of the property owner.
     * @return the interface type of the property owner
     */
    public Class<? extends Model> getOwnerType() {
        return ownerType;
    }

    /**
     * Returns the name of this property.
     * @return the property name
     */
    public String getPropertyName() {
        return name;
    }

    @Override
    public String toString() {
        return java.text.MessageFormat.format(
            "{0}#{1}", //$NON-NLS-1$
            getOwnerType().getName(),
            getPropertyName());
    }
}
