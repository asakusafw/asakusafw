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
package com.asakusafw.dmdl.windgate.jdbc.driver;

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
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

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
import com.asakusafw.dmdl.windgate.util.JdbcProcessDescriptionGenerator;
import com.asakusafw.runtime.value.Date;
import com.asakusafw.runtime.value.DateTime;
import com.asakusafw.runtime.value.DateUtil;
import com.asakusafw.utils.java.model.syntax.ClassDeclaration;
import com.asakusafw.utils.java.model.syntax.Expression;
import com.asakusafw.utils.java.model.syntax.ExpressionStatement;
import com.asakusafw.utils.java.model.syntax.FieldDeclaration;
import com.asakusafw.utils.java.model.syntax.InfixOperator;
import com.asakusafw.utils.java.model.syntax.MethodDeclaration;
import com.asakusafw.utils.java.model.syntax.ModelFactory;
import com.asakusafw.utils.java.model.syntax.Name;
import com.asakusafw.utils.java.model.syntax.PostfixOperator;
import com.asakusafw.utils.java.model.syntax.SimpleName;
import com.asakusafw.utils.java.model.syntax.Statement;
import com.asakusafw.utils.java.model.syntax.TypeBodyDeclaration;
import com.asakusafw.utils.java.model.util.AttributeBuilder;
import com.asakusafw.utils.java.model.util.ExpressionBuilder;
import com.asakusafw.utils.java.model.util.JavadocBuilder;
import com.asakusafw.utils.java.model.util.Models;
import com.asakusafw.utils.java.model.util.TypeBuilder;
import com.asakusafw.windgate.core.vocabulary.DataModelJdbcSupport;
import com.asakusafw.windgate.core.vocabulary.DataModelJdbcSupport.DataModelPreparedStatement;
import com.asakusafw.windgate.core.vocabulary.DataModelJdbcSupport.DataModelResultSet;

/**
 * Emits {@link DataModelJdbcSupport} implementations.
 * @since 0.2.2
 */
public class JdbcSupportEmitter extends JavaDataModelDriver {

    static final Logger LOG = LoggerFactory.getLogger(JdbcSupportEmitter.class);

    /**
     * Category name for JDBC support.
     */
    public static final String CATEGORY_JDBC = "jdbc"; //$NON-NLS-1$

    @Override
    public void generateResources(EmitContext context, ModelDeclaration model) throws IOException {
        if (isTarget(model) == false) {
            return;
        }
        checkColumnExists(model);
        checkColumnType(model);
        checkColumnConflict(model);
        Name supportClassName = generateSupport(context, model);
        if (hasTableTrait(model)) {
            generateImporterDescription(context, model, supportClassName);
            generateExporterDescription(context, model, supportClassName);
        }
    }

    private Name generateSupport(EmitContext context, ModelDeclaration model) throws IOException {
        assert context != null;
        assert model != null;
        EmitContext next = new EmitContext(
                context.getSemantics(),
                context.getConfiguration(),
                model,
                CATEGORY_JDBC,
                "{0}JdbcSupport"); //$NON-NLS-1$
        LOG.debug("Generating JDBC support for {}", //$NON-NLS-1$
                context.getQualifiedTypeName().toNameString());
        SupportGenerator.emit(next, model);
        LOG.debug("Generated JDBC support for {}: {}", //$NON-NLS-1$
                context.getQualifiedTypeName().toNameString(),
                next.getQualifiedTypeName().toNameString());
        return next.getQualifiedTypeName();
    }

    private void generateImporterDescription(
            EmitContext context,
            ModelDeclaration model,
            Name supportName) throws IOException {
        assert context != null;
        assert model != null;
        assert supportName != null;
        EmitContext next = new EmitContext(
                context.getSemantics(),
                context.getConfiguration(),
                model,
                CATEGORY_JDBC,
                "Abstract{0}JdbcImporterDescription"); //$NON-NLS-1$
        JdbcProcessDescriptionGenerator.Description desc = new JdbcProcessDescriptionGenerator.Description(
                Messages.getString("JdbcSupportEmitter.javadocImporter"), //$NON-NLS-1$
                context.getQualifiedTypeName());
        desc.setTableName(getTableName(model));
        desc.setColumnNames(getColumnNames(model));
        desc.setSupportClassName(supportName);
        JdbcProcessDescriptionGenerator.generateImporter(next, desc);
    }

