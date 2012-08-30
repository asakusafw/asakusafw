/**
 * Copyright 2011-2012 Asakusa Framework Team.
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
package com.asakusafw.utils.java.jsr199.testing;

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

/**
 * 発生した例外情報を保持するプロセッサ。
 */
public class SafeProcessor implements Processor {

    private Processor delegate;

    private RuntimeException runtimeException;

    private Error error;

    /**
     * インスタンスを生成する。
     * @param delegate 委譲先のプロセッサ
     * @throws IllegalArgumentException 引数に{@code null}が含まれる場合
     */
    public SafeProcessor(Processor delegate) {
        if (delegate == null) {
            throw new IllegalArgumentException("delegate must not be null"); //$NON-NLS-1$
        }
        this.delegate = delegate;
    }

    /**
     * {@link #process(Set, RoundEnvironment)}で実行されたエラーをスローする。
     */
    public void rethrow() {
        if (runtimeException != null) {
            throw runtimeException;
        } else if (error != null) {
            throw error;
        }
    }

    @Override
    public void init(ProcessingEnvironment processingEnv) {
        this.delegate.init(processingEnv);
    }

    @Override
    public Set<String> getSupportedOptions() {
        return this.delegate.getSupportedOptions();
    }

    @Override
    public Set<String> getSupportedAnnotationTypes() {
        return this.delegate.getSupportedAnnotationTypes();
    }

    @Override
    public SourceVersion getSupportedSourceVersion() {
        return this.delegate.getSupportedSourceVersion();
    }

    @Override
    public boolean process(
            Set<? extends TypeElement> annotations,
            RoundEnvironment roundEnv) {
        try {
            return this.delegate.process(annotations, roundEnv);
        } catch (RuntimeException e) {
            runtimeException = e;
            throw e;
        } catch (Error e) {
            error = e;
            throw e;
        }
    }

    @Override
    public Iterable<? extends Completion> getCompletions(
            Element element,
            AnnotationMirror annotation,
            ExecutableElement member,
            String userText) {
        return this.delegate.getCompletions(element, annotation, member, userText);
    }
}
