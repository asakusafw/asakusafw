/**
 * Copyright 2011 Asakusa Framework Team.
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

import static org.hamcrest.Matchers.*;
import static org.junit.Assert.*;

import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;


import org.hamcrest.Matcher;
import org.junit.Test;

import com.asakusafw.vocabulary.batch.BatchDescription;
import com.asakusafw.vocabulary.batch.JobFlowWorkDescription;
import com.asakusafw.vocabulary.batch.Work;
import com.asakusafw.vocabulary.flow.FlowDescription;

/**
 * Test for {@link BatchDescription}.
 */
public class BatchDescriptionTest {

    /**
     * 単純なテスト。
     */
    @Test
    public void simple() {
        Collection<Work> works = exec(new SimpleBatch()).getWorks();
        assertThat(works.size(), is(1));
        assertThat(dependencies(works, JobFlow1.class), isJust());
    }

    /**
     * 連結リスト。
     */
    @Test
    public void list() {
        Collection<Work> works = exec(new ListBatch()).getWorks();
        assertThat(works.size(), is(3));
        assertThat(dependencies(works, JobFlow1.class), isJust());
        assertThat(dependencies(works, JobFlow2.class), isJust(JobFlow1.class));
        assertThat(dependencies(works, JobFlow3.class), isJust(JobFlow2.class));
    }

    /**
     * グラフ構造。
     */
    @Test
    public void graph() {
        Collection<Work> works = exec(new GraphBatch()).getWorks();
        assertThat(works.size(), is(4));
        assertThat(dependencies(works, JobFlow1.class), isJust());
        assertThat(dependencies(works, JobFlow2.class), isJust(JobFlow1.class));
        assertThat(dependencies(works, JobFlow3.class), isJust(JobFlow1.class));
        assertThat(dependencies(works, JobFlow4.class), isJust(JobFlow2.class, JobFlow3.class));
    }

    /**
     * 名前の衝突。
     */
    @Test(expected = IllegalStateException.class)
    public void nameConflict() {
        exec(new NameConflictBatch());
    }

    /**
     * 不正な名前。
     */
    @Test(expected = IllegalArgumentException.class)
    public void invalidName() {
        exec(new InvalidNameBatch());
    }

    /**
     * 注釈なしのジョブフローを含む。
     */
    @Test(expected = IllegalArgumentException.class)
    public void notAnnotatedJobFlow() {
        exec(new NotAnnotatedJobFlowBatch());
    }

    /**
     * soonやafterを指定しない (describeの途中)。
     */
    @Test(expected = IllegalStateException.class)
    public void underConstruction_middle() {
        exec(new MiddleUnderConstructionBatch());
    }

    /**
     * soonやafterを指定しない (describeの最後)。
     */
    @Test(expected = IllegalStateException.class)
    public void underConstruction_last() {
        exec(new LastUnderConstructionBatch());
    }

    private Set<Class<?>> dependencies(
            Collection<Work> works,
            Class<? extends FlowDescription> target) {
        Work found = findWork(works, target);
        Set<Class<?>> result = new HashSet<Class<?>>();
        for (Work work : found.getDependencies()) {
            result.add(asJobFlow(work));
        }
        return result;
    }

    private Class<?> asJobFlow(Work work) {
        assertThat(work.getDescription(), instanceOf(JobFlowWorkDescription.class));
        return ((JobFlowWorkDescription) work.getDescription()).getFlowClass();
    }

    private Work findWork(
            Collection<Work> works,
            Class<? extends FlowDescription> target) {
        JobFlowWorkDescription desc = new JobFlowWorkDescription(target);
        for (Work work : works) {
            if (work.getDescription().equals(desc)) {
                return work;
            }
        }
        throw new AssertionError(target + " in " + works);
    }

    private Matcher<? super Set<Class<?>>> isJust(Class<?>... classes) {
        Set<Class<?>> result = new HashSet<Class<?>>();
        Collections.addAll(result, classes);
        return is(result);
    }

    private BatchDescription exec(BatchDescription batch) {
        batch.start();
        return batch;
    }
}
