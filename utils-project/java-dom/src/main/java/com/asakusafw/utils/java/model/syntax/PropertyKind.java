/**
 * Copyright 2011-2013 Asakusa Framework Team.
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
 * {@link Model}が持つプロパティの種類。
 */
public enum PropertyKind {

    /**
     * {@link Annotation#getType()}を表現する。
     */
    ANNOTATION_TYPE(
        Annotation.class,
        "type" //$$NON-NLS-1$$
    ),

    /**
     * {@link AnnotationElement#getName()}を表現する。
     */
    ANNOTATION_ELEMENT_NAME(
        AnnotationElement.class,
        "name" //$$NON-NLS-1$$
    ),

    /**
     * {@link AnnotationElement#getExpression()}を表現する。
     */
    ANNOTATION_ELEMENT_EXPRESSION(
        AnnotationElement.class,
        "expression" //$$NON-NLS-1$$
    ),

    /**
     * {@link AnnotationElementDeclaration#getType()}を表現する。
     */
    ANNOTATION_ELEMENT_DECLARATION_TYPE(
        AnnotationElementDeclaration.class,
        "type" //$$NON-NLS-1$$
    ),

    /**
     * {@link AnnotationElementDeclaration#getName()}を表現する。
     */
    ANNOTATION_ELEMENT_DECLARATION_NAME(
        AnnotationElementDeclaration.class,
        "name" //$$NON-NLS-1$$
    ),

    /**
     * {@link AnnotationElementDeclaration#getDefaultExpression()}を表現する。
     */
    ANNOTATION_ELEMENT_DECLARATION_DEFAULT_EXPRESSION(
        AnnotationElementDeclaration.class,
        "defaultExpression" //$$NON-NLS-1$$
    ),

    /**
     * {@link ArrayAccessExpression#getArray()}を表現する。
     */
    ARRAY_ACCESS_EXPRESSION_ARRAY(
        ArrayAccessExpression.class,
        "array" //$$NON-NLS-1$$
    ),

    /**
     * {@link ArrayAccessExpression#getIndex()}を表現する。
     */
    ARRAY_ACCESS_EXPRESSION_INDEX(
        ArrayAccessExpression.class,
        "index" //$$NON-NLS-1$$
    ),

    /**
     * {@link ArrayCreationExpression#getType()}を表現する。
     */
    ARRAY_CREATION_EXPRESSION_TYPE(
        ArrayCreationExpression.class,
        "type" //$$NON-NLS-1$$
    ),

    /**
     * {@link ArrayCreationExpression#getDimensionExpressions()}を表現する。
     */
    ARRAY_CREATION_EXPRESSION_DIMENSION_EXPRESSIONS(
        ArrayCreationExpression.class,
        "dimensionExpressions" //$$NON-NLS-1$$
    ),

    /**
     * {@link ArrayCreationExpression#getArrayInitializer()}を表現する。
     */
    ARRAY_CREATION_EXPRESSION_ARRAY_INITIALIZER(
        ArrayCreationExpression.class,
        "arrayInitializer" //$$NON-NLS-1$$
    ),

    /**
     * {@link ArrayInitializer#getElements()}を表現する。
     */
    ARRAY_INITIALIZER_ELEMENTS(
        ArrayInitializer.class,
        "elements" //$$NON-NLS-1$$
    ),

    /**
     * {@link ArrayType#getComponentType()}を表現する。
     */
    ARRAY_TYPE_COMPONENT_TYPE(
        ArrayType.class,
        "componentType" //$$NON-NLS-1$$
    ),

    /**
     * {@link AssertStatement#getExpression()}を表現する。
     */
    ASSERT_STATEMENT_EXPRESSION(
        AssertStatement.class,
        "expression" //$$NON-NLS-1$$
    ),

    /**
     * {@link AssertStatement#getMessage()}を表現する。
     */
    ASSERT_STATEMENT_MESSAGE(
        AssertStatement.class,
        "message" //$$NON-NLS-1$$
    ),

    /**
     * {@link AssignmentExpression#getLeftHandSide()}を表現する。
     */
    ASSIGNMENT_EXPRESSION_LEFT_HAND_SIDE(
        AssignmentExpression.class,
        "leftHandSide" //$$NON-NLS-1$$
    ),

    /**
     * {@link AssignmentExpression#getOperator()}を表現する。
     */
    ASSIGNMENT_EXPRESSION_OPERATOR(
        AssignmentExpression.class,
        "operator" //$$NON-NLS-1$$
    ),

    /**
     * {@link AssignmentExpression#getRightHandSide()}を表現する。
     */
    ASSIGNMENT_EXPRESSION_RIGHT_HAND_SIDE(
        AssignmentExpression.class,
        "rightHandSide" //$$NON-NLS-1$$
    ),

