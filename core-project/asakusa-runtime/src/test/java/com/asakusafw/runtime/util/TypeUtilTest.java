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
package com.asakusafw.runtime.util;

import static org.hamcrest.Matchers.*;
import static org.junit.Assert.*;

import java.lang.reflect.Type;
import java.util.AbstractCollection;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Set;

import org.junit.Test;

/**
 * Test for {@link TypeUtil}.
 */
public class TypeUtilTest {

    /**
     * Objectの起動
     */
    @Test
    public void object() {
        List<Type> invoked = TypeUtil.invoke(Object.class, String.class);
        assertThat(invoked.size(), is(0));
    }

    /**
     * クラスの起動。
     */
    @Test
    public void invokeClass() {
        List<Type> invoked = TypeUtil.invoke(ArrayList.class, StringList.class);
        assertThat(invoked.size(), is(1));
        assertThat(invoked.get(0), is((Type) String.class));
    }

    /**
     * 深いクラスの起動。
     */
    @Test
    public void invokeDeepClass() {
        List<Type> invoked = TypeUtil.invoke(AbstractCollection.class, StringList.class);
        assertThat(invoked.size(), is(1));
        assertThat(invoked.get(0), is((Type) String.class));
    }

    /**
     * インターフェースの起動。
     */
    @Test
    public void invokeInterface() {
        List<Type> invoked = TypeUtil.invoke(List.class, StringList.class);
        assertThat(invoked.size(), is(1));
        assertThat(invoked.get(0), is((Type) String.class));
    }

    /**
     * 深いインターフェースの起動。
     */
    @Test
    public void invokeDeepInterface() {
        List<Type> invoked = TypeUtil.invoke(Collection.class, StringList.class);
        assertThat(invoked.size(), is(1));
        assertThat(invoked.get(0), is((Type) String.class));
    }

    /**
     * インターフェースからインターフェースの起動。
     */
    @Test
    public void invokeInterfaceFromInterface() {
        List<Type> invoked = TypeUtil.invoke(List.class, IStringList.class);
        assertThat(invoked.size(), is(1));
        assertThat(invoked.get(0), is((Type) String.class));
    }

    /**
     * インターフェースから深いインターフェースの起動。
     */
    @Test
    public void invokeDeepInterfaceFromInterface() {
        List<Type> invoked = TypeUtil.invoke(Collection.class, IStringList.class);
        assertThat(invoked.size(), is(1));
        assertThat(invoked.get(0), is((Type) String.class));
    }

    /**
     * 関係のない型の起動。
     */
    @Test
    public void invokeOrthogonal() {
        List<Type> invoked = TypeUtil.invoke(Set.class, IStringList.class);
        assertThat(invoked, is(nullValue()));
    }

    private static class StringList extends ArrayList<String> {

        private static final long serialVersionUID = 1L;
    }

    private interface IStringList extends List<String> {

        // no members
    }
}
