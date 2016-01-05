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
package com.asakusafw.dmdl.thundergate.driver;

import static org.hamcrest.Matchers.*;
import static org.junit.Assert.*;

import org.apache.hadoop.io.Text;
import org.junit.Before;
import org.junit.Test;

import com.asakusafw.dmdl.thundergate.GeneratorTesterRoot;
import com.asakusafw.thundergate.runtime.cache.ThunderGateCacheSupport;

/**
 * Test for {@link CacheSupportDriver} and {@link CacheSupportEmitter}.
 */
public class CacheSupportEmitterTest extends GeneratorTesterRoot {

    /**
     * Initializes the test.
     * @throws Exception if some errors were occurred
     */
    @Before
    public void setUp() throws Exception {
        emitDrivers.add(new CacheSupportEmitter());
    }

    /**
     * simple.
     */
    @Test
    public void cache() {
        ModelLoader loaded = generateJava("cache");
        ModelWrapper model = loaded.newModel("Model");

        assertThat(model.unwrap(), is(instanceOf(ThunderGateCacheSupport.class)));
        ThunderGateCacheSupport support = (ThunderGateCacheSupport) model.unwrap();

        model.set("sid", 100L);
        assertThat(support.__tgc__SystemId(), is(100L));
        assertThat(support.__tgc__TimestampColumn(), is("LAST_UPDT_DATETIME"));
        assertThat(support.__tgc__Deleted(), is(false));
    }

    /**
     * not a cache.
     */
    @Test
    public void cache_no() {
        ModelLoader loaded = generateJava("cache_no");
        ModelWrapper model = loaded.newModel("Model");
        assertThat(model.unwrap(), not(instanceOf(ThunderGateCacheSupport.class)));
    }

    /**
     * with delete flag (text).
     */
    @Test
    public void cache_delete() {
        ModelLoader loaded = generateJava("cache_delete");
        ModelWrapper model = loaded.newModel("Model");

        assertThat(model.unwrap(), is(instanceOf(ThunderGateCacheSupport.class)));
        ThunderGateCacheSupport support = (ThunderGateCacheSupport) model.unwrap();

        model.set("logical_delete_flag", new Text("Y"));
        assertThat(support.__tgc__Deleted(), is(true));
        model.set("logical_delete_flag", new Text("N"));
        assertThat(support.__tgc__Deleted(), is(false));
        model.set("logical_delete_flag", null);
        assertThat(support.__tgc__Deleted(), is(false));
    }

    /**
     * with delete flag (boolean).
     */
    @Test
    public void cache_delete_boolean() {
        ModelLoader loaded = generateJava("cache_delete_boolean");
        ModelWrapper model = loaded.newModel("Model");

        assertThat(model.unwrap(), is(instanceOf(ThunderGateCacheSupport.class)));
        ThunderGateCacheSupport support = (ThunderGateCacheSupport) model.unwrap();

        model.set("logical_delete_flag", true);
        assertThat(support.__tgc__Deleted(), is(true));
        model.set("logical_delete_flag", false);
        assertThat(support.__tgc__Deleted(), is(false));
        model.setOption("logical_delete_flag", null);
        assertThat(support.__tgc__Deleted(), is(false));
    }

    /**
     * with delete flag (integer).
     */
    @Test
    public void cache_delete_integer() {
        ModelLoader loaded = generateJava("cache_delete_integer");
        ModelWrapper model = loaded.newModel("Model");

        assertThat(model.unwrap(), is(instanceOf(ThunderGateCacheSupport.class)));
        ThunderGateCacheSupport support = (ThunderGateCacheSupport) model.unwrap();

        model.set("logical_delete_flag", (byte) 1);
        assertThat(support.__tgc__Deleted(), is(true));
        model.set("logical_delete_flag", (byte) 0);
        assertThat(support.__tgc__Deleted(), is(false));
        model.set("logical_delete_flag", (byte) 2);
        assertThat(support.__tgc__Deleted(), is(false));
        model.setOption("logical_delete_flag", null);
        assertThat(support.__tgc__Deleted(), is(false));
    }

    /**
     * attribute to properties.
     */
    @Test
    public void invalid_cache_location() {
        shouldSemanticError("invalid_cache_location");
    }

    /**
     * attribute to projective model.
     */
    @Test
    public void invalid_cache_projective() {
        shouldSemanticError("invalid_cache_projective");
    }

    /**
     * empty elements.
     */
    @Test
    public void invalid_cache_empty() {
        shouldSemanticError("invalid_cache_empty");
    }

    /**
     * extra elements.
     */
    @Test
    public void invalid_cache_extra() {
        shouldSemanticError("invalid_cache_extra");
    }

    /**
     * invalid types for sid and timestamp.
     */
    @Test
    public void invalid_cache_type() {
        shouldSemanticError("invalid_cache_type");
    }

    /**
     * invalid types for delete flag (text).
     */
    @Test
    public void invalid_cache_type_text() {
        shouldSemanticError("invalid_cache_type_text");
    }

    /**
     * invalid types for delete flag (boolean).
     */
    @Test
    public void invalid_cache_type_boolean() {
        shouldSemanticError("invalid_cache_type_boolean");
    }

    /**
     * invalid types for delete flag (integer).
     */
    @Test
    public void invalid_cache_type_integer() {
        shouldSemanticError("invalid_cache_type_integer");
    }
}
