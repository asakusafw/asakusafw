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
package com.asakusafw.dmdl.java.emitter;

import static org.hamcrest.Matchers.*;
import static org.junit.Assert.*;

import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.List;
import java.util.Map;

import org.apache.hadoop.io.Text;
import org.junit.Test;

import com.asakusafw.dmdl.java.GeneratorTesterRoot;
import com.asakusafw.dmdl.java.emitter.driver.ProjectionDriver;
import com.asakusafw.runtime.value.IntOption;
import com.asakusafw.runtime.value.LongOption;

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

    /**
     * projection test.
     * @throws Exception if failed
     */
    @Test
    public void projection_reference() throws Exception {
        ModelLoader loader = generate();
        Class<?> projection = loader.modelType("Projection");

        Type list = projection.getMethod("getRefList").getGenericReturnType();
        assertType(list, List.class, IntOption.class);

        Type map = projection.getMethod("getRefMap").getGenericReturnType();
        assertType(map, Map.class, String.class, LongOption.class);
    }

    private static void assertType(Type actual, Class<?> raw, Class<?>... arguments) {
        assertThat(actual, is(instanceOf(ParameterizedType.class)));

        ParameterizedType a = (ParameterizedType) actual;
        Type actualRaw = a.getRawType();
        Type[] actualArgs = a.getActualTypeArguments();
        assertThat(actualRaw, equalTo(raw));
        assertThat(actualArgs, equalTo(arguments));
    }
}