    private void generateExporterDescription(
            EmitContext context,
            ModelDeclaration model,
            Name supportName) throws IOException {
        assert context != null;
        assert model != null;
        assert supportName != null;
        EmitContext next = new EmitContext(
                context.getSemantics(),
                context.getConfiguration(),
                model,
                CATEGORY_JDBC,
                "Abstract{0}JdbcExporterDescription"); //$NON-NLS-1$
        JdbcProcessDescriptionGenerator.Description desc = new JdbcProcessDescriptionGenerator.Description(
                Messages.getString("JdbcSupportEmitter.javadocExporter"), //$NON-NLS-1$
                context.getQualifiedTypeName());
        desc.setTableName(getTableName(model));
        desc.setColumnNames(getColumnNames(model));
        desc.setSupportClassName(supportName);
        JdbcProcessDescriptionGenerator.generateExporter(next, desc);
    }

    private boolean isTarget(ModelDeclaration model) {
        assert model != null;
        return hasTableTrait(model) || hasColumnTrait(model);
    }

    private boolean hasTableTrait(ModelDeclaration model) {
        assert model != null;
        return model.getTrait(JdbcTableTrait.class) != null;
    }

    private boolean hasColumnTrait(ModelDeclaration model) {
        assert model != null;
        boolean sawTrait = false;
        for (PropertyDeclaration prop : model.getDeclaredProperties()) {
            if (prop.getTrait(JdbcColumnTrait.class) != null) {
                sawTrait = true;
            }
        }
        return sawTrait;
    }

    private String getTableName(ModelDeclaration model) {
        assert hasTableTrait(model);
        return model.getTrait(JdbcTableTrait.class).getName();
    }

    private List<String> getColumnNames(ModelDeclaration model) {
        List<String> results = new ArrayList<>();
        for (PropertyDeclaration property : model.getDeclaredProperties()) {
            JdbcColumnTrait columnTrait = property.getTrait(JdbcColumnTrait.class);
            if (columnTrait != null) {
                results.add(columnTrait.getName());
            }
        }
        return results;
    }

    private void checkColumnExists(ModelDeclaration model) throws IOException {
        assert model != null;
        if (hasColumnTrait(model) == false) {
            throw new IOException(MessageFormat.format(
                    Messages.getString("JdbcSupportEmitter.errorNoColumns"), //$NON-NLS-1$
                    model.getName().identifier,
                    JdbcColumnDriver.TARGET_NAME));
        }
    }

    private void checkColumnType(ModelDeclaration model) throws IOException {
        assert model != null;
        for (PropertyDeclaration prop : model.getDeclaredProperties()) {
            if (model.getTrait(JdbcColumnTrait.class) == null) {
                continue;
            }
            Type type = prop.getType();
            if ((type instanceof BasicType) == false) {
                throw new IOException(MessageFormat.format(
                        Messages.getString("JdbcSupportEmitter.errorUnsupportedType"), //$NON-NLS-1$
                        type,
                        model.getName().identifier,
                        prop.getName().identifier));
            }
        }
    }

    private void checkColumnConflict(ModelDeclaration model) throws IOException {
        assert model != null;
        Map<String, PropertyDeclaration> saw = new HashMap<>();
        for (PropertyDeclaration prop : model.getDeclaredProperties()) {
            JdbcColumnTrait trait = prop.getTrait(JdbcColumnTrait.class);
            if (trait == null) {
                continue;
            }
            String name = trait.getName();
            if (saw.containsKey(name)) {
                PropertyDeclaration other = saw.get(name);
                throw new IOException(MessageFormat.format(
                        Messages.getString("JdbcSupportEmitter.errorConflictColumnName"), //$NON-NLS-1$
                        name,
                        model.getName().identifier,
                        prop.getName().identifier,
                        other.getName().identifier));
            }
            saw.put(name, prop);
        }
    }

    private static final class SupportGenerator {

        private static final String NAME_CALENDAR = "calendar"; //$NON-NLS-1$

        private static final String NAME_DATETIME = "datetime"; //$NON-NLS-1$

        private static final String NAME_DATE = "date"; //$NON-NLS-1$

        private static final String NAME_TEXT = "text"; //$NON-NLS-1$

        private static final String NAME_RESULT_SET_SUPPORT = "ResultSetSupport"; //$NON-NLS-1$