    /**
     * {@link BasicType#getTypeKind()}を表現する。
     */
    BASIC_TYPE_TYPE_KIND(
        BasicType.class,
        "typeKind" //$$NON-NLS-1$$
    ),

    /**
     * {@link Block#getStatements()}を表現する。
     */
    BLOCK_STATEMENTS(
        Block.class,
        "statements" //$$NON-NLS-1$$
    ),

    /**
     * {@link BlockComment#getString()}を表現する。
     */
    BLOCK_COMMENT_STRING(
        BlockComment.class,
        "string" //$$NON-NLS-1$$
    ),

    /**
     * {@link BranchStatement#getTarget()}を表現する。
     */
    BRANCH_STATEMENT_TARGET(
        BranchStatement.class,
        "target" //$$NON-NLS-1$$
    ),

    /**
     * {@link CastExpression#getType()}を表現する。
     */
    CAST_EXPRESSION_TYPE(
        CastExpression.class,
        "type" //$$NON-NLS-1$$
    ),

    /**
     * {@link CastExpression#getExpression()}を表現する。
     */
    CAST_EXPRESSION_EXPRESSION(
        CastExpression.class,
        "expression" //$$NON-NLS-1$$
    ),

    /**
     * {@link CatchClause#getParameter()}を表現する。
     */
    CATCH_CLAUSE_PARAMETER(
        CatchClause.class,
        "parameter" //$$NON-NLS-1$$
    ),

    /**
     * {@link CatchClause#getBody()}を表現する。
     */
    CATCH_CLAUSE_BODY(
        CatchClause.class,
        "body" //$$NON-NLS-1$$
    ),

    /**
     * {@link ClassBody#getBodyDeclarations()}を表現する。
     */
    CLASS_BODY_BODY_DECLARATIONS(
        ClassBody.class,
        "bodyDeclarations" //$$NON-NLS-1$$
    ),

    /**
     * {@link ClassDeclaration#getTypeParameters()}を表現する。
     */
    CLASS_DECLARATION_TYPE_PARAMETERS(
        ClassDeclaration.class,
        "typeParameters" //$$NON-NLS-1$$
    ),

    /**
     * {@link ClassDeclaration#getSuperClass()}を表現する。
     */
    CLASS_DECLARATION_SUPER_CLASS(
        ClassDeclaration.class,
        "superClass" //$$NON-NLS-1$$
    ),

    /**
     * {@link ClassDeclaration#getSuperInterfaceTypes()}を表現する。
     */
    CLASS_DECLARATION_SUPER_INTERFACE_TYPES(
        ClassDeclaration.class,
        "superInterfaceTypes" //$$NON-NLS-1$$
    ),

    /**
     * {@link ClassInstanceCreationExpression#getQualifier()}を表現する。
     */
    CLASS_INSTANCE_CREATION_EXPRESSION_QUALIFIER(
        ClassInstanceCreationExpression.class,
        "qualifier" //$$NON-NLS-1$$
    ),

    /**
     * {@link ClassInstanceCreationExpression#getTypeArguments()}を表現する。
     */
    CLASS_INSTANCE_CREATION_EXPRESSION_TYPE_ARGUMENTS(
        ClassInstanceCreationExpression.class,
        "typeArguments" //$$NON-NLS-1$$
    ),

    /**
     * {@link ClassInstanceCreationExpression#getType()}を表現する。
     */
    CLASS_INSTANCE_CREATION_EXPRESSION_TYPE(
        ClassInstanceCreationExpression.class,
        "type" //$$NON-NLS-1$$
    ),

    /**
     * {@link ClassInstanceCreationExpression#getArguments()}を表現する。
     */
    CLASS_INSTANCE_CREATION_EXPRESSION_ARGUMENTS(
        ClassInstanceCreationExpression.class,
        "arguments" //$$NON-NLS-1$$
    ),

    /**
     * {@link ClassInstanceCreationExpression#getBody()}を表現する。
     */
    CLASS_INSTANCE_CREATION_EXPRESSION_BODY(
        ClassInstanceCreationExpression.class,
        "body" //$$NON-NLS-1$$
    ),

    /**
     * {@link ClassLiteral#getType()}を表現する。
     */
    CLASS_LITERAL_TYPE(
        ClassLiteral.class,
        "type" //$$NON-NLS-1$$
    ),

    /**
     * {@link CompilationUnit#getPackageDeclaration()}を表現する。
     */
    COMPILATION_UNIT_PACKAGE_DECLARATION(
        CompilationUnit.class,
        "packageDeclaration" //$$NON-NLS-1$$
    ),

    /**
     * {@link CompilationUnit#getImportDeclarations()}を表現する。
     */
    COMPILATION_UNIT_IMPORT_DECLARATIONS(
        CompilationUnit.class,
        "importDeclarations" //$$NON-NLS-1$$
    ),

