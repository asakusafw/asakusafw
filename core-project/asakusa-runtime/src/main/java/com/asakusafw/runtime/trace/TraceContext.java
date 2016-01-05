/**
 * Copyright 2011-2016 Asakusa Framework Team.
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
package com.asakusafw.runtime.trace;

import java.text.MessageFormat;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

/**
 * Represents a context each tracing.
 * @since 0.5.1
 */
public final class TraceContext {

    private final int serialNumber;

    private final String operatorClass;

    private final String operatorMethod;

    private final PortDirection portDirection;

    private final String portName;

    private final Class<?> dataType;

    private final Map<String, String> attributes;

    /**
     * Creates a new instance.
     * @param serialNumber the serial number of target tracepoint (unique in the jobflow)
     * @param operatorClass context operator class
     * @param operatorMethod context operator method
     * @param portDirection context operator port direction
     * @param portName context operator port name
     * @param dataType context data type
     * @param attributes the trace attributes
     * @throws IllegalArgumentException if some parameters were {@code null}
     */
    public TraceContext(
            int serialNumber,
            String operatorClass,
            String operatorMethod,
            PortDirection portDirection,
            String portName,
            Class<?> dataType,
            Map<String, String> attributes) {
        this.serialNumber = serialNumber;
        this.operatorClass = operatorClass;
        this.operatorMethod = operatorMethod;
        this.portDirection = portDirection;
        this.portName = portName;
        this.dataType = dataType;
        this.attributes = Collections.unmodifiableMap(new HashMap<>(attributes));
    }

    /**
     * Creates a new instance.
     * @param serialNumber the serial number of target tracepoint (unique in the jobflow)
     * @param operatorClass context operator class
     * @param operatorMethod context operator method
     * @param portDirection context operator port direction
     * @param portName context operator port name
     * @param dataType context data type
     * @param attributeKeyValuePairs key-value pairs of trace attributes
     * @throws IllegalArgumentException if some parameters were {@code null}
     */
    public TraceContext(
            int serialNumber,
            String operatorClass,
            String operatorMethod,
            PortDirection portDirection,
            String portName,
            Class<?> dataType,
            String... attributeKeyValuePairs) {
        this.serialNumber = serialNumber;
        this.operatorClass = operatorClass;
        this.operatorMethod = operatorMethod;
        this.portDirection = portDirection;
        this.portName = portName;
        this.dataType = dataType;
        if (attributeKeyValuePairs.length % 2 != 0) {
            throw new IllegalArgumentException();
        }
        Map<String, String> map = new HashMap<>();
        for (int i = 0; i < attributeKeyValuePairs.length; i += 2) {
            String key = attributeKeyValuePairs[i + 0];
            String value = attributeKeyValuePairs[i + 1];
            map.put(key, value);
        }
        this.attributes = Collections.unmodifiableMap(map);
    }

    /**
     * Returns the serial number of target tracepoint.
     * The each serial number must be unique in the same jobflow.
     * @return the serial number
     */
    public int getSerialNumber() {
        return serialNumber;
    }

    /**
     * Returns the context operator class name.
     * @return the operator class name
     */
    public String getOperatorClass() {
        return operatorClass;
    }

    /**
     * Returns the context operator method name.
     * @return the operator method
     */
    public String getOperatorMethod() {
        return operatorMethod;
    }

    /**
     * Returns the context operator port direction.
     * @return the port direction
     */
    public PortDirection getPortDirection() {
        return portDirection;
    }

    /**
     * Returns the context operator port name.
     * @return the port name
     */
    public String getPortName() {
        return portName;
    }

    /**
     * Returns the data type of trace target.
     * @return the data type
     */
    public Class<?> getDataType() {
        return dataType;
    }

    /**
     * Returns the trace attributes.
     * @return the attributes
     */
    public Map<String, String> getAttributes() {
        return attributes;
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((attributes == null) ? 0 : attributes.hashCode());
        result = prime * result + ((dataType == null) ? 0 : dataType.hashCode());
        result = prime * result + ((operatorClass == null) ? 0 : operatorClass.hashCode());
        result = prime * result + ((operatorMethod == null) ? 0 : operatorMethod.hashCode());
        result = prime * result + ((portDirection == null) ? 0 : portDirection.hashCode());
        result = prime * result + ((portName == null) ? 0 : portName.hashCode());
        result = prime * result + serialNumber;
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
        TraceContext other = (TraceContext) obj;
        if (serialNumber != other.serialNumber) {
            return false;
        }
        if (dataType == null) {
            if (other.dataType != null) {
                return false;
            }
        } else if (!dataType.equals(other.dataType)) {
            return false;
        }
        if (operatorClass == null) {
            if (other.operatorClass != null) {
                return false;
            }
        } else if (!operatorClass.equals(other.operatorClass)) {
            return false;
        }
        if (operatorMethod == null) {
            if (other.operatorMethod != null) {
                return false;
            }
        } else if (!operatorMethod.equals(other.operatorMethod)) {
            return false;
        }
        if (portDirection != other.portDirection) {
            return false;
        }
        if (portName == null) {
            if (other.portName != null) {
                return false;
            }
        } else if (!portName.equals(other.portName)) {
            return false;
        }
        if (attributes == null) {
            if (other.attributes != null) {
                return false;
            }
        } else if (!attributes.equals(other.attributes)) {
            return false;
        }
        return true;
    }

    @Override
    public String toString() {
        return MessageFormat.format(
                "{0}@{1}#{2}.{3}.{4}:{5}", //$NON-NLS-1$
                String.valueOf(serialNumber),
                operatorClass,
                operatorMethod,
                portDirection,
                portName,
                dataType.getName());
    }

    /**
     * Represents port direction.
     * @since 0.5.1
     */
    public enum PortDirection {

        /**
         * Input port.
         */
        INPUT,

        /**
         * Output port.
         */
        OUTPUT
    }
}