        private static final String NAME_PREPARED_STATEMENT_SUPPORT = "PreparedStatementSupport"; //$NON-NLS-1$

        private static final String NAME_PROPERTY_POSITIONS = "PROPERTY_POSITIONS"; //$NON-NLS-1$

        private static final String NAME_COLUMN_MAP = "COLUMN_MAP"; //$NON-NLS-1$

        private static final String NAME_CREATE_VECTOR = "createPropertyVector"; //$NON-NLS-1$

        private final EmitContext context;

        private final ModelDeclaration model;

        private final ModelFactory f;

        private SupportGenerator(EmitContext context, ModelDeclaration model) {
            assert context != null;
            assert model != null;
            this.context = context;
            this.model = model;
            this.f = context.getModelFactory();
        }

        static void emit(EmitContext context, ModelDeclaration model) throws IOException {
            assert context != null;
            assert model != null;
            SupportGenerator emitter = new SupportGenerator(context, model);
            emitter.emit();
        }

        private void emit() throws IOException {
            ClassDeclaration decl = f.newClassDeclaration(
                    new JavadocBuilder(f)
                        .inline(Messages.getString("JdbcSupportEmitter.javadocClass"), //$NON-NLS-1$
                                d -> d.linkType(context.resolve(model.getSymbol())))
                        .toJavadoc(),
                    new AttributeBuilder(f)
                        .Public()
                        .toAttributes(),
                    context.getTypeName(),
                    Collections.emptyList(),
                    null,
                    Collections.singletonList(f.newParameterizedType(
                            context.resolve(DataModelJdbcSupport.class),
                            context.resolve(model.getSymbol()))),
                    createMembers());
            context.emit(decl);
        }

        private List<TypeBodyDeclaration> createMembers() {
            List<TypeBodyDeclaration> results = new ArrayList<>();
            results.addAll(createPropertyPositions());
            results.addAll(createColumnMap());
            results.add(createGetSupportedType());
            results.add(createIsSupported());
            results.add(createGetColumnMap());
            results.add(createCreateResultSetSupport());
            results.add(createCreatePreparedStatementSupport());
            results.add(createCreatePropertyVector());
            results.add(createResultSetSupportClass());
            results.add(createPreparedStatementSupportClass());
            return results;
        }

        private List<TypeBodyDeclaration> createPropertyPositions() {
            List<TypeBodyDeclaration> results = new ArrayList<>();
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
            List<Statement> statements = new ArrayList<>();
            SimpleName map = f.newSimpleName("map"); //$NON-NLS-1$
            statements.add(new TypeBuilder(f, context.resolve(LinkedHashMap.class))
                .parameterize()
                .newObject()
                .toLocalVariableDeclaration(
                        f.newParameterizedType(
                                context.resolve(Map.class),
                                context.resolve(String.class),
                                context.resolve(Integer.class)),
                        map));

            List<PropertyDeclaration> properties = getProperties();
            for (int i = 0, n = properties.size(); i < n; i++) {
                PropertyDeclaration property = properties.get(i);
                JdbcColumnTrait trait = property.getTrait(JdbcColumnTrait.class);
                assert trait != null;
                statements.add(new ExpressionBuilder(f, map)
                    .method("put", Models.toLiteral(f, trait.getName()), Models.toLiteral(f, i)) //$NON-NLS-1$
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

        private List<TypeBodyDeclaration> createColumnMap() {
            List<TypeBodyDeclaration> results = new ArrayList<>();
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
                            context.resolve(String.class)),
                    f.newSimpleName(NAME_COLUMN_MAP),
                    null));
            List<Statement> statements = new ArrayList<>();
            SimpleName map = f.newSimpleName("map"); //$NON-NLS-1$
            statements.add(new TypeBuilder(f, context.resolve(LinkedHashMap.class))
                .parameterize()
                .newObject()
                .toLocalVariableDeclaration(
                        f.newParameterizedType(
                                context.resolve(Map.class),
                                context.resolve(String.class),
                                context.resolve(String.class)),
                        map));

            for (PropertyDeclaration property : getProperties()) {
                JdbcColumnTrait trait = property.getTrait(JdbcColumnTrait.class);
                assert trait != null;
                statements.add(new ExpressionBuilder(f, map)
                    .method("put",
                            Models.toLiteral(f, trait.getName()),
                            Models.toLiteral(f, property.getName().identifier))
                    .toStatement());
            }
            statements.add(new ExpressionBuilder(f, f.newSimpleName(NAME_COLUMN_MAP))
                .assignFrom(new TypeBuilder(f, context.resolve(Collections.class))
                        .method("unmodifiableMap", map) //$NON-NLS-1$
                        .toExpression())
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
                    f.newSimpleName("getSupportedType"), //$NON-NLS-1$
                    Collections.emptyList(),
                    Arrays.asList(new Statement[] {
                            new TypeBuilder(f, context.resolve(model.getSymbol()))
                                .dotClass()
                                .toReturnStatement()
                    }));
            return decl;
        }

