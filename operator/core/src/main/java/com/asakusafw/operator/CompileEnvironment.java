/**
 * Copyright 2011-2016 Asakusa Framework Team.
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
package com.asakusafw.operator;

import java.io.IOException;
import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.EnumSet;
import java.util.HashSet;
import java.util.List;
import java.util.Objects;
import java.util.ServiceConfigurationError;
import java.util.ServiceLoader;
import java.util.Set;

import javax.annotation.processing.ProcessingEnvironment;
import javax.lang.model.element.Element;
import javax.lang.model.element.TypeElement;
import javax.lang.model.type.DeclaredType;
import javax.lang.model.type.TypeKind;
import javax.lang.model.type.TypeMirror;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.asakusafw.operator.description.ClassDescription;
import com.asakusafw.operator.model.DataModelMirror;
import com.asakusafw.operator.util.DescriptionHelper;
import com.asakusafw.utils.java.jsr269.bridge.Jsr269;
import com.asakusafw.utils.java.model.syntax.CompilationUnit;
import com.asakusafw.utils.java.model.util.Models;

/**
 * Represents operator compiler environment.
 */
public class CompileEnvironment {

    static final Logger LOG = LoggerFactory.getLogger(CompileEnvironment.class);

    private final ProcessingEnvironment processingEnvironment;

    private final List<OperatorDriver> operatorDrivers;

    private final List<DataModelMirrorRepository> dataModelMirrors;

    private final Set<String> generatedResourceKeys = new HashSet<>();

    private volatile boolean strict = true;

    private volatile boolean forceRegenerateResources = false;

    private volatile boolean flowpartExternalIo = false;

    private volatile boolean forceGenerateImplementation = false;

    /**
     * Creates a new instance.
     * @param processingEnvironment current annotation processing environment
     * @param operatorDrivers supported operator drivers
     * @param dataModelMirrors supported data model mirror repositories
     * @throws IllegalArgumentException if some parameters were {@code null}
     * @see #newInstance(ProcessingEnvironment, ClassLoader, Support...)
     */
    public CompileEnvironment(
            ProcessingEnvironment processingEnvironment,
            List<? extends OperatorDriver> operatorDrivers,
            List<? extends DataModelMirrorRepository> dataModelMirrors) {
        Objects.requireNonNull(processingEnvironment, "processingEnvironment must not be null"); //$NON-NLS-1$
        Objects.requireNonNull(operatorDrivers, "operatorDrivers must not be null"); //$NON-NLS-1$
        Objects.requireNonNull(dataModelMirrors, "dataModelMirrors must not be null"); //$NON-NLS-1$
        this.processingEnvironment = processingEnvironment;
        this.operatorDrivers = new ArrayList<>(operatorDrivers);
        this.dataModelMirrors = new ArrayList<>(dataModelMirrors);
    }

    /**
     * Creates a new instance.
     * @param processingEnvironment current annotation processing environment.
     * @param features list of supported features
     * @return the created instance
     * @throws IllegalArgumentException if some parameters were {@code null}
     */
    public static CompileEnvironment newInstance(
            ProcessingEnvironment processingEnvironment,
            Support... features) {
        Objects.requireNonNull(processingEnvironment, "processingEnvironment must not be null"); //$NON-NLS-1$
        Objects.requireNonNull(features, "features must not be null"); //$NON-NLS-1$
        ClassLoader classLoader = findServiceClassLoader();
        return newInstance(processingEnvironment, classLoader, features);
    }

    private static ClassLoader findServiceClassLoader() {
        ClassLoader classLoader = Thread.currentThread().getContextClassLoader();
        try {
            Class.forName(CompileEnvironment.class.getName(), false, classLoader);
        } catch (ClassNotFoundException e) {
            LOG.debug("Thread context class loader is invalid", e); //$NON-NLS-1$
            classLoader = CompileEnvironment.class.getClassLoader();
        }
        return classLoader;
    }

