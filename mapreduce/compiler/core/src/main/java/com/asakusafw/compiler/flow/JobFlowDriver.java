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
package com.asakusafw.compiler.flow;

import java.lang.annotation.Annotation;
import java.lang.reflect.Constructor;
import java.lang.reflect.Modifier;
import java.lang.reflect.Type;
import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.asakusafw.compiler.common.Precondition;
import com.asakusafw.runtime.util.TypeUtil;
import com.asakusafw.vocabulary.external.ExporterDescription;
import com.asakusafw.vocabulary.external.ImporterDescription;
import com.asakusafw.vocabulary.flow.Export;
import com.asakusafw.vocabulary.flow.FlowDescription;
import com.asakusafw.vocabulary.flow.Import;
import com.asakusafw.vocabulary.flow.In;
import com.asakusafw.vocabulary.flow.JobFlow;
import com.asakusafw.vocabulary.flow.Out;
import com.asakusafw.vocabulary.flow.graph.FlowGraph;

/**
 * Analyzes jobflow classes (which annotated with {@link JobFlow}).
 */
public final class JobFlowDriver {

    static final Logger LOG = LoggerFactory.getLogger(JobFlowDriver.class);

    private final Class<? extends FlowDescription> description;

    private JobFlowClass jobFlowClass;

    private final List<String> diagnostics;

    private JobFlowDriver(Class<? extends FlowDescription> description) {
        assert description != null;
        this.description = description;
        this.diagnostics = new ArrayList<>();
    }

    /**
     * Creates a new instance.
     * @param description the target jobflow class
     * @return the created instance
     * @throws IllegalArgumentException if the parameter is {@code null}
     */
    public static JobFlowDriver analyze(Class<? extends FlowDescription> description) {
        Precondition.checkMustNotBeNull(description, "description"); //$NON-NLS-1$
        JobFlowDriver analyzer = new JobFlowDriver(description);
        analyzer.analyze();
        return analyzer;
    }

    /**
     * Returns information of the target jobflow class.
     * @return information of the target jobflow class, or {@code null} if the target jobflow class is not valid
     */
    public JobFlowClass getJobFlowClass() {
        return jobFlowClass;
    }

    /**
     * Returns the target jobflow class.
     * @return the target jobflow class
     */
    public Class<? extends FlowDescription> getDescription() {
        return description;
    }

    /**
     * Returns the diagnostics messages.
     * @return the diagnostics messages
     */
    public List<String> getDiagnostics() {
        return diagnostics;
    }

    private void analyze() {
        JobFlow config = findConfig();
        Constructor<? extends FlowDescription> ctor = findConstructor();
        if (ctor == null) {
            return;
        }
        FlowDescriptionDriver driver = parseParameters(ctor);
        if (hasError()) {
            return;
        }
        FlowDescription instance = newInstance(ctor, driver.getPorts());
        if (hasError()) {
            return;
        }
        try {
            FlowGraph graph = driver.createFlowGraph(instance);
            this.jobFlowClass = new JobFlowClass(config, graph);
        } catch (Exception e) {
            error(e, Messages.getString("JobFlowDriver.errorFailedToAnalyzeFlow"), //$NON-NLS-1$
                    description.getName(), e.toString());
        }
    }

    private FlowDescription newInstance(
            Constructor<? extends FlowDescription> ctor,
            List<?> ports) {
        assert ctor != null;
        assert ports != null;
        try {
            return ctor.newInstance(ports.toArray());
        } catch (Exception e) {
            error(e, Messages.getString("JobFlowDriver.errorFailedToInstantiate"), //$NON-NLS-1$
                    description.getName(), e.toString());
            return null;
        }
    }

    private JobFlow findConfig() {
        if (description.getEnclosingClass() != null) {
            error(null, Messages.getString("JobFlowDriver.errorEnclosingClass")); //$NON-NLS-1$
        }
        if (Modifier.isPublic(description.getModifiers()) == false) {
            error(null, Messages.getString("JobFlowDriver.errorNotPublic")); //$NON-NLS-1$
        }
        if (Modifier.isAbstract(description.getModifiers())) {
            error(null, Messages.getString("JobFlowDriver.errorAbstract")); //$NON-NLS-1$
        }
        JobFlow conf = description.getAnnotation(JobFlow.class);
        if (conf == null) {
            error(null, Messages.getString("JobFlowDriver.errorMissingAnnotation")); //$NON-NLS-1$
        }
        return conf;
    }