    /**
     * {@link CompilationUnit#getTypeDeclarations()}を表現する。
     */
    COMPILATION_UNIT_TYPE_DECLARATIONS(
        CompilationUnit.class,
        "typeDeclarations" //$$NON-NLS-1$$
    ),

    /**
     * {@link CompilationUnit#getComments()}を表現する。
     */
    COMPILATION_UNIT_COMMENTS(
        CompilationUnit.class,
        "comments" //$$NON-NLS-1$$
    ),

    /**
     * {@link ConditionalExpression#getCondition()}を表現する。
     */
    CONDITIONAL_EXPRESSION_CONDITION(
        ConditionalExpression.class,
        "condition" //$$NON-NLS-1$$
    ),

    /**
     * {@link ConditionalExpression#getThenExpression()}を表現する。
     */
    CONDITIONAL_EXPRESSION_THEN_EXPRESSION(
        ConditionalExpression.class,
        "thenExpression" //$$NON-NLS-1$$
    ),

    /**
     * {@link ConditionalExpression#getElseExpression()}を表現する。
     */
    CONDITIONAL_EXPRESSION_ELSE_EXPRESSION(
        ConditionalExpression.class,
        "elseExpression" //$$NON-NLS-1$$
    ),

    /**
     * {@link ConstructorInvocation#getTypeArguments()}を表現する。
     */
    CONSTRUCTOR_INVOCATION_TYPE_ARGUMENTS(
        ConstructorInvocation.class,
        "typeArguments" //$$NON-NLS-1$$
    ),

    /**
     * {@link ConstructorInvocation#getArguments()}を表現する。
     */
    CONSTRUCTOR_INVOCATION_ARGUMENTS(
        ConstructorInvocation.class,
        "arguments" //$$NON-NLS-1$$
    ),

    /**
     * {@link DoStatement#getBody()}を表現する。
     */
    DO_STATEMENT_BODY(
        DoStatement.class,
        "body" //$$NON-NLS-1$$
    ),

    /**
     * {@link DoStatement#getCondition()}を表現する。
     */
    DO_STATEMENT_CONDITION(
        DoStatement.class,
        "condition" //$$NON-NLS-1$$
    ),

    /**
     * {@link DocBlock#getTag()}を表現する。
     */
    DOC_BLOCK_TAG(
        DocBlock.class,
        "tag" //$$NON-NLS-1$$
    ),

    /**
     * {@link DocBlock#getElements()}を表現する。
     */
    DOC_BLOCK_ELEMENTS(
        DocBlock.class,
        "elements" //$$NON-NLS-1$$
    ),

    /**
     * {@link DocField#getType()}を表現する。
     */
    DOC_FIELD_TYPE(
        DocField.class,
        "type" //$$NON-NLS-1$$
    ),

    /**
     * {@link DocField#getName()}を表現する。
     */
    DOC_FIELD_NAME(
        DocField.class,
        "name" //$$NON-NLS-1$$
    ),

    /**
     * {@link DocMethod#getType()}を表現する。
     */
    DOC_METHOD_TYPE(
        DocMethod.class,
        "type" //$$NON-NLS-1$$
    ),

    /**
     * {@link DocMethod#getName()}を表現する。
     */
    DOC_METHOD_NAME(
        DocMethod.class,
        "name" //$$NON-NLS-1$$
    ),

    /**
     * {@link DocMethod#getFormalParameters()}を表現する。
     */
    DOC_METHOD_FORMAL_PARAMETERS(
        DocMethod.class,
        "formalParameters" //$$NON-NLS-1$$
    ),

    /**
     * {@link DocMethodParameter#getType()}を表現する。
     */
    DOC_METHOD_PARAMETER_TYPE(
        DocMethodParameter.class,
        "type" //$$NON-NLS-1$$
    ),

    /**
     * {@link DocMethodParameter#getName()}を表現する。
     */
    DOC_METHOD_PARAMETER_NAME(
        DocMethodParameter.class,
        "name" //$$NON-NLS-1$$
    ),

    /**
     * {@link DocMethodParameter#isVariableArity()}を表現する。
     */
    DOC_METHOD_PARAMETER_VARIABLE_ARITY(
        DocMethodParameter.class,
        "variableArity" //$$NON-NLS-1$$
    ),

    /**
     * {@link DocText#getString()}を表現する。
     */
    DOC_TEXT_STRING(
        DocText.class,
        "string" //$$NON-NLS-1$$
    ),

    /**
     * {@link EnhancedForStatement#getParameter()}を表現する。
     */
    ENHANCED_FOR_STATEMENT_PARAMETER(
        EnhancedForStatement.class,
        "parameter" //$$NON-NLS-1$$
    ),

    /**
     * {@link EnhancedForStatement#getExpression()}を表現する。
     */
    ENHANCED_FOR_STATEMENT_EXPRESSION(
        EnhancedForStatement.class,
        "expression" //$$NON-NLS-1$$
    ),

