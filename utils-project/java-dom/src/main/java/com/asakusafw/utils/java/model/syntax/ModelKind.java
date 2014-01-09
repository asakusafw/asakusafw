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
package com.asakusafw.utils.java.model.syntax;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

/**
 * {@link Model}の種類。
 */
public enum ModelKind {

    /**
     * {@link AlternateConstructorInvocation}を表現する。
     */
    ALTERNATE_CONSTRUCTOR_INVOCATION(AlternateConstructorInvocation.class, new PropertyKind[] {
        PropertyKind.CONSTRUCTOR_INVOCATION_TYPE_ARGUMENTS,
        PropertyKind.CONSTRUCTOR_INVOCATION_ARGUMENTS,
    }),

    /**
     * {@link AnnotationDeclaration}を表現する。
     */
    ANNOTATION_DECLARATION(AnnotationDeclaration.class, new PropertyKind[] {
        PropertyKind.TYPE_BODY_DECLARATION_JAVADOC,
        PropertyKind.TYPE_BODY_DECLARATION_MODIFIERS,
        PropertyKind.TYPE_DECLARATION_NAME,
        PropertyKind.TYPE_DECLARATION_BODY_DECLARATIONS,
    }),

    /**
     * {@link AnnotationElement}を表現する。
     */
    ANNOTATION_ELEMENT(AnnotationElement.class, new PropertyKind[] {
        PropertyKind.ANNOTATION_ELEMENT_NAME,
        PropertyKind.ANNOTATION_ELEMENT_EXPRESSION,
    }),

    /**
     * {@link AnnotationElementDeclaration}を表現する。
     */
    ANNOTATION_ELEMENT_DECLARATION(AnnotationElementDeclaration.class, new PropertyKind[] {
        PropertyKind.TYPE_BODY_DECLARATION_JAVADOC,
        PropertyKind.TYPE_BODY_DECLARATION_MODIFIERS,
        PropertyKind.ANNOTATION_ELEMENT_DECLARATION_TYPE,
        PropertyKind.ANNOTATION_ELEMENT_DECLARATION_NAME,
        PropertyKind.ANNOTATION_ELEMENT_DECLARATION_DEFAULT_EXPRESSION,
    }),

    /**
     * {@link ArrayAccessExpression}を表現する。
     */
    ARRAY_ACCESS_EXPRESSION(ArrayAccessExpression.class, new PropertyKind[] {
        PropertyKind.ARRAY_ACCESS_EXPRESSION_ARRAY,
        PropertyKind.ARRAY_ACCESS_EXPRESSION_INDEX,
    }),

    /**
     * {@link ArrayCreationExpression}を表現する。
     */
    ARRAY_CREATION_EXPRESSION(ArrayCreationExpression.class, new PropertyKind[] {
        PropertyKind.ARRAY_CREATION_EXPRESSION_TYPE,
        PropertyKind.ARRAY_CREATION_EXPRESSION_DIMENSION_EXPRESSIONS,
        PropertyKind.ARRAY_CREATION_EXPRESSION_ARRAY_INITIALIZER,
    }),

    /**
     * {@link ArrayInitializer}を表現する。
     */
    ARRAY_INITIALIZER(ArrayInitializer.class, new PropertyKind[] {
        PropertyKind.ARRAY_INITIALIZER_ELEMENTS,
    }),

    /**
     * {@link ArrayType}を表現する。
     */
    ARRAY_TYPE(ArrayType.class, new PropertyKind[] {
        PropertyKind.ARRAY_TYPE_COMPONENT_TYPE,
    }),

    /**
     * {@link AssertStatement}を表現する。
     */
    ASSERT_STATEMENT(AssertStatement.class, new PropertyKind[] {
        PropertyKind.ASSERT_STATEMENT_EXPRESSION,
        PropertyKind.ASSERT_STATEMENT_MESSAGE,
    }),

    /**
     * {@link AssignmentExpression}を表現する。
     */
    ASSIGNMENT_EXPRESSION(AssignmentExpression.class, new PropertyKind[] {
        PropertyKind.ASSIGNMENT_EXPRESSION_LEFT_HAND_SIDE,
        PropertyKind.ASSIGNMENT_EXPRESSION_OPERATOR,
        PropertyKind.ASSIGNMENT_EXPRESSION_RIGHT_HAND_SIDE,
    }),

