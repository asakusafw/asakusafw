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
package com.asakusafw.testdriver;

import static com.asakusafw.testdriver.FlowDriverPortTestHelper.*;
import static org.hamcrest.Matchers.*;
import static org.junit.Assert.*;

import java.io.File;
import java.io.IOException;
import java.net.URI;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Scanner;
import java.util.Set;
import java.util.function.UnaryOperator;

import org.apache.hadoop.io.Text;
import org.junit.ClassRule;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;

import com.asakusafw.runtime.windows.WindowsSupport;
import com.asakusafw.testdriver.core.DataModelDefinition;
import com.asakusafw.testdriver.core.DataModelReflection;
import com.asakusafw.testdriver.core.DataModelSink;
import com.asakusafw.testdriver.core.DataModelSinkFactory;
import com.asakusafw.testdriver.core.DataModelSource;
import com.asakusafw.testdriver.core.DataModelSourceFactory;
import com.asakusafw.testdriver.core.Difference;
import com.asakusafw.testdriver.core.DifferenceSink;
import com.asakusafw.testdriver.core.DifferenceSinkFactory;
import com.asakusafw.testdriver.core.ModelTester;
import com.asakusafw.testdriver.core.ModelTransformer;
import com.asakusafw.testdriver.core.ModelVerifier;
import com.asakusafw.testdriver.core.TestContext;
import com.asakusafw.testdriver.core.TestRule;
import com.asakusafw.testdriver.core.Verifier;
import com.asakusafw.testdriver.core.VerifierFactory;
import com.asakusafw.testdriver.core.VerifyContext;
import com.asakusafw.testdriver.core.VerifyRule;
import com.asakusafw.testdriver.core.VerifyRuleFactory;
import com.asakusafw.testdriver.model.DefaultDataModelDefinition;
import com.asakusafw.testdriver.testing.dsl.SimpleStreamFormat;
import com.asakusafw.testdriver.testing.dsl.TextStreamFormat;
import com.asakusafw.testdriver.testing.model.Simple;
import com.asakusafw.utils.io.Provider;

/**
 * Test for {@link FlowDriverOutput}.
 */
public class FlowDriverOutputTest {

    /**
     * Windows platform support.
     */
    @ClassRule
    public static final WindowsSupport WINDOWS_SUPPORT = new WindowsSupport();

    /**
     * A temporary folder.
     */
    @Rule
    public final TemporaryFolder folder = new TemporaryFolder();

    /**
     * Test method for {@link FlowDriverOutput#prepare(DataModelSourceFactory)}.
     */
    @Test
    public void prepare_factory() {
        MockFlowDriverOutput<Text> mock = MockFlowDriverOutput.text(getClass(), provider());
        mock.prepare(factory("Hello1", "Hello2"));
        verify(mock.getSource(), DEFINITION, list("Hello1", "Hello2"));
    }

    /**
     * Test method for {@link FlowDriverOutput#verify(VerifierFactory)}.
     */
    @Test
    public void verify_factory() {
        MockFlowDriverOutput<Text> mock = MockFlowDriverOutput.text(getClass(), provider());
        VerifierFactory factory = new VerifierFactory() {
            @Override
            public <T> Verifier createVerifier(DataModelDefinition<T> definition, VerifyContext context) {
                return null;
            }
        };
        mock.verify(factory);
        assertThat(mock.getVerifier(), is(sameInstance(factory)));
    }

    /**
     * simple test for {@link FlowDriverOutput#verify(DataModelSourceFactory, String, ModelTester)}.
     */
    @Test
    public void verify_factory_uri_tester() {
        MockFlowDriverOutput<Text> mock = MockFlowDriverOutput.text(getClass(), tool_rule("Hello3"));
        mock.verify(factory("Hello1", "Hello2", "Hello3"), "data/dummy", null);
        assertThat(test(mock.getVerifier(), "Hello1", "Hello2", "Hello3"), hasSize(1));
    }

