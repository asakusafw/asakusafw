/**
 * Copyright 2011 Asakusa Framework Team.
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
package com.asakusafw.dmdl.windgate.driver;

import java.io.IOException;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Types;
import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Collections;
import java.util.EnumSet;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;

import org.apache.hadoop.io.Text;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.asakusafw.dmdl.java.emitter.EmitContext;
import com.asakusafw.dmdl.java.spi.JavaDataModelDriver;
import com.asakusafw.dmdl.model.BasicTypeKind;
import com.asakusafw.dmdl.semantics.ModelDeclaration;
import com.asakusafw.dmdl.semantics.PropertyDeclaration;
import com.asakusafw.dmdl.semantics.Type;
import com.asakusafw.dmdl.semantics.type.BasicType;
import com.asakusafw.runtime.value.Date;
import com.asakusafw.runtime.value.DateTime;
import com.asakusafw.runtime.value.DateUtil;
import com.asakusafw.windgate.core.vocabulary.DataModelJdbcSupport;
import com.asakusafw.windgate.core.vocabulary.DataModelJdbcSupport.DataModelPreparedStatement;
import com.asakusafw.windgate.core.vocabulary.DataModelJdbcSupport.DataModelResultSet;
import com.ashigeru.lang.java.model.syntax.ClassDeclaration;
import com.ashigeru.lang.java.model.syntax.Expression;
import com.ashigeru.lang.java.model.syntax.ExpressionStatement;
import com.ashigeru.lang.java.model.syntax.FieldDeclaration;
import com.ashigeru.lang.java.model.syntax.FormalParameterDeclaration;
import com.ashigeru.lang.java.model.syntax.InfixOperator;
import com.ashigeru.lang.java.model.syntax.MethodDeclaration;
import com.ashigeru.lang.java.model.syntax.ModelFactory;
import com.ashigeru.lang.java.model.syntax.PostfixOperator;
import com.ashigeru.lang.java.model.syntax.SimpleName;
import com.ashigeru.lang.java.model.syntax.Statement;
import com.ashigeru.lang.java.model.syntax.TypeBodyDeclaration;
import com.ashigeru.lang.java.model.syntax.TypeParameterDeclaration;
import com.ashigeru.lang.java.model.util.AttributeBuilder;
import com.ashigeru.lang.java.model.util.ExpressionBuilder;
import com.ashigeru.lang.java.model.util.JavadocBuilder;
import com.ashigeru.lang.java.model.util.Models;
import com.ashigeru.lang.java.model.util.TypeBuilder;

/**
 * Emits {@link DataModelJdbcSupport} implementations.
 * @since 0.2.2
 */
public class JdbcSupportEmitter extends JavaDataModelDriver {

    static final Logger LOG = LoggerFactory.getLogger(JdbcSupportEmitter.class);

    /**
     * Category name for JDBC support.
     */
    public static final String CATEGORY_JDBC = "jdbc";

    @Override
    public void generateResources(EmitContext context, ModelDeclaration model) throws IOException {
        if (isTarget(model) == false) {
            return;
        }
        checkColumnType(model);
        checkColumnConflict(model);
        EmitContext next = new EmitContext(
                context.getSemantics(),
                context.getConfiguration(),
                model,
                CATEGORY_JDBC,
                "{0}JdbcSupport");
        LOG.debug("Generating JDBC support for {}",
                context.getQualifiedTypeName().toNameString());
        Generator.emit(next, model);
        LOG.debug("Generated JDBC support for {}: {}",
                context.getQualifiedTypeName().toNameString(),
                next.getQualifiedTypeName().toNameString());
    }

    private boolean isTarget(ModelDeclaration model) {
        assert model != null;
        boolean sawTrait = false;
        boolean sawNoTrait = false;
        for (PropertyDeclaration prop : model.getDeclaredProperties()) {
            if (prop.getTrait(JdbcSupportTrait.class) != null) {
                sawTrait = true;
            } else {
                sawNoTrait = true;
            }
        }
        if (sawNoTrait == false && sawTrait) {
            // exists some properties, and all properties have column names
            return true;
        }
        if (sawTrait == false) {
            // all properties do not have column names
            return false;
        }

        LOG.warn("@{} is not declared in some properties: {}",
                JdbcSupportDriver.TARGET_NAME,
                model.getName().identifier);
        return false;
    }

    private void checkColumnType(ModelDeclaration model) throws IOException {
        assert model != null;
        for (PropertyDeclaration prop : model.getDeclaredProperties()) {
            Type type = prop.getType();
            if ((type instanceof BasicType) == false) {
                throw new IOException(MessageFormat.format(
                        "Type \"{0}\" can not map to a column: {1}.{2} ",
                        type,
                        prop.getOwner().getName().identifier,
                        prop.getName().identifier));
            }
        }
    }