    /**
     * {@link BasicType}を表現する。
     */
    BASIC_TYPE(BasicType.class, new PropertyKind[] {
        PropertyKind.BASIC_TYPE_TYPE_KIND,
    }),

    /**
     * {@link Block}を表現する。
     */
    BLOCK(Block.class, new PropertyKind[] {
        PropertyKind.BLOCK_STATEMENTS,
    }),

    /**
     * {@link BlockComment}を表現する。
     */
    BLOCK_COMMENT(BlockComment.class, new PropertyKind[] {
        PropertyKind.BLOCK_COMMENT_STRING,
    }),

    /**
     * {@link BreakStatement}を表現する。
     */
    BREAK_STATEMENT(BreakStatement.class, new PropertyKind[] {
        PropertyKind.BRANCH_STATEMENT_TARGET,
    }),

    /**
     * {@link CastExpression}を表現する。
     */
    CAST_EXPRESSION(CastExpression.class, new PropertyKind[] {
        PropertyKind.CAST_EXPRESSION_TYPE,
        PropertyKind.CAST_EXPRESSION_EXPRESSION,
    }),

    /**
     * {@link CatchClause}を表現する。
     */
    CATCH_CLAUSE(CatchClause.class, new PropertyKind[] {
        PropertyKind.CATCH_CLAUSE_PARAMETER,
        PropertyKind.CATCH_CLAUSE_BODY,
    }),

    /**
     * {@link ClassBody}を表現する。
     */
    CLASS_BODY(ClassBody.class, new PropertyKind[] {
        PropertyKind.CLASS_BODY_BODY_DECLARATIONS,
    }),

    /**
     * {@link ClassDeclaration}を表現する。
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
     * {@link ClassInstanceCreationExpression}を表現する。
     */
    CLASS_INSTANCE_CREATION_EXPRESSION(ClassInstanceCreationExpression.class, new PropertyKind[] {
        PropertyKind.CLASS_INSTANCE_CREATION_EXPRESSION_QUALIFIER,
        PropertyKind.CLASS_INSTANCE_CREATION_EXPRESSION_TYPE_ARGUMENTS,
        PropertyKind.CLASS_INSTANCE_CREATION_EXPRESSION_TYPE,
        PropertyKind.CLASS_INSTANCE_CREATION_EXPRESSION_ARGUMENTS,
        PropertyKind.CLASS_INSTANCE_CREATION_EXPRESSION_BODY,
    }),

    /**
     * {@link ClassLiteral}を表現する。
     */
    CLASS_LITERAL(ClassLiteral.class, new PropertyKind[] {
        PropertyKind.CLASS_LITERAL_TYPE,
    }),

    /**
     * {@link CompilationUnit}を表現する。
     */
    COMPILATION_UNIT(CompilationUnit.class, new PropertyKind[] {
        PropertyKind.COMPILATION_UNIT_PACKAGE_DECLARATION,
        PropertyKind.COMPILATION_UNIT_IMPORT_DECLARATIONS,
        PropertyKind.COMPILATION_UNIT_TYPE_DECLARATIONS,
        PropertyKind.COMPILATION_UNIT_COMMENTS,
    }),

    /**
     * {@link ConditionalExpression}を表現する。
     */
    CONDITIONAL_EXPRESSION(ConditionalExpression.class, new PropertyKind[] {
        PropertyKind.CONDITIONAL_EXPRESSION_CONDITION,
        PropertyKind.CONDITIONAL_EXPRESSION_THEN_EXPRESSION,
        PropertyKind.CONDITIONAL_EXPRESSION_ELSE_EXPRESSION,
    }),

    /**
     * {@link ConstructorDeclaration}を表現する。
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
     * {@link ContinueStatement}を表現する。
     */
    CONTINUE_STATEMENT(ContinueStatement.class, new PropertyKind[] {
        PropertyKind.BRANCH_STATEMENT_TARGET,
    }),

    /**
     * {@link DoStatement}を表現する。
     */
    DO_STATEMENT(DoStatement.class, new PropertyKind[] {
        PropertyKind.DO_STATEMENT_BODY,
        PropertyKind.DO_STATEMENT_CONDITION,
    }),

    /**
     * {@link DocBlock}を表現する。
     */
    DOC_BLOCK(DocBlock.class, new PropertyKind[] {
        PropertyKind.DOC_BLOCK_TAG,
        PropertyKind.DOC_BLOCK_ELEMENTS,
    }),

