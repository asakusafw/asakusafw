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
package com.asakusafw.compiler.repository;

import static org.hamcrest.Matchers.*;
import static org.junit.Assert.*;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.hamcrest.Matcher;
import org.junit.Rule;
import org.junit.Test;

import com.asakusafw.compiler.batch.AbstractWorkDescriptionProcessor;
import com.asakusafw.compiler.batch.AbstractWorkflowProcessor;
import com.asakusafw.compiler.batch.BatchCompilerEnvironmentProvider;
import com.asakusafw.compiler.batch.WorkDescriptionProcessor;
import com.asakusafw.compiler.batch.Workflow;
import com.asakusafw.compiler.batch.WorkflowProcessor;
import com.asakusafw.compiler.flow.example.SimpleJobFlow;
import com.asakusafw.utils.collections.Sets;
import com.asakusafw.vocabulary.batch.JobFlowWorkDescription;
import com.asakusafw.vocabulary.batch.WorkDescription;

/**
 * Test for {@link SpiWorkflowProcessorRepository}.
 */
public class SpiWorkflowProcessorRepositoryTest {

    /**
     * The compiler environment.
     */
    @Rule
    public BatchCompilerEnvironmentProvider prov = new BatchCompilerEnvironmentProvider();

    /**
     * {@link SpiWorkflowProcessorRepository#findWorkflowProcessors(java.util.Set)}
     */
    @Test
    public void findWorkflowProcessors_simple() {
        SpiWorkflowProcessorRepository repo = new SpiWorkflowProcessorRepository() {
            @Override
            protected Iterable<? extends WorkflowProcessor> loadServices() {
                return Arrays.asList(new WorkflowProcessor[] {
                        new MockProc1(),
                });
            }
        };
        repo.initialize(prov.getEnvironment());
        Set<WorkflowProcessor> processors = repo.findWorkflowProcessors(set(
                new MockDesc1()));
        assertThat(classes(processors), contains((Object) MockProc1.class));
    }

    /**
     * {@link SpiWorkflowProcessorRepository#findWorkflowProcessors(java.util.Set)}
     */
    @Test
    public void findWorkflowProcessors_multi() {
        SpiWorkflowProcessorRepository repo = new SpiWorkflowProcessorRepository() {
            @Override
            protected Iterable<? extends WorkflowProcessor> loadServices() {
                return Arrays.asList(new WorkflowProcessor[] {
                        new MockProc1(),
                        new MockProc2(),
                });
            }
        };
        repo.initialize(prov.getEnvironment());
        Set<WorkflowProcessor> processors = repo.findWorkflowProcessors(set(
                new MockDesc1(),
                new MockDesc2()));
        assertThat(classes(processors), contains((Object) MockProc2.class));
    }

    /**
     * {@link SpiWorkflowProcessorRepository#findWorkflowProcessors(java.util.Set)}
     */
    @Test
    public void findWorkflowProcessors_super() {
        SpiWorkflowProcessorRepository repo = new SpiWorkflowProcessorRepository() {
            @Override
            protected Iterable<? extends WorkflowProcessor> loadServices() {
                return Arrays.asList(new WorkflowProcessor[] {
                        new MockProc3(),
                });
            }
        };
        repo.initialize(prov.getEnvironment());
        Set<WorkflowProcessor> processors = repo.findWorkflowProcessors(set(
                new MockDesc1(),
                new MockDesc2(),
                new JobFlowWorkDescription(SimpleJobFlow.class)));
        assertThat(classes(processors), contains((Object) MockProc3.class));
    }

    /**
     * {@link SpiWorkflowProcessorRepository#findWorkflowProcessors(java.util.Set)}
     */
    @Test
    public void findWorkflowProcessors_containsUnknown() {
        SpiWorkflowProcessorRepository repo = new SpiWorkflowProcessorRepository() {
            @Override
            protected Iterable<? extends WorkflowProcessor> loadServices() {
                return Arrays.asList(new WorkflowProcessor[] {
                        new MockProc1(),
                        new MockProc2(),
                });
            }
        };
        repo.initialize(prov.getEnvironment());
        Set<WorkflowProcessor> processors = repo.findWorkflowProcessors(set(
                new MockDesc1(),
                new JobFlowWorkDescription(SimpleJobFlow.class),
                new MockDesc2()));
        assertThat(processors.size(), is(0));
    }

    /**
     * {@link SpiWorkflowProcessorRepository#findDescriptionProcessor(WorkDescription)}.
     */
    @Test
    public void findDescriptionProcessor_simple() {
        SpiWorkflowProcessorRepository repo = new SpiWorkflowProcessorRepository() {
            @Override
            protected Iterable<? extends WorkflowProcessor> loadServices() {
                return Arrays.asList(new WorkflowProcessor[] {
                        new MockProc1(),
                });
            }
        };
        repo.initialize(prov.getEnvironment());
        assertThat(
                repo.findDescriptionProcessor(new MockDesc1()),
                instanceOf(MockDescProc1.class));
        assertThat(
                repo.findDescriptionProcessor(new MockDesc2()),
                is(nullValue()));
    }

