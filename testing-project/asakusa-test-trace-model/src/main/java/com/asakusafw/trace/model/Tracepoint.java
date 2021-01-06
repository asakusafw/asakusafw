/**
 * Copyright 2011-2021 Asakusa Framework Team.
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
package com.asakusafw.trace.model;

import java.text.MessageFormat;

import com.google.gson.annotations.SerializedName;

/**
 * Represents a point where tracing is enabled.
 * @since 0.5.1
 */
public class Tracepoint {

    @SerializedName("operator_class")
    private String operatorClassName;

    @SerializedName("method")
    private String operatorMethodName;

    @SerializedName("kind")
    private PortKind portKind;

    @SerializedName("name")
    private String portName;

    Tracepoint() {
        return;
    }

    /**
     * Creates a new instance.
     * @param operatorClassName the operator class name
     * @param operatorMethodName the operator method name
     * @param portKind the operator port kind
     * @param portName the operator port name
     * @throws IllegalArgumentException if some parameters were {@code null}
     */
    public Tracepoint(
            String operatorClassName, String operatorMethodName,
            PortKind portKind, String portName) {
        this.operatorClassName = operatorClassName;
        this.operatorMethodName = operatorMethodName;
        this.portKind = portKind;
        this.portName = portName;
    }

    /**
     * Returns the target operator class name.
     * @return the operator class name
     */
    public String getOperatorClassName() {
        return operatorClassName;
    }

    /**
     * Returns the target operator method name.
     * @return the operator method name
     */
    public String getOperatorMethodName() {
        return operatorMethodName;
    }

    /**
     * Returns the target port kind.
     * @return the port kind
     */
    public PortKind getPortKind() {
        return portKind;
    }

    /**
     * Returns the target port name.
     * @return the port name
     */
    public String getPortName() {
        return portName;
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((operatorClassName == null) ? 0 : operatorClassName.hashCode());
        result = prime * result + ((operatorMethodName == null) ? 0 : operatorMethodName.hashCode());
        result = prime * result + ((portKind == null) ? 0 : portKind.hashCode());
        result = prime * result + ((portName == null) ? 0 : portName.hashCode());
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
        Tracepoint other = (Tracepoint) obj;
        if (operatorClassName == null) {
            if (other.operatorClassName != null) {
                return false;
            }
        } else if (!operatorClassName.equals(other.operatorClassName)) {
            return false;
        }
        if (operatorMethodName == null) {
            if (other.operatorMethodName != null) {
                return false;
            }
        } else if (!operatorMethodName.equals(other.operatorMethodName)) {
            return false;
        }
        if (portKind != other.portKind) {
            return false;
        }
        if (portName == null) {
            if (other.portName != null) {
                return false;
            }
        } else if (!portName.equals(other.portName)) {
            return false;
        }
        return true;
    }

    @Override
    public String toString() {
        return MessageFormat.format(
                "{0}#{1}-{2}({3})", //$NON-NLS-1$
                operatorClassName,
                operatorMethodName,
                portKind,
                portName);
    }

    /**
     * Represents input/output port kind.
     * @since 0.5.1
     */
    public enum PortKind {

        /**
         * Input port.
         */
        INPUT,

        /**
         * Output port.
         */
        OUTPUT,
        ;

        @Override
        public String toString() {
            return name().toLowerCase();
        }
    }
}