    /**
     * {@link EnhancedForStatement#getBody()}を表現する。
     */
    ENHANCED_FOR_STATEMENT_BODY(
        EnhancedForStatement.class,
        "body" //$$NON-NLS-1$$
    ),

    /**
     * {@link EnumConstantDeclaration#getName()}を表現する。
     */
    ENUM_CONSTANT_DECLARATION_NAME(
        EnumConstantDeclaration.class,
        "name" //$$NON-NLS-1$$
    ),

    /**
     * {@link EnumConstantDeclaration#getArguments()}を表現する。
     */
    ENUM_CONSTANT_DECLARATION_ARGUMENTS(
        EnumConstantDeclaration.class,
        "arguments" //$$NON-NLS-1$$
    ),

    /**
     * {@link EnumConstantDeclaration#getBody()}を表現する。
     */
    ENUM_CONSTANT_DECLARATION_BODY(
        EnumConstantDeclaration.class,
        "body" //$$NON-NLS-1$$
    ),

    /**
     * {@link EnumDeclaration#getSuperInterfaceTypes()}を表現する。
     */
    ENUM_DECLARATION_SUPER_INTERFACE_TYPES(
        EnumDeclaration.class,
        "superInterfaceTypes" //$$NON-NLS-1$$
    ),

    /**
     * {@link EnumDeclaration#getConstantDeclarations()}を表現する。
     */
    ENUM_DECLARATION_CONSTANT_DECLARATIONS(
        EnumDeclaration.class,
        "constantDeclarations" //$$NON-NLS-1$$
    ),

    /**
     * {@link ExpressionStatement#getExpression()}を表現する。
     */
    EXPRESSION_STATEMENT_EXPRESSION(
        ExpressionStatement.class,
        "expression" //$$NON-NLS-1$$
    ),

    /**
     * {@link FieldAccessExpression#getQualifier()}を表現する。
     */
    FIELD_ACCESS_EXPRESSION_QUALIFIER(
        FieldAccessExpression.class,
        "qualifier" //$$NON-NLS-1$$
    ),

    /**
     * {@link FieldAccessExpression#getName()}を表現する。
     */
    FIELD_ACCESS_EXPRESSION_NAME(
        FieldAccessExpression.class,
        "name" //$$NON-NLS-1$$
    ),

    /**
     * {@link FieldDeclaration#getType()}を表現する。
     */
    FIELD_DECLARATION_TYPE(
        FieldDeclaration.class,
        "type" //$$NON-NLS-1$$
    ),

    /**
     * {@link FieldDeclaration#getVariableDeclarators()}を表現する。
     */
    FIELD_DECLARATION_VARIABLE_DECLARATORS(
        FieldDeclaration.class,
        "variableDeclarators" //$$NON-NLS-1$$
    ),

    /**
     * {@link ForStatement#getInitialization()}を表現する。
     */
    FOR_STATEMENT_INITIALIZATION(
        ForStatement.class,
        "initialization" //$$NON-NLS-1$$
    ),

    /**
     * {@link ForStatement#getCondition()}を表現する。
     */
    FOR_STATEMENT_CONDITION(
        ForStatement.class,
        "condition" //$$NON-NLS-1$$
    ),

    /**
     * {@link ForStatement#getUpdate()}を表現する。
     */
    FOR_STATEMENT_UPDATE(
        ForStatement.class,
        "update" //$$NON-NLS-1$$
    ),

    /**
     * {@link ForStatement#getBody()}を表現する。
     */
    FOR_STATEMENT_BODY(
        ForStatement.class,
        "body" //$$NON-NLS-1$$
    ),

    /**
     * {@link FormalParameterDeclaration#getModifiers()}を表現する。
     */
    FORMAL_PARAMETER_DECLARATION_MODIFIERS(
        FormalParameterDeclaration.class,
        "modifiers" //$$NON-NLS-1$$
    ),

    /**
     * {@link FormalParameterDeclaration#getType()}を表現する。
     */
    FORMAL_PARAMETER_DECLARATION_TYPE(
        FormalParameterDeclaration.class,
        "type" //$$NON-NLS-1$$
    ),

    /**
     * {@link FormalParameterDeclaration#isVariableArity()}を表現する。
     */
    FORMAL_PARAMETER_DECLARATION_VARIABLE_ARITY(
        FormalParameterDeclaration.class,
        "variableArity" //$$NON-NLS-1$$
    ),

    /**
     * {@link FormalParameterDeclaration#getName()}を表現する。
     */
    FORMAL_PARAMETER_DECLARATION_NAME(
        FormalParameterDeclaration.class,
        "name" //$$NON-NLS-1$$
    ),

    /**
     * {@link FormalParameterDeclaration#getExtraDimensions()}を表現する。
     */
    FORMAL_PARAMETER_DECLARATION_EXTRA_DIMENSIONS(
        FormalParameterDeclaration.class,
        "extraDimensions" //$$NON-NLS-1$$
    ),