    /**
     * Missing rule URI test for {@link FlowDriverOutput#verify(DataModelSourceFactory, String, ModelTester)}.
     */
    @Test(expected = IllegalArgumentException.class)
    public void verify_factory_uri_tester_missingRule() {
        MockFlowDriverOutput<Text> mock = MockFlowDriverOutput.text(getClass(), provider());
        mock.verify(factory("Hello1", "Hello2", "Hello3"), "data/__MISSING__", null);
    }

    /**
     * simple test for {@link FlowDriverOutput#verify(DataModelSourceFactory, String, ModelTester)}.
     */
    @Test
    public void verify_factory_uri_tester_withTester() {
        MockFlowDriverOutput<Text> mock = MockFlowDriverOutput.text(getClass(), tool_rule("Hello3"));
        mock.verify(factory("TESTER", "Hello2", "Hello3"), "data/dummy", modelTester("TESTER"));
        assertThat(test(mock.getVerifier(), "TESTER", "Hello2", "Hello3"), hasSize(2));
    }

    /**
     * Test method for {@link FlowDriverOutput#verify(DataModelSourceFactory, ModelVerifier)}.
     */
    @Test
    public void verify_factory_verifier() {
        MockFlowDriverOutput<Text> mock = MockFlowDriverOutput.text(getClass(), provider());
        mock.verify(factory("Hello1", "VERIFIER", "Hello3"), modelVerifier("VERIFIER"));
        assertThat(test(mock.getVerifier(), "Hello1", "VERIFIER", "Hello3"), hasSize(1));
    }

    /**
     * Test method for {@link FlowDriverOutput#dumpActual(DataModelSinkFactory)}.
     */
    @Test
    public void dumpActual_factory() {
        MockDataModelSink sink = new MockDataModelSink();
        MockFlowDriverOutput<Text> mock = MockFlowDriverOutput.text(getClass(), provider());
        mock.dumpActual(new DataModelSinkFactory() {
            @Override
            public <T> DataModelSink createSink(DataModelDefinition<T> definition, TestContext context) throws IOException {
                return sink;
            }
        });
        insert(mock.getResultSink(), "Hello1", "Hello2", "Hello3");
        assertThat(sink.getBuffer(), containsInAnyOrder("Hello1", "Hello2", "Hello3"));
    }

    /**
     * Test method for {@link FlowDriverOutput#dumpDifference(com.asakusafw.testdriver.core.DifferenceSinkFactory)}.
     */
    @Test
    public void dumpDifference_factory() {
        MockDiffSink sink = new MockDiffSink();
        MockFlowDriverOutput<Text> mock = MockFlowDriverOutput.text(getClass(), provider());
        mock.dumpDifference(new DifferenceSinkFactory() {
            @Override
            public <T> DifferenceSink createSink(DataModelDefinition<T> definition, TestContext context) throws IOException {
                return sink;
            }
        });
        checkInstance(mock.getDifferenceSink(), sink);
    }

    /**
     * simple test for {@link FlowDriverOutput#prepare(String)}.
     */
    @Test
    public void prepare_uri() {
        MockFlowDriverOutput<Text> mock = MockFlowDriverOutput.text(getClass(), tool_data);
        mock.prepare("data/dummy");
        verify(mock.getSource(), DEFINITION, list("Hello1", "Hello2", "Hello3"));
    }

    /**
     * missing resource in {@link FlowDriverOutput#prepare(String)}.
     */
    @Test(expected = IllegalArgumentException.class)
    public void prepare_uri_missing() {
        new MockFlowDriverOutput<>(getClass(), Text.class, provider()).prepare("data/__MISSING__");
    }

    /**
     * Test method for {@link FlowDriverOutput#prepare(Iterable)}.
     */
    @Test
    public void prepare_collection() {
        MockFlowDriverOutput<Text> mock = MockFlowDriverOutput.text(getClass(), provider());
        mock.prepare(list("Hello1", "Hello2"));
        verify(mock.getSource(), DEFINITION, list("Hello1", "Hello2"));
    }

