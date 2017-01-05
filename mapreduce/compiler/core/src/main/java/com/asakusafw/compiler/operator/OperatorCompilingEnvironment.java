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

import java.io.IOException;
import java.util.HashSet;
import java.util.Set;

import javax.annotation.processing.Filer;
import javax.annotation.processing.Messager;
import javax.annotation.processing.ProcessingEnvironment;
import javax.lang.model.element.Element;
import javax.lang.model.element.TypeElement;
import javax.lang.model.type.DeclaredType;
import javax.lang.model.type.TypeKind;
import javax.lang.model.type.TypeMirror;
import javax.lang.model.util.Elements;
import javax.lang.model.util.Types;

import com.asakusafw.compiler.common.Precondition;
import com.asakusafw.utils.java.jsr269.bridge.Jsr269;
import com.asakusafw.utils.java.model.syntax.CompilationUnit;
import com.asakusafw.utils.java.model.syntax.ModelFactory;

/**
 * Represents a compiler environment for operator DSL compiler.
 * @since 0.1.0
 * @version 0.7.0
 */
public class OperatorCompilingEnvironment {

    private static final Element[] EMPTY_ELEMENTS = new Element[0];

    private static final String KEY_FORCE_GENERATE = "com.asakusafw.operator.generate.force"; //$NON-NLS-1$

    private static final String DEFAULT_FORCE_GENERATE = "false"; //$NON-NLS-1$

    private final ProcessingEnvironment processingEnvironment;

    private final ModelFactory factory;

    private final OperatorCompilerOptions options;

    private final boolean forceGenerate;

    private final Set<String> generatedResourceKeys = new HashSet<>();

    /**
     * Creates a new instance.
     * @param processingEnvironment the annotation processing environment
     * @param factory the Java DOM factory
     * @param options the compiler options
     * @throws IllegalArgumentException if the parameters are {@code null}
     */
    public OperatorCompilingEnvironment(
            ProcessingEnvironment processingEnvironment,
            ModelFactory factory,
            OperatorCompilerOptions options) {
        Precondition.checkMustNotBeNull(processingEnvironment, "processingEnvironment"); //$NON-NLS-1$
        Precondition.checkMustNotBeNull(factory, "factory"); //$NON-NLS-1$
        Precondition.checkMustNotBeNull(options, "options"); //$NON-NLS-1$
        this.processingEnvironment = processingEnvironment;
        this.factory = factory;
        this.options = options;
        this.forceGenerate = Boolean.parseBoolean(options.getProperty(KEY_FORCE_GENERATE, DEFAULT_FORCE_GENERATE));
    }

    /**
     * Returns the annotation processing environment.
     * @return the annotation processing environment
     */
    public ProcessingEnvironment getProcessingEnvironment() {
        return processingEnvironment;
    }

    /**
     * Returns the Java DOM factory.
     * @return the Java DOM factory
     */
    public ModelFactory getFactory() {
        return factory;
    }

    /**
     * Returns the class loader for loading service classes.
     * @return the class loader for loading service classes
     */
    public ClassLoader getServiceClassLoader() {
        return options.getServiceClassLoader();
    }

    /**
     * Returns the compiler options.
     * @return the compiler options
     */
    public OperatorCompilerOptions getOptions() {
        return options;
    }

    /**
     * Returns the messager.
     * @return the messager
     */
    public Messager getMessager() {
        return getProcessingEnvironment().getMessager();
    }

    /**
     * Returns the element utilities.
     * @return the element utilities
     */
    public Elements getElementUtils() {
        return getProcessingEnvironment().getElementUtils();
    }

    /**
     * Returns the type utilities.
     * @return the type utilities
     */
    public Types getTypeUtils() {
        return getProcessingEnvironment().getTypeUtils();
    }

    /**
     * Emits a compilation unit.
     * @param unit the target compilation unit
     * @throws IOException if error occurred while emitting the compilation unit
     * @throws IllegalArgumentException if the parameter is {@code null}
     */
    public void emit(CompilationUnit unit) throws IOException {
        emit(unit, EMPTY_ELEMENTS);
    }

    /**
     * Emits a compilation unit.
     * @param unit the target compilation unit
     * @param originatingElements the original elements
     * @throws IOException if error occurred while emitting the compilation unit
     * @throws IllegalArgumentException if the parameter is {@code null}
     * @since 0.7.0
     */
    public void emit(CompilationUnit unit, Element... originatingElements) throws IOException {
        Precondition.checkMustNotBeNull(unit, "unit"); //$NON-NLS-1$
        Filer filer = getProcessingEnvironment().getFiler();
        new Jsr269(factory).emit(filer, unit, originatingElements);
    }

    /**
     * Loads a mirror of the data model corresponded to the specified type.
     * @param type the corresponded type
     * @return the loaded data model mirror,
     *     or {@code null} if the type does not represent a valid data model for this repository
     * @throws IllegalArgumentException if some parameters were {@code null}
     */
    public DataModelMirror loadDataModel(TypeMirror type) {
        if (type == null) {
            throw new IllegalArgumentException("type must not be null"); //$NON-NLS-1$
        }
        return options.getDataModelRepository().load(this, type);
    }

    /**
     * Returns the declared type (no type arguments) for the target class.
     * @param type the target class
     * @return the corresponded declared type
     * @throws IllegalArgumentException if the parameter is {@code null}
     */
    public DeclaredType getDeclaredType(Class<?> type) {
        Precondition.checkMustNotBeNull(type, "type"); //$NON-NLS-1$
        TypeElement elem = getElementUtils().getTypeElement(type.getName());
        if (elem == null) {
            throw new IllegalStateException(type.getName());
        }
        return getTypeUtils().getDeclaredType(elem);
    }

    /**
     * Returns the type erasure.
     * @param type the target type
     * @return the type erasure
     * @throws IllegalArgumentException if the parameter is {@code null}
     */
    public TypeMirror getErasure(TypeMirror type) {
        Precondition.checkMustNotBeNull(type, "type"); //$NON-NLS-1$
        // several version of Eclipse may compute invalid erasure
        if (type.getKind() == TypeKind.DECLARED) {
            TypeElement element = (TypeElement) ((DeclaredType) type).asElement();
            return getTypeUtils().getDeclaredType(element);
        }
        return getTypeUtils().erasure(type);
    }

    /**
     * Sets the target resources is generated.
     * @param key the target resource key
     * @since 0.7.0
     */
    public void setResourceGenerated(String key) {
        Precondition.checkMustNotBeNull(key, "key"); //$NON-NLS-1$
        generatedResourceKeys.add(key);
    }

    /**
     * Returns whether the target resources is generated or not.
     * @param key the target resource key
     * @return {@code true} if it is already generated, otherwise {@code false}
     * @since 0.7.0
     */
    public boolean isResourceGenerated(String key) {
        Precondition.checkMustNotBeNull(key, "key"); //$NON-NLS-1$
        if (forceGenerate) {
            return false;
        }
        return generatedResourceKeys.contains(key);
    }
}
