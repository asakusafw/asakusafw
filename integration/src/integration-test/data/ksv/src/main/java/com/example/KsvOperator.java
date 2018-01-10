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
package com.example;

import java.util.List;

import com.asakusafw.runtime.core.Result;
import com.asakusafw.vocabulary.flow.processor.PartialAggregation;
import com.asakusafw.vocabulary.model.Key;
import com.asakusafw.vocabulary.operator.CoGroup;
import com.asakusafw.vocabulary.operator.Fold;
import com.asakusafw.vocabulary.operator.GroupSort;
import com.asakusafw.vocabulary.operator.Sticky;
import com.asakusafw.vocabulary.operator.Update;
import com.example.modelgen.dmdl.model.Ksv;

/**
 * Operators.
 */
public abstract class KsvOperator {

    final Ksv buffer = new Ksv();

    /**
     * No-operations.
     * @param model the target model
     */
    @Update
    @Sticky
    public void nop(Ksv model) {
        return;
    }

    /**
     * Sort inputs and just output them.
     * @param in the inputs
     * @param out the outputs
     */
    @GroupSort
    public void sort(@Key(group = "key", order = "sort") List<Ksv> in, Result<Ksv> out) {
        for (Ksv model : in) {
            out.add(model);
        }
    }

    /**
     * Sort inputs and just output them.
     * @param in the inputs
     * @param out the outputs
     */
    @CoGroup
    public void cogroup(@Key(group = "key", order = "sort") List<Ksv> in, Result<Ksv> out) {
        for (Ksv model : in) {
            out.add(model);
        }
    }

    /**
     * Sort inputs and just output them.
     * @param in1 the first inputs
     * @param in2 the second inputs
     * @param out the outputs
     */
    @CoGroup
    public void cogroup2in(
            @Key(group = "key", order = "sort") List<Ksv> in1,
            @Key(group = "key", order = "sort") List<Ksv> in2,
            Result<Ksv> out) {
        for (Ksv model : in1) {
            out.add(model);
        }
        for (Ksv model : in2) {
            out.add(model);
        }
    }

    /**
     * Sort inputs and output them to both.
     * @param in the inputs
     * @param out1 the first output
     * @param out2 the second output
     */
    @CoGroup
    public void cogroup2out(
            @Key(group = "key", order = "sort") List<Ksv> in,
            Result<Ksv> out1,
            Result<Ksv> out2) {
        for (Ksv model : in) {
            buffer.copyFrom(model);
            out1.add(buffer);
            out2.add(model);
        }
    }

    /**
     * Sort inputs and output to individual results.
     * @param in1 the first inputs
     * @param in2 the second inputs
     * @param out1 the first output
     * @param out2 the second output
     */
    @CoGroup
    public void cogroup2inout(
            @Key(group = "key", order = "sort") List<Ksv> in1,
            @Key(group = "key", order = "sort") List<Ksv> in2,
            Result<Ksv> out1,
            Result<Ksv> out2) {
        for (Ksv model : in1) {
            out1.add(model);
        }
        for (Ksv model : in2) {
            out2.add(model);
        }
    }

    /**
     * Aggregates inputs w/ partial aggregation.
     * @param accumulator the accumulator object
     * @param operand the operand object
     */
    @Fold(partialAggregation = PartialAggregation.PARTIAL)
    public void aggregate(@Key(group = "key") Ksv accumulator, Ksv operand) {
        accumulator.getSortOption().add(operand.getSortOption());
    }
}
