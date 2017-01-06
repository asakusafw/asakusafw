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

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

/**
 * Represents a kind of {@link Model}.
 * @since 0.1.0
 * @version 0.9.1
 */
public enum ModelKind {

    /**
     * Represents {@link AlternateConstructorInvocation}.
     */
    ALTERNATE_CONSTRUCTOR_INVOCATION(AlternateConstructorInvocation.class, new PropertyKind[] {
        PropertyKind.CONSTRUCTOR_INVOCATION_TYPE_ARGUMENTS,
        PropertyKind.CONSTRUCTOR_INVOCATION_ARGUMENTS,
    }),

    /**
     * Represents {@link AnnotationDeclaration}.
     */
    ANNOTATION_DECLARATION(AnnotationDeclaration.class, new PropertyKind[] {
        PropertyKind.TYPE_BODY_DECLARATION_JAVADOC,
        PropertyKind.TYPE_BODY_DECLARATION_MODIFIERS,
        PropertyKind.TYPE_DECLARATION_NAME,
        PropertyKind.TYPE_DECLARATION_BODY_DECLARATIONS,
    }),

    /**
     * Represents {@link AnnotationElement}.
     */
    ANNOTATION_ELEMENT(AnnotationElement.class, new PropertyKind[] {
        PropertyKind.ANNOTATION_ELEMENT_NAME,
        PropertyKind.ANNOTATION_ELEMENT_EXPRESSION,
    }),

    /**
     * Represents {@link AnnotationElementDeclaration}.
     */
    ANNOTATION_ELEMENT_DECLARATION(AnnotationElementDeclaration.class, new PropertyKind[] {
        PropertyKind.TYPE_BODY_DECLARATION_JAVADOC,
        PropertyKind.TYPE_BODY_DECLARATION_MODIFIERS,
        PropertyKind.ANNOTATION_ELEMENT_DECLARATION_TYPE,
        PropertyKind.ANNOTATION_ELEMENT_DECLARATION_NAME,
        PropertyKind.ANNOTATION_ELEMENT_DECLARATION_DEFAULT_EXPRESSION,
    }),

    /**
     * Represents {@link ArrayAccessExpression}.
     */
    ARRAY_ACCESS_EXPRESSION(ArrayAccessExpression.class, new PropertyKind[] {
        PropertyKind.ARRAY_ACCESS_EXPRESSION_ARRAY,
        PropertyKind.ARRAY_ACCESS_EXPRESSION_INDEX,
    }),

    /**
     * Represents {@link ArrayCreationExpression}.
     */
    ARRAY_CREATION_EXPRESSION(ArrayCreationExpression.class, new PropertyKind[] {
        PropertyKind.ARRAY_CREATION_EXPRESSION_TYPE,
        PropertyKind.ARRAY_CREATION_EXPRESSION_DIMENSION_EXPRESSIONS,
        PropertyKind.ARRAY_CREATION_EXPRESSION_ARRAY_INITIALIZER,
    }),

    /**
     * Represents {@link ArrayInitializer}.
     */
    ARRAY_INITIALIZER(ArrayInitializer.class, new PropertyKind[] {
        PropertyKind.ARRAY_INITIALIZER_ELEMENTS,
    }),

    /**
     * Represents {@link ArrayType}.
     */
    ARRAY_TYPE(ArrayType.class, new PropertyKind[] {
        PropertyKind.ARRAY_TYPE_COMPONENT_TYPE,
    }),

    /**
     * Represents {@link AssertStatement}.
     */
    ASSERT_STATEMENT(AssertStatement.class, new PropertyKind[] {
        PropertyKind.ASSERT_STATEMENT_EXPRESSION,
        PropertyKind.ASSERT_STATEMENT_MESSAGE,
    }),

    /**
     * Represents {@link AssignmentExpression}.
     */
    ASSIGNMENT_EXPRESSION(AssignmentExpression.class, new PropertyKind[] {
        PropertyKind.ASSIGNMENT_EXPRESSION_LEFT_HAND_SIDE,
        PropertyKind.ASSIGNMENT_EXPRESSION_OPERATOR,
        PropertyKind.ASSIGNMENT_EXPRESSION_RIGHT_HAND_SIDE,
    }),

