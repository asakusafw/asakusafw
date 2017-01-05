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
package com.asakusafw.compiler.operator;

import java.util.Collections;
import java.util.List;

import com.asakusafw.compiler.common.Precondition;
import com.asakusafw.compiler.operator.util.GeneratorUtil;
import com.asakusafw.utils.java.model.syntax.Attribute;
import com.asakusafw.utils.java.model.syntax.Javadoc;
import com.asakusafw.utils.java.model.syntax.ModelFactory;
import com.asakusafw.utils.java.model.syntax.SimpleName;
import com.asakusafw.utils.java.model.syntax.Type;
import com.asakusafw.utils.java.model.syntax.TypeBodyDeclaration;
import com.asakusafw.utils.java.model.syntax.TypeDeclaration;
import com.asakusafw.utils.java.model.util.ImportBuilder;

/**
 * An abstract super class of generating support classes for operator classes.
 * @since 0.1.0
 * @version 0.5.0
 */
public abstract class OperatorClassGenerator {

    /**
     * The current environment.
     */
    protected final OperatorCompilingEnvironment environment;

    /**
     * The Java DOM factory.
     */
    protected final ModelFactory factory;

    /**
     * The current import declaration builder.
     */
    protected final ImportBuilder importer;

    /**
     * The target operator class.
     */
    protected final OperatorClass operatorClass;

    /**
     * The utility object for this generator.
     */
    protected final GeneratorUtil util;

    /**
     * Creates a new instance.
     * @param environment the current environment
     * @param factory the Java DOM factory
     * @param importer the import declaration builder
     * @param operatorClass the target operator class
     * @throws IllegalArgumentException if the parameters are {@code null}
     */
    public OperatorClassGenerator(
            OperatorCompilingEnvironment environment,
            ModelFactory factory,
            ImportBuilder importer,
            OperatorClass operatorClass) {
        Precondition.checkMustNotBeNull(environment, "environment"); //$NON-NLS-1$
        Precondition.checkMustNotBeNull(factory, "factory"); //$NON-NLS-1$
        Precondition.checkMustNotBeNull(importer, "importer"); //$NON-NLS-1$
        Precondition.checkMustNotBeNull(operatorClass, "operatorClass"); //$NON-NLS-1$
        this.environment = environment;
        this.factory = factory;
        this.importer = importer;
        this.operatorClass = operatorClass;
        this.util = new GeneratorUtil(environment, factory, importer);
    }

    /**
     * Generates a declaration of support class.
     * @return the generated type declaration
     */
    public TypeDeclaration generate() {
        SimpleName name = getClassName();
        importer.resolvePackageMember(name);
        return factory.newClassDeclaration(
                createJavadoc(),
                getAttributes(),
                name,
                Collections.emptyList(),
                getSuperClass(),
                Collections.emptyList(),
                createMembers());
    }

    /**
     * Returns the simple name of the generating type.
     * @return the simple name of the generating type
     */
    protected abstract SimpleName getClassName();

    /**
     * Returns the attributes for the generating type.
     * @return the attributes for the generating type
     * @since 0.5.0
     */
    protected abstract List<? extends Attribute> getAttributes();

    /**
     * Returns the super class for the generating type.
     * @return the super class, or {@code null} if the generating type will not have any explicit super type
     */
    protected Type getSuperClass() {
        return null;
    }

    /**
     * Returns the documentation comments for the generating type.
     * @return the documentation comments for the generating type
     */
    protected abstract Javadoc createJavadoc();

    /**
     * Returns the members for the generating type.
     * @return the members for the generating type
     */
    protected abstract List<TypeBodyDeclaration> createMembers();
}