        private MethodDeclaration createIsSupported() {
            SimpleName columnNames = f.newSimpleName("columnNames"); //$NON-NLS-1$
            List<Statement> statements = new ArrayList<>();
            statements.add(createNullCheck(columnNames));
            statements.add(f.newIfStatement(
                    new ExpressionBuilder(f, columnNames)
                        .method("isEmpty") //$NON-NLS-1$
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
                                    f.newSimpleName("e")), //$NON-NLS-1$
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
                    f.newSimpleName("isSupported"), //$NON-NLS-1$
                    Arrays.asList(f.newFormalParameterDeclaration(
                            f.newParameterizedType(
                                    context.resolve(List.class),
                                    context.resolve(String.class)),
                            columnNames)),
                    statements);
            return decl;
        }

        private MethodDeclaration createGetColumnMap() {
            MethodDeclaration decl = f.newMethodDeclaration(
                    null,
                    new AttributeBuilder(f)
                        .annotation(context.resolve(Override.class))
                        .Public()
                        .toAttributes(),
                    f.newParameterizedType(
                            context.resolve(Map.class),
                            context.resolve(String.class),
                            context.resolve(String.class)),
                    f.newSimpleName("getColumnMap"), //$NON-NLS-1$
                    Collections.emptyList(),
                    Arrays.asList(new ExpressionBuilder(f, f.newSimpleName(NAME_COLUMN_MAP))
                            .toReturnStatement()));
            return decl;
        }

