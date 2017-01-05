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

import java.io.PrintWriter;
import java.io.StringWriter;
import java.lang.annotation.Annotation;
import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.ServiceLoader;
import java.util.Set;

import javax.annotation.processing.Completion;
import javax.annotation.processing.ProcessingEnvironment;
import javax.annotation.processing.Processor;
import javax.annotation.processing.RoundEnvironment;
import javax.lang.model.SourceVersion;
import javax.lang.model.element.AnnotationMirror;
import javax.lang.model.element.Element;
import javax.lang.model.element.ExecutableElement;
import javax.lang.model.element.TypeElement;
import javax.tools.Diagnostic;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.asakusafw.compiler.common.Precondition;
import com.asakusafw.utils.collections.Sets;
import com.asakusafw.utils.java.model.util.Models;

/**
 * An implementation of Java annotation processor which generates a support class for operator classes.
 * <p>
 * This annotation processor can enhance by using {@link com.asakusafw.compiler.operator.OperatorProcessor}.
 * Developers can inherit it and put the sub-class name into
 * {@code META-INF/services/com.asakusafw.compiler.operator.OperatorProcessor} to use the custom operators.
 * </p>
 * @since 0.1.0
 * @version 0.7.0
 */
public class OperatorCompiler implements Processor {

    /**
     * The compiler version.
     */
    public static final String VERSION = "0.1.0"; //$NON-NLS-1$

    static final Logger LOG = LoggerFactory.getLogger(OperatorCompiler.class);

    private OperatorCompilingEnvironment environment;

    private Set<OperatorProcessor> subProcessors;

    @Override
    public void init(ProcessingEnvironment processingEnv) {
        try {
            OperatorCompilerOptions options = loadOptions(processingEnv);
            this.environment = new OperatorCompilingEnvironment(
                    processingEnv,
                    Models.getModelFactory(),
                    options);
            this.subProcessors = loadSubProcessors(environment);
        } catch (RuntimeException e) {
            environment.getMessager().printMessage(
                    Diagnostic.Kind.ERROR,
                    e.getMessage());
            LOG.error(e.getMessage(), e);
        }
    }

    /**
     * Returns the compiler options.
     * @param processingEnv the annotation processing environment
     * @return the compiler options
     * @throws IllegalArgumentException if the parameter is {@code null}
     * @throws OperatorCompilerException if error occurred while extracting operator compiler options
     */
    protected OperatorCompilerOptions loadOptions(ProcessingEnvironment processingEnv) {
        Precondition.checkMustNotBeNull(processingEnv, "processingEnv"); //$NON-NLS-1$
        return OperatorCompilerOptions.parse(processingEnv.getOptions());
    }

    private Set<OperatorProcessor> loadSubProcessors(OperatorCompilingEnvironment env) {
        assert env != null;
        Map<Class<?>, OperatorProcessor> results = new HashMap<>();
        for (OperatorProcessor proc : findOperatorProcessors(env)) {
            proc.initialize(env);
            Class<? extends Annotation> target = proc.getTargetAnnotationType();
            if (target == null) {
                env.getMessager().printMessage(
                    Diagnostic.Kind.WARNING,
                    MessageFormat.format(
                        Messages.getString("OperatorCompiler.warnSkipInvalidProcessor"), //$NON-NLS-1$
                        proc.getClass().getName()));
            } else if (results.containsKey(target)) {
                env.getMessager().printMessage(
                        Diagnostic.Kind.WARNING,
                        MessageFormat.format(
                            Messages.getString("OperatorCompiler.warnSkipConflictProcessor"), //$NON-NLS-1$
                            proc.getClass().getName(),
                            target.getName(),
                            results.get(target).getClass().getName()));
            } else {
                results.put(target, proc);
            }
        }
        return Sets.from(results.values());
    }

    /**
     * Returns the available operator processors.
     * @param env the current environment
     * @return the available operator processors
     * @throws IllegalArgumentException if the parameter is {@code null}
     */
    protected Iterable<OperatorProcessor> findOperatorProcessors(OperatorCompilingEnvironment env) {
        List<OperatorProcessor> results = new ArrayList<>();
        Iterator<OperatorProcessor> iter = ServiceLoader
            .load(OperatorProcessor.class, env.getServiceClassLoader())
            .iterator();
        while (iter.hasNext()) {
            try {
                results.add(iter.next());
            } catch (RuntimeException e) {
                environment.getMessager().printMessage(Diagnostic.Kind.ERROR, toDetailString(e));
            }
        }
        return results;
    }

    @Override
    public Set<String> getSupportedOptions() {
        return Collections.emptySet();
    }

    /**
     * Returns the current supported source version.
     * @return the supported version
     * @since 0.5.3
     */
    public static SourceVersion computeSupportedVersion() {
        return SourceVersion.latestSupported();
    }

    @Override
    public SourceVersion getSupportedSourceVersion() {
        return computeSupportedVersion();
    }

    @Override
    public Set<String> getSupportedAnnotationTypes() {
        Set<String> results = new HashSet<>();
        for (OperatorProcessor proc : subProcessors) {
            Class<? extends Annotation> type = proc.getTargetAnnotationType();
            results.add(type.getName());
        }
        return results;
    }

    @Override
    public Iterable<? extends Completion> getCompletions(
            Element element,
            AnnotationMirror annotation,
            ExecutableElement member,
            String userText) {
        return Collections.emptyList();
    }

    @Override
    public boolean process(
            Set<? extends TypeElement> annotations,
            RoundEnvironment roundEnv) {
        assert annotations != null;
        assert roundEnv != null;
        if (annotations.isEmpty()) {
            return false;
        }

        try {
            start(roundEnv);
        } catch (OperatorCompilerException e) {
            if (e.getKind() != null) {
                environment.getMessager().printMessage(
                        e.getKind(),
                        e.getMessage());
            }
            LOG.debug(e.getMessage(), e);
        } catch (RuntimeException e) {
            environment.getMessager().printMessage(Diagnostic.Kind.ERROR, toDetailString(e));
        }
        return false;
    }

    private String toDetailString(RuntimeException e) {
        StringWriter writer = new StringWriter();
        try (PrintWriter pw = new PrintWriter(writer)) {
            pw.println(Messages.getString("OperatorCompiler.errorDetailHeader")); //$NON-NLS-1$
            e.printStackTrace(pw);
        }
        return writer.toString();
    }

    private void start(RoundEnvironment roundEnv) {
        assert roundEnv != null;

        // collects operator classes
        OperatorClassCollector collector = new OperatorClassCollector(environment, roundEnv);
        for (OperatorProcessor proc : subProcessors) {
            collector.add(proc);
        }
        List<OperatorClass> classes = collector.collect();

        // emits operator factory/implementation classes
        OperatorClassEmitter emitter = new OperatorClassEmitter(environment);
        for (OperatorClass operatorClass : classes) {
            emitter.emit(operatorClass);
        }
    }
}
