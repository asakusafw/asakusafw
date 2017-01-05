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
package com.asakusafw.compiler.flow.stage;

import com.asakusafw.compiler.common.Precondition;
import com.asakusafw.utils.java.model.syntax.Name;

/**
 * A compiled model of {@link ShuffleModel}.
 */
public class CompiledShuffle {

    private final Name keyTypeName;

    private final Name valueTypeName;

    private final Name groupComparatorTypeName;

    private final Name sortComparatorTypeName;

    private final Name partitionerTypeName;

    /**
     * Creates a new instance.
     * @param keyTypeName the qualified class name of shuffle key
     * @param valueTypeName the qualified class name of shuffle value
     * @param groupComparatorTypeName the qualified class name of grouping comparator
     * @param sortComparatorTypeName the qualified class name of sort comparator
     * @param partitionerTypeName the qualified class name of shuffle partitioner
     * @throws IllegalArgumentException if the parameters are {@code null}
     */
    public CompiledShuffle(
            Name keyTypeName,
            Name valueTypeName,
            Name groupComparatorTypeName,
            Name sortComparatorTypeName,
            Name partitionerTypeName) {
        Precondition.checkMustNotBeNull(keyTypeName, "keyTypeName"); //$NON-NLS-1$
        Precondition.checkMustNotBeNull(valueTypeName, "valueTypeName"); //$NON-NLS-1$
        Precondition.checkMustNotBeNull(groupComparatorTypeName, "groupComparatorTypeName"); //$NON-NLS-1$
        Precondition.checkMustNotBeNull(sortComparatorTypeName, "sortComparatorTypeName"); //$NON-NLS-1$
        Precondition.checkMustNotBeNull(partitionerTypeName, "partitionerTypeName"); //$NON-NLS-1$
        this.keyTypeName = keyTypeName;
        this.valueTypeName = valueTypeName;
        this.groupComparatorTypeName = groupComparatorTypeName;
        this.sortComparatorTypeName = sortComparatorTypeName;
        this.partitionerTypeName = partitionerTypeName;
    }

    /**
     * Returns the qualified name of shuffle key class.
     * @return the qualified class name of shuffle key
     */
    public Name getKeyTypeName() {
        return keyTypeName;
    }

    /**
     * Returns the qualified name of shuffle value class.
     * @return the qualified class name of shuffle value
     */
    public Name getValueTypeName() {
        return valueTypeName;
    }

    /**
     * Returns the qualified name of grouping comparator class.
     * @return the qualified class name of grouping comparator
     */
    public Name getGroupComparatorTypeName() {
        return groupComparatorTypeName;
    }

    /**
     * Returns the qualified name of sort comparator class.
     * @return the qualified class name of sort comparator
     */
    public Name getSortComparatorTypeName() {
        return sortComparatorTypeName;
    }

    /**
     * Returns the qualified name of shuffle partitioner class.
     * @return the qualified class name of shuffle partitioner
     */
    public Name getPartitionerTypeName() {
        return partitionerTypeName;
    }
}
