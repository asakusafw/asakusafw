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
package com.asakusafw.operator.method;

import static org.hamcrest.Matchers.*;
import static org.junit.Assert.*;

import java.util.Collections;

import javax.lang.model.element.TypeElement;

import org.junit.Test;

import com.asakusafw.operator.Callback;
import com.asakusafw.operator.Constants;
import com.asakusafw.operator.OperatorCompilerTestRoot;
import com.asakusafw.operator.description.ClassDescription;
import com.asakusafw.operator.model.OperatorClass;

/**
 * Test for {@link OperatorImplementationEmitter}.
 */
public class OperatorImplementationEmitterTest extends OperatorCompilerTestRoot {

    /**
     * an empty operator class.
     */
    @Test
    public void empty() {
        compile("com.example.Empty");
    }

    /**
     * no abstract methods.
     */
    @Test
    public void noAbstract() {
        Object object = compile("com.example.NoAbstract");
        assertThat(access(object, "field"), is((Object) 0));
        invoke(object, "method");
        assertThat(access(object, "field"), is((Object) 10));
    }

    /**
     * with abstract methods.
     */
    @Test
    public void abstractMethods() {
        compile("com.example.Abstract");
    }

    /**
     * with complex abstract method.
     */
    @Test
    public void complex() {
        compile("com.example.Complex");
    }

    /**
     * mixed methods.
     */
    @Test
    public void mixed() {
        Object object = compile("com.example.Mixed");
        invoke(object, "method", 100);
        assertThat(access(object, "field"), is((Object) 100));
    }

    private Object compile(String name) {
        add(name);
        ClassLoader classLoader = start(new Callback() {
            @Override
            protected void test() {
                TypeElement element = env.findTypeElement(new ClassDescription(name));
                if (round.getRootElements().contains(element)) {
                    assertThat(name, element, is(notNullValue()));
                    OperatorImplementationEmitter emitter = new OperatorImplementationEmitter(env);
                    emitter.emit(new OperatorClass(element, Collections.emptyList()));
                }
            }
        });
        return create(classLoader, Constants.getImplementationClass(name));
    }
}
