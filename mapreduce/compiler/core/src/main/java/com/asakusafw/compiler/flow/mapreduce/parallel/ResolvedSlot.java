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

import java.util.List;

import com.asakusafw.compiler.common.Precondition;
import com.asakusafw.compiler.flow.DataClass;
import com.asakusafw.compiler.flow.DataClass.Property;

/**
 * Represents resolved information of {@link Slot}.
 */
public class ResolvedSlot {

    private final Slot source;

    private final int slotNumber;

    private final DataClass valueClass;

    private final List<Property> sortProperties;

    /**
     * Creates a new instance.
     * @param source the source slot information
     * @param slotNumber the slot number
     * @param valueClass the resolved data type of the slot
     * @param sortProperties the resolved properties of the sort key
     * @throws IllegalArgumentException if the parameters are {@code null}
     */
    public ResolvedSlot(Slot source, int slotNumber, DataClass valueClass, List<Property> sortProperties) {
        Precondition.checkMustNotBeNull(source, "source"); //$NON-NLS-1$
        Precondition.checkMustNotBeNull(valueClass, "valueClass"); //$NON-NLS-1$
        Precondition.checkMustNotBeNull(sortProperties, "sortProperties"); //$NON-NLS-1$
        this.source = source;
        this.slotNumber = slotNumber;
        this.valueClass = valueClass;
        this.sortProperties = sortProperties;
    }

    /**
     * Returns the source slot information.
     * @return the source slot information
     */
    public Slot getSource() {
        return source;
    }

    /**
     * Returns the slot number.
     * @return the slot number
     */
    public int getSlotNumber() {
        return slotNumber;
    }

    /**
     * Returns the resolved data type of the slot.
     * @return the resolved data type of the slot
     */
    public DataClass getValueClass() {
        return valueClass;
    }

    /**
     * Returns the resolved properties of the sort key.
     * @return the resolved properties of the sort key
     */
    public List<DataClass.Property> getSortProperties() {
        return sortProperties;
    }
}
