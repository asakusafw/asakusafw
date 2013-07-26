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
package com.asakusafw.dmdl.java.emitter;

import java.io.IOException;
import java.io.PrintWriter;
import java.math.BigDecimal;
import java.text.MessageFormat;
import java.util.Collections;
import java.util.Set;

import org.apache.hadoop.io.Text;

import com.asakusafw.dmdl.java.Configuration;
import com.asakusafw.dmdl.java.util.JavaName;
import com.asakusafw.dmdl.java.util.NameUtil;
import com.asakusafw.dmdl.model.AstDescription;
import com.asakusafw.dmdl.model.AstName;
import com.asakusafw.dmdl.model.AstSimpleName;
import com.asakusafw.dmdl.model.BasicTypeKind;
import com.asakusafw.dmdl.semantics.Declaration;
import com.asakusafw.dmdl.semantics.DmdlSemantics;
import com.asakusafw.dmdl.semantics.ModelDeclaration;
import com.asakusafw.dmdl.semantics.ModelSymbol;
import com.asakusafw.dmdl.semantics.PropertyDeclaration;
import com.asakusafw.dmdl.semantics.trait.NamespaceTrait;
import com.asakusafw.dmdl.semantics.type.BasicType;
import com.asakusafw.runtime.value.BooleanOption;
import com.asakusafw.runtime.value.ByteOption;
import com.asakusafw.runtime.value.Date;
import com.asakusafw.runtime.value.DateOption;
import com.asakusafw.runtime.value.DateTime;
import com.asakusafw.runtime.value.DateTimeOption;
import com.asakusafw.runtime.value.DecimalOption;
import com.asakusafw.runtime.value.DoubleOption;
import com.asakusafw.runtime.value.FloatOption;
import com.asakusafw.runtime.value.IntOption;
import com.asakusafw.runtime.value.LongOption;
import com.asakusafw.runtime.value.ShortOption;
import com.asakusafw.runtime.value.StringOption;
import com.asakusafw.utils.collections.Sets;
import com.asakusafw.utils.java.model.syntax.Comment;
import com.asakusafw.utils.java.model.syntax.CompilationUnit;
import com.asakusafw.utils.java.model.syntax.Expression;
import com.asakusafw.utils.java.model.syntax.ModelFactory;
import com.asakusafw.utils.java.model.syntax.Name;
import com.asakusafw.utils.java.model.syntax.QualifiedName;
import com.asakusafw.utils.java.model.syntax.SimpleName;
import com.asakusafw.utils.java.model.syntax.Type;
import com.asakusafw.utils.java.model.syntax.TypeDeclaration;
import com.asakusafw.utils.java.model.util.ImportBuilder;
import com.asakusafw.utils.java.model.util.Models;

/**
 * Emitting context.
 */
public final class EmitContext {

    private final DmdlSemantics semantics;

    private final Configuration config;

    private final ModelFactory factory;

    private final SimpleName typeName;

    private final ImportBuilder imports;

    private final Set<String> fieldNames;

    /**
     * Creates and returns a new instance.
     * @param semantics the root semantics model
     * @param config the configuration of this processing
     * @param model the model to process
     * @param categoryName the category name of processing
     * @param typeNamePattern the type name pattern in {@link MessageFormat#format(String, Object...)}
     * @throws IllegalArgumentException if some parameters were {@code null}
     */
    public EmitContext(
            DmdlSemantics semantics,
            Configuration config,
            ModelDeclaration model,
            String categoryName,
            String typeNamePattern) {
        if (semantics == null) {
            throw new IllegalArgumentException("semantics must not be null"); //$NON-NLS-1$
        }
        if (config == null) {
            throw new IllegalArgumentException("config must not be null"); //$NON-NLS-1$
        }
        if (model == null) {
            throw new IllegalArgumentException("model must not be null"); //$NON-NLS-1$
        }
        if (categoryName == null) {
            throw new IllegalArgumentException("categoryName must not be null"); //$NON-NLS-1$
        }
        this.semantics = semantics;
        this.config = config;
        this.factory = config.getFactory();
        this.typeName = getTypeName(model, typeNamePattern);
        Name namespace = getNamespace(model);
        this.imports = new ImportBuilder(
                factory,
                factory.newPackageDeclaration(
                        Models.append(factory,
                                config.getBasePackage(),
                                namespace,
                                factory.newSimpleName(categoryName))),
                ImportBuilder.Strategy.TOP_LEVEL);
        this.imports.resolvePackageMember(this.typeName);
        this.fieldNames = collectFieldNames(model);
    }

