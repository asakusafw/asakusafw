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

import static org.hamcrest.Matchers.*;
import static org.junit.Assert.*;

import javax.annotation.processing.ProcessingEnvironment;
import javax.annotation.processing.RoundEnvironment;
import javax.lang.model.element.TypeElement;
import javax.lang.model.type.TypeMirror;
import javax.lang.model.util.Elements;
import javax.lang.model.util.Types;

import com.asakusafw.utils.java.model.util.Models;

/**
 * A simple callback object for handling operator processors.
 */
public abstract class Callback {

    private RuntimeException runtimeException;

    private Error error;

    /**
     * the environment object.
     */
    protected OperatorCompilingEnvironment env;

    /**
     * the type environment.
     */
    protected Types types;

    /**
     * the element environment.
     */
    protected Elements elements;

    /**
     * the round environment.
     */
    protected RoundEnvironment round;

    /**
     * Invokes {@link #test()}.
     * @param pEnv the processing environment
     * @param rEnv the round environment
     */
    public void run(ProcessingEnvironment pEnv, RoundEnvironment rEnv) {
        this.env = new OperatorCompilingEnvironment(
                pEnv,
                Models.getModelFactory(),
                OperatorCompilerOptions.parse(pEnv.getOptions()));
        this.round = rEnv;
        this.types = pEnv.getTypeUtils();
        this.elements = pEnv.getElementUtils();
        try {
            test();
        } catch (RuntimeException e) {
            this.runtimeException = e;
        } catch (Error e) {
            this.error = e;
        }
    }

    /**
     * Re-throws exceptions during {@link #test()}.
     */
    public void rethrow() {
        if (runtimeException != null) {
            throw runtimeException;
        } else if (error != null) {
            throw error;
        }
    }

    /**
     * Executes tests.
     */
    protected abstract void test();

    /**
     * Returns a type mirror.
     * @param klass the target type
     * @param arguments the type arguments
     * @return the type mirror
     */
    protected TypeMirror getType(Class<?> klass, TypeMirror...arguments) {
        TypeElement type = elements.getTypeElement(klass.getName());
        assertThat(klass.getName(), type, not(nullValue()));
        if (arguments.length == 0) {
            return types.erasure(type.asType());
        } else {
            return types.getDeclaredType(type, arguments);
        }
    }
}
