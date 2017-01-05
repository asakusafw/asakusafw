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
package com.asakusafw.compiler.flow;

import static org.hamcrest.Matchers.*;
import static org.junit.Assert.*;

import org.junit.Test;

import com.asakusafw.utils.java.model.syntax.ModelFactory;
import com.asakusafw.utils.java.model.util.Models;

/**
 * Test for {@link DataClass}.
 */
public class DataClassTest {

    ModelFactory f = Models.getModelFactory();

    /**
     * test for {@link DataClass.Unresolved#getType()}.
     */
    @Test
    public void unresolved_getType() {
        DataClass dc = new DataClass.Unresolved(f, Void.class);
        assertThat(dc.getType(), is((Object) Void.class));
    }

    /**
     * test for {@link DataClass.Unresolved#findProperty(String)}.
     */
    @Test
    public void unresolved_findProperty() {
        DataClass dc = new DataClass.Unresolved(f, Void.class);
        assertThat(dc.findProperty("something"), is(nullValue()));
    }

    /**
     * test for {@link DataClass.Unresolved#createNewInstance(com.asakusafw.utils.java.model.syntax.Type)}.
     */
    @Test
    public void unresolved_createNewInstance() {
        DataClass dc = new DataClass.Unresolved(f, Void.class);
        assertThat(
                dc.createNewInstance(Models.toType(f, Void.class)),
                not(nullValue()));
    }

    /**
     * test for {@link DataClass.Unresolved#assign(com.asakusafw.utils.java.model.syntax.Expression, com.asakusafw.utils.java.model.syntax.Expression)}.
     */
    @Test
    public void unresolved_assign() {
        DataClass dc = new DataClass.Unresolved(f, Void.class);
        assertThat(
                dc.assign(f.newSimpleName("a"), f.newSimpleName("b")),
                not(nullValue()));
    }

    /**
     * test for {@link DataClass.Unresolved#createReader(com.asakusafw.utils.java.model.syntax.Expression, com.asakusafw.utils.java.model.syntax.Expression)}.
     */
    @Test
    public void unresolved_createReader() {
        DataClass dc = new DataClass.Unresolved(f, Void.class);
        assertThat(
                dc.createReader(f.newThis(), Models.toNullLiteral(f)),
                not(nullValue()));
    }

    /**
     * test for {@link DataClass.Unresolved#createWriter(com.asakusafw.utils.java.model.syntax.Expression, com.asakusafw.utils.java.model.syntax.Expression)}.
     */
    @Test
    public void unresolved_createWriter() {
        DataClass dc = new DataClass.Unresolved(f, Void.class);
        assertThat(
                dc.createWriter(f.newThis(), Models.toNullLiteral(f)),
                not(nullValue()));
    }
}