    /**
     * Represents {@link BasicType}.
     */
    BASIC_TYPE(BasicType.class, new PropertyKind[] {
        PropertyKind.BASIC_TYPE_TYPE_KIND,
    }),

    /**
     * Represents {@link Block}.
     */
    BLOCK(Block.class, new PropertyKind[] {
        PropertyKind.BLOCK_STATEMENTS,
    }),

    /**
     * Represents {@link BlockComment}.
     */
    BLOCK_COMMENT(BlockComment.class, new PropertyKind[] {
        PropertyKind.BLOCK_COMMENT_STRING,
    }),

    /**
     * Represents {@link BreakStatement}.
     */
    BREAK_STATEMENT(BreakStatement.class, new PropertyKind[] {
        PropertyKind.BRANCH_STATEMENT_TARGET,
    }),

    /**
     * Represents {@link CastExpression}.
     */
    CAST_EXPRESSION(CastExpression.class, new PropertyKind[] {
        PropertyKind.CAST_EXPRESSION_TYPE,
        PropertyKind.CAST_EXPRESSION_EXPRESSION,
    }),

    /**
     * Represents {@link CatchClause}.
     */
    CATCH_CLAUSE(CatchClause.class, new PropertyKind[] {
        PropertyKind.CATCH_CLAUSE_PARAMETER,
        PropertyKind.CATCH_CLAUSE_BODY,
    }),

    /**
     * Represents {@link ClassBody}.
     */
    CLASS_BODY(ClassBody.class, new PropertyKind[] {
        PropertyKind.CLASS_BODY_BODY_DECLARATIONS,
    }),

    /**
     * Represents {@link ClassDeclaration}.
     */
    CLASS_DECLARATION(ClassDeclaration.class, new PropertyKind[] {
        PropertyKind.TYPE_BODY_DECLARATION_JAVADOC,
        PropertyKind.TYPE_BODY_DECLARATION_MODIFIERS,
        PropertyKind.TYPE_DECLARATION_NAME,
        PropertyKind.CLASS_DECLARATION_TYPE_PARAMETERS,
        PropertyKind.CLASS_DECLARATION_SUPER_CLASS,
        PropertyKind.CLASS_DECLARATION_SUPER_INTERFACE_TYPES,
        PropertyKind.TYPE_DECLARATION_BODY_DECLARATIONS,
    }),

    /**
     * Represents {@link ClassInstanceCreationExpression}.
     */
    CLASS_INSTANCE_CREATION_EXPRESSION(ClassInstanceCreationExpression.class, new PropertyKind[] {
        PropertyKind.CLASS_INSTANCE_CREATION_EXPRESSION_QUALIFIER,
        PropertyKind.CLASS_INSTANCE_CREATION_EXPRESSION_TYPE_ARGUMENTS,
        PropertyKind.CLASS_INSTANCE_CREATION_EXPRESSION_TYPE,
        PropertyKind.CLASS_INSTANCE_CREATION_EXPRESSION_ARGUMENTS,
        PropertyKind.CLASS_INSTANCE_CREATION_EXPRESSION_BODY,
    }),

    /**
     * Represents {@link ClassLiteral}.
     */
    CLASS_LITERAL(ClassLiteral.class, new PropertyKind[] {
        PropertyKind.CLASS_LITERAL_TYPE,
    }),

    /**
     * Represents {@link CompilationUnit}.
     */
    COMPILATION_UNIT(CompilationUnit.class, new PropertyKind[] {
        PropertyKind.COMPILATION_UNIT_PACKAGE_DECLARATION,
        PropertyKind.COMPILATION_UNIT_IMPORT_DECLARATIONS,
        PropertyKind.COMPILATION_UNIT_TYPE_DECLARATIONS,
        PropertyKind.COMPILATION_UNIT_COMMENTS,
    }),