    private Constructor<? extends FlowDescription> findConstructor() {
        @SuppressWarnings("unchecked")
        Constructor<? extends FlowDescription>[] ctors =
            (Constructor<? extends FlowDescription>[]) description.getConstructors();
        if (ctors.length == 0) {
            error(null, Messages.getString("JobFlowDriver.errorMissingPublicConstructor")); //$NON-NLS-1$
            return null;
        } else if (ctors.length >= 2) {
            error(null, Messages.getString("JobFlowDriver.errorExtraPublicConstructor")); //$NON-NLS-1$
            return ctors[0];
        } else {
            return ctors[0];
        }
    }

    /**
     * Returns whether this analysis result contains any erroneous information or not.
     * @return {@code true} if this contains any erroneous information, otherwise {@code false}
     */
    public boolean hasError() {
        return diagnostics.isEmpty() == false;
    }

    private FlowDescriptionDriver parseParameters(Constructor<?> ctor) {
        assert ctor != null;
        List<Parameter> rawParams = parseRawParameters(ctor);
        FlowDescriptionDriver driver = new FlowDescriptionDriver();
        for (Parameter raw : rawParams) {
            analyzeParameter(raw, driver);
        }
        return driver;
    }

    private void analyzeParameter(Parameter parameter, FlowDescriptionDriver driver) {
        assert parameter != null;
        assert driver != null;
        if (parameter.raw == In.class) {
            analyzeInput(parameter, driver);
        } else if (parameter.raw == Out.class) {
            analyzeOutput(parameter, driver);
        } else {
            error(
                    null,
                    Messages.getString("JobFlowDriver.errorUnsupportedConstructorParameter"), //$NON-NLS-1$
                    parameter.getPosition());
        }
    }

    private void analyzeInput(Parameter parameter, FlowDescriptionDriver driver) {
        assert parameter != null;
        assert driver != null;
        Type dataType = invoke(In.class, parameter.type);
        if (dataType == null) {
            error(
                    null,
                    Messages.getString("JobFlowDriver.errorMissingInputType"), //$NON-NLS-1$
                    parameter.getPosition());
            return;
        }
        if (parameter.exporter != null) {
            error(
                    null,
                    Messages.getString("JobFlowDriver.errorInputWithExporter"), //$NON-NLS-1$
                    parameter.getPosition());
        }
        if (parameter.importer == null) {
            error(
                    null,
                    Messages.getString("JobFlowDriver.errorMissingImporter"), //$NON-NLS-1$
                    parameter.getPosition());
            return;
        } else {
            String name = parameter.importer.name();
            if (driver.isValidName(name) == false) {
                error(
                        null,
                        Messages.getString("JobFlowDriver.errorInvalidInputName"), //$NON-NLS-1$
                        parameter.getPosition(),
                        name);
                return;
            }
            Class<? extends ImporterDescription> aClass = parameter.importer.description();
            ImporterDescription importer;
            try {
                importer = aClass.newInstance();
            } catch (Exception e) {
                error(
                        e,
                        Messages.getString("JobFlowDriver.errorImporterFailedToInstantiate"), //$NON-NLS-1$
                        aClass.getName(),
                        parameter.getPosition());
                return;
            }

            if (importer.getModelType() == null) {
                error(
                        null,
                        Messages.getString("JobFlowDriver.errorImporterMissingDataType"), //$NON-NLS-1$
                        aClass.getName(),
                        parameter.getPosition());
                return;
            }
            if (dataType.equals(importer.getModelType()) == false) {
                error(
                        null,
                        Messages.getString("JobFlowDriver.errorImporterInconsistentDataType"), //$NON-NLS-1$
                        aClass.getName(),
                        parameter.getPosition());
                return;
            }
            driver.createIn(name, importer);
        }
    }