    /**
     * {@link IfStatement#getCondition()}を表現する。
     */
    IF_STATEMENT_CONDITION(
        IfStatement.class,
        "condition" //$$NON-NLS-1$$
    ),

    /**
     * {@link IfStatement#getThenStatement()}を表現する。
     */
    IF_STATEMENT_THEN_STATEMENT(
        IfStatement.class,
        "thenStatement" //$$NON-NLS-1$$
    ),

    /**
     * {@link IfStatement#getElseStatement()}を表現する。
     */
    IF_STATEMENT_ELSE_STATEMENT(
        IfStatement.class,
        "elseStatement" //$$NON-NLS-1$$
    ),

    /**
     * {@link ImportDeclaration#getImportKind()}を表現する。
     */
    IMPORT_DECLARATION_IMPORT_KIND(
        ImportDeclaration.class,
        "importKind" //$$NON-NLS-1$$
    ),

    /**
     * {@link ImportDeclaration#getName()}を表現する。
     */
    IMPORT_DECLARATION_NAME(
        ImportDeclaration.class,
        "name" //$$NON-NLS-1$$
    ),

    /**
     * {@link InfixExpression#getLeftOperand()}を表現する。
     */
    INFIX_EXPRESSION_LEFT_OPERAND(
        InfixExpression.class,
        "leftOperand" //$$NON-NLS-1$$
    ),

    /**
     * {@link InfixExpression#getOperator()}を表現する。
     */
    INFIX_EXPRESSION_OPERATOR(
        InfixExpression.class,
        "operator" //$$NON-NLS-1$$
    ),

    /**
     * {@link InfixExpression#getRightOperand()}を表現する。
     */
    INFIX_EXPRESSION_RIGHT_OPERAND(
        InfixExpression.class,
        "rightOperand" //$$NON-NLS-1$$
    ),

    /**
     * {@link InitializerDeclaration#getBody()}を表現する。
     */
    INITIALIZER_DECLARATION_BODY(
        InitializerDeclaration.class,
        "body" //$$NON-NLS-1$$
    ),

    /**
     * {@link InstanceofExpression#getExpression()}を表現する。
     */
    INSTANCEOF_EXPRESSION_EXPRESSION(
        InstanceofExpression.class,
        "expression" //$$NON-NLS-1$$
    ),

    /**
     * {@link InstanceofExpression#getType()}を表現する。
     */
    INSTANCEOF_EXPRESSION_TYPE(
        InstanceofExpression.class,
        "type" //$$NON-NLS-1$$
    ),

    /**
     * {@link InterfaceDeclaration#getTypeParameters()}を表現する。
     */
    INTERFACE_DECLARATION_TYPE_PARAMETERS(
        InterfaceDeclaration.class,
        "typeParameters" //$$NON-NLS-1$$
    ),

    /**
     * {@link InterfaceDeclaration#getSuperInterfaceTypes()}を表現する。
     */
    INTERFACE_DECLARATION_SUPER_INTERFACE_TYPES(
        InterfaceDeclaration.class,
        "superInterfaceTypes" //$$NON-NLS-1$$
    ),

    /**
     * {@link Javadoc#getBlocks()}を表現する。
     */
    JAVADOC_BLOCKS(
        Javadoc.class,
        "blocks" //$$NON-NLS-1$$
    ),

    /**
     * {@link Keyword#getQualifier()}を表現する。
     */
    KEYWORD_QUALIFIER(
        Keyword.class,
        "qualifier" //$$NON-NLS-1$$
    ),

    /**
     * {@link LabeledStatement#getLabel()}を表現する。
     */
    LABELED_STATEMENT_LABEL(
        LabeledStatement.class,
        "label" //$$NON-NLS-1$$
    ),

    /**
     * {@link LabeledStatement#getBody()}を表現する。
     */
    LABELED_STATEMENT_BODY(
        LabeledStatement.class,
        "body" //$$NON-NLS-1$$
    ),

    /**
     * {@link LineComment#getString()}を表現する。
     */
    LINE_COMMENT_STRING(
        LineComment.class,
        "string" //$$NON-NLS-1$$
    ),

    /**
     * {@link Literal#getToken()}を表現する。
     */
    LITERAL_TOKEN(
        Literal.class,
        "token" //$$NON-NLS-1$$
    ),

    /**
     * {@link LocalClassDeclaration#getDeclaration()}を表現する。
     */
    LOCAL_CLASS_DECLARATION_DECLARATION(
        LocalClassDeclaration.class,
        "declaration" //$$NON-NLS-1$$
    ),

    /**
     * {@link LocalVariableDeclaration#getModifiers()}を表現する。
     */
    LOCAL_VARIABLE_DECLARATION_MODIFIERS(
        LocalVariableDeclaration.class,
        "modifiers" //$$NON-NLS-1$$
    ),