    /**
     * {@link DocField}を表現する。
     */
    DOC_FIELD(DocField.class, new PropertyKind[] {
        PropertyKind.DOC_FIELD_TYPE,
        PropertyKind.DOC_FIELD_NAME,
    }),

    /**
     * {@link DocMethod}を表現する。
     */
    DOC_METHOD(DocMethod.class, new PropertyKind[] {
        PropertyKind.DOC_METHOD_TYPE,
        PropertyKind.DOC_METHOD_NAME,
        PropertyKind.DOC_METHOD_FORMAL_PARAMETERS,
    }),

    /**
     * {@link DocMethodParameter}を表現する。
     */
    DOC_METHOD_PARAMETER(DocMethodParameter.class, new PropertyKind[] {
        PropertyKind.DOC_METHOD_PARAMETER_TYPE,
        PropertyKind.DOC_METHOD_PARAMETER_NAME,
        PropertyKind.DOC_METHOD_PARAMETER_VARIABLE_ARITY,
    }),

    /**
     * {@link DocText}を表現する。
     */
    DOC_TEXT(DocText.class, new PropertyKind[] {
        PropertyKind.DOC_TEXT_STRING,
    }),

    /**
     * {@link EmptyStatement}を表現する。
     */
    EMPTY_STATEMENT(EmptyStatement.class, new PropertyKind[] {
    }),

    /**
     * {@link EnhancedForStatement}を表現する。
     */
    ENHANCED_FOR_STATEMENT(EnhancedForStatement.class, new PropertyKind[] {
        PropertyKind.ENHANCED_FOR_STATEMENT_PARAMETER,
        PropertyKind.ENHANCED_FOR_STATEMENT_EXPRESSION,
        PropertyKind.ENHANCED_FOR_STATEMENT_BODY,
    }),

    /**
     * {@link EnumConstantDeclaration}を表現する。
     */
    ENUM_CONSTANT_DECLARATION(EnumConstantDeclaration.class, new PropertyKind[] {
        PropertyKind.TYPE_BODY_DECLARATION_JAVADOC,
        PropertyKind.TYPE_BODY_DECLARATION_MODIFIERS,
        PropertyKind.ENUM_CONSTANT_DECLARATION_NAME,
        PropertyKind.ENUM_CONSTANT_DECLARATION_ARGUMENTS,
        PropertyKind.ENUM_CONSTANT_DECLARATION_BODY,
    }),

    /**
     * {@link EnumDeclaration}を表現する。
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
     * {@link ExpressionStatement}を表現する。
     */
    EXPRESSION_STATEMENT(ExpressionStatement.class, new PropertyKind[] {
        PropertyKind.EXPRESSION_STATEMENT_EXPRESSION,
    }),

    /**
     * {@link FieldAccessExpression}を表現する。
     */
    FIELD_ACCESS_EXPRESSION(FieldAccessExpression.class, new PropertyKind[] {
        PropertyKind.FIELD_ACCESS_EXPRESSION_QUALIFIER,
        PropertyKind.FIELD_ACCESS_EXPRESSION_NAME,
    }),

    /**
     * {@link FieldDeclaration}を表現する。
     */
    FIELD_DECLARATION(FieldDeclaration.class, new PropertyKind[] {
        PropertyKind.TYPE_BODY_DECLARATION_JAVADOC,
        PropertyKind.TYPE_BODY_DECLARATION_MODIFIERS,
        PropertyKind.FIELD_DECLARATION_TYPE,
        PropertyKind.FIELD_DECLARATION_VARIABLE_DECLARATORS,
    }),

    /**
     * {@link ForStatement}を表現する。
     */
    FOR_STATEMENT(ForStatement.class, new PropertyKind[] {
        PropertyKind.FOR_STATEMENT_INITIALIZATION,
        PropertyKind.FOR_STATEMENT_CONDITION,
        PropertyKind.FOR_STATEMENT_UPDATE,
        PropertyKind.FOR_STATEMENT_BODY,
    }),

    /**
     * {@link FormalParameterDeclaration}を表現する。
     */
    FORMAL_PARAMETER_DECLARATION(FormalParameterDeclaration.class, new PropertyKind[] {
        PropertyKind.FORMAL_PARAMETER_DECLARATION_MODIFIERS,
        PropertyKind.FORMAL_PARAMETER_DECLARATION_TYPE,
        PropertyKind.FORMAL_PARAMETER_DECLARATION_VARIABLE_ARITY,
        PropertyKind.FORMAL_PARAMETER_DECLARATION_NAME,
        PropertyKind.FORMAL_PARAMETER_DECLARATION_EXTRA_DIMENSIONS,
    }),

