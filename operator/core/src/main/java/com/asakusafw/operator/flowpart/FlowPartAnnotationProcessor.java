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
package com.asakusafw.operator.flowpart;

import java.util.Collection;
import java.util.Collections;
import java.util.EnumSet;
import java.util.Set;

import javax.annotation.processing.ProcessingEnvironment;
import javax.annotation.processing.RoundEnvironment;
import javax.lang.model.element.TypeElement;
import javax.lang.model.util.ElementFilter;

import com.asakusafw.operator.AbstractOperatorAnnotationProcessor;
import com.asakusafw.operator.CompileEnvironment;
import com.asakusafw.operator.Constants;
import com.asakusafw.operator.model.OperatorClass;
import com.asakusafw.operator.util.Logger;

/**
 * Process flow-part operators.
 */
public class FlowPartAnnotationProcessor extends AbstractOperatorAnnotationProcessor {

    static final Logger LOG = Logger.get(FlowPartAnnotationProcessor.class);

    static final Collection<CompileEnvironment.Support> FEATURES = Collections.unmodifiableSet(EnumSet.of(
            CompileEnvironment.Support.FORCE_REGENERATE_RESOURCES,
            CompileEnvironment.Support.FLOWPART_EXTERNAL_IO));

    @Override
    protected CompileEnvironment createCompileEnvironment(ProcessingEnvironment processingEnv) {
        return CompileEnvironment.newInstance(
                processingEnv,
                CompileEnvironment.Support.DATA_MODEL_REPOSITORY);
    }

    @Override
    protected Collection<CompileEnvironment.Support> getSupportedFeatures() {
        return FEATURES;
    }

    @Override
    public Set<String> getSupportedAnnotationTypes() {
        return Collections.singleton(Constants.TYPE_FLOW_PART.getClassName());
    }

    @Override
    protected void run(Set<? extends TypeElement> annotations, RoundEnvironment roundEnv) {
        assert annotations != null;
        assert roundEnv != null;
        FlowPartAnalyzer analyzer = new FlowPartAnalyzer(environment);
        for (TypeElement annotation : annotations) {
            Set<TypeElement> types = ElementFilter.typesIn(roundEnv.getElementsAnnotatedWith(annotation));
            for (TypeElement type : types) {
                analyzer.register(type);
            }
        }
        Collection<OperatorClass> operatorClasses = analyzer.resolve();
        LOG.debug("found {} flow-part classes", operatorClasses.size()); //$NON-NLS-1$
        FlowPartFactoryEmitter emitter = new FlowPartFactoryEmitter(environment);
        for (OperatorClass aClass : operatorClasses) {
            LOG.debug("emitting support class: {}", aClass.getDeclaration().getQualifiedName()); //$NON-NLS-1$
            emitter.emit(aClass);
        }
    }
}