    /**
     * Test method for {@link FlowDriverOutput#prepare(Provider)}.
     */
    @Test
    public void prepare_iterator() {
        MockFlowDriverOutput<Text> mock = MockFlowDriverOutput.text(getClass(), provider());
        mock.prepare(provider("Hello1", "Hello2"));
        verify(mock.getSource(), DEFINITION, list("Hello1", "Hello2"));
    }

    /**
     * simple test for {@link FlowDriverOutput#prepare(Class, String)}.
     */
    @Test
    public void prepare_directio_path() {
        MockFlowDriverOutput<?> mock = new MockFlowDriverOutput<>(getClass(), Simple.class, provider())
                .prepare(SimpleStreamFormat.class, "directio/simple.txt");
        verify(mock.getSource(), DEFINITION, list("Hello, world!"));
    }

    /**
     * simple test for {@link FlowDriverOutput#prepare(Class, java.io.File)}.
     */
    @Test
    public void prepare_directio_file() {
        MockFlowDriverOutput<?> mock = new MockFlowDriverOutput<>(getClass(), Simple.class, provider())
                .prepare(SimpleStreamFormat.class, asFile("directio/simple.txt"));
        verify(mock.getSource(), DEFINITION, list("Hello, world!"));
    }

    /**
     * Test method for {@link FlowDriverOutput#verify(String, String)}.
     */
    @Test
    public void verify_uri_uri() {
        MockFlowDriverOutput<Text> mock = MockFlowDriverOutput.text(getClass(), tool_data_rule("Hello3"));
        mock.verify("data/dummy", "data/dummy2");
        assertThat(test(mock.getVerifier(), "Hello1", "Hello2", "Hello3"), hasSize(1));
    }

    /**
     * Test method for {@link FlowDriverOutput#verify(String, String)}.
     */
    @Test(expected = IllegalArgumentException.class)
    public void verify_uri_uri_missingExpect() {
        MockFlowDriverOutput<Text> mock = MockFlowDriverOutput.text(getClass(), tool_rule("Hello3"));
        mock.verify("data/__MISSING__", "data/dummy");
    }

    /**
     * Test method for {@link FlowDriverOutput#verify(DataModelSourceFactory, String)}.
     */
    @Test
    public void verify_factory_uri() {
        MockFlowDriverOutput<Text> mock = MockFlowDriverOutput.text(getClass(), tool_rule("Hello3"));
        mock.verify(factory("Hello1", "Hello2", "Hello3"), "data/dummy");
        assertThat(test(mock.getVerifier(), "Hello1", "Hello2", "Hello3"), hasSize(1));
    }

    /**
     * Test method for {@link FlowDriverOutput#verify(Iterable, String)}.
     */
    @Test
    public void verify_iterable_uri() {
        MockFlowDriverOutput<Text> mock = MockFlowDriverOutput.text(getClass(), tool_rule("Hello3"));
        mock.verify(list("Hello1", "Hello2", "Hello3"), "data/dummy");
        assertThat(test(mock.getVerifier(), "Hello1", "Hello2", "Hello3"), hasSize(1));
    }

    /**
     * Test method for {@link FlowDriverOutput#verify(com.asakusafw.utils.io.Provider, String)}.
     */
    @Test
    public void verify_provider_uri() {
        MockFlowDriverOutput<Text> mock = MockFlowDriverOutput.text(getClass(), tool_rule("Hello3"));
        mock.verify(provider("Hello1", "Hello2", "Hello3"), "data/dummy");
        assertThat(test(mock.getVerifier(), "Hello1", "Hello2", "Hello3"), hasSize(1));
    }

    /**
     * Test method for {@link FlowDriverOutput#verify(Class, String, String)}.
     */
    @Test
    public void verify_directio_path_uri() {
        MockFlowDriverOutput<Text> mock = MockFlowDriverOutput.text(getClass(), tool_data_rule("Hello3"));
        mock.verify(TextStreamFormat.class, "directio/hello123.txt", "data/dummy2");
        assertThat(test(mock.getVerifier(), "Hello1", "Hello2", "Hello3"), hasSize(1));
    }