    /**
     * {@link IfStatement}を表現する。
     */
    IF_STATEMENT(IfStatement.class, new PropertyKind[] {
        PropertyKind.IF_STATEMENT_CONDITION,
        PropertyKind.IF_STATEMENT_THEN_STATEMENT,
        PropertyKind.IF_STATEMENT_ELSE_STATEMENT,
    }),

    /**
     * {@link ImportDeclaration}を表現する。
     */
    IMPORT_DECLARATION(ImportDeclaration.class, new PropertyKind[] {
        PropertyKind.IMPORT_DECLARATION_IMPORT_KIND,
        PropertyKind.IMPORT_DECLARATION_NAME,
    }),

    /**
     * {@link InfixExpression}を表現する。
     */
    INFIX_EXPRESSION(InfixExpression.class, new PropertyKind[] {
        PropertyKind.INFIX_EXPRESSION_LEFT_OPERAND,
        PropertyKind.INFIX_EXPRESSION_OPERATOR,
        PropertyKind.INFIX_EXPRESSION_RIGHT_OPERAND,
    }),

    /**
     * {@link InitializerDeclaration}を表現する。
     */
    INITIALIZER_DECLARATION(InitializerDeclaration.class, new PropertyKind[] {
        PropertyKind.TYPE_BODY_DECLARATION_JAVADOC,
        PropertyKind.TYPE_BODY_DECLARATION_MODIFIERS,
        PropertyKind.INITIALIZER_DECLARATION_BODY,
    }),

    /**
     * {@link InstanceofExpression}を表現する。
     */
    INSTANCEOF_EXPRESSION(InstanceofExpression.class, new PropertyKind[] {
        PropertyKind.INSTANCEOF_EXPRESSION_EXPRESSION,
        PropertyKind.INSTANCEOF_EXPRESSION_TYPE,
    }),

    /**
     * {@link InterfaceDeclaration}を表現する。
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
     * {@link Javadoc}を表現する。
     */
    JAVADOC(Javadoc.class, new PropertyKind[] {
        PropertyKind.JAVADOC_BLOCKS,
    }),

    /**
     * {@link LabeledStatement}を表現する。
     */
    LABELED_STATEMENT(LabeledStatement.class, new PropertyKind[] {
        PropertyKind.LABELED_STATEMENT_LABEL,
        PropertyKind.LABELED_STATEMENT_BODY,
    }),

    /**
     * {@link LineComment}を表現する。
     */
    LINE_COMMENT(LineComment.class, new PropertyKind[] {
        PropertyKind.LINE_COMMENT_STRING,
    }),

    /**
     * {@link Literal}を表現する。
     */
    LITERAL(Literal.class, new PropertyKind[] {
        PropertyKind.LITERAL_TOKEN,
    }),

    /**
     * {@link LocalClassDeclaration}を表現する。
     */
    LOCAL_CLASS_DECLARATION(LocalClassDeclaration.class, new PropertyKind[] {
        PropertyKind.LOCAL_CLASS_DECLARATION_DECLARATION,
    }),

    /**
     * {@link LocalVariableDeclaration}を表現する。
     */
    LOCAL_VARIABLE_DECLARATION(LocalVariableDeclaration.class, new PropertyKind[] {
        PropertyKind.LOCAL_VARIABLE_DECLARATION_MODIFIERS,
        PropertyKind.LOCAL_VARIABLE_DECLARATION_TYPE,
        PropertyKind.LOCAL_VARIABLE_DECLARATION_VARIABLE_DECLARATORS,
    }),

    /**
     * {@link MarkerAnnotation}を表現する。
     */
    MARKER_ANNOTATION(MarkerAnnotation.class, new PropertyKind[] {
        PropertyKind.ANNOTATION_TYPE,
    }),

    /**
     * {@link MethodDeclaration}を表現する。
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
     * {@link MethodInvocationExpression}を表現する。
     */
    METHOD_INVOCATION_EXPRESSION(MethodInvocationExpression.class, new PropertyKind[] {
        PropertyKind.METHOD_INVOCATION_EXPRESSION_QUALIFIER,
        PropertyKind.METHOD_INVOCATION_EXPRESSION_TYPE_ARGUMENTS,
        PropertyKind.METHOD_INVOCATION_EXPRESSION_NAME,
        PropertyKind.METHOD_INVOCATION_EXPRESSION_ARGUMENTS,
    }),

