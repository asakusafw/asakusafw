/**
 * Copyright 2011-2015 Asakusa Framework Team.
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
package com.asakusafw.dmdl.analyzer.driver;

import static org.hamcrest.Matchers.*;
import static org.junit.Assert.*;

import org.junit.Before;
import org.junit.Test;

import com.asakusafw.dmdl.DmdlTesterRoot;
import com.asakusafw.dmdl.semantics.DmdlSemantics;
import com.asakusafw.dmdl.semantics.ModelDeclaration;
import com.asakusafw.dmdl.semantics.trait.NamespaceTrait;

/**
 * Test for {@link NamespaceDriver}.
 */
public class NamespaceDriverTest extends DmdlTesterRoot {

    /**
     * Initializes the test.
     * @throws Exception if some errors were occurred
     */
    @Before
    public void setUp() throws Exception {
        attributeDrivers.add(new NamespaceDriver());
    }

    /**
     * simple namespace.
     */
    @Test
    public void namespace() {
        DmdlSemantics world = resolve();
        ModelDeclaration model = world.findModelDeclaration("simple");
        assertThat(model.getSymbol(), is(model("simple")));

        NamespaceTrait trait = model.getTrait(NamespaceTrait.class);
        assertThat(trait, not(nullValue()));
        assertThat(trait.getNamespace().toString(), is("com.example"));
    }

    /**
     * namespace is not specified.
     */
    @Test
    public void empty() {
        DmdlSemantics world = resolve();
        ModelDeclaration model = world.findModelDeclaration("simple");
        assertThat(model.getSymbol(), is(model("simple")));

        NamespaceTrait trait = model.getTrait(NamespaceTrait.class);
        assertThat(trait, nullValue());
    }

    /**
     * namespace is attached to property.
     */
    @Test
    public void invalid_namespace_property() {
        shouldSemanticError();
    }

    /**
     * missing element.
     */
    @Test
    public void invalid_namespace_empty() {
        shouldSemanticError();
    }

    /**
     * extra element.
     */
    @Test
    public void invalid_namespace_extra() {
        shouldSemanticError();
    }

    /**
     * inconsistent type.
     */
    @Test
    public void invalid_namespace_string() {
        shouldSemanticError();
    }
}
