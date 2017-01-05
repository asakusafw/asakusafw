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

import com.asakusafw.compiler.flow.processor.MasterCheckFlowProcessor;
import com.asakusafw.compiler.flow.testing.model.Ex1;
import com.asakusafw.compiler.flow.testing.model.Ex2;
import com.asakusafw.vocabulary.model.Key;
import com.asakusafw.vocabulary.operator.MasterCheck;
import com.asakusafw.vocabulary.operator.MasterSelection;

/**
 * An operator class for testing {@link MasterCheckFlowProcessor}.
 */
public abstract class MasterCheckFlow {

    /**
     * simple.
     * @param master the master data
     * @param model target data model
     * @return hit/miss
     */
    @MasterCheck
    public abstract boolean simple(
            @Key(group = "string") Ex2 master,
            @Key(group = "string") Ex1 model);

    /**
     * w/ selector.
     * @param master the master data
     * @param model target data model
     * @return hit/miss
     */
    @MasterCheck(selection = "selector")
    public abstract boolean selection(
            @Key(group = "string") Ex2 master,
            @Key(group = "string") Ex1 model);

    /**
     * non-parameterized selector.
     * @param masters list of masters
     * @param model the data model
     * @return the selected master data, or {@code null} if there is no suitable master data
     */
    @MasterSelection
    public Ex2 selector(List<Ex2> masters, Ex1 model) {
        for (Ex2 master : masters) {
            if (master.getValueOption().equals(model.getValueOption())) {
                return master;
            }
        }
        return null;
    }
}