    private void checkColumnConflict(ModelDeclaration model) throws IOException {
        assert model != null;
        Map<String, PropertyDeclaration> saw = new HashMap<String, PropertyDeclaration>();
        for (PropertyDeclaration prop : model.getDeclaredProperties()) {
            JdbcSupportTrait trait = prop.getTrait(JdbcSupportTrait.class);
            assert trait != null;
            String name = trait.getName();
            if (saw.containsKey(name)) {
                PropertyDeclaration other = saw.get(name);
                throw new IOException(MessageFormat.format(
                        "Column name \"{0}\" is already declared in \"{3}\": {1}.{2}",
                        name,
                        prop.getOwner().getName().identifier,
                        prop.getName().identifier,
                        other.getName().identifier));
            }
            saw.put(name, prop);
        }
    }

    private static class Generator {

        private static final String NAME_CALENDAR = "calendar";

        private static final String NAME_DATETIME = "datetime";

        private static final String NAME_DATE = "date";

        private static final String NAME_TEXT = "text";

        private static final String NAME_RESULT_SET_SUPPORT = "ResultSetSupport";

        private static final String NAME_PREPARED_STATEMENT_SUPPORT = "PreparedStatementSupport";

        private static final String NAME_PROPERTY_POSITIONS = "PROPERTY_POSITIONS";

        private static final String NAME_CREATE_VECTOR = "createPropertyVector";

        private final EmitContext context;

        private final ModelDeclaration model;

        private final ModelFactory f;

        private Generator(EmitContext context, ModelDeclaration model) {
            assert context != null;
            assert model != null;
            this.context = context;
            this.model = model;
            this.f = context.getModelFactory();
        }

        static void emit(EmitContext context, ModelDeclaration model) throws IOException {
            assert context != null;
            assert model != null;
            Generator emitter = new Generator(context, model);
            emitter.emit();
        }

        private void emit() throws IOException {
            ClassDeclaration decl = f.newClassDeclaration(
                    new JavadocBuilder(f)
                        .text("Supports JDBC interfaces for ",
                                model.getName())
                        .linkType(context.resolve(model.getSymbol()))
                        .text(".")
                        .toJavadoc(),
                    new AttributeBuilder(f)
                        .Public()
                        .Final()
                        .toAttributes(),
                    context.getTypeName(),
                    Collections.<TypeParameterDeclaration>emptyList(),
                    null,
                    Collections.singletonList(f.newParameterizedType(
                            context.resolve(DataModelJdbcSupport.class),
                            context.resolve(model.getSymbol()))),
                    createMembers());
            context.emit(decl);
        }

        private List<TypeBodyDeclaration> createMembers() {
            List<TypeBodyDeclaration> results = new ArrayList<TypeBodyDeclaration>();
            results.addAll(createMetaData());
            results.add(createGetSupportedType());
            results.add(createIsSupported());
            results.add(createCreateResultSetSupport());
            results.add(createCreatePreparedStatementSupport());
            results.add(createCreatePropertyVector());
            results.add(createResultSetSupportClass());
            results.add(createPreparedStatementSupportClass());
            return results;
        }

        private List<TypeBodyDeclaration> createMetaData() {
            List<TypeBodyDeclaration> results = new ArrayList<TypeBodyDeclaration>();
            results.add(f.newFieldDeclaration(
                    null,
                    new AttributeBuilder(f)
                        .Private()
                        .Static()
                        .Final()
                        .toAttributes(),
                    f.newParameterizedType(
                            context.resolve(Map.class),
                            context.resolve(String.class),
                            context.resolve(Integer.class)),
                    f.newSimpleName(NAME_PROPERTY_POSITIONS),
                    null));
            List<Statement> statements = new ArrayList<Statement>();
            SimpleName map = f.newSimpleName("map");
            statements.add(new TypeBuilder(f, context.resolve(TreeMap.class))
                .parameterize(context.resolve(String.class), context.resolve(Integer.class))
                .newObject()
                .toLocalVariableDeclaration(
                        f.newParameterizedType(
                                context.resolve(Map.class),
                                context.resolve(String.class),
                                context.resolve(Integer.class)),
                        map));
            List<PropertyDeclaration> properties = model.getDeclaredProperties();
            for (int i = 0, n = properties.size(); i < n; i++) {
                PropertyDeclaration property = properties.get(i);
                JdbcSupportTrait trait = property.getTrait(JdbcSupportTrait.class);
                assert trait != null;
                statements.add(new ExpressionBuilder(f, map)
                    .method("put", Models.toLiteral(f, trait.getName()), Models.toLiteral(f, i))
                    .toStatement());
            }
            statements.add(new ExpressionBuilder(f, f.newSimpleName(NAME_PROPERTY_POSITIONS))
                .assignFrom(map)
                .toStatement());
            results.add(f.newInitializerDeclaration(
                    null,
                    new AttributeBuilder(f)
                        .Static()
                        .toAttributes(),
                    f.newBlock(statements)));
            return results;
        }

