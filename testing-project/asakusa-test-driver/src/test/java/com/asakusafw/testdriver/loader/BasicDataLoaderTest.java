/**
 * Copyright 2011-2018 Asakusa Framework Team.
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

import static org.hamcrest.Matchers.*;
import static org.junit.Assert.*;

import java.math.BigDecimal;
import java.util.Comparator;
import java.util.List;

import org.junit.Rule;
import org.junit.Test;

import com.asakusafw.runtime.core.GroupView;
import com.asakusafw.runtime.core.View;
import com.asakusafw.runtime.value.DecimalOption;
import com.asakusafw.runtime.value.IntOption;
import com.asakusafw.runtime.value.StringOption;
import com.asakusafw.testdriver.FileSystemCleaner;
import com.asakusafw.testdriver.core.DataModelDefinition;
import com.asakusafw.testdriver.core.DataModelReflection;
import com.asakusafw.testdriver.core.DataModelSource;
import com.asakusafw.testdriver.core.DataModelSourceFactory;
import com.asakusafw.testdriver.core.TestContext;
import com.asakusafw.testdriver.model.DefaultDataModelDefinition;

/**
 * Test for {@link BasicDataLoader}.
 */
public class BasicDataLoaderTest {

    static final DataModelDefinition<MockDataModel> DEF = new DefaultDataModelDefinition<>(MockDataModel.class);

    /**
     * Resets all Hadoop file systems.
     */
    @Rule
    public final FileSystemCleaner fsCleaner = new FileSystemCleaner();

    /**
     * simple case.
     */
    @Test
    public void simple() {
        List<MockDataModel> list = loader(new MockDataModel("Hello, world!"))
            .asList();
        assertThat(list, contains(new MockDataModel("Hello, world!")));
    }

    /**
     * as list.
     */
    @Test
    public void list() {
        List<MockDataModel> results = loader(
                new MockDataModel("A"),
                new MockDataModel("B"),
                new MockDataModel("C"))
            .asList();
        assertThat(results, containsInAnyOrder(
                new MockDataModel("A"),
                new MockDataModel("B"),
                new MockDataModel("C")));
    }

    /**
     * as view.
     */
    @Test
    public void view() {
        View<MockDataModel> results = loader(
                new MockDataModel("A"),
                new MockDataModel("B"),
                new MockDataModel("C"))
            .asView();
        assertThat(results, containsInAnyOrder(
                new MockDataModel("A"),
                new MockDataModel("B"),
                new MockDataModel("C")));
    }

    /**
     * order by property.
     */
    @Test
    public void order_property() {
        List<MockDataModel> list = loader(
                new MockDataModel(5, "A"),
                new MockDataModel(2, "B"),
                new MockDataModel(1, "C"),
                new MockDataModel(3, "D"),
                new MockDataModel(4, "E"))
                .order("key")
                .asList();
        assertThat(list, contains(
                new MockDataModel(1, "C"),
                new MockDataModel(2, "B"),
                new MockDataModel(3, "D"),
                new MockDataModel(4, "E"),
                new MockDataModel(5, "A")));
    }

    /**
     * order by property.
     */
    @Test
    public void order_property_invert() {
        List<MockDataModel> list = loader(
                new MockDataModel(5, "A"),
                new MockDataModel(2, "B"),
                new MockDataModel(1, "C"),
                new MockDataModel(3, "D"),
                new MockDataModel(4, "E"))
                .order("key DESC")
                .asList();
        assertThat(list, contains(
                new MockDataModel(5, "A"),
                new MockDataModel(4, "E"),
                new MockDataModel(3, "D"),
                new MockDataModel(2, "B"),
                new MockDataModel(1, "C")));
    }

    /**
     * order by comparator.
     */
    @Test
    public void order_comparator() {
        List<MockDataModel> list = loader(
                new MockDataModel(5, "A"),
                new MockDataModel(2, "B"),
                new MockDataModel(1, "C"),
                new MockDataModel(3, "D"),
                new MockDataModel(4, "E"))
                .order(Comparator.comparing(MockDataModel::getKeyOption))
                .asList();
        assertThat(list, contains(
                new MockDataModel(1, "C"),
                new MockDataModel(2, "B"),
                new MockDataModel(3, "D"),
                new MockDataModel(4, "E"),
                new MockDataModel(5, "A")));
    }

    /**
     * group - simple case.
     */
    @Test
    public void group() {
        GroupView<MockDataModel> results = loader(
                new MockDataModel(0, "A"),
                new MockDataModel(1, "B"),
                new MockDataModel(1, "C"),
                new MockDataModel(2, "D"))
            .group("key")
            .asView();
        assertThat(results.find(k(0)), containsInAnyOrder(
                new MockDataModel(0, "A")));
        assertThat(results.find(k(1)), containsInAnyOrder(
                new MockDataModel(1, "B"),
                new MockDataModel(1, "C")));
        assertThat(results.find(k(2)), containsInAnyOrder(
                new MockDataModel(2, "D")));
        assertThat(results.find(k(3)), hasSize(0));
        assertThat(results, containsInAnyOrder(
                new MockDataModel(0, "A"),
                new MockDataModel(1, "B"),
                new MockDataModel(1, "C"),
                new MockDataModel(2, "D")));
    }