    /**
     * Test method for {@link FlowDriverOutput#verify(Class, File, String)}.
     */
    @Test
    public void verify_directio_file_uri() {
        MockFlowDriverOutput<Text> mock = MockFlowDriverOutput.text(getClass(), tool_data_rule("Hello3"));
        mock.verify(TextStreamFormat.class, asFile("directio/hello123.txt"), "data/dummy2");
        assertThat(test(mock.getVerifier(), "Hello1", "Hello2", "Hello3"), hasSize(1));
    }

    /**
     * Test method for {@link FlowDriverOutput#verify(String, String, ModelTester)}.
     */
    @Test
    public void verify_uri_uri_tester() {
        MockFlowDriverOutput<Text> mock = MockFlowDriverOutput.text(getClass(), tool_data_rule("Hello3"));
        mock.verify("data/dummy", "data/dummy2", modelTester("Hello1"));
        assertThat(test(mock.getVerifier(), "Hello1", "Hello2", "Hello3"), hasSize(2));
    }

    /**
     * Test method for {@link FlowDriverOutput#verify(Iterable, String, ModelTester)}.
     */
    @Test
    public void verify_iterable_uri_tester() {
        MockFlowDriverOutput<Text> mock = MockFlowDriverOutput.text(getClass(), tool_rule("Hello3"));
        mock.verify(list("Hello1", "Hello2", "Hello3"), "data/dummy", modelTester("Hello1"));
        assertThat(test(mock.getVerifier(), "Hello1", "Hello2", "Hello3"), hasSize(2));
    }

    /**
     * Test method for {@link FlowDriverOutput#verify(Provider, String, ModelTester)}.
     */
    @Test
    public void verify_provider_uri_tester() {
        MockFlowDriverOutput<Text> mock = MockFlowDriverOutput.text(getClass(), tool_rule("Hello3"));
        mock.verify(provider("Hello1", "Hello2", "Hello3"), "data/dummy", modelTester("Hello1"));
        assertThat(test(mock.getVerifier(), "Hello1", "Hello2", "Hello3"), hasSize(2));
    }

    /**
     * Test method for {@link FlowDriverOutput#verify(Class, String, String, ModelTester)}.
     */
    @Test
    public void verify_directio_path_uri_tester() {
        MockFlowDriverOutput<Text> mock = MockFlowDriverOutput.text(getClass(), tool_data_rule("Hello3"));
        mock.verify(TextStreamFormat.class, "directio/hello123.txt", "data/dummy2", modelTester("Hello1"));
        assertThat(test(mock.getVerifier(), "Hello1", "Hello2", "Hello3"), hasSize(2));
    }

    /**
     * Test method for {@link FlowDriverOutput#verify(Class, File, String, ModelTester)}.
     */
    @Test
    public void verify_directio_file_uri_tester() {
        MockFlowDriverOutput<Text> mock = MockFlowDriverOutput.text(getClass(), tool_data_rule("Hello3"));
        mock.verify(TextStreamFormat.class, asFile("directio/hello123.txt"), "data/dummy2", modelTester("Hello1"));
        assertThat(test(mock.getVerifier(), "Hello1", "Hello2", "Hello3"), hasSize(2));
    }

    /**
     * Test method for {@link FlowDriverOutput#verify(String, ModelVerifier)}.
     */
    @Test
    public void verify_uri_verifier() {
        MockFlowDriverOutput<Text> mock = MockFlowDriverOutput.text(getClass(), tool_data);
        mock.verify("data/dummy", modelVerifier("Hello2"));
        assertThat(test(mock.getVerifier(), "Hello1", "Hello2", "Hello3"), hasSize(1));
    }

    /**
     * Test method for {@link FlowDriverOutput#verify(Iterable, ModelVerifier)}.
     */
    @Test
    public void verify_iterable_verifier() {
        MockFlowDriverOutput<Text> mock = MockFlowDriverOutput.text(getClass(), provider());
        mock.verify(list("Hello1", "Hello2", "Hello3"), modelVerifier("Hello2"));
        assertThat(test(mock.getVerifier(), "Hello1", "Hello2", "Hello3"), hasSize(1));
    }