        private MethodDeclaration createGetSupportedType() {
            MethodDeclaration decl = f.newMethodDeclaration(
                    null,
                    new AttributeBuilder(f)
                        .annotation(context.resolve(Override.class))
                        .Public()
                        .toAttributes(),
                    f.newParameterizedType(
                            context.resolve(Class.class),
                            context.resolve(model.getSymbol())),
                    f.newSimpleName("getSupportedType"),
                    Collections.<FormalParameterDeclaration>emptyList(),
                    Arrays.asList(new Statement[] {
                            new TypeBuilder(f, context.resolve(model.getSymbol()))
                                .dotClass()
                                .toReturnStatement()
                    }));
            return decl;
        }

        private MethodDeclaration createIsSupported() {
            SimpleName columnNames = f.newSimpleName("columnNames");
            List<Statement> statements = new ArrayList<Statement>();
            statements.add(createNullCheck(columnNames));
            statements.add(f.newIfStatement(
                    new ExpressionBuilder(f, columnNames)
                        .method("isEmpty")
                        .toExpression(),
                    f.newBlock(f.newReturnStatement(Models.toLiteral(f, false)))));
            statements.add(f.newTryStatement(
                    f.newBlock(
                            new ExpressionBuilder(f, f.newThis())
                                .method(NAME_CREATE_VECTOR, columnNames)
                                .toStatement(),
                            new ExpressionBuilder(f, Models.toLiteral(f, true))
                                .toReturnStatement()),
                    Arrays.asList(f.newCatchClause(
                            f.newFormalParameterDeclaration(
                                    context.resolve(IllegalArgumentException.class),
                                    f.newSimpleName("e")),
                            f.newBlock(new ExpressionBuilder(f, Models.toLiteral(f, false))
                                .toReturnStatement()))),
                    null));
            MethodDeclaration decl = f.newMethodDeclaration(
                    null,
                    new AttributeBuilder(f)
                        .annotation(context.resolve(Override.class))
                        .Public()
                        .toAttributes(),
                    context.resolve(boolean.class),
                    f.newSimpleName("isSupported"),
                    Arrays.asList(f.newFormalParameterDeclaration(
                            f.newParameterizedType(
                                    context.resolve(List.class),
                                    context.resolve(String.class)),
                            columnNames)),
                    statements);
            return decl;
        }

        private MethodDeclaration createCreateResultSetSupport() {
            SimpleName resultSet = f.newSimpleName("resultSet");
            SimpleName columnNames = f.newSimpleName("columnNames");
            List<Statement> statements = new ArrayList<Statement>();
            statements.add(createNullCheck(resultSet));
            statements.add(createNullCheck(columnNames));
            SimpleName vector = f.newSimpleName("vector");
            statements.add(new ExpressionBuilder(f, f.newThis())
                .method(NAME_CREATE_VECTOR, columnNames)
                .toLocalVariableDeclaration(context.resolve(int[].class), vector));
            statements.add(new TypeBuilder(f, f.newNamedType(f.newSimpleName(NAME_RESULT_SET_SUPPORT)))
                .newObject(resultSet, vector)
                .toReturnStatement());
            MethodDeclaration decl = f.newMethodDeclaration(
                    null,
                    new AttributeBuilder(f)
                        .annotation(context.resolve(Override.class))
                        .Public()
                        .toAttributes(),
                    context.resolve(f.newParameterizedType(
                            context.resolve(DataModelResultSet.class),
                            context.resolve(model.getSymbol()))),
                    f.newSimpleName("createResultSetSupport"),
                    Arrays.asList(
                            f.newFormalParameterDeclaration(
                                context.resolve(ResultSet.class),
                                resultSet),
                            f.newFormalParameterDeclaration(
                                    f.newParameterizedType(
                                            context.resolve(List.class),
                                            context.resolve(String.class)),
                                    columnNames)),
                    statements);
            return decl;
        }

        private MethodDeclaration createCreatePreparedStatementSupport() {
            SimpleName preparedStatement = f.newSimpleName("statement");
            SimpleName columnNames = f.newSimpleName("columnNames");
            List<Statement> statements = new ArrayList<Statement>();
            statements.add(createNullCheck(preparedStatement));
            statements.add(createNullCheck(columnNames));
            SimpleName vector = f.newSimpleName("vector");
            statements.add(new ExpressionBuilder(f, f.newThis())
                .method(NAME_CREATE_VECTOR, columnNames)
                .toLocalVariableDeclaration(context.resolve(int[].class), vector));
            statements.add(new TypeBuilder(f, f.newNamedType(f.newSimpleName(NAME_PREPARED_STATEMENT_SUPPORT)))
                .newObject(preparedStatement, vector)
                .toReturnStatement());
            MethodDeclaration decl = f.newMethodDeclaration(
                    null,
                    new AttributeBuilder(f)
                        .annotation(context.resolve(Override.class))
                        .Public()
                        .toAttributes(),
                    context.resolve(f.newParameterizedType(
                            context.resolve(DataModelPreparedStatement.class),
                            context.resolve(model.getSymbol()))),
                    f.newSimpleName("createPreparedStatementSupport"),
                    Arrays.asList(
                            f.newFormalParameterDeclaration(
                                context.resolve(PreparedStatement.class),
                                preparedStatement),
                            f.newFormalParameterDeclaration(
                                    f.newParameterizedType(
                                            context.resolve(List.class),
                                            context.resolve(String.class)),
                                    columnNames)),
                    statements);
            return decl;
        }