    private Set<String> collectFieldNames(ModelDeclaration model) {
        assert model != null;
        Set<String> results = Sets.create();
        for (PropertyDeclaration property : model.getDeclaredProperties()) {
            results.add(getFieldName(property).getToken());
        }
        return results;
    }

    /**
     * Returns Java DOM factory in use.
     * @return the Java DOM factory
     */
    public ModelFactory getModelFactory() {
        return factory;
    }

    /**
     * Returns the root semantics model.
     * @return the semantics model
     */
    public DmdlSemantics getSemantics() {
        return semantics;
    }

    /**
     * Returns the current configuration.
     * @return the current configuration
     */
    public Configuration getConfiguration() {
        return config;
    }

    private SimpleName getTypeName(ModelDeclaration model, String namePattern) {
        assert model != null;
        assert namePattern != null;
        return factory.newSimpleName(MessageFormat.format(
                namePattern,
                JavaName.of(model.getName()).toTypeName()));
    }

    private Name getNamespace(ModelDeclaration model) {
        assert model != null;
        NamespaceTrait trait = model.getTrait(NamespaceTrait.class);
        AstName name;
        if (trait == null) {
            name = new AstSimpleName(null, NameConstants.DEFAULT_NAMESPACE);
        } else {
            name = trait.getNamespace();
        }
        return Models.toName(factory, NameUtil.toPackageName(name));
    }

    /**
     * Returns the simple name of processing type.
     * @return the type name
     */
    public SimpleName getTypeName() {
        return typeName;
    }

    /**
     * Returns the qualified name of processing type.
     * @return the type name
     */
    public QualifiedName getQualifiedTypeName() {
        return factory.newQualifiedName(
                imports.getPackageDeclaration().getName(),
                typeName);
    }

    /**
     * Emits the type declaration as the suitable Java source program.
     * @param type the declaration
     * @throws IOException if failed to emit a Java program
     * @throws IllegalArgumentException if some parameters were {@code null}
     */
    public void emit(TypeDeclaration type) throws IOException {
        if (type == null) {
            throw new IllegalArgumentException("type must not be null"); //$NON-NLS-1$
        }
        CompilationUnit compilationUnit = factory.newCompilationUnit(
                imports.getPackageDeclaration(),
                imports.toImportDeclarations(),
                Collections.singletonList(type),
                Collections.<Comment>emptyList());
        PrintWriter writer = config.getOutput().openFor(compilationUnit);
        try {
            Models.emit(compilationUnit, writer);
        } finally {
            writer.close();
        }
    }

    /**
     * Resolves the model symbol to the corresponded Java DOM type symbol.
     * @param model the model symbol
     * @return the corresponded type symbol
     * @throws IllegalArgumentException if some parameters were {@code null}
     */
    public Type resolve(ModelSymbol model) {
        if (model == null) {
            throw new IllegalArgumentException("model must not be null"); //$NON-NLS-1$
        }
        ModelDeclaration decl = model.findDeclaration();
        if (decl == null) {
            throw new IllegalArgumentException();
        }
        Name qualifiedName = Models.append(factory,
                config.getBasePackage(),
                getNamespace(decl),
                factory.newSimpleName(NameConstants.CATEGORY_DATA_MODEL),
                getTypeName(decl, NameConstants.PATTERN_DATA_MODEL));
        return imports.toType(qualifiedName);
    }

    /**
     * Resolves the runtime type to the corresponded Java DOM type symbol.
     * @param type the runtime type
     * @return the corresponded type symbol
     * @throws IllegalArgumentException if some parameters were {@code null}
     */
    public Type resolve(java.lang.reflect.Type type) {
        if (type == null) {
            throw new IllegalArgumentException("type must not be null"); //$NON-NLS-1$
        }
        return imports.toType(type);
    }