    /**
     * {@link Modifier}を表現する。
     */
    MODIFIER(Modifier.class, new PropertyKind[] {
        PropertyKind.MODIFIER_MODIFIER_KIND,
    }),

    /**
     * {@link NamedType}を表現する。
     */
    NAMED_TYPE(NamedType.class, new PropertyKind[] {
        PropertyKind.NAMED_TYPE_NAME,
    }),

    /**
     * {@link NormalAnnotation}を表現する。
     */
    NORMAL_ANNOTATION(NormalAnnotation.class, new PropertyKind[] {
        PropertyKind.ANNOTATION_TYPE,
        PropertyKind.NORMAL_ANNOTATION_ELEMENTS,
    }),

    /**
     * {@link PackageDeclaration}を表現する。
     */
    PACKAGE_DECLARATION(PackageDeclaration.class, new PropertyKind[] {
        PropertyKind.PACKAGE_DECLARATION_JAVADOC,
        PropertyKind.PACKAGE_DECLARATION_ANNOTATIONS,
        PropertyKind.PACKAGE_DECLARATION_NAME,
    }),

    /**
     * {@link ParameterizedType}を表現する。
     */
    PARAMETERIZED_TYPE(ParameterizedType.class, new PropertyKind[] {
        PropertyKind.PARAMETERIZED_TYPE_TYPE,
        PropertyKind.PARAMETERIZED_TYPE_TYPE_ARGUMENTS,
    }),

    /**
     * {@link ParenthesizedExpression}を表現する。
     */
    PARENTHESIZED_EXPRESSION(ParenthesizedExpression.class, new PropertyKind[] {
        PropertyKind.PARENTHESIZED_EXPRESSION_EXPRESSION,
    }),

    /**
     * {@link PostfixExpression}を表現する。
     */
    POSTFIX_EXPRESSION(PostfixExpression.class, new PropertyKind[] {
        PropertyKind.POSTFIX_EXPRESSION_OPERAND,
        PropertyKind.POSTFIX_EXPRESSION_OPERATOR,
    }),

    /**
     * {@link QualifiedName}を表現する。
     */
    QUALIFIED_NAME(QualifiedName.class, new PropertyKind[] {
        PropertyKind.QUALIFIED_NAME_QUALIFIER,
        PropertyKind.QUALIFIED_NAME_SIMPLE_NAME,
    }),

    /**
     * {@link QualifiedType}を表現する。
     */
    QUALIFIED_TYPE(QualifiedType.class, new PropertyKind[] {
        PropertyKind.QUALIFIED_TYPE_QUALIFIER,
        PropertyKind.QUALIFIED_TYPE_SIMPLE_NAME,
    }),

    /**
     * {@link ReturnStatement}を表現する。
     */
    RETURN_STATEMENT(ReturnStatement.class, new PropertyKind[] {
        PropertyKind.RETURN_STATEMENT_EXPRESSION,
    }),

    /**
     * {@link SimpleName}を表現する。
     */
    SIMPLE_NAME(SimpleName.class, new PropertyKind[] {
        PropertyKind.SIMPLE_NAME_STRING,
    }),

    /**
     * {@link SingleElementAnnotation}を表現する。
     */
    SINGLE_ELEMENT_ANNOTATION(SingleElementAnnotation.class, new PropertyKind[] {
        PropertyKind.ANNOTATION_TYPE,
        PropertyKind.SINGLE_ELEMENT_ANNOTATION_EXPRESSION,
    }),

    /**
     * {@link StatementExpressionList}を表現する。
     */
    STATEMENT_EXPRESSION_LIST(StatementExpressionList.class, new PropertyKind[] {
        PropertyKind.STATEMENT_EXPRESSION_LIST_EXPRESSIONS,
    }),

    /**
     * {@link Super}を表現する。
     */
    SUPER(Super.class, new PropertyKind[] {
        PropertyKind.KEYWORD_QUALIFIER,
    }),

    /**
     * {@link SuperConstructorInvocation}を表現する。
     */
    SUPER_CONSTRUCTOR_INVOCATION(SuperConstructorInvocation.class, new PropertyKind[] {
        PropertyKind.SUPER_CONSTRUCTOR_INVOCATION_QUALIFIER,
        PropertyKind.CONSTRUCTOR_INVOCATION_TYPE_ARGUMENTS,
        PropertyKind.CONSTRUCTOR_INVOCATION_ARGUMENTS,
    }),