        private Statement createNullCheck(SimpleName parameter) {
            assert parameter != null;
            return f.newIfStatement(
                    new ExpressionBuilder(f, parameter)
                        .apply(InfixOperator.EQUALS, Models.toNullLiteral(f))
                        .toExpression(),
                    f.newBlock(new TypeBuilder(f, context.resolve(IllegalArgumentException.class))
                        .newObject(Models.toLiteral(f, MessageFormat.format(
                                "{0} must not be null",
                                parameter.getToken())))
                        .toThrowStatement()));
        }

        private MethodDeclaration createCreatePropertyVector() {
            SimpleName columnNames = f.newSimpleName("columnNames");
            SimpleName vector = f.newSimpleName("vector");
            List<Statement> statements = new ArrayList<Statement>();

            statements.add(new TypeBuilder(f, context.resolve(int[].class))
                .newArray(new ExpressionBuilder(f, f.newSimpleName(NAME_PROPERTY_POSITIONS))
                    .method("size")
                    .toExpression())
                .toLocalVariableDeclaration(context.resolve(int[].class), vector));

            SimpleName index = f.newSimpleName("i");
            SimpleName column = f.newSimpleName("column");
            SimpleName position = f.newSimpleName("position");
            statements.add(f.newForStatement(
                    f.newLocalVariableDeclaration(
                            new AttributeBuilder(f).toAttributes(),
                            context.resolve(int.class),
                            Arrays.asList(
                                    f.newVariableDeclarator(
                                            index,
                                            Models.toLiteral(f, 0)),
                                    f.newVariableDeclarator(
                                            f.newSimpleName("n"),
                                            new ExpressionBuilder(f, columnNames)
                                                .method("size")
                                                .toExpression()))),
                    new ExpressionBuilder(f, index)
                        .apply(InfixOperator.LESS, f.newSimpleName("n"))
                        .toExpression(),
                    f.newStatementExpressionList(new ExpressionBuilder(f, index)
                        .apply(PostfixOperator.INCREMENT)
                        .toExpression()),
                    f.newBlock(new Statement[] {
                            new ExpressionBuilder(f, columnNames)
                                .method("get", index)
                                .toLocalVariableDeclaration(context.resolve(String.class), column),
                            new ExpressionBuilder(f, f.newSimpleName(NAME_PROPERTY_POSITIONS))
                                .method("get", column)
                                .toLocalVariableDeclaration(context.resolve(Integer.class), position),
                            f.newIfStatement(
                                    new ExpressionBuilder(f, position)
                                        .apply(InfixOperator.EQUALS, Models.toNullLiteral(f))
                                        .apply(InfixOperator.CONDITIONAL_OR, new ExpressionBuilder(f, vector)
                                            .array(position)
                                            .apply(InfixOperator.NOT_EQUALS, Models.toLiteral(f, 0))
                                            .toExpression())
                                        .toExpression(),
                                    f.newBlock(new TypeBuilder(f, context.resolve(IllegalArgumentException.class))
                                        .newObject(column)
                                        .toThrowStatement())),
                            new ExpressionBuilder(f, vector)
                                .array(position)
                                .assignFrom(new ExpressionBuilder(f, index)
                                    .apply(InfixOperator.PLUS, Models.toLiteral(f, 1))
                                    .toExpression())
                            .toStatement()
                    })));
            statements.add(new ExpressionBuilder(f, vector).toReturnStatement());
            return f.newMethodDeclaration(
                    null,
                    new AttributeBuilder(f)
                        .Private()
                        .toAttributes(),
                    context.resolve(context.resolve(int[].class)),
                    f.newSimpleName(NAME_CREATE_VECTOR),
                    Arrays.asList(f.newFormalParameterDeclaration(
                            f.newParameterizedType(
                                    context.resolve(List.class),
                                    context.resolve(String.class)),
                            columnNames)),
                    statements);
        }