    /**
     * {@link LocalVariableDeclaration#getType()}を表現する。
     */
    LOCAL_VARIABLE_DECLARATION_TYPE(
        LocalVariableDeclaration.class,
        "type" //$$NON-NLS-1$$
    ),

    /**
     * {@link LocalVariableDeclaration#getVariableDeclarators()}を表現する。
     */
    LOCAL_VARIABLE_DECLARATION_VARIABLE_DECLARATORS(
        LocalVariableDeclaration.class,
        "variableDeclarators" //$$NON-NLS-1$$
    ),

    /**
     * {@link MethodDeclaration#getReturnType()}を表現する。
     */
    METHOD_DECLARATION_RETURN_TYPE(
        MethodDeclaration.class,
        "returnType" //$$NON-NLS-1$$
    ),

    /**
     * {@link MethodDeclaration#getExtraDimensions()}を表現する。
     */
    METHOD_DECLARATION_EXTRA_DIMENSIONS(
        MethodDeclaration.class,
        "extraDimensions" //$$NON-NLS-1$$
    ),

    /**
     * {@link MethodInvocationExpression#getQualifier()}を表現する。
     */
    METHOD_INVOCATION_EXPRESSION_QUALIFIER(
        MethodInvocationExpression.class,
        "qualifier" //$$NON-NLS-1$$
    ),

    /**
     * {@link MethodInvocationExpression#getTypeArguments()}を表現する。
     */
    METHOD_INVOCATION_EXPRESSION_TYPE_ARGUMENTS(
        MethodInvocationExpression.class,
        "typeArguments" //$$NON-NLS-1$$
    ),

    /**
     * {@link MethodInvocationExpression#getName()}を表現する。
     */
    METHOD_INVOCATION_EXPRESSION_NAME(
        MethodInvocationExpression.class,
        "name" //$$NON-NLS-1$$
    ),

    /**
     * {@link MethodInvocationExpression#getArguments()}を表現する。
     */
    METHOD_INVOCATION_EXPRESSION_ARGUMENTS(
        MethodInvocationExpression.class,
        "arguments" //$$NON-NLS-1$$
    ),

    /**
     * {@link MethodOrConstructorDeclaration#getTypeParameters()}を表現する。
     */
    METHOD_OR_CONSTRUCTOR_DECLARATION_TYPE_PARAMETERS(
        MethodOrConstructorDeclaration.class,
        "typeParameters" //$$NON-NLS-1$$
    ),

    /**
     * {@link MethodOrConstructorDeclaration#getName()}を表現する。
     */
    METHOD_OR_CONSTRUCTOR_DECLARATION_NAME(
        MethodOrConstructorDeclaration.class,
        "name" //$$NON-NLS-1$$
    ),

    /**
     * {@link MethodOrConstructorDeclaration#getFormalParameters()}を表現する。
     */
    METHOD_OR_CONSTRUCTOR_DECLARATION_FORMAL_PARAMETERS(
        MethodOrConstructorDeclaration.class,
        "formalParameters" //$$NON-NLS-1$$
    ),

    /**
     * {@link MethodOrConstructorDeclaration#getExceptionTypes()}を表現する。
     */
    METHOD_OR_CONSTRUCTOR_DECLARATION_EXCEPTION_TYPES(
        MethodOrConstructorDeclaration.class,
        "exceptionTypes" //$$NON-NLS-1$$
    ),

    /**
     * {@link MethodOrConstructorDeclaration#getBody()}を表現する。
     */
    METHOD_OR_CONSTRUCTOR_DECLARATION_BODY(
        MethodOrConstructorDeclaration.class,
        "body" //$$NON-NLS-1$$
    ),

    /**
     * {@link Modifier#getModifierKind()}を表現する。
     */
    MODIFIER_MODIFIER_KIND(
        Modifier.class,
        "modifierKind" //$$NON-NLS-1$$
    ),

    /**
     * {@link NamedType#getName()}を表現する。
     */
    NAMED_TYPE_NAME(
        NamedType.class,
        "name" //$$NON-NLS-1$$
    ),

    /**
     * {@link NormalAnnotation#getElements()}を表現する。
     */
    NORMAL_ANNOTATION_ELEMENTS(
        NormalAnnotation.class,
        "elements" //$$NON-NLS-1$$
    ),

    /**
     * {@link PackageDeclaration#getJavadoc()}を表現する。
     */
    PACKAGE_DECLARATION_JAVADOC(
        PackageDeclaration.class,
        "javadoc" //$$NON-NLS-1$$
    ),

    /**
     * {@link PackageDeclaration#getAnnotations()}を表現する。
     */
    PACKAGE_DECLARATION_ANNOTATIONS(
        PackageDeclaration.class,
        "annotations" //$$NON-NLS-1$$
    ),

