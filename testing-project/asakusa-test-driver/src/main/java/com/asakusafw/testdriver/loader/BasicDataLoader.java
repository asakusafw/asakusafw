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
package com.asakusafw.testdriver.loader;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.Iterator;
import java.util.List;
import java.util.stream.Collectors;

import com.asakusafw.runtime.core.View;
import com.asakusafw.testdriver.core.DataModelDefinition;
import com.asakusafw.testdriver.core.DataModelReflection;
import com.asakusafw.testdriver.core.DataModelSource;
import com.asakusafw.testdriver.core.DataModelSourceFactory;
import com.asakusafw.testdriver.core.PropertyName;
import com.asakusafw.testdriver.core.PropertyType;
import com.asakusafw.testdriver.core.TestContext;

/**
 * A basic implementation of {@link DataLoader}.
 * @param <T> the data type
 * @since 0.9.1
 */
public class BasicDataLoader<T> implements DataLoader<T> {

    private final TestContext context;

    private final DataModelDefinition<T> definition;

    private final DataModelSourceFactory factory;

    private Comparator<DataModelReflection> refComparator;

    /**
     * Creates a new instance.
     * @param context the current test context
     * @param definition the target data definition
     * @param factory the input
     */
    public BasicDataLoader(TestContext context, DataModelDefinition<T> definition, DataModelSourceFactory factory) {
        this.context = context;
        this.definition = definition;
        this.factory = factory;
    }

    @Override
    public GroupLoader<T> group(String... terms) {
        List<PropertyName> names = new ArrayList<>();
        for (String term : terms) {
            PropertyName name = PropertyName.parse(term);
            PropertyType type = definition.getType(name);
            if (type == null) {
                throw new IllegalArgumentException();
            }
            names.add(name);
        }
        return new BasicGroupLoader<>(context, definition, factory, names, refComparator);
    }

    @Override
    public DataLoader<T> order(String... terms) {
        if (refComparator != null) {
            throw new IllegalStateException();
        }
        refComparator = Util.toComparator(definition, terms);
        return this;
    }

    @Override
    public DataLoader<T> order(Comparator<? super T> comparator) {
        if (refComparator != null) {
            throw new IllegalStateException();
        }
        refComparator = Util.toComparator(definition, comparator);
        return this;
    }

    @Override
    public List<T> asList() {
        List<DataModelReflection> refs = new ArrayList<>();
        try (DataModelSource source = factory.createSource(definition, context)) {
            while (true) {
                DataModelReflection ref = source.next();
                if (ref == null) {
                    break;
                }
                refs.add(ref);
            }
        } catch (IOException e) {
            throw new IllegalStateException(e);
        }
        if (refComparator != null) {
            refs.sort(refComparator);
        }
        return refs.stream()
                .sequential()
                .map(definition::toObject)
                .collect(Collectors.toList());
    }

    @Override
    public View<T> asView() {
        return new ListView<>(asList());
    }

    private static final class ListView<T> implements View<T> {

        private final List<T> entity;

        ListView(List<T> entity) {
            this.entity = entity;
        }

        @Override
        public Iterator<T> iterator() {
            return entity.iterator();
        }

        @Override
        public String toString() {
            return entity.toString();
        }
    }
}