        private ClassDeclaration createResultSetSupportClass() {
            SimpleName resultSet = f.newSimpleName("resultSet");
            SimpleName properties = f.newSimpleName("properties");
            List<TypeBodyDeclaration> members = new ArrayList<TypeBodyDeclaration>();
            members.add(createPrivateField(ResultSet.class, resultSet, false));
            members.add(createPrivateField(int[].class, properties, false));
            Set<BasicTypeKind> kinds = collectTypeKinds();
            if (kinds.contains(BasicTypeKind.TEXT)) {
                members.add(createPrivateField(Text.class, f.newSimpleName(NAME_TEXT), true));
            }
            if (kinds.contains(BasicTypeKind.DATE)) {
                members.add(createPrivateField(Date.class, f.newSimpleName(NAME_DATE), true));
            }
            if (kinds.contains(BasicTypeKind.DATETIME)) {
                members.add(createPrivateField(DateTime.class, f.newSimpleName(NAME_DATETIME), true));
            }
            if (kinds.contains(BasicTypeKind.DATE) || kinds.contains(BasicTypeKind.DATETIME)) {
                members.add(createCalendarBuffer());
            }
            members.add(f.newConstructorDeclaration(
                    null,
                    new AttributeBuilder(f).toAttributes(),
                    f.newSimpleName(NAME_RESULT_SET_SUPPORT),
                    Arrays.asList(
                            f.newFormalParameterDeclaration(context.resolve(ResultSet.class), resultSet),
                            f.newFormalParameterDeclaration(context.resolve(int[].class), properties)),
                    Arrays.asList(mapField(resultSet), mapField(properties))));

            SimpleName object = f.newSimpleName("object");
            List<Statement> statements = new ArrayList<Statement>();
            statements.add(f.newIfStatement(
                    new ExpressionBuilder(f, resultSet)
                        .method("next")
                        .apply(InfixOperator.EQUALS, Models.toLiteral(f, false))
                        .toExpression(),
                    f.newBlock(new ExpressionBuilder(f, Models.toLiteral(f, false))
                        .toReturnStatement())));
            List<PropertyDeclaration> declared = model.getDeclaredProperties();
            for (int i = 0, n = declared.size(); i < n; i++) {
                statements.add(createResultSetSupportStatement(
                        object,
                        resultSet,
                        f.newArrayAccessExpression(properties, Models.toLiteral(f, i)),
                        declared.get(i)));
            }
            statements.add(new ExpressionBuilder(f, Models.toLiteral(f, true))
                .toReturnStatement());
            members.add(f.newMethodDeclaration(
                    null,
                    new AttributeBuilder(f)
                        .annotation(context.resolve(Override.class))
                        .Public()
                        .toAttributes(),
                    Collections.<TypeParameterDeclaration>emptyList(),
                    context.resolve(boolean.class),
                    f.newSimpleName("next"),
                    Arrays.asList(f.newFormalParameterDeclaration(context.resolve(model.getSymbol()), object)),
                    0,
                    Arrays.asList(context.resolve(SQLException.class)),
                    f.newBlock(statements)));

            return f.newClassDeclaration(
                    null,
                    new AttributeBuilder(f)
                        .Private()
                        .Static()
                        .Final()
                        .toAttributes(),
                    f.newSimpleName(NAME_RESULT_SET_SUPPORT),
                    null,
                    Arrays.asList(f.newParameterizedType(
                            context.resolve(DataModelResultSet.class),
                            context.resolve(model.getSymbol()))),
                    members);
        }

        private Set<BasicTypeKind> collectTypeKinds() {
            EnumSet<BasicTypeKind> kinds = EnumSet.noneOf(BasicTypeKind.class);
            for (PropertyDeclaration prop : model.getDeclaredProperties()) {
                kinds.add(toBasicKind(prop.getType()));
            }
            return kinds;
        }

        private FieldDeclaration createCalendarBuffer() {
            return f.newFieldDeclaration(
                    null,
                    new AttributeBuilder(f)
                        .Private()
                        .Final()
                        .toAttributes(),
                    context.resolve(Calendar.class),
                    f.newSimpleName(NAME_CALENDAR),
                    new TypeBuilder(f, context.resolve(Calendar.class))
                        .method("getInstance")
                        .toExpression());
        }