    /**
     * {@link PackageDeclaration#getName()}を表現する。
     */
    PACKAGE_DECLARATION_NAME(
        PackageDeclaration.class,
        "name" //$$NON-NLS-1$$
    ),

    /**
     * {@link ParameterizedType#getType()}を表現する。
     */
    PARAMETERIZED_TYPE_TYPE(
        ParameterizedType.class,
        "type" //$$NON-NLS-1$$
    ),

    /**
     * {@link ParameterizedType#getTypeArguments()}を表現する。
     */
    PARAMETERIZED_TYPE_TYPE_ARGUMENTS(
        ParameterizedType.class,
        "typeArguments" //$$NON-NLS-1$$
    ),

    /**
     * {@link ParenthesizedExpression#getExpression()}を表現する。
     */
    PARENTHESIZED_EXPRESSION_EXPRESSION(
        ParenthesizedExpression.class,
        "expression" //$$NON-NLS-1$$
    ),

    /**
     * {@link PostfixExpression#getOperand()}を表現する。
     */
    POSTFIX_EXPRESSION_OPERAND(
        PostfixExpression.class,
        "operand" //$$NON-NLS-1$$
    ),

    /**
     * {@link PostfixExpression#getOperator()}を表現する。
     */
    POSTFIX_EXPRESSION_OPERATOR(
        PostfixExpression.class,
        "operator" //$$NON-NLS-1$$
    ),

    /**
     * {@link QualifiedName#getQualifier()}を表現する。
     */
    QUALIFIED_NAME_QUALIFIER(
        QualifiedName.class,
        "qualifier" //$$NON-NLS-1$$
    ),

    /**
     * {@link QualifiedName#getSimpleName()}を表現する。
     */
    QUALIFIED_NAME_SIMPLE_NAME(
        QualifiedName.class,
        "simpleName" //$$NON-NLS-1$$
    ),

    /**
     * {@link QualifiedType#getQualifier()}を表現する。
     */
    QUALIFIED_TYPE_QUALIFIER(
        QualifiedType.class,
        "qualifier" //$$NON-NLS-1$$
    ),

    /**
     * {@link QualifiedType#getSimpleName()}を表現する。
     */
    QUALIFIED_TYPE_SIMPLE_NAME(
        QualifiedType.class,
        "simpleName" //$$NON-NLS-1$$
    ),

    /**
     * {@link ReturnStatement#getExpression()}を表現する。
     */
    RETURN_STATEMENT_EXPRESSION(
        ReturnStatement.class,
        "expression" //$$NON-NLS-1$$
    ),

    /**
     * {@link SimpleName#getToken()}を表現する。
     */
    SIMPLE_NAME_STRING(
        SimpleName.class,
        "token" //$$NON-NLS-1$$
    ),

    /**
     * {@link SingleElementAnnotation#getExpression()}を表現する。
     */
    SINGLE_ELEMENT_ANNOTATION_EXPRESSION(
        SingleElementAnnotation.class,
        "expression" //$$NON-NLS-1$$
    ),

    /**
     * {@link StatementExpressionList#getExpressions()}を表現する。
     */
    STATEMENT_EXPRESSION_LIST_EXPRESSIONS(
        StatementExpressionList.class,
        "expressions" //$$NON-NLS-1$$
    ),

    /**
     * {@link SuperConstructorInvocation#getQualifier()}を表現する。
     */
    SUPER_CONSTRUCTOR_INVOCATION_QUALIFIER(
        SuperConstructorInvocation.class,
        "qualifier" //$$NON-NLS-1$$
    ),

    /**
     * {@link SwitchCaseLabel#getExpression()}を表現する。
     */
    SWITCH_CASE_LABEL_EXPRESSION(
        SwitchCaseLabel.class,
        "expression" //$$NON-NLS-1$$
    ),

    /**
     * {@link SwitchStatement#getExpression()}を表現する。
     */
    SWITCH_STATEMENT_EXPRESSION(
        SwitchStatement.class,
        "expression" //$$NON-NLS-1$$
    ),

    /**
     * {@link SwitchStatement#getStatements()}を表現する。
     */
    SWITCH_STATEMENT_STATEMENTS(
        SwitchStatement.class,
        "statements" //$$NON-NLS-1$$
    ),

    /**
     * {@link SynchronizedStatement#getExpression()}を表現する。
     */
    SYNCHRONIZED_STATEMENT_EXPRESSION(
        SynchronizedStatement.class,
        "expression" //$$NON-NLS-1$$
    ),

    /**
     * {@link SynchronizedStatement#getBody()}を表現する。
     */
    SYNCHRONIZED_STATEMENT_BODY(
        SynchronizedStatement.class,
        "body" //$$NON-NLS-1$$
    ),

    /**
     * {@link ThrowStatement#getExpression()}を表現する。
     */
    THROW_STATEMENT_EXPRESSION(
        ThrowStatement.class,
        "expression" //$$NON-NLS-1$$
    ),

