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
package com.asakusafw.dmdl.directio.hive.common;

import java.io.IOException;
import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import org.apache.hadoop.hive.serde2.typeinfo.TypeInfo;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.asakusafw.directio.hive.annotation.HiveField;
import com.asakusafw.directio.hive.annotation.HiveTable;
import com.asakusafw.directio.hive.serde.DataModelDescriptor;
import com.asakusafw.directio.hive.serde.DataModelDescriptorBuilder;
import com.asakusafw.directio.hive.serde.PropertyDescriptor;
import com.asakusafw.directio.hive.serde.StringValueSerdeFactory;
import com.asakusafw.directio.hive.serde.TimestampValueSerdeFactory;
import com.asakusafw.directio.hive.serde.ValueSerdeFactory;
import com.asakusafw.dmdl.java.emitter.EmitContext;
import com.asakusafw.dmdl.java.spi.JavaDataModelDriver;
import com.asakusafw.dmdl.model.AstDescription;
import com.asakusafw.dmdl.semantics.ModelDeclaration;
import com.asakusafw.dmdl.semantics.PropertyDeclaration;
import com.asakusafw.dmdl.semantics.type.BasicType;
import com.asakusafw.runtime.value.ValueOption;
import com.asakusafw.utils.java.model.syntax.Annotation;
import com.asakusafw.utils.java.model.syntax.ClassBody;
import com.asakusafw.utils.java.model.syntax.ClassDeclaration;
import com.asakusafw.utils.java.model.syntax.ConstructorDeclaration;
import com.asakusafw.utils.java.model.syntax.Expression;
import com.asakusafw.utils.java.model.syntax.FieldDeclaration;
import com.asakusafw.utils.java.model.syntax.InitializerDeclaration;
import com.asakusafw.utils.java.model.syntax.MethodDeclaration;
import com.asakusafw.utils.java.model.syntax.ModelFactory;
import com.asakusafw.utils.java.model.syntax.Name;
import com.asakusafw.utils.java.model.syntax.QualifiedName;
import com.asakusafw.utils.java.model.syntax.SimpleName;
import com.asakusafw.utils.java.model.syntax.Statement;
import com.asakusafw.utils.java.model.syntax.TypeBodyDeclaration;
import com.asakusafw.utils.java.model.util.AttributeBuilder;
import com.asakusafw.utils.java.model.util.ExpressionBuilder;
import com.asakusafw.utils.java.model.util.JavadocBuilder;
import com.asakusafw.utils.java.model.util.Models;
import com.asakusafw.utils.java.model.util.TypeBuilder;

/**
 * Emits a factory class of {@link DataModelDescriptor}.
 * The generated class will have {@code "public static DataModelDescriptor get()"} method.
 * @since 0.7.0
 */
public class HiveDataModelEmitter extends JavaDataModelDriver {

    static final Logger LOG = LoggerFactory.getLogger(HiveDataModelEmitter.class);

    static final String PATTERN_FACTORY = "{0}DescriptorFactory"; //$NON-NLS-1$

    /**
     * Category name for Hive common.
     */
    public static final String CATEGORY = "hive.common"; //$NON-NLS-1$

    /**
     * The getter method name.
     */
    public static final String NAME_GETTER_METHOD = "get"; //$NON-NLS-1$

    /**
     * Returns the generated class name for this.
     * @param context the current context
     * @param model the target data model
     * @return the factory class name
     */
    public static QualifiedName getClassName(EmitContext context, ModelDeclaration model) {
        return createContext(context, model).getQualifiedTypeName();
    }

    @Override
    public void generateResources(EmitContext context, ModelDeclaration model) throws IOException {
        if (model.getTrait(HiveDataModelTrait.class) == null) {
            return;
        }
        checkPropertyType(model);
        Generator.generate(createContext(context, model), model);
    }

    @Override
    public List<Annotation> getTypeAnnotations(
            EmitContext context, ModelDeclaration model) throws IOException {
        HiveDataModelTrait trait = model.getTrait(HiveDataModelTrait.class);
        if (trait == null) {
            return Collections.emptyList();
        }
        ModelFactory f = context.getModelFactory();
        List<Expression> values = new ArrayList<>();
        for (Namer namer : trait.getDataFormatNamers()) {
            Name name = namer.computeName(context, model);
            values.add(f.newClassLiteral(context.resolve(name)));
        }
        return new AttributeBuilder(f)
            .annotation(context.resolve(HiveTable.class), f.newArrayInitializer(values))
            .toAnnotations();
    }

