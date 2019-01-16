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
package com.asakusafw.operator.model;

import static org.hamcrest.Matchers.*;
import static org.junit.Assert.*;

import org.junit.Rule;
import org.junit.Test;

import com.asakusafw.operator.AnnotationProcessing;
import com.asakusafw.operator.MockPropertyMirrorCollector;
import com.asakusafw.operator.OperatorCompilerTestRoot;

/**
 * Test for {@link PartialDataModelMirror}.
 */
public class PartialDataModelMirrorTest {

    /**
     * APT emulator.
     */
    @Rule
    public final AnnotationProcessing apt = new AnnotationProcessing() {
        @Override
        protected void beforeCompile(OperatorCompilerTestRoot runner) {
            runner.add("TInteger", "public class TInteger<T extends Integer> {}");
            runner.add("TCharSequence", "public class TCharSequence<T extends CharSequence> {}");
            runner.add("TStringBuilder", "public class TStringBuilder<T extends StringBuilder> {}");
            runner.add("TMultiple", "public class TMultiple<S extends Number & CharSequence, T extends S> {}");
        }
    };

    /**
     * Test for identical.
     */
    @Test
    public void identical() {
        DataModelMirror a = new PartialDataModelMirror(
                apt.env,
                apt.getTypeVariable("TCharSequence", "T"),
                new MockPropertyMirrorCollector());
        DataModelMirror b = new PartialDataModelMirror(
                apt.env,
                apt.getTypeVariable("TCharSequence", "T"),
                new MockPropertyMirrorCollector());

        assertThat(a.getKind(), is(DataModelMirror.Kind.PARTIAL));

        assertThat(a.isSame(b), is(true));
        assertThat(a.canInvoke(b), is(true));
        assertThat(a.canContain(b), is(true));

        assertThat(b.isSame(a), is(true));
        assertThat(b.canInvoke(a), is(true));
        assertThat(b.canContain(a), is(true));
    }

    /**
     * Test for subtype.
     */
    @Test
    public void subtype() {
        DataModelMirror a = new PartialDataModelMirror(
                apt.env,
                apt.getTypeVariable("TStringBuilder", "T"),
                new MockPropertyMirrorCollector());
        DataModelMirror b = new PartialDataModelMirror(
                apt.env,
                apt.getTypeVariable("TCharSequence", "T"),
                new MockPropertyMirrorCollector());

        assertThat(a.isSame(b), is(false));
        assertThat(a.canInvoke(b), is(true));
        assertThat(a.canContain(b), is(true));

        assertThat(b.isSame(a), is(false));
        assertThat(b.canInvoke(a), is(false));
        assertThat(b.canContain(a), is(false));
    }

    /**
     * Test for different.
     */
    @Test
    public void different() {
        DataModelMirror a = new PartialDataModelMirror(
                apt.env,
                apt.getTypeVariable("TStringBuilder", "T"),
                new MockPropertyMirrorCollector());
        DataModelMirror b = new PartialDataModelMirror(
                apt.env,
                apt.getTypeVariable("TInteger", "T"),
                new MockPropertyMirrorCollector());

        assertThat(a.isSame(b), is(false));
        assertThat(a.canInvoke(b), is(false));
        assertThat(a.canContain(b), is(false));

        assertThat(b.isSame(a), is(false));
        assertThat(b.canInvoke(a), is(false));
        assertThat(b.canContain(a), is(false));
    }

    /**
     * Test for partial subtype.
     */
    @Test
    public void concrete_subtype() {
        DataModelMirror a = new PartialDataModelMirror(
                apt.env,
                apt.getTypeVariable("TStringBuilder", "T"),
                new MockPropertyMirrorCollector());
        DataModelMirror b = new ConcreteDataModelMirror(
                apt.env,
                apt.getType(CharSequence.class),
                new MockPropertyMirrorCollector());

        assertThat(a.isSame(b), is(false));
        assertThat(a.canInvoke(b), is(true));
        assertThat(a.canContain(b), is(false));
    }

    /**
     * Test for partial different.
     */
    @Test
    public void partial_different() {
        DataModelMirror a = new PartialDataModelMirror(
                apt.env,
                apt.getTypeVariable("TStringBuilder", "T"),
                new MockPropertyMirrorCollector());
        DataModelMirror b = new ConcreteDataModelMirror(
                apt.env,
                apt.getType(Integer.class),
                new MockPropertyMirrorCollector());

        assertThat(a.isSame(b), is(false));
        assertThat(a.canInvoke(b), is(false));
        assertThat(a.canContain(b), is(false));
    }

    /**
     * Test for findProperty.
     */
    @Test
    public void findProperty() {
        DataModelMirror type = new PartialDataModelMirror(
                apt.env,
                apt.getTypeVariable("TStringBuilder", "T"),
                new MockPropertyMirrorCollector());
        PropertyMirror property = type.findProperty("string_builder");
        assertThat(property.getName(), is(equalToIgnoringCase(StringBuilder.class.getSimpleName())));
        assertThat(property.getType(), is(apt.sameType(StringBuilder.class)));
    }

    /**
     * Test for findProperty with multiple properties.
     */
    @Test
    public void findProperty_multiple() {
        DataModelMirror type = new PartialDataModelMirror(
                apt.env,
                apt.getTypeVariable("TMultiple", "T"),
                new MockPropertyMirrorCollector());
        assertThat(type.findProperty("number"), is(notNullValue()));
        assertThat(type.findProperty("char_sequence"), is(notNullValue()));
    }
}