    /**
     * Represents {@link ConditionalExpression}.
     */
    CONDITIONAL_EXPRESSION(ConditionalExpression.class, new PropertyKind[] {
        PropertyKind.CONDITIONAL_EXPRESSION_CONDITION,
        PropertyKind.CONDITIONAL_EXPRESSION_THEN_EXPRESSION,
        PropertyKind.CONDITIONAL_EXPRESSION_ELSE_EXPRESSION,
    }),

    /**
     * Represents {@link ConstructorDeclaration}.
     */
    CONSTRUCTOR_DECLARATION(ConstructorDeclaration.class, new PropertyKind[] {
        PropertyKind.TYPE_BODY_DECLARATION_JAVADOC,
        PropertyKind.TYPE_BODY_DECLARATION_MODIFIERS,
        PropertyKind.METHOD_OR_CONSTRUCTOR_DECLARATION_TYPE_PARAMETERS,
        PropertyKind.METHOD_OR_CONSTRUCTOR_DECLARATION_NAME,
        PropertyKind.METHOD_OR_CONSTRUCTOR_DECLARATION_FORMAL_PARAMETERS,
        PropertyKind.METHOD_OR_CONSTRUCTOR_DECLARATION_EXCEPTION_TYPES,
        PropertyKind.METHOD_OR_CONSTRUCTOR_DECLARATION_BODY,
    }),

    /**
     * Represents {@link ContinueStatement}.
     */
    CONTINUE_STATEMENT(ContinueStatement.class, new PropertyKind[] {
        PropertyKind.BRANCH_STATEMENT_TARGET,
    }),

    /**
     * Represents {@link DoStatement}.
     */
    DO_STATEMENT(DoStatement.class, new PropertyKind[] {
        PropertyKind.DO_STATEMENT_BODY,
        PropertyKind.DO_STATEMENT_CONDITION,
    }),

    /**
     * Represents {@link DocBlock}.
     */
    DOC_BLOCK(DocBlock.class, new PropertyKind[] {
        PropertyKind.DOC_BLOCK_TAG,
        PropertyKind.DOC_BLOCK_ELEMENTS,
    }),

    /**
     * Represents {@link DocField}.
     */
    DOC_FIELD(DocField.class, new PropertyKind[] {
        PropertyKind.DOC_FIELD_TYPE,
        PropertyKind.DOC_FIELD_NAME,
    }),

    /**
     * Represents {@link DocMethod}.
     */
    DOC_METHOD(DocMethod.class, new PropertyKind[] {
        PropertyKind.DOC_METHOD_TYPE,
        PropertyKind.DOC_METHOD_NAME,
        PropertyKind.DOC_METHOD_FORMAL_PARAMETERS,
    }),

    /**
     * Represents {@link DocMethodParameter}.
     */
    DOC_METHOD_PARAMETER(DocMethodParameter.class, new PropertyKind[] {
        PropertyKind.DOC_METHOD_PARAMETER_TYPE,
        PropertyKind.DOC_METHOD_PARAMETER_NAME,
        PropertyKind.DOC_METHOD_PARAMETER_VARIABLE_ARITY,
    }),

    /**
     * Represents {@link DocText}.
     */
    DOC_TEXT(DocText.class, new PropertyKind[] {
        PropertyKind.DOC_TEXT_STRING,
    }),

    /**
     * Represents {@link EmptyStatement}.
     */
    EMPTY_STATEMENT(EmptyStatement.class, new PropertyKind[] {
    }),

    /**
     * Represents {@link EnhancedForStatement}.
     */
    ENHANCED_FOR_STATEMENT(EnhancedForStatement.class, new PropertyKind[] {
        PropertyKind.ENHANCED_FOR_STATEMENT_PARAMETER,
        PropertyKind.ENHANCED_FOR_STATEMENT_EXPRESSION,
        PropertyKind.ENHANCED_FOR_STATEMENT_BODY,
    }),