    @Override
    public List<Annotation> getMemberAnnotations(
            EmitContext context, PropertyDeclaration property) throws IOException {
        ModelDeclaration model = property.getOwner().findDeclaration();
        if (model == null) {
            return Collections.emptyList();
        }
        if (model.getTrait(HiveDataModelTrait.class) == null) {
            return Collections.emptyList();
        }
        String name = HiveFieldTrait.getColumnName(property);
        TypeInfo type = HiveFieldTrait.getTypeInfo(property);
        boolean present = HiveFieldTrait.get(property).isColumnPresent();
        ModelFactory f = context.getModelFactory();
        return new AttributeBuilder(f)
            .annotation(context.resolve(HiveField.class),
                    "name", Models.toLiteral(f, name), //$NON-NLS-1$
                    "type", Models.toLiteral(f, type.getQualifiedName()), //$NON-NLS-1$
                    "ignore", Models.toLiteral(f, present == false)) //$NON-NLS-1$
            .toAnnotations();
    }

    private void checkPropertyType(ModelDeclaration model) throws IOException {
        assert model != null;
        for (PropertyDeclaration prop : model.getDeclaredProperties()) {
            HiveFieldTrait trait = HiveFieldTrait.get(prop);
            if (trait.isColumnPresent()) {
                com.asakusafw.dmdl.semantics.Type type = prop.getType();
                if ((type instanceof BasicType) == false) {
                    throw new IOException(MessageFormat.format(
                            Messages.getString("HiveDataModelEmitter.diagnosticUnsupportedPropertyType"), //$NON-NLS-1$
                            type,
                            prop.getOwner().getName().identifier,
                            prop.getName().identifier));
                }
            }
        }
    }

    private static EmitContext createContext(EmitContext context, ModelDeclaration model) {
        return new EmitContext(
                context.getSemantics(),
                context.getConfiguration(),
                model,
                CATEGORY,
                PATTERN_FACTORY);
    }

    static Expression computeValueType(EmitContext context, PropertyDeclaration property) {
        ModelFactory f = context.getModelFactory();
        HiveFieldTrait field = HiveFieldTrait.get(property);
        switch (field.getTypeKind()) {
        case NATURAL:
            return new TypeBuilder(f, context.resolve(ValueSerdeFactory.class))
                .field(getValueSerdeNaturalName(f, (BasicType) property.getType()))
                .toExpression();
        case STRING:
            return new TypeBuilder(f, context.resolve(StringValueSerdeFactory.class))
                .field(getValueSerdeStringName(f, (BasicType) property.getType()))
                .toExpression();
        case TIMESTAMP:
            return new TypeBuilder(f, context.resolve(TimestampValueSerdeFactory.class))
                .field(getValueSerdeTimestampName(f, (BasicType) property.getType()))
                .toExpression();
        case CHAR:
            return new TypeBuilder(f, context.resolve(ValueSerdeFactory.class))
                .method("getChar", Models.toLiteral(f, field.getStringLength())) //$NON-NLS-1$
                .toExpression();
        case VARCHAR:
            return new TypeBuilder(f, context.resolve(ValueSerdeFactory.class))
                .method("getVarchar", Models.toLiteral(f, field.getStringLength())) //$NON-NLS-1$
                .toExpression();
        case DECIMAL:
            return new TypeBuilder(f, context.resolve(ValueSerdeFactory.class))
                .method("getDecimal", //$NON-NLS-1$
                        Models.toLiteral(f, field.getDecimalPrecision()),
                        Models.toLiteral(f, field.getDecimalScale()))
                .toExpression();
        default:
            throw new AssertionError(field.getTypeKind());
        }
    }

    private static SimpleName getValueSerdeNaturalName(ModelFactory f, BasicType type) {
        Class<?> valueClass = EmitContext.getFieldTypeAsClass(type.getKind());
        Enum<?> element = ValueSerdeFactory.fromClass(valueClass);
        return f.newSimpleName(element.name());
    }

    private static SimpleName getValueSerdeStringName(ModelFactory f, BasicType type) {
        Class<?> valueClass = EmitContext.getFieldTypeAsClass(type.getKind());
        Enum<?> element = StringValueSerdeFactory.fromClass(valueClass);
        return f.newSimpleName(element.name());
    }

    private static SimpleName getValueSerdeTimestampName(ModelFactory f, BasicType type) {
        Class<?> valueClass = EmitContext.getFieldTypeAsClass(type.getKind());
        Enum<?> element = TimestampValueSerdeFactory.fromClass(valueClass);
        return f.newSimpleName(element.name());
    }

    private static final class Generator {

        private static final String NAME_SINGLETON_FIELD = "SINGLETON"; //$NON-NLS-1$

        private final EmitContext context;

        private final ModelDeclaration model;

        private final ModelFactory f;

        private Generator(EmitContext context, ModelDeclaration model) {
            this.context = context;
            this.model = model;
            this.f = context.getModelFactory();
        }

        static void generate(EmitContext context, ModelDeclaration model) throws IOException {
            new Generator(context, model).emit();
        }

        private void emit() throws IOException {
            ClassDeclaration decl = f.newClassDeclaration(
                    new JavadocBuilder(f)
                        .inline("Hive table information for {0}.",
                                d -> d.linkType(context.resolve(model.getSymbol())))
                        .toJavadoc(),
                    new AttributeBuilder(f)
                        .Public()
                        .Final()
                        .toAttributes(),
                    context.getTypeName(),
                    Collections.emptyList(),
                    null,
                    Collections.emptyList(),
                    createMembers());
            context.emit(decl);
        }

