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
package com.asakusafw.testdriver.core;

import static org.hamcrest.Matchers.*;
import static org.junit.Assert.*;

import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.List;

import org.junit.Test;

import com.asakusafw.testdriver.core.MockExporterRetriever.Desc;

/**
 * Test for {@link TestResultInspector}.
 * @since 0.2.0
 */
@Deprecated
public class TestResultInspectorTest extends SpiTestRoot {

    private final VerifyContext context;
    {
        context = new VerifyContext(new TestContext.Empty());
        context.testFinished();
    }

    /**
     * simple testing.
     * @throws Exception if failed
     */
    @Test
    public void simple() throws Exception {
        TestResultInspector inspector = new TestResultInspector(
                new MockDataModelAdapter(String.class),
                new MockSourceProvider()
                    .add(uri("testing:src"), "1:Hello, world!"),
                new MockVerifyRuleProvider()
                    .add(uri("testing:rule"), rule()),
                new MockExporterRetriever().wrap());

        Desc desc = MockExporterRetriever.create("1:Hello, world!");
        List<Difference> results = inspector.inspect(
                desc.getModelType(),
                desc,
                context,
                uri("testing:src"),
                uri("testing:rule"));
        assertThat(results.size(), is(0));
    }

    /**
     * using SPI.
     * @throws Exception if failed
     */
    @Test
    public void spi() throws Exception {
        register(DataModelAdapter.class, MockDataModelAdapter.class);
        register(DataModelSourceProvider.class, MockSourceProvider.class);
        register(VerifyRuleProvider.class, MockVerifyRuleProvider.class);
        ClassLoader loader = register(ExporterRetriever.class, MockExporterRetriever.class);
        TestResultInspector inspector = new TestResultInspector(loader);

        Desc desc = MockExporterRetriever.create("MOCK");
        List<Difference> results = inspector.inspect(
                desc.getModelType(),
                desc,
                context,
                uri("default:source"),
                uri("default:rule"));
        assertThat(results.toString(), results.size(), is(0));
    }

    /**
     * result matched to expected key, but value is mismatched.
     * @throws Exception if failed
     */
    @Test
    public void inconsistent_result() throws Exception {
        TestResultInspector inspector = new TestResultInspector(
                new MockDataModelAdapter(String.class),
                new MockSourceProvider()
                    .add(uri("testing:src"), "1:Hello, world!"),
                new MockVerifyRuleProvider()
                    .add(uri("testing:rule"), rule()),
                new MockExporterRetriever().wrap());

        Desc desc = MockExporterRetriever.create("1:BAD");
        List<Difference> results = inspector.inspect(
                desc.getModelType(),
                desc,
                context,
                uri("testing:src"),
                uri("testing:rule"));
        assertThat(results.size(), is(1));
    }

    /**
     * result is empty.
     * @throws Exception if failed
     */
    @Test
    public void empty_result() throws Exception {
        TestResultInspector inspector = new TestResultInspector(
                new MockDataModelAdapter(String.class),
                new MockSourceProvider()
                    .add(uri("testing:src"), "1:Hello, world!"),
                new MockVerifyRuleProvider()
                    .add(uri("testing:rule"), rule()),
                new MockExporterRetriever().wrap());

        Desc desc = MockExporterRetriever.create();
        List<Difference> results = inspector.inspect(
                desc.getModelType(),
                desc,
                context,
                uri("testing:src"),
                uri("testing:rule"));
        assertThat(results.size(), is(1));
    }

    /**
     * unnecessory results.
     * @throws Exception if failed
     */
    @Test
    public void extra_result() throws Exception {
        TestResultInspector inspector = new TestResultInspector(
                new MockDataModelAdapter(String.class),
                new MockSourceProvider()
                    .add(uri("testing:src"), "1:Hello, world!"),
                new MockVerifyRuleProvider()
                    .add(uri("testing:rule"), rule()),
                new MockExporterRetriever().wrap());

        Desc desc = MockExporterRetriever.create("1:Hello, world!", "2:BAD");
        List<Difference> results = inspector.inspect(
                desc.getModelType(),
                desc,
                context,
                uri("testing:src"),
                uri("testing:rule"));
        assertThat(results.size(), is(1));
    }