    /**
     * Test method for {@link FlowDriverOutput#verify(com.asakusafw.utils.io.Provider, ModelVerifier)}.
     */
    @Test
    public void verify_provider_verifier() {
        MockFlowDriverOutput<Text> mock = MockFlowDriverOutput.text(getClass(), provider());
        mock.verify(provider("Hello1", "Hello2", "Hello3"), modelVerifier("Hello2"));
        assertThat(test(mock.getVerifier(), "Hello1", "Hello2", "Hello3"), hasSize(1));
    }

    /**
     * Test method for {@link FlowDriverOutput#verify(Class, String, ModelVerifier)}.
     */
    @Test
    public void verify_directio_path_verifier() {
        MockFlowDriverOutput<Text> mock = MockFlowDriverOutput.text(getClass(), tool_data_rule("Hello3"));
        mock.verify(TextStreamFormat.class, "directio/hello123.txt", modelVerifier("Hello2"));
        assertThat(test(mock.getVerifier(), "Hello1", "Hello2", "Hello3"), hasSize(1));
    }

    /**
     * Test method for {@link FlowDriverOutput#verify(Class, File, ModelVerifier)}.
     */
    @Test
    public void verify_directio_file_verifier() {
        MockFlowDriverOutput<Text> mock = MockFlowDriverOutput.text(getClass(), tool_data_rule("Hello3"));
        mock.verify(TextStreamFormat.class, asFile("directio/hello123.txt"), modelVerifier("Hello2"));
        assertThat(test(mock.getVerifier(), "Hello1", "Hello2", "Hello3"), hasSize(1));
    }

    /**
     * Test method for {@link FlowDriverOutput#filter(UnaryOperator)}.
     */
    @Test
    public void filter() {
        MockFlowDriverOutput<Text> mock = MockFlowDriverOutput.text(getClass(), tool_rule("Hello3"));
        UnaryOperator<DataModelSource> filter = source -> new DataModelSource() {
            @Override
            public DataModelReflection next() throws IOException {
                DataModelReflection next = source.next();
                if (next == null || DEFINITION.toObject(next).toString().equals("Hello2")) {
                    return next;
                }
                return next;
            }
            @Override
            public void close() throws IOException {
                source.close();
            }
        };
        mock.filter(filter);
        mock.verify(list("Hello1", "Hello3"), "data/dummy");
        assertThat(test(mock.getVerifier(), "Hello1", "Hello2", "Hello3"), hasSize(1));
    }

    /**
     * Test method for {@link FlowDriverOutput#transform(ModelTransformer)}.
     */
    @Test
    public void transform() {
        MockFlowDriverOutput<Text> mock = MockFlowDriverOutput.text(getClass(), tool_rule("Hello3!"));
        ModelTransformer<Text> transformer = new ModelTransformer<Text>() {
            @Override
            public void transform(Text model) {
                model.set(model.toString() + "!");
            }
        };
        mock.transform(transformer);
        mock.verify(list("Hello1!", "Hello2!", "Hello3!"), "data/dummy");
        assertThat(test(mock.getVerifier(), "Hello1", "Hello2", "Hello3"), hasSize(1));
    }

    /**
     * Test method for {@link FlowDriverOutput#dumpActual(String)}.
     */
    @Test
    public void dumpActual_uri() {
        MockDataModelSink sink = new MockDataModelSink();
        MockFlowDriverOutput<Text> mock = MockFlowDriverOutput.text(getClass(), new MockTestDataToolProvider() {
            @Override
            public DataModelSinkFactory getDataModelSinkFactory(URI uri) {
                assertThat(uri.getPath(), endsWith("testing"));
                return new DataModelSinkFactory() {
                    @Override
                    public <T> DataModelSink createSink(DataModelDefinition<T> definition, TestContext context) {
                        return sink;
                    }
                };
            }
        });
        mock.dumpActual("testing");
        checkInstance(mock.getResultSink(), sink);
    }

