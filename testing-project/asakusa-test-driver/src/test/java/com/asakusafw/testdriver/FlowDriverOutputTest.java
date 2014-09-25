/**
 * Copyright 2011-2014 Asakusa Framework Team.
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
import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.apache.hadoop.io.Text;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;

import com.asakusafw.testdriver.core.DataModelDefinition;
import com.asakusafw.testdriver.core.DataModelReflection;
import com.asakusafw.testdriver.core.DataModelSink;
import com.asakusafw.testdriver.core.DataModelSinkFactory;
import com.asakusafw.testdriver.core.DataModelSource;
import com.asakusafw.testdriver.core.DataModelSourceFactory;
import com.asakusafw.testdriver.core.DataModelSourceFilter;
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
import com.asakusafw.utils.io.Provider;

/**
 * Test for {@link FlowDriverOutput}.
 */
public class FlowDriverOutputTest {

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
        MockFlowDriverOutput mock = new MockFlowDriverOutput(getClass(), new MockTestDataToolProvider());
        mock.prepare(factory("Hello1", "Hello2"));
        verify(mock.getSource(), DEFINITION, list("Hello1", "Hello2"));
    }

    /**
     * Test method for {@link FlowDriverOutput#verify(VerifierFactory)}.
     */
    @Test
    public void verify_factory() {
        MockFlowDriverOutput mock = new MockFlowDriverOutput(getClass(), new MockTestDataToolProvider());
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
        MockFlowDriverOutput mock = new MockFlowDriverOutput(getClass(), tool_rule("Hello3"));
        mock.verify(factory("Hello1", "Hello2", "Hello3"), "data/dummy", null);
        assertThat(test(mock.getVerifier(), "Hello1", "Hello2", "Hello3"), hasSize(1));
    }

    /**
     * Missing rule URI test for {@link FlowDriverOutput#verify(DataModelSourceFactory, String, ModelTester)}.
     */
    @Test(expected = IllegalArgumentException.class)
    public void verify_factory_uri_tester_missingRule() {
        MockFlowDriverOutput mock = new MockFlowDriverOutput(getClass(), new MockTestDataToolProvider());
        mock.verify(factory("Hello1", "Hello2", "Hello3"), "data/__MISSING__", null);
    }

    /**
     * simple test for {@link FlowDriverOutput#verify(DataModelSourceFactory, String, ModelTester)}.
     */
    @Test
    public void verify_factory_uri_tester_withTester() {
        MockFlowDriverOutput mock = new MockFlowDriverOutput(getClass(), tool_rule("Hello3"));
        mock.verify(factory("TESTER", "Hello2", "Hello3"), "data/dummy", modelTester("TESTER"));
        assertThat(test(mock.getVerifier(), "TESTER", "Hello2", "Hello3"), hasSize(2));
    }

    /**
     * Test method for {@link FlowDriverOutput#verify(DataModelSourceFactory, ModelVerifier)}.
     */
    @Test
    public void verify_factory_verifier() {
        MockFlowDriverOutput mock = new MockFlowDriverOutput(getClass(), new MockTestDataToolProvider());
        mock.verify(factory("Hello1", "VERIFIER", "Hello3"), modelVerifier("VERIFIER"));
        assertThat(test(mock.getVerifier(), "Hello1", "VERIFIER", "Hello3"), hasSize(1));
    }

    /**
     * Test method for {@link FlowDriverOutput#dumpActual(DataModelSinkFactory)}.
     */
    @Test
    public void dumpActual_factory() {
        final MockDataModelSink sink = new MockDataModelSink();
        MockFlowDriverOutput mock = new MockFlowDriverOutput(getClass(), new MockTestDataToolProvider());
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
        final MockDiffSink sink = new MockDiffSink();
        MockFlowDriverOutput mock = new MockFlowDriverOutput(getClass(), new MockTestDataToolProvider());
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
        MockFlowDriverOutput mock = new MockFlowDriverOutput(getClass(), tool_data);
        mock.prepare("data/dummy");
        verify(mock.getSource(), DEFINITION, list("Hello1", "Hello2", "Hello3"));
    }

    /**
     * missing resource in {@link FlowDriverOutput#prepare(String)}.
     */
    @Test(expected = IllegalArgumentException.class)
    public void prepare_uri_missing() {
        new MockFlowDriverOutput(getClass(), new MockTestDataToolProvider()).prepare("data/__MISSING__");
    }

    /**
     * Test method for {@link FlowDriverOutput#prepare(Iterable)}.
     */
    @Test
    public void prepare_collection() {
        MockFlowDriverOutput mock = new MockFlowDriverOutput(getClass(), new MockTestDataToolProvider());
        mock.prepare(list("Hello1", "Hello2"));
        verify(mock.getSource(), DEFINITION, list("Hello1", "Hello2"));
    }

    /**
     * Test method for {@link FlowDriverOutput#prepare(Provider)}.
     */
    @Test
    public void prepare_iterator() {
        MockFlowDriverOutput mock = new MockFlowDriverOutput(getClass(), new MockTestDataToolProvider());
        mock.prepare(provider("Hello1", "Hello2"));
        verify(mock.getSource(), DEFINITION, list("Hello1", "Hello2"));
    }

    /**
     * Test method for {@link FlowDriverOutput#verify(String, String)}.
     */
    @Test
    public void verify_uri_uri() {
        MockFlowDriverOutput mock = new MockFlowDriverOutput(getClass(), tool_data_rule("Hello3"));
        mock.verify("data/dummy", "data/dummy2");
        assertThat(test(mock.getVerifier(), "Hello1", "Hello2", "Hello3"), hasSize(1));
    }

    /**
     * Test method for {@link FlowDriverOutput#verify(String, String)}.
     */
    @Test(expected = IllegalArgumentException.class)
    public void verify_uri_uri_missingExpect() {
        MockFlowDriverOutput mock = new MockFlowDriverOutput(getClass(), tool_rule("Hello3"));
        mock.verify("data/__MISSING__", "data/dummy");
    }

    /**
     * Test method for {@link FlowDriverOutput#verify(DataModelSourceFactory, String)}.
     */
    @Test
    public void verify_factory_uri() {
        MockFlowDriverOutput mock = new MockFlowDriverOutput(getClass(), tool_rule("Hello3"));
        mock.verify(factory("Hello1", "Hello2", "Hello3"), "data/dummy");
        assertThat(test(mock.getVerifier(), "Hello1", "Hello2", "Hello3"), hasSize(1));
    }

    /**
     * Test method for {@link FlowDriverOutput#verify(Iterable, String)}.
     */
    @Test
    public void verify_iterable_uri() {
        MockFlowDriverOutput mock = new MockFlowDriverOutput(getClass(), tool_rule("Hello3"));
        mock.verify(list("Hello1", "Hello2", "Hello3"), "data/dummy");
        assertThat(test(mock.getVerifier(), "Hello1", "Hello2", "Hello3"), hasSize(1));
    }

    /**
     * Test method for {@link FlowDriverOutput#verify(com.asakusafw.utils.io.Provider, String)}.
     */
    @Test
    public void verify_provider_uri() {
        MockFlowDriverOutput mock = new MockFlowDriverOutput(getClass(), tool_rule("Hello3"));
        mock.verify(provider("Hello1", "Hello2", "Hello3"), "data/dummy");
        assertThat(test(mock.getVerifier(), "Hello1", "Hello2", "Hello3"), hasSize(1));
    }

    /**
     * Test method for {@link FlowDriverOutput#verify(String, String, ModelTester)}.
     */
    @Test
    public void verify_uri_uri_tester() {
        MockFlowDriverOutput mock = new MockFlowDriverOutput(getClass(), tool_data_rule("Hello3"));
        mock.verify("data/dummy", "data/dummy2", modelTester("Hello1"));
        assertThat(test(mock.getVerifier(), "Hello1", "Hello2", "Hello3"), hasSize(2));
    }

    /**
     * Test method for {@link FlowDriverOutput#verify(Iterable, String, ModelTester)}.
     */
    @Test
    public void verify_iterable_uri_tester() {
        MockFlowDriverOutput mock = new MockFlowDriverOutput(getClass(), tool_rule("Hello3"));
        mock.verify(list("Hello1", "Hello2", "Hello3"), "data/dummy", modelTester("Hello1"));
        assertThat(test(mock.getVerifier(), "Hello1", "Hello2", "Hello3"), hasSize(2));
    }

    /**
     * Test method for {@link FlowDriverOutput#verify(Provider, String, ModelTester)}.
     */
    @Test
    public void verify_provider_uri_tester() {
        MockFlowDriverOutput mock = new MockFlowDriverOutput(getClass(), tool_rule("Hello3"));
        mock.verify(provider("Hello1", "Hello2", "Hello3"), "data/dummy", modelTester("Hello1"));
        assertThat(test(mock.getVerifier(), "Hello1", "Hello2", "Hello3"), hasSize(2));
    }

    /**
     * Test method for {@link FlowDriverOutput#verify(String, ModelVerifier)}.
     */
    @Test
    public void verify_uri_verifier() {
        MockFlowDriverOutput mock = new MockFlowDriverOutput(getClass(), tool_data);
        mock.verify("data/dummy", modelVerifier("Hello2"));
        assertThat(test(mock.getVerifier(), "Hello1", "Hello2", "Hello3"), hasSize(1));
    }

    /**
     * Test method for {@link FlowDriverOutput#verify(Iterable, ModelVerifier)}.
     */
    @Test
    public void verify_iterable_verifier() {
        MockFlowDriverOutput mock = new MockFlowDriverOutput(getClass(), new MockTestDataToolProvider());

        mock.verify(list("Hello1", "Hello2", "Hello3"), modelVerifier("Hello2"));
        assertThat(test(mock.getVerifier(), "Hello1", "Hello2", "Hello3"), hasSize(1));
    }

    /**
     * Test method for {@link FlowDriverOutput#verify(com.asakusafw.utils.io.Provider, ModelVerifier)}.
     */
    @Test
    public void verify_privider_verifier() {
        MockFlowDriverOutput mock = new MockFlowDriverOutput(getClass(), new MockTestDataToolProvider());
        mock.verify(provider("Hello1", "Hello2", "Hello3"), modelVerifier("Hello2"));
        assertThat(test(mock.getVerifier(), "Hello1", "Hello2", "Hello3"), hasSize(1));
    }

    /**
     * Test method for {@link FlowDriverOutput#filter(DataModelSourceFilter)}.
     */
    @Test
    public void filter() {
        MockFlowDriverOutput mock = new MockFlowDriverOutput(getClass(), tool_rule("Hello3"));
        DataModelSourceFilter filter = new DataModelSourceFilter() {
            @Override
            public DataModelSource apply(final DataModelSource source) {
                return new DataModelSource() {
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
        MockFlowDriverOutput mock = new MockFlowDriverOutput(getClass(), tool_rule("Hello3!"));
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
        final MockDataModelSink sink = new MockDataModelSink();
        MockFlowDriverOutput mock = new MockFlowDriverOutput(getClass(), new MockTestDataToolProvider() {
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
        final File file = new File(folder.getRoot(), "testing");
        final MockDataModelSink sink = new MockDataModelSink();
        MockFlowDriverOutput mock = new MockFlowDriverOutput(getClass(), new MockTestDataToolProvider() {
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
     * Test method for {@link FlowDriverOutput#dumpDifference(String)}.
     */
    @Test
    public void dumpDifference_uri() {
        final MockDiffSink sink = new MockDiffSink();
        MockFlowDriverOutput mock = new MockFlowDriverOutput(getClass(), new MockTestDataToolProvider() {
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
        final File file = new File(folder.getRoot(), "testing");
        final MockDiffSink sink = new MockDiffSink();
        MockFlowDriverOutput mock = new MockFlowDriverOutput(getClass(), new MockTestDataToolProvider() {
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

    private final MockTestDataToolProvider tool_data_rule(final String... key) {
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

    private MockTestDataToolProvider tool_rule(final String... key) {
        return new MockTestDataToolProvider() {
            @Override
            public VerifyRuleFactory getVerifyRuleFactory(URI ruleUri, List<? extends TestRule> extraRules) {
                assertThat(ruleUri.getPath(), endsWith("data/dummy"));
                return new MockVerifyRuleFactory(extraRules, key);
            }
        };
    }

    private final ModelTester<Text> modelTester(final String key) {
        return new ModelTester<Text>() {
            @Override
            public Object verify(Text expected, Text actual) {
                if (actual.toString().equals(key)) {
                    return key;
                }
                return null;
            }
        };
    }

    private final ModelVerifier<Text> modelVerifier(final String key) {
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
        try {
            Verifier v = factory.createVerifier(DEFINITION, new VerifyContext(CONTEXT));
            try {
                DataModelSource a = source(pattern);
                try {
                    return v.verify(a);
                } finally {
                    a.close();
                }
            } finally {
                v.close();
            }
        } catch (IOException e) {
            throw new AssertionError(e);
        }
    }

    private void insert(DataModelSinkFactory sinkFactory, String... values) {
        try {
            DataModelSink sink = sinkFactory.createSink(DEFINITION, CONTEXT);
            try {
                for (Text text : list(values)) {
                    sink.put(DEFINITION.toReflection(text));
                }
            } finally {
                sink.close();
            }
        } catch (IOException e) {
            throw new AssertionError(e);
        }
    }

    private void checkInstance(DataModelSinkFactory factory, DataModelSink instance) {
        try {
            DataModelSink sink = factory.createSink(DEFINITION, CONTEXT);
            try {
                assertThat(sink, is(sameInstance(instance)));
            } finally {
                sink.close();
            }
        } catch (IOException e) {
            throw new AssertionError(e);
        }
    }

    private void checkInstance(DifferenceSinkFactory factory, DifferenceSink instance) {
        try {
            DifferenceSink sink = factory.createSink(DEFINITION, CONTEXT);
            try {
                assertThat(sink, is(sameInstance(instance)));
            } finally {
                sink.close();
            }
        } catch (IOException e) {
            throw new AssertionError(e);
        }
    }

    private static final class MockVerifyRuleFactory extends VerifyRuleFactory {

        private final Set<String> fail;

        private final List<? extends TestRule> extras;

        MockVerifyRuleFactory(List<? extends TestRule> extraRules, String... fail) {
            this.extras = extraRules == null ? Collections.<TestRule>emptyList() : extraRules;
            this.fail = new HashSet<String>(Arrays.asList(fail));
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
