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
package com.asakusafw.operator.builtin;

import static org.junit.Assert.*;

import org.junit.Test;

import com.asakusafw.operator.model.OperatorElement;
import com.asakusafw.vocabulary.flow.graph.ObservationCount;

/**
 * Test for {@link DslBuilder}.
 */
public class DslBuilderTest extends OperatorDriverTestRoot {

    /**
     * Creates a new instance.
     */
    public DslBuilderTest() {
        super(new UpdateOperatorDriver());
    }

    /**
     * w/o observation counts.
     */
    @Test
    public void observation_nothing() {
        compile(new Action("com.example.Simple") {
            @Override
            protected void perform(OperatorElement target) {
                assertThat(target, hasAttribute(ObservationCount.DONT_CARE));
            }
        });
    }

    /**
     * w/ sticky.
     */
    @Test
    public void observation_sticky() {
        compile(new Action("com.example.WithSticky") {
            @Override
            protected void perform(OperatorElement target) {
                assertThat(target, hasAttribute(ObservationCount.AT_LEAST_ONCE));
            }
        });
    }

    /**
     * w/ sticky.
     */
    @Test
    public void observation_volatile() {
        compile(new Action("com.example.WithVolatile") {
            @Override
            protected void perform(OperatorElement target) {
                assertThat(target, hasAttribute(ObservationCount.AT_MOST_ONCE));
            }
        });
    }

    /**
     * w/ sticky and volatile.
     */
    @Test
    public void observation_both() {
        compile(new Action("com.example.WithStickyVolatile") {
            @Override
            protected void perform(OperatorElement target) {
                assertThat(target, hasAttribute(ObservationCount.EXACTLY_ONCE));
            }
        });
    }
}
