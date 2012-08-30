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
package com.asakusafw.dmdl.java.emitter;

import static org.hamcrest.Matchers.*;
import static org.junit.Assert.*;

import org.apache.hadoop.io.Text;
import org.junit.Test;

import com.asakusafw.dmdl.java.GeneratorTesterRoot;
import com.asakusafw.dmdl.java.emitter.driver.ProjectionDriver;

/**
 * Test for {@link ProjectiveModelEmitter}.
 */
public class ProjectiveModelEmitterTest extends GeneratorTesterRoot {

    /**
     * projection test.
     */
    @Test
    public void projection() {
        emitDrivers.add(new ProjectionDriver());

        ModelLoader loader = generate();
        Class<?> projection = loader.modelType("Projection");
        ModelWrapper object = loader.newModel("Record");
        assertThat(object.unwrap(), instanceOf(projection));

        object.setInterfaceType(projection);

        object.set("hoge", 100);
        assertThat(object.get("hoge"), is((Object) 100));

        object.set("foo", new Text("Hello, Projections!"));
        assertThat(object.get("foo"), is((Object) new Text("Hello, Projections!")));
    }
}
