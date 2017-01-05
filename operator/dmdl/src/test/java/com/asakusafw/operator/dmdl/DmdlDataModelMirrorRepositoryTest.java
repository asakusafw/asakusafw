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
package com.asakusafw.operator.dmdl;

import static org.hamcrest.Matchers.*;
import static org.junit.Assert.*;

import org.junit.Rule;
import org.junit.Test;

import com.asakusafw.operator.AnnotationProcessing;
import com.asakusafw.operator.Constants;
import com.asakusafw.operator.OperatorCompilerTestRoot;
import com.asakusafw.operator.model.DataModelMirror;
import com.asakusafw.operator.model.PropertyMirror;
import com.asakusafw.runtime.value.DecimalOption;
import com.asakusafw.runtime.value.FloatOption;
import com.asakusafw.runtime.value.IntOption;

/**
 * Test for {@link DmdlDataModelMirrorRepository}.
 */
public class DmdlDataModelMirrorRepositoryTest {

    /**
     * APT emulator.
     */
    @Rule
    public final AnnotationProcessing apt = new AnnotationProcessing() {
        @Override
        protected void beforeCompile(OperatorCompilerTestRoot runner) {
            runner.addSpiDataModelMirrorRepositories(DmdlDataModelMirrorRepositoryTest.class.getClassLoader());
            runner.add("THolder", String.format("public abstract class THolder<T extends %s> extends %s {}",
                    MockIntProjection.class.getName(),
                    Constants.TYPE_FLOW_DESCRIPTION.getClassName()));
            runner.add("TOther", String.format("public abstract class TOther<T extends %s> extends %s {}",
                    MockFloatProjection.class.getName(),
                    Constants.TYPE_FLOW_DESCRIPTION.getClassName()));
        }
    };

    /**
     * Loads a concrete DMDL data model.
     */
    @Test
    public void load_concrete() {
        DataModelMirror mirror = apt.env.findDataModel(apt.getType(MockDataModel.class));
        assertThat(mirror, is(notNullValue()));

        assertThat(mirror.getKind(), is(DataModelMirror.Kind.CONCRETE));

        PropertyMirror pInt = mirror.findProperty("int");
        assertThat(pInt, is(notNullValue()));
        assertThat(pInt.getType(), is(apt.sameType(apt.getType(IntOption.class))));

        PropertyMirror pFloat = mirror.findProperty("float");
        assertThat(pFloat, is(notNullValue()));
        assertThat(pFloat.getType(), is(apt.sameType(apt.getType(FloatOption.class))));

        PropertyMirror pDecimal = mirror.findProperty("mutiple_segments_named");
        assertThat(pDecimal, is(notNullValue()));
        assertThat(pDecimal.getType(), is(apt.sameType(apt.getType(DecimalOption.class))));
    }

    /**
     * Loads a partial DMDL data model.
     */
    @Test
    public void load_partial() {
        DataModelMirror mirror = apt.env.findDataModel(apt.getTypeVariable("THolder", "T"));
        assertThat(mirror, is(notNullValue()));

        assertThat(mirror.getKind(), is(DataModelMirror.Kind.PARTIAL));

        PropertyMirror pInt = mirror.findProperty("int");
        assertThat(pInt, is(notNullValue()));
        assertThat(pInt.getType(), is(apt.sameType(apt.getType(IntOption.class))));
    }

    /**
     * Loads a not DMDL model class.
     */
    @Test
    public void load_normal_class() {
        DataModelMirror mirror = apt.env.findDataModel(apt.getType(String.class));
        assertThat(mirror, is(nullValue()));
    }

    /**
     * Loads a not DMDL model variable.
     */
    @Test
    public void load_normal_variable() {
        DataModelMirror mirror = apt.env.findDataModel(apt.getTypeVariable("TOther", "T"));
        assertThat(mirror, is(nullValue()));
    }
}
