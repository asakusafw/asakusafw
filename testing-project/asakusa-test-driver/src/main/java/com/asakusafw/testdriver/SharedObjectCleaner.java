/**
 * Copyright 2011-2015 Asakusa Framework Team.
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
package com.asakusafw.testdriver;

import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.junit.rules.ExternalResource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.asakusafw.runtime.core.util.Shared;

/**
 * Test helper for operator classes with {@link Shared} class fields.
 * Using this,
 * <p>
 * Example:
 * </p>
<pre><code>
// operator class with shared class field
public abstract class SomeOperator {
    // shared value container
    static final Shared&lt;Hoge&gt; SHARED = new Shared&lt;Hoge&gt;() {
        ...
    }
    ...
}

// test class
public class SomeOperatorTest {
    // auto-remove shared value containers in SomeOperator class
    &#64;Rule
    public final SharedObjectCleaner cleaner = new SharedObjectCleaner()
        .add(SomeOperator.class);

    &#64;Test
    public void test() {
        ...
    }
}
</code></pre>
 * @since 0.7.3
 */
public class SharedObjectCleaner extends ExternalResource {

    static final Logger LOG = LoggerFactory.getLogger(SharedObjectCleaner.class);

    private final List<Shared<?>> sharedValues = new ArrayList<Shared<?>>();

    /**
     * Registers {@link Shared} objects.
     * @param objects shared objects
     * @return this
     */
    public SharedObjectCleaner add(Shared<?>... objects) {
        synchronized (this) {
            Collections.addAll(sharedValues, objects);
        }
        return this;
    }

    /**
     * Registers shared objects declared in class fields.
     * @param classes target classes which has shared class fields
     * @return this
     */
    public final SharedObjectCleaner add(Class<?>... classes) {
        for (Class<?> aClass : classes) {
            List<Shared<?>> containers = collect(aClass);
            add(containers.toArray(new Shared[containers.size()]));
        }
        return this;
    }

    private List<Shared<?>> collect(Class<?> aClass) {
        LOG.debug("collecting Shared fields: {}", aClass.getName()); //$NON-NLS-1$
        List<Shared<?>> results = new ArrayList<Shared<?>>();
        for (Field f : aClass.getDeclaredFields()) {
            if (Shared.class.isAssignableFrom(f.getType()) == false) {
                continue;
            }
            if (Modifier.isStatic(f.getModifiers()) == false) {
                LOG.warn(MessageFormat.format(
                        "shared field should be static: {0}#{1}",
                        aClass.getName(),
                        f.getName()));
                continue;
            }
            try {
                f.setAccessible(true);
                Shared<?> s = (Shared<?>) f.get(null);
                if (s == null) {
                    LOG.warn(MessageFormat.format(
                            "shared field is not initialized: {0}#{1}",
                            aClass.getName(),
                            f.getName()));
                } else {
                    results.add(s);
                }
            } catch (Exception e) {
                LOG.warn(MessageFormat.format(
                        "failed to collect shared field: {0}#{1}",
                        aClass.getName(),
                        f.getName()), e);
            }
        }
        return results;
    }

    @Override
    protected final void before() {
        removeAll();
    }

    @Override
    protected final void after() {
        removeAll();
    }

    private synchronized void removeAll() {
        for (Shared<?> s : sharedValues) {
            s.remove();
        }
    }
}
