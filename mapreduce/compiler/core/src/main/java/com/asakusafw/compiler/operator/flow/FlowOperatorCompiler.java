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
package com.asakusafw.compiler.operator.flow;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
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

import com.asakusafw.compiler.operator.OperatorCompiler;
import com.asakusafw.compiler.operator.OperatorCompilerException;
import com.asakusafw.compiler.operator.OperatorCompilerOptions;
import com.asakusafw.compiler.operator.OperatorCompilingEnvironment;
import com.asakusafw.utils.java.model.util.Models;
import com.asakusafw.vocabulary.flow.FlowPart;

/**
 * An implementation of Java annotation processor which generates a support class for flow-part classes.
 * @since 0.1.0
 * @version 0.7.0
 */
public class FlowOperatorCompiler implements Processor {

    /**
     * The compiler version.
     */
    public static final String VERSION = "0.1.0"; //$NON-NLS-1$

    static final Logger LOG = LoggerFactory.getLogger(FlowOperatorCompiler.class);

    private OperatorCompilingEnvironment environment;

    @Override
    public void init(ProcessingEnvironment processingEnv) {
        this.environment = new OperatorCompilingEnvironment(
                processingEnv,
                Models.getModelFactory(),
                OperatorCompilerOptions.parse(processingEnv.getOptions()));
    }

    @Override
    public Set<String> getSupportedOptions() {
        return Collections.emptySet();
    }

    @Override
    public Set<String> getSupportedAnnotationTypes() {
        Set<String> results = new HashSet<>();
        results.add(FlowPart.class.getName());
        return results;
    }

    @Override
    public SourceVersion getSupportedSourceVersion() {
        return OperatorCompiler.computeSupportedVersion();
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
            pw.println(Messages.getString("FlowOperatorCompiler.errorDetailHeader")); //$NON-NLS-1$
            e.printStackTrace(pw);
        }
        return writer.toString();
    }

    private void start(RoundEnvironment roundEnv) {
        assert roundEnv != null;
        FlowPartClassCollector collector = new FlowPartClassCollector(environment);
        for (Element element : roundEnv.getElementsAnnotatedWith(FlowPart.class)) {
            collector.add(element);
        }
        List<FlowPartClass> collected = collector.collect();
        FlowClassEmitter emitter = new FlowClassEmitter(environment);
        for (FlowPartClass aClass : collected) {
            emitter.emit(aClass);
        }
    }
}
