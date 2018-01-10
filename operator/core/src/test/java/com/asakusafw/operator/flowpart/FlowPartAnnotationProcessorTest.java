/**
 * Copyright 2011-2018 Asakusa Framework Team.
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

import static org.hamcrest.Matchers.*;
import static org.junit.Assert.*;

import org.junit.Test;

import com.asakusafw.operator.Constants;
import com.asakusafw.operator.MockSource;
import com.asakusafw.operator.OperatorCompilerTestRoot;
import com.asakusafw.operator.StringDataModelMirrorRepository;

/**
 * Test for {@link FlowPartAnnotationProcessor}.
 */
public class FlowPartAnnotationProcessorTest extends OperatorCompilerTestRoot {

    /**
     * simple case.
     */
    @Test
    public void simple() {
        Object factory = compile("com.example.Simple");
        Object node = invoke(factory, "create", MockSource.of(String.class));
        assertThat(field(node.getClass(), "out"), is(notNullValue()));
    }

    private Object compile(String name) {
        add(new StringDataModelMirrorRepository());
        add(name);
        ClassLoader classLoader = start(flowPartProcessor());
        Object factory = create(classLoader, Constants.getFactoryClass(name));
        return factory;
    }
}
