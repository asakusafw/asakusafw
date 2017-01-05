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

import com.asakusafw.compiler.flow.processor.MasterBranchFlowProcessor;
import com.asakusafw.compiler.flow.testing.model.Ex1;
import com.asakusafw.compiler.flow.testing.model.Ex2;
import com.asakusafw.vocabulary.model.Key;
import com.asakusafw.vocabulary.operator.MasterBranch;
import com.asakusafw.vocabulary.operator.MasterSelection;

/**
 * An operator class for testing {@link MasterBranchFlowProcessor}.
 */
public abstract class MasterBranchFlow {

    /**
     * simple.
     * @param master the master data
     * @param model target data model
     * @return branch target
     */
    @MasterBranch
    public Speed simple(
            @Key(group = "string") Ex2 master,
            @Key(group = "string") Ex1 model) {
        return withParameter(master, model, 30);
    }

    /**
     * parameterized.
     * @param master the master data
     * @param model target data model
     * @param parameter additional parameter
     * @return branch target
     */
    @MasterBranch
    public Speed withParameter(
            @Key(group = "string") Ex2 master,
            @Key(group = "string") Ex1 model,
            int parameter) {
        if (master == null) {
            return Speed.STOP;
        }
        if (master.getValue() + model.getValue() > parameter) {
            return Speed.HIGH;
        }
        if (master.getValue() + model.getValue()  <= 0) {
            return Speed.STOP;
        }
        return Speed.LOW;
    }

    /**
     * w/ selector.
     * @param master the master data
     * @param model target data model
     * @return branch target
     */
    @MasterBranch(selection = "selector")
    public Speed selection(
            @Key(group = "string") Ex2 master,
            @Key(group = "string") Ex1 model) {
        return withParameter(master, model, 30);
    }

    /**
     * w/ parameter and (non-parameterized) selector.
     * @param master the master data
     * @param model target data model
     * @param parameter additional parameter
     * @return branch target
     */
    @MasterBranch(selection = "selector")
    public Speed selectionWithParameter0(
            @Key(group = "string") Ex2 master,
            @Key(group = "string") Ex1 model,
            int parameter) {
        return withParameter(master, model, parameter);
    }

    /**
     * w/ parameter and (parameterized) selector.
     * @param master the master data
     * @param model target data model
     * @param parameter additional parameter
     * @return branch target
     */
    @MasterBranch(selection = "selectorWithParameter")
    public Speed selectionWithParameter1(
            @Key(group = "string") Ex2 master,
            @Key(group = "string") Ex1 model,
            int parameter) {
        return withParameter(master, model, parameter);
    }

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

    /**
     * parameterized selector.
     * @param masters list of masters
     * @param model the data model
     * @param parameter the additional parameter
     * @return the selected master data, or {@code null} if there is no suitable master data
     */
    @MasterSelection
    public Ex2 selectorWithParameter(List<Ex2> masters, Ex1 model, int parameter) {
        for (Ex2 master : masters) {
            if (master.getValueOption().has(parameter)) {
                return master;
            }
        }
        return null;
    }

    /**
     * Speed kind.
     */
    public enum Speed {

        /**
         * high speed.
         */
        HIGH,

        /**
         * low speed.
         */
        LOW,

        /**
         * stopped.
         */
        STOP,
    }
}
