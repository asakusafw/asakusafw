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

import java.text.MessageFormat;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Objects;

import javax.annotation.Generated;
import javax.lang.model.SourceVersion;

import com.asakusafw.operator.description.AnnotationDescription;
import com.asakusafw.operator.description.ClassDescription;
import com.asakusafw.operator.description.Descriptions;
import com.asakusafw.operator.description.ValueDescription;

/**
 * Available constant values in this project.
 * @since 0.9.0
 * @version 0.9.1
 */
public final class Constants {

    private static final String BASE = "com.asakusafw.vocabulary."; //$NON-NLS-1$

    private static ClassDescription classOf(String name) {
        return new ClassDescription(BASE + name);
    }

    /**
     * {@code OperatorHelper} annotation type name.
     */
    public static final ClassDescription TYPE_ANNOTATION_HELPER = classOf("operator.OperatorHelper"); //$NON-NLS-1$

    /**
     * {@code In} type name.
     */
    public static final ClassDescription TYPE_IN = classOf("flow.In"); //$NON-NLS-1$

    /**
     * {@code Out} type name.
     */
    public static final ClassDescription TYPE_OUT = classOf("flow.Out"); //$NON-NLS-1$

    /**
     * {@code Source} type name.
     */
    public static final ClassDescription TYPE_SOURCE = classOf("flow.Source"); //$NON-NLS-1$

    /**
     * {@code DataTable} type name.
     * @since 0.9.1
     */
    public static final ClassDescription TYPE_TABLE =
            new ClassDescription("com.asakusafw.runtime.core.DataTable"); //$NON-NLS-1$

    /**
     * {@code Result} type name.
     */
    public static final ClassDescription TYPE_RESULT =
            new ClassDescription("com.asakusafw.runtime.core.Result"); //$NON-NLS-1$

    /**
     * {@code Key} type name.
     */
    public static final ClassDescription TYPE_KEY = classOf("model.Key"); //$NON-NLS-1$

    /**
     * {@code Joined} type name.
     */
    public static final ClassDescription TYPE_JOINED = classOf("model.Joined"); //$NON-NLS-1$

    /**
     * {@code Summarized} type name.
     */
    public static final ClassDescription TYPE_SUMMARIZED = classOf("model.Summarized"); //$NON-NLS-1$

    /**
     * {@code FlowPart} annotation type name.
     */
    public static final ClassDescription TYPE_FLOW_PART = classOf("flow.FlowPart"); //$NON-NLS-1$

    /**
     * {@code FlowDescription} type name.
     */
    public static final ClassDescription TYPE_FLOW_DESCRIPTION = classOf("flow.FlowDescription"); //$NON-NLS-1$

    /**
     * {@code Import} type name.
     */
    public static final ClassDescription TYPE_IMPORT = classOf("flow.Import"); //$NON-NLS-1$

    /**
     * {@code Export} type name.
     */
    public static final ClassDescription TYPE_EXPORT = classOf("flow.Export"); //$NON-NLS-1$

    /**
     * {@code ImporterDescription} type name.
     */
    public static final ClassDescription TYPE_IMPORTER_DESC = classOf("external.ImporterDescription"); //$NON-NLS-1$

    /**
     * {@code ExporterDescription} type name.
     */
    public static final ClassDescription TYPE_EXPORTER_DESC = classOf("external.ExporterDescription"); //$NON-NLS-1$

    /**
     * {@code FlowElementBuilder} type name.
     */
    public static final ClassDescription TYPE_ELEMENT_BUILDER =
            classOf("flow.builder.FlowElementBuilder"); //$NON-NLS-1$

    /**
     * singleton name of flow-part factory method.
     */
    public static final String NAME_FLOW_PART_FACTORY_METHOD = "create"; //$NON-NLS-1$

    /**
     * Simple name pattern for operator implementation class (0: simple name of operator class).
     */
    private static final String PATTERN_IMPLEMENTATION_CLASS = "{0}Impl"; //$NON-NLS-1$

    /**
     * Simple name pattern for operator factory class (0: simple name of operator/flow-part class).
     */
    private static final String PATTERN_FACTORY_CLASS = "{0}Factory"; //$NON-NLS-1$

    /**
     * Simple name pattern for built-in operator annotation class (0: simple name).
     */
    private static final String PATTERN_BUILTIN_OPERATOR_ANNOTATION_CLASS = BASE + "operator.{0}"; //$NON-NLS-1$

    /**
     * The generator ID.
     */
    public static final String GENERATOR_ID = "com.asakusafw.operator"; //$NON-NLS-1$

    /**
     * The generator name.
     */
    public static final String GENERATOR_NAME = "Asakusa Operator DSL Compiler"; //$NON-NLS-1$

    /**
     * The generator version.
     */
    public static final String GENERATOR_VERSION = "3.0.0"; //$NON-NLS-1$

    /**
     * Returns the implementation class name of target class with the specified name.
     * @param simpleName the simple class name of the operator annotation
     * @return qualified name
     * @throws IllegalArgumentException if some parameters were {@code null}
     */
    public static ClassDescription getBuiltinOperatorClass(String simpleName) {
        Objects.requireNonNull(simpleName, "simpleName must not be null"); //$NON-NLS-1$
        return new ClassDescription(MessageFormat.format(PATTERN_BUILTIN_OPERATOR_ANNOTATION_CLASS, simpleName));
    }

    /**
     * Returns the implementation class name of target class with the specified name.
     * @param originalName the original class name
     * @return related implementation class name
     * @throws IllegalArgumentException if some parameters were {@code null}
     */
    public static ClassDescription getImplementationClass(CharSequence originalName) {
        Objects.requireNonNull(originalName, "originalName must not be null"); //$NON-NLS-1$
        return new ClassDescription(MessageFormat.format(PATTERN_IMPLEMENTATION_CLASS, originalName));
    }

    /**
     * Returns the factory class name of target class with the specified name.
     * @param originalName the original class name
     * @return related factory class name
     * @throws IllegalArgumentException if some parameters were {@code null}
     */
    public static ClassDescription getFactoryClass(CharSequence originalName) {
        Objects.requireNonNull(originalName, "originalName must not be null"); //$NON-NLS-1$
        return new ClassDescription(MessageFormat.format(PATTERN_FACTORY_CLASS, originalName));
    }

    /**
     * Returns the current supported source version.
     * @return the supported version
     */
    public static SourceVersion getSupportedSourceVersion() {
        return SourceVersion.latestSupported();
    }

    /**
     * Returns {@link Generated} annotation.
     * @return the annotation
     */
    public static AnnotationDescription getGenetedAnnotation() {
        Map<String, ValueDescription> elements = new LinkedHashMap<>();
        elements.put("value", Descriptions.valueOf(new String[] { //$NON-NLS-1$
                GENERATOR_ID
        }));
        elements.put("comments", //$NON-NLS-1$
                Descriptions.valueOf(MessageFormat.format(
                        "generated by {0} {1}", //$NON-NLS-1$
                        GENERATOR_NAME, GENERATOR_VERSION)));
        return new AnnotationDescription(Descriptions.classOf(Generated.class), elements);
    }

    private Constants() {
        return;
    }
}