    /**
     * Represents {@link EnumConstantDeclaration}.
     */
    ENUM_CONSTANT_DECLARATION(EnumConstantDeclaration.class, new PropertyKind[] {
        PropertyKind.TYPE_BODY_DECLARATION_JAVADOC,
        PropertyKind.TYPE_BODY_DECLARATION_MODIFIERS,
        PropertyKind.ENUM_CONSTANT_DECLARATION_NAME,
        PropertyKind.ENUM_CONSTANT_DECLARATION_ARGUMENTS,
        PropertyKind.ENUM_CONSTANT_DECLARATION_BODY,
    }),

    /**
     * Represents {@link EnumDeclaration}.
     */
    ENUM_DECLARATION(EnumDeclaration.class, new PropertyKind[] {
        PropertyKind.TYPE_BODY_DECLARATION_JAVADOC,
        PropertyKind.TYPE_BODY_DECLARATION_MODIFIERS,
        PropertyKind.TYPE_DECLARATION_NAME,
        PropertyKind.ENUM_DECLARATION_SUPER_INTERFACE_TYPES,
        PropertyKind.ENUM_DECLARATION_CONSTANT_DECLARATIONS,
        PropertyKind.TYPE_DECLARATION_BODY_DECLARATIONS,
    }),

    /**
     * Represents {@link ExpressionStatement}.
     */
    EXPRESSION_STATEMENT(ExpressionStatement.class, new PropertyKind[] {
        PropertyKind.EXPRESSION_STATEMENT_EXPRESSION,
    }),

    /**
     * Represents {@link FieldAccessExpression}.
     */
    FIELD_ACCESS_EXPRESSION(FieldAccessExpression.class, new PropertyKind[] {
        PropertyKind.FIELD_ACCESS_EXPRESSION_QUALIFIER,
        PropertyKind.FIELD_ACCESS_EXPRESSION_NAME,
    }),

    /**
     * Represents {@link FieldDeclaration}.
     */
    FIELD_DECLARATION(FieldDeclaration.class, new PropertyKind[] {
        PropertyKind.TYPE_BODY_DECLARATION_JAVADOC,
        PropertyKind.TYPE_BODY_DECLARATION_MODIFIERS,
        PropertyKind.FIELD_DECLARATION_TYPE,
        PropertyKind.FIELD_DECLARATION_VARIABLE_DECLARATORS,
    }),

    /**
     * Represents {@link ForStatement}.
     */
    FOR_STATEMENT(ForStatement.class, new PropertyKind[] {
        PropertyKind.FOR_STATEMENT_INITIALIZATION,
        PropertyKind.FOR_STATEMENT_CONDITION,
        PropertyKind.FOR_STATEMENT_UPDATE,
        PropertyKind.FOR_STATEMENT_BODY,
    }),

    /**
     * Represents {@link FormalParameterDeclaration}.
     */
    FORMAL_PARAMETER_DECLARATION(FormalParameterDeclaration.class, new PropertyKind[] {
        PropertyKind.FORMAL_PARAMETER_DECLARATION_MODIFIERS,
        PropertyKind.FORMAL_PARAMETER_DECLARATION_TYPE,
        PropertyKind.FORMAL_PARAMETER_DECLARATION_VARIABLE_ARITY,
        PropertyKind.FORMAL_PARAMETER_DECLARATION_NAME,
        PropertyKind.FORMAL_PARAMETER_DECLARATION_EXTRA_DIMENSIONS,
    }),

    /**
     * Represents {@link IfStatement}.
     */
    IF_STATEMENT(IfStatement.class, new PropertyKind[] {
        PropertyKind.IF_STATEMENT_CONDITION,
        PropertyKind.IF_STATEMENT_THEN_STATEMENT,
        PropertyKind.IF_STATEMENT_ELSE_STATEMENT,
    }),

    /**
     * Represents {@link ImportDeclaration}.
     */
    IMPORT_DECLARATION(ImportDeclaration.class, new PropertyKind[] {
        PropertyKind.IMPORT_DECLARATION_IMPORT_KIND,
        PropertyKind.IMPORT_DECLARATION_NAME,
    }),