    /**
     * Creates a new instance.
     * @param processingEnvironment current annotation processing environment.
     * @param serviceLoader a class loader to be used for loading services listed in {@code features}
     * @param features list of supported features
     * @return the created instance
     * @throws IllegalArgumentException if some parameters were {@code null}
     */
    public static CompileEnvironment newInstance(
            ProcessingEnvironment processingEnvironment,
            ClassLoader serviceLoader,
            Support... features) {
        Objects.requireNonNull(processingEnvironment, "processingEnvironment must not be null"); //$NON-NLS-1$
        Objects.requireNonNull(serviceLoader, "serviceLoader must not be null"); //$NON-NLS-1$
        Objects.requireNonNull(features, "features must not be null"); //$NON-NLS-1$
        Set<Support> set = EnumSet.noneOf(Support.class);
        Collections.addAll(set, features);
        List<OperatorDriver> operatorDrivers = new ArrayList<>();
        List<DataModelMirrorRepository> dataModelMirrors = new ArrayList<>();
        if (set.contains(Support.OPERATOR_DRIVER)) {
            operatorDrivers.addAll(load(OperatorDriver.class, serviceLoader));
        }
        if (set.contains(Support.DATA_MODEL_REPOSITORY)) {
            dataModelMirrors.addAll(load(DataModelMirrorRepository.class, serviceLoader));
        }
        return new CompileEnvironment(processingEnvironment, operatorDrivers, dataModelMirrors)
            .withStrict(set.contains(Support.STRICT_CHECKING))
            .withFlowpartExternalIo(set.contains(Support.FLOWPART_EXTERNAL_IO))
            .withForceRegenerateResources(set.contains(Support.FORCE_REGENERATE_RESOURCES))
            .withForceGenerateImplementation(set.contains(Support.FORCE_GENERATE_IMPLEMENTATION));
    }

    private static <T> List<T> load(Class<T> spi, ClassLoader serviceLoader) {
        try {
            List<T> results = new ArrayList<>();
            for (T service : ServiceLoader.load(spi, serviceLoader)) {
                results.add(service);
            }
            return results;
        } catch (ServiceConfigurationError e) {
            throw new IllegalStateException(MessageFormat.format(
                    Messages.getString("CompileEnvironment.errorFailLoadService"), //$NON-NLS-1$
                    spi.getName()), e);
        }
    }

    /**
     * Returns the current processing environment.
     * @return the current processing environment
     */
    public ProcessingEnvironment getProcessingEnvironment() {
        return processingEnvironment;
    }

    /**
     * Emits the target compilation unit onto suitable location.
     * @param unit target compilation unit
     * @param originatingElements the original elements
     * @throws IOException if failed to emit by I/O error
     * @throws IllegalArgumentException if some parameters were {@code null}
     */
    public void emit(CompilationUnit unit, Element... originatingElements) throws IOException {
        Objects.requireNonNull(unit, "unit must not be null"); //$NON-NLS-1$
        new Jsr269(Models.getModelFactory()).emit(processingEnvironment.getFiler(), unit, originatingElements);
    }

    /**
     * Returns data-model mirror.
     * @param typeName target type name
     * @return related data-model mirror, or {@code null} if not found
     * @throws IllegalArgumentException if some parameters were {@code null}
     */
    public DataModelMirror findDataModel(ClassDescription typeName) {
        Objects.requireNonNull(typeName, "typeName must not be null"); //$NON-NLS-1$
        DeclaredType type = findDeclaredType(typeName);
        if (type == null) {
            return null;
        }
        return findDataModel(type);
    }

    /**
     * Returns data-model mirror.
     * @param type target type mirror
     * @return related data-model mirror, or {@code null} if not found
     * @throws IllegalArgumentException if some parameters were {@code null}
     */
    public DataModelMirror findDataModel(TypeMirror type) {
        Objects.requireNonNull(type, "type must not be null"); //$NON-NLS-1$
        for (DataModelMirrorRepository repo : dataModelMirrors) {
            DataModelMirror mirror = repo.load(this, type);
            if (mirror != null) {
                return mirror;
            }
        }
        return null;
    }

    /**
     * Returns registered operator drivers.
     * @return operator drivers
     */
    public List<OperatorDriver> getOperatorDrivers() {
        return operatorDrivers;
    }

    /**
     * Returns a registered operator driver related to the annotation type.
     * @param annotationType target annotation type
     * @return related operator driver, or {@code null} if it is not registered
     * @throws IllegalArgumentException if some parameters were {@code null}
     */
    public OperatorDriver findDriver(TypeElement annotationType) {
        Objects.requireNonNull(annotationType, "annotationType must not be null"); //$NON-NLS-1$
        ClassDescription aClass = DescriptionHelper.toDescription(this, annotationType);
        for (OperatorDriver driver : operatorDrivers) {
            if (driver.getAnnotationTypeName().equals(aClass)) {
                return driver;
            }
        }
        return null;
    }

    /**
     * Returns the type element which has the specified qualified name.
     * @param aClass fully qualified type name
     * @return target type element, or {@code null} if it is not found
     * @throws IllegalArgumentException if some parameters were {@code null}
     */
    public TypeElement findTypeElement(ClassDescription aClass) {
        Objects.requireNonNull(aClass, "aClass must not be null"); //$NON-NLS-1$
        TypeElement type = processingEnvironment.getElementUtils().getTypeElement(aClass.getClassName());
        return type;
    }

