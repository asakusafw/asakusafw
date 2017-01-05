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
package com.asakusafw.compiler.flow.mapreduce.parallel;

import static org.hamcrest.Matchers.*;
import static org.junit.Assert.*;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import org.junit.Test;

import com.asakusafw.compiler.flow.DataClass.Property;
import com.asakusafw.compiler.flow.ExternalIoDescriptionProcessor.SourceInfo;
import com.asakusafw.compiler.flow.JobflowCompilerTestRoot;
import com.asakusafw.compiler.flow.Location;
import com.asakusafw.compiler.flow.testing.model.Ex1;
import com.asakusafw.compiler.flow.testing.model.Ex2;
import com.asakusafw.runtime.stage.input.TemporaryInputFormat;
import com.asakusafw.runtime.stage.output.TemporaryOutputFormat;

/**
 * Test for {@link SlotResolver}.
 */
public class SlotResolverTest extends JobflowCompilerTestRoot {

    /**
     * single slot.
     */
    @Test
    public void single() {
        SlotResolver resolver = new SlotResolver(environment);
        Slot slot = new Slot(
                "out",
                Ex1.class,
                Arrays.asList("sid"),
                Arrays.asList(input("a")),
                TemporaryOutputFormat.class);
        List<ResolvedSlot> resolved = resolver.resolve(Arrays.asList(slot));
        assertThat(environment.hasError(), is(false));

        assertThat(resolved.size(), is(1));

        ResolvedSlot slot0 = resolved.get(0);
        assertThat(slot0.getSource(), is(slot));
        assertThat(slot0.getValueClass().getType(), equalTo((Object) Ex1.class));
        assertThat(slot0.getSortProperties().size(), is(1));
        Property prop = slot0.getSortProperties().get(0);
        assertThat(prop.getName(), is("sid"));
    }

    /**
     * multiple slots.
     */
    @Test
    public void multiple() {
        SlotResolver resolver = new SlotResolver(environment);
        List<Slot> slots = Arrays.asList(new Slot[] {
                new Slot(
                        "out1",
                        Ex1.class,
                        Arrays.asList("sid"),
                        Arrays.asList(input("a/1")),
                        TemporaryOutputFormat.class),
                new Slot(
                        "out2",
                        Ex2.class,
                        Arrays.asList("sid"),
                        Arrays.asList(input("a/2")),
                        TemporaryOutputFormat.class),
                new Slot(
                        "out3",
                        Ex1.class,
                        Arrays.asList("sid"),
                        Arrays.asList(input("a/3")),
                        TemporaryOutputFormat.class),
        });
        List<ResolvedSlot> resolved = resolver.resolve(slots);
        assertThat(environment.hasError(), is(false));

        assertThat(resolved.size(), is(3));
        assertThat(resolved.get(0).getSlotNumber(), is(0));
        assertThat(resolved.get(1).getSlotNumber(), is(1));
        assertThat(resolved.get(2).getSlotNumber(), is(2));
        assertThat(resolved.get(0).getValueClass().getType(), equalTo((Object) Ex1.class));
        assertThat(resolved.get(1).getValueClass().getType(), equalTo((Object) Ex2.class));
        assertThat(resolved.get(2).getValueClass().getType(), equalTo((Object) Ex1.class));
    }

    /**
     * invalid class.
     */
    @Test
    public void invalid_class() {
        SlotResolver resolver = new SlotResolver(environment);
        Slot slot = new Slot(
                "out",
                Void.class,
                Arrays.asList("sid"),
                Arrays.asList(input("a")),
                TemporaryOutputFormat.class);
        resolver.resolve(Arrays.asList(slot));
        assertThat(environment.hasError(), is(true));
    }

    /**
     * invalid property.
     */
    @Test
    public void invalid_property() {
        SlotResolver resolver = new SlotResolver(environment);
        Slot slot = new Slot(
                "out",
                Ex1.class,
                Arrays.asList("missing"),
                Arrays.asList(input("a")),
                TemporaryOutputFormat.class);
        resolver.resolve(Arrays.asList(slot));
        assertThat(environment.hasError(), is(true));
    }

    private SourceInfo input(String path) {
        return new SourceInfo(Collections.singleton(Location.fromPath(path, '/')), TemporaryInputFormat.class);
    }
}