    /**
     * Represents {@link InfixExpression}.
     */
    INFIX_EXPRESSION(InfixExpression.class, new PropertyKind[] {
        PropertyKind.INFIX_EXPRESSION_LEFT_OPERAND,
        PropertyKind.INFIX_EXPRESSION_OPERATOR,
        PropertyKind.INFIX_EXPRESSION_RIGHT_OPERAND,
    }),

    /**
     * Represents {@link InitializerDeclaration}.
     */
    INITIALIZER_DECLARATION(InitializerDeclaration.class, new PropertyKind[] {
        PropertyKind.TYPE_BODY_DECLARATION_JAVADOC,
        PropertyKind.TYPE_BODY_DECLARATION_MODIFIERS,
        PropertyKind.INITIALIZER_DECLARATION_BODY,
    }),

    /**
     * Represents {@link InstanceofExpression}.
     */
    INSTANCEOF_EXPRESSION(InstanceofExpression.class, new PropertyKind[] {
        PropertyKind.INSTANCEOF_EXPRESSION_EXPRESSION,
        PropertyKind.INSTANCEOF_EXPRESSION_TYPE,
    }),

    /**
     * Represents {@link InterfaceDeclaration}.
     */
    INTERFACE_DECLARATION(InterfaceDeclaration.class, new PropertyKind[] {
        PropertyKind.TYPE_BODY_DECLARATION_JAVADOC,
        PropertyKind.TYPE_BODY_DECLARATION_MODIFIERS,
        PropertyKind.TYPE_DECLARATION_NAME,
        PropertyKind.INTERFACE_DECLARATION_TYPE_PARAMETERS,
        PropertyKind.INTERFACE_DECLARATION_SUPER_INTERFACE_TYPES,
        PropertyKind.TYPE_DECLARATION_BODY_DECLARATIONS,
    }),

    /**
     * Represents {@link Javadoc}.
     */
    JAVADOC(Javadoc.class, new PropertyKind[] {
        PropertyKind.JAVADOC_BLOCKS,
    }),

    /**
     * Represents {@link LabeledStatement}.
     */
    LABELED_STATEMENT(LabeledStatement.class, new PropertyKind[] {
        PropertyKind.LABELED_STATEMENT_LABEL,
        PropertyKind.LABELED_STATEMENT_BODY,
    }),

    /**
     * Represents {@link LambdaExpression}.
     * @since 0.9.0
     */
    LAMBDA_EXPRESSION(LabeledStatement.class, new PropertyKind[] {
        PropertyKind.LAMBDA_EXPRESSION_PARAMETERS,
        PropertyKind.LAMBDA_EXPRESSION_BODY,
    }),

    /**
     * Represents {@link LineComment}.
     */
    LINE_COMMENT(LineComment.class, new PropertyKind[] {
        PropertyKind.LINE_COMMENT_STRING,
    }),

    /**
     * Represents {@link Literal}.
     */
    LITERAL(Literal.class, new PropertyKind[] {
        PropertyKind.LITERAL_TOKEN,
    }),

    /**
     * Represents {@link LocalClassDeclaration}.
     */
    LOCAL_CLASS_DECLARATION(LocalClassDeclaration.class, new PropertyKind[] {
        PropertyKind.LOCAL_CLASS_DECLARATION_DECLARATION,
    }),

    /**
     * Represents {@link LocalVariableDeclaration}.
     */
    LOCAL_VARIABLE_DECLARATION(LocalVariableDeclaration.class, new PropertyKind[] {
        PropertyKind.LOCAL_VARIABLE_DECLARATION_MODIFIERS,
        PropertyKind.LOCAL_VARIABLE_DECLARATION_TYPE,
        PropertyKind.LOCAL_VARIABLE_DECLARATION_VARIABLE_DECLARATORS,
    }),

    /**
     * Represents {@link MarkerAnnotation}.
     */
    MARKER_ANNOTATION(MarkerAnnotation.class, new PropertyKind[] {
        PropertyKind.ANNOTATION_TYPE,
    }),