    /**
     * Returns the type mirror witch has the specified qualified name.
     * @param aClass fully qualified type name
     * @return target type mirror, or {@code null} if it is not found
     * @throws IllegalArgumentException if some parameters were {@code null}
     */
    public DeclaredType findDeclaredType(ClassDescription aClass) {
        Objects.requireNonNull(aClass, "aClass must not be null"); //$NON-NLS-1$
        TypeElement type = findTypeElement(aClass);
        if (type == null) {
            return null;
        }
        return processingEnvironment.getTypeUtils().getDeclaredType(type);
    }

    /**
     * Returns the type erasure.
     * @param type original type
     * @return erased type
     * @throws IllegalArgumentException if some parameters were {@code null}
     */
    public TypeMirror getErasure(TypeMirror type) {
        Objects.requireNonNull(type, "type must not be null"); //$NON-NLS-1$
        // Note: for Eclipse JDT
        if (type.getKind() == TypeKind.DECLARED) {
            TypeElement element = (TypeElement) ((DeclaredType) type).asElement();
            return processingEnvironment.getTypeUtils().getDeclaredType(element);
        }
        return processingEnvironment.getTypeUtils().erasure(type);
    }

    /**
     * Sets the target resources is generated.
     * @param key the target resource key
     */
    public void setResourceGenerated(ClassDescription key) {
        Objects.requireNonNull(key, "key must not be null"); //$NON-NLS-1$
        generatedResourceKeys.add(key.getInternalName());
    }

    /**
     * Returns whether the target resources is generated or not.
     * @param key the target resource key
     * @return {@code true} if it is already generated, otherwise {@code false}
     */
    public boolean isResourceGenerated(ClassDescription key) {
        Objects.requireNonNull(key, "key must not be null"); //$NON-NLS-1$
        if (forceRegenerateResources) {
            return false;
        }
        return generatedResourceKeys.contains(key.getInternalName());
    }

    /**
     * Returns whether strict checking is available.
     * @return {@code true} if it is available, otherwise {@code false}
     */
    public boolean isStrict() {
        return strict;
    }

    /**
     * Returns whether the compiler validates DSL semantics strictly or not.
     * @param newValue {@code true} iff check semantics strictly
     * @return this
     */
    public CompileEnvironment withStrict(boolean newValue) {
        this.strict = newValue;
        return this;
    }

    /**
     * Returns whether external I/O declaration is available in flow-parts or not.
     * @return {@code true} if it is available, otherwise {@code false}
     */
    public boolean isFlowpartExternalIo() {
        return flowpartExternalIo;
    }

    /**
     * Sets whether external I/O declarations are available in flow-parts or not.
     * @param newValue {@code true} iff they are available
     * @return this
     */
    public CompileEnvironment withFlowpartExternalIo(boolean newValue) {
        this.flowpartExternalIo = newValue;
        return this;
    }

    /**
     * Returns whether always generates implementation classes or not.
     * @return {@code true} if it is available, otherwise {@code false}
     */
    public boolean isForceGenerateImplementation() {
        return forceGenerateImplementation;
    }

    /**
     * Sets whether always generates implementation classes or not.
     * @param newValue {@code true} iff it is available
     * @return this
     */
    public CompileEnvironment withForceGenerateImplementation(boolean newValue) {
        this.forceGenerateImplementation = newValue;
        return this;
    }

    /**
     * Returns whether always re-generates resources or not.
     * @return {@code true} if it is available, otherwise {@code false}
     */
    public boolean isForceRegenerateResources() {
        return forceRegenerateResources;
    }

    /**
     * Sets whether always re-generates resources.
     * @param newValue {@code true} iff it is available
     * @return this
     */
    public CompileEnvironment withForceRegenerateResources(boolean newValue) {
        this.forceRegenerateResources = newValue;
        return this;
    }

    /**
     * Represents kind of supported features in {@link CompileEnvironment}.
     */
    public enum Support {

        /**
         * Supports operator drivers.
         */
        OPERATOR_DRIVER,

        /**
         * Supports data model repository.
         */
        DATA_MODEL_REPOSITORY,

        /**
         * Supports strict checking.
         */
        STRICT_CHECKING,

        /**
         * Forcibly regenerates resources.
         */
        FORCE_REGENERATE_RESOURCES,

        /**
         * Always generate implementation classes.
         */
        FORCE_GENERATE_IMPLEMENTATION,

        /**
         * Supports external I/O ports in flow parts.
         */
        FLOWPART_EXTERNAL_IO,
    }
}