    /**
     * Test method for {@link FlowDriverOutput#dumpActual(java.io.File)}.
     */
    @Test
    public void dumpActual_file() {
        File file = new File(folder.getRoot(), "testing");
        MockDataModelSink sink = new MockDataModelSink();
        MockFlowDriverOutput<Text> mock = MockFlowDriverOutput.text(getClass(), new MockTestDataToolProvider() {
            @Override
            public DataModelSinkFactory getDataModelSinkFactory(URI uri) {
                assertThat(new File(uri).getName(), is(file.getName()));
                return new DataModelSinkFactory() {
                    @Override
                    public <T> DataModelSink createSink(DataModelDefinition<T> definition, TestContext context) {
                        return sink;
                    }
                };
            }
        });
        mock.dumpActual(file);
        checkInstance(mock.getResultSink(), sink);
    }

    /**
     * Test method for {@link FlowDriverOutput#dumpActual(Class, File)}.
     * @throws Exception if failed
     */
    @Test
    public void dumpActual_directio() throws Exception {
        File file = new File(folder.getRoot(), "testing");
        MockFlowDriverOutput<?> mock = new MockFlowDriverOutput<>(getClass(), Simple.class, provider())
                .dumpActual(SimpleStreamFormat.class, file);
        DefaultDataModelDefinition<Simple> def = new DefaultDataModelDefinition<>(Simple.class);
        try (DataModelSink sink = mock.getResultSink().createSink(def, CONTEXT)) {
            Simple buf = new Simple();
            buf.setValueAsString("Hello, world!");
            sink.put(def.toReflection(buf));
        }
        try (Scanner s = new Scanner(file, StandardCharsets.UTF_8.name())) {
            assertThat(s.hasNextLine(), is(true));
            assertThat(s.nextLine(), is("Hello, world!"));
            assertThat(s.hasNextLine(), is(false));
        }
    }

    /**
     * Test method for {@link FlowDriverOutput#dumpDifference(String)}.
     */
    @Test
    public void dumpDifference_uri() {
        MockDiffSink sink = new MockDiffSink();
        MockFlowDriverOutput<Text> mock = MockFlowDriverOutput.text(getClass(), new MockTestDataToolProvider() {
            @Override
            public DifferenceSinkFactory getDifferenceSinkFactory(URI uri) {
                assertThat(uri.getPath(), endsWith("testing"));
                return new DifferenceSinkFactory() {
                    @Override
                    public <T> DifferenceSink createSink(DataModelDefinition<T> definition, TestContext context) {
                        return sink;
                    }
                };
            }
        });
        mock.dumpDifference("testing");
        checkInstance(mock.getDifferenceSink(), sink);
    }

    /**
     * Test method for {@link FlowDriverOutput#dumpDifference(java.io.File)}.
     */
    @Test
    public void dumpDifference_file() {
        File file = new File(folder.getRoot(), "testing");
        MockDiffSink sink = new MockDiffSink();
        MockFlowDriverOutput<Text> mock = MockFlowDriverOutput.text(getClass(), new MockTestDataToolProvider() {
            @Override
            public DifferenceSinkFactory getDifferenceSinkFactory(URI uri) {
                assertThat(uri.getPath(), endsWith("testing"));
                return new DifferenceSinkFactory() {
                    @Override
                    public <T> DifferenceSink createSink(DataModelDefinition<T> definition, TestContext context) {
                        return sink;
                    }
                };
            }
        });
        mock.dumpDifference(file);
        checkInstance(mock.getDifferenceSink(), sink);
    }

    private final MockTestDataToolProvider tool_data_rule(String... key) {
        return new MockTestDataToolProvider() {
            @Override
            public DataModelSourceFactory getDataModelSourceFactory(URI uri) {
                assertThat(uri.getPath(), endsWith("data/dummy"));
                return factory("Hello1", "Hello2", "Hello3");
            }
            @Override
            public VerifyRuleFactory getVerifyRuleFactory(URI ruleUri, List<? extends TestRule> extraRules) {
                assertThat(ruleUri.getPath(), endsWith("data/dummy2"));
                return new MockVerifyRuleFactory(extraRules, key);
            }
        };
    }