    /**
     * {@link SwitchCaseLabel}を表現する。
     */
    SWITCH_CASE_LABEL(SwitchCaseLabel.class, new PropertyKind[] {
        PropertyKind.SWITCH_CASE_LABEL_EXPRESSION,
    }),

    /**
     * {@link SwitchDefaultLabel}を表現する。
     */
    SWITCH_DEFAULT_LABEL(SwitchDefaultLabel.class, new PropertyKind[] {
    }),

    /**
     * {@link SwitchStatement}を表現する。
     */
    SWITCH_STATEMENT(SwitchStatement.class, new PropertyKind[] {
        PropertyKind.SWITCH_STATEMENT_EXPRESSION,
        PropertyKind.SWITCH_STATEMENT_STATEMENTS,
    }),

    /**
     * {@link SynchronizedStatement}を表現する。
     */
    SYNCHRONIZED_STATEMENT(SynchronizedStatement.class, new PropertyKind[] {
        PropertyKind.SYNCHRONIZED_STATEMENT_EXPRESSION,
        PropertyKind.SYNCHRONIZED_STATEMENT_BODY,
    }),

    /**
     * {@link This}を表現する。
     */
    THIS(This.class, new PropertyKind[] {
        PropertyKind.KEYWORD_QUALIFIER,
    }),

    /**
     * {@link ThrowStatement}を表現する。
     */
    THROW_STATEMENT(ThrowStatement.class, new PropertyKind[] {
        PropertyKind.THROW_STATEMENT_EXPRESSION,
    }),

    /**
     * {@link TryStatement}を表現する。
     */
    TRY_STATEMENT(TryStatement.class, new PropertyKind[] {
        PropertyKind.TRY_STATEMENT_TRY_BLOCK,
        PropertyKind.TRY_STATEMENT_CATCH_CLAUSES,
        PropertyKind.TRY_STATEMENT_FINALLY_BLOCK,
    }),

    /**
     * {@link TypeParameterDeclaration}を表現する。
     */
    TYPE_PARAMETER_DECLARATION(TypeParameterDeclaration.class, new PropertyKind[] {
        PropertyKind.TYPE_PARAMETER_DECLARATION_NAME,
        PropertyKind.TYPE_PARAMETER_DECLARATION_TYPE_BOUNDS,
    }),

    /**
     * {@link UnaryExpression}を表現する。
     */
    UNARY_EXPRESSION(UnaryExpression.class, new PropertyKind[] {
        PropertyKind.UNARY_EXPRESSION_OPERATOR,
        PropertyKind.UNARY_EXPRESSION_OPERAND,
    }),

    /**
     * {@link VariableDeclarator}を表現する。
     */
    VARIABLE_DECLARATOR(VariableDeclarator.class, new PropertyKind[] {
        PropertyKind.VARIABLE_DECLARATOR_NAME,
        PropertyKind.VARIABLE_DECLARATOR_EXTRA_DIMENSIONS,
        PropertyKind.VARIABLE_DECLARATOR_INITIALIZER,
    }),

    /**
     * {@link WhileStatement}を表現する。
     */
    WHILE_STATEMENT(WhileStatement.class, new PropertyKind[] {
        PropertyKind.WHILE_STATEMENT_CONDITION,
        PropertyKind.WHILE_STATEMENT_BODY,
    }),

    /**
     * {@link Wildcard}を表現する。
     */
    WILDCARD(Wildcard.class, new PropertyKind[] {
        PropertyKind.WILDCARD_BOUND_KIND,
        PropertyKind.WILDCARD_TYPE_BOUND,
    }),
    ;

    private Class<? extends Model> interfaceType;

    private List<PropertyKind> properties;

    private ModelKind(
            Class<? extends Model> interfaceType,
            PropertyKind[] properties) {
        assert interfaceType != null;
        assert properties != null;
        this.interfaceType = interfaceType;
        this.properties =
            Collections.unmodifiableList(Arrays.asList(properties));
    }

    /**
     * この種類を表現するインターフェースの型を返す。
     * @return この種類を表現するインターフェースの型
     */
    public Class<? extends Model> getInterfaceType() {
        return interfaceType;
    }

    /**
     * この種類の要素が公開するプロパティの一覧を返す。
     * @return この種類の要素が公開するプロパティの一覧
     */
    public List<PropertyKind> getProperties() {
        return properties;
    }
}
