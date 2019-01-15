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
 * Test for {@link ConcreteDataModelMirror}.
 */
public class ConcreteDataModelMirrorTest {

    /**
     * APT emulator.
     */
    @Rule
    public final AnnotationProcessing apt = new AnnotationProcessing() {
        @Override
        protected void beforeCompile(OperatorCompilerTestRoot runner) {
            runner.add("TCharSequence", "public class TCharSequence<T extends CharSequence> {}");
            runner.add("TStringBuilder", "public class TStringBuilder<T extends StringBuilder> {}");
        }
    };

    /**
     * Test for identical.
     */
    @Test
    public void identical() {
        DataModelMirror a = new ConcreteDataModelMirror(
                apt.env,
                apt.getType(String.class),
                new MockPropertyMirrorCollector());
        DataModelMirror b = new ConcreteDataModelMirror(
                apt.env,
                apt.getType(String.class),
                new MockPropertyMirrorCollector());
        assertThat(a.getKind(), is(DataModelMirror.Kind.CONCRETE));

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
        DataModelMirror a = new ConcreteDataModelMirror(
                apt.env,
                apt.getType(String.class),
                new MockPropertyMirrorCollector());
        DataModelMirror b = new ConcreteDataModelMirror(
                apt.env,
                apt.getType(CharSequence.class),
                new MockPropertyMirrorCollector());

        assertThat(a.isSame(b), is(false));
        assertThat(a.canInvoke(b), is(true));
        assertThat(a.canContain(b), is(false));

        assertThat(b.isSame(a), is(false));
        assertThat(b.canInvoke(a), is(false));
        assertThat(b.canContain(a), is(false));
    }

    /**
     * Test for different.
     */
    @Test
    public void different() {
        DataModelMirror a = new ConcreteDataModelMirror(
                apt.env,
                apt.getType(String.class),
                new MockPropertyMirrorCollector());
        DataModelMirror b = new ConcreteDataModelMirror(
                apt.env,
                apt.getType(StringBuilder.class),
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
    public void partial_subtype() {
        DataModelMirror a = new ConcreteDataModelMirror(
                apt.env,
                apt.getType(String.class),
                new MockPropertyMirrorCollector());
        DataModelMirror b = new PartialDataModelMirror(
                apt.env,
                apt.getTypeVariable("TCharSequence", "T"),
                new MockPropertyMirrorCollector());

        assertThat(a.isSame(b), is(false));
        assertThat(a.canInvoke(b), is(true));
        assertThat(a.canContain(b), is(true));
    }

    /**
     * Test for partial different.
     */
    @Test
    public void partial_different() {
        DataModelMirror a = new ConcreteDataModelMirror(
                apt.env,
                apt.getType(String.class),
                new MockPropertyMirrorCollector());
        DataModelMirror b = new PartialDataModelMirror(
                apt.env,
                apt.getTypeVariable("TStringBuilder", "T"),
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
        DataModelMirror type = new ConcreteDataModelMirror(
                apt.env,
                apt.getType(String.class),
                new MockPropertyMirrorCollector());
        PropertyMirror property = type.findProperty("string");
        assertThat(property.getName(), is(equalToIgnoringCase(String.class.getSimpleName())));
        assertThat(property.getType(), is(apt.sameType(String.class)));
    }
}