    /**
     * group - order by property.
     */
    @Test
    public void group_order_property() {
        GroupView<MockDataModel> results = loader(
                new MockDataModel(0, d(0), "A"),
                new MockDataModel(1, d(3), "B"),
                new MockDataModel(1, d(1), "C"),
                new MockDataModel(1, d(2), "D"))
            .group("key")
            .order("sort")
            .asView();
        assertThat(results.find(k(0)), contains(
                new MockDataModel(0, d(0), "A")));
        assertThat(results.find(k(1)), contains(
                new MockDataModel(1, d(1), "C"),
                new MockDataModel(1, d(2), "D"),
                new MockDataModel(1, d(3), "B")));
    }

    /**
     * group - order by property.
     */
    @Test
    public void group_order_property_invert() {
        GroupView<MockDataModel> results = loader(
                new MockDataModel(0, d(0), "A"),
                new MockDataModel(1, d(3), "B"),
                new MockDataModel(1, d(1), "C"),
                new MockDataModel(1, d(2), "D"))
                .group("key")
                .order("-sort")
                .asView();
        assertThat(results.find(k(0)), contains(
                new MockDataModel(0, d(0), "A")));
        assertThat(results.find(k(1)), contains(
                new MockDataModel(1, d(3), "B"),
                new MockDataModel(1, d(2), "D"),
                new MockDataModel(1, d(1), "C")));
    }

    /**
     * group - order by property.
     */
    @Test
    public void group_order_comparator() {
        GroupView<MockDataModel> results = loader(
                new MockDataModel(0, d(0), "A"),
                new MockDataModel(1, d(3), "B"),
                new MockDataModel(1, d(1), "C"),
                new MockDataModel(1, d(2), "D"))
                .group("key")
                .order(Comparator.comparing(MockDataModel::getSortOption))
                .asView();
        assertThat(results.find(k(0)), contains(
                new MockDataModel(0, d(0), "A")));
        assertThat(results.find(k(1)), contains(
                new MockDataModel(1, d(1), "C"),
                new MockDataModel(1, d(2), "D"),
                new MockDataModel(1, d(3), "B")));
    }

    /**
     * group - ordered.
     */
    @Test
    public void group_ordered() {
        GroupView<MockDataModel> results = loader(
                new MockDataModel(0, d(0), "A"),
                new MockDataModel(1, d(3), "B"),
                new MockDataModel(1, d(1), "C"),
                new MockDataModel(1, d(2), "D"))
            .order("sort")
            .group("key")
            .asView();
        assertThat(results.find(k(0)), contains(
                new MockDataModel(0, d(0), "A")));
        assertThat(results.find(k(1)), contains(
                new MockDataModel(1, d(1), "C"),
                new MockDataModel(1, d(2), "D"),
                new MockDataModel(1, d(3), "B")));
    }

    /**
     * group - validate key element count.
     */
    @Test
    public void group_validate_count() {
        GroupView<MockDataModel> results = loader()
                .group("key", "sort")
                .asView();

        assertThat(results.find(new IntOption(), new DecimalOption()), hasSize(0));

        fail(() -> results.find());
        fail(() -> results.find(new IntOption()));
        fail(() -> results.find(new IntOption(), new DecimalOption(), new StringOption()));
    }

    /**
     * group - validate key element type.
     */
    @Test
    public void group_validate_type() {
        GroupView<MockDataModel> results = loader()
                .group("key", "sort")
                .asView();

        assertThat(results.find(new IntOption(0), new DecimalOption(d(0))), hasSize(0));
        fail(() -> results.find(new StringOption(""), new DecimalOption(d(0))));
        fail(() -> results.find(new IntOption(0), new StringOption("")));
        fail(() -> results.find(new IntOption(), new StringOption()));
        fail(() -> results.find(0, BigDecimal.valueOf(0)));
    }

    private static void fail(Runnable r) {
        try {
            r.run();
            throw new AssertionError();
        } catch (RuntimeException e) {
            // ok
        }
    }

    private static BigDecimal d(long value) {
        return BigDecimal.valueOf(value);
    }

    private static IntOption k(int value) {
        return new IntOption(value);
    }

    private static BasicDataLoader<MockDataModel> loader(MockDataModel... objects) {
        return new BasicDataLoader<>(new TestContext.Empty(), DEF, factory(objects));
    }

    private static DataModelSourceFactory factory(MockDataModel... objects) {
        return new DataModelSourceFactory() {
            @Override
            public <T> DataModelSource createSource(DataModelDefinition<T> definition, TestContext context) {
                return new DataModelSource() {
                    int index = 0;
                    @Override
                    public DataModelReflection next() {
                        if (index >= objects.length) {
                            return null;
                        }
                        return DEF.toReflection(objects[index++]);
                    }
                    @Override
                    public void close() {
                        return;
                    }
                };
            }
        };
    }
}
