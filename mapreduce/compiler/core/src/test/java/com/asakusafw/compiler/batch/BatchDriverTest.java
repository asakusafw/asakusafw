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
package com.asakusafw.compiler.batch;

import static org.hamcrest.Matchers.*;
import static org.junit.Assert.*;

import java.util.Collection;

import org.junit.Test;

import com.asakusafw.compiler.batch.batch.Abstract;
import com.asakusafw.compiler.batch.batch.DescribeFailBatch;
import com.asakusafw.compiler.batch.batch.InstantiateFailBatch;
import com.asakusafw.compiler.batch.batch.JobFlow1;
import com.asakusafw.compiler.batch.batch.NoEmptyParameterConstructor;
import com.asakusafw.compiler.batch.batch.NotAnnotated;
import com.asakusafw.compiler.batch.batch.SimpleBatch;
import com.asakusafw.compiler.batch.batch.TopLevel;
import com.asakusafw.vocabulary.batch.JobFlowWorkDescription;
import com.asakusafw.vocabulary.batch.Work;

/**
 * Test for {@link BatchDriver}.
 */
public class BatchDriverTest {

    /**
     * simple case.
     */
    @Test
    public void simple() {
        BatchDriver analyze = BatchDriver.analyze(SimpleBatch.class);
        assertThat(analyze.hasError(), is(false));

        BatchClass batch = analyze.getBatchClass();
        Collection<Work> works = batch.getDescription().getWorks();

        assertThat(works.size(), is(1));
        Work work = works.iterator().next();
        assertThat(work.getDeclaring(), is(batch.getDescription()));
        assertThat(work.getDependencies().size(), is(0));
        assertThat(work.getDescription(), is((Object) new JobFlowWorkDescription(JobFlow1.class)));
    }

    /**
     * w/ abstract.
     */
    @Test
    public void Abstract() {
        BatchDriver analyze = BatchDriver.analyze(Abstract.class);
        assertThat(analyze.hasError(), is(true));
    }

    /**
     * w/o public.
     */
    @Test
    public void NotPublic() {
        BatchDriver analyze = BatchDriver.analyze(NotPublic.class);
        assertThat(analyze.hasError(), is(true));
    }

    /**
     * w/o top-level.
     */
    @Test
    public void NotTopLevel() {
        BatchDriver analyze = BatchDriver.analyze(TopLevel.Inner.class);
        assertThat(analyze.hasError(), is(true));
    }

    /**
     * w/o annotation.
     */
    @Test
    public void NotAnnotated() {
        BatchDriver analyze = BatchDriver.analyze(NotAnnotated.class);
        assertThat(analyze.hasError(), is(true));
    }

    /**
     * w/o zero-parameters ctor.
     */
    @Test
    public void NoEmptyParameterConstructor() {
        BatchDriver analyze = BatchDriver.analyze(NoEmptyParameterConstructor.class);
        assertThat(analyze.hasError(), is(true));
    }

    /**
     * cannot instantiate.
     */
    @Test
    public void InstantiateFailure() {
        BatchDriver analyze = BatchDriver.analyze(InstantiateFailBatch.class);
        assertThat(analyze.hasError(), is(true));
    }

    /**
     * cannot describe.
     */
    @Test
    public void DescribeFailure() {
        BatchDriver analyze = BatchDriver.analyze(DescribeFailBatch.class);
        assertThat(analyze.hasError(), is(true));
    }
}
