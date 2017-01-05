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
import javax.annotation.Generated;

import com.asakusafw.compiler.flow.testing.model.Ex1;
import com.asakusafw.compiler.flow.testing.model.Ex2;
/**
 * An operator implementation class for{@link MasterCheckFlow}.
 */
@Generated("OperatorImplementationClassGenerator:0.1.0") public class MasterCheckFlowImpl extends MasterCheckFlow {
    /**
     * Creates a new instance.
     */
    public MasterCheckFlowImpl() {
        return;
    }
    @Override public boolean simple(Ex2 master, Ex1 model) {
        throw new UnsupportedOperationException("master check operator does not have method body");
    }
    @Override public boolean selection(Ex2 master, Ex1 model) {
        throw new UnsupportedOperationException("master check operator does not have method body");
    }
}