    private void analyzeOutput(Parameter parameter, FlowDescriptionDriver driver) {
        assert parameter != null;
        assert driver != null;
        Type dataType = invoke(Out.class, parameter.type);
        if (dataType == null) {
            error(
                    null,
                    Messages.getString("JobFlowDriver.errorMissingTypeParameterInfo"), //$NON-NLS-1$
                    parameter.getPosition());
            return;
        }
        if (parameter.importer != null) {
            error(
                    null,
                    Messages.getString("JobFlowDriver.errorOutputWithImporter"), //$NON-NLS-1$
                    parameter.getPosition());
        }
        if (parameter.exporter == null) {
            error(
                    null,
                    Messages.getString("JobFlowDriver.errorMissingExporter"), //$NON-NLS-1$
                    parameter.getPosition());
            return;
        } else {
            String name = parameter.exporter.name();
            if (driver.isValidName(name) == false) {
                error(
                        null,
                        Messages.getString("JobFlowDriver.errorInvalidOutputName"), //$NON-NLS-1$
                        parameter.getPosition(),
                        name);
                return;
            }
            Class<? extends ExporterDescription> aClass = parameter.exporter.description();

            ExporterDescription exporter;
            try {
                exporter = aClass.newInstance();
            } catch (Exception e) {
                error(
                        e,
                        Messages.getString("JobFlowDriver.errorExporterFailedToInstantiate"), //$NON-NLS-1$
                        aClass.getName(),
                        parameter.getPosition());
                return;
            }

            if (exporter.getModelType() == null) {
                error(
                        null,
                        Messages.getString("JobFlowDriver.errorExporterMissingDataType"), //$NON-NLS-1$
                        aClass.getName(),
                        parameter.getPosition());
                return;
            }
            if (dataType.equals(exporter.getModelType()) == false) {
                error(
                        null,
                        Messages.getString("JobFlowDriver.errorExporterInconsistentDataType"), //$NON-NLS-1$
                        aClass.getName(),
                        parameter.getPosition());
                return;
            }
            driver.createOut(name, exporter);
        }
    }

    private void error(
            Throwable reason,
            String message,
            Object... args) {
        StringBuilder buf = new StringBuilder();
        buf.append(format(message, args));
        buf.append(" - "); //$NON-NLS-1$
        buf.append(description.getName());
        String text = buf.toString();
        diagnostics.add(text);
        if (reason == null) {
            LOG.error(text);
        } else {
            LOG.error(text, reason);
        }
    }

    private String format(String message, Object... args) {
        assert message != null;
        assert args != null;
        if (args.length == 0) {
            return message;
        } else {
            return MessageFormat.format(message, args);
        }
    }

    private Type invoke(Class<?> target, Type subtype) {
        assert target != null;
        assert subtype != null;
        List<Type> invoked = TypeUtil.invoke(target, subtype);
        if (invoked == null || invoked.size() != 1) {
            return null;
        }
        return invoked.get(0);
    }

    private List<Parameter> parseRawParameters(Constructor<?> ctor) {
        assert ctor != null;
        Class<?>[] rawTypes = ctor.getParameterTypes();
        Type[] types = ctor.getGenericParameterTypes();
        Annotation[][] annotations = ctor.getParameterAnnotations();
        List<Parameter> results = new ArrayList<>();
        for (int i = 0; i < types.length; i++) {
            Import importer = null;
            Export expoter = null;
            for (Annotation a : annotations[i]) {
                if (a.annotationType() == Import.class) {
                    importer = (Import) a;
                } else if (a.annotationType() == Export.class) {
                    expoter = (Export) a;
                }
            }
            results.add(new Parameter(
                    i,
                    rawTypes[i],
                    types[i],
                    importer,
                    expoter));
        }
        return results;
    }

    private static class Parameter {

        final int index;

        final Class<?> raw;

        final Type type;

        final Import importer;

        final Export exporter;

        Parameter(
                int index,
                Class<?> raw,
                Type type,
                Import importer,
                Export exporter) {
            assert raw != null;
            assert type != null;
            this.index = index;
            this.raw = raw;
            this.type = type;
            this.importer = importer;
            this.exporter = exporter;
        }

        int getPosition() {
            return index + 1;
        }
    }
}
