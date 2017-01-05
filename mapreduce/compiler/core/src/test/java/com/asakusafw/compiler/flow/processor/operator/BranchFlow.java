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

import com.asakusafw.compiler.flow.processor.BranchFlowProcessor;
import com.asakusafw.compiler.flow.testing.model.Ex1;
import com.asakusafw.vocabulary.operator.Branch;

/**
 * An operator class for testing {@link BranchFlowProcessor}.
 */
public abstract class BranchFlow {

    /**
     * simple.
     * @param model target data model
     * @return branch target
     */
    @Branch
    public Speed simple(Ex1 model) {
        return withParameter(model, 100);
    }

    /**
     * parameterized.
     * @param model target data model
     * @param parameter additional parameter
     * @return branch target
     */
    @Branch
    public Speed withParameter(Ex1 model, int parameter) {
        if (model.getValue() > parameter) {
            return Speed.HIGH;
        }
        if (model.getValue() <= 0) {
            return Speed.STOP;
        }
        return Speed.LOW;
    }

    /**
     * Speed kind.
     */
    public enum Speed {

        /**
         * High speed.
         */
        HIGH,

        /**
         * Low speed.
         */
        LOW,

        /**
         * Stopped.
         */
        STOP,
    }
}
