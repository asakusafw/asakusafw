/**
 * Copyright 2011-2021 Asakusa Framework Team.
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
package com.asakusafw.vocabulary.batch;

import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicBoolean;

import com.asakusafw.vocabulary.flow.FlowDescription;

/**
 * An abstract super class for describing the details of a batch workflow.
 * Subclasses must override {@link #describe()} method and build a workflow in the method, like as following:
<pre><code>
&#64;Batch(name = "hoge")
public class HogeBatch extends BatchDescription {
    &#64;Override
    public void describe() {
        Work first = run(FirstFlow.class).soon();
        Work second = run(SecondFlow.class).after(first);
        Work para = run(ParallelFlow.class).after(first);
        Work join = run(JoinFlow.class).after(second, para);
        ...
    }
}
</code></pre>
 * In the above example, only {@code FirstFlow} will be started first. And then, after the {@code FirstFlow} was
 * completed, {@code SecondFlow} and {@code ParallelFlow} will be concurrently processed. Finally,
 * {@code JoinFlow} was processed after the all other jobflows were completed.
 */
public abstract class BatchDescription {

    static final Work[] NOTHING = new Work[0];

    private final Map<String, Work> works = new LinkedHashMap<>();

    private DependencyBuilder adding;

    private final AtomicBoolean described = new AtomicBoolean(false);

    /**
     * Analyzes batch DSL using {@link #describe() batch description method}.
     * Application developers should not invoke this method directly.
     */
    public final void start() {
        if (described.compareAndSet(false, true) == false) {
            return;
        }
        describe();
        checkFlushed();
    }

    /**
     * Describes workflow structure.
     * Subclasses must override this method and build a workflow using Asakusa batch DSL.
     */
    protected abstract void describe();

    /**
     * Start registering a new <em>script job</em> to this batch.
     * @param scriptDefinition the script definition path
     * @return a builder for specifying dependencies of the adding job
     * @throws IllegalArgumentException if the script definition is something wrong
     * @throws IllegalStateException if another job is building dependencies
     * @deprecated does not supported
     */
    @Deprecated
    protected DependencyBuilder run(String scriptDefinition) {
        throw new UnsupportedOperationException();
    }

    /**
     * Start registering a new <em>jobflow</em> to this batch.
     * Clients must tell the dependencies of the target jobflow to the resulting builder of this method, by using
     * either methods.
     * <ul>
     * <li> {@link DependencyBuilder#soon() run(...).soon()} </li>
     * <li> {@link DependencyBuilder#after(Work, Work...) run(...).after(...)} </li>
     * </ul>
     * The both above methods will return a {@link Work} object which represents the registered jobflow.
     * And then the latter method accepts {@link Work} objects as its dependencies.
     * @param jobflow the target jobflow class
     * @return a builder for specifying dependencies of the adding job
     * @throws IllegalArgumentException if the jobflow class is something wrong
     * @throws IllegalStateException if another job is building dependencies
     */
    protected DependencyBuilder run(Class<? extends FlowDescription> jobflow) {
        if (jobflow == null) {
            throw new IllegalArgumentException("jobFlow must not be null"); //$NON-NLS-1$
        }
        return run0(new JobFlowWorkDescription(jobflow));
    }

    private DependencyBuilder run0(WorkDescription description) {
        assert description != null;
        checkFlushed();
        DependencyBuilder builder = new DependencyBuilder(description);
        adding = builder;
        return builder;
    }

    private void checkFlushed() {
        if (adding != null) {
            throw new IllegalStateException(MessageFormat.format(
                    Messages.getString("BatchDescription.errorIncomplete"), //$NON-NLS-1$
                    adding.description));
        }
    }

    /**
     * Returns a collection of Unit-of-Works which is represented in this batch.
     * @return a collection of Unit-of-Works of this batch
     */
    public Collection<Work> getWorks() {
        return new ArrayList<>(works.values());
    }

    Work register(Work work) {
        assert work != null;
        String name = work.getDescription().getName();
        if (works.containsKey(name)) {
            throw new IllegalStateException(MessageFormat.format(
                    Messages.getString("BatchDescription.errorDuplicateDescription"), //$NON-NLS-1$
                    name,
                    work.getDescription(),
                    works.get(name).getDescription()));
        }
        works.put(name, work);
        adding = null;
        return work;
    }

    @Override
    public String toString() {
        return MessageFormat.format(
                "{0}'{'works={1}'}'", //$NON-NLS-1$
                getClass().getName(),
                works);
    }

    /**
     * A builder for building batch by specifying Unit-of-Works and their dependencies.
     */
    protected class DependencyBuilder {

        final WorkDescription description;

        DependencyBuilder(WorkDescription description) {
            assert description != null;
            this.description = description;
        }

        /**
         * Completes registering job without any dependencies.
         * @return the registered job
         */
        public Work soon() {
            return register(new Work(BatchDescription.this, description, Collections.emptyList()));
        }

        /**
         * Completes registering job with dependencies.
         * The registered job will be executed after the precedent jobs were (successfully) completed.
         * @param dependency the precedent job
         * @param rest the other precedent job
         * @return the registered job
         * @throws IllegalArgumentException if some parameters are {@code null}
         */
        public Work after(Work dependency, Work... rest) {
            if (dependency == null) {
                throw new IllegalArgumentException("dependency must not be null"); //$NON-NLS-1$
            }
            if (rest == null) {
                throw new IllegalArgumentException("rest must not be null"); //$NON-NLS-1$
            }
            List<Work> dependencies = new ArrayList<>();
            dependencies.add(dependency);
            Collections.addAll(dependencies, rest);
            for (Work p : dependencies) {
                if (dependency.getDeclaring() != BatchDescription.this) {
                    throw new IllegalArgumentException(MessageFormat.format(
                            Messages.getString("BatchDescription.errorInvalidDependency"), //$NON-NLS-1$
                            p));
                }
            }
            return register(new Work(BatchDescription.this, description, dependencies));
        }
    }
}