    /**
     * key is not matched.
     * @throws Exception if failed
     */
    @Test
    public void inconsistent_key() throws Exception {
        TestResultInspector inspector = new TestResultInspector(
                new MockDataModelAdapter(String.class),
                new MockSourceProvider()
                    .add(uri("testing:src"), "1:Hello, world!"),
                new MockVerifyRuleProvider()
                    .add(uri("testing:rule"), rule()),
                new MockExporterRetriever().wrap());

        Desc desc = MockExporterRetriever.create("2:Hello, world!");
        List<Difference> results = inspector.inspect(
                desc.getModelType(),
                desc,
                context,
                uri("testing:src"),
                uri("testing:rule"));
        assertThat(results.toString(), results.size(), is(2));
    }

    /**
     * invalid data model type.
     * @throws Exception if failed
     */
    @Test(expected = IOException.class)
    public void unknown_type() throws Exception {
        TestResultInspector inspector = new TestResultInspector(
                new MockDataModelAdapter(Integer.class),
                new MockSourceProvider()
                    .add(uri("testing:src"), "1:Hello, world!"),
                new MockVerifyRuleProvider()
                    .add(uri("testing:rule"), rule()),
                new MockExporterRetriever().wrap());

        Desc desc = MockExporterRetriever.create("1:Hello, world!");
        inspector.inspect(
                desc.getModelType(),
                desc,
                context,
                uri("testing:src"),
                uri("testing:rule"));
    }

    /**
     * invalid source.
     * @throws Exception if failed
     */
    @Test(expected = IOException.class)
    public void unknown_source() throws Exception {
        TestResultInspector inspector = new TestResultInspector(
                new MockDataModelAdapter(String.class),
                new MockSourceProvider()
                    .add(uri("testing:src"), "1:Hello, world!"),
                new MockVerifyRuleProvider()
                    .add(uri("testing:rule"), rule()),
                new MockExporterRetriever().wrap());

        Desc desc = MockExporterRetriever.create("1:Hello, world!");
        inspector.inspect(
                desc.getModelType(),
                desc,
                context,
                uri("unknown:src"),
                uri("testing:rule"));
    }

    /**
     * invalid rule.
     * @throws Exception if failed
     */
    @Test(expected = IOException.class)
    public void unknown_rule() throws Exception {
        TestResultInspector inspector = new TestResultInspector(
                new MockDataModelAdapter(String.class),
                new MockSourceProvider()
                    .add(uri("testing:src"), "1:Hello, world!"),
                new MockVerifyRuleProvider()
                    .add(uri("testing:rule"), rule()),
                new MockExporterRetriever().wrap());

        Desc desc = MockExporterRetriever.create("1:Hello, world!");
        inspector.inspect(
                desc.getModelType(),
                desc,
                context,
                uri("testing:src"),
                uri("unknown:rule"));
    }

    private VerifyRule rule() {
        return new VerifyRule() {
            private final DataModelDefinition<String> def = ValueDefinition.of(String.class);

            @Override
            public Object getKey(DataModelReflection target) {
                String string = def.toObject(target);
                String[] split = string.split(":", 2);
                return split[0];
            }

            @Override
            public Object verify(DataModelReflection expected, DataModelReflection actual) {
                if (expected == null || actual == null) {
                    return "invalid";
                }
                String ex = def.toObject(expected).split(":", 2)[1];
                String ac = def.toObject(actual).split(":", 2)[1];
                return ex.equals(ac) ? null : "mismatch";
            }
        };
    }

    private URI uri(String str) {
        try {
            return new URI(str);
        } catch (URISyntaxException e) {
            throw new AssertionError(e);
        }
    }
}
