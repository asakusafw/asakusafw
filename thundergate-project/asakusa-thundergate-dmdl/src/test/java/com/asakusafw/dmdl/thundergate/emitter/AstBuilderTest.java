/**
 * Copyright 2011-2016 Asakusa Framework Team.
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
package com.asakusafw.dmdl.thundergate.emitter;

import static org.hamcrest.Matchers.*;
import static org.junit.Assert.*;

import org.junit.Test;

/**
 * Test for {@link AstBuilder}.
 */
public class AstBuilderTest {

    /**
     * Test method for {@link AstBuilder#toDmdlName(String)}.
     */
    @Test
    public void toDmdlName() {
        assertThat(AstBuilder.toDmdlName("HELLO").identifier, is("hello"));
        assertThat(AstBuilder.toDmdlName("HELLO_WORLD").identifier, is("hello_world"));
        assertThat(AstBuilder.toDmdlName("__HELLO__").identifier, is("hello"));
    }
}
