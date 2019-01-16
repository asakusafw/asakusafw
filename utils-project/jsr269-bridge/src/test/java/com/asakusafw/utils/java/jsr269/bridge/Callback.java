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
package com.asakusafw.utils.java.jsr269.bridge;

import static org.hamcrest.Matchers.*;
import static org.junit.Assert.*;

import java.io.IOError;
import java.io.IOException;

import javax.annotation.processing.ProcessingEnvironment;
import javax.annotation.processing.RoundEnvironment;
import javax.lang.model.element.TypeElement;
import javax.lang.model.type.TypeMirror;
import javax.lang.model.util.Elements;
import javax.lang.model.util.Types;

/**
 * Call back from {@link DelegateProcessor}.
 */
public abstract class Callback {

    private RuntimeException runtimeException;

    private Error error;

    /**
     * The processing environment.
     */
    protected ProcessingEnvironment env;

    /**
     * The type environment.
     */
    protected Types types;

    /**
     * The element utilities.
     */
    protected Elements elements;

    /**
     * The current rounding environment.
     */
    protected RoundEnvironment round;

    /**
     * Runs {@link #test()} method.
     * @param env processing environment
     * @param round rounding environment
     */
    @SuppressWarnings("hiding")
    public void run(ProcessingEnvironment env, RoundEnvironment round) {
        this.env = env;
        this.round = round;
        this.types = env.getTypeUtils();
        this.elements = env.getElementUtils();
        try {
            test();
        } catch (IOException e) {
            this.error = new IOError(e);
        } catch (RuntimeException e) {
            this.runtimeException = e;
        } catch (Error e) {
            this.error = e;
        }
    }

    /**
     * Throws exceptions/errors which are thrown in {@link #test()}.
     */
    public void rethrow() {
        if (runtimeException != null) {
            throw runtimeException;
        } else if (error != null) {
            throw error;
        }
    }

    /**
     * Performs the test.
     * @throws IOException if compilation was failed
     */
    protected abstract void test() throws IOException;

    /**
     * Returns the type mirror.
     * @param aClass target raw type
     * @param arguments type arguments
     * @return the declared type
     */
    protected TypeMirror getType(Class<?> aClass, TypeMirror...arguments) {
        TypeElement type = elements.getTypeElement(aClass.getName());
        assertThat(aClass.getName(), type, not(nullValue()));
        if (arguments.length == 0) {
            return types.erasure(type.asType());
        } else {
            return types.getDeclaredType(type, arguments);
        }
    }
}
