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
package com.asakusafw.compiler.flow.mapreduce.parallel;

import java.util.ArrayList;
import java.util.List;

import com.asakusafw.compiler.common.Precondition;
import com.asakusafw.compiler.flow.DataClass;
import com.asakusafw.compiler.flow.DataClass.Property;
import com.asakusafw.compiler.flow.FlowCompilingEnvironment;

/**
 * Resolved {@link Slot} objects into {@link ResolvedSlot}.
 */
public class SlotResolver {

    private final FlowCompilingEnvironment environment;

    /**
     * Creates a new instance.
     * @param environment the current environment
     * @throws IllegalArgumentException if the parameter is {@code null}
     */
    public SlotResolver(FlowCompilingEnvironment environment) {
        Precondition.checkMustNotBeNull(environment, "environment"); //$NON-NLS-1$
        this.environment = environment;
    }

    /**
     * Analyzes the target slot objects and returns resolved them.
     * @param slots the target slots
     * @return the resolved slots
     * @throws IllegalArgumentException if the parameter is {@code null}
     */
    public List<ResolvedSlot> resolve(List<Slot> slots) {
        Precondition.checkMustNotBeNull(slots, "slots"); //$NON-NLS-1$
        List<ResolvedSlot> results = new ArrayList<>();
        int number = 0;
        for (Slot slot : slots) {
            ResolvedSlot compiled = compile(slot, number++);
            results.add(compiled);
        }
        return results;
    }

    private ResolvedSlot compile(Slot slot, int number) {
        assert slot != null;
        DataClass valueClass = environment.getDataClasses().load(slot.getType());
        List<Property> sortProperties = new ArrayList<>();
        if (valueClass == null) {
            valueClass = new DataClass.Unresolved(environment.getModelFactory(), slot.getType());
            environment.error(Messages.getString("SlotResolver.errorMissingDataClass"), slot.getType()); //$NON-NLS-1$
        } else {
            for (String name : slot.getSortPropertyNames()) {
                Property property = valueClass.findProperty(name);
                if (property == null) {
                    environment.error(Messages.getString("SlotResolver.errorMissingProperty"), //$NON-NLS-1$
                            slot.getType(), name);
                } else {
                    sortProperties.add(property);
                }
            }
        }
        return new ResolvedSlot(slot, number, valueClass, sortProperties);
    }
}
