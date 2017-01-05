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
package com.asakusafw.compiler.flow.join;

import java.text.MessageFormat;
import java.util.Collections;
import java.util.List;
import java.util.Set;

import com.asakusafw.compiler.common.Precondition;
import com.asakusafw.compiler.flow.DataClass;
import com.asakusafw.compiler.flow.DataClass.Property;
import com.asakusafw.vocabulary.flow.graph.FlowResourceDescription;
import com.asakusafw.vocabulary.flow.graph.InputDescription;

/**
 * Represents an external resource for enabling side-data join operations.
 */
public class JoinResourceDescription implements FlowResourceDescription {

    private final InputDescription masterInput;

    private final DataClass masterDataClass;

    private final List<DataClass.Property> masterJoinKeys;

    private final DataClass transactionDataClass;

    private final List<DataClass.Property> transactionJoinKeys;

    /**
     * Creates a new instance.
     * @param masterInput the source input of the master data
     * @param masterDataClass the type of the master data
     * @param masterJoinKeys the join key of the master data
     * @param transactionDataClass the type of the transaction data
     * @param transactionJoinKeys the join key of the transaction data
     * @throws IllegalArgumentException if the parameters are {@code null}
     */
    public JoinResourceDescription(
            InputDescription masterInput, DataClass masterDataClass, List<Property> masterJoinKeys,
            DataClass transactionDataClass, List<Property> transactionJoinKeys) {
        Precondition.checkMustNotBeNull(masterInput, "masterInput"); //$NON-NLS-1$
        Precondition.checkMustNotBeNull(masterDataClass, "masterDataClass"); //$NON-NLS-1$
        Precondition.checkMustNotBeNull(masterJoinKeys, "masterJoinKeys"); //$NON-NLS-1$
        Precondition.checkMustNotBeNull(transactionDataClass, "transactionDataClass"); //$NON-NLS-1$
        Precondition.checkMustNotBeNull(transactionJoinKeys, "transactionJoinKeys"); //$NON-NLS-1$
        this.masterInput = masterInput;
        this.masterDataClass = masterDataClass;
        this.masterJoinKeys = masterJoinKeys;
        this.transactionDataClass = transactionDataClass;
        this.transactionJoinKeys = transactionJoinKeys;
    }

    @Override
    public Set<InputDescription> getSideDataInputs() {
        return Collections.singleton(masterInput);
    }

    /**
     * Returns the local name of the external resource that provides the master data.
     * @return the local name of the external resource
     */
    public String getCacheName() {
        return masterInput.getName();
    }

    /**
     * Returns the type of the master data.
     * @return the type of the master data
     */
    public DataClass getMasterDataClass() {
        return masterDataClass;
    }

    /**
     * Returns the type of the transaction data.
     * @return the type of the transaction data
     */
    public DataClass getTransactionDataClass() {
        return transactionDataClass;
    }

    /**
     * Returns the join key of the master data.
     * @return the join key of the master data
     */
    public List<DataClass.Property> getMasterJoinKeys() {
        return masterJoinKeys;
    }

    /**
     * Returns the join key of the master data.
     * @return the join key of the master data
     */
    public List<DataClass.Property> getTransactionJoinKeys() {
        return transactionJoinKeys;
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + masterDataClass.hashCode();
        result = prime * result + masterInput.hashCode();
        result = prime * result + masterJoinKeys.hashCode();
        result = prime * result + transactionDataClass.hashCode();
        result = prime * result + transactionJoinKeys.hashCode();
        return result;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj == null) {
            return false;
        }
        if (getClass() != obj.getClass()) {
            return false;
        }
        JoinResourceDescription other = (JoinResourceDescription) obj;
        if (!masterDataClass.equals(other.masterDataClass)) {
            return false;
        }
        if (!masterInput.equals(other.masterInput)) {
            return false;
        }
        if (!masterJoinKeys.equals(other.masterJoinKeys)) {
            return false;
        }
        if (!transactionDataClass.equals(other.transactionDataClass)) {
            return false;
        }
        if (!transactionJoinKeys.equals(other.transactionJoinKeys)) {
            return false;
        }
        return true;
    }

    @Override
    public String toString() {
        return MessageFormat.format(
                "{0}(left={1}{2}, {3}{4})", //$NON-NLS-1$
                masterInput.getName(),
                masterDataClass,
                masterJoinKeys,
                transactionDataClass,
                transactionJoinKeys);
    }
}
