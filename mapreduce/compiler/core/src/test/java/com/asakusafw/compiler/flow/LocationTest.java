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
package com.asakusafw.compiler.flow;

import static org.hamcrest.Matchers.*;
import static org.junit.Assert.*;

import org.junit.Test;

/**
 * Test for {@link Location}.
 */
public class LocationTest {

    /**
     * the root location.
     */
    @Test
    public void root() {
        Location location = new Location(null, "hello");
        assertThat(location.getParent(), is(nullValue()));
        assertThat(location.getName(), is("hello"));
    }

    /**
     * child locations.
     */
    @Test
    public void child() {
        Location parent = new Location(null, "parent");
        Location location = new Location(parent, "hello");
        assertThat(location.getParent(), is(parent));
        assertThat(location.getName(), is("hello"));
    }

    /**
     * test for prefix locations.
     */
    @Test
    public void prefix() {
        Location location = new Location(null, "hello");
        assertThat(location.isPrefix(), is(false));

        Location prefix = location.asPrefix();
        assertThat(location.isPrefix(), is(false));
        assertThat(prefix.isPrefix(), is(true));
    }

    /**
     * test for append w/ string.
     */
    @Test
    public void appendString() {
        Location root = new Location(null, "root");
        Location append = root.append("last");
        assertThat(append.getParent(), is(root));
        assertThat(append.getName(), is("last"));
    }

    /**
     * test for append w/ relative location.
     */
    @Test
    public void appendLocation() {
        Location branch = new Location(null, "branch");
        Location last = new Location(branch, "last");
        Location root = new Location(null, "root");
        Location append = root.append(last);
        Location rootBranch = new Location(root, "branch");

        assertThat(append.getParent(), is(rootBranch));
        assertThat(append.getName(), is("last"));
    }

    /**
     * test for from/to path for trivial location.
     */
    @Test
    public void convert_trivial() {
        Location root = Location.fromPath("root", '/');
        assertThat(root.toPath('/'), is("root"));
    }

    /**
     * test for from/to path.
     */
    @Test
    public void convert_path() {
        Location root = Location.fromPath("root>branch>last", '>');
        assertThat(root.toPath('/'), is("root/branch/last"));
    }

    /**
     * test for from/to path for strange path.
     */
    @Test
    public void convert_normalize() {
        Location root = Location.fromPath("/root//last/", '/');
        assertThat(root.toPath('/'), is("root/last"));
    }

    /**
     * test for from/to path for prefix locations.
     */
    @Test
    public void convert_prefix() {
        Location root = Location.fromPath("root/prefix-*", '/');
        assertThat(root.toPath('/'), is("root/prefix-*"));
    }

    /**
     * same location.
     */
    @Test
    public void isPrefixOf_same() {
        assertThat(loc("a/b/c").isPrefixOf(loc("a/b/c")), is(true));
    }

    /**
     * truth prefix location.
     */
    @Test
    public void isPrefixOf_truth() {
        assertThat(loc("a/b").isPrefixOf(loc("a/b/c")), is(true));
    }

    /**
     * truth prefix location.
     */
    @Test
    public void isPrefixOf_reverse() {
        assertThat(loc("a/b/c").isPrefixOf(loc("a/b")), is(false));
    }

    /**
     * truth prefix location.
     */
    @Test
    public void isPrefixOf_otherRoot() {
        assertThat(loc("x/a/b").isPrefixOf(loc("a/b/c")), is(false));
    }

    private Location loc(String path) {
        return Location.fromPath(path, '/');
    }
}