        private ClassDeclaration createPreparedStatementSupportClass() {
            SimpleName preparedStatement = f.newSimpleName("statement");
            SimpleName properties = f.newSimpleName("properties");
            List<TypeBodyDeclaration> members = new ArrayList<TypeBodyDeclaration>();
            members.add(createPrivateField(PreparedStatement.class, preparedStatement, false));
            members.add(createPrivateField(int[].class, properties, false));
            Set<BasicTypeKind> kinds = collectTypeKinds();
            if (kinds.contains(BasicTypeKind.DATE)) {
                members.add(f.newFieldDeclaration(
                        null,
                        new AttributeBuilder(f)
                            .Private()
                            .Final()
                            .toAttributes(),
                        context.resolve(java.sql.Date.class),
                        f.newSimpleName(NAME_DATE),
                        new TypeBuilder(f, context.resolve(java.sql.Date.class))
                            .newObject(Models.toLiteral(f, 0L))
                            .toExpression()));
            }
            if (kinds.contains(BasicTypeKind.DATETIME)) {
                members.add(f.newFieldDeclaration(
                        null,
                        new AttributeBuilder(f)
                            .Private()
                            .Final()
                            .toAttributes(),
                        context.resolve(java.sql.Timestamp.class),
                        f.newSimpleName(NAME_DATETIME),
                        new TypeBuilder(f, context.resolve(java.sql.Timestamp.class))
                            .newObject(Models.toLiteral(f, 0L))
                            .toExpression()));
            }
            if (kinds.contains(BasicTypeKind.DATE) || kinds.contains(BasicTypeKind.DATETIME)) {
                members.add(createCalendarBuffer());
            }
            members.add(f.newConstructorDeclaration(
                    null,
                    new AttributeBuilder(f).toAttributes(),
                    f.newSimpleName(NAME_PREPARED_STATEMENT_SUPPORT),
                    Arrays.asList(
                            f.newFormalParameterDeclaration(
                                    context.resolve(PreparedStatement.class), preparedStatement),
                            f.newFormalParameterDeclaration(
                                    context.resolve(int[].class), properties)),
                    Arrays.asList(mapField(preparedStatement), mapField(properties))));

            SimpleName object = f.newSimpleName("object");
            List<Statement> statements = new ArrayList<Statement>();
            List<PropertyDeclaration> declared = model.getDeclaredProperties();
            for (int i = 0, n = declared.size(); i < n; i++) {
                statements.add(createPreparedStatementSupportStatement(
                        object,
                        preparedStatement,
                        f.newArrayAccessExpression(properties, Models.toLiteral(f, i)),
                        declared.get(i)));
            }
            members.add(f.newMethodDeclaration(
                    null,
                    new AttributeBuilder(f)
                        .annotation(context.resolve(Override.class))
                        .Public()
                        .toAttributes(),
                    Collections.<TypeParameterDeclaration>emptyList(),
                    context.resolve(void.class),
                    f.newSimpleName("setParameters"),
                    Arrays.asList(f.newFormalParameterDeclaration(context.resolve(model.getSymbol()), object)),
                    0,
                    Arrays.asList(context.resolve(SQLException.class)),
                    f.newBlock(statements)));

            return f.newClassDeclaration(
                    null,
                    new AttributeBuilder(f)
                        .Private()
                        .Static()
                        .Final()
                        .toAttributes(),
                    f.newSimpleName(NAME_PREPARED_STATEMENT_SUPPORT),
                    null,
                    Arrays.asList(f.newParameterizedType(
                            context.resolve(DataModelPreparedStatement.class),
                            context.resolve(model.getSymbol()))),
                    members);
        }

        private Statement createResultSetSupportStatement(
                Expression object,
                Expression resultSet,
                Expression position,
                PropertyDeclaration property) {
            List<Statement> statements = new ArrayList<Statement>();
            SimpleName value = f.newSimpleName("value");
            SimpleName calendar = f.newSimpleName(NAME_CALENDAR);
            SimpleName text = f.newSimpleName(NAME_TEXT);
            SimpleName date = f.newSimpleName(NAME_DATE);
            SimpleName datetime = f.newSimpleName(NAME_DATETIME);
            BasicTypeKind kind = toBasicKind(property.getType());
            switch (kind) {
            case INT:
                statements.add(createResultSetMapping(object, resultSet, position, property, "getInt"));
                statements.add(createResultSetNullMapping(object, resultSet, property));
                break;
            case LONG:
                statements.add(createResultSetMapping(object, resultSet, position, property, "getLong"));
                statements.add(createResultSetNullMapping(object, resultSet, property));
                break;
            case FLOAT:
                statements.add(createResultSetMapping(object, resultSet, position, property, "getFloat"));
                statements.add(createResultSetNullMapping(object, resultSet, property));
                break;
            case DOUBLE:
                statements.add(createResultSetMapping(object, resultSet, position, property, "getDouble"));
                statements.add(createResultSetNullMapping(object, resultSet, property));
                break;
            case BYTE:
                statements.add(createResultSetMapping(object, resultSet, position, property, "getByte"));
                statements.add(createResultSetNullMapping(object, resultSet, property));
                break;
            case SHORT:
                statements.add(createResultSetMapping(object, resultSet, position, property, "getShort"));
                statements.add(createResultSetNullMapping(object, resultSet, property));
                break;
            case BOOLEAN:
                statements.add(createResultSetMapping(object, resultSet, position, property, "getBoolean"));
                statements.add(createResultSetNullMapping(object, resultSet, property));
                break;
            case DECIMAL:
                statements.add(createResultSetMapping(object, resultSet, position, property, "getBigDecimal"));
                break;
            case TEXT:
                statements.add(new ExpressionBuilder(f, resultSet)
                    .method("getString", position)
                    .toLocalVariableDeclaration(context.resolve(String.class), value));
                statements.add(f.newIfStatement(
                        new ExpressionBuilder(f, value)
                            .apply(InfixOperator.NOT_EQUALS, Models.toNullLiteral(f))
                            .toExpression(),
                        f.newBlock(
                                new ExpressionBuilder(f, text)
                                    .method("set", value)
                                    .toStatement(),
                                new ExpressionBuilder(f, object)
                                    .method(context.getValueSetterName(property), text)
                                    .toStatement()),
                        f.newBlock(setNullToProperty(object, property))));
                break;
            case DATE:
                statements.add(new ExpressionBuilder(f, resultSet)
                    .method("getDate", position, calendar)
                    .toLocalVariableDeclaration(context.resolve(java.sql.Date.class), value));
                statements.add(f.newIfStatement(
                        new ExpressionBuilder(f, value)
                            .apply(InfixOperator.NOT_EQUALS, Models.toNullLiteral(f))
                            .toExpression(),
                        f.newBlock(
                                new ExpressionBuilder(f, calendar)
                                    .method("setTime", value)
                                    .toStatement(),
                                new ExpressionBuilder(f, date)
                                    .method("setElapsedDays", new TypeBuilder(f, context.resolve(DateUtil.class))
                                        .method("getDayFromCalendar", calendar)
                                        .toExpression())
                                    .toStatement(),
                                new ExpressionBuilder(f, object)
                                    .method(context.getValueSetterName(property), date)
                                    .toStatement()),
                        f.newBlock(setNullToProperty(object, property))));
                break;
            case DATETIME:
                statements.add(new ExpressionBuilder(f, resultSet)
                    .method("getTimestamp", position, calendar)
                    .toLocalVariableDeclaration(context.resolve(java.sql.Timestamp.class), value));
                statements.add(f.newIfStatement(
                        new ExpressionBuilder(f, value)
                            .apply(InfixOperator.NOT_EQUALS, Models.toNullLiteral(f))
                            .toExpression(),
                        f.newBlock(
                                new ExpressionBuilder(f, calendar)
                                    .method("setTime", value)
                                    .toStatement(),
                                new ExpressionBuilder(f, datetime)
                                    .method("setElapsedSeconds", new TypeBuilder(f, context.resolve(DateUtil.class))
                                        .method("getSecondFromCalendar", calendar)
                                        .toExpression())
                                    .toStatement(),
                                new ExpressionBuilder(f, object)
                                    .method(context.getValueSetterName(property), datetime)
                                    .toStatement()),
                        f.newBlock(setNullToProperty(object, property))));
                break;
            default:
                throw new AssertionError(kind);
            }
            return f.newIfStatement(
                    new ExpressionBuilder(f, position)
                        .apply(InfixOperator.NOT_EQUALS, Models.toLiteral(f, 0))
                        .toExpression(),
                    f.newBlock(statements));
        }