    private final MockTestDataToolProvider tool_data = new MockTestDataToolProvider() {
        @Override
        public DataModelSourceFactory getDataModelSourceFactory(URI uri) {
            assertThat(uri.toString(), endsWith("data/dummy"));
            return factory("Hello1", "Hello2", "Hello3");
        }
    };

    private MockTestDataToolProvider tool_rule(String... key) {
        return new MockTestDataToolProvider() {
            @Override
            public VerifyRuleFactory getVerifyRuleFactory(URI ruleUri, List<? extends TestRule> extraRules) {
                assertThat(ruleUri.getPath(), endsWith("data/dummy"));
                return new MockVerifyRuleFactory(extraRules, key);
            }
        };
    }

    private final ModelTester<Text> modelTester(String key) {
        return (expected, actual) -> {
            if (actual.toString().equals(key)) {
                return key;
            }
            return null;
        };
    }

    private final ModelVerifier<Text> modelVerifier(String key) {
        return new ModelVerifier<Text>() {
            @Override
            public Object getKey(Text target) {
                return target.toString();
            }
            @Override
            public Object verify(Text expected, Text actual) {
                if (expected.toString().equals(key)) {
                    return key;
                }
                return null;
            }
        };
    }

    private List<Difference> test(VerifierFactory factory, String... pattern) {
        try (Verifier v = factory.createVerifier(DEFINITION, new VerifyContext(CONTEXT));
                DataModelSource a = source(pattern)) {
            return v.verify(a);
        } catch (IOException e) {
            throw new AssertionError(e);
        }
    }

    private void insert(DataModelSinkFactory sinkFactory, String... values) {
        try (DataModelSink sink = sinkFactory.createSink(DEFINITION, CONTEXT)) {
            for (Text text : list(values)) {
                sink.put(DEFINITION.toReflection(text));
            }
        } catch (IOException e) {
            throw new AssertionError(e);
        }
    }

    private void checkInstance(DataModelSinkFactory factory, DataModelSink instance) {
        try (DataModelSink sink = factory.createSink(DEFINITION, CONTEXT)) {
            assertThat(sink, is(sameInstance(instance)));
        } catch (IOException e) {
            throw new AssertionError(e);
        }
    }

    private void checkInstance(DifferenceSinkFactory factory, DifferenceSink instance) {
        try (DifferenceSink sink = factory.createSink(DEFINITION, CONTEXT)) {
            assertThat(sink, is(sameInstance(instance)));
        } catch (IOException e) {
            throw new AssertionError(e);
        }
    }

    private static final class MockVerifyRuleFactory extends VerifyRuleFactory {

        private final Set<String> fail;

        private final List<? extends TestRule> extras;

        MockVerifyRuleFactory(List<? extends TestRule> extraRules, String... fail) {
            this.extras = extraRules == null ? Collections.emptyList() : extraRules;
            this.fail = new HashSet<>(Arrays.asList(fail));
        }

        @Override
        public <T> VerifyRule createRule(DataModelDefinition<T> definition, VerifyContext context) throws IOException {
            return new MockVerifyRule(extras, fail);
        }
    }

    private static final class MockVerifyRule implements VerifyRule {

        private final List<? extends TestRule> extras;

        private final Set<String> fail;

        MockVerifyRule(List<? extends TestRule> extras, Set<String> fail) {
            this.extras = extras;
            this.fail = fail;
        }

        @Override
        public Object getKey(DataModelReflection target) {
            return target;
        }

        @Override
        public Object verify(DataModelReflection expected, DataModelReflection actual) {
            if (actual == null) {
                return "extra: " + actual;
            }
            if (fail.contains(DEFINITION.toObject(actual).toString())) {
                return "Unexpected";
            }
            for (TestRule rule : extras) {
                Object result = rule.verify(expected, actual);
                if (result != null) {
                    return result;
                }
            }
            return null;
        }
    }

    private static final class MockDiffSink implements DifferenceSink {

        MockDiffSink() {
            return;
        }

        @Override
        public void put(Difference difference) throws IOException {
            return;
        }

        @Override
        public void close() throws IOException {
            return;
        }
    }
}