        private List<TypeBodyDeclaration> createMembers() {
            List<TypeBodyDeclaration> results = new ArrayList<>();
            results.add(createSingletonField());
            results.add(createStaticInitializer());
            results.add(createConstructor());
            results.add(createGetterMethod());
            return results;
        }

        private FieldDeclaration createSingletonField() {
            return f.newFieldDeclaration(
                    null,
                    new AttributeBuilder(f)
                        .Private()
                        .Static()
                        .Final()
                        .toAttributes(),
                    context.resolve(DataModelDescriptor.class),
                    f.newSimpleName(NAME_SINGLETON_FIELD),
                    null);
        }

        private InitializerDeclaration createStaticInitializer() {
            List<Statement> statements = new ArrayList<>();
            SimpleName builder = f.newSimpleName("builder"); //$NON-NLS-1$
            statements.add(new TypeBuilder(f, context.resolve(DataModelDescriptorBuilder.class))
                    .newObject(f.newClassLiteral(context.resolve(model.getSymbol())))
                    .toLocalVariableDeclaration(context.resolve(DataModelDescriptorBuilder.class), builder));
            AstDescription description = model.getDescription();
            if (description != null) {
                statements.add(new ExpressionBuilder(f, builder)
                        .method("comment", Models.toLiteral(f, description.getText())) //$NON-NLS-1$
                        .toStatement());
            }
            for (PropertyDeclaration property : model.getDeclaredProperties()) {
                HiveFieldTrait field = HiveFieldTrait.get(property);
                if (field.isColumnPresent() == false) {
                    continue;
                }
                Expression descriptor = createNewPropertyDescriptor(property);
                statements.add(new ExpressionBuilder(f, builder)
                    .method("property", descriptor) //$NON-NLS-1$
                    .toStatement());
            }
            statements.add(new ExpressionBuilder(f, f.newSimpleName(NAME_SINGLETON_FIELD))
                    .assignFrom(new ExpressionBuilder(f, builder)
                            .method("build") //$NON-NLS-1$
                            .toExpression())
                    .toStatement());
            return f.newInitializerDeclaration(
                    null,
                    new AttributeBuilder(f).Static().toAttributes(),
                    f.newBlock(statements));
        }

        private Expression createNewPropertyDescriptor(PropertyDeclaration property) {
            Expression columnName = Models.toLiteral(f, HiveFieldTrait.getColumnName(property));
            Expression typeDesc = computeValueType(property);
            Expression comment = null;
            AstDescription description = property.getDescription();
            if (description == null) {
                comment = Models.toNullLiteral(f);
            } else {
                comment = Models.toLiteral(f, description.getText());
            }
            SimpleName dataModel = f.newSimpleName("dataModel"); //$NON-NLS-1$
            ClassBody block = f.newClassBody(Arrays.asList(f.newMethodDeclaration(
                    null,
                    new AttributeBuilder(f)
                        .annotation(context.resolve(Override.class))
                        .Public()
                        .toAttributes(),
                    new TypeBuilder(f, context.resolve(ValueOption.class))
                        .parameterize(f.newWildcard())
                        .toType(),
                    f.newSimpleName("extract"), //$NON-NLS-1$
                    Arrays.asList(f.newFormalParameterDeclaration(context.resolve(Object.class), dataModel)),
                    Arrays.asList(new ExpressionBuilder(f, dataModel)
                            .castTo(context.resolve(model.getSymbol()))
                            .method(context.getOptionGetterName(property))
                            .toReturnStatement()))));
            return new TypeBuilder(f, context.resolve(PropertyDescriptor.class))
                .newObject(Arrays.asList(columnName, typeDesc, comment), block)
                .toExpression();
        }

        private Expression computeValueType(PropertyDeclaration property) {
            return HiveDataModelEmitter.computeValueType(context, property);
        }

        private ConstructorDeclaration createConstructor() {
            return f.newConstructorDeclaration(
                    null,
                    new AttributeBuilder(f).Private().toAttributes(),
                    context.getTypeName(),
                    Collections.emptyList(),
                    Arrays.asList(f.newReturnStatement()));
        }

        private MethodDeclaration createGetterMethod() {
            return f.newMethodDeclaration(
                    new JavadocBuilder(f)
                        .inline("Returns a data model descriptor for {0}.",
                                d -> d.linkType(context.resolve(model.getSymbol())))
                        .returns()
                            .text("the descriptor object")
                        .toJavadoc(),
                    new AttributeBuilder(f)
                        .Public()
                        .Static()
                        .toAttributes(),
                    context.resolve(DataModelDescriptor.class),
                    f.newSimpleName(NAME_GETTER_METHOD),
                    Collections.emptyList(),
                    Arrays.asList(new ExpressionBuilder(f, f.newSimpleName(NAME_SINGLETON_FIELD))
                            .toReturnStatement()));
        }
    }
}