    /**
     * Represents {@link MethodDeclaration}.
     */
    METHOD_DECLARATION(MethodDeclaration.class, new PropertyKind[] {
        PropertyKind.TYPE_BODY_DECLARATION_JAVADOC,
        PropertyKind.TYPE_BODY_DECLARATION_MODIFIERS,
        PropertyKind.METHOD_OR_CONSTRUCTOR_DECLARATION_TYPE_PARAMETERS,
        PropertyKind.METHOD_DECLARATION_RETURN_TYPE,
        PropertyKind.METHOD_OR_CONSTRUCTOR_DECLARATION_NAME,
        PropertyKind.METHOD_OR_CONSTRUCTOR_DECLARATION_FORMAL_PARAMETERS,
        PropertyKind.METHOD_DECLARATION_EXTRA_DIMENSIONS,
        PropertyKind.METHOD_OR_CONSTRUCTOR_DECLARATION_EXCEPTION_TYPES,
        PropertyKind.METHOD_OR_CONSTRUCTOR_DECLARATION_BODY,
    }),

    /**
     * Represents {@link MethodInvocationExpression}.
     */
    METHOD_INVOCATION_EXPRESSION(MethodInvocationExpression.class, new PropertyKind[] {
        PropertyKind.METHOD_INVOCATION_EXPRESSION_QUALIFIER,
        PropertyKind.METHOD_INVOCATION_EXPRESSION_TYPE_ARGUMENTS,
        PropertyKind.METHOD_INVOCATION_EXPRESSION_NAME,
        PropertyKind.METHOD_INVOCATION_EXPRESSION_ARGUMENTS,
    }),

    /**
     * Represents {@link MethodReferenceExpression}.
     * @since 0.9.1
     */
    METHOD_REFERENCE_EXPRESSION(MethodReferenceExpression.class, new PropertyKind[] {
        PropertyKind.METHOD_OR_CONSTRUCTOR_REFERENCE_EXPRESSION_QUALIFIER,
        PropertyKind.METHOD_OR_CONSTRUCTOR_REFERENCE_EXPRESSION_TYPE_ARGUMENTS,
        PropertyKind.METHOD_REFERENCE_EXPRESSION_NAME,
    }),

    /**
     * Represents {@link ConstructorReferenceExpression}.
     * @since 0.9.1
     */
    CONSTRUCTOR_REFERENCE_EXPRESSION(ConstructorReferenceExpression.class, new PropertyKind[] {
        PropertyKind.METHOD_OR_CONSTRUCTOR_REFERENCE_EXPRESSION_QUALIFIER,
        PropertyKind.METHOD_OR_CONSTRUCTOR_REFERENCE_EXPRESSION_TYPE_ARGUMENTS,
    }),

    /**
     * Represents {@link Modifier}.
     */
    MODIFIER(Modifier.class, new PropertyKind[] {
        PropertyKind.MODIFIER_MODIFIER_KIND,
    }),

    /**
     * Represents {@link NamedType}.
     */
    NAMED_TYPE(NamedType.class, new PropertyKind[] {
        PropertyKind.NAMED_TYPE_NAME,
    }),

    /**
     * Represents {@link NormalAnnotation}.
     */
    NORMAL_ANNOTATION(NormalAnnotation.class, new PropertyKind[] {
        PropertyKind.ANNOTATION_TYPE,
        PropertyKind.NORMAL_ANNOTATION_ELEMENTS,
    }),

    /**
     * Represents {@link PackageDeclaration}.
     */
    PACKAGE_DECLARATION(PackageDeclaration.class, new PropertyKind[] {
        PropertyKind.PACKAGE_DECLARATION_JAVADOC,
        PropertyKind.PACKAGE_DECLARATION_ANNOTATIONS,
        PropertyKind.PACKAGE_DECLARATION_NAME,
    }),