        private MethodDeclaration createCreateResultSetSupport() {
            SimpleName resultSet = f.newSimpleName("resultSet"); //$NON-NLS-1$
            SimpleName columnNames = f.newSimpleName("columnNames"); //$NON-NLS-1$
            List<Statement> statements = new ArrayList<>();
            statements.add(createNullCheck(resultSet));
            statements.add(createNullCheck(columnNames));
            SimpleName vector = f.newSimpleName("vector"); //$NON-NLS-1$
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
                    f.newSimpleName("createResultSetSupport"), //$NON-NLS-1$
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
            SimpleName preparedStatement = f.newSimpleName("statement"); //$NON-NLS-1$
            SimpleName columnNames = f.newSimpleName("columnNames"); //$NON-NLS-1$
            List<Statement> statements = new ArrayList<>();
            statements.add(createNullCheck(preparedStatement));
            statements.add(createNullCheck(columnNames));
            SimpleName vector = f.newSimpleName("vector"); //$NON-NLS-1$
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
                    f.newSimpleName("createPreparedStatementSupport"), //$NON-NLS-1$
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
                                "{0} must not be null", //$NON-NLS-1$
                                parameter.getToken())))
                        .toThrowStatement()));
        }

        private MethodDeclaration createCreatePropertyVector() {
            SimpleName columnNames = f.newSimpleName("columnNames"); //$NON-NLS-1$
            SimpleName vector = f.newSimpleName("vector"); //$NON-NLS-1$
            List<Statement> statements = new ArrayList<>();

            statements.add(new TypeBuilder(f, context.resolve(int[].class))
                .newArray(new ExpressionBuilder(f, f.newSimpleName(NAME_PROPERTY_POSITIONS))
                    .method("size") //$NON-NLS-1$
                    .toExpression())
                .toLocalVariableDeclaration(context.resolve(int[].class), vector));

            SimpleName index = f.newSimpleName("i"); //$NON-NLS-1$
            SimpleName column = f.newSimpleName("column"); //$NON-NLS-1$
            SimpleName position = f.newSimpleName("position"); //$NON-NLS-1$
            statements.add(f.newForStatement(
                    f.newLocalVariableDeclaration(
                            new AttributeBuilder(f).toAttributes(),
                            context.resolve(int.class),
                            Arrays.asList(
                                    f.newVariableDeclarator(
                                            index,
                                            Models.toLiteral(f, 0)),
                                    f.newVariableDeclarator(
                                            f.newSimpleName("n"), //$NON-NLS-1$
                                            new ExpressionBuilder(f, columnNames)
                                                .method("size") //$NON-NLS-1$
                                                .toExpression()))),
                    new ExpressionBuilder(f, index)
                        .apply(InfixOperator.LESS, f.newSimpleName("n")) //$NON-NLS-1$
                        .toExpression(),
                    f.newStatementExpressionList(new ExpressionBuilder(f, index)
                        .apply(PostfixOperator.INCREMENT)
                        .toExpression()),
                    f.newBlock(new Statement[] {
                            new ExpressionBuilder(f, columnNames)
                                .method("get", index) //$NON-NLS-1$
                                .toLocalVariableDeclaration(context.resolve(String.class), column),
                            new ExpressionBuilder(f, f.newSimpleName(NAME_PROPERTY_POSITIONS))
                                .method("get", column) //$NON-NLS-1$
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
            SimpleName resultSet = f.newSimpleName("resultSet"); //$NON-NLS-1$
            SimpleName properties = f.newSimpleName("properties"); //$NON-NLS-1$
            List<TypeBodyDeclaration> members = new ArrayList<>();
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

            SimpleName object = f.newSimpleName("object"); //$NON-NLS-1$
            List<Statement> statements = new ArrayList<>();
            statements.add(f.newIfStatement(
                    new ExpressionBuilder(f, resultSet)
                        .method("next") //$NON-NLS-1$
                        .apply(InfixOperator.EQUALS, Models.toLiteral(f, false))
                        .toExpression(),
                    f.newBlock(new ExpressionBuilder(f, Models.toLiteral(f, false))
                        .toReturnStatement())));
            List<PropertyDeclaration> declared = getProperties();
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
                    Collections.emptyList(),
                    context.resolve(boolean.class),
                    f.newSimpleName("next"), //$NON-NLS-1$
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
            for (PropertyDeclaration prop : getProperties()) {
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
                        .method("getInstance") //$NON-NLS-1$
                        .toExpression());
        }

        private ClassDeclaration createPreparedStatementSupportClass() {
            SimpleName preparedStatement = f.newSimpleName("statement"); //$NON-NLS-1$
            SimpleName properties = f.newSimpleName("properties"); //$NON-NLS-1$
            List<TypeBodyDeclaration> members = new ArrayList<>();
            members.add(createPrivateField(PreparedStatement.class, preparedStatement, false));
            members.add(createPrivateField(int[].class, properties, false));
            Set<BasicTypeKind> kinds = collectTypeKinds();
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

            SimpleName object = f.newSimpleName("object"); //$NON-NLS-1$
            List<Statement> statements = new ArrayList<>();
            List<PropertyDeclaration> declared = getProperties();
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
                    Collections.emptyList(),
                    context.resolve(void.class),
                    f.newSimpleName("setParameters"), //$NON-NLS-1$
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
            List<Statement> statements = new ArrayList<>();
            SimpleName value = f.newSimpleName("value"); //$NON-NLS-1$
            SimpleName calendar = f.newSimpleName(NAME_CALENDAR);
            SimpleName text = f.newSimpleName(NAME_TEXT);
            SimpleName date = f.newSimpleName(NAME_DATE);
            SimpleName datetime = f.newSimpleName(NAME_DATETIME);
            BasicTypeKind kind = toBasicKind(property.getType());
            switch (kind) {
            case INT:
                statements.add(
                        createResultSetMapping(object, resultSet, position, property, "getInt")); //$NON-NLS-1$
                statements.add(
                        createResultSetNullMapping(object, resultSet, property));
                break;
            case LONG:
                statements.add(
                        createResultSetMapping(object, resultSet, position, property, "getLong")); //$NON-NLS-1$
                statements.add(
                        createResultSetNullMapping(object, resultSet, property));
                break;
            case FLOAT:
                statements.add(
                        createResultSetMapping(object, resultSet, position, property, "getFloat")); //$NON-NLS-1$
                statements.add(
                        createResultSetNullMapping(object, resultSet, property));
                break;
            case DOUBLE:
                statements.add(
                        createResultSetMapping(object, resultSet, position, property, "getDouble")); //$NON-NLS-1$
                statements.add(
                        createResultSetNullMapping(object, resultSet, property));
                break;
            case BYTE:
                statements.add(
                        createResultSetMapping(object, resultSet, position, property, "getByte")); //$NON-NLS-1$
                statements.add(
                        createResultSetNullMapping(object, resultSet, property));
                break;
            case SHORT:
                statements.add(
                        createResultSetMapping(object, resultSet, position, property, "getShort")); //$NON-NLS-1$
                statements.add(
                        createResultSetNullMapping(object, resultSet, property));
                break;
            case BOOLEAN:
                statements.add(
                        createResultSetMapping(object, resultSet, position, property, "getBoolean")); //$NON-NLS-1$
                statements.add(
                        createResultSetNullMapping(object, resultSet, property));
                break;
            case DECIMAL:
                statements.add(
                        createResultSetMapping(object, resultSet, position, property, "getBigDecimal")); //$NON-NLS-1$
                break;
            case TEXT:
                statements.add(new ExpressionBuilder(f, resultSet)
                    .method("getString", position) //$NON-NLS-1$
                    .toLocalVariableDeclaration(context.resolve(String.class), value));
                statements.add(f.newIfStatement(
                        new ExpressionBuilder(f, value)
                            .apply(InfixOperator.NOT_EQUALS, Models.toNullLiteral(f))
                            .toExpression(),
                        f.newBlock(
                                new ExpressionBuilder(f, text)
                                    .method("set", value) //$NON-NLS-1$
                                    .toStatement(),
                                new ExpressionBuilder(f, object)
                                    .method(context.getValueSetterName(property), text)
                                    .toStatement()),
                        f.newBlock(setNullToProperty(object, property))));
                break;
            case DATE:
                statements.add(new ExpressionBuilder(f, resultSet)
                    .method("getDate", position, calendar) //$NON-NLS-1$
                    .toLocalVariableDeclaration(context.resolve(java.sql.Date.class), value));
                statements.add(f.newIfStatement(
                        new ExpressionBuilder(f, value)
                            .apply(InfixOperator.NOT_EQUALS, Models.toNullLiteral(f))
                            .toExpression(),
                        f.newBlock(
                                new ExpressionBuilder(f, calendar)
                                    .method("setTime", value) //$NON-NLS-1$
                                    .toStatement(),
                                new ExpressionBuilder(f, date)
                                    .method("setElapsedDays", //$NON-NLS-1$
                                            new TypeBuilder(f, context.resolve(DateUtil.class))
                                        .method("getDayFromCalendar", calendar) //$NON-NLS-1$
                                        .toExpression())
                                    .toStatement(),
                                new ExpressionBuilder(f, object)
                                    .method(context.getValueSetterName(property), date)
                                    .toStatement()),
                        f.newBlock(setNullToProperty(object, property))));
                break;
            case DATETIME:
                statements.add(new ExpressionBuilder(f, resultSet)
                    .method("getTimestamp", position, calendar) //$NON-NLS-1$
                    .toLocalVariableDeclaration(context.resolve(java.sql.Timestamp.class), value));
                statements.add(f.newIfStatement(
                        new ExpressionBuilder(f, value)
                            .apply(InfixOperator.NOT_EQUALS, Models.toNullLiteral(f))
                            .toExpression(),
                        f.newBlock(
                                new ExpressionBuilder(f, calendar)
                                    .method("setTime", value) //$NON-NLS-1$
                                    .toStatement(),
                                new ExpressionBuilder(f, datetime)
                                    .method("setElapsedSeconds", //$NON-NLS-1$
                                            new TypeBuilder(f, context.resolve(DateUtil.class))
                                        .method("getSecondFromCalendar", calendar) //$NON-NLS-1$
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
                        .method("wasNull") //$NON-NLS-1$
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
                                .method("isNull") //$NON-NLS-1$
                                .toExpression(),
                            f.newBlock(new ExpressionBuilder(f, statement)
                                .method("setNull", position, createNullType(property)) //$NON-NLS-1$
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
            List<Statement> statements = new ArrayList<>();
            SimpleName calendar = f.newSimpleName(NAME_CALENDAR);
            BasicTypeKind kind = toBasicKind(property.getType());
            switch (kind) {
            case INT:
                statements.add(
                        createParameterMapping(object, statement, position, property, "setInt")); //$NON-NLS-1$
                break;
            case LONG:
                statements.add(
                        createParameterMapping(object, statement, position, property, "setLong")); //$NON-NLS-1$
                break;
            case FLOAT:
                statements.add(
                        createParameterMapping(object, statement, position, property, "setFloat")); //$NON-NLS-1$
                break;
            case DOUBLE:
                statements.add(
                        createParameterMapping(object, statement, position, property, "setDouble")); //$NON-NLS-1$
                break;
            case BYTE:
                statements.add(
                        createParameterMapping(object, statement, position, property, "setByte")); //$NON-NLS-1$
                break;
            case SHORT:
                statements.add(
                        createParameterMapping(object, statement, position, property, "setShort")); //$NON-NLS-1$
                break;
            case BOOLEAN:
                statements.add(
                        createParameterMapping(object, statement, position, property, "setBoolean")); //$NON-NLS-1$
                break;
            case DECIMAL:
                statements.add(
                        createParameterMapping(object, statement, position, property, "setBigDecimal")); //$NON-NLS-1$
                break;
            case TEXT:
                statements.add(new ExpressionBuilder(f, statement)
                    .method("setString", position, new ExpressionBuilder(f, object) //$NON-NLS-1$
                        .method(context.getValueGetterName(property))
                        .method("toString") //$NON-NLS-1$
                        .toExpression())
                    .toStatement());
                break;
            case DATE:
                statements.add(new TypeBuilder(f, context.resolve(DateUtil.class))
                    .method("setDayToCalendar", //$NON-NLS-1$
                            new ExpressionBuilder(f, object)
                                .method(context.getValueGetterName(property))
                                .method("getElapsedDays") //$NON-NLS-1$
                                .toExpression(),
                            calendar)
                    .toStatement());
                statements.add(new ExpressionBuilder(f, statement)
                    .method("setDate", position, new TypeBuilder(f, context.resolve(java.sql.Date.class)) //$NON-NLS-1$
                            .newObject(new ExpressionBuilder(f, calendar)
                                    .method("getTimeInMillis") //$NON-NLS-1$
                                    .toExpression())
                            .toExpression())
                    .toStatement());
                break;
            case DATETIME:
                statements.add(new TypeBuilder(f, context.resolve(DateUtil.class))
                    .method("setSecondToCalendar", //$NON-NLS-1$
                            new ExpressionBuilder(f, object)
                                .method(context.getValueGetterName(property))
                                .method("getElapsedSeconds") //$NON-NLS-1$
                                .toExpression(),
                            calendar)
                    .toStatement());
                statements.add(new ExpressionBuilder(f, statement)
                    .method("setTimestamp", position, new TypeBuilder(f, context.resolve(java.sql.Timestamp.class)) //$NON-NLS-1$
                            .newObject(new ExpressionBuilder(f, calendar)
                                    .method("getTimeInMillis") //$NON-NLS-1$
                                    .toExpression())
                            .toExpression())
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
                return "INTEGER"; //$NON-NLS-1$
            case LONG:
                return "BIGINT"; //$NON-NLS-1$
            case FLOAT:
                return "FLOAT"; //$NON-NLS-1$
            case DOUBLE:
                return "DOUBLE"; //$NON-NLS-1$
            case BYTE:
                return "TINYINT"; //$NON-NLS-1$
            case SHORT:
                return "SMALLINT"; //$NON-NLS-1$
            case BOOLEAN:
                return "BOOLEAN"; //$NON-NLS-1$
            case DECIMAL:
                return "DECIMAL"; //$NON-NLS-1$
            case TEXT:
                return "VARCHAR"; //$NON-NLS-1$
            case DATE:
                return "DATE"; //$NON-NLS-1$
            case DATETIME:
                return "TIMESTAMP"; //$NON-NLS-1$
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

        private List<PropertyDeclaration> getProperties() {
            List<PropertyDeclaration> results = new ArrayList<>();
            for (PropertyDeclaration property : model.getDeclaredProperties()) {
                if (property.getTrait(JdbcColumnTrait.class) != null) {
                    results.add(property);
                }
            }
            return results;
        }
    }
}