    /**
     * Resolves the qualified type name to the corresponded type symbol.
     * @param name the qualified type name
     * @return the corresponded type symbol
     * @throws IllegalArgumentException if some parameters were {@code null}
     */
    public Type resolve(Name name) {
        if (name == null) {
            throw new IllegalArgumentException("name must not be null"); //$NON-NLS-1$
        }
        return imports.toType(name);
    }

    /**
     * Resolves the Java DOM type to imported type representation.
     * @param type the Java DOM type
     * @return the imported type representation
     * @throws IllegalArgumentException if some parameters were {@code null}
     */
    public Type resolve(Type type) {
        if (type == null) {
            throw new IllegalArgumentException("type must not be null"); //$NON-NLS-1$
        }
        return imports.resolve(type);
    }

    /**
     * Returns the corresponded property name.
     * @param property target property
     * @return the corresponded name
     * @throws IllegalArgumentException if some parameters were {@code null}
     */
    public SimpleName getFieldName(PropertyDeclaration property) {
        if (property == null) {
            throw new IllegalArgumentException("property must not be null"); //$NON-NLS-1$
        }
        String name = JavaName.of(property.getName()).toMemberName();
        return factory.newSimpleName(name);
    }

    /**
     * Returns the corresponded property value type.
     * @param property target property
     * @return the corresponded type
     * @throws IllegalArgumentException if some parameters were {@code null}
     */
    public Type getValueType(PropertyDeclaration property) {
        if (property == null) {
            throw new IllegalArgumentException("property must not be null"); //$NON-NLS-1$
        }
        if (property.getType() instanceof BasicType) {
            BasicType bt = (BasicType) property.getType();
            switch (bt.getKind()) {
            case BOOLEAN:
                return resolve(boolean.class);
            case DATE:
                return resolve(Date.class);
            case DATETIME:
                return resolve(DateTime.class);
            case DECIMAL:
                return resolve(BigDecimal.class);
            case DOUBLE:
                return resolve(double.class);
            case FLOAT:
                return resolve(float.class);
            case BYTE:
                return resolve(byte.class);
            case SHORT:
                return resolve(short.class);
            case INT:
                return resolve(int.class);
            case LONG:
                return resolve(long.class);
            case TEXT:
                return resolve(Text.class);
            default:
                throw new IllegalArgumentException(MessageFormat.format(
                        "Unsupported basic type: {0}", //$NON-NLS-1$
                        bt.getKind()));
            }
        }
        throw new IllegalArgumentException();
    }

    /**
     * Returns the corresponded property data type.
     * @param property target property
     * @return the corresponded type
     * @throws IllegalArgumentException if some parameters were {@code null}
     */
    public Type getFieldType(PropertyDeclaration property) {
        if (property == null) {
            throw new IllegalArgumentException("property must not be null"); //$NON-NLS-1$
        }
        if (property.getType() instanceof BasicType) {
            BasicType bt = (BasicType) property.getType();
            switch (bt.getKind()) {
            case BOOLEAN:
                return resolve(BooleanOption.class);
            case DATE:
                return resolve(DateOption.class);
            case DATETIME:
                return resolve(DateTimeOption.class);
            case DECIMAL:
                return resolve(DecimalOption.class);
            case BYTE:
                return resolve(ByteOption.class);
            case SHORT:
                return resolve(ShortOption.class);
            case INT:
                return resolve(IntOption.class);
            case LONG:
                return resolve(LongOption.class);
            case FLOAT:
                return resolve(FloatOption.class);
            case DOUBLE:
                return resolve(DoubleOption.class);
            case TEXT:
                return resolve(StringOption.class);
            default:
                throw new IllegalArgumentException(MessageFormat.format(
                        "Unsupported basic type: {0}", //$NON-NLS-1$
                        bt.getKind()));
            }
        }
        throw new IllegalArgumentException();
    }