    /**
     * {@link TryStatement#getTryBlock()}を表現する。
     */
    TRY_STATEMENT_TRY_BLOCK(
        TryStatement.class,
        "tryBlock" //$$NON-NLS-1$$
    ),

    /**
     * {@link TryStatement#getCatchClauses()}を表現する。
     */
    TRY_STATEMENT_CATCH_CLAUSES(
        TryStatement.class,
        "catchClauses" //$$NON-NLS-1$$
    ),

    /**
     * {@link TryStatement#getFinallyBlock()}を表現する。
     */
    TRY_STATEMENT_FINALLY_BLOCK(
        TryStatement.class,
        "finallyBlock" //$$NON-NLS-1$$
    ),

    /**
     * {@link TypeBodyDeclaration#getJavadoc()}を表現する。
     */
    TYPE_BODY_DECLARATION_JAVADOC(
        TypeBodyDeclaration.class,
        "javadoc" //$$NON-NLS-1$$
    ),

    /**
     * {@link TypeBodyDeclaration#getModifiers()}を表現する。
     */
    TYPE_BODY_DECLARATION_MODIFIERS(
        TypeBodyDeclaration.class,
        "modifiers" //$$NON-NLS-1$$
    ),

    /**
     * {@link TypeDeclaration#getName()}を表現する。
     */
    TYPE_DECLARATION_NAME(
        TypeDeclaration.class,
        "name" //$$NON-NLS-1$$
    ),

    /**
     * {@link TypeDeclaration#getBodyDeclarations()}を表現する。
     */
    TYPE_DECLARATION_BODY_DECLARATIONS(
        TypeDeclaration.class,
        "bodyDeclarations" //$$NON-NLS-1$$
    ),

    /**
     * {@link TypeParameterDeclaration#getName()}を表現する。
     */
    TYPE_PARAMETER_DECLARATION_NAME(
        TypeParameterDeclaration.class,
        "name" //$$NON-NLS-1$$
    ),

    /**
     * {@link TypeParameterDeclaration#getTypeBounds()}を表現する。
     */
    TYPE_PARAMETER_DECLARATION_TYPE_BOUNDS(
        TypeParameterDeclaration.class,
        "typeBounds" //$$NON-NLS-1$$
    ),

    /**
     * {@link UnaryExpression#getOperator()}を表現する。
     */
    UNARY_EXPRESSION_OPERATOR(
        UnaryExpression.class,
        "operator" //$$NON-NLS-1$$
    ),

    /**
     * {@link UnaryExpression#getOperand()}を表現する。
     */
    UNARY_EXPRESSION_OPERAND(
        UnaryExpression.class,
        "operand" //$$NON-NLS-1$$
    ),

    /**
     * {@link VariableDeclarator#getName()}を表現する。
     */
    VARIABLE_DECLARATOR_NAME(
        VariableDeclarator.class,
        "name" //$$NON-NLS-1$$
    ),

    /**
     * {@link VariableDeclarator#getExtraDimensions()}を表現する。
     */
    VARIABLE_DECLARATOR_EXTRA_DIMENSIONS(
        VariableDeclarator.class,
        "extraDimensions" //$$NON-NLS-1$$
    ),

    /**
     * {@link VariableDeclarator#getInitializer()}を表現する。
     */
    VARIABLE_DECLARATOR_INITIALIZER(
        VariableDeclarator.class,
        "initializer" //$$NON-NLS-1$$
    ),

    /**
     * {@link WhileStatement#getCondition()}を表現する。
     */
    WHILE_STATEMENT_CONDITION(
        WhileStatement.class,
        "condition" //$$NON-NLS-1$$
    ),

    /**
     * {@link WhileStatement#getBody()}を表現する。
     */
    WHILE_STATEMENT_BODY(
        WhileStatement.class,
        "body" //$$NON-NLS-1$$
    ),

    /**
     * {@link Wildcard#getBoundKind()}を表現する。
     */
    WILDCARD_BOUND_KIND(
        Wildcard.class,
        "boundKind" //$$NON-NLS-1$$
    ),

    /**
     * {@link Wildcard#getTypeBound()}を表現する。
     */
    WILDCARD_TYPE_BOUND(
        Wildcard.class,
        "typeBound" //$$NON-NLS-1$$
    ),
    ;

    private Class<? extends Model> ownerType;

    private String name;

    private PropertyKind(Class<? extends Model> ownerType, String name) {
        assert ownerType != null;
        assert name != null;
        this.ownerType = ownerType;
        this.name = name;
    }

    /**
     * このプロパティを公開するインターフェース型を返す。
     * @return このプロパティを公開するインターフェース型
     */
    public Class<? extends Model> getOwnerType() {
        return ownerType;
    }

    /**
     * このプロパティの名称を返す。
     * @return このプロパティの名称
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