        private Statement createResultSetNullMapping(
                Expression object,
                Expression resultSet,
                PropertyDeclaration property) {
            return f.newIfStatement(
                    new ExpressionBuilder(f, resultSet)
                        .method("wasNull")
                        .toExpression(),
                    f.newBlock(setNullToProperty(object, property)));
        }

        private ExpressionStatement setNullToProperty(
                Expression object,
                PropertyDeclaration property) {
            assert object != null;
            assert property != null;
            return new ExpressionBuilder(f, object)
                .method(context.getOptionSetterName(property), Models.toNullLiteral(f))
                .toStatement();
        }

        private Statement createPreparedStatementSupportStatement(
                Expression object,
                Expression statement,
                Expression position,
                PropertyDeclaration property) {
            assert object != null;
            assert statement != null;
            assert position != null;
            assert property != null;
            return f.newIfStatement(
                    new ExpressionBuilder(f, position)
                        .apply(InfixOperator.NOT_EQUALS, Models.toLiteral(f, 0))
                        .toExpression(),
                    f.newBlock(f.newIfStatement(
                            new ExpressionBuilder(f, object)
                                .method(context.getOptionGetterName(property))
                                .method("isNull")
                                .toExpression(),
                            f.newBlock(new ExpressionBuilder(f, statement)
                                .method("setNull", position, createNullType(property))
                                .toStatement()),
                            f.newBlock(createParameterSetter(object, statement, position, property)))));
        }

