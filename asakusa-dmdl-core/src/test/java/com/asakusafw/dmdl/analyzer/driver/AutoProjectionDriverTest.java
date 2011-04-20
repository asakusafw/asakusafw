/**
 * Copyright 2011 Asakusa Framework Team.
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

import java.util.Collections;
import java.util.List;

import org.junit.Before;
import org.junit.Test;

import com.asakusafw.dmdl.DmdlTesterRoot;
import com.asakusafw.dmdl.semantics.DmdlSemantics;
import com.asakusafw.dmdl.semantics.ModelDeclaration;
import com.asakusafw.dmdl.semantics.ModelSymbol;
import com.asakusafw.dmdl.semantics.trait.ProjectionsTrait;

/**
 * Test for {@link AutoProjectionDriver}.
 */
public class AutoProjectionDriverTest extends DmdlTesterRoot {

    /**
     * Initializes the test.
     * @throws Exception if some errors were occurred
     */
    @Before
    public void setUp() throws Exception {
        attributeDrivers.add(new AutoProjectionDriver());
    }

    /**
     * auto projection.
     */
    @Test
    public void auto_projection() {
        DmdlSemantics world = resolve();
        ModelDeclaration model = world.findModelDeclaration("simple");
        assertThat(model.getSymbol(), is(model("simple")));

        List<ModelSymbol> projections = projections(model);
        assertThat(projections.size(), is(1));
        assertThat(projections, hasItem(model("p1")));
    }

    /**
     * includes subset.
     */
    @Test
    public void auto_projection_subset() {
        DmdlSemantics world = resolve();
        ModelDeclaration model = world.findModelDeclaration("simple");
        assertThat(model.getSymbol(), is(model("simple")));

        List<ModelSymbol> projections = projections(model);
        assertThat(projections.size(), is(1));
        assertThat(projections, hasItem(model("p1")));
    }

    /**
     * does not include superset.
     */
    @Test
    public void auto_projection_superset() {
        DmdlSemantics world = resolve();
        ModelDeclaration model = world.findModelDeclaration("simple");
        assertThat(model.getSymbol(), is(model("simple")));

        List<ModelSymbol> projections = projections(model);
        assertThat(projections.size(), is(0));
    }

    /**
     * does not include superset.
     */
    @Test
    public void auto_projection_incompatible() {
        DmdlSemantics world = resolve();
        ModelDeclaration model = world.findModelDeclaration("simple");
        assertThat(model.getSymbol(), is(model("simple")));

        List<ModelSymbol> projections = projections(model);
        assertThat(projections.size(), is(0));
    }

    /**
     * only includes projective models.
     */
    @Test
    public void auto_projection_record() {
        DmdlSemantics world = resolve();
        ModelDeclaration model = world.findModelDeclaration("simple");
        assertThat(model.getSymbol(), is(model("simple")));

        List<ModelSymbol> projections = projections(model);
        assertThat(projections.size(), is(0));
    }

    /**
     * already includes some projections.
     */
    @Test
    public void auto_projection_already() {
        DmdlSemantics world = resolve();
        ModelDeclaration model = world.findModelDeclaration("simple");
        assertThat(model.getSymbol(), is(model("simple")));

        List<ModelSymbol> projections = projections(model);
        assertThat(projections.size(), is(2));
        assertThat(projections, hasItem(model("p1")));
        assertThat(projections, hasItem(model("p2")));
    }

    /**
     * auto projection for summarization.
     */
    @Test
    public void auto_projection_summarize() {
        DmdlSemantics world = resolve();
        ModelDeclaration model = world.findModelDeclaration("simple");
        assertThat(model.getSymbol(), is(model("simple")));

        List<ModelSymbol> projections = projections(model);
        assertThat(projections.size(), is(1));
        assertThat(projections, hasItem(model("total")));
    }

    /**
     * attribute is attached to property.
     */
    @Test
    public void invalid_auto_projection_property() {
        shouldSemanticError();
    }

    /**
     * extra element.
     */
    @Test
    public void invalid_auto_projection_extra() {
        shouldSemanticError();
    }

    private List<ModelSymbol> projections(ModelDeclaration model) {
        ProjectionsTrait trait = model.getTrait(ProjectionsTrait.class);
        if (trait == null) {
            return Collections.emptyList();
        }
        return trait.getProjections();
    }
}
