/**
 * Copyright 2011-2014 Asakusa Framework Team.
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

import java.util.Collections;
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
import com.asakusafw.utils.collections.Sets;
import com.asakusafw.utils.java.model.util.Models;
import com.asakusafw.vocabulary.flow.FlowPart;

/**
 * フロー部品をもとにした演算子ファクトリーを生成する。
 * @since 0.1.0
 * @version 0.5.3
 */
public class FlowOperatorCompiler implements Processor {

    /**
     * このコンパイラのバージョン。
     */
    public static final String VERSION = "0.0.1";

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
        Set<String> results = Sets.create();
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
            environment.getMessager().printMessage(
                    Diagnostic.Kind.ERROR,
                    e.getMessage());
            LOG.debug(e.getMessage(), e);
        }
        return false;
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