    /**
     * Represents {@link ParameterizedType}.
     */
    PARAMETERIZED_TYPE(ParameterizedType.class, new PropertyKind[] {
        PropertyKind.PARAMETERIZED_TYPE_TYPE,
        PropertyKind.PARAMETERIZED_TYPE_TYPE_ARGUMENTS,
    }),

    /**
     * Represents {@link ParenthesizedExpression}.
     */
    PARENTHESIZED_EXPRESSION(ParenthesizedExpression.class, new PropertyKind[] {
        PropertyKind.PARENTHESIZED_EXPRESSION_EXPRESSION,
    }),

    /**
     * Represents {@link PostfixExpression}.
     */
    POSTFIX_EXPRESSION(PostfixExpression.class, new PropertyKind[] {
        PropertyKind.POSTFIX_EXPRESSION_OPERAND,
        PropertyKind.POSTFIX_EXPRESSION_OPERATOR,
    }),

    /**
     * Represents {@link QualifiedName}.
     */
    QUALIFIED_NAME(QualifiedName.class, new PropertyKind[] {
        PropertyKind.QUALIFIED_NAME_QUALIFIER,
        PropertyKind.QUALIFIED_NAME_SIMPLE_NAME,
    }),

    /**
     * Represents {@link QualifiedType}.
     */
    QUALIFIED_TYPE(QualifiedType.class, new PropertyKind[] {
        PropertyKind.QUALIFIED_TYPE_QUALIFIER,
        PropertyKind.QUALIFIED_TYPE_SIMPLE_NAME,
    }),

    /**
     * Represents {@link ReturnStatement}.
     */
    RETURN_STATEMENT(ReturnStatement.class, new PropertyKind[] {
        PropertyKind.RETURN_STATEMENT_EXPRESSION,
    }),

    /**
     * Represents {@link SimpleName}.
     */
    SIMPLE_NAME(SimpleName.class, new PropertyKind[] {
        PropertyKind.SIMPLE_NAME_STRING,
    }),

    /**
     * Represents {@link SingleElementAnnotation}.
     */
    SINGLE_ELEMENT_ANNOTATION(SingleElementAnnotation.class, new PropertyKind[] {
        PropertyKind.ANNOTATION_TYPE,
        PropertyKind.SINGLE_ELEMENT_ANNOTATION_EXPRESSION,
    }),

    /**
     * Represents {@link StatementExpressionList}.
     */
    STATEMENT_EXPRESSION_LIST(StatementExpressionList.class, new PropertyKind[] {
        PropertyKind.STATEMENT_EXPRESSION_LIST_EXPRESSIONS,
    }),

    /**
     * Represents {@link Super}.
     */
    SUPER(Super.class, new PropertyKind[] {
        PropertyKind.KEYWORD_QUALIFIER,
    }),

    /**
     * Represents {@link SuperConstructorInvocation}.
     */
    SUPER_CONSTRUCTOR_INVOCATION(SuperConstructorInvocation.class, new PropertyKind[] {
        PropertyKind.SUPER_CONSTRUCTOR_INVOCATION_QUALIFIER,
        PropertyKind.CONSTRUCTOR_INVOCATION_TYPE_ARGUMENTS,
        PropertyKind.CONSTRUCTOR_INVOCATION_ARGUMENTS,
    }),

    /**
     * Represents {@link SwitchCaseLabel}.
     */
    SWITCH_CASE_LABEL(SwitchCaseLabel.class, new PropertyKind[] {
        PropertyKind.SWITCH_CASE_LABEL_EXPRESSION,
    }),

    /**
     * Represents {@link SwitchDefaultLabel}.
     */
    SWITCH_DEFAULT_LABEL(SwitchDefaultLabel.class, new PropertyKind[] {
    }),

    /**
     * Represents {@link SwitchStatement}.
     */
    SWITCH_STATEMENT(SwitchStatement.class, new PropertyKind[] {
        PropertyKind.SWITCH_STATEMENT_EXPRESSION,
        PropertyKind.SWITCH_STATEMENT_STATEMENTS,
    }),

