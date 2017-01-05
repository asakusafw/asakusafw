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
package com.asakusafw.compiler.flow.processor.operator;

import java.util.List;

import com.asakusafw.compiler.flow.processor.MasterJoinFlowProcessor;
import com.asakusafw.compiler.flow.testing.model.Ex1;
import com.asakusafw.compiler.flow.testing.model.Ex2;
import com.asakusafw.compiler.flow.testing.model.ExJoined;
import com.asakusafw.compiler.flow.testing.model.ExJoined2;
import com.asakusafw.runtime.core.Result;
import com.asakusafw.vocabulary.operator.MasterJoin;
import com.asakusafw.vocabulary.operator.MasterSelection;
import com.asakusafw.vocabulary.operator.Split;

/**
 * An operator class for testing {@link MasterJoinFlowProcessor}.
 */
public abstract class MasterJoinFlow {

    /**
     * join.
     * @param ex1 master
     * @param ex2 transaction
     * @return result
     */
    @MasterJoin
    public abstract ExJoined join(Ex1 ex1, Ex2 ex2);

    /**
     * join w/ renaming join key.
     * @param ex1 master
     * @param ex2 transaction
     * @return result
     */
    @MasterJoin
    public abstract ExJoined2 renameKey(Ex1 ex1, Ex2 ex2);

    /**
     * w/ selector.
     * @param ex1 master
     * @param ex2 transaction
     * @return result
     */
    @MasterJoin(selection = "selector")
    public abstract ExJoined selection(Ex1 ex1, Ex2 ex2);

    /**
     * non-parameterized selector.
     * @param masters list of masters
     * @param model the data model
     * @return the selected master data, or {@code null} if there is no suitable master data
     */
    @MasterSelection
    public Ex1 selector(List<Ex1> masters, Ex2 model) {
        for (Ex1 master : masters) {
            if (master.getStringOption().equals(model.getStringOption())) {
                return master;
            }
        }
        return null;
    }

    /**
     * split.
     * @param joined joined data
     * @param ex1 master
     * @param ex2 transaction
     */
    @Split
    public abstract void split(ExJoined joined, Result<Ex1> ex1, Result<Ex2> ex2);
}
