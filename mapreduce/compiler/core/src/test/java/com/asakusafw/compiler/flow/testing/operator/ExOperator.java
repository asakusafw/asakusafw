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
package com.asakusafw.compiler.flow.testing.operator;

import java.util.Iterator;
import java.util.List;

import com.asakusafw.compiler.flow.testing.model.Ex1;
import com.asakusafw.compiler.flow.testing.model.Ex2;
import com.asakusafw.compiler.flow.testing.model.ExSummarized;
import com.asakusafw.runtime.core.Result;
import com.asakusafw.vocabulary.model.Key;
import com.asakusafw.vocabulary.operator.Branch;
import com.asakusafw.vocabulary.operator.CoGroup;
import com.asakusafw.vocabulary.operator.Fold;
import com.asakusafw.vocabulary.operator.Logging;
import com.asakusafw.vocabulary.operator.Sticky;
import com.asakusafw.vocabulary.operator.Summarize;
import com.asakusafw.vocabulary.operator.Update;
import com.asakusafw.vocabulary.operator.Volatile;

/**
 * Operator class for testing.
 */
public abstract class ExOperator {

    /**
     * Update operator.
     * @param model target object
     * @param value value to be set
     */
    @Update
    public void update(Ex1 model, int value) {
        model.setValue(value);
    }

    /**
     * Volatile.
     * @param model target object
     */
    @Volatile
    @Update
    public void random(Ex1 model) {
        model.setValue((int) (Math.random() * Integer.MAX_VALUE));
    }

    /**
     * sticky (raise error).
     * @param model target object
     */
    @Sticky
    @Update
    public void error(Ex1 model) {
        throw new IllegalStateException();
    }

    /**
     * folding operator.
     * @param a context
     * @param b felt
     */
    @Fold
    public void foldAdd(@Key(group = "STRING") Ex1 a, Ex1 b) {
        a.getValueOption().add(b.getValueOption());
    }

    /**
     * co-group operator.
     * @param list target list
     * @param result result
     */
    @CoGroup
    public void cogroupAdd(@Key(group = "STRING", order = "SID") List<Ex1> list, Result<Ex1> result) {
        Iterator<Ex1> iter = list.iterator();
        Ex1 first = iter.next();
        while (iter.hasNext()) {
            Ex1 next = iter.next();
            first.getValueOption().add(next.getValueOption());
        }
        result.add(first);
    }

    /**
     * branch operator.
     * @param model target model
     * @return result
     */
    @Branch
    public Answer branch(Ex1 model) {
        int value = model.getValueOption().get();
        if (value == 1) {
            return Answer.YES;
        }
        if (value == 0) {
            return Answer.NO;
        }
        return Answer.CANCEL;
    }

    /**
     * summarize operator.
     * @param model target object
     * @return results
     */
    @Summarize
    public abstract ExSummarized summarize(Ex1 model);

    /**
     * complex co-group operator.
     * @param ex1 model1
     * @param ex2 model2
     * @param r1 output1
     * @param r2 output2
     */
    @CoGroup
    public void cogroup(
            @Key(group = "value", order = "sid") List<Ex1> ex1,
            @Key(group = "value", order = "string DESC") List<Ex2> ex2,
            Result<Ex1> r1,
            Result<Ex2> r2) {
        if (ex1.isEmpty() == false) {
            r1.add(ex1.get(0));
        }
        if (ex2.isEmpty() == false) {
            r2.add(ex2.get(0));
        }
    }

    /**
     * logging operator.
     * @param ex1 model
     * @return result
     */
    @Logging
    public String logging(Ex1 ex1) {
        return ex1.getStringOption().toString();
    }

    /**
     * answer kind.
     */
    public enum Answer {

        /**
         * yes.
         */
        YES,

        /**
         * no.
         */
        NO,

        /**
         * canceled.
         */
        CANCEL,
    }
}