    /**
     * Represents {@link SynchronizedStatement}.
     */
    SYNCHRONIZED_STATEMENT(SynchronizedStatement.class, new PropertyKind[] {
        PropertyKind.SYNCHRONIZED_STATEMENT_EXPRESSION,
        PropertyKind.SYNCHRONIZED_STATEMENT_BODY,
    }),

    /**
     * Represents {@link This}.
     */
    THIS(This.class, new PropertyKind[] {
        PropertyKind.KEYWORD_QUALIFIER,
    }),

    /**
     * Represents {@link ThrowStatement}.
     */
    THROW_STATEMENT(ThrowStatement.class, new PropertyKind[] {
        PropertyKind.THROW_STATEMENT_EXPRESSION,
    }),

    /**
     * Represents {@link TryResource}.
     */
    TRY_RESOURCE(TryResource.class, new PropertyKind[] {
        PropertyKind.TRY_RESOURCE_PARAMETER,
        PropertyKind.TRY_RESOURCE_INITIALIZER,
    }),

    /**
     * Represents {@link TryStatement}.
     */
    TRY_STATEMENT(TryStatement.class, new PropertyKind[] {
        PropertyKind.TRY_STATEMENT_RESOURCES,
        PropertyKind.TRY_STATEMENT_TRY_BLOCK,
        PropertyKind.TRY_STATEMENT_CATCH_CLAUSES,
        PropertyKind.TRY_STATEMENT_FINALLY_BLOCK,
    }),

    /**
     * Represents {@link TypeParameterDeclaration}.
     */
    TYPE_PARAMETER_DECLARATION(TypeParameterDeclaration.class, new PropertyKind[] {
        PropertyKind.TYPE_PARAMETER_DECLARATION_NAME,
        PropertyKind.TYPE_PARAMETER_DECLARATION_TYPE_BOUNDS,
    }),

    /**
     * Represents {@link UnaryExpression}.
     */
    UNARY_EXPRESSION(UnaryExpression.class, new PropertyKind[] {
        PropertyKind.UNARY_EXPRESSION_OPERATOR,
        PropertyKind.UNARY_EXPRESSION_OPERAND,
    }),

    /**
     * Represents {@link UnionType}.
     * @since 0.9.0
     */
    UNION_TYPE(UnaryExpression.class, new PropertyKind[] {
            PropertyKind.UNION_TYPE_ALTERNATIVE_TYPES,
    }),

    /**
     * Represents {@link VariableDeclarator}.
     */
    VARIABLE_DECLARATOR(VariableDeclarator.class, new PropertyKind[] {
        PropertyKind.VARIABLE_DECLARATOR_NAME,
        PropertyKind.VARIABLE_DECLARATOR_EXTRA_DIMENSIONS,
        PropertyKind.VARIABLE_DECLARATOR_INITIALIZER,
    }),

    /**
     * Represents {@link WhileStatement}.
     */
    WHILE_STATEMENT(WhileStatement.class, new PropertyKind[] {
        PropertyKind.WHILE_STATEMENT_CONDITION,
        PropertyKind.WHILE_STATEMENT_BODY,
    }),

    /**
     * Represents {@link Wildcard}.
     */
    WILDCARD(Wildcard.class, new PropertyKind[] {
        PropertyKind.WILDCARD_BOUND_KIND,
        PropertyKind.WILDCARD_TYPE_BOUND,
    }),
    ;

    private Class<? extends Model> interfaceType;

    private List<PropertyKind> properties;

    ModelKind(Class<? extends Model> interfaceType, PropertyKind[] properties) {
        assert interfaceType != null;
        assert properties != null;
        this.interfaceType = interfaceType;
        this.properties = Collections.unmodifiableList(Arrays.asList(properties));
    }

    /**
     * Returns the interface type of this model kind.
     * @return the interface type of this model kind
     */
    public Class<? extends Model> getInterfaceType() {
        return interfaceType;
    }

    /**
     * Returns property kinds in this model kind.
     * @return property kinds
     */
    public List<PropertyKind> getProperties() {
        return properties;
    }
}
