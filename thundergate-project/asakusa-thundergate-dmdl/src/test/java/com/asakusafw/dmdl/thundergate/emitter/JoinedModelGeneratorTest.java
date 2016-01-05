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

import java.util.Arrays;

import org.apache.hadoop.io.Text;
import org.hamcrest.BaseMatcher;
import org.hamcrest.Description;
import org.hamcrest.Matcher;
import org.junit.Before;
import org.junit.Test;

import com.asakusafw.dmdl.java.emitter.CompositeDataModelDriver;
import com.asakusafw.dmdl.thundergate.Constants;
import com.asakusafw.dmdl.thundergate.GeneratorTesterRoot;
import com.asakusafw.dmdl.thundergate.model.Attribute;
import com.asakusafw.dmdl.thundergate.model.JoinedModelDescription;
import com.asakusafw.dmdl.thundergate.model.PropertyTypeKind;
import com.asakusafw.dmdl.thundergate.model.StringType;
import com.asakusafw.dmdl.thundergate.model.TableModelDescription;
import com.asakusafw.dmdl.thundergate.util.JoinedModelBuilder;
import com.asakusafw.dmdl.thundergate.util.TableModelBuilder;
import com.asakusafw.vocabulary.model.Joined;
import com.asakusafw.vocabulary.model.Key;

/**
 * Test for {@link JoinedModelGenerator}.
 */
public class JoinedModelGeneratorTest extends GeneratorTesterRoot {

    /**
     * Initializes the test.
     * @throws Exception if some errors were occurred
     */
    @Before
    public void setUp() throws Exception {
        emitDrivers.add(new CompositeDataModelDriver(getClass().getClassLoader()));
    }

    /**
     * simple join.
     */
    @Test
    public void simple() {
        TableModelDescription left = new TableModelBuilder("LEFT")
            .add(null, "SID", PropertyTypeKind.LONG, Attribute.PRIMARY_KEY)
            .add(null, "RIGHT_ID", PropertyTypeKind.LONG)
            .add(null, "VALUE", new StringType(255))
            .toDescription();
        TableModelDescription right = new TableModelBuilder("RIGHT")
            .add(null, "SID", PropertyTypeKind.LONG, Attribute.PRIMARY_KEY)
            .add(null, "VALUE", new StringType(255))
            .toDescription();
        JoinedModelDescription join = new JoinedModelBuilder("SIMPLE", left, "l", right, "r")
            .on("l.RIGHT_ID", "r.SID")
            .add("ID", "r.SID")
            .add("LEFT", "l.VALUE")
            .add("RIGHT", "r.VALUE")
            .toDescription();

        emitDmdl(RecordModelGenerator.generate(left));
        emitDmdl(RecordModelGenerator.generate(right));
        emitDmdl(JoinedModelGenerator.generate(join));

        ModelLoader loader = generateJava();
        loader.setNamespace(Constants.SOURCE_VIEW);

        ModelWrapper object = loader.newModel("Simple");

        object.set("id", 127L);
        assertThat(object.get("id"), eq(127L));
        object.set("left", new Text("Hello, left!"));
        assertThat(object.get("left"), eq(new Text("Hello, left!")));
        object.set("right", new Text("Hello, right!"));
        assertThat(object.get("right"), eq(new Text("Hello, right!")));

        Joined annotation = object.unwrap().getClass().getAnnotation(Joined.class);
        assertThat(annotation, not(nullValue()));

        assertThat(annotation.terms().length, is(2));

        loader.setNamespace(Constants.SOURCE_TABLE);
        Joined.Term a = annotation.terms()[0];
        assertThat(a.source(), eq(loader.modelType("Left")));
        assertThat(a.mappings().length, is(2));
        assertThat(a.mappings(), hasItemInArray(mapping("rightId", "id")));
        assertThat(a.mappings(), hasItemInArray(mapping("value", "left")));
        assertThat(a.shuffle(), is(grouping("rightId")));

        Joined.Term b = annotation.terms()[1];
        assertThat(b.source(), eq(loader.modelType("Right")));
        assertThat(b.mappings().length, is(2));
        assertThat(b.mappings(), hasItemInArray(mapping("sid", "id")));
        assertThat(b.mappings(), hasItemInArray(mapping("value", "right")));
        assertThat(b.shuffle(), is(grouping("sid")));
    }

    private Matcher<Key> grouping(final String... properties) {
        return new BaseMatcher<Key>() {
            @Override
            public boolean matches(Object object) {
                if (object instanceof Key) {
                    Key elem = (Key) object;
                    if (Arrays.equals(elem.group(), properties) == false) {
                        return false;
                    }
                }
                return true;
            }
            @Override
            public void describeTo(Description desc) {
                desc.appendText(Arrays.toString(properties));
            }
        };
    }

    private Matcher<Joined.Mapping> mapping(final String src, final String dst) {
        return new BaseMatcher<Joined.Mapping>() {
            @Override
            public boolean matches(Object object) {
                if (object instanceof Joined.Mapping) {
                    Joined.Mapping elem = (Joined.Mapping) object;
                    if (src.equals(elem.source()) == false) {
                        return false;
                    }
                    if (dst.equals(elem.destination()) == false) {
                        return false;
                    }
                }
                return true;
            }
            @Override
            public void describeTo(Description desc) {
                desc.appendText(src + " -> " + dst);
            }
        };
    }

    private Matcher<Object> eq(final Object value) {
        return is(value);
    }
}