    /**
     * Returns the corresponded property initializer.
     * @param property target property
     * @return the corresponded expression
     * @throws IllegalArgumentException if some parameters were {@code null}
     */
    public Expression getFieldInitializer(PropertyDeclaration property) {
        if (property == null) {
            throw new IllegalArgumentException("property must not be null"); //$NON-NLS-1$
        }
        if (property.getType() instanceof BasicType) {
            return factory.newClassInstanceCreationExpression(getFieldType(property));
        }
        throw new IllegalArgumentException();
    }

    /**
     * Returns the corresponded value getter name.
     * @param property target property
     * @return the corresponded name
     * @throws IllegalArgumentException if some parameters were {@code null}
     */
    public SimpleName getValueGetterName(PropertyDeclaration property) {
        if (property == null) {
            throw new IllegalArgumentException("property must not be null"); //$NON-NLS-1$
        }
        JavaName name = JavaName.of(property.getName());
        if (isBoolean(property)) {
            name.addFirst("is"); //$NON-NLS-1$
        } else {
            name.addFirst("get"); //$NON-NLS-1$
        }
        return factory.newSimpleName(name.toMemberName());
    }

    private boolean isBoolean(PropertyDeclaration property) {
        assert property != null;
        if ((property.getType() instanceof BasicType) == false) {
            return false;
        }
        BasicType type = (BasicType) property.getType();
        return type.getKind() == BasicTypeKind.BOOLEAN;
    }

    /**
     * Returns the corresponded value setter name.
     * @param property target property
     * @return the corresponded name
     * @throws IllegalArgumentException if some parameters were {@code null}
     */
    public SimpleName getValueSetterName(PropertyDeclaration property) {
        if (property == null) {
            throw new IllegalArgumentException("property must not be null"); //$NON-NLS-1$
        }
        JavaName name = JavaName.of(property.getName());
        name.addFirst("set"); //$NON-NLS-1$
        return factory.newSimpleName(name.toMemberName());
    }

    /**
     * Returns the corresponded option getter name.
     * @param property target property
     * @return the corresponded name
     * @throws IllegalArgumentException if some parameters were {@code null}
     */
    public SimpleName getOptionGetterName(PropertyDeclaration property) {
        if (property == null) {
            throw new IllegalArgumentException("property must not be null"); //$NON-NLS-1$
        }
        JavaName name = JavaName.of(property.getName());
        name.addFirst("get"); //$NON-NLS-1$
        name.addLast("Option"); //$NON-NLS-1$
        return factory.newSimpleName(name.toMemberName());
    }

    /**
     * Returns the corresponded option setter name.
     * @param property target property
     * @return the corresponded name
     * @throws IllegalArgumentException if some parameters were {@code null}
     */
    public SimpleName getOptionSetterName(PropertyDeclaration property) {
        if (property == null) {
            throw new IllegalArgumentException("property must not be null"); //$NON-NLS-1$
        }
        JavaName name = JavaName.of(property.getName());
        name.addFirst("set"); //$NON-NLS-1$
        name.addLast("Option"); //$NON-NLS-1$
        return factory.newSimpleName(name.toMemberName());
    }

    /**
     * Returns a variable name which is different to other property names.
     * @param hint the naming hint
     * @return a variable name
     * @throws IllegalArgumentException if some parameters were {@code null}
     */
    public SimpleName createVariableName(String hint) {
        if (hint == null) {
            throw new IllegalArgumentException("hint must not be null"); //$NON-NLS-1$
        }
        if (fieldNames.contains(hint) == false) {
            return factory.newSimpleName(hint);
        }
        for (int i = 0; true; i++) {
            String next = hint + i;
            if (fieldNames.contains(next) == false) {
                return factory.newSimpleName(next);
            }
        }
    }

    /**
     * Returns the description text of the declaration.
     * @param declaration the target declaration
     * @return the description text
     * @throws IllegalArgumentException if some parameters were {@code null}
     */
    public String getDescription(Declaration declaration) {
        if (declaration == null) {
            throw new IllegalArgumentException("declaration must not be null"); //$NON-NLS-1$
        }
        AstDescription description = declaration.getDescription();
        if (description == null) {
            return declaration.getName().identifier;
        } else {
            return description.getText();
        }
    }
}