        private List<Statement> createParameterSetter(
                Expression object,
                Expression statement,
                Expression position,
                PropertyDeclaration property) {
            assert object != null;
            assert statement != null;
            assert position != null;
            assert property != null;
            List<Statement> statements = new ArrayList<Statement>();
            SimpleName date = f.newSimpleName(NAME_DATE);
            SimpleName calendar = f.newSimpleName(NAME_CALENDAR);
            SimpleName datetime = f.newSimpleName(NAME_DATETIME);
            BasicTypeKind kind = toBasicKind(property.getType());
            switch (kind) {
            case INT:
                statements.add(createParameterMapping(object, statement, position, property, "setInt"));
                break;
            case LONG:
                statements.add(createParameterMapping(object, statement, position, property, "setLong"));
                break;
            case FLOAT:
                statements.add(createParameterMapping(object, statement, position, property, "setFloat"));
                break;
            case DOUBLE:
                statements.add(createParameterMapping(object, statement, position, property, "setDouble"));
                break;
            case BYTE:
                statements.add(createParameterMapping(object, statement, position, property, "setByte"));
                break;
            case SHORT:
                statements.add(createParameterMapping(object, statement, position, property, "setShort"));
                break;
            case BOOLEAN:
                statements.add(createParameterMapping(object, statement, position, property, "setBoolean"));
                break;
            case DECIMAL:
                statements.add(createParameterMapping(object, statement, position, property, "setBigDecimal"));
                break;
            case TEXT:
                statements.add(new ExpressionBuilder(f, statement)
                    .method("setString", position, new ExpressionBuilder(f, object)
                        .method(context.getValueGetterName(property))
                        .method("toString")
                        .toExpression())
                    .toStatement());
                break;
            case DATE:
                statements.add(new TypeBuilder(f, context.resolve(DateUtil.class))
                    .method("setDayToCalendar",
                            new ExpressionBuilder(f, object)
                                .method(context.getValueGetterName(property))
                                .method("getElapsedDays")
                                .toExpression(),
                            calendar)
                    .toStatement());
                statements.add(new ExpressionBuilder(f, date)
                    .method("setTime", new ExpressionBuilder(f, calendar)
                        .method("getTimeInMillis")
                        .toExpression())
                    .toStatement());
                statements.add(new ExpressionBuilder(f, statement)
                    .method("setDate", position, date)
                    .toStatement());
                break;
            case DATETIME:
                statements.add(new TypeBuilder(f, context.resolve(DateUtil.class))
                    .method("setSecondToCalendar",
                            new ExpressionBuilder(f, object)
                                .method(context.getValueGetterName(property))
                                .method("getElapsedSeconds")
                                .toExpression(),
                            calendar)
                    .toStatement());
                statements.add(new ExpressionBuilder(f, datetime)
                    .method("setTime", new ExpressionBuilder(f, calendar)
                        .method("getTimeInMillis")
                        .toExpression())
                    .toStatement());
                statements.add(new ExpressionBuilder(f, statement)
                    .method("setTimestamp", position, datetime)
                    .toStatement());
                break;
            default:
                throw new AssertionError(kind);
            }
            return statements;
        }

        private Expression createNullType(PropertyDeclaration property) {
            assert property != null;
            return new TypeBuilder(f, context.resolve(Types.class))
                .field(getJdbcTypeName(property))
                .toExpression();
        }

        private String getJdbcTypeName(PropertyDeclaration property) {
            assert property != null;
            BasicTypeKind kind = toBasicKind(property.getType());
            switch (kind) {
            case INT:
                return "INTEGER";
            case LONG:
                return "BIGINT";
            case FLOAT:
                return "FLOAT";
            case DOUBLE:
                return "DOUBLE";
            case BYTE:
                return "TINYINT";
            case SHORT:
                return "SMALLINT";
            case BOOLEAN:
                return "BOOLEAN";
            case DECIMAL:
                return "DECIMAL";
            case TEXT:
                return "VARCHAR";
            case DATE:
                return "DATE";
            case DATETIME:
                return "TIMESTAMP";
            default:
                throw new AssertionError(kind);
            }
        }

        private Statement createParameterMapping(
                Expression object,
                Expression statement,
                Expression position,
                PropertyDeclaration property,
                String name) {
            assert object != null;
            assert statement != null;
            assert position != null;
            assert property != null;
            assert name != null;
            return new ExpressionBuilder(f, statement)
                .method(name, position, new ExpressionBuilder(f, object)
                    .method(context.getValueGetterName(property))
                    .toExpression())
                .toStatement();
        }

        private ExpressionStatement createResultSetMapping(
                Expression object,
                Expression resultSet,
                Expression position,
                PropertyDeclaration property,
                String name) {
            assert object != null;
            assert resultSet != null;
            assert position != null;
            assert property != null;
            assert name != null;
            return new ExpressionBuilder(f, object)
                .method(context.getValueSetterName(property), new ExpressionBuilder(f, resultSet)
                    .method(name, position)
                    .toExpression())
                .toStatement();
        }

        private BasicTypeKind toBasicKind(Type type) {
            assert type instanceof BasicType;
            BasicType basicType = (BasicType) type;
            return basicType.getKind();
        }

        private ExpressionStatement mapField(SimpleName name) {
            return new ExpressionBuilder(f, f.newThis())
                .field(name)
                .assignFrom(name)
                .toStatement();
        }

        private FieldDeclaration createPrivateField(Class<?> type, SimpleName name, boolean newInstance) {
            Expression initializer;
            if (newInstance) {
                initializer = new TypeBuilder(f, context.resolve(type))
                    .newObject()
                    .toExpression();
            } else {
                initializer = null;
            }
            return f.newFieldDeclaration(
                    null,
                    new AttributeBuilder(f)
                        .Private()
                        .Final()
                        .toAttributes(),
                    context.resolve(type),
                    name,
                    initializer);
        }
    }
}
