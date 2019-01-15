/**
 * Copyright 2011-2019 Asakusa Framework Team.
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
import java.util.Collection;
import java.util.Collections;
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

import com.asakusafw.operator.util.Logger;

/**
 * Abstract implementation of Asakusa operator annotation processor.
 */
public abstract class AbstractOperatorAnnotationProcessor implements Processor {

    static final Logger LOG = Logger.get(AbstractOperatorAnnotationProcessor.class);

    /**
     * The current compile environment.
     */
    protected volatile CompileEnvironment environment;

    /**
     * Creates a new instance.
     */
    protected AbstractOperatorAnnotationProcessor() {
        LOG.debug("creating operator annotation processor: {}", this); //$NON-NLS-1$
    }

    @Override
    public final void init(ProcessingEnvironment processingEnv) {
        LOG.debug("initializing operator annotation processor: {}", this); //$NON-NLS-1$
        try {
            this.environment = createCompileEnvironment(processingEnv);
        } catch (RuntimeException e) {
            processingEnv.getMessager().printMessage(
                    Diagnostic.Kind.ERROR,
                    MessageFormat.format(
                            Messages.getString("AbstractOperatorAnnotationProcessor.errorFailInitialize"), //$NON-NLS-1$
                            e.toString()));
            LOG.error(Messages.getString("AbstractOperatorAnnotationProcessor.logFailInitialize"), e); //$NON-NLS-1$
        } catch (LinkageError e) {
            processingEnv.getMessager().printMessage(
                    Diagnostic.Kind.ERROR,
                    MessageFormat.format(
                            Messages.getString("AbstractOperatorAnnotationProcessor.errorFailLinkage"), //$NON-NLS-1$
                            e.toString()));
            LOG.error(Messages.getString("AbstractOperatorAnnotationProcessor.logFailLinkage"), e); //$NON-NLS-1$
            throw e;
        }
    }

    /**
     * Creates a compile environment for this processing (for testing).
     * @param processingEnv current processing environment
     * @return created environment
     */
    protected abstract CompileEnvironment createCompileEnvironment(ProcessingEnvironment processingEnv);

    @Override
    public final Set<String> getSupportedOptions() {
        return CompilerOption.getOptionNames(getSupportedFeatures());
    }

    /**
     * Returns the supported features.
     * @return the supported features
     */
    protected Collection<? extends CompileEnvironment.Support> getSupportedFeatures() {
        return Collections.emptySet();
    }

    @Override
    public final SourceVersion getSupportedSourceVersion() {
        return Constants.getSupportedSourceVersion();
    }

    @Override
    public Iterable<? extends Completion> getCompletions(
            Element element,
            AnnotationMirror annotation,
            ExecutableElement member,
            String userText) {
        return Collections.emptySet();
    }

    @Override
    public final boolean process(Set<? extends TypeElement> annotations, RoundEnvironment roundEnv) {
        if (environment == null) {
            return false;
        }
        LOG.debug("starting operator annotation processor: {}", this); //$NON-NLS-1$
        try {
            if (annotations.isEmpty() == false) {
                run(annotations, roundEnv);
            }
        } catch (RuntimeException e) {
            environment.getProcessingEnvironment().getMessager().printMessage(
                    Diagnostic.Kind.ERROR,
                    MessageFormat.format(
                            Messages.getString("AbstractOperatorAnnotationProcessor.errorFailCompile"), //$NON-NLS-1$
                            e.toString()));
            LOG.error(Messages.getString("AbstractOperatorAnnotationProcessor.logFailCompile"), e); //$NON-NLS-1$
        }
        return false;
    }

    /**
     * Runs this annotation processor.
     * @param annotations the target operator annotations
     * @param roundEnv the processing round environment
     */
    protected abstract void run(Set<? extends TypeElement> annotations, RoundEnvironment roundEnv);
}