    /**
     * {@link SpiWorkflowProcessorRepository#findDescriptionProcessor(WorkDescription)}.
     */
    @Test
    public void findDescriptionProcessor_multi() {
        SpiWorkflowProcessorRepository repo = new SpiWorkflowProcessorRepository() {
            @Override
            protected Iterable<? extends WorkflowProcessor> loadServices() {
                return Arrays.asList(new WorkflowProcessor[] {
                        new MockProc1(),
                        new MockProc2(),
                });
            }
        };
        repo.initialize(prov.getEnvironment());
        assertThat(
                repo.findDescriptionProcessor(new MockDesc1()),
                instanceOf(MockDescProc1.class));
        assertThat(
                repo.findDescriptionProcessor(new MockDesc2()),
                instanceOf(MockDescProc2.class));
    }

    /**
     * {@link SpiWorkflowProcessorRepository#findDescriptionProcessor(WorkDescription)}.
     */
    @Test
    public void findDescriptionProcessor_super() {
        SpiWorkflowProcessorRepository repo = new SpiWorkflowProcessorRepository() {
            @Override
            protected Iterable<? extends WorkflowProcessor> loadServices() {
                return Arrays.asList(new WorkflowProcessor[] {
                        new MockProc3(),
                });
            }
        };
        repo.initialize(prov.getEnvironment());
        assertThat(
                repo.findDescriptionProcessor(new MockDesc1()),
                instanceOf(MockDescProc3.class));
        assertThat(
                repo.findDescriptionProcessor(new MockDesc2()),
                instanceOf(MockDescProc3.class));
    }

    private Set<WorkDescription> set(WorkDescription... descriptions) {
        return Sets.from(descriptions);
    }

    private Set<Object> classes(Set<?> instances) {
        Set<Object> results = new HashSet<>();
        for (Object t : instances) {
            results.add(t.getClass());
        }
        return results;
    }

    @SafeVarargs
    private static <T> Matcher<? super Set<T>> contains(T... values) {
        Set<T> expect = new HashSet<>();
        Collections.addAll(expect, values);
        return is(expect);
    }

    private static class MockProc1 extends AbstractWorkflowProcessor {
        public MockProc1() {
            return;
        }
        @Override
        public Collection<Class<? extends WorkDescriptionProcessor<?>>> getDescriptionProcessors() {
            List<Class<? extends WorkDescriptionProcessor<?>>> results = new ArrayList<>();
            results.add(MockDescProc1.class);
            return results;
        }
        @Override
        public void process(Workflow workflow) throws IOException {
            return;
        }
    }

    private static class MockProc2 extends AbstractWorkflowProcessor {
        public MockProc2() {
            return;
        }
        @Override
        public Collection<Class<? extends WorkDescriptionProcessor<?>>> getDescriptionProcessors() {
            List<Class<? extends WorkDescriptionProcessor<?>>> results = new ArrayList<>();
            results.add(MockDescProc1.class);
            results.add(MockDescProc2.class);
            return results;
        }
        @Override
        public void process(Workflow workflow) throws IOException {
            return;
        }
    }

    private static class MockProc3 extends AbstractWorkflowProcessor {
        public MockProc3() {
            return;
        }
        @Override
        public Collection<Class<? extends WorkDescriptionProcessor<?>>> getDescriptionProcessors() {
            List<Class<? extends WorkDescriptionProcessor<?>>> results = new ArrayList<>();
            results.add(MockDescProc3.class);
            return results;
        }
        @Override
        public void process(Workflow workflow) throws IOException {
            return;
        }
    }

    private static class MockDescProc1 extends AbstractWorkDescriptionProcessor<MockDesc1> {
        @SuppressWarnings("unused")
        public MockDescProc1() {
            return;
        }
        @Override
        public Object process(MockDesc1 description) throws IOException {
            return null;
        }
    }

    private static class MockDescProc2 extends AbstractWorkDescriptionProcessor<MockDesc2> {
        @SuppressWarnings("unused")
        public MockDescProc2() {
            return;
        }
        @Override
        public Object process(MockDesc2 description) throws IOException {
            return null;
        }
    }

    private static class MockDescProc3 extends AbstractWorkDescriptionProcessor<WorkDescription> {
        @SuppressWarnings("unused")
        public MockDescProc3() {
            return;
        }
        @Override
        public Object process(WorkDescription description) throws IOException {
            return null;
        }
    }

    private static class MockDesc1 extends WorkDescription {
        public MockDesc1() {
            return;
        }
        @Override
        public String getName() {
            return "mockdesc1";
        }
    }

    private static class MockDesc2 extends WorkDescription {
        public MockDesc2() {
            return;
        }
        @Override
        public String getName() {
            return "mockdesc2";
        }
    }